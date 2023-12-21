package hu.unideb.inf.sensor.contact;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface ContactSensor {
    ReadOnlyBooleanProperty getIsContactProperty();

    void initConnection();
}
