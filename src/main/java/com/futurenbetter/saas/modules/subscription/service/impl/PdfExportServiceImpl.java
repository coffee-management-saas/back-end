package com.futurenbetter.saas.modules.subscription.service.impl;

import com.futurenbetter.saas.modules.order.entity.Order;
import com.futurenbetter.saas.modules.order.entity.OrderItem;
import com.futurenbetter.saas.modules.order.entity.ToppingPerOrderItem;
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
import java.util.List;

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
            if (billingInvoice.getTransaction() != null
                    && billingInvoice.getTransaction().getPaymentGateway() != null) {
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
            document.add(new Paragraph(
                    "----------------------------------------------------------------------------------"));

            document.add(new Paragraph("Tên đơn vị mua hàng: " + billingInvoice.getShop().getShopName(), fontNormal));
            document.add(new Paragraph("Hình thức thanh toán: " + paymentMethod, fontNormal));
            document.add(new Paragraph(" "));

            // --- Bảng chi tiết (PdfPTable) ---
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1, 4, 2, 2, 3 });

            addCell(table, "STT", fontBold);
            addCell(table, "Tên hàng hóa, dịch vụ", fontBold);
            addCell(table, "Đơn vị", fontBold);
            addCell(table, "Số lượng", fontBold);
            addCell(table, "Thành tiền", fontBold);

            addCell(table, "1", fontNormal);
            String planName = "N/A";
            if (billingInvoice.getShopSubscription() != null
                    && billingInvoice.getShopSubscription().getPlan() != null) {
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

    @Override
    public byte[] generateOrderPdf(Order order) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Sử dụng page size nhỏ hơn để giống receipt (80mm width)
            // 80mm approx 226 points. 1 point = 1/72 inch.
            Document document = new Document(new Rectangle(226, 800));
            PdfWriter.getInstance(document, out);
            document.setMargins(5, 5, 10, 10);
            document.open();

            // --- FONT ---
            String fontPath = "src/main/resources/fonts/arial.ttf";
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font fontBoldLarge = new Font(bf, 10, Font.BOLD);
            Font fontBold = new Font(bf, 8, Font.BOLD);
            Font fontNormal = new Font(bf, 7, Font.NORMAL);
            Font fontSmall = new Font(bf, 6, Font.NORMAL);
            Font fontTitle = new Font(bf, 11, Font.BOLD);

            // 1. Header: Shop Info
            Paragraph shopName = new Paragraph(order.getShop().getShopName().toUpperCase(), fontTitle);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            Paragraph shopAddress = new Paragraph(order.getShop().getAddress(), fontSmall);
            shopAddress.setAlignment(Element.ALIGN_CENTER);
            document.add(shopAddress);

            document.add(new Paragraph(" "));

            // 2. Receipt Title
            Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN", fontBoldLarge);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph receiptNo = new Paragraph("Số HĐ: " + String.format("%06d", order.getOrderId()), fontNormal);
            receiptNo.setAlignment(Element.ALIGN_CENTER);
            document.add(receiptNo);

            document.add(new Paragraph(" "));

            // 3. Info Table (Info like Cashier, Table, Date/Time)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[] { 1.2f, 1f });

            DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HH:mm");

            addInfoCell(infoTable, "Mã HĐ: #" + String.format("%06d", order.getOrderId()), fontNormal,
                    Element.ALIGN_LEFT);
            addInfoCell(infoTable, "TN: " + (order.getShop().getShopName()), fontNormal, Element.ALIGN_RIGHT);

            addInfoCell(infoTable,
                    "Bàn: " + (order.getOrderType() != null ? order.getOrderType().toString() : "MANG VỀ"), fontNormal,
                    Element.ALIGN_LEFT);
            addInfoCell(infoTable, "Ngày: " + order.getCreatedAt().format(dtfDate), fontNormal, Element.ALIGN_RIGHT);

            addInfoCell(infoTable, "Giờ vào: " + order.getCreatedAt().format(dtfTime), fontNormal, Element.ALIGN_LEFT);
            addInfoCell(infoTable, "Giờ ra: " + order.getUpdatedAt().format(dtfTime), fontNormal, Element.ALIGN_RIGHT);

            document.add(infoTable);
            // 4. Products Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 0.4f, 2.0f, 0.4f, 1.1f, 0.5f, 1.4f });
            table.setSpacingBefore(5);

            addReceiptHeaderCell(table, "STT", fontBold);
            addReceiptHeaderCell(table, "Tên món", fontBold);
            addReceiptHeaderCell(table, "SL", fontBold);
            addReceiptHeaderCell(table, "Đơn giá", fontBold);
            addReceiptHeaderCell(table, "GG", fontBold);
            addReceiptHeaderCell(table, "Thành tiền", fontBold);

            int stt = 1;
            List<OrderItem> items = order.getOrderItems();
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                boolean isLastItem = (i == items.size() - 1);
                boolean hasToppings = (item.getToppingPerOrderItems() != null
                        && !item.getToppingPerOrderItems().isEmpty());
                int baseBorder = Rectangle.LEFT | Rectangle.RIGHT;
                int mainRowBorder = baseBorder;

                if (isLastItem && !hasToppings) {
                    mainRowBorder |= Rectangle.BOTTOM;
                }

                String productName = item.getProductVariant().getProduct().getName();
                if (item.getProductVariant().getSize() != null) {
                    productName += "(" + item.getProductVariant().getSize().getCode() + ")";
                }

                addReceiptBodyCell(table, String.valueOf(stt++), fontNormal, Element.ALIGN_LEFT, mainRowBorder);
                addReceiptBodyCell(table, productName, fontNormal, Element.ALIGN_LEFT, mainRowBorder);
                addReceiptBodyCell(table, String.valueOf(item.getQuantity()), fontNormal, Element.ALIGN_CENTER,
                        mainRowBorder);
                addReceiptBodyCell(table, String.format("%,d", item.getUnitPrice()), fontNormal, Element.ALIGN_RIGHT,
                        mainRowBorder);
                addReceiptBodyCell(table, "0", fontNormal, Element.ALIGN_RIGHT, mainRowBorder);
                addReceiptBodyCell(table, String.format("%,d", item.getUnitPrice() * item.getQuantity()), fontNormal,
                        Element.ALIGN_RIGHT, mainRowBorder);

                if (hasToppings) {
                    java.util.List<ToppingPerOrderItem> toppings = item.getToppingPerOrderItems();
                    for (int j = 0; j < toppings.size(); j++) {
                        ToppingPerOrderItem topping = toppings.get(j);
                        boolean isLastToppingOfLastItem = (isLastItem && (j == toppings.size() - 1));
                        int toppingBorder = baseBorder;
                        if (isLastToppingOfLastItem) {
                            toppingBorder |= Rectangle.BOTTOM;
                        }

                        addReceiptBodyCell(table, "", fontSmall, Element.ALIGN_LEFT, toppingBorder);
                        addReceiptBodyCell(table, " - " + topping.getTopping().getName(), fontSmall, Element.ALIGN_LEFT,
                                toppingBorder);
                        addReceiptBodyCell(table, String.valueOf(topping.getQuantity()), fontSmall,
                                Element.ALIGN_CENTER, toppingBorder);
                        addReceiptBodyCell(table, String.format("%,d", topping.getPrice()), fontSmall,
                                Element.ALIGN_RIGHT, toppingBorder);
                        addReceiptBodyCell(table, "0", fontSmall, Element.ALIGN_RIGHT, toppingBorder);
                        addReceiptBodyCell(table, String.format("%,d", topping.getPrice() * topping.getQuantity()),
                                fontSmall, Element.ALIGN_RIGHT, toppingBorder);
                    }
                }
            }

            document.add(table);
            // 5. Totals Section
            long basePrice = order.getBasePrice() != null ? order.getBasePrice() : 0L;
            long discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0L;
            long paidPrice = order.getPaidPrice() != null ? order.getPaidPrice() : 0L;

            Paragraph pDiscount = new Paragraph("Tổng giảm giá món: " + String.format("%,d đ", discountAmount),
                    fontNormal);
            document.add(pDiscount);

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[] { 1f, 1f });

            addTotalRow(footerTable, "Thành tiền:", String.format("%,d đ", basePrice), fontNormal, fontNormal);
            addTotalRow(footerTable, "Tổng tiền:", String.format("%,d đ", paidPrice), fontBold, fontBold);

            String paymentGateway = (order.getPaymentGateway() != null ? order.getPaymentGateway().toString()
                    : "TIỀN MẶT");

            addTotalRow(footerTable, "+Thanh toán (" + paymentGateway + ")", String.format("%,d đ", paidPrice),
                    fontNormal, fontNormal);

            document.add(footerTable);
            document.add(new Paragraph(" "));

            // 6. Customer Info
            if (order.getCustomer() != null) {
                document.add(new Paragraph("Khách hàng: "
                        + (order.getCustomer().getFullname() != null ? order.getCustomer().getFullname() : "") + " - "
                        + (order.getCustomer().getPhone() != null ? order.getCustomer().getPhone() : ""), fontNormal));
                document.add(new Paragraph("Thành viên mặc định", fontSmall));
            } else {
                document.add(new Paragraph("Khách lẻ", fontNormal));
            }

            document.add(new Paragraph(" "));

            // 7. Wifi & Promo
            Paragraph wifi = new Paragraph(
                    "WIFI: " + order.getShop().getShopName().toUpperCase() + "\nPASS: "
                            + (order.getShop().getPhone() != null ? order.getShop().getPhone() : "12345678"),
                    fontSmall);
            wifi.setAlignment(Element.ALIGN_CENTER);
            document.add(wifi);

            Paragraph promo = new Paragraph("ĐĂNG KÝ THÀNH VIÊN NHẬN NGAY VOUCHER 20%", fontBold);
            promo.setAlignment(Element.ALIGN_CENTER);
            document.add(promo);

            document.add(new Paragraph(" "));

            PdfPTable qrTable = new PdfPTable(1);
            qrTable.setWidthPercentage(40);
            PdfPCell qrCell = new PdfPCell(new Phrase("\n\n [QR CODE] \n\n", fontSmall));
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setPadding(10);
            qrTable.addCell(qrCell);
            document.add(qrTable);

            document.add(new Paragraph(" "));

            Paragraph thanks = new Paragraph("Cảm ơn Quý Khách", fontNormal);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            Paragraph poweredBy = new Paragraph("Powered by Future&Better", fontSmall);
            poweredBy.setAlignment(Element.ALIGN_CENTER);
            document.add(poweredBy);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating order PDF", e);
            throw new RuntimeException("Lỗi thiết kế hóa đơn Tiếng Việt", e);
        }
    }

    private void addInfoCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addReceiptHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingBottom(5);
        cell.setPaddingTop(5);
        cell.setNoWrap(true);
        table.addCell(cell);
    }

    private void addReceiptBodyCell(PdfPTable table, String text, Font font, int alignment, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(border);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        cell.setNoWrap(true);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fontLabel));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, fontValue));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
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