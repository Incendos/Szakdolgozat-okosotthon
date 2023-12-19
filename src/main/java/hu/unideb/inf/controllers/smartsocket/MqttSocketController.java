package hu.unideb.inf.controllers.smartsocket;


import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import lombok.Builder;
import lombok.Singular;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Builder
public class MqttSocketController implements SmartSocketController {

    private Mqtt5Client mqttClient;

    private String friendlyName;

    @Singular("powerCommandTopic")
    private List<String> powerCommandTopic;

    @Singular("powerStateTopic")
    private List<String> powerStateTopic;

    @Singular("powerQueryTopic")
    private List<String> powerQueryTopic;

    private String powerToggleParameter;
    private String powerOnParameter;
    private String powerOffParameter;
    private String powerQueryParameter;

    private Function<String, Boolean> powerStateQuery;

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

    @Override
    public void setPower(boolean isOn) {
        mqttClient.toBlocking().publishWith()
                .topic(String.join("/", powerCommandTopic))
                .payload((isOn ? powerOnParameter : powerOffParameter).getBytes(StandardCharsets.UTF_8))
                .send();
    }

    public static class MqttSocketControllerBuilder {
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
