package ui.controllers;

import ui.controllers.MachineStateController;
import ui.controllers.AppController;
import ui.impl.models.MachineStateConsole;
import ui.impl.models.Specifications;
import enigmaEngine.exceptions.InvalidCharactersException;
import enigmaEngine.exceptions.InvalidPlugBoardException;
import enigmaEngine.exceptions.InvalidReflectorException;
import enigmaEngine.exceptions.InvalidRotorException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.InputMismatchException;
import java.util.List;

public class MachineDetailsController {
    // Main component
    private AppController mainController;
    @FXML private MachineStateController firstMachineStateComponentController;
    @FXML private MachineStateController currentMachineStateComponentController;
    // Models
    private Specifications specs;
    private MachineStateConsole machineStatesConsole;
    // Machine configuration status
    @FXML private Label maxRotorsInMachineLabel;
    @FXML private Label currentUsedMachineRotorsLabel;
    @FXML private Label totalReflectorsInMachineLabel;
    @FXML private Label currentSelectedMachineReflectorLabel;
    @FXML private Label machineConfigurationMessageCounterLabel;
    // For disabling screen partitions
    @FXML private VBox configurationVBox;
    @FXML private Label setCodeLabel;
    // Screen buttons
    @FXML private Button setCodeButton;
    // User configuration input section
    @FXML private TextField rotorsAndOrderTextField;
    @FXML private TextField rotorsStartingPosTextField;
    @FXML private TextField plugBoardPairsTextField;
    @FXML private ChoiceBox<String> reflectorChoiceBox;
    @FXML ScrollPane mainScrollPane;

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public MachineDetailsController() {
        specs = new Specifications();
        machineStatesConsole = new MachineStateConsole();
    }

    @FXML
    private void initialize() {
        if (firstMachineStateComponentController != null && currentMachineStateComponentController != null) {

            // Only for binding the ENTER key to the input text field
            setCodeButton.setDefaultButton(true);
            setCodeButton.setOnAction(event -> getConfigurationFromUser());
            // Adding change property
            rotorsAndOrderTextField.textProperty().addListener(new ClearStatusListener());
            rotorsStartingPosTextField.textProperty().addListener(new ClearStatusListener());
            plugBoardPairsTextField.textProperty().addListener(new ClearStatusListener());
            // Model
            maxRotorsInMachineLabel.textProperty().bind(specs.rotorsAmountInMachineXMLProperty());
            currentUsedMachineRotorsLabel.textProperty().bind(specs.currentRotorsInMachineProperty());
            totalReflectorsInMachineLabel.textProperty().bind(specs.reflectorsAmountInMachineXMLProperty());
            currentSelectedMachineReflectorLabel.textProperty().bind(specs.currentReflectorInMachineProperty());
            machineConfigurationMessageCounterLabel.textProperty().bind(specs.messagesProcessedProperty());

            // Default values are added in certain screen places. This is called after constructor and after FXML variables are created.
            reset();
        }
    }

    private void setConfigurationDisability(boolean bool) {
        configurationVBox.setDisable(bool);
    }

    private void initializeMachineState() {
        machineStatesConsole.setFirstMachineState("NaN");
        machineStatesConsole.setCurrentMachineState("NaN");
    }

    @FXML
    void getConfigurationFromUser() {
        if (initializeEnigmaCode(true)) {
            String tmp = setCodeLabel.getText();
            updateConfigurationFieldsAndMachineStateDisability();
            setCodeLabel.setText(tmp);
            mainController.resetScreens(false, null);
        }
    }

