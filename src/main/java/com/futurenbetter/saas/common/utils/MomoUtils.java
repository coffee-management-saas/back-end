package com.futurenbetter.saas.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MomoUtils {

    private final ObjectMapper objectMapper;

    public String decodeType(String extraData) {
        try {
            byte[] decodeBytes = Base64.getUrlDecoder().decode(extraData);
            Map<String, Object> data = objectMapper.readValue(decodeBytes, Map.class);
            return (String) data.get("type");
        } catch (Exception e) {
            return null;
        }
    }

    public String hmacSha256(String data, String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo signature: " + e.getMessage());
        }
    }
}