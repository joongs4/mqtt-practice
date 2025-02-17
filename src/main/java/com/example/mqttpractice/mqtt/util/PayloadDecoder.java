package com.example.mqttpractice.mqtt.util;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Slf4j
public class PayloadDecoder {
    //
    public static KuraPayloadProto.KuraPayload decode(Object messagePayload) {
        //
        byte[] payloadBytes = convertToBytes(messagePayload);
//        logInput(payloadBytes);

        if(isGzip(payloadBytes)) {
            return decodeGzip(payloadBytes);
        } else {
            return decodePayload(payloadBytes);
        }
    }

    private static boolean isGzip(byte[] data) {
        return data.length > 2 && data[0] == (byte) 0x1F && data[1] == (byte) 0x8B;
    }

    private static KuraPayloadProto.KuraPayload decodeGzip(byte[] payloadBytes) {
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

            KuraPayloadProto.KuraPayload payload = KuraPayloadProto.KuraPayload.parseFrom(uncompressedData);
            log.info("Decoded payload: {}", payload);
            return payload;

        } catch (Exception e) {
            log.error("Failed to decode payload", e);
            throw new RuntimeException("Failed to decode payload", e);
        }
    }

    private static KuraPayloadProto.KuraPayload decodePayload(byte[] payloadBytes) {

        try{
            return KuraPayloadProto.KuraPayload.parseFrom(payloadBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to decode payload", e);
            throw new RuntimeException("Failed to decode payload", e);
        }
    }


    private static void logInput(byte[] payload) {
        // 16진수로 출력
        StringBuilder hexString = new StringBuilder();
        for (byte b : payload) {
            hexString.append(String.format("%02X ", b));
        }
        log.info("Received payload (hex): {}", hexString.toString());
    }

    private static byte[] convertToBytes(Object payload) {
        if (payload instanceof byte[]) {
            return (byte[]) payload;
        } else if (payload instanceof String) {
            return ((String) payload).getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Unsupported payload type: " +
                payload.getClass().getName());
    }
}