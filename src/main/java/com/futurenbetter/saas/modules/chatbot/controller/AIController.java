package com.futurenbetter.saas.modules.chatbot.controller;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIChatService chatService;

    @PostMapping("/chat")
    Object chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String ingest(@RequestParam("file") MultipartFile file) throws IOException {
        return chatService.ingestFile(file);
    }
}
