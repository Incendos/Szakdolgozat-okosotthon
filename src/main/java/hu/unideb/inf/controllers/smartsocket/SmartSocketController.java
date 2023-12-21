package hu.unideb.inf.controllers.smartsocket;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface SmartSocketController {
    /**
     * If the state of the device changes it will be reflected through the returned value.
     * @return a wrapped boolean that indicates if the socket is on or off.
     */
    ReadOnlyBooleanProperty getIsPowerOnProperty();

    /**
     * Initializes connection to the smart device. This should be called right after instancing a class.
     */
    void initConnection();

    /**
     * Inverts the state of the device. (If it's on, then turns it off)
     */
    void toggle();

    /**
     * Sets the state of the device.
     * @param isOn the desired state.
     */
    void setPower(boolean isOn);
}
