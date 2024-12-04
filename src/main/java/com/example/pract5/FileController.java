package com.example.pract5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileInfo>> getAllFiles() {
        List<FileInfo> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId, @PathVariable String fileName) {
        try {
            File file = fileService.downloadFile(fileId, fileName);
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)))
                    .body(fileBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileService.uploadFile(file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @DeleteMapping("/{fileId}/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId, @PathVariable String fileName) {
        try {
            fileService.deleteFile(fileId, fileName);
            return ResponseEntity.ok("File deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
