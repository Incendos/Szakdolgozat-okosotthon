package hu.unideb.inf;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import hu.unideb.inf.controllers.smartsocket.MqttSocketController;
import hu.unideb.inf.controllers.smartsocket.SmartSocketController;

public class Main {
    public static void main(String[] args) {
        Mqtt5Client mqtt5Client = Mqtt5Client.builder()
                .identifier("TestClient")
                .serverHost("192.168.1.120")
                .serverPort(1883)
                .automaticReconnectWithDefaultConfig()
                .build();

        SmartSocketController TuyaController = MqttSocketController.builder()
                .defaultTuyaConfig("AubessSmartSocket001")
                .mqttClient(mqtt5Client)
                .build();
        TuyaController.initConnection();

        SmartSocketController TasmotaController = MqttSocketController.builder()
                .defaultTasmotaConfig("tasmota_068573")
                .mqttClient(mqtt5Client)
                .build();
        TasmotaController.initConnection();

        TuyaController.toggle();
        TasmotaController.toggle();
    }
}
