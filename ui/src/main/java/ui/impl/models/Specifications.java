package ui.impl.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Specifications {
    // Screen 1 specs
    private final StringProperty rotorsAmountInMachineXML;
    private final StringProperty currentRotorsInMachine;
    private final StringProperty reflectorsAmountInMachineXML;
    private final StringProperty currentReflectorInMachine;
    private final StringProperty messagesProcessed;

    public Specifications() {
        this.rotorsAmountInMachineXML = new SimpleStringProperty(this, "rotorsAmountInMachineXML", "NaN");
        this.currentRotorsInMachine = new SimpleStringProperty(this, "currentRotorsInMachine", "NaN");
        this.reflectorsAmountInMachineXML = new SimpleStringProperty(this, "reflectorsAmountInMachineXML", "NaN");
        this.currentReflectorInMachine = new SimpleStringProperty(this, "currentReflectorInMachine", "NaN");
        this.messagesProcessed = new SimpleStringProperty(this, "messagesProcessed", "NaN");
    }

    public String getRotorsAmountInMachineXML() {
        return rotorsAmountInMachineXML.get();
    }

    public void setRotorsAmountInMachineXML(String rotorsAmountInMachineXML) {
        this.rotorsAmountInMachineXML.set(rotorsAmountInMachineXML);
    }

    public String getCurrentRotorsInMachine() {
        return currentRotorsInMachine.get();
    }

    public void setCurrentRotorsInMachine(String currentRotorsInMachine) {
        this.currentRotorsInMachine.set(currentRotorsInMachine);
    }

    public String getReflectorsAmountInMachineXML() {
        return reflectorsAmountInMachineXML.get();
    }

    public void setReflectorsAmountInMachineXML(String reflectorsAmountInMachineXML) {
        this.reflectorsAmountInMachineXML.set(reflectorsAmountInMachineXML);
    }

    public String getCurrentReflectorInMachine() {
        return currentReflectorInMachine.get();
    }

    public void setCurrentReflectorInMachine(String currentReflectorInMachine) {
        this.currentReflectorInMachine.set(currentReflectorInMachine);
    }

    public String getMessagesProcessed() {
        return messagesProcessed.get();
    }

    public void setMessagesProcessed(String messagesProcessed) {
        this.messagesProcessed.set(messagesProcessed);
    }

    public StringProperty rotorsAmountInMachineXMLProperty() {
        return rotorsAmountInMachineXML;
    }

    public StringProperty currentRotorsInMachineProperty() {
        return currentRotorsInMachine;
    }

    public StringProperty reflectorsAmountInMachineXMLProperty() {
        return reflectorsAmountInMachineXML;
    }

    public StringProperty currentReflectorInMachineProperty() {
        return currentReflectorInMachine;
    }

    public StringProperty messagesProcessedProperty() {
        return messagesProcessed;
    }
}
