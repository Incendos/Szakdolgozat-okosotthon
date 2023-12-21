package hu.unideb.inf.sensor.temperature;

import javafx.beans.property.ReadOnlyDoubleProperty;

public interface TemperatureSensor {
    /**
     * @return the last measured temperature (in Celsius) wrapped in a Double Property.
     */
    ReadOnlyDoubleProperty getTemperatureProperty();

    /**
     * Initializes connection to the smart device. This should be called right after instancing a class.
     */
    void initConnection();
}
