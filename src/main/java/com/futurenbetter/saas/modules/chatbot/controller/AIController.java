package com.futurenbetter.saas.modules.chatbot.controller;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIChatService chatService;

    @PostMapping("/chat")
    String chat(ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String ingest(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String content = "";

        if (fileName != null && fileName.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                content = extractor.getText();
            }
        } else {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        if (content.isBlank()) {
            return "Tệp trống hoặc không thể đọc nội dung.";
        }
        chatService.ingestData(content);
        return "Đã nạp tài liệu từ file " + fileName + " thành công";
    }
}
