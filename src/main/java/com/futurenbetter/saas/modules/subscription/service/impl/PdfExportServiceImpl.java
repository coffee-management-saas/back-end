package com.futurenbetter.saas.modules.subscription.service.impl;

import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.enums.PaymentGatewayEnum;
import com.futurenbetter.saas.modules.subscription.service.PdfExportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfExportServiceImpl implements PdfExportService {

    @Override
    public byte[] generateInvoicePdf(BillingInvoice billingInvoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // --- NẠP FONT TIẾNG VIỆT ---
            // Sử dụng IDENTITY_H để hỗ trợ vẽ ký tự Tiếng Việt có dấu
            String fontPath = "src/main/resources/fonts/arial.ttf";
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font fontBold = new Font(bf, 10, Font.BOLD);
            Font fontNormal = new Font(bf, 10, Font.NORMAL);
            Font fontTitle = new Font(bf, 16, Font.BOLD);

            // --- BẮT ĐẦU VẼ NỘI DUNG (Dùng được Tiếng Việt có dấu) ---
            Paragraph header = new Paragraph("Mẫu số: 01GTKT3/001\nKý hiệu: FN/26P\nSố: "
                    + String.format("%07d", billingInvoice.getBillingInvoiceId()), fontNormal);
            header.setAlignment(Element.ALIGN_RIGHT);
            document.add(header);

            Paragraph title = new Paragraph("HÓA ĐƠN GIÁ TRỊ GIA TĂNG", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String paymentMethod = "N/A";
            if (billingInvoice.getTransaction() != null && billingInvoice.getTransaction().getPaymentGateway() != null) {
                paymentMethod = billingInvoice.getTransaction().getPaymentGateway().toString();
            }

            log.info(paymentMethod);

            // Ngày tháng năm Tiếng Việt
            String dateText = String.format("Ngày %02d tháng %02d năm %d",
                    billingInvoice.getCreatedAt().getDayOfMonth(),
                    billingInvoice.getCreatedAt().getMonthValue(),
                    billingInvoice.getCreatedAt().getYear());
            Paragraph subTitle = new Paragraph(dateText, fontNormal);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Đơn vị bán hàng: HỆ THỐNG FUTURE&BETTER", fontBold));
            document.add(new Paragraph("Địa chỉ: " + billingInvoice.getShop().getAddress(), fontNormal));
            document.add(new Paragraph("----------------------------------------------------------------------------------"));

            document.add(new Paragraph("Tên đơn vị mua hàng: " + billingInvoice.getShop().getShopName(), fontNormal));
            document.add(new Paragraph("Hình thức thanh toán: " + paymentMethod, fontNormal));
            document.add(new Paragraph(" "));

            // --- Bảng chi tiết (PdfPTable) ---
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 2, 2, 3});

            addCell(table, "STT", fontBold);
            addCell(table, "Tên hàng hóa, dịch vụ", fontBold);
            addCell(table, "Đơn vị", fontBold);
            addCell(table, "Số lượng", fontBold);
            addCell(table, "Thành tiền", fontBold);

            addCell(table, "1", fontNormal);
            String planName = "N/A";
            if (billingInvoice.getShopSubscription() != null && billingInvoice.getShopSubscription().getPlan() != null) {
                planName = billingInvoice.getShopSubscription().getPlan().getSubscriptionPlanName();
            }
            addCell(table, planName, fontNormal);
            addCell(table, "Gói", fontNormal);
            addCell(table, "1", fontNormal);
            addCell(table, String.format("%,d", billingInvoice.getAmount()), fontNormal);

            document.add(table);

            // --- Tổng kết phí ---
            document.add(new Paragraph(" "));
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(40);
            footerTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addFooterRow(footerTable, "Cộng tiền hàng:", String.format("%,d", billingInvoice.getAmount()), fontNormal);
            addFooterRow(footerTable, "Thuế GTGT (0%):", "0", fontNormal);
            addFooterRow(footerTable, "TỔNG CỘNG:", String.format("%,d VND", billingInvoice.getAmount()), fontBold);
            document.add(footerTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi thiết kế hóa đơn Tiếng Việt", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addFooterRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }
}