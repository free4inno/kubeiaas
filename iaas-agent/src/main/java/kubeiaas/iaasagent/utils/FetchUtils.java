package kubeiaas.iaasagent.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Slf4j
public class FetchUtils {
    public static final String tmpFilePrefix = "fetch";
    public static final String tmpFileSuffix = ".tmp";

    public static Optional<File> fetchFile(URL url, String dirPath, String fileName) throws IOException {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return Optional.empty();
        }
        File targetFile = new File(dir.getAbsolutePath() + File.separator + fileName);
        if (targetFile.exists()) {
            return Optional.of(targetFile);
        }
        File tmpFile = File.createTempFile(tmpFilePrefix, tmpFileSuffix, dir);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(tmpFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        boolean success = tmpFile.renameTo(targetFile);
        if (!success) {
            return Optional.empty();
        } else {
            return Optional.of(targetFile);
        }
    }
}
