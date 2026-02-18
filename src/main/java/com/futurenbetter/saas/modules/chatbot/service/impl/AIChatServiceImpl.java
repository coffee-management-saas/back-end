package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.dto.response.AIOrderResponse;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import com.futurenbetter.saas.modules.chatbot.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;
    private final ChatHistoryService chatHistoryService;

    public AIChatServiceImpl(ChatClient.Builder builder,
            PgVectorStore vectorStore,
            ChatHistoryService chatHistoryService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.chatHistoryService = chatHistoryService;
    }

    @Override
    public Object chat(ChatRequest request) {
        // Tối ưu: Giảm TopK xuống 3 để giảm lượng token đầu vào -> AI xử lý nhanh hơn
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.message())
                        .topK(3)
                        .build());

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        BeanOutputConverter<AIOrderResponse> converter = new BeanOutputConverter<>(AIOrderResponse.class);

        String systemPrompt = """
                Bạn là NV Future&Better. Trả lời ngắn gọn, dùng emoji ☕.

                MENU:
                {context}

                QUY TẮC PHẢN HỒI:
                1. Mặc định action="INFO".
                2. Khi khách hỏi menu:
                   - CHỈ liệt kê 1-2 món đặc trưng nhất của mỗi loại (Category).
                   - KHÔNG liệt kê toàn bộ menu.
                   - Sau đó hỏi khách thích loại nào để tư vấn thêm.
                3. Từ chối trả lời nhg thông tin không liên quan đến cửa hàng.
                3. KHÔNG trả về dữ liệu 'orderRequest' khi action="INFO".
                4. CHỈ khi khách xác nhận "Chốt đơn" / "Đặt món này":
                   - Mới trả về action="ORDER" và kèm 'orderRequest'.

                OUTPUT JSON: {format}
                """;

        String rawResponse = chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                        .param("context", context)
                        .param("format", converter.getFormat()))
                .user(request.message())
                .call()
                .content();

        String sanitizedResponse = rawResponse.replaceAll("```json", "").replaceAll("```", "").trim();

        AIOrderResponse aiResult;
        try {
            aiResult = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS
                            .mappedFeature(), true)
                    .readValue(sanitizedResponse, AIOrderResponse.class);
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", sanitizedResponse, e);
            // Fallback nếu parse lỗi
            aiResult = new AIOrderResponse("INFO", rawResponse, null);
        }

        // Lưu lịch sử (Async đã được handle bởi @Async trong ChatHistoryService)
        chatHistoryService.saveHistory(request.message(), aiResult.message(), context);

        return aiResult;
    }

    @Override
    @Transactional
    public String ingestFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String content = "";

        if (fileName != null) {
            if (fileName.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                        XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    content = extractor.getText();
                }
            } else if (fileName.endsWith(".xlsx")) {
                try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
                    StringBuilder sb = new StringBuilder();
                    for (org.apache.poi.ss.usermodel.Sheet sheet : workbook) {
                        sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                        for (org.apache.poi.ss.usermodel.Row row : sheet) {
                            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                                switch (cell.getCellType()) {
                                    case STRING -> sb.append(cell.getStringCellValue()).append("\t");
                                    case NUMERIC -> sb.append(cell.getNumericCellValue()).append("\t");
                                    case BOOLEAN -> sb.append(cell.getBooleanCellValue()).append("\t");
                                    case FORMULA -> sb.append(cell.getCellFormula()).append("\t");
                                    default -> sb.append("").append("\t");
                                }
                            }
                            sb.append("\n");
                        }
                        sb.append("\n");
                    }
                    content = sb.toString();
                }
            } else {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        }

        if (content.isBlank()) {
            return "Tệp trống hoặc không thể đọc nội dung.";
        }

        ingestData(content);
        return "Đã nạp tài liệu từ file " + fileName + " thành công";
    }

    @Override
    @Transactional
    public void ingestData(String content) {
        if (content == null || content.isBlank())
            return;
        String sanitizedContent = content.replace("\u0000", "");

        log.info("Starting summary generation...");
        String summaryInput = sanitizedContent.length() > 3000 ? sanitizedContent.substring(0, 3000) : sanitizedContent;

        String summaryPrompt = "Hãy tóm tắt nội dung sau thành một danh sách menu ngắn gọn (chỉ gồm tên món và loại): \n"
                + summaryInput;
        String summary = chatClient.prompt().user(summaryPrompt).call().content();
        log.info("Summary generation complete.");

        Document summaryDoc = new Document(summary, Map.of("type", "MENU_SUMMARY"));
        vectorStore.add(List.of(summaryDoc));

        log.info("Starting content chunking and embedding...");
        Document fullDoc = new Document(sanitizedContent, Map.of("type", "MENU_DETAIL"));
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.split(List.of(fullDoc));

        chunks.forEach(chunk -> chunk.getMetadata().put("type", "MENU_DETAIL"));

        vectorStore.add(chunks);
        log.info("Ingestion complete. Added {} chunks.", chunks.size());
    }
}
