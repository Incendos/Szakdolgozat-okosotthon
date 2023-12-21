package hu.unideb.inf.sensor.contact;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import lombok.Builder;
import lombok.Singular;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Builder
public class MqttContactSensor implements ContactSensor {

    private final ReadOnlyBooleanWrapper isContact = new ReadOnlyBooleanWrapper(false);

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
    @Singular("contactTopic")
    private List<String> contactTopic;

    /**
     * A mapper function that transforms the payload we get on the {@link #contactTopic} into a boolean.
     */
    private Function<String, Boolean> contactQuery;

    /**
     * Connects to the broker if not already connected and subscribes to the {@link #contactTopic}
     */
    @Override
    public void initConnection() {
        if(mqttClient.getConfig().getConnectionConfig().isEmpty()) {
            mqttClient.toBlocking().connect();
        }
        mqttClient.toAsync().subscribeWith()
                .topicFilter(String.join("/", contactTopic))
                .callback((publish) -> {
                    String respString = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    System.out.println(friendlyName + ": " + respString);
                    isContact.set(contactQuery.apply(respString));
                })
                .send();
    }

    @Override
    public ReadOnlyBooleanProperty getIsContactProperty() {
        return isContact.getReadOnlyProperty();
    }

    /**
     * For more information: <a href="https://www.zigbee2mqtt.io/devices/SNZB-04.html">https://www.zigbee2mqtt.io/devices/SNZB-04.html</a>
     * @return the builder configured for a Sonoff device.
     */
    public static class MqttContactSensorBuilder {
        public MqttContactSensorBuilder defaultSonoffConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .contactTopic("zigbee2mqtt").contactTopic(friendlyName).contactTopic("contact")
                    .contactQuery((response) -> response.equals("true"));
        }
    }
}
