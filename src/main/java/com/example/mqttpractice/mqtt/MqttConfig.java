package com.example.mqttpractice.mqtt;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.AttributeAccessor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${spring.mqtt.broker.url}")
    private String brokerUrl;

    @Value("${spring.mqtt.broker.username}")
    private String username;

    @Value("${spring.mqtt.broker.password}")
    private String password;

    @Value("${spring.mqtt.client.id}")
    private String clientId;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[] { brokerUrl });
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler("client-producer", mqttClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("default-topic");  // 기본 토픽 설정 (선택사항)
        return messageHandler;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound(
            MqttPahoClientFactory mqttClientFactory,
            @Qualifier(value = "mqttInputChannel") MessageChannel mqttInputChannel,
            @Qualifier(value = "errorChannel") MessageChannel errorChannel,
            @Value("${spring.mqtt.client.id}-consumer") String clientId,
            @Value("${spring.mqtt.topics[0].name}") String topic) {

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory, "#");
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);  // 바이트 배열로 직접 받기
        adapter.setConverter(converter);
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel);

        adapter.setErrorMessageStrategy(new ErrorMessageStrategy() {
            @Override
            public ErrorMessage buildErrorMessage(Throwable t, AttributeAccessor attributes) {
                log.error("Error occurred while processing message", t);
                return new ErrorMessage(t);
            }
        });
        return adapter;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel edcMqttChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel edcReplyChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel defaultChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
}
