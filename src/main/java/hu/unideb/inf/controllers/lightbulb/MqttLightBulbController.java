package hu.unideb.inf.controllers.lightbulb;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import lombok.Builder;
import lombok.Singular;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Builder
public class MqttLightBulbController implements LightBulbController {

    /**
     * The mqtt client that is used to publish messages and subscribe to topics.
     */
    private Mqtt5Client mqttClient;

    /**
     * The friendly name of the device we wish to control.
     */
    private String friendlyName;

    /**
     * The power state of the LightBulb.
     */
    private final ReadOnlyBooleanWrapper isPowerOn = new ReadOnlyBooleanWrapper(false);

    /**
     * The topic the controlled device subscribes to in order to listen for power commands.
     * Each list element corresponds to the next topic level in order. <br>
     * ["zigbee2mqtt", "friendlyName", "Power"] would indicate the following topic: "zigbee2mqtt/friendlyName/Power"
     */
    @Singular("powerCommandTopic")
    private List<String> powerCommandTopic;

    /**
     * The topic that the controlled device publishes its state to.
     * Each list element corresponds to the next topic level in order. <br>
     * ["zigbee2mqtt", "friendlyName", "Power"] would indicate the following topic: "zigbee2mqtt/friendlyName/Power"
     */
    @Singular("powerStateTopic")
    private List<String> powerStateTopic;

    /**
     * The payload we send on the {@link #powerCommandTopic} to toggle power.
     */
    private String powerToggleParameter;
    /**
     * The payload we send on the {@link #powerCommandTopic} to turn on power.
     */
    private String powerOnParameter;
    /**
     * The payload we send on the {@link #powerCommandTopic} to turn off power.
     */
    private String powerOffParameter;

    /**
     * A mapper function that transforms the payload we get on the {@link #powerStateTopic} into a boolean.
     */
    private Function<String, Boolean> powerStateQuery;

    @Override
    public ReadOnlyBooleanProperty getPowerProperty() {
        return isPowerOn.getReadOnlyProperty();
    }

    @Override
    public void setPower(boolean isOn) {
        mqttClient.toBlocking().publishWith()
                .topic(String.join("/", powerCommandTopic))
                .payload((isOn ? powerOnParameter : powerOffParameter).getBytes(StandardCharsets.UTF_8))
                .send();
    }

    @Override
    public void initConnection() {
        if(mqttClient.getConfig().getConnectionConfig().isEmpty()) {
            mqttClient.toBlocking().connect();
        }
        mqttClient.toAsync().subscribeWith()
                .topicFilter(String.join("/", powerStateTopic))
                .callback((publish) -> {
                    String respString = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    System.out.println(friendlyName + ": " + respString);
                    isPowerOn.set(powerStateQuery.apply(respString));
                })
                .send();
    }

    @Override
    public void toggle() {
        mqttClient.toBlocking().publishWith()
                .topic(String.join("/", powerCommandTopic))
                .payload(powerToggleParameter.getBytes(StandardCharsets.UTF_8))
                .send();
    }

    public static class MqttLightBulbControllerBuilder {
        /**
         * For more information: <a href="https://tasmota.github.io/docs/MQTT/">https://tasmota.github.io/docs/MQTT/</a>
         * @return the builder configured for a Tasmota device.
         */
        public MqttLightBulbControllerBuilder defaultTasmotaConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .powerToggleParameter("Toggle")
                    .powerOnParameter("On")
                    .powerOffParameter("Off")
                    .powerCommandTopic("cmnd").powerCommandTopic(friendlyName).powerCommandTopic("Power")
                    .powerStateTopic("stat").powerStateTopic(friendlyName).powerStateTopic("POWER")
                    .powerStateQuery((resp) -> resp.equals("ON"));
        }
    }
}
