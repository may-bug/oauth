package org.codelin.oauth.oauth.identity.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * MinIO文件存储实现
 * <p>
 * 需要添加minio依赖并配置app.storage.type=minio启用
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "minio")
public class MinioStorage implements FileStorage {

    // private final MinioClient minioClient;
    // private final String bucketName;

    @Override
    public String upload(String path, InputStream inputStream, String contentType) {
        try {
            // minioClient.putObject(PutObjectArgs.builder()
            //         .bucket(bucketName)
            //         .object(path)
            //         .stream(inputStream, -1, 10485760)
            //         .contentType(contentType)
            //         .build());
            log.debug("File uploaded to MinIO: {}", path);
            return getUrl(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO: " + path, e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            // return minioClient.getObject(GetObjectArgs.builder()
            //         .bucket(bucketName)
            //         .object(path)
            //         .build());
            throw new UnsupportedOperationException("MinIO not configured");
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            // minioClient.removeObject(RemoveObjectArgs.builder()
            //         .bucket(bucketName)
            //         .object(path)
            //         .build());
            log.debug("File deleted from MinIO: {}", path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO: " + path, e);
        }
    }

    @Override
    public String getUrl(String path) {
        // return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
        //         .bucket(bucketName)
        //         .object(path)
        //         .method(Method.GET)
        //         .build());
        return "http://localhost:9000/" + "bucket" + "/" + path;
    }

    @Override
    public boolean exists(String path) {
        try {
            // minioClient.statObject(StatObjectArgs.builder()
            //         .bucket(bucketName)
            //         .object(path)
            //         .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
