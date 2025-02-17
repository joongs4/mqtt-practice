package com.example.mqttpractice.rest;


import com.example.mqttpractice.mqtt.KuraWebClient;
import com.example.mqttpractice.mqtt.MqttProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestResource {
    //
    private final MqttProducer mqttProducer;
    private final KuraWebClient kuraWebClient;

    @GetMapping
    public String test() {
        //
        mqttProducer.publishMessage();
        return UUID.randomUUID().toString();
    }

    @GetMapping("2")
    public String test2(){
        return kuraWebClient.test();
    }
}
