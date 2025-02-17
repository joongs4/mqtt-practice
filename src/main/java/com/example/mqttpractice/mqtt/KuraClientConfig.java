package com.example.mqttpractice.mqtt;


import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class KuraClientConfig {
    //
    private static final String BASIC_TOKEN = "Basic ZWRnZWh1YjpiaXphZG1pbg==";

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, BASIC_TOKEN)
                //TODO : local test시 ssl 인증서 무시
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .secure(spec -> {
                                    try {
                                        spec.sslContext(
                                                SslContextBuilder.forClient()
                                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                                        .build()
                                        );
                                    } catch (SSLException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                ));
    }
}
