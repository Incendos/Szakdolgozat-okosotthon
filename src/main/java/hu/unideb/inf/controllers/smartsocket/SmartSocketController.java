package hu.unideb.inf.controllers.smartsocket;

import javafx.beans.property.ReadOnlyBooleanWrapper;

public interface SmartSocketController {
    ReadOnlyBooleanWrapper isPowerOn = new ReadOnlyBooleanWrapper(false);
    void initConnection();
    void toggle();
    void setPower(boolean isOn);
}
