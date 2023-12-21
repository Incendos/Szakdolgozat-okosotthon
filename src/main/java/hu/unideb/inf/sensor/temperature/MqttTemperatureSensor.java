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

    /**
     * The mqtt client that is used to publish messages and subscribe to topics.
     */
    private Mqtt5Client mqttClient;

    /**
     * The friendly name of the device we wish to control.
     */
    private String friendlyName;

    /**
     * The topic that the controlled device publishes its state to.
     * Each list element corresponds to the next topic level in order. <br>
     * ["zigbee2mqtt", "friendlyName", "Power"] would indicate the following topic: "zigbee2mqtt/friendlyName/Power"
     */
    @Singular("temperatureTopic")
    private List<String> temperatureTopic;

    /**
     * A mapper function that transforms the payload we get on the {@link #temperatureTopic} into a Double value.
     */
    private Function<String, Double> temperatureQuery;

    /**
     * Connects to the broker if not already connected and subscribes to the {@link #temperatureTopic}.
     */
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
        /**
         * For more information: <a href="https://tasmota.github.io/docs/MQTT/">https://tasmota.github.io/docs/MQTT/</a>
         * @return the builder configured for a Tasmota device.
         */
        public MqttTemperatureSensorBuilder defaultTasmotaConfig(String friendlyName) {
            /**
             * Tasmota devices usually publish every telemetry data under 1 topic in json format,
             * While other devices can be configured to publish each data on a different topics, this is not true for Tasmota.
             * @TODO switch to a json converter and/or create new class for Tasmota devices?
             */
            return this.friendlyName(friendlyName)
                    .temperatureTopic("tele").temperatureTopic(friendlyName).temperatureTopic("SENSOR")
                    .temperatureQuery((response) -> {
                        int tempIndex = response.indexOf("Temperature");
                        return Double.parseDouble(response.substring(tempIndex+13, tempIndex+17));
                    });
        }
    }
}
