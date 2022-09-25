package ui.controllers;

import automateDecryption.Difficulty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import ui.impl.ModelMain;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

import java.util.List;
import java.util.Set;

public class AppController {
    // Main component
    static private ModelMain modelMainApp;
    @FXML private BorderPane mainBorderPane;
    // Sub components
    @FXML private StackPane stackPaneContainer;
    @FXML private ScrollPane screen1Component;
    @FXML private ScrollPane screen2Component;
    @FXML private ScrollPane screen3Component;
    @FXML private HeaderController headerComponentController;
    @FXML private MachineDetailsController screen1ComponentController;
    @FXML private EncryptDecryptController screen2ComponentController;
    @FXML private BruteForceController screen3ComponentController;

    public AppController() {
        modelMainApp = new ModelMain();
    }

    @FXML
    public void initialize() {
        if (headerComponentController != null && screen1ComponentController != null &&
                screen2ComponentController != null && screen3ComponentController != null) {
            headerComponentController.setMainController(this);
            screen1ComponentController.setMainController(this);
            screen2ComponentController.setMainController(this);
            screen3ComponentController.setMainController(this);
        }
    }

    public static ModelMain getConsoleApp() {
        return modelMainApp;
    }

    public void updateScreens(String currentMachineState) {
        screen1ComponentController.updateMachineStateAndStatus(currentMachineState);
        screen2ComponentController.updateMachineStateAndStatistics(currentMachineState);
        screen3ComponentController.updateMachineStateAndDictionary(currentMachineState);
    }

    public void reset() {
        screen1ComponentController.reset();
    }
    public void resetScreens(boolean bool, Object controller) {
        screen1ComponentController.resetMachineStateAndStatus();
        screen2ComponentController.resetMachineStateAndStatistics(bool);
        screen3ComponentController.resetMachineStateAndEnigmaOutput(bool, controller);
    }
    public void updateScreenOne(List<String> choiceBoxItems, String numberOfRotors, String numberOfReflectors) {
        screen1ComponentController.updateScreenOne(choiceBoxItems, numberOfRotors, numberOfReflectors);
    }
    public void initializeMachineStates(String machineStateConsoleString) {
        screen2ComponentController.initializeMachineStatesAndMouseInputKeyboard();
        screen3ComponentController.initializeMachineStates(machineStateConsoleString);
    }

    public void updateScreensDisability(boolean bool) {
        screen2ComponentController.setEnigmaDecryptionInputDisability(bool);
        screen3ComponentController.setBruteForceDisability(bool);
    }

    public void updateLabelTextsToEmpty(Object component) {
        headerComponentController.updateLabelTextsToEmpty();
        screen1ComponentController.updateLabelTextsToEmpty();
        screen2ComponentController.updateLabelTextsToEmpty(component);
        screen3ComponentController.updateLabelTextsToEmpty();
    }

    public void updateDynamicKeyboardsAndAmountAgents(int amountAgents) {
        screen2ComponentController.updateDynamicKeyboards();
        screen3ComponentController.updateAgentsMaxSizeAndDictionary(amountAgents);
    }

    // Swap screens
    public void changeToScreen1() {
        screen1Component.toFront();
    }
    public void changeToScreen2() {
        screen2Component.toFront();
    }
    public void changeToScreen3() {
        screen3Component.toFront();
    }


    public Set<String> getDictionary() {
        return modelMainApp.getWordsDictionary();
    }

    public void setDMProperties(int agents, int missionSize, Difficulty difficulty) {
        modelMainApp.setDMProperties(agents, missionSize, difficulty);
    }

    public void startResumeDM() {
        modelMainApp.startResumeDM();
    }

    public void setEncryptedText(String text) {
        modelMainApp.setEncryptedText(text);
    }



    public void updateStylesheet(Number num) {
        mainBorderPane.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainBorderPane.getStylesheets().add(getClass().getClassLoader().getResource("main/generalStyleOne.css").toString());
        } else if (num.equals(1)) {
            mainBorderPane.getStylesheets().add(getClass().getClassLoader().getResource("main/generalStyleTwo.css").toString());
        } else {
            mainBorderPane.getStylesheets().add(getClass().getClassLoader().getResource("main/generalStyleThree.css").toString());
        }
        headerComponentController.updateStylesheet(num);
        screen1ComponentController.updateStylesheet(num);
        screen2ComponentController.updateStylesheet(num);
        screen3ComponentController.updateStylesheet(num);
    }

    public void updateAnimation(Number num) {
        screen2ComponentController.updateAnimation(num);
    }
}