package hu.unideb.inf.sensor.motion;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import lombok.Builder;
import lombok.Singular;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Builder
public class MqttMotionSensor implements MotionSensor {

    private final ReadOnlyBooleanWrapper Motion = new ReadOnlyBooleanWrapper(false);

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
    @Singular("motionTopic")
    private List<String> motionTopic;

    /**
     * A mapper function that transforms the payload we get on the {@link #motionTopic} into a boolean.
     */
    private Function<String, Boolean> motionQuery;

    /**
     * Connects to the broker if not already connected and subscribes to the {@link #motionTopic}
     */
    @Override
    public void initConnection() {
        if(mqttClient.getConfig().getConnectionConfig().isEmpty()) {
            mqttClient.toBlocking().connect();
        }
        mqttClient.toAsync().subscribeWith()
                .topicFilter(String.join("/", motionTopic))
                .callback((publish) -> {
                    String respString = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    System.out.println(friendlyName + ": " + respString);
                    Motion.set(motionQuery.apply(respString));
                })
                .send();
    }

    @Override
    public ReadOnlyBooleanProperty getMotionProperty() {
        return Motion.getReadOnlyProperty();
    }

    public static class MqttMotionSensorBuilder {
        /**
         * For more information: <a href="https://www.zigbee2mqtt.io/devices/SNZB-03.html">https://www.zigbee2mqtt.io/devices/SNZB-03.html</a>
         * @return the builder configured for a Sonoff device.
         */
        public MqttMotionSensorBuilder defaultSonoffConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .motionTopic("zigbee2mqtt").motionTopic(friendlyName).motionTopic("occupancy")
                    .motionQuery((response) -> response.equals("true"));
        }
    }
}
