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

    private Mqtt5Client mqttClient;

    private String friendlyName;

    @Singular("contactTopic")
    private List<String> contactTopic;

    private Function<String, Boolean> contactQuery;

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

    public static class MqttContactSensorBuilder {
        public MqttContactSensorBuilder defaultSonoffConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .contactTopic("zigbee2mqtt").contactTopic(friendlyName).contactTopic("contact")
                    .contactQuery((response) -> response.equals("true"));
        }
    }
}
