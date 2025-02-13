package com.example.mqttpractice.mqtt.util;

import com.example.mqttpractice.mqtt.dto.KuraPayload;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
public class PayloadDecoder {
    //
    public KuraPayload decode(Object messagePayload) {
        //
        byte[] payloadBytes = convertToBytes(messagePayload);
        logInput(payloadBytes);

        if(isGzip(payloadBytes)) {
            return decodeGzip(payloadBytes);
        } else {
            return decodePayload(payloadBytes);
        }
    }

    private boolean isGzip(byte[] data) {
        return data.length > 2 && data[0] == (byte) 0x1F && data[1] == (byte) 0x8B;
    }

    private KuraPayload decodeGzip(byte[] payloadBytes) {
        //
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(payloadBytes));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            // 압축 해제된 데이터
            byte[] uncompressedData = outputStream.toByteArray();

            // Protocol Buffers 디코딩
            KuraPayload kuraPayload = decodePayload(uncompressedData);
            log.info("Decoded payload: {}", kuraPayload);
            return kuraPayload;

        } catch (Exception e) {
            log.error("Failed to decode payload", e);
            throw new RuntimeException("Failed to decode payload", e);
        }
    }

    private KuraPayload decodePayload(byte[] payloadBytes) {


        KuraPayload payload = new KuraPayload();
        KuraPayloadProto.KuraPayload protoPayload;

        try {
            // Protocol Buffers 형식으로 디코딩
            protoPayload = KuraPayloadProto.KuraPayload.parseFrom(payloadBytes);

            // Timestamp 설정
            if (protoPayload.hasTimestamp()) {
                payload.setTimestamp(new Date(protoPayload.getTimestamp()));
            }

            // Position 설정
            if (protoPayload.hasPosition()) {
                KuraPayloadProto.KuraPayload.KuraPosition protoPos = protoPayload.getPosition();
                KuraPayload.Position position = new KuraPayload.Position();

                if (protoPos.hasLatitude()) position.setLatitude(protoPos.getLatitude());
                if (protoPos.hasLongitude()) position.setLongitude(protoPos.getLongitude());
                if (protoPos.hasAltitude()) position.setAltitude(protoPos.getAltitude());
                if (protoPos.hasPrecision()) position.setPrecision(protoPos.getPrecision());
                if (protoPos.hasHeading()) position.setHeading(protoPos.getHeading());
                if (protoPos.hasSpeed()) position.setSpeed(protoPos.getSpeed());
                if (protoPos.hasTimestamp()) position.setTimestamp(new Date(protoPos.getTimestamp()));
                if (protoPos.hasSatellites()) position.setSatellites(protoPos.getSatellites());
                if (protoPos.hasStatus()) position.setStatus(protoPos.getStatus());

                payload.setPosition(position);
            }

            // Metrics 설정
            Map<String, Object> metrics = new HashMap<>();
            for (KuraPayloadProto.KuraPayload.KuraMetric protoMetric : protoPayload.getMetricList()) {
                KuraPayload.Metric metric = new KuraPayload.Metric();
                metric.setName(protoMetric.getName());

                switch (protoMetric.getType()) {
                    case STRING:
                        metric.setType(KuraPayload.Metric.ValueType.STRING);
                        metric.setValue(protoMetric.getStringValue());
                        break;
                    case DOUBLE:
                        metric.setType(KuraPayload.Metric.ValueType.DOUBLE);
                        metric.setValue(protoMetric.getDoubleValue());
                        break;
                    case INT32:
                        metric.setType(KuraPayload.Metric.ValueType.INT32);
                        metric.setValue(protoMetric.getIntValue());
                        break;
                    case FLOAT:
                        metric.setType(KuraPayload.Metric.ValueType.FLOAT);
                        metric.setValue(protoMetric.getFloatValue());
                        break;
                    case INT64:
                        metric.setType(KuraPayload.Metric.ValueType.INT64);
                        metric.setValue(protoMetric.getLongValue());
                        break;
                    case BOOL:
                        metric.setType(KuraPayload.Metric.ValueType.BOOL);
                        metric.setValue(protoMetric.getBoolValue());
                        break;
                    case BYTES:
                        metric.setType(KuraPayload.Metric.ValueType.BYTES);
                        metric.setValue(protoMetric.getBytesValue().toByteArray());
                        break;
                }

                metrics.put(metric.getName(), metric);
            }
            payload.setMetrics(metrics);

            // Body 설정
            if (protoPayload.hasBody()) {
                payload.setBody(protoPayload.getBody().toByteArray());
            }

        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to decode protobuf message", e);
        }

        return payload;
    }


    private void logInput(byte[] payload) {
        // 16진수로 출력
        StringBuilder hexString = new StringBuilder();
        for (byte b : payload) {
            hexString.append(String.format("%02X ", b));
        }
        log.info("Received payload (hex): {}", hexString.toString());
    }

    private byte[] convertToBytes(Object payload) {
        if (payload instanceof byte[]) {
            return (byte[]) payload;
        } else if (payload instanceof String) {
            return ((String) payload).getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Unsupported payload type: " +
                payload.getClass().getName());
    }
}