package hu.unideb.inf.sensor.contact;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface ContactSensor {
    /**
     * @return a wrapped boolean value whether the sensor parts are in contact.
     */
    ReadOnlyBooleanProperty getIsContactProperty();

    /**
     * Initializes connection to the smart device. This should be called right after instancing a class.
     */
    void initConnection();
}
