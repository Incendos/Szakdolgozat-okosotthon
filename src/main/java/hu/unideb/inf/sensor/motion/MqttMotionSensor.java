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

    private Mqtt5Client mqttClient;

    private String friendlyName;

    @Singular("motionTopic")
    private List<String> motionTopic;

    private Function<String, Boolean> motionQuery;

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
        public MqttMotionSensorBuilder defaultSonoffConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .motionTopic("zigbee2mqtt").motionTopic(friendlyName).motionTopic("occupancy")
                    .motionQuery((response) -> response.equals("true"));
        }
    }
}
