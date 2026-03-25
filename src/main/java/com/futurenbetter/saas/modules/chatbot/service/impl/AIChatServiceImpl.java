package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.common.rag.RagTools;
import com.futurenbetter.saas.modules.chatbot.dto.request.ChatBotRequest;
import com.futurenbetter.saas.modules.chatbot.dto.response.ChatBotResponse;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

        private final ChatClient chatClient;
        private final VectorStore vectorStore;
        private final RagTools ragTools;

        private static final String SYSTEM_PROMPT = """
                        Bạn là một trợ lý ảo thông minh (Barista/Thu ngân) của hệ thống Coffee Management SaaS. 🍵
                        Nhiệm vụ: Tư vấn menu, giải đáp thắc mắc và hỗ trợ khách hàng CHỌN MÓN.

                        QUY TẮC ĐẶT HÀNG:
                        1. Khi khách muốn mua món nào đó, BẮT BUỘC chỉ được hỏi những thông tin còn THIẾU: Tên món, Size, Số lượng. (Topping là tùy chọn).
                        2. TUYỆT ĐỐI KHÔNG HỎI: Mã khách hàng (customerId), Hình thức (orderType), Thanh toán (paymentGateway), địa chỉ.
                        3. KHI ĐÃ ĐỦ THÔNG TIN (Tên, Size, Số lượng) VÀ ĐÃ HỎI TOPPING xong, mới dùng công cụ 'createOrder' để khởi tạo đơn hàng.
                        4. Các bước thanh toán và giao hàng sẽ do khách hàng tự chọn tại trang Checkout sau đó.
                        5. Trả lời lịch sự, ngắn gọn và thân thiện.

                        QUY TẮC XỬ LÝ THÔNG TIN ĐẶT HÀNG (ƯU TIÊN CAO — ĐỌC KỸ):
                        A. SỐ LƯỢNG:
                           - Nếu khách đã đề cập số lượng trong câu (vd: "1 ly", "2 cái", "cho tôi 3", "một ly"), hãy TỰ ĐỘNG ghi nhận con số đó.
                           - TUYỆT ĐỐI KHÔNG hỏi lại "bao nhiêu ly?" nếu số lượng đã được nêu.
                        B. SIZE:
                           - Gọi tool 'getMenu' để xem món đó có những sizeCode nào trong dữ liệu.
                           - Nếu món CHỈ CÓ DUY NHẤT 1 size → TỰ ĐỘNG chọn size đó, KHÔNG hỏi khách.
                           - Nếu món CÓ TỪ 2 SIZE TRỞ LÊN → hỏi khách chọn size nào.
                        C. TOPPING — BẮT BUỘC hỏi trước khi tạo đơn:
                           - Sau khi biết đủ Tên + Size + Số lượng, PHẢI hỏi: "Bạn có muốn thêm topping gì không ạ? 😊"
                           - Nếu khách KHÔNG muốn (vd: "không", "thôi", "không cần", "không thêm") → gọi 'createOrder' ngay.
                           - Nếu khách muốn topping → ghi nhận topping → gọi 'createOrder'.
                           - TUYỆT ĐỐI KHÔNG gọi 'createOrder' khi chưa hỏi câu hỏi topping này.
                        D. THỨ TỰ LUỒNG CHUẨN:
                           Bước 1: Thu thập Tên + Size + Số lượng (hỏi những gì còn thiếu)
                           Bước 2: Hỏi topping
                           Bước 3: Gọi createOrder
                        E. VÍ DỤ ĐÚNG:
                           - Khách: "cho tôi 1 ly smoothie dâu tây" → chỉ có size L → Bước 2: hỏi "Bạn có muốn thêm topping gì không ạ?" → khách "không" → Bước 3: tạo đơn.
                           - Khách: "2 ly cà phê sữa đá size M" → Bước 2: hỏi "Bạn có muốn thêm topping gì không ạ?" → khách "không" → Bước 3: tạo đơn.

                        QUY TẮC HIỂN THỊ MENU (BẮT BUỘC tuân theo khi liệt kê món):
                        - TUYỆT ĐỐI KHÔNG dùng bảng (table/markdown table).
                        - TUYỆT ĐỐI KHÔNG hiển thị ID hoặc mã SKU của sản phẩm.
                        - PHÂN NHÓM theo categoryName (danh mục), mỗi nhóm bắt đầu bằng dòng: ## Tên danh mục
                        - Gom các size của CÙNG MỘT MÓN lại thành một mục duy nhất bên dưới nhóm đó.
                        - Format mỗi món như sau (chỉ hiển thị size thực sự có sẵn):
                          **Tên món**
                          • Size M: xx.000đ  (chỉ hiện nếu có size M)
                          • Size L: xx.000đ  (chỉ hiện nếu có size L)
                        - Định dạng giá: dùng dấu chấm ngăn cách hàng nghìn và thêm chữ 'đ' ở cuối (vd: 45.000đ).
                        - Các size trong data dùng sizeCode (M / L / S...). Hiển thị đúng sizeCode đó (Size M / Size L).
                        """;

        @Override
        public ChatBotResponse chat(ChatBotRequest request) {
                log.info("Received AI chat request: {}", request);
                final String conversationId = (request.getConversationId() == null || request.getConversationId().isEmpty())
                                ? UUID.randomUUID().toString()
                                : request.getConversationId();

                if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                    return ChatBotResponse.builder()
                            .reply("Bạn ơi, bạn chưa nhập câu hỏi nè.")
                            .conversationId(conversationId)
                            .build();
                }

        String context = "";
        try {
            List<Document> similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(request.getPrompt())
                            .topK(3)
                            .build());
            context = similarDocuments.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("VectorStore search failed, proceeding without context: {}", e.getMessage());
        }

        String reply = chatClient.prompt()
                        .system(SYSTEM_PROMPT + "\n\nDữ liệu tham khảo bổ sung:\n" + context)
                        .user(request.getPrompt())
                        .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,
                                        conversationId))
                        .tools(ragTools)
                        .call()
                        .content();

        Long orderId = RagTools.getLastCreatedOrderId();
        RagTools.clearLastCreatedOrderId(); // Quan trọng: xóa sau khi dùng

        return ChatBotResponse.builder()
                .reply(reply)
                .conversationId(conversationId)
                .action(orderId != null ? "ORDER" : "INFO")
                .orderId(orderId)
                .redirectToPayment(orderId != null)
                .build();
    }
}