package org.dainst.chronontology.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;

import static org.dainst.chronontology.Constants.*;

/**
 * @author Simon Hohl
 *
 */
public class UpdateMappingHandler implements Handler {

    protected final Dispatcher dispatcher;
    private final String[] types;
    private final static Logger logger = Logger.getLogger(UpdateMappingHandler.class);

    public UpdateMappingHandler(Dispatcher dispatcher, String[] types) {
        this.dispatcher= dispatcher;
        this.types = types;
    }

    @Override
    public Object handle(Request req, Response res) throws IOException {
        JsonNode mapping = JsonUtils.json(req.body());
        JsonNode result= JsonUtils.json();

        if(mapping == null) {
            res.status(HTTP_BAD_REQUEST);
            ((ObjectNode)result).put("status","failure");
            logger.info("User tried to update mapping, but not provide any in POST request.");
            return result;
        }

        ElasticsearchDatastore esDatastore = (ElasticsearchDatastore) dispatcher.getDatastoreByClass(ElasticsearchDatastore.class);
        if(esDatastore == null) {
            res.status(HTTP_BAD_REQUEST);
            ((ObjectNode)result).put("status","failure");
            logger.info("No elastic search datastore found for mapping update.");
            return result;
        }

        FilesystemDatastore fsDatastore = (FilesystemDatastore) dispatcher.getDatastoreByClass(FilesystemDatastore.class);
        if(fsDatastore == null) {
            res.status(HTTP_BAD_REQUEST);
            ((ObjectNode)result).put("status","failure");
            logger.info("No filesystem datastore found for mapping update.");
            return result;
        }

        esDatastore.clearIndex();
        esDatastore.initializeIndex();

        for(String type : types){
            esDatastore.postMapping(type, mapping);
        }

        rebuildElasticSearchIndexFromFilesystem(fsDatastore, esDatastore);

        res.status(HTTP_OK);

        ((ObjectNode)result).put("status","success");

        return result;
    }

    private JsonNode rebuildElasticSearchIndexFromFilesystem(FilesystemDatastore fsDatastore, ElasticsearchDatastore esDatastore) throws JsonProcessingException {
        JsonNode result= JsonUtils.json();

        File baseFolder = new File(fsDatastore.getBaseFolderName());

        for(File typeFolder : baseFolder.listFiles()) {
            for(File documentFolder: typeFolder.listFiles()){
                esDatastore.put(
                        typeFolder.getName(),
                        documentFolder.getName(),
                        fsDatastore.get(typeFolder.getName(), documentFolder.getName())
                );
            }
        }

        return result;
    }
}
