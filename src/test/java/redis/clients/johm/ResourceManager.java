package redis.clients.johm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Hashtable;

public class ResourceManager {
    private static Hashtable<String, String> fileCache = new Hashtable<String, String>();

    public static String extract(String resourcePath) {
        if(fileCache.contains(resourcePath)) {
            return fileCache.get(resourcePath);
        }

        URL resource = ResourceManager.class.getResource(resourcePath);
        try {
            File tempFilePath = File.createTempFile(
                    FilenameUtils.getBaseName(resource.getFile()),
                    "." + FilenameUtils.getExtension(resource.getFile()));

            InputStream resourceStream = resource.openStream();
            OutputStream tempfileStream = FileUtils.openOutputStream(tempFilePath);
            IOUtils.copy(resourceStream, tempfileStream);

            IOUtils.closeQuietly(resourceStream);
            IOUtils.closeQuietly(tempfileStream);

            fileCache.put(resourcePath, tempFilePath.getAbsolutePath());
            return tempFilePath.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }
}
