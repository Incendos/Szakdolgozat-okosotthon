package hu.unideb.inf.sensor.temperature;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import lombok.Builder;
import lombok.Singular;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Builder
public class MqttTemperatureSensor implements TemperatureSensor {
    private final ReadOnlyDoubleWrapper temperatureWrapper = new ReadOnlyDoubleWrapper(0.0);

    private Mqtt5Client mqttClient;

    private String friendlyName;

    @Singular("temperatureTopic")
    private List<String> temperatureTopic;

    private Function<String, Double> temperatureQuery;

    @Override
    public void initConnection() {
        if(mqttClient.getConfig().getConnectionConfig().isEmpty()) {
            mqttClient.toBlocking().connect();
        }
        mqttClient.toAsync().subscribeWith()
                .topicFilter(String.join("/", temperatureTopic))
                .callback((publish) -> {
                    String respString = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    temperatureWrapper.set(temperatureQuery.apply(respString));
                    System.out.println(friendlyName + ", temp: " + temperatureWrapper.get() + " C");
                })
                .send();
    }

    @Override
    public ReadOnlyDoubleProperty getTemperatureProperty() {
        return temperatureWrapper.getReadOnlyProperty();
    }

    public static class MqttTemperatureSensorBuilder {
        public MqttTemperatureSensorBuilder defaultTasmotaConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .temperatureTopic("tele").temperatureTopic(friendlyName).temperatureTopic("SENSOR")
                    .temperatureQuery((response) -> {
                        int tempIndex = response.indexOf("Temperature");
                        return Double.parseDouble(response.substring(tempIndex+13, tempIndex+17));
                    });
        }
    }
}
