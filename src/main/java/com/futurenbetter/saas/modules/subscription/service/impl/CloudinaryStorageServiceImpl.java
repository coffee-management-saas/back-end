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
        try {
            String publicId = "invoices/" + fileName;

            Map uploadResult = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                   "public_id", publicId,
                    "resources_type", "raw",
                    "flags", "attachment"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload lên Cloudinary", e);
        }
    }
}
