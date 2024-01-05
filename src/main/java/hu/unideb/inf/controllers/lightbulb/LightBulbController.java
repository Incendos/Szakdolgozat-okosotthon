package hu.unideb.inf.controllers.lightbulb;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface LightBulbController {
    /**
     * Initializes connection to the smart device. This should be called right after instancing a class.
     */
    void initConnection();
    /**
     * If the state of the device changes it will be reflected through the returned value.
     * @return a wrapped boolean that indicates if the bulb is on or off.
     */
    ReadOnlyBooleanProperty getPowerProperty();

    /**
     * Sets the state of the bulb.
     * @param isOn the desired power state.
     */
    void setPower(boolean isOn);

    /**
     * Toggles the power of the bulb (inverts it).
     */
    void toggle();
}