    @FXML
    void setConfigurationRandomly() {
        if (initializeEnigmaCode(false)) {
            updateConfigurationFieldsAndMachineStateDisability();
            mainController.resetScreens(false, null);
        }
    }
    private boolean initializeEnigmaCode(boolean isManual) {
        String rotors, startingPositions, plugBoardPairs, reflectorID;
        if (isManual) {
            // Get input from user and generate it to the machine
            if (isValidConfigurationTextFields()) {
                rotors = rotorsAndOrderTextField.getText();
                startingPositions = rotorsStartingPosTextField.getText();
                plugBoardPairs = plugBoardPairsTextField.getText();
                reflectorID = reflectorChoiceBox.getValue();

                startingPositions = new StringBuilder(startingPositions).reverse().toString();

                try {
                    AppController.getConsoleApp().initializeEnigmaCodeManually(rotors, startingPositions, plugBoardPairs, reflectorID);
                    setCodeLabel.setText("Manually initialized configuration code.");
                } catch (NumberFormatException e) {
                    setCodeLabel.setText("Non-numeric value was inserted in 'Rotors And Order'.");
                    return false;
                } catch (InvalidRotorException | InvalidReflectorException | InvalidPlugBoardException |
                         InvalidCharactersException | NullPointerException | InputMismatchException | IllegalArgumentException e) {
                    setCodeLabel.setText(e.getLocalizedMessage());
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            AppController.getConsoleApp().initializeEnigmaCodeAutomatically();
            setCodeLabel.setText("Automatically initialized configuration code.");
        }
        String machineStateConsoleString = AppController.getConsoleApp().getMachineHistoryStates().getCurrentMachineCode();
        updateMachineStatesAndDisability(machineStateConsoleString, false);

        specs.setCurrentRotorsInMachine(Integer.toString(AppController.getConsoleApp().getEngine().getEngineDTO().getSelectedRotors().size()));
        specs.setCurrentReflectorInMachine(AppController.getConsoleApp().getEngine().getEngineDTO().getSelectedReflector());
        specs.setMessagesProcessed(Integer.toString(AppController.getConsoleApp().getEngine().getEngineDTO().getMessagesSentCounter()));
        return true;
    }

    public void updateMachineStatesAndDisability(String machineStateConsoleString, boolean bool) {
        mainController.updateScreensDisability(bool);
        mainController.initializeMachineStates(machineStateConsoleString);
        firstMachineStateComponentController.setInitializedControllerComponents(AppController.getConsoleApp().getEngine().getEngineDTO());
        currentMachineStateComponentController.setInitializedControllerComponents(AppController.getConsoleApp().getEngine().getEngineDTO());
    }

    private boolean isValidConfigurationTextFields() {
        if (rotorsAndOrderTextField.getText().equals("")) {
            setCodeLabel.setText("You did not add your rotors' IDs and their order.");
            return false;
        }
        else if (rotorsStartingPosTextField.getText().equals("")) {
            setCodeLabel.setText("You did not add your rotors' starting positions.");
            return false;
        }
        else if (plugBoardPairsTextField.getText().trim().length() % 2 == 1) {
            setCodeLabel.setText("Enter an even number of plug board pairs values.");
            return false;
        }
        return true;
    }

    private void updateConfigurationFieldsAndMachineStateDisability() {
        rotorsAndOrderTextField.setText("");
        rotorsStartingPosTextField.setText("");
        plugBoardPairsTextField.setText("");
        reflectorChoiceBox.setValue(reflectorChoiceBox.getItems().get(0));
    }

    public void updateScreenOne(List<String> choiceBoxItems, String numberOfRotors, String numberOfReflectors) {
        reflectorChoiceBox.setItems(FXCollections.observableArrayList(choiceBoxItems));
        reflectorChoiceBox.setValue(reflectorChoiceBox.getItems().get(0));

        specs.setRotorsAmountInMachineXML(numberOfRotors);
        specs.setReflectorsAmountInMachineXML(numberOfReflectors);

        setConfigurationDisability(false);
    }
    public void reset() {
        setConfigurationDisability(true);
        initializeMachineState();

        specs.setRotorsAmountInMachineXML("NaN");
        specs.setCurrentRotorsInMachine("NaN");
        specs.setReflectorsAmountInMachineXML("NaN");
        specs.setCurrentReflectorInMachine("NaN");
        specs.setMessagesProcessed("NaN");
    }

    public void updateMachineStateAndStatus(String currentMachineState) {
        specs.setMessagesProcessed(Integer.toString(AppController.getConsoleApp().getMessageCounter()));
        machineStatesConsole.setCurrentMachineState(currentMachineState);
        currentMachineStateComponentController.setInitializedControllerComponents(AppController.getConsoleApp().getEngine().getEngineDTO());
    }

    public void resetMachineStateAndStatus() {
        machineStatesConsole.setCurrentMachineState(machineStatesConsole.getFirstMachineState());
        currentMachineStateComponentController.resetMachineStateComponentComponent(AppController.getConsoleApp().getEngine().getEngineDTO());
    }

    class ClearStatusListener implements ChangeListener<String> {
        @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            mainController.updateLabelTextsToEmpty(this);
        }
    }
    public void updateLabelTextsToEmpty() {
        setCodeLabel.setText("");
    }



    public void updateStylesheet(Number num) {
        firstMachineStateComponentController.updateStylesheet(num);
        currentMachineStateComponentController.updateStylesheet(num);
        mainScrollPane.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainScrollPane.getStylesheets().add(getClass().getClassLoader().getResource("machineDetails/machineDetailsStyleOne.css").toString());
        } else if (num.equals(1)) {
            mainScrollPane.getStylesheets().add(getClass().getClassLoader().getResource("machineDetails/machineDetailsStyleTwo.css").toString());
        } else {
            mainScrollPane.getStylesheets().add(getClass().getClassLoader().getResource("machineDetails/machineDetailsStyleThree.css").toString());
        }
    }
}