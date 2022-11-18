package com.smartsparrow.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A util class to perform different file operations. All the path supplied as argument in any
 * of the public method supplied by this class are relative to the system temporary folder.
 */
public class Files {

    private static final Logger log = LoggerFactory.getLogger(Files.class);
    private static final String destination = System.getProperty("java.io.tmpdir");
    private static final int BUFFER_SIZE = 1024;

    /**
     * Save the zipped {@link File} to the system temporary folder.
     * as the supplied format.
     *
     * @param file       the file to be saved
     * @param folderName the name of the sub-directory where to save the file
     * @param fileName   the name of the file
     * @return the {@link File} saved
     * @throws IOException when any I/O operation fails
     */
    public static File saveZip(File file, String folderName, String fileName) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            return _save(zipInputStream, folderName, fileName);
        }
    }

    /**
     * Decompress a zip archive
     *
     * @param file         the file to unzip
     * @param targetFolder the folder name where the archive will be unzipped
     * @return a {@link Map} of file names and File instances (directories is not included).
     */
    public static Map<String, File> unzip(File file, String targetFolder) throws IOException {
        checkArgument(file != null, "file required");

        Map<String, File> toUpload = new HashMap<>();
        String destinationFolder = getBasePath(targetFolder);
        File dir = new File(destinationFolder);
        makeDir(dir);

        try (FileInputStream fileInputStream = new FileInputStream(file);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destinationFolder + File.separator + zipEntry.getName());

                makeDir(newFile.getParentFile());

                if (!zipEntry.isDirectory()) {
                    toUpload.put(zipEntry.getName(), newFile);
                }

                decompress(zipInputStream, zipEntry, newFile);

                zipEntry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            log.error(String.format("error while unzipping file `%s`", file.getAbsolutePath()), e);
            throw e;
        }
        // zipInputStream.closeEntry()
        // honestly I am not sure should we close zip entry in the end or not
        // looks like it's only needed to do before reading next entry

        return toUpload;
    }

    /**
     * Recursively delete the supplied file
     *
     * @param file the file to delete
     * @throws IOException when any I/O operation fails
     */
    public static void deleteAll(File file) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("About to delete {}", file.getAbsolutePath());
        }

        String[] children = file.list();

        if (children != null) {

            if (children.length > 0) {
                for (String child : children) {
                    deleteAll(new File(file.getAbsolutePath() + File.separator + child));
                }
            }
        }

        // just delete the file
        java.nio.file.Files.delete(Paths.get(file.getAbsolutePath()));
    }

    /**
     * Check if the supplied {@link File} is a zip file.
     *
     * @param file the file to check
     * @return true if is a {@link ZipInputStream} <br/> false if it is not a zipped stream
     * @throws IOException re-thrown from the getNextEntry method
     */
    public static boolean isAZip(File file) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            return zis.getNextEntry() != null;
        }
    }

    /**
     * Save a zipped input stream into a file. The method read each {@link ZipEntry} and write it to the
     * {@link ZipOutputStream}
     *
     * @param zipInputStream the stream to save
     * @param folderName     the folder name where to save the zip archive
     * @param fileName       the name of the zipped archive
     * @return the {@link File} saved
     * @throws IOException when any I/O operation fails
     */
    private static File _save(ZipInputStream zipInputStream, String folderName, String fileName) throws IOException {
        String destinationFolder = getBasePath(folderName);
        File dir = new File(destinationFolder);
        makeDir(dir);
        File file = new File(destinationFolder + File.separator + fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("entry name {}", zipEntry.getName());
                }
                zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                int length;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((length = zipInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }

                zipEntry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            log.error(String.format("error to save zip folderName='%s' fileName='%s'", folderName, fileName), e);
            throw e;
        }
        return file;
    }

    /**
     * Decompress a zip entry and write the new file from the input stream
     *
     * @param inputStream the source input stream
     * @param zipEntry    the current zip entry
     * @param newFile     the new file to write to
     */
    private static void decompress(InputStream inputStream, ZipEntry zipEntry, File newFile) throws IOException {
        if (zipEntry.isDirectory()) {
            makeDir(newFile);
        } else {
            writeFile(inputStream, newFile);
        }
    }

    /**
     * Write the content of a new file from the supplied input stream
     *
     * @param inputStream the source input stream
     * @param newFile     the new file to write to
     */
    private static void writeFile(InputStream inputStream, File newFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            log.error(String.format("Error writing to %s", newFile.getName()), e);
            throw e;
        }
    }

    /**
     * Create a directory if this doesn't exist yet
     *
     * @param newFile the new file from which the directory will be created
     */
    private static void makeDir(File newFile) {
        if (!newFile.exists()) {
            boolean created = newFile.mkdirs();
            if (!created) {
                log.warn("Could not create folder {}", newFile.getAbsolutePath());
            }
        }
    }

    /**
     * Returns the base path given a folder name. The folder name is always relative to the system temporary folder
     *
     * @param folderName the folder name to attach to the destination path
     * @return a string
     */
    private static String getBasePath(String folderName) {
        return destination + File.separator + folderName;
    }
}
