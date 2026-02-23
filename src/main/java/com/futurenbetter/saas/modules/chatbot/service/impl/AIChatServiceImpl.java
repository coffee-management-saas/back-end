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
import org.apache.poi.ss.usermodel.Cell;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        boolean isCalorieQuery = queryLower.contains("calo") || queryLower.contains("calorie")
                || queryLower.contains("năng lượng") || queryLower.contains("kcal");

        boolean isCalorieRankQuery = isCalorieQuery && (
                queryLower.contains("cao nhất") || queryLower.contains("thấp nhất")
                        || queryLower.contains("nhiều nhất") || queryLower.contains("ít nhất")
                        || queryLower.contains("cao hơn") || queryLower.contains("thấp hơn")
                        || queryLower.contains("trên") || queryLower.contains("dưới")
                        || queryLower.contains("khoảng"));

        boolean isTagQuery = queryLower.contains("ít ngọt") || queryLower.contains("thanh mát")
                || queryLower.contains("thanh khiết") || queryLower.contains("béo")
                || queryLower.contains("giải nhiệt") || queryLower.contains("caffeine")
                || queryLower.contains("không đường") || queryLower.contains("giảm cân")
                || queryLower.contains("healthy") || queryLower.contains("chua ngọt")
                || queryLower.contains("signature") || queryLower.contains("no lâu")
                || queryLower.contains("tráng miệng") || queryLower.contains("mát lạnh")
                || queryLower.contains("tỉnh táo") || queryLower.contains("sáng trưa")
                || queryLower.contains("thư giãn") || queryLower.contains("ít béo")
                || queryLower.contains("đậm đà") || queryLower.contains("nhẹ");

        boolean isBroadMenuQuery = queryLower.contains("menu") || queryLower.contains("món gì")
                || queryLower.contains("có gì") || queryLower.contains("bán gì")
                || queryLower.contains("danh sách") || queryLower.contains("thực đơn");

        String context;

        if (isCalorieRankQuery || isCalorieQuery || isTagQuery || isBroadMenuQuery) {
            List<Document> allDocs = getAllDocuments();

            if (isCalorieRankQuery) {
                context = buildCalorieContext(allDocs, queryLower);
            } else if (isCalorieQuery) {
                List<Document> byName = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(request.message())
                                .topK(5)
                                .similarityThreshold(0.1)
                                .build());
                context = buildDefaultContext(byName) + "\n\n=== TOÀN BỘ MENU (để tham khảo calo) ===\n"
                        + buildFullMenuContext(allDocs);
            } else if (isTagQuery) {
                context = buildTagContext(allDocs, queryLower);
            } else {
                context = buildFullMenuContext(allDocs);
            }
        } else {
            List<Document> similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(request.message())
                            .topK(5)
                            .similarityThreshold(0.15)
                            .build());
            context = buildDefaultContext(similarDocuments);
        }

        BeanOutputConverter<AIOrderResponse> converter = new BeanOutputConverter<>(AIOrderResponse.class);

        String contextBlock = (context == null || context.isBlank())
                ? "[KHÔNG CÓ DỮ LIỆU MENU – hãy thông báo cho khách rằng bạn chưa có thông tin menu hiện tại]"
                : context;

        String systemPrompt = """

                Bạn là nhân viên tư vấn tại quán Future&Better. Giao tiếp lịch sự, thân thiện.

                LỊCH SỬ TRÒ CHUYỆN:
                {history}

                DỮ LIỆU MENU TỪ HỆ THỐNG (chỉ dùng thông tin này, KHÔNG tự bịa thêm):
                {context}

                QUY TẮC PHÂN TÍCH DỮ LIỆU:
                1. ƯU TIÊN TAGS: Khi khách hỏi "ít ngọt", "tỉnh táo", "giải nhiệt",... hãy quét mục [Tags] để liệt kê ĐÚNG các món có tag phù hợp.
                2. SO SÁNH CALO: Sử dụng SỐ LIỆU trong trường [Calo] để so sánh chính xác. Không ước đoán.
                   - "cao nhất" → món có số calo lớn nhất trong dữ liệu
                   - "thấp nhất" → món có số calo nhỏ nhất
                   - "dưới X calo" / "trên X calo" / "khoảng X calo" → lọc theo ngưỡng
                3. KHỚP TÊN CHÍNH XÁC: Không nhầm tên món dù có Tags giống nhau.
                4. LIỆT KÊ ĐẦY ĐỦ: Khi khách hỏi "món nào ít ngọt?" → liệt kê TẤT CẢ món có tag đó, không chỉ một vài món.

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
                6. Khi khách hỏi về TAGS / ĐẶC TÍNH (ít ngọt, giải nhiệt, ...):
                   - Liệt kê TẤT CẢ món phù hợp với tag đó.
                   - Kèm giá và một câu mô tả ngắn từ [Gợi ý].
                7. Khi khách hỏi về CALO:
                   - Trả lời chính xác theo số liệu trong [Calo].
                   - Sắp xếp từ thấp đến cao (hoặc theo yêu cầu).
                   - TRÌNH BÀY MỖI MÓN TRÊN MỘT DÒNG RIÊNG, theo mẫu sau (dùng \\n để xuống dòng):
                     🔸 Tên Món – M: Xk | L: Xk – Calo: X kcal
                   - Ví dụ đúng:
                     🔸 Mousse Đào sà – M: 42k – Calo: 250 kcal\\n🔸 Trà Oolong Cam sả tươi – M: 35k | L: 43k – Calo: 150 kcal
                   - TUYỆT ĐỐI không gộp nhiều món trên cùng một dòng hay dùng dấu •.
                8. Khi khách muốn đặt món:
                   - Thu thập đủ: Tên món (đúng trong menu), Số lượng, Size (M/L), Topping.
                   - Nếu thiếu: Giữ `action="INFO"`, hỏi cụ thể từng thứ còn thiếu.
                9. Chỉ chuyển `action="ORDER"` khi ĐỦ thông tin VÀ khách xác nhận ("Đặt đi", "Chốt đơn", "Ok đặt", v.v.).
                10. `toppingItems` PHẢI là mảng JSON. Không có topping: `[]`.
                11. Trong `orderRequest`, luôn set `orderType = "ONLINE"`.

                ĐỊNH DẠNG JSON BẮT BUỘC:
                {format}
                """;

        try {
            String rawResponse = chatClient.prompt()
                    .system(s -> s.text(systemPrompt)
                            .param("history", history)
                            .param("context", contextBlock)
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
            log.error("Error in chat processing", e);
            return "Xin lỗi, tôi gặp chút trục trặc khi xử lý thông tin. Bạn có thể nhắc lại được không?";
        }
    }

    private List<Document> getAllDocuments() {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("món ăn thức uống menu")
                        .topK(200)
                        .similarityThreshold(0.0)
                        .build());
    }

    private String buildCalorieContext(List<Document> docs, String queryLower) {
        List<Document> docsWithCalo = docs.stream()
                .filter(d -> parseCalo(d) > 0)
                .collect(Collectors.toList());

        if (docsWithCalo.isEmpty()) {
            return "[Không tìm thấy thông tin calo trong menu]";
        }

        boolean ascending = queryLower.contains("thấp nhất") || queryLower.contains("ít nhất")
                || queryLower.contains("thấp hơn") || queryLower.contains("dưới")
                || queryLower.contains("ít calo");

        docsWithCalo.sort((a, b) -> {
            int ca = parseCalo(a);
            int cb = parseCalo(b);
            return ascending ? Integer.compare(ca, cb) : Integer.compare(cb, ca);
        });

        OptionalInt threshold = extractCalorieThreshold(queryLower);
        if (threshold.isPresent()) {
            int val = threshold.getAsInt();
            boolean below = queryLower.contains("dưới") || queryLower.contains("thấp hơn");
            boolean above = queryLower.contains("trên") || queryLower.contains("cao hơn");
            boolean around = queryLower.contains("khoảng");

            docsWithCalo = docsWithCalo.stream().filter(d -> {
                int c = parseCalo(d);
                if (below)  return c < val;
                if (above)  return c > val;
                if (around) return Math.abs(c - val) <= 50;
                return true;
            }).collect(Collectors.toList());
        }

        if (docsWithCalo.isEmpty()) {
            return "[Không tìm thấy món phù hợp với yêu cầu calo]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== DANH SÁCH MÓN THEO CALO (")
                .append(ascending ? "thấp → cao" : "cao → thấp")
                .append(") ===\n");
        docsWithCalo.forEach(doc -> sb.append(formatDocContext(doc)).append("\n"));
        return sb.toString();
    }

    private String buildTagContext(List<Document> docs, String queryLower) {
        Map<String, List<String>> tagMapping = new LinkedHashMap<>();
        tagMapping.put("ít ngọt",       List.of("ít_ngọt", "it_ngot"));
        tagMapping.put("không ngọt",    List.of("ít_ngọt", "it_ngot"));
        tagMapping.put("thanh mát",     List.of("thanh_mát", "thanh_mat"));
        tagMapping.put("thanh khiết",   List.of("thanh_khiết", "thanh_khiet"));
        tagMapping.put("giải nhiệt",    List.of("giải_nhiệt", "giai_nhiet"));
        tagMapping.put("mát lạnh",      List.of("mát_lạnh", "mat_lanh"));
        tagMapping.put("không caffeine",List.of("caffeine_free"));
        tagMapping.put("caffeine free", List.of("caffeine_free"));
        tagMapping.put("no caffeine",   List.of("caffeine_free"));
        tagMapping.put("béo ngậy",      List.of("béo_ngậy", "beo_ngay"));
        tagMapping.put("giảm cân",      List.of("ít_béo", "it_beo"));
        tagMapping.put("healthy",       List.of("thanh_mát", "ít_béo", "it_beo"));
        tagMapping.put("chua ngọt",     List.of("chua_ngọt", "chua_ngot"));
        tagMapping.put("signature",     List.of("signature"));
        tagMapping.put("no lâu",        List.of("no_lâu", "no_lau"));
        tagMapping.put("tráng miệng",   List.of("tráng_miệng", "trang_mieng"));
        tagMapping.put("tỉnh táo",      List.of("sáng_trưa", "sang_trua", "caffeine"));
        tagMapping.put("sáng trưa",     List.of("sáng_trưa", "sang_trua"));
        tagMapping.put("thư giãn",      List.of("thư_giãn", "thu_gian"));
        tagMapping.put("ít béo",        List.of("ít_béo", "it_beo"));
        tagMapping.put("đậm đà",        List.of("đậm_đà", "dam_da"));
        tagMapping.put("mềm mịn",       List.of("mềm_mịn", "mem_min"));
        tagMapping.put("nhẹ",           List.of("thanh_nhẹ", "thanh_nhe"));
        tagMapping.put("tiêu hóa",      List.of("tiêu_hóa", "tieu_hoa"));

        Set<String> searchTags = new HashSet<>();
        tagMapping.forEach((keyword, tags) -> {
            if (queryLower.contains(keyword)) {
                searchTags.addAll(tags);
            }
        });

        List<Document> filtered;

        if (searchTags.isEmpty()) {
            filtered = docs;
        } else {
            filtered = docs.stream()
                    .filter(doc -> {
                        String rawTags = ((String) doc.getMetadata().getOrDefault("tags", ""))
                                .toLowerCase()
                                .replace(" ", "_");
                        return searchTags.stream().anyMatch(rawTags::contains);
                    })
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            return "[Không tìm thấy món phù hợp với đặc tính yêu cầu]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== MÓN PHÙ HỢP VỚI ĐẶC TÍNH YÊU CẦU ===\n");
        filtered.forEach(doc -> sb.append(formatDocContext(doc)).append("\n"));
        return sb.toString();
    }

    private String buildFullMenuContext(List<Document> docs) {
        Map<String, List<Document>> grouped = new LinkedHashMap<>();
        docs.forEach(doc -> {
            String nhom = (String) doc.getMetadata().getOrDefault("nhom", "KHÁC");
            grouped.computeIfAbsent(nhom, k -> new ArrayList<>()).add(doc);
        });

        StringBuilder sb = new StringBuilder();
        sb.append("=== TOÀN BỘ MENU ===\n");
        grouped.forEach((nhom, items) -> {
            sb.append("\n[NHÓM: ").append(nhom).append("]\n");
            items.forEach(doc -> sb.append(formatDocContext(doc)).append("\n"));
        });
        return sb.toString();
    }

    private String buildDefaultContext(List<Document> docs) {
        return docs.stream()
                .map(this::formatDocContext)
                .collect(Collectors.joining("\n"));
    }

    private String formatDocContext(Document doc) {
        String ten   = (String) doc.getMetadata().getOrDefault("ten_mon", "?");
        String nhom  = (String) doc.getMetadata().getOrDefault("nhom", "");
        String tags  = (String) doc.getMetadata().getOrDefault("tags", "");
        String calo  = (String) doc.getMetadata().getOrDefault("calories", "");
        String logic = (String) doc.getMetadata().getOrDefault("logic_goi_y", "");
        String giaM  = (String) doc.getMetadata().getOrDefault("gia_m", "-");
        String giaL  = (String) doc.getMetadata().getOrDefault("gia_l", "-");

        return String.format(
                "--- MÓN: %s [%s] ---\n" +
                        "[Giá]: M: %s | L: %s\n" +
                        "[Calo]: %s kcal\n" +
                        "[Tags]: %s\n" +
                        "[Gợi ý tư vấn]: %s",
                ten, nhom, giaM, giaL, calo, tags, logic
        );
    }

    private int parseCalo(Document doc) {
        String raw = (String) doc.getMetadata().getOrDefault("calories", "0");
        if (raw == null || raw.isBlank()) return 0;
        try {
            String digits = raw.replaceAll("[^0-9]", "").trim();
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private OptionalInt extractCalorieThreshold(String query) {
        Matcher m = Pattern.compile("(\\d{2,4})\\s*(calo|kcal|cal|calories)?").matcher(query);
        while (m.find()) {
            int val = Integer.parseInt(m.group(1));
            if (val >= 50 && val <= 2000) {
                return OptionalInt.of(val);
            }
        }
        return OptionalInt.empty();
    }

    private String parseJson(String raw) {
        if (raw == null || raw.isBlank()) return "{}";

        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start == -1 || end == -1) {
            return "{\"action\":\"INFO\", \"message\":\""
                    + raw.replace("\"", "'").replace("\n", " ")
                    + "\", \"orderRequest\":null}";
        }

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
                    default   -> sb.append(String.format("\\u%04x", (int) c));
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
        if (fileName == null) return "Tên tệp không hợp lệ.";

        if (fileName.endsWith(".xlsx")) {
            return ingestExcel(file, fileName);
        }

        String content = "";
        if (fileName.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                 XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                content = extractor.getText();
            }
        } else {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        if (content.isBlank()) return "Tệp trống hoặc không thể đọc nội dung.";

        ingestData(content);
        return "Đã nạp tài liệu từ file " + fileName + " thành công";
    }

    private String ingestExcel(MultipartFile file, String fileName) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            List<Document> excelDocs = new ArrayList<>();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            String currentNhom = "";

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                String nhom   = getCellValue(row.getCell(1));
                String tenMon = getCellValue(row.getCell(2));

                if (!nhom.isBlank()) {
                    currentNhom = nhom;
                }

                if (tenMon.isBlank()) continue;
                if (tenMon.equalsIgnoreCase("Tên Topping") || tenMon.equalsIgnoreCase("Tên Món / Combo")) continue;

                String giaM  = getCellValue(row.getCell(3));
                String giaL  = getCellValue(row.getCell(4));
                String tags  = getCellValue(row.getCell(5));
                String calo  = getCellValue(row.getCell(6));
                String logic = getCellValue(row.getCell(7));

                String mainContent = String.format("Món: %s. Nhóm: %s. Đặc tính: %s.", tenMon, currentNhom, tags);

                Document doc = new Document(mainContent);
                doc.getMetadata().put("ten_mon",    tenMon);
                doc.getMetadata().put("nhom",       currentNhom);
                doc.getMetadata().put("gia_m",      giaM);
                doc.getMetadata().put("gia_l",      giaL);
                doc.getMetadata().put("tags",       tags);
                doc.getMetadata().put("calories",   calo);
                doc.getMetadata().put("logic_goi_y", logic);
                doc.getMetadata().put("source",     fileName);

                String fullInfo = String.format("%s (%s) - Giá Size M: %s, L: %s. Đặc tính: %s. Gợi ý: %s",
                        tenMon, currentNhom, giaM, giaL, tags, logic);
                doc.getMetadata().put("full_summary", fullInfo);

                excelDocs.add(doc);
            }

            if (!excelDocs.isEmpty()) {
                vectorStore.add(excelDocs);
                return "Đã nạp thành công " + excelDocs.size() + " món ăn từ file Excel.";
            }
            return "Không tìm thấy dữ liệu hợp lệ trong file Excel.";
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    return String.format("%d", (long) val);
                } else {
                    return String.format("%.0f", val);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            default:
                return "";
        }
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