package hu.unideb.inf.controllers.smartsocket;


import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import lombok.Builder;
import lombok.Singular;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Builder
public class MqttSocketController implements SmartSocketController {

    /**
     * The mqtt client that is used to publish messages and subscribe to topics.
     */
    private Mqtt5Client mqttClient;

    /**
     * The friendly name of the device we wish to control.
     */
    private String friendlyName;

    /**
     * The power state of the socket.
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
     * The topic we can publish to in order to make the device publish its power state.
     * Each list element corresponds to the next topic level in order. <br>
     * ["zigbee2mqtt", "friendlyName", "Power"] would indicate the following topic: "zigbee2mqtt/friendlyName/Power"
     */
    @Singular("powerQueryTopic")
    private List<String> powerQueryTopic;

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
     * The payload we send on the {@link #powerQueryTopic}.
     */
    private String powerQueryParameter;

    /**
     * A mapper function that transforms the payload we get on the {@link #powerStateTopic} into a boolean.
     */
    private Function<String, Boolean> powerStateQuery;

    /**
     * Connects to the broker if not already connected and subscribes to the {@link #powerStateTopic}
     */
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

    /**
     * publishes the {@link #powerToggleParameter} to the {@link #powerCommandTopic} in order to
     * toggle the power on the device.
     */
    @Override
    public void toggle() {
        mqttClient.toBlocking().publishWith()
                .topic(String.join("/", powerCommandTopic))
                .payload(powerToggleParameter.getBytes(StandardCharsets.UTF_8))
                .send();
    }

    /**
     * publishes to the {@link #powerCommandTopic} in order to
     * turn on/off the power on the device.
     * @param isOn the desired state we want to set. if true sends the {@link #powerOnParameter} as payload, otherwise
     *             sends {@link #powerOffParameter}
     */
    @Override
    public void setPower(boolean isOn) {
        mqttClient.toBlocking().publishWith()
                .topic(String.join("/", powerCommandTopic))
                .payload((isOn ? powerOnParameter : powerOffParameter).getBytes(StandardCharsets.UTF_8))
                .send();
    }

    @Override
    public ReadOnlyBooleanProperty getIsPowerOnProperty() {
        return isPowerOn.getReadOnlyProperty();
    }

    public static class MqttSocketControllerBuilder {
        /**
         * For more information: <a href="https://tasmota.github.io/docs/MQTT/">https://tasmota.github.io/docs/MQTT/</a>
         * @return the builder configured for a Tasmota device.
         */
        public MqttSocketControllerBuilder defaultTasmotaConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .powerToggleParameter("Toggle")
                    .powerOnParameter("On")
                    .powerOffParameter("Off")
                    .powerQueryParameter("")
                    .powerCommandTopic("cmnd").powerCommandTopic(friendlyName).powerCommandTopic("Power")
                    .powerStateTopic("stat").powerStateTopic(friendlyName).powerStateTopic("POWER")
                    .powerQueryTopic("stat").powerQueryTopic(friendlyName).powerQueryTopic("Power")
                    .powerStateQuery((String resp) -> {
                        return resp.equals("ON");
                    });
        }

        /**
         * For more information: <a href="https://www.zigbee2mqtt.io/devices/TS011F_plug_1.html">https://www.zigbee2mqtt.io/devices/TS011F_plug_1.html</a>
         * @return the builder configured for a Tuya device.
         */
        public MqttSocketControllerBuilder defaultTuyaConfig(String friendlyName) {
            return this.friendlyName(friendlyName)
                    .powerToggleParameter("{\"state\": \"TOGGLE\"}")
                    .powerOnParameter("{\"state\": \"ON\"}")
                    .powerOffParameter("{\"state\": \"OFF\"}")
                    .powerQueryParameter("{\"state\": \"\"}")
                    .powerCommandTopic("zigbee2mqtt").powerCommandTopic(friendlyName).powerCommandTopic("set")
                    .powerStateTopic("zigbee2mqtt").powerStateTopic(friendlyName).powerStateTopic("state")
                    .powerQueryTopic("zigbee2mqtt").powerQueryTopic(friendlyName).powerQueryTopic("get")
                    .powerStateQuery((String resp) -> {
                        return resp.equals("ON");
                    });
        }
    }
}
