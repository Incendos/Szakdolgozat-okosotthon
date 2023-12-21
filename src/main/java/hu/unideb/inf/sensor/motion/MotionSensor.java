package hu.unideb.inf.sensor.motion;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface MotionSensor {
    /**
     * @return a wrapped boolean value whether the sensor parts are in contact.
     */
    ReadOnlyBooleanProperty getMotionProperty();

    /**
     * Initializes connection to the smart device. This should be called right after instancing a class.
     */
    void initConnection();
}
