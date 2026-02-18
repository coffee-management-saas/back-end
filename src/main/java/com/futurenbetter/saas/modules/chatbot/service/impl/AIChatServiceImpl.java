package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.dto.response.AIOrderResponse;
import com.futurenbetter.saas.modules.chatbot.entity.AIChatHistory;
import com.futurenbetter.saas.modules.chatbot.repository.AIChatHistoryRepository;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.modules.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;
    private final AIChatHistoryRepository chatHistoryRepository;
    private final OrderService orderService;

    public AIChatServiceImpl(ChatClient.Builder builder,
            PgVectorStore vectorStore,
            AIChatHistoryRepository chatHistoryRepository,
            OrderService orderService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.chatHistoryRepository = chatHistoryRepository;
        this.orderService = orderService;
    }

    @Override
    public Object chat(ChatRequest request) {
        String intent = classifyIntent(request.message());
        String filterExpression;

        if ("SUMMARY".equals(intent)) {
            filterExpression = "type == 'MENU_SUMMARY'";
        } else {
            filterExpression = "type == 'MENU_DETAIL'";
        }

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.message())
                        .topK(5)
                        .filterExpression(filterExpression)
                        .build());

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        BeanOutputConverter<AIOrderResponse> converter = new BeanOutputConverter<>(AIOrderResponse.class);

        String systemPrompt = """
                Bạn là trợ lý ảo của quán cà phê Future&Better.
                Nhiệm vụ: Tư vấn menu và hỗ trợ đặt món dựa trên thông tin được cung cấp.

                QUY TẮC TRẢ LỜI:
                1. Dựa hoàn toàn vào thông tin trong phần 'Ngữ cảnh' để trả lời.
                2. Nếu khách muốn đặt món, hãy trích xuất thông tin món ăn và số lượng để tạo đơn hàng.
                3. Nếu khách hỏi menu (tổng quan):
                   - Hãy liệt kê một vài món tiêu biểu của từng loại (Category).
                   - SAU ĐÓ, phải đặt câu hỏi ngược lại cho khách: "Bạn thích loại đồ uống nào (cà phê, trà, hay đá xay) để mình tư vấn thêm nhé?"
                4. Nếu khách hỏi chi tiết hoặc chat bình thường, trả về action "INFO" và câu trả lời ngắn gọn, đúng trọng tâm.

                NGỮ CẢNH:
                {context}

                ĐỊNH DẠNG TRẢ VỀ (JSON):
                {format}
                """;

        AIOrderResponse aiResult = chatClient.prompt()
                .options(ChatOptions.builder().build())
                .advisors(advisorSpec -> advisorSpec.param("headers", Map.of("ngrok-skip-browser-warning", true)))
                .system(s -> s.text(systemPrompt)
                        .param("context", context)
                        .param("format", converter.getFormat()))
                .user(request.message())
                .call()
                .entity(converter);

        if (aiResult != null && "ORDER".equals(aiResult.action()) && aiResult.orderRequest() != null) {
            try {
                OrderResponse orderResponse = orderService.createOrder(aiResult.orderRequest());

                savedHistory(request.message(), aiResult.message(), context);
                return orderResponse;
            } catch (Exception e) {
                return "Đã có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.";
            }
        }
        savedHistory(request.message(), request.message(), context);
        return aiResult.message();
    }

    private String classifyIntent(String message) {
        String prompt = """
                Phân loại câu hỏi của người dùng thành một trong hai loại sau:
                - SUMMARY: Nếu người dùng hỏi tổng quát về menu, danh sách món, có những gì (Ví dụ: "Menu có gì?", "Quán có món gì?").
                - DETAIL: Nếu người dùng hỏi chi tiết về giá, thành phần, hoặc muốn đặt món (Ví dụ: "Cà phê muối giá bao nhiêu?", "Cho tôi 1 trà đào").

                Chỉ trả về đúng từ khóa: SUMMARY hoặc DETAIL.
                Câu hỏi: {query}
                """;

        return chatClient.prompt()
                .user(u -> u.text(prompt).param("query", message))
                .call()
                .content();
    }

    private void savedHistory(String userMsg, String aiRes, String context) {
        AIChatHistory history = AIChatHistory.builder()
                .userMessage(userMsg)
                .aiResponse(aiRes)
                .retrievedContext(context)
                .createdAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(history);
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
