package hu.unideb.inf.sensor.motion;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface MotionSensor {
    ReadOnlyBooleanProperty getMotionProperty();

    void initConnection();
}
