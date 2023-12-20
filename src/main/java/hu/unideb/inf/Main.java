package hu.unideb.inf;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import hu.unideb.inf.controllers.smartsocket.MqttSocketController;
import hu.unideb.inf.controllers.smartsocket.SmartSocketController;
import hu.unideb.inf.sensor.contact.ContactSensor;
import hu.unideb.inf.sensor.contact.MqttContactSensor;
import hu.unideb.inf.sensor.motion.MotionSensor;
import hu.unideb.inf.sensor.motion.MqttMotionSensor;
import hu.unideb.inf.sensor.temperature.MqttTemperatureSensor;
import hu.unideb.inf.sensor.temperature.TemperatureSensor;

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
                .mqttClient(mqtt5Client).build();
        TasmotaController.initConnection();

        ContactSensor SonoffContactSensor = MqttContactSensor.builder()
                .defaultSonoffConfig("DoorSensor001")
                .mqttClient(mqtt5Client)
                .build();
        SonoffContactSensor.initConnection();

        MotionSensor SonoffMotionSensor = MqttMotionSensor.builder()
                .defaultSonoffConfig("MotionSensor001")
                .mqttClient(mqtt5Client)
                .build();
        SonoffMotionSensor.initConnection();

        TemperatureSensor TasmotaTempSensor = MqttTemperatureSensor.builder()
                .defaultTasmotaConfig("tasmota_99562A")
                .mqttClient(mqtt5Client)
                .build();
        TasmotaTempSensor.initConnection();
    }
}
