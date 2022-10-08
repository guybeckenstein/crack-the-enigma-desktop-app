package ui.controllers;

import javafx.scene.layout.BorderPane;
import ui.impl.models.ModelMain;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AppController {
    // Main component
    static private ModelMain modelMainApp;
    @FXML private BorderPane mainBorderPane;
    // Sub components
    @FXML private ScrollPane machineDetailsComponent;
    @FXML private ScrollPane encryptDecryptComponent;
    @FXML private ScrollPane bruteForceComponent;
    @FXML private HeaderController headerComponentController;
    @FXML private MachineDetailsController machineDetailsComponentController;
    @FXML private EncryptDecryptController encryptDecryptComponentController;
    @FXML private BruteForceController bruteForceComponentController;

    public AppController() {
        modelMainApp = new ModelMain();
    }

    @FXML
    public void initialize() {
        if (headerComponentController != null && machineDetailsComponentController != null &&
                encryptDecryptComponentController != null && bruteForceComponentController != null) {
            headerComponentController.setMainController(this);
            machineDetailsComponentController.setMainController(this);
            encryptDecryptComponentController.setMainController(this);
            bruteForceComponentController.setMainController(this);
        }
    }

    public static ModelMain getModelMain() {
        return modelMainApp;
    }

    public void updateScreens(String currentMachineState) {
        machineDetailsComponentController.updateMachineStateAndStatus(currentMachineState);
        encryptDecryptComponentController.updateMachineStateAndStatistics(currentMachineState);
        bruteForceComponentController.updateMachineStateAndDictionary(currentMachineState);
    }

    public void reset() {
        machineDetailsComponentController.reset();
    }
    public void resetScreens(boolean bool, Object controller) {
        machineDetailsComponentController.resetMachineStateAndStatus();
        encryptDecryptComponentController.resetMachineStateAndStatistics(bool);
        bruteForceComponentController.resetMachineStateAndEnigmaOutput(bool, controller);
    }
    public void updateMachineDetailsScreen(List<String> choiceBoxItems, String numberOfRotors, String numberOfReflectors) {
        machineDetailsComponentController.updateScreen(choiceBoxItems, numberOfRotors, numberOfReflectors);
    }
    public void initializeMachineStates(String machineStateConsoleString) {
        encryptDecryptComponentController.initializeMachineStatesAndMouseInputKeyboard();
        bruteForceComponentController.initializeMachineStates(machineStateConsoleString);
    }

    public void updateScreensDisability(boolean bool) {
        encryptDecryptComponentController.setEnigmaDecryptionInputDisability(bool);
        bruteForceComponentController.setBruteForceDisability(bool);
    }

    public void updateLabelTextsToEmpty(Object component) {
        headerComponentController.updateLabelTextsToEmpty();
        machineDetailsComponentController.updateLabelTextsToEmpty();
        encryptDecryptComponentController.updateLabelTextsToEmpty(component);
        bruteForceComponentController.updateLabelTextsToEmpty();
    }

    public void updateDynamicKeyboardsAndAmountAgents(int amountAgents) {
        encryptDecryptComponentController.updateDynamicKeyboards();
        bruteForceComponentController.updateAgentsMaxSizeAndDictionary(amountAgents);
    }

    // Swap screens
    public void changeToMachineDetailsScreen() {
        machineDetailsComponent.toFront();
    }
    public void changeToEncryptDecryptScreen() {
        encryptDecryptComponent.toFront();
    }
    public void changeToBruteForceScreen() {
        bruteForceComponent.toFront();
    }


    public Set<String> getDictionary() {
        return modelMainApp.getWordsDictionary();
    }

    public void updateStylesheet(Number num) {
        mainBorderPane.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainBorderPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("main/generalStyleOne.css")).toString());
        } else if (num.equals(1)) {
            mainBorderPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("main/generalStyleTwo.css")).toString());
        } else {
            mainBorderPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("main/generalStyleThree.css")).toString());
        }
        headerComponentController.updateStylesheet(num);
        machineDetailsComponentController.updateStylesheet(num);
        encryptDecryptComponentController.updateStylesheet(num);
        bruteForceComponentController.updateStylesheet(num);
    }

    public void updateAnimation(Number num) {
        encryptDecryptComponentController.updateAnimation(num);
    }

    public void updateInputLists() {
        machineDetailsComponentController.updateInputLists();
    }
}