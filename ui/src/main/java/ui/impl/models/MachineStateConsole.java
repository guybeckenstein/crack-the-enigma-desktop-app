package ui.impl.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MachineStateConsole {
    // Screens 1 + 2
    private final StringProperty firstMachineState;
    private final StringProperty currentMachineState;

    public MachineStateConsole() {
        this.firstMachineState = new SimpleStringProperty(this, "firstMachineState", "NaN");
        this.currentMachineState = new SimpleStringProperty(this, "currentMachineState", "NaN");
    }

    public String getFirstMachineState() {
        return firstMachineState.get();
    }

    public StringProperty firstMachineStateProperty() {
        return firstMachineState;
    }

    public void setFirstMachineState(String firstMachineState) {
        this.firstMachineState.set(firstMachineState);
    }

    public String getCurrentMachineState() {
        return currentMachineState.get();
    }

    public StringProperty currentMachineStateProperty() {
        return currentMachineState;
    }

    public void setCurrentMachineState(String currentMachineState) {
        this.currentMachineState.set(currentMachineState);
    }

}
