package com.futurenbetter.saas.modules.subscription.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.futurenbetter.saas.modules.subscription.service.CloudinaryStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageServiceImpl implements CloudinaryStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadInvoice(byte[] data, String fileName) {
        return uploadFile(data, fileName, "invoices");
    }

    @Override
    public String uploadFile(byte[] data, String fileName, String folder) {
        try {
            Map uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", fileName,
                    "resource_type", "auto"
            );

            Map uploadResult = cloudinary.uploader().upload(data, uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload hình ảnh");
        }
    }
}
