package org.dainst.chronontology.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Key value store where the values are of type JsonNode
 * and get stored on a file system.
 *
 * @author Daniel M. de Oliveira
 */
public class FileSystemDatastore implements VersionedDatastore {

    final static Logger logger = Logger.getLogger(FileSystemDatastore.class);

    private static final String EXT = ".txt";
    private final String baseFolder;

    public FileSystemDatastore(String baseFolder) {
        this.baseFolder=baseFolder;
    }


    @Override
    public JsonNode get(String bucket, String key, Integer version) {

        File dir= dirPath(bucket,key).toFile();
        if (!dir.exists()) return null;
        File latest= getLatest(dir);
        if (latest==null) return null;

        String content= "";
        try {
            content = new String(Files.readAllBytes(
                    filePath(bucket,key,latest.getName())));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }

        JsonNode n= null;
        try {
            n = new ObjectMapper().readTree(content);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        return n;
    }


    /**
     * @param key
     * @return null if there exists no item for key.
     */
    @Override
    public JsonNode get(
            final String bucket,
            final String key) {

        return get(bucket,key,null);
    };

    private Path filePath(final String typeName, final String key,String filename) {
        return Paths.get(dirPath(typeName,key).toString(),filename);
    }

    private Path dirPath(final String typeName,final String key) {
        return Paths.get(baseFolder,typeName,key);
    }


    /**
     * Scans the dir for files matching the number + extension
     * pattern and returns the file with the highest number in its
     * filename.
     *
     * @param dir
     * @return null if no files matching the pattern are found.
     */
    private File getLatest(File dir) {
        File[] files=
            dir.listFiles(new FilenameFilter() {
                    public boolean accept( File dir, String name ) {
                        return name.matches( "\\d+\\"+EXT );
                    }}
                );

        Arrays.sort(files, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1);
                int n2 = extractNumber(o2);
                return n1 - n2;
            }

            private int extractNumber(File f) {
                int nr= Integer.parseInt(FilenameUtils.getBaseName(f.getName()));
                return nr;
            }
        });

        if (files.length==0) return null;
        return files[files.length-1];
    }

    /**
     * Takes a file which is assumed to follow the number + extension pattern,
     * and derives a new filename by taking the basename of <code>latest</code>
     * and incrementing its number by one.
     *
     * @param latest
     * @return derived filename.
     */
    private String oneUp(File latest) {
        if (latest==null) return "1"+EXT;
        int num= Integer.parseInt(FilenameUtils.getBaseName(latest.getName()));
        return ++num+EXT;
    }

    /**
     * Stores or updates the item for key.
     * @param key
     * @param value
     */
    public boolean put(
            final String typeName,
            String key,
            JsonNode value) {

        File dir= dirPath(typeName,key).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            Files.write( filePath(typeName,key,oneUp(getLatest(dir)))
                    , value.toString().getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void remove(String bucket, String key) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isConnected() {
        // This is sufficient for the moment.
        return true;
    }


}
