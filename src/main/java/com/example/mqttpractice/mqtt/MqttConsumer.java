package com.example.mqttpractice.mqtt;

import com.example.mqttpractice.mqtt.dto.EsfConfigurations;
import com.example.mqttpractice.mqtt.util.PayloadDecoder;
import com.google.protobuf.ByteString;
import io.micrometer.common.util.StringUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.kura.core.message.protobuf.KuraPayloadProto;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringSource;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MqttConsumer {

    private static final String EDC_MQTT_TOPIC_PATTERN = "\\$EDC/.+/.+/MQTT/.+";  // $EDC/#/#/MQTT/# 패턴
    private static final String EDC_REPLY_TOPIC_PATTERN = "\\$EDC/.+/.+/.*/REPLY/.+";  // $EDC/kura/spring-mqtt-client/CONF-V1/REPLY/ddcea6be-e0a1-4445-87ea-6a2ee39bd6d1

    private static final Unmarshaller unmarshaller;

    static {
        try {
            JAXBContext context = JAXBContext.newInstance(EsfConfigurations.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        ;
    }


    @Router(inputChannel = "mqttInputChannel")
    public String route(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        if (StringUtils.isEmpty(topic)) {
            return "defaultChannel";
        }

        if (topic.matches(EDC_MQTT_TOPIC_PATTERN)) {
            return "edcMqttChannel";
        } else if (topic.matches(EDC_REPLY_TOPIC_PATTERN)) {
            return "edcReplyChannel";
        }
        return "defaultChannel";
    }

    @ServiceActivator(inputChannel = "edcMqttChannel")
    public void handleEdcMqttMessage(Message<?> message) {
        log.info("Received EDC message from topic: {}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));
    }

    @ServiceActivator(inputChannel = "edcReplyChannel")
    public void handleEdcReplyMessage(Message<?> message) {
        log.info("Received EDC Reply message from topic: {}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));

        KuraPayloadProto.KuraPayload payload = PayloadDecoder.decode(message.getPayload());
        EsfConfigurations esfConfigurations = unmarshal(payload.getBody(), EsfConfigurations.class);
    }

    private <T> T unmarshal(ByteString byteString, Class<T> targetClass) {
        //
        try {
            return unmarshaller.unmarshal(new StringSource(byteString.toString(StandardCharsets.UTF_8)), targetClass).getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @ServiceActivator(inputChannel = "defaultChannel")
    public void handleDefaultMessage(Message<?> message) {
        log.info("Received default message from topic: {}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));

        KuraPayloadProto.KuraPayload payload = PayloadDecoder.decode(message.getPayload());
    }
}