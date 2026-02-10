package com.project.NutriTracker.service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloudStorageService {

    private final Storage storage;
    private final String bucketName;

    public CloudStorageService(@Value("${app.gcs.bucket.name}") String bucketName) throws IOException {
        this.bucketName = bucketName;

        // Initialize Google Cloud Storage with credentials
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource("vision-account-key.json").getInputStream());

        this.storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        log.info("Cloud Storage Service initialized with bucket: {}", bucketName);
    }

    /**
     * Upload a file to Google Cloud Storage
     * 
     * @param file   The file to upload
     * @param folder The folder path within the bucket (e.g., "meals", "profiles")
     * @return The public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = folder + "/" + UUID.randomUUID().toString() + extension;

        // Create blob
        BlobId blobId = BlobId.of(bucketName, filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        // Upload file
        Blob blob = storage.create(blobInfo, file.getBytes());

        log.info("File uploaded successfully: {}", filename);

        // Return public URL
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, filename);
    }

    /**
     * Upload byte array to Google Cloud Storage
     * 
     * @param bytes       The byte array to upload
     * @param folder      The folder path within the bucket
     * @param contentType The content type (e.g., "image/jpeg")
     * @return The public URL of the uploaded file
     */
    public String uploadBytes(byte[] bytes, String folder, String contentType) throws IOException {
        // Generate unique filename
        String extension = contentType.contains("jpeg") ? ".jpg" : contentType.contains("png") ? ".png" : ".bin";
        String filename = folder + "/" + UUID.randomUUID().toString() + extension;

        // Create blob
        BlobId blobId = BlobId.of(bucketName, filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        // Upload bytes
        Blob blob = storage.create(blobInfo, bytes);

        log.info("Bytes uploaded successfully: {}", filename);

        // Return public URL
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, filename);
    }

    /**
     * Delete a file from Google Cloud Storage
     * 
     * @param fileUrl The public URL of the file to delete
     * @return true if deleted successfully
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf(bucketName + "/") + bucketName.length() + 1);

            BlobId blobId = BlobId.of(bucketName, filename);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("File deleted successfully: {}", filename);
            } else {
                log.warn("File not found for deletion: {}", filename);
            }

            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file: {}", fileUrl, e);
            return false;
        }
    }

    // /**
    // * Generate a signed URL for temporary access to a file
    // *
    // * @param fileUrl The public URL of the file
    // * @param durationMinutes How long the signed URL should be valid
    // * @return The signed URL
    // */
    // public String generateSignedUrl(String fileUrl, int durationMinutes) {
    // try {
    // String filename = fileUrl.substring(fileUrl.lastIndexOf(bucketName + "/") +
    // bucketName.length() + 1);
    // BlobId blobId = BlobId.of(bucketName, filename);

    // Blob blob = storage.get(blobId);
    // if (blob == null) {
    // throw new IllegalArgumentException("File not found: " + filename);
    // }

    // return blob.signUrl(durationMinutes, TimeUnit.MINUTES).toString();
    // } catch (Exception e) {
    // log.error("Error generating signed URL: {}", fileUrl, e);
    // throw new RuntimeException("Failed to generate signed URL", e);
    // }
    // }
}
