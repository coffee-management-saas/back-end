package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.entity.AIChatHistory;
import com.futurenbetter.saas.modules.chatbot.repository.AIChatHistoryRepository;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;
    private final AIChatHistoryRepository chatHistoryRepository;

    public AIChatServiceImpl(ChatClient.Builder builder, PgVectorStore vectorStore, AIChatHistoryRepository chatHistoryRepository) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public String chat(ChatRequest request) {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.message())
                        .topK(5)
                        .build());

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String aiResponse = chatClient.prompt()
                .options(ChatOptions.builder().build())
                .advisors(advisorSpec -> advisorSpec
                        .param("headers", Map.of("ngrok-skip-browser-warning",true)))
                .system(s -> s.text("Bạn chuyên hỗ trợ tư vấn menu của quán cà phê Futture&Better.\n" +
                        "1. Chỉ sử dụng những thông tin từ 'Ngữ cảnh' để trả lời.\n" +
                        "2. Sử dụng cách trả lời như tư vấn cho khách hàng hài lòng. \n" +
                        "3. Nếu câu hỏi KHÔNG có trong 'Ngữ cảnh' hoặc 'Ngữ cảnh' trống," +
                        " hãy trả lời chính xác câu: 'Xin lỗi, tôi không tìm thấy thông tin này " +
                        "trong menu của quán chúng tôi. \n\n" +
                        "Ngữ cảnh:\n{context}")
                        .param("context", context))
                .user(request.message())
                .call()
                .content();

        AIChatHistory history = AIChatHistory.builder()
                .userMessage(request.message())
                .aiResponse(aiResponse)
                .retrievedContext(context)
                .createdAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(history);
        return aiResponse;
    }

    @Override
    @Transactional
    public void ingestData(String content) {
        if (content == null || content.isBlank()) return;
        String sanitizedContent = content.replace("\u0000", "");
        Document fullDoc = new Document(sanitizedContent);

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.split(List.of(fullDoc));
        vectorStore.add(chunks);
    }
}
