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

    @Autowired
    private NotificationService notificationService;

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

        Invoice invoice = invoiceRepository.findByOrderId(order.getId())
                .orElseGet(() -> Invoice.builder()
                        .invoiceNumber("INV-" + System.currentTimeMillis() % 1000000)
                        .order(order)
                        .build());

        invoice.setSubtotal(subtotal);
        invoice.setDiscount(discountAmount);
        invoice.setCouponCode(request.getCouponCode());
        invoice.setGstAmount(gstAmount);
        invoice.setTotalPayable(totalPayable);
        invoice.setPaymentStatus("COMPLETED");
        invoice.setPdfUrl("/api/cashier/invoice/" + order.getId() + "/pdf");
        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment payment = paymentRepository.findByInvoiceId(savedInvoice.getId()).stream().findFirst().orElseGet(() ->
                Payment.builder().invoice(savedInvoice).build()
        );
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH");
        payment.setTransactionRef(request.getTransactionRef() != null ? request.getTransactionRef() : ("TXN-" + System.currentTimeMillis()));
        payment.setPaymentStatus("COMPLETED");
        Payment savedPayment = paymentRepository.save(payment);

        // Reset Table Status to AVAILABLE
        RestaurantTable table = order.getTable();
        table.setStatus("AVAILABLE");
        table.setCurrentSessionId(null);
        table.setCurrentTokenSerial(null);
        tableRepository.save(table);

        return mapToInvoiceResponse(savedInvoice, savedPayment);
    }

    @Transactional
    public InvoiceResponse generateBillForCustomer(GenerateInvoiceRequest request) {
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
        order.setStatus("BILL_REQUESTED");
        orderRepository.save(order);

        Invoice invoice = invoiceRepository.findByOrderId(order.getId())
                .orElseGet(() -> Invoice.builder()
                        .invoiceNumber("INV-" + System.currentTimeMillis() % 1000000)
                        .order(order)
                        .subtotal(subtotal)
                        .discount(finalDiscount)
                        .couponCode(request.getCouponCode())
                        .gstAmount(gstAmount)
                        .totalPayable(totalPayable)
                        .paymentStatus("PENDING")
                        .createdAt(LocalDateTime.now())
                        .pdfUrl("/api/cashier/invoice/" + order.getId() + "/pdf")
                        .build()
                );

        invoice.setSubtotal(subtotal);
        invoice.setDiscount(discountAmount);
        invoice.setCouponCode(request.getCouponCode());
        invoice.setGstAmount(gstAmount);
        invoice.setTotalPayable(totalPayable);
        invoice.setPaymentStatus("PENDING");
        if (invoice.getCreatedAt() == null) {
            invoice.setCreatedAt(LocalDateTime.now());
        }
        Invoice savedInvoice = invoiceRepository.save(invoice);

        String billMsg = "Bill Invoice generated for Table #" + order.getTable().getTableNumber() + " (Total: ₹" + totalPayable + "). Customer can now pay via UPI, Card, or Cash.";
        notificationService.sendNotification("WAITER", "📄 Bill Invoice Generated", billMsg, order.getId(), order.getTable().getId());
        notificationService.sendNotification("CASHIER", "📄 Bill Invoice Generated", billMsg, order.getId(), order.getTable().getId());

        return mapToInvoiceResponse(savedInvoice, null);
    }

    @Transactional
    public InvoiceResponse customerPayInvoice(CustomerPaymentRequest request) {
        Invoice invoice = invoiceRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order"));
        Order order = invoice.getOrder();
        RestaurantTable table = order.getTable();

        String pMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH";

        Payment payment = paymentRepository.findByInvoiceId(invoice.getId()).stream().findFirst().orElseGet(() ->
                Payment.builder().invoice(invoice).build()
        );
        payment.setPaymentMethod(pMethod);
        payment.setTransactionRef(request.getTransactionRef() != null ? request.getTransactionRef() : (pMethod + "-" + System.currentTimeMillis() % 100000));

        if ("CASH".equalsIgnoreCase(pMethod)) {
            invoice.setPaymentStatus("CASH_PENDING_WAITER");
            payment.setPaymentStatus("PENDING");
            invoiceRepository.save(invoice);
            paymentRepository.save(payment);

            String cashAlertMsg = "💵 CASH PAYMENT ALERT: Table #" + table.getTableNumber() + " selected CASH payment (₹" + invoice.getTotalPayable() + "). Waiter, please collect cash from table and submit to Cashier!";
            notificationService.sendNotification("WAITER", "💵 Cash Payment Requested", cashAlertMsg, order.getId(), table.getId());
            notificationService.sendNotification("CASHIER", "💵 Cash Payment Requested", cashAlertMsg, order.getId(), table.getId());
        } else {
            // Online Payment (UPI / CARD)
            invoice.setPaymentStatus("COMPLETED");
            payment.setPaymentStatus("COMPLETED");
            order.setStatus("PAID");
            orderRepository.save(order);
            invoiceRepository.save(invoice);
            paymentRepository.save(payment);

            // Free Table in MySQL
            table.setStatus("AVAILABLE");
            table.setCurrentSessionId(null);
            table.setCurrentTokenSerial(null);
            tableRepository.save(table);

            String onlinePayMsg = "✅ Table #" + table.getTableNumber() + " Paid ₹" + invoice.getTotalPayable() + " via " + pMethod + "! Table is now FREE.";
            notificationService.sendNotification("WAITER", "✅ Payment Received", onlinePayMsg, order.getId(), table.getId());
            notificationService.sendNotification("CASHIER", "✅ Payment Received", onlinePayMsg, order.getId(), table.getId());
        }

        return mapToInvoiceResponse(invoice, payment);
    }

    @Transactional
    public InvoiceResponse confirmCashPayment(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order id: " + orderId));
        Order order = invoice.getOrder();
        RestaurantTable table = order.getTable();

        invoice.setPaymentStatus("COMPLETED");
        order.setStatus("PAID");
        orderRepository.save(order);
        invoiceRepository.save(invoice);

        Payment payment = paymentRepository.findByInvoiceId(invoice.getId()).stream().findFirst().orElseGet(() ->
                Payment.builder().invoice(invoice).paymentMethod("CASH").build()
        );
        payment.setPaymentStatus("COMPLETED");
        paymentRepository.save(payment);

        // Free Table in MySQL
        table.setStatus("AVAILABLE");
        table.setCurrentSessionId(null);
        table.setCurrentTokenSerial(null);
        tableRepository.save(table);

        String cashSuccessMsg = "💵 Cash Payment Confirmed for Table #" + table.getTableNumber() + " (₹" + invoice.getTotalPayable() + "). Table is now FREE.";
        notificationService.sendNotification("WAITER", "💵 Cash Confirmed", cashSuccessMsg, order.getId(), table.getId());
        notificationService.sendNotification("CASHIER", "💵 Cash Confirmed", cashSuccessMsg, order.getId(), table.getId());

        return mapToInvoiceResponse(invoice, payment);
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
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        String pMethod = payment != null ? payment.getPaymentMethod() : "UNPAID";
        String pStatus = invoice.getPaymentStatus();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            com.itextpdf.kernel.geom.PageSize thermalPage = new com.itextpdf.kernel.geom.PageSize(226, 680);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(thermalPage);
            Document doc = new Document(pdfDoc);
            doc.setMargins(10, 10, 10, 10);

            // Header Title
            doc.add(new Paragraph("ARTISANAL CAFE & BISTRO")
                    .setFontSize(13)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(1));

            doc.add(new Paragraph("Near Godavari River, Narsapur\nTel: +91 9876543210")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            doc.add(new Paragraph("--------------------------------------------------")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2));

            doc.add(new Paragraph("*** RECEIPT ***")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            // Metadata Table
            LocalDateTime invDate = invoice.getCreatedAt() != null ? invoice.getCreatedAt() : LocalDateTime.now();
            metaTable.addCell(new Cell().add(new Paragraph("Inv #: " + invoice.getInvoiceNumber()).setFontSize(8).setBold()).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Date: " + invDate.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"))).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Table: #" + order.getTable().getTableNumber()).setFontSize(8)).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Order #: " + order.getOrderNumber()).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            doc.add(metaTable);

            doc.add(new Paragraph("--------------------------------------------------")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(2)
                    .setMarginBottom(4));

            // Items Table
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{45, 15, 20, 20})).useAllAvailableWidth();
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("ITEM").setFontSize(8).setBold()).setBorder(null));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("QTY").setFontSize(8).setBold().setTextAlignment(TextAlignment.CENTER)).setBorder(null));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("PRICE").setFontSize(8).setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("TOTAL").setFontSize(8).setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            for (OrderItem item : order.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getMenuItem().getName()).setFontSize(8)).setBorder(null));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(8).setTextAlignment(TextAlignment.CENTER)).setBorder(null));
                itemsTable.addCell(new Cell().add(new Paragraph("₹" + item.getUnitPrice()).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
                itemsTable.addCell(new Cell().add(new Paragraph("₹" + item.getTotalPrice()).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            }

            doc.add(itemsTable);

            doc.add(new Paragraph("--------------------------------------------------")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(4)
                    .setMarginBottom(4));

            // Calculations Summary
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            summaryTable.addCell(new Cell().add(new Paragraph("SUBTOTAL:").setFontSize(8)).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getSubtotal()).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("DISCOUNT (" + (invoice.getCouponCode() != null ? invoice.getCouponCode() : "PROMO") + "):").setFontSize(8)).setBorder(null));
                summaryTable.addCell(new Cell().add(new Paragraph("-₹" + invoice.getDiscount()).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            }

            summaryTable.addCell(new Cell().add(new Paragraph("GST TAX (5%):").setFontSize(8)).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("+₹" + invoice.getGstAmount()).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            summaryTable.addCell(new Cell().add(new Paragraph("TOTAL PAYABLE:").setFontSize(10).setBold()).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getTotalPayable()).setFontSize(10).setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            summaryTable.addCell(new Cell().add(new Paragraph("PAYMENT METHOD:").setFontSize(8)).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph(pMethod).setFontSize(8).setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            summaryTable.addCell(new Cell().add(new Paragraph("PAYMENT STATUS:").setFontSize(8)).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph(pStatus).setFontSize(8).setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(null));

            doc.add(summaryTable);

            doc.add(new Paragraph("--------------------------------------------------")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(4)
                    .setMarginBottom(6));

            doc.add(new Paragraph("THANK YOU FOR DINING WITH US!\nPLEASE VISIT AGAIN")
                    .setFontSize(8)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8));

            doc.add(new Paragraph("||| || ||||| |||| || |||| ||| |||||||")
                    .setFontSize(10)
                    .setBold()
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
                .paymentStatus(payment != null ? payment.getPaymentStatus() : (invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "PENDING"))
                .pdfUrl(invoice.getPdfUrl())
                .createdAt(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .items(orderRes.getItems())
                .build();
    }
}
