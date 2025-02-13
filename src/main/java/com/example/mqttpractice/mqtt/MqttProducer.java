package com.example.mqttpractice.mqtt;

import com.example.mqttpractice.mqtt.dto.KuraPayload;
import com.example.mqttpractice.mqtt.util.PayloadEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MqttProducer {

    private final MqttGateway mqttGateway;
    private final PayloadEncoder ENCODER = new PayloadEncoder();

    public static final String METRIC_REQUEST_ID = "request.id";
    public static final String REQUESTER_CLIENT_ID = "requester.client.id";

    private static final String KURA_CLIENT_ID = "kura-cloud-connection-1";

    public void publishMessage() {
//        requestAssetList();
//        requestAssetValues("A1A");
        updateConfigurations();
    }

    private void requestAssetList() {
        String requestTopic = "$EDC/kura/" + KURA_CLIENT_ID + "/ASSET-V1/GET/assets";

        Map<String, Object> metrics = new HashMap<>();
        metrics.put(METRIC_REQUEST_ID, UUID.randomUUID().toString());
        metrics.put(REQUESTER_CLIENT_ID, "spring-mqtt-client");

        KuraPayload payload = new KuraPayload();
        payload.setMetrics(metrics);
        // 필요한 경우 페이로드에 추가 정보 설정

        byte[] data = ENCODER.encode(payload);  // Protocol Buffers로 인코딩
        mqttGateway.sendToMqtt(requestTopic, data);
    }

    private void requestAssetValues(String assetName) {
        String requestTopic = "$EDC/kura/" + KURA_CLIENT_ID + "/ASSET-V1/EXEC/read";

        String jsonRequest = "[{" +
                "\"name\": \"A1A\"," +          // Asset 이름
                "\"channels\": [" +             // 읽고자 하는 채널들
                "\"humidity\"" +
                "]" +
                "}]";

        Map<String, Object> properties = new HashMap<>();
        properties.put("request.args", Arrays.asList("read"));

        Map<String, Object> metrics = new HashMap<>();
        metrics.put(METRIC_REQUEST_ID, UUID.randomUUID().toString());
        metrics.put(REQUESTER_CLIENT_ID, "spring-mqtt-client");

        KuraPayload payload = new KuraPayload();
        payload.setMetrics(metrics);
        payload.setBody(jsonRequest.getBytes(StandardCharsets.UTF_8));

        byte[] data = ENCODER.encode(payload);
        mqttGateway.sendToMqtt(requestTopic, data);
    }

    private void updateConfigurations() {
        final String SNAPSHOT_RESOURCE_PATH = "snapshotToUpload.xml";
        final String SNAPSHOT_UPLOAD_TOPIC = "$EDC/kura/" + KURA_CLIENT_ID + "/CONF-V1/PUT/configurations";
        byte[] snapshotXml = loadSnapshotFile(SNAPSHOT_RESOURCE_PATH);

        // 스냅샷 내용을 base64로 인코딩
        String base64Content = Base64.getEncoder().encodeToString(snapshotXml);


        // payload 생성
        KuraPayload payload = new KuraPayload();
        payload.setBody(snapshotXml);

        // metrics 설정
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("request.id", UUID.randomUUID().toString());  // metric. 접두사 제거
        metrics.put("requester.client.id", "spring-mqtt-client");
        metrics.put("metric.request.id", UUID.randomUUID().toString());
        metrics.put("snapshot.content", base64Content);
        metrics.put("snapshot.name", "snapshotToUpload.xml");
        payload.setMetrics(metrics);

        byte[] data = ENCODER.encode(payload);
        mqttGateway.sendToMqtt(SNAPSHOT_UPLOAD_TOPIC, data);
    }


    private void 현재_상태_스냅샷_생성() {
        final String SNAPSHOT_RESOURCE_PATH = "snapshotToUpload.xml";
        final String SNAPSHOT_UPLOAD_TOPIC = "$EDC/kura/" + KURA_CLIENT_ID + "/CONF-V1/EXEC/snapshot";
        byte[] snapshotXml = loadSnapshotFile(SNAPSHOT_RESOURCE_PATH);

        // 스냅샷 내용을 base64로 인코딩
        String base64Content = Base64.getEncoder().encodeToString(snapshotXml);


        // payload 생성
        KuraPayload payload = new KuraPayload();
//        payload.setBody(snapshotXml);  // snapshot.xml 파일 내용
        payload.setBody(snapshotXml);

        // metrics 설정
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("request.id", UUID.randomUUID().toString());  // metric. 접두사 제거
        metrics.put("requester.client.id", "spring-mqtt-client");
        metrics.put("metric.request.id", UUID.randomUUID().toString());
        metrics.put("snapshot.content", base64Content);
        metrics.put("snapshot.name", "snapshotToUpload.xml");
        payload.setMetrics(metrics);

        byte[] data = ENCODER.encode(payload);
        mqttGateway.sendToMqtt(SNAPSHOT_UPLOAD_TOPIC, data);
    }

    private byte[] loadSnapshotFile(String file) {
        try {
            ClassPathResource resource = new ClassPathResource(file);
            return resource.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load snapshot file: " + file, e);
        }
    }

}