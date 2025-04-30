package com.itextos.beacon.mysqlimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FolderCompressor {
	
	
	
	public static void uncompressFile(String zipFilePath,String destDir) {
    
        File dir = new File(destDir);
        // Create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zis.getNextEntry();

            while (entry != null) {
                File newFile = newFile(dir, entry);
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // Make parent directories if needed
                    new File(newFile.getParent()).mkdirs();

                    // Write file content
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                        	fos.write(buffer, 0, len); // ✅ Correct: offset = 0, length = len
                            
                        }
                    }
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }

            System.out.println("Unzip completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
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

    
    // Helper method to safely create a file from a zip entry
    private static File newFile(File destDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destDir, zipEntry.getName());

        // Prevent Zip Slip vulnerability
        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
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

