package com.example.mqttpractice.mqtt;

import com.example.mqttpractice.mqtt.dto.KuraPayload;
import com.example.mqttpractice.mqtt.util.PayloadDecoder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.kura.core.cloud.KuraTopicImpl;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class MqttConsumer {

    private static final PayloadDecoder PAYLOAD_DECODER = new PayloadDecoder();
    private static final String EDC_TOPIC_PATTERN = "\\$EDC/.+/.+/MQTT/.+";  // $EDC/#/#/MQTT/# 패턴



    @Router(inputChannel = "mqttInputChannel")
    public String route(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);

        if (topic != null && topic.matches(EDC_TOPIC_PATTERN)) {
            return "edcChannel";
        }
        return "defaultChannel";
    }

    @ServiceActivator(inputChannel = "edcChannel")
    public void handleEdcMessage(Message<?> message) {
        log.info("Received EDC message from topic: {}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));
        KuraPayload kuraPayload = PAYLOAD_DECODER.decode(message.getPayload());
        // EDC 메시지 처리 로직
        log.info("Processed EDC message: {}", kuraPayload);

    }

    @ServiceActivator(inputChannel = "defaultChannel")
    public void handleDefaultMessage(Message<?> message) {
        log.info("Received default message from topic: {}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));

        if(Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString().contains("REPLY")){
            KuraTopicImpl kuraTopic = new KuraTopicImpl(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString(), "$EDC");
            KuraPayload kuraPayload = PAYLOAD_DECODER.decode(message.getPayload());
            String replyPayload = null;
            if(kuraPayload.getBody() != null && kuraPayload.getBody().length > 0){
                replyPayload = new String(kuraPayload.getBody());
                System.out.println(replyPayload);
            }
            log.info("Processed default message: {}", kuraPayload);
        } else {
            KuraPayload kuraPayload = PAYLOAD_DECODER.decode(message.getPayload());
            // 일반 메시지 처리 로직
            log.info("Processed default message: {}", kuraPayload);
        }
    }
}