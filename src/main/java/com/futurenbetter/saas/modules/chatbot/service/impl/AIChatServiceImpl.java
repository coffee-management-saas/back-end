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
                        1. Khi khách muốn mua món nào đó, BẮT BUỘC chỉ được hỏi: Tên món, Size (M/L), và Số lượng. (Topping là tùy chọn).
                        2. TUYỆT ĐỐI KHÔNG HỎI: Mã khách hàng (customerId), Hình thức (orderType), Thanh toán (paymentGateway), địa chỉ.
                        3. KHI ĐÃ ĐỦ THÔNG TIN (Tên, Size, Số lượng), hãy dùng công cụ 'createOrder' để khởi tạo đơn hàng ngay lập tức.
                        4. Các bước thanh toán và giao hàng sẽ do khách hàng tự chọn tại trang Checkout sau đó.
                        5. Trả lời lịch sự, ngắn gọn và thân thiện.
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