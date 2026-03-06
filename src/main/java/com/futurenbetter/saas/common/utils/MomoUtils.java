package com.futurenbetter.saas.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MomoUtils {

    private final ObjectMapper objectMapper;

    public Map<String, Object> decodeExtraData(String extraData) {
        if (extraData == null || extraData.isEmpty())
            return null;
        try {
            String decodedString = extraData;
            if (extraData.contains("%")) {
                decodedString = java.net.URLDecoder.decode(extraData, StandardCharsets.UTF_8);
            }
            byte[] decodeBytes = Base64.getDecoder().decode(decodedString);
            return objectMapper.readValue(decodeBytes, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String decodeType(String extraData) {
        Map<String, Object> data = decodeExtraData(extraData);
        return data != null ? (String) data.get("type") : null;
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