package com.example.pract5;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {
    private String uploadPath = "/upload-files";

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public List<FileInfo> getAllFiles() {
        return fileInfoRepository.findAll();
    }

    public File downloadFile(String fileId, String fileName) throws IOException {
        Optional<FileInfo> fileInfo = fileInfoRepository.findById(fileId + fileName);
        if (fileInfo.isEmpty()) throw new FileNotFoundException("No file with id " + fileId);
        Path filePath = Paths.get(uploadPath, fileInfo.get().getId(), fileInfo.get().getName());
        return filePath.toFile();
    }

    public void uploadFile(byte[] fileBytes, String fileName) throws IOException {
        String id = UUID.randomUUID() + fileName;
        Files.createDirectories(Paths.get(uploadPath, id));
        try (OutputStream os = new FileOutputStream(Paths.get(uploadPath, id, fileName).toFile())) {
            os.write(fileBytes);
        }

        fileInfoRepository.save(new FileInfo(id, fileName, new Date()));
    }

    public void deleteFile(String fileId, String fileName) throws IOException {
        if (!fileInfoRepository.existsById(fileId + fileName))
            throw new FileNotFoundException("No file with id " + fileId + fileName);

        Path dirPath = Paths.get(uploadPath, fileId);

        try {
            Files.deleteIfExists(dirPath.resolve(fileName));
        } catch (IOException e) {
            throw new IOException("Failed to delete file or directory for id " + fileId + fileName);
        }

        try {
            Files.deleteIfExists(dirPath);
        } finally {
            fileInfoRepository.deleteById(fileId + fileName);
        }
    }
}
