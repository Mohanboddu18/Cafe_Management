package com.cafe.management.service;

import com.cafe.management.dto.BillingDTOs.*;
import com.cafe.management.dto.OrderDTOs;
import com.cafe.management.entity.*;
import com.cafe.management.exception.BadRequestException;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BillingInvoiceService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private OrderService orderService;

    public ApplyCouponResponse validateAndApplyCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            return ApplyCouponResponse.builder().valid(false).message("Invalid coupon code").build();
        }

        Coupon coupon = couponRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElse(null);

        if (coupon == null || coupon.getValidUntil().isBefore(LocalDate.now())) {
            return ApplyCouponResponse.builder().valid(false).message("Coupon code is invalid or expired").build();
        }

        if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            return ApplyCouponResponse.builder().valid(false)
                    .message("Order subtotal must be at least $" + coupon.getMinOrderAmount() + " to use this coupon").build();
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else if ("FLAT".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = coupon.getDiscountValue();
        }

        BigDecimal net = subtotal.subtract(discount);
        if (net.compareTo(BigDecimal.ZERO) < 0) net = BigDecimal.ZERO;

        return ApplyCouponResponse.builder()
                .couponCode(coupon.getCode())
                .discountAmount(discount)
                .netSubtotal(net)
                .valid(true)
                .message("Coupon applied successfully!")
                .build();
    }

    @Transactional
    public InvoiceResponse processPayment(ProcessPaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            ApplyCouponResponse couponRes = validateAndApplyCoupon(request.getCouponCode(), subtotal);
            if (couponRes.isValid()) {
                discountAmount = couponRes.getDiscountAmount();
            }
        }

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discountAmount);
        BigDecimal gstPercentage = request.getGstPercentage() != null ? request.getGstPercentage() : BigDecimal.valueOf(5.0);
        BigDecimal gstAmount = subtotalAfterDiscount.multiply(gstPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = subtotalAfterDiscount.add(gstAmount);

        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(gstAmount);
        order.setNetAmount(totalPayable);
        order.setStatus("PAID");
        orderRepository.save(order);

        String invNum = "INV-" + System.currentTimeMillis() % 1000000;

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invNum)
                .order(order)
                .subtotal(subtotal)
                .discount(discountAmount)
                .couponCode(request.getCouponCode())
                .gstAmount(gstAmount)
                .totalPayable(totalPayable)
                .pdfUrl("/api/cashier/invoice/" + invNum + "/pdf")
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment payment = Payment.builder()
                .invoice(savedInvoice)
                .paymentMethod(request.getPaymentMethod().toUpperCase())
                .transactionRef(request.getTransactionRef())
                .paymentStatus("COMPLETED")
                .build();
        paymentRepository.save(payment);

        // Reset Table Status to AVAILABLE
        RestaurantTable table = order.getTable();
        table.setStatus("AVAILABLE");
        tableRepository.save(table);

        return mapToInvoiceResponse(savedInvoice, payment);
    }

    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order id: " + orderId));
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        return mapToInvoiceResponse(invoice, payment);
    }

    public byte[] generateInvoicePdfBytes(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        Order order = invoice.getOrder();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            // Header Title
            doc.add(new Paragraph("ARTISANAL CAFE & BISTRO")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("123 Gourmet Street, Foodville | Tel: +1 (555) 019-2831")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("----------------------------------------------------------------------------------------------------")
                    .setFontColor(ColorConstants.LIGHT_GRAY));

            // Metadata Table
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            metaTable.addCell(new Cell().add(new Paragraph("Invoice No: " + invoice.getInvoiceNumber()).setBold()).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Date: " + invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Table No: " + order.getTable().getTableNumber())).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Order No: " + order.getOrderNumber()).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            doc.add(metaTable);

            doc.add(new Paragraph("\n"));

            // Items Table
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20})).useAllAvailableWidth();
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Item").setBold()));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setTextAlignment(TextAlignment.CENTER)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Unit Price").setBold().setTextAlignment(TextAlignment.RIGHT)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)));

            for (OrderItem item : order.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getMenuItem().getName())));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setTextAlignment(TextAlignment.CENTER)));
                itemsTable.addCell(new Cell().add(new Paragraph("$" + item.getUnitPrice()).setTextAlignment(TextAlignment.RIGHT)));
                itemsTable.addCell(new Cell().add(new Paragraph("$" + item.getTotalPrice()).setTextAlignment(TextAlignment.RIGHT)));
            }

            doc.add(itemsTable);
            doc.add(new Paragraph("\n"));

            // Calculations Summary
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            summaryTable.addCell(new Cell().add(new Paragraph("Subtotal:")).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("$" + invoice.getSubtotal()).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            if (invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("Discount (" + (invoice.getCouponCode() != null ? invoice.getCouponCode() : "Special") + "):")).setBorder(null));
                summaryTable.addCell(new Cell().add(new Paragraph("-$" + invoice.getDiscount()).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            }

            summaryTable.addCell(new Cell().add(new Paragraph("GST Tax (5%):")).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("+$" + invoice.getGstAmount()).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            summaryTable.addCell(new Cell().add(new Paragraph("TOTAL PAYABLE:").setFontSize(14).setBold()).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("$" + invoice.getTotalPayable()).setFontSize(14).setBold().setFontColor(ColorConstants.BLUE).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            doc.add(summaryTable);

            doc.add(new Paragraph("\n\nThank you for dining with us! Please come again.")
                    .setFontSize(11)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice, Payment payment) {
        OrderDTOs.OrderResponse orderRes = orderService.mapToOrderResponse(invoice.getOrder());
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrder().getId())
                .orderNumber(invoice.getOrder().getOrderNumber())
                .tableNumber(invoice.getOrder().getTable().getTableNumber())
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscount())
                .couponCode(invoice.getCouponCode())
                .gstAmount(invoice.getGstAmount())
                .totalPayable(invoice.getTotalPayable())
                .paymentMethod(payment != null ? payment.getPaymentMethod() : "PENDING")
                .paymentStatus(payment != null ? payment.getPaymentStatus() : "UNPAID")
                .pdfUrl(invoice.getPdfUrl())
                .createdAt(invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .items(orderRes.getItems())
                .build();
    }
}
