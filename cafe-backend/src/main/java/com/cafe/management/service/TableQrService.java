package com.cafe.management.service;

import com.cafe.management.entity.QrCodeEntity;
import com.cafe.management.entity.RestaurantTable;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.QrCodeRepository;
import com.cafe.management.repository.RestaurantTableRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class TableQrService {

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    public RestaurantTable createTable(Integer tableNumber, Integer capacity) {
        String token = "TBL-QR-" + String.format("%03d", tableNumber) + "-" + UUID.randomUUID().toString().substring(0, 6);
        String targetUrl = "http://localhost:4200/customer/menu?table=" + tableNumber;

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(tableNumber)
                .capacity(capacity)
                .status("AVAILABLE")
                .qrToken(token)
                .qrCodeUrl(targetUrl)
                .build();

        RestaurantTable savedTable = tableRepository.save(table);

        // Generate Base64 QR Image
        String base64Image = generateQrCodeBase64(targetUrl, 300, 300);

        QrCodeEntity qrCodeEntity = QrCodeEntity.builder()
                .table(savedTable)
                .qrData(targetUrl)
                .imageBase64(base64Image)
                .build();

        qrCodeRepository.save(qrCodeEntity);

        return savedTable;
    }

    public String generateQrCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR Code", e);
        }
    }

    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    public RestaurantTable getTableByNumber(Integer tableNumber) {
        return tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with table number: " + tableNumber));
    }

    public QrCodeEntity getQrCodeByTableId(Long tableId) {
        return qrCodeRepository.findByTableId(tableId)
                .orElseGet(() -> {
                    RestaurantTable table = tableRepository.findById(tableId)
                            .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + tableId));
                    String base64 = generateQrCodeBase64(table.getQrCodeUrl(), 300, 300);
                    QrCodeEntity entity = QrCodeEntity.builder()
                            .table(table)
                            .qrData(table.getQrCodeUrl())
                            .imageBase64(base64)
                            .build();
                    return qrCodeRepository.save(entity);
                });
    }

    public RestaurantTable updateTableStatus(Long tableId, String status) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        table.setStatus(status.toUpperCase());
        return tableRepository.save(table);
    }
}
