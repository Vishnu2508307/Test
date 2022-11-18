package com.smartsparrow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilesTest {

    private File file;
    private String folderName = "foo";

    @BeforeEach
    void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("screen-master.zip");
        if (url != null) {
            file = new File(url.getFile());
        }
    }

    @Test
    void save() throws IOException {
        String fileName = "bar.zip";
        File created = Files.saveZip(file, folderName, fileName);
        assertNotNull(created);
        java.nio.file.Files.delete(Paths.get(created.getAbsolutePath()));
    }

    @Test
    void unzip() throws IOException {
        Map<String, File> files = Files.unzip(file, folderName);
        assertEquals(10, files.size());

        deleteAll(files);
    }

    @Test
    void deleteAll_singleFile() throws IOException {
        Path created = java.nio.file.Files.createTempFile(null, null);
        Files.deleteAll(new File(created.toAbsolutePath().toString()));
    }

    @Test
    void deleteAll_recursively() throws IOException {
        Path dir = java.nio.file.Files.createTempDirectory(null);
        final Path created = java.nio.file.Files.createTempFile(dir, null, null);
        Files.deleteAll(new File(dir.toAbsolutePath().toString()));

        assertThrows(FileNotFoundException.class, ()-> {
            File file = new File(created.toAbsolutePath().toString());
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                assertNull(fileInputStream);
            }
        });
    }

    private void deleteAll(Map<String, File> files) throws IOException {
        for (File current : files.values()) {
            deleteRecursively(current);
        }
    }

    private void deleteRecursively(File currentFile) throws IOException {
        File parent = currentFile.getParentFile();
        java.nio.file.Files.delete(Paths.get(currentFile.getAbsolutePath()));

        while (parent != null) {
            String[] files = parent.list();
            if (files != null && files.length == 0) {
                java.nio.file.Files.delete(Paths.get(parent.getAbsolutePath()));
            }
            parent = parent.getParentFile();
        }
    }
}
