package com.example.mqttpractice.mqtt.util;

import com.example.mqttpractice.mqtt.dto.KuraPayload;
import com.google.protobuf.ByteString;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto;
import java.util.Map;

public class PayloadEncoder {

    public byte[] encode(KuraPayload payload) {
        try {
            KuraPayloadProto.KuraPayload.Builder protoMsg = KuraPayloadProto.KuraPayload.newBuilder();

            // Set timestamp if exists
            if (payload.getTimestamp() != null) {
                protoMsg.setTimestamp(payload.getTimestamp().getTime());
            }

            // Set position if exists
            if (payload.getPosition() != null) {
                KuraPayloadProto.KuraPayload.KuraPosition.Builder positionBuilder =
                        KuraPayloadProto.KuraPayload.KuraPosition.newBuilder();

                if (payload.getPosition().getLatitude() != null) {
                    positionBuilder.setLatitude(payload.getPosition().getLatitude());
                }
                if (payload.getPosition().getLongitude() != null) {
                    positionBuilder.setLongitude(payload.getPosition().getLongitude());
                }
                // ... set other position fields similarly

                protoMsg.setPosition(positionBuilder.build());
            }

            // Set metrics if exists
            if (payload.getMetrics() != null) {
                for (Map.Entry<String, Object> entry : payload.getMetrics().entrySet()) {
                    KuraPayloadProto.KuraPayload.KuraMetric.Builder metricBuilder =
                            KuraPayloadProto.KuraPayload.KuraMetric.newBuilder();

                    metricBuilder.setName(entry.getKey());
//                    KuraPayload.Metric metric = (KuraPayload.Metric)entry.getValue();

                    KuraPayload.Metric.ValueType valueType = KuraPayload.Metric.ValueType.fromObject(entry.getValue());

                    switch (valueType) {
                        case STRING:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.STRING);
                            metricBuilder.setStringValue((String) entry.getValue());
                            break;
                        case DOUBLE:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.DOUBLE);
                            metricBuilder.setDoubleValue((Double) entry.getValue());
                            break;
                        case INT32:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.INT32);
                            metricBuilder.setIntValue((Integer) entry.getValue());
                            break;
                        case FLOAT:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.FLOAT);
                            metricBuilder.setFloatValue((Float) entry.getValue());
                            break;
                        case INT64:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.INT64);
                            metricBuilder.setLongValue((Long) entry.getValue());
                            break;
                        case BOOL:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.BOOL);
                            metricBuilder.setBoolValue((Boolean) entry.getValue());
                            break;
                        case BYTES:
                            metricBuilder.setType(KuraPayloadProto.KuraPayload.KuraMetric.ValueType.BYTES);
                            metricBuilder.setBytesValue(ByteString.copyFrom((byte[]) entry.getValue()));
                            break;
                    }

                    protoMsg.addMetric(metricBuilder.build());
                }
            }

            // Set body if exists
            if (payload.getBody() != null) {
                protoMsg.setBody(ByteString.copyFrom(payload.getBody()));
            }

            return protoMsg.build().toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to encode payload", e);
        }
    }
}
