package hu.unideb.inf.sensor.temperature;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;

public interface TemperatureSensor {
    ReadOnlyDoubleProperty getTemperatureProperty();

    void initConnection();
}
