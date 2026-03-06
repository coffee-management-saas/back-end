package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.dto.response.AIOrderResponse;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;

import java.text.Normalizer;
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
import com.futurenbetter.saas.modules.product.entity.ProductVariant;
import com.futurenbetter.saas.modules.product.repository.ProductVariantRepository;
import com.futurenbetter.saas.modules.product.repository.ToppingRepository;
import com.futurenbetter.saas.modules.product.entity.Topping;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.order.dto.response.OrderResponse;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.data.domain.Pageable;

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
    private final ProductVariantRepository productVariantRepository;
    private final ToppingRepository toppingRepository;
    private final OrderService orderService;

    public AIChatServiceImpl(ChatClient.Builder builder,
            PgVectorStore vectorStore,
            ChatHistoryServiceImpl chatHistoryService,
            ProductVariantRepository productVariantRepository,
            ToppingRepository toppingRepository,
            OrderService orderService) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.chatHistoryService = chatHistoryService;
        this.productVariantRepository = productVariantRepository;
        this.toppingRepository = toppingRepository;
        this.orderService = orderService;
    }

    @Override
    public Object chat(ChatRequest request) {
        String activeSessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? "default-session"
                : request.sessionId();

        String history = chatHistoryService.getChatHistoryContext(activeSessionId);
        String currentMessage = request.message();
        String queryLower = currentMessage.toLowerCase();
        String refinedQuery = currentMessage;

        if (currentMessage.length() < 15 && history != null && !history.isBlank()) {
            String lastUserMsg = "";
            String[] segments = history.split("Khách: ");
            if (segments.length > 1) {
                String lastSegment = segments[segments.length - 1];
                int aiIdx = lastSegment.indexOf("\nAI: ");
                if (aiIdx != -1) {
                    lastUserMsg = lastSegment.substring(0, aiIdx).trim();
                } else {
                    lastUserMsg = lastSegment.trim();
                }
            }
            if (!lastUserMsg.isEmpty() && !currentMessage.equalsIgnoreCase(lastUserMsg)) {
                refinedQuery = lastUserMsg + " " + currentMessage;
                log.info("Refined search query for short message: '{}' -> '{}'", currentMessage, refinedQuery);
            }
        }

        boolean isCalorieQuery = queryLower.contains("calo") || queryLower.contains("calorie")
                || queryLower.contains("năng lượng") || queryLower.contains("kcal");

        boolean isCalorieRankQuery = isCalorieQuery
                && (queryLower.contains("cao nhất") || queryLower.contains("thấp nhất")
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
            List<ProductVariant> shopVariants = getVariantsForDocs(allDocs);

            if (isCalorieRankQuery) {
                context = buildCalorieContext(allDocs, queryLower, shopVariants);
            } else if (isCalorieQuery) {
                List<Document> byName = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(refinedQuery)
                                .topK(10)
                                .similarityThreshold(0.1)
                                .build());
                context = buildDefaultContext(byName, shopVariants) + "\n\n=== TOÀN BỘ MENU (để tham khảo calo) ===\n"
                        + buildFullMenuContext(allDocs, shopVariants);
            } else if (isTagQuery) {
                context = buildTagContext(allDocs, queryLower, shopVariants);
            } else {
                context = buildFullMenuContext(allDocs, shopVariants);
            }
        } else {
            List<Document> similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(refinedQuery)
                            .topK(10)
                            .similarityThreshold(0.1)
                            .build());
            List<ProductVariant> shopVariants = getVariantsForDocs(similarDocuments);
            context = buildDefaultContext(similarDocuments, shopVariants);
        }

        String toppingContext = buildToppingContext();
        String finalContext = context + "\n\n" + toppingContext;

        BeanOutputConverter<AIOrderResponse> converter = new BeanOutputConverter<>(AIOrderResponse.class);

        String contextBlock = (finalContext == null || finalContext.isBlank())
                ? "[KHÔNG CÓ DỮ LIỆU MENU – hãy thông báo cho khách rằng bạn chưa có thông tin menu hiện tại]"
                : finalContext;

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

                QUY TRÌNH ĐẶT MÓN (Bắt buộc theo thứ tự):
                Bước 1: Khách hỏi/muốn đặt món -> AI tìm món trong DỮ LIỆU MENU.
                Bước 2: AI hỏi Size (M/L) và Topping (nếu chưa có).
                Bước 3: AI tổng hợp đơn hàng gồm: Tên món, Số lượng, Size, Topping và Tổng giá dự kiến.
                Bước 4: AI hỏi khách: "Bạn xác nhận chốt đơn này chứ?".
                Bước 5: Khách nói "Đồng ý", "Chốt luôn", "Ok",... -> AI mới được chuyển `action="ORDER"` và điền thong tin vào `orderRequest`.

                QUY TẮC BẮT BUỘC:
                1. LUÔN trả về JSON theo đúng định dạng. Tuyệt đối không kèm văn bản ngoài JSON.
                2. CHỈ dùng thông tin có trong DỮ LIỆU MENU. KHÔNG được tự bịa.
                3. AI PHẢI lấy đúng ID (ví dụ: 123) từ mục `[ID_SIZE_...]` để điền vào `productVariantId`. KHÔNG ĐƯỢC để null khi đặt món.
                4. Nếu khách chưa xác nhận chốt đơn cuối cùng, LUÔN để `action="INFO"` và `orderRequest=null`.
                5. Trong `orderRequest`, LUÔN set `orderType="ONLINE"` và `paymentGateway="CASH"` làm mặc định (Khách sẽ chọn lại phương thức thanh toán sau tại trang checkout).
                6. Trình bày Menu/Tin nhắn đẹp mắt, mỗi món một dòng, dùng dấu xuống dòng (\\n).

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
            chatHistoryService.savedHistory(activeSessionId, request.message(), aiResult.message(), contextBlock);

            if ("ORDER".equals(aiResult.action()) && aiResult.orderRequest() != null) {
                try {
                    boolean hasInvalidItem = aiResult.orderRequest().getOrderItems().stream()
                            .anyMatch(item -> item.getProductVariantId() == null || item.getProductVariantId() <= 0);

                    if (hasInvalidItem) {
                        return new AIOrderResponse("INFO",
                                "Tôi xin lỗi, có chút vấn đề về mã sản phẩm. Bạn vui lòng nói lại món và size muốn đặt để tôi xác nhận lại chính xác nhé!",
                                null, false, null);
                    }

                    aiResult.orderRequest().setOrderType(com.futurenbetter.saas.modules.order.enums.OrderType.ONLINE);
                    aiResult.orderRequest().setPaymentGateway(null);

                    if (aiResult.orderRequest().getOrderItems() == null
                            || aiResult.orderRequest().getOrderItems().isEmpty()) {
                        return new AIOrderResponse("INFO",
                                "Tôi chưa thấy danh sách món trong đơn hàng. Bạn vui lòng chọn món muốn đặt nhé!",
                                null, false, null);
                    }

                    OrderResponse createdOrder = orderService.createOrder(aiResult.orderRequest());
                    String successMsg = aiResult.message() + "\n\n✅ **Đơn hàng #" + createdOrder.getOrderId()
                            + " đã được khởi tạo!**\n*Mời bạn nhấn vào nút thanh toán bên dưới để chọn phương thức thanh toán phù hợp.*";

                    return new AIOrderResponse("ORDER", successMsg, aiResult.orderRequest(), true,
                            createdOrder.getOrderId());
                } catch (Exception orderEx) {
                    log.error("Order Creation Failed", orderEx);
                    return "Rất tiếc, hệ thống gặp lỗi khi tạo đơn: " + orderEx.getMessage();
                }
            }
            return aiResult;

        } catch (Exception e) {
            log.error("Chat processing error", e);
            return "Tôi gặp khó khăn khi kết nối dữ liệu, bạn vui lòng thử lại nhé!";
        }
    }

    private List<ProductVariant> getVariantsForDocs(List<Document> docs) {
        Long shopId = SecurityUtils.getCurrentShopId();
        if (shopId == null || docs.isEmpty())
            return Collections.emptyList();

        Set<String> productNames = docs.stream()
                .map(d -> (String) d.getMetadata().get("ten_mon"))
                .filter(Objects::nonNull)
                .map(this::normalizeName)
                .collect(Collectors.toSet());

        return productVariantRepository.findAllByShopId(shopId).stream()
                .filter(v -> productNames.contains(normalizeName(v.getProduct().getName())))
                .collect(Collectors.toList());
    }

    private List<Document> getAllDocuments() {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("món ăn thức uống menu")
                        .topK(200)
                        .similarityThreshold(0.0)
                        .build());
    }

    private String buildCalorieContext(List<Document> docs, String queryLower, List<ProductVariant> shopVariants) {
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
                if (below)
                    return c < val;
                if (above)
                    return c > val;
                if (around)
                    return Math.abs(c - val) <= 50;
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
        docsWithCalo.forEach(doc -> sb.append(formatDocContext(doc, shopVariants)).append("\n"));
        return sb.toString();
    }

    private String buildTagContext(List<Document> docs, String queryLower, List<ProductVariant> shopVariants) {
        Map<String, List<String>> tagMapping = new LinkedHashMap<>();
        tagMapping.put("ít ngọt", List.of("ít_ngọt", "it_ngot"));
        tagMapping.put("không ngọt", List.of("ít_ngọt", "it_ngot"));
        tagMapping.put("thanh mát", List.of("thanh_mát", "thanh_mat"));
        tagMapping.put("thanh khiết", List.of("thanh_khiết", "thanh_khiet"));
        tagMapping.put("giải nhiệt", List.of("giải_nhiệt", "giai_nhiet"));
        tagMapping.put("mát lạnh", List.of("mát_lạnh", "mat_lanh"));
        tagMapping.put("không caffeine", List.of("caffeine_free"));
        tagMapping.put("caffeine free", List.of("caffeine_free"));
        tagMapping.put("no caffeine", List.of("caffeine_free"));
        tagMapping.put("béo ngậy", List.of("béo_ngậy", "beo_ngay"));
        tagMapping.put("giảm cân", List.of("ít_béo", "it_beo"));
        tagMapping.put("healthy", List.of("thanh_mát", "ít_béo", "it_beo"));
        tagMapping.put("chua ngọt", List.of("chua_ngọt", "chua_ngot"));
        tagMapping.put("signature", List.of("signature"));
        tagMapping.put("no lâu", List.of("no_lâu", "no_lau"));
        tagMapping.put("tráng miệng", List.of("tráng_miệng", "trang_mieng"));
        tagMapping.put("tỉnh táo", List.of("sáng_trưa", "sang_trua", "caffeine"));
        tagMapping.put("sáng trưa", List.of("sáng_trưa", "sang_trua"));
        tagMapping.put("thư giãn", List.of("thư_giãn", "thu_gian"));
        tagMapping.put("ít béo", List.of("ít_béo", "it_beo"));
        tagMapping.put("đậm đà", List.of("đậm_đà", "dam_da"));
        tagMapping.put("mềm mịn", List.of("mềm_mịn", "mem_min"));
        tagMapping.put("nhẹ", List.of("thanh_nhẹ", "thanh_nhe"));
        tagMapping.put("tiêu hóa", List.of("tiêu_hóa", "tieu_hoa"));

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
        filtered.forEach(doc -> sb.append(formatDocContext(doc, shopVariants)).append("\n"));
        return sb.toString();
    }

    private String buildFullMenuContext(List<Document> docs, List<ProductVariant> shopVariants) {
        Map<String, List<Document>> grouped = new LinkedHashMap<>();
        docs.forEach(doc -> {
            String nhom = (String) doc.getMetadata().getOrDefault("nhom", "KHÁC");
            grouped.computeIfAbsent(nhom, k -> new ArrayList<>()).add(doc);
        });

        StringBuilder sb = new StringBuilder();
        sb.append("=== TOÀN BỘ MENU ===\n");
        grouped.forEach((nhom, items) -> {
            sb.append("\n[NHÓM: ").append(nhom).append("]\n");
            items.forEach(doc -> sb.append(formatDocContext(doc, shopVariants)).append("\n"));
        });
        return sb.toString();
    }

    private String buildDefaultContext(List<Document> docs, List<ProductVariant> shopVariants) {
        return docs.stream()
                .map(doc -> this.formatDocContext(doc, shopVariants))
                .collect(Collectors.joining("\n"));
    }

    private String formatDocContext(Document doc, List<ProductVariant> shopVariants) {
        String ten = (String) doc.getMetadata().getOrDefault("ten_mon", "?");
        String nhom = (String) doc.getMetadata().getOrDefault("nhom", "");
        String tags = (String) doc.getMetadata().getOrDefault("tags", "");
        String calo = (String) doc.getMetadata().getOrDefault("calories", "");
        String logic = (String) doc.getMetadata().getOrDefault("logic_goi_y", "");
        String giaM = (String) doc.getMetadata().getOrDefault("gia_m", "-");
        String giaL = (String) doc.getMetadata().getOrDefault("gia_l", "-");

        StringBuilder idInfo = new StringBuilder();
        if (shopVariants != null && !shopVariants.isEmpty()) {
            String targetTenNorm = normalizeName(ten);
            List<ProductVariant> variants = shopVariants.stream()
                    .filter(v -> normalizeName(v.getProduct().getName()).equals(targetTenNorm))
                    .toList();

            for (ProductVariant v : variants) {
                String sizeCode = v.getSize().getCode().toUpperCase();
                idInfo.append(String.format("\n[ID_SIZE_%s]: %d", sizeCode, v.getId()));
            }
        }

        return String.format(
                "--- MÓN: %s [%s] ---\n" +
                        "[Giá]: M: %s | L: %s\n" +
                        "[Calo]: %s kcal\n" +
                        "[Gợi ý]: %s%s",
                ten, nhom, giaM, giaL, calo, logic, idInfo.toString());
    }

    private String normalizeName(String name) {
        if (name == null)
            return "";

        String normalized = name.toLowerCase()
                .replace("oolong", "ô long")
                .replace("(signature)", "")
                .replace("signature", "")
                .replace("-", "")
                .replace(".", "")
                .trim();

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replace("đ", "d");

        return normalized.replaceAll("[^a-z0-9]", "").trim();
    }

    private String buildToppingContext() {
        Long shopId = SecurityUtils.getCurrentShopId();
        if (shopId == null)
            return "";

        List<Topping> toppings = toppingRepository.findAllByShopId(shopId, Pageable.unpaged()).getContent();
        if (toppings.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder("\n=== DANH SÁCH TOPPING KHẢ DỤNG ===\n");
        for (Topping t : toppings) {
            sb.append(String.format("• %s - Giá: %,dđ | [ID]: %d\n", t.getName(), t.getPrice(), t.getId()));
        }
        return sb.toString();
    }

    private int parseCalo(Document doc) {
        String raw = (String) doc.getMetadata().getOrDefault("calories", "0");
        if (raw == null || raw.isBlank())
            return 0;
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
        if (raw == null || raw.isBlank())
            return "{}";

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
        if (fileName == null)
            return "Tên tệp không hợp lệ.";

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

        if (content.isBlank())
            return "Tệp trống hoặc không thể đọc nội dung.";

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
                if (row == null)
                    continue;

                String nhom = getCellValue(row.getCell(1));
                String tenMon = getCellValue(row.getCell(2));

                if (!nhom.isBlank()) {
                    currentNhom = nhom;
                }

                if (tenMon.isBlank())
                    continue;
                if (tenMon.equalsIgnoreCase("Tên Topping") || tenMon.equalsIgnoreCase("Tên Món / Combo"))
                    continue;

                String giaM = getCellValue(row.getCell(3));
                String giaL = getCellValue(row.getCell(4));
                String tags = getCellValue(row.getCell(5));
                String calo = getCellValue(row.getCell(6));
                String logic = getCellValue(row.getCell(7));

                String mainContent = String.format("Món: %s. Nhóm: %s. Đặc tính: %s.", tenMon, currentNhom, tags);

                Document doc = new Document(mainContent);
                doc.getMetadata().put("ten_mon", tenMon);
                doc.getMetadata().put("nhom", currentNhom);
                doc.getMetadata().put("gia_m", giaM);
                doc.getMetadata().put("gia_l", giaL);
                doc.getMetadata().put("tags", tags);
                doc.getMetadata().put("calories", calo);
                doc.getMetadata().put("logic_goi_y", logic);
                doc.getMetadata().put("source", fileName);

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
        if (cell == null)
            return "";
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
        if (content == null || content.isBlank())
            return;

        String sanitizedContent = content.replace("\u0000", "");
        TokenTextSplitter splitter = new TokenTextSplitter(800, 100, 5, 10000, true);
        List<Document> documents = splitter.split(new Document(sanitizedContent));
        documents.forEach(doc -> doc.getMetadata().put("source", "menu_upload"));
        vectorStore.add(documents);
    }
}