package mercury.glue.step;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ZipUtils {

    public static void main(String[] args) throws IOException, URISyntaxException {
        modifyManifestInZip("plugin/plugin_success.zip", new HashMap<>());
    }

    /**
     * Copies original zip file into temp file and modifies {@code manifest.json} if it is presented in zip.
     * Updates plugin id in {@code manifest.json} and increases version if needed.
     * @param zipName name of zip file
     * @param fields a Map of key string to replace with the value string
     * @return path for modified file
     * @throws IOException
     * @throws URISyntaxException
     */
    static Path modifyManifestInZip(String zipName, Map<String, String> fields) throws IOException, URISyntaxException {
        Path tempPath = createTempFileForZip(zipName);
        try (FileSystem fs = FileSystems.newFileSystem(tempPath, null)) {
            Path source = fs.getPath("/manifest.json");
            if (!Files.exists(source)) {
                return tempPath;
            }
            Path temp = fs.getPath("/___manifest___.json");
            if (Files.exists(temp)) {
                throw new IOException("temp file exists, generate another name");
            }
            Files.move(source, temp);
            streamCopy(temp, source, fields);
            Files.delete(temp);
        }
        return tempPath;
    }

    private static Path createTempFileForZip(String zipName) throws IOException, URISyntaxException {
        Path tempPath = Files.createTempFile("citrusPlugin-", ".zip");
        URL url = ZipUtils.class.getClassLoader().getResource(zipName);
        Path sourcePath = Paths.get(url.toURI());
        Files.copy(sourcePath, tempPath, StandardCopyOption.REPLACE_EXISTING);
        return tempPath;
    }

    private static void streamCopy(Path src, Path dst, Map<String, String> fields) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(src), Charset.forName("UTF-8")));
             BufferedWriter bw = new BufferedWriter(
                     new OutputStreamWriter(Files.newOutputStream(dst), Charset.forName("UTF-8")))) {

            String line;
            while ((line = br.readLine()) != null) {

                for (Map.Entry<String, String> entry : fields.entrySet()) {

                    if (line.contains(entry.getKey())) {
                        line = line.replace(entry.getKey(), entry.getValue());
                        fields.remove(entry.getKey());
                        break;
                    }

                }
                bw.write(line);
                bw.newLine();
            }
        }
    }
    /**
     * Copies original zip file into temp file and modifies {@code package.json} if it is presented in zip.
     * Updates plugin id in {@code package.json} and increases version if needed.
     * @param zipName name of zip file
     * @param fields a Map of key string to replace with the value string
     * @return path for modified file
     * @throws IOException
     * @throws URISyntaxException
     */
    static Path modifyPackageInZip(String zipName, Map<String, String> fields) throws IOException, URISyntaxException {
        Path tempPath = createTempFileForZip(zipName);
        try (FileSystem fs = FileSystems.newFileSystem(tempPath, null)) {
            Path source = fs.getPath("/package.json");
            if (!Files.exists(source)) {
                return tempPath;
            }
            Path temp = fs.getPath("/___package___.json");
            if (Files.exists(temp)) {
                throw new IOException("temp file exists, generate another name");
            }
            Files.move(source, temp);
            streamCopy(temp, source, fields);
            Files.delete(temp);
        }
        return tempPath;
    }
}
