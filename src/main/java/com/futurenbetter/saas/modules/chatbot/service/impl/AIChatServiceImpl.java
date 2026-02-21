package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.dto.response.AIOrderResponse;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;

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
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;
    private final ChatHistoryServiceImpl chatHistoryService;

    public AIChatServiceImpl(ChatClient.Builder builder,
            PgVectorStore vectorStore,
            ChatHistoryServiceImpl chatHistoryService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.chatHistoryService = chatHistoryService;
    }

    @Override
    public Object chat(ChatRequest request) {
        String activeSessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? "default-session"
                : request.sessionId();

        String history = chatHistoryService.getChatHistoryContext(activeSessionId);

        String queryLower = request.message().toLowerCase();
        boolean isBroadMenuQuery = queryLower.contains("menu") || queryLower.contains("món gì")
                || queryLower.contains("có gì") || queryLower.contains("bán gì")
                || queryLower.contains("danh sách") || queryLower.contains("thực đơn");

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(isBroadMenuQuery ? "danh sách món ăn thức uống menu" : request.message())
                        .topK(isBroadMenuQuery ? 50 : 10) // Broad → lấy nhiều để đủ menu
                        .similarityThreshold(isBroadMenuQuery ? 0.0 : 0.3) // Broad → không lọc
                        .build());

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        BeanOutputConverter<AIOrderResponse> converter = new BeanOutputConverter<>(AIOrderResponse.class);

        String contextBlock = context.isBlank()
                ? "[KHÔNG CÓ DỮ LIỆU MENU – hãy thông báo cho khách rằng bạn chưa có thông tin menu hiện tại]"
                : context;

        String systemPrompt = """

                Bạn là nhân viên tư vấn tại quán Future&Better. Giao tiếp lịch sự, thân thiện.

                LỊCH SỬ TRÒ CHUYỆN:
                {history}

                DỮ LIỆU MENU TỪ HỆ THỐNG (chỉ dùng thông tin này, KHÔNG tự bịa thêm):
                {context}

                QUY TẮC BẮT BUỘC:
                1. LUÔN trả về JSON theo đúng định dạng. Tuyệt đối không kèm văn bản ngoài JSON.
                2. CHỈ dùng thông tin có trong DỮ LIỆU MENU. KHÔNG được tự bịa tên món, giá, đặc tính.
                3. Nếu DỮ LIỆU MENU bắt đầu bằng [KHÔNG CÓ DỮ LIỆU], báo khách và hướng dẫn liên hệ nhân viên.
                4. Mặc định `action="INFO"`. `orderRequest` phải là `null` ở trạng thái này.
                5. Khi khách hỏi menu / danh sách món, TRÌNH BÀY theo cấu trúc sau trong field `message`:
                   - NHÓM các món theo DANH MỤC (ví dụ: ☕ Trà Sữa, 🥤 Combo, 🍹 Smoothy, 🥐 Bánh...).
                   - Mỗi món liệt kê RÕ size và giá. Ví dụ:
                     • Trà Oolong Trân Châu Tươi – M: 45.000đ | L: 55.000đ
                   - Dùng ký tự xuống dòng (\\n) để phân cách, dễ đọc.
                   - Kết thúc bằng câu mời đặt món.
                6. Khi khách muốn đặt món:
                   - Thu thập đủ: Tên món (đúng trong menu), Số lượng, Size (M/L), Topping.
                   - Nếu thiếu: Giữ `action="INFO"`, hỏi cụ thể từng thứ còn thiếu.
                7. Chỉ chuyển `action="ORDER"` khi ĐỦ thông tin VÀ khách xác nhận ("Đặt đi", "Chốt đơn", "Ok đặt", v.v.).
                8. `toppingItems` PHẢI là mảng JSON. Không có topping: `[]`.
                9. Trong `orderRequest`, luôn set `orderType = "ONLINE"`.

                ĐỊNH DẠNG JSON BẮT BUỘC:
                {format}
                """;

        try {
            String rawResponse = chatClient.prompt()
                    .system(s -> s.text(systemPrompt)
                            .param("history", history)
                            .param("context", contextBlock) // Dùng contextBlock (có fallback message nếu rỗng)
                            .param("format", converter.getFormat()))
                    .user(request.message())
                    .call()
                    .content();

            AIOrderResponse aiResult = converter.convert(parseJson(rawResponse));

            chatHistoryService.savedHistory(activeSessionId, request.message(), aiResult.message(), context);

            if ("ORDER".equals(aiResult.action()) && aiResult.orderRequest() != null) {
                return new AIOrderResponse(
                        "ORDER",
                        aiResult.message(),
                        aiResult.orderRequest(),
                        true);
            }
            return aiResult.message();
        } catch (Exception e) {
            return "Xin lỗi, tôi gặp chút trục trặc khi xử lý thông tin. Bạn có thể nhắc lại được không?";
        }
    }

    private String parseJson(String raw) {
        if (raw == null)
            return "{}";
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start == -1 || end == -1)
            return raw;
        String json = raw.substring(start, end + 1);
        return escapeControlCharsInJsonStrings(json);
    }

    private String escapeControlCharsInJsonStrings(String json) {
        StringBuilder sb = new StringBuilder(json.length() + 64);
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                sb.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                sb.append(c);
                inString = !inString;
                continue;
            }

            if (inString && c < 0x20) {
                switch (c) {
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    case '\b' -> sb.append("\\b");
                    case '\f' -> sb.append("\\f");
                    default -> sb.append(String.format("\\u%04x", (int) c));
                }
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
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

        if (content.isBlank()) return "Tệp trống hoặc không thể đọc nội dung.";

        ingestData(content);
        return "Đã nạp tài liệu từ file " + fileName + " thành công";
    }

    @Override
    @Transactional
    public void ingestData(String content) {
        if (content == null || content.isBlank()) return;

        String sanitizedContent = content.replace("\u0000", "");
        TokenTextSplitter splitter = new TokenTextSplitter(800, 100, 5, 10000, true);
        List<Document> documents = splitter.split(new Document(sanitizedContent));
        documents.forEach(doc -> doc.getMetadata().put("source", "menu_upload"));
        vectorStore.add(documents);
    }
}
