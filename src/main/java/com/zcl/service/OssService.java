package com.zcl.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 阿里云 OSS 服务
 * 提供后端代理上传能力
 */
@Service
public class OssService {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.bucket}")
    private String bucket;

    @Value("${oss.accessKeyId}")
    private String accessKeyId;

    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    private static final String UPLOAD_DIR = "product-images/";

    /**
     * 上传文件到 OSS
     *
     * @param inputStream 文件输入流
     * @param originalName 原始文件名
     * @return 图片的完整访问 URL
     */
    public String uploadFile(InputStream inputStream, String originalName) {
        String ext = "jpg";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        String objectKey = UPLOAD_DIR + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "." + ext;

        // OSS SDK 要求 endpoint 不带 https:// 前缀
        String cleanEndpoint = endpoint.replace("https://", "").replace("http://", "").trim();
        OSS ossClient = new OSSClientBuilder().build(
                cleanEndpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucket, objectKey, inputStream);
            return "https://" + bucket + "." + cleanEndpoint + "/" + objectKey;
        } finally {
            ossClient.shutdown();
        }
    }
}
