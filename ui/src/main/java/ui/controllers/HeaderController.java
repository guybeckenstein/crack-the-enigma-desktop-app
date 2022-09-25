package ui.controllers;

import enigmaEngine.exceptions.*;
import ui.controllers.AppController;
import enigmaEngine.interfaces.Reflector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderController {
    private AppController mainController;
    @FXML private HBox headerHBox;
    private String currXMLFilePath = "";

    // private final IntegerProperty chosenButton = new SimpleIntegerProperty();
    @FXML private TextField xmlFilePathTextField;

    @FXML private Label loadXMLErrorLabel;

    @FXML private ChoiceBox<String> styleChoiceBox;

    @FXML private ChoiceBox<String> animationChoiceBox;

    @FXML private Button machineDetailsButton;

    @FXML private Button decryptInputWithEnigmaButton;

    @FXML private Button bruteForceButton;


    public HeaderController() {

    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        machineDetailsButton.getStyleClass().add("chosen-button");

        styleChoiceBox.getItems().addAll("Style #1", "Style #2", "Style #3");
        styleChoiceBox.setValue("Style #1");
        animationChoiceBox.getItems().addAll("No Animation", "Animation");
        animationChoiceBox.setValue("No Animation");

        styleChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            mainController.updateStylesheet(number2);
        });

        animationChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            mainController.updateAnimation(number2);
        });
    }

    @FXML
    void machineDetailsButtonActionListener() {
        machineDetailsButton.getStyleClass().add("chosen-button");
        decryptInputWithEnigmaButton.getStyleClass().remove("chosen-button");
        bruteForceButton.getStyleClass().remove("chosen-button");
        mainController.changeToScreen1();
    }

    @FXML
    void decryptInputWithEnigmaButtonActionListener() {
        machineDetailsButton.getStyleClass().remove("chosen-button");
        decryptInputWithEnigmaButton.getStyleClass().add("chosen-button");
        bruteForceButton.getStyleClass().remove("chosen-button");
        mainController.changeToScreen2();
    }

    @FXML
    void bruteForceButtonActionListener() {
        machineDetailsButton.getStyleClass().remove("chosen-button");
        decryptInputWithEnigmaButton.getStyleClass().remove("chosen-button");
        bruteForceButton.getStyleClass().add("chosen-button");
        mainController.changeToScreen3();
    }

    @FXML
    void loadXML() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pick your XML file for Ex2.");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

            File newXMLFile = fc.showOpenDialog(null);
            String filePath = newXMLFile.getAbsolutePath();
            if (filePath.equals(currXMLFilePath)) {
                throw new FileAlreadyExistsException("File given is already defined as the Enigma machine.");
            } else if (!filePath.equals("")) {

                AppController.getConsoleApp().readMachineFromXMLFile(filePath);
                xmlFilePathTextField.setText(filePath);
                currXMLFilePath = filePath;

                // Update reflector choice box options
                List<Reflector.ReflectorID> unsortedReflectors = AppController.getConsoleApp().getEngine().getReflectors();
                Collections.sort(unsortedReflectors);
                mainController.updateScreenOne(
                        unsortedReflectors.stream().map(String::valueOf).collect(Collectors.toList()),
                        Integer.toString(AppController.getConsoleApp().getEngine().getEngineDTO().getTotalNumberOfRotors()),
                        Integer.toString(AppController.getConsoleApp().getEngine().getEngineDTO().getTotalReflectors())
                );
                mainController.initializeMachineStates("NaN");
                mainController.updateScreensDisability(true);
                mainController.updateDynamicKeyboardsAndAmountAgents(AppController.getConsoleApp().getXmlDTO().getTotalAgents());

                loadXMLErrorLabel.setText("Machine XML file successfully loaded.");

            }
        } catch (NullPointerException e) { // If a user exits XML file search
            // Continue...
        } catch (FileAlreadyExistsException e) {
            loadXMLErrorLabel.setText(e.getLocalizedMessage());
        } catch (InvalidMachineException | JAXBException | InvalidRotorException | IOException
                 | InvalidABCException | UnknownSourceException | InvalidReflectorException
                 | InvalidDecipherException | InvalidAgentsAmountException e) {
            loadXMLErrorLabel.setText(e.getLocalizedMessage());
            mainController.reset();
            mainController.initializeMachineStates("NaN");
            mainController.updateScreensDisability(true);
            currXMLFilePath = "";
        }
    }
    public void updateLabelTextsToEmpty() {
        loadXMLErrorLabel.setText("");
    }



    public void updateStylesheet(Number num) {
        headerHBox.getStylesheets().remove(0);
        if (num.equals(0)) {
            headerHBox.getStylesheets().add(getClass().getClassLoader().getResource("header/headerStyleOne.css").toString());
        } else if (num.equals(1)) {
            headerHBox.getStylesheets().add(getClass().getClassLoader().getResource("header/headerStyleTwo.css").toString());
        } else {
            headerHBox.getStylesheets().add(getClass().getClassLoader().getResource("header/headerStyleThree.css").toString());
        }
    }
}