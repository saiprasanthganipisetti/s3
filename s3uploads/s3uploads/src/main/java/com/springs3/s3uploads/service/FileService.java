package com.springs3.s3uploads.service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class FileService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    public AmazonS3 s3Client;

    public String uploadFile(MultipartFile files) {
        File fileObj = convertMultiPartFileToFile(files);
//        long n = files.getSize();
        String fileName = System.currentTimeMillis() + "_" + files.getOriginalFilename();
//        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
//        fileObj.delete();
        Map<String, String> map = new HashMap<>();
        TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client)
                .withMultipartUploadThreshold((long) (50 * 1024 * 1025)).build();
        Upload result = tm.upload(bucketName, fileName, fileObj);
        return "File uploaded : " + fileName;
    }


    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed ...";
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }
}

