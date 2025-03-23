package com.itextos.beacon.platform.mysqltabledatadump;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class FolderCompressor {
    public static void compressFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        Path sourceFolder = Paths.get(sourceFolderPath);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            Files.walk(sourceFolder).forEach(path -> {
                try {
                    String zipEntryName = sourceFolder.relativize(path).toString();
                    if (Files.isDirectory(path)) {
                        return;
                    }
                    zos.putNextEntry(new ZipEntry(zipEntryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) {
        String sourceFolder = "path/to/source/folder"; // Change to the folder you want to compress
        String zipFile = "path/to/destination.zip";    // Change to desired output zip file
        try {
            compressFolder(sourceFolder, zipFile);
            System.out.println("Folder compressed successfully!");
        } catch (IOException e) {
            System.err.println("Error compressing folder: " + e.getMessage());
        }
    }
}

