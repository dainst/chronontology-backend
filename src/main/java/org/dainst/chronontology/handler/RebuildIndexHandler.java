package org.dainst.chronontology.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.dainst.chronontology.handler.dispatch.Dispatcher;
import org.dainst.chronontology.store.Datastore;
import org.dainst.chronontology.store.ElasticsearchDatastore;
import org.dainst.chronontology.store.FilesystemDatastore;
import org.dainst.chronontology.util.JsonUtils;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;

import static org.dainst.chronontology.Constants.*;

/**
 * Created by Simon Hohl on 04.07.17.
 */
public class RebuildIndexHandler implements Handler {

    protected final Dispatcher dispatcher;
    private final String[] types;
    private final static Logger logger = Logger.getLogger(RebuildIndexHandler.class);

    public RebuildIndexHandler(Dispatcher dispatcher, String[] types) {
        this.dispatcher= dispatcher;
        this.types = types;
    }

    @Override
    public Object handle(Request req, Response res) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = getClass().getClassLoader();

        JsonNode result= JsonUtils.json();
        //Results datastores= new Results("datastores");
        Datastore[] datastores = dispatcher.getDatastores();

        ElasticsearchDatastore esDatastore = (ElasticsearchDatastore) dispatcher.getDatastoreByClass(ElasticsearchDatastore.class);
        FilesystemDatastore fsDatastore = (FilesystemDatastore) dispatcher.getDatastoreByClass(FilesystemDatastore.class);

        if(esDatastore == null || fsDatastore == null) {
            res.status(HTTP_BAD_REQUEST);
            ((ObjectNode)result).put("status","failure, simple dispatcher mode active.");
            return result;
        }

        esDatastore.clearIndex();
        esDatastore.initializeIndex();

        for(String type : types){
            // TODO: mapping.json in resources folder per Type?
            JsonNode mapping = mapper.readTree(new File(classLoader.getResource("mapping.json").getFile()));
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
