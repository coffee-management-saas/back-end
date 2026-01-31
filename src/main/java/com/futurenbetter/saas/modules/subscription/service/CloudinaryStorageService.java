package com.futurenbetter.saas.modules.subscription.service;

public interface CloudinaryStorageService {

    String uploadInvoice(byte[] data, String fileName);

    String uploadFile(byte[] data, String fileName, String folder);
}
