package com.example.mqttpractice.mqtt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KuraWebClient {
    //
    private final WebClient.Builder webClientBuilder;

    public String test(){

        String a = getDriverPids("https://localhost:443");

        return a;
    }

    public String getDriverPids(String baseUrl) {
        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/services/wire/v1/drivers/pids")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    log.error("Failed to fetch driver PIDs from {}", baseUrl, error);
                    return Mono.empty();
                }).block();
    }
}
