package ui.controllers;

import ui.impl.models.MachineStateModel;
import enigmaEngine.exceptions.InvalidCharactersException;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;

// Second screen
public class EncryptDecryptController {
    // Main component
    private AppController mainController;
    @FXML private MachineStateController firstMachineStateComponentController;
    @FXML private MachineStateController currentMachineStateComponentController;
    // Models
    private final MachineStateModel machineStateModel;
    //
    @FXML private BorderPane borderPaneContainer;
    // Machine states
    @FXML private Label currentMachineInitialStateLabel;
    @FXML private Label setCodeLabel;
    @FXML private TextField inputToEncryptDecryptInput;
    @FXML private Button encryptionDecryptionInputButton;
    @FXML private Button resetMachineStateButton;

    @FXML private Button enterCurrentKeyboardInputButton;
    @FXML private GridPane keyboardInputGridPane; // Only for binding the ENTER key to the input text field
    // Mouse bonus
    @FXML private TextField mouseInputTextField; // Automatic input after each click
    @FXML private TextField mouseOutputTextField; // Automatic output after each click
    @FXML private FlowPane mouseInputFlowPane; // Dynamic component of XML Enigma buttons (all ABC letters), for input (clickable button-nodes)
    @FXML private FlowPane mouseOutputFlowPane; // Dynamic component of XML Enigma buttons (all ABC letters), for output (non-clickable button-nodes)
    Button lastOutputButton = null;

    @FXML private ScrollPane mainScrollPane;


    boolean isAnimation = false;
    private final RotateTransition rotateTransition;
    private final ScaleTransition scaleTransition;
    private final FadeTransition fadeTransition;
    private final ParallelTransition parallelTransition;
    private static int numOfClicks;

    public EncryptDecryptController() {
        machineStateModel = new MachineStateModel(); // Model

        rotateTransition = new RotateTransition(Duration.millis(500));
        scaleTransition = new ScaleTransition(Duration.millis(500));
        fadeTransition = new FadeTransition(Duration.millis(500), resetMachineStateButton);
        parallelTransition = new ParallelTransition(rotateTransition, scaleTransition);
        numOfClicks = 0;
    }

    @FXML
    private void initialize() {
        if (firstMachineStateComponentController != null && currentMachineStateComponentController != null) {
            // Only for binding the ENTER key to the input text field
            enterCurrentKeyboardInputButton.setOnAction(this::enterCurrentKeyboardInputButtonActionListener);
            keyboardInputGridPane.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
                if (ev.getCode() == KeyCode.ENTER) {
                    enterCurrentKeyboardInputButton.fire();
                    ev.consume();
                }
            });
            // Adding change property
            inputToEncryptDecryptInput.textProperty().addListener(new ClearStatusListener());
            // Initialization
            setEnigmaDecryptionInputDisability(true);
            encryptionDecryptionInputButton.getStyleClass().add("chosen-button");


            rotateTransition.setCycleCount(1);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.setByAngle(360);
            rotateTransition.setAxis(Rotate.X_AXIS);
            scaleTransition.setByX(1.5f);
            scaleTransition.setByY(1.5f);
            scaleTransition.setCycleCount(4);
            scaleTransition.setAutoReverse(true);
        }
    }

    public void setEnigmaDecryptionInputDisability(boolean bool) {
        borderPaneContainer.setDisable(bool);
    }

    @FXML
    void enterCurrentKeyboardInputButtonActionListener(ActionEvent event) {
        try {
            String messageInput = inputToEncryptDecryptInput.getText().toUpperCase();
            if (messageInput.equals("")) {
                throw new InputMismatchException("No encryption message was written.");
            }
            produceProcessedMessage(messageInput);
        } catch (InvalidCharactersException | InputMismatchException e) {
            setCodeLabel.setText(e.getLocalizedMessage());
        }
    }

    private void produceProcessedMessage(String messageInput) throws InvalidCharactersException {
        setCodeLabel.setText("Processed message: " + messageInput + " -> " + AppController.getModelMain().getMessageAndProcessIt(messageInput, true));
        mainController.updateScreens(AppController.getModelMain().getCurrentMachineStateAsString());
        mainController.updateLabelTextsToEmpty(this);

        if (isAnimation) {
            fadeTransition.setNode(resetMachineStateButton);
            if (numOfClicks % 3 == 0) {
                fadeTransition.setFromValue(1);
                fadeTransition.setFromValue(0.5);
            }
            if (numOfClicks % 3 == 1) {
                fadeTransition.setFromValue(0.5);
                fadeTransition.setFromValue(0);
            }
            if (numOfClicks % 3 == 2) {
                fadeTransition.setFromValue(0);
                fadeTransition.setFromValue(1);
            }
            numOfClicks++;
            fadeTransition.play();
        }
    }

    public void mouseEndOfInputButtonActionListener() {
        if (mouseInputTextField.getText().equals("")) {
            new Alert(Alert.AlertType.WARNING, "No encryption message was written.").show();
        } else {
            try {
                produceProcessedMessage(mouseInputTextField.getText());
            } catch (InvalidCharactersException | InputMismatchException e) {
                setCodeLabel.setText(e.getLocalizedMessage());
            }
        }
    }

    public void clearInputButtonActionListener() {
        if (mouseInputTextField.getText().equals("")) {
            new Alert(Alert.AlertType.WARNING, "No encryption message was written.").show();
        } else {
            lastOutputButton.getStyleClass().remove("pressed-keyboard-button-output");
            mouseInputTextField.setText("");
            mouseOutputTextField.setText("");
        }
    }

    public void initializeMachineStatesAndMouseInputKeyboard() {
        // Machine States
        firstMachineStateComponentController.setInitializedControllerComponents(AppController.getModelMain().getEngine().getEngineDTO());
        currentMachineStateComponentController.setInitializedControllerComponents(AppController.getModelMain().getEngine().getEngineDTO());

        // Enigma code input
        currentMachineInitialStateLabel.setText(AppController.getModelMain().getMachineStatisticsAndHistory());
        updateButtonsCSS();
        setCodeLabel.setText("");

    }

    public void updateDynamicKeyboards() {
        createDynamicInputKeyboardFromABC();
        createDynamicOutputKeyboardFromABC();
    }

    private void createDynamicInputKeyboardFromABC() {
        List<Character> xmlABC = AppController.getModelMain().getXmlDTO().getABCFromXML();
        EventHandler<MouseEvent> mouseClickHandler = event -> {
            if (MouseButton.PRIMARY.equals(event.getButton()) || MouseButton.SECONDARY.equals(event.getButton())) {
                mouseInputTextField.setText(mouseInputTextField.getText() + ((Button)event.getSource()).getText()); // Updates input text
                try {
                    String outputLetter = AppController.getModelMain().getMessageAndProcessIt(((Button)event.getSource()).getText(), false); // Creates output
                    mouseOutputTextField.setText(mouseOutputTextField.getText() + outputLetter); // Updates output text
                    for (Node node : mouseOutputFlowPane.getChildren()) {
                        if (node instanceof Button) {
                            Button button = (Button)node;
                            if (button.getText().equalsIgnoreCase(outputLetter)) {
                                rotateTransition.setNode((Button)event.getSource());
                                node.getStyleClass().add("pressed-keyboard-button-output");
                                if (lastOutputButton != null) {
                                    lastOutputButton.getStyleClass().remove("pressed-keyboard-button-output");
                                }
                                lastOutputButton = button;

                                if (isAnimation) {
                                    scaleTransition.setNode(button);

                                    parallelTransition.setCycleCount(1);
                                    parallelTransition.setAutoReverse(true);
                                    parallelTransition.play();
                                }
                            }
                        }
                    }
                } catch (InvalidCharactersException e) {
                    new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).show();
                }
            }
        };
        // Remove old keyboard buttons
        if (mouseInputFlowPane.getChildren().size() > 0) {
            mouseInputFlowPane.getChildren().subList(0, mouseInputFlowPane.getChildren().size()).clear();
        }
        // Add new
        for (Character ch : xmlABC) {
            Button currButton = new Button(ch.toString());
            currButton.setOnMouseClicked(mouseClickHandler);
            currButton.getStyleClass().add("enigma-keyboard-button");
            mouseInputFlowPane.getChildren().add(currButton);
        }
    }

    private void createDynamicOutputKeyboardFromABC() {
        List<Character> xmlABC = AppController.getModelMain().getXmlDTO().getABCFromXML();
        // Remove old keyboard buttons
        if (mouseOutputFlowPane.getChildren().size() > 0) {
            mouseOutputFlowPane.getChildren().subList(0, mouseOutputFlowPane.getChildren().size()).clear();
        }
        // Add new
        for (Character ch : xmlABC) {
            Button currButton = new Button(ch.toString());
            currButton.getStyleClass().add("enigma-keyboard-button");
            mouseOutputFlowPane.getChildren().add(currButton);
        }
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void resetMachineStateButtonActionListener() {
        AppController.getModelMain().resetMachine();
        mainController.resetScreens(true, this);

        encryptionDecryptionInputButton.getStyleClass().remove("chosen-button");
        resetMachineStateButton.getStyleClass().add("chosen-button");
    }


    @FXML
    void inputToEncryptDecryptOnKeyPressed() {
        updateButtonsCSS();
    }

    @FXML
    void inputToEncryptDecryptOnMouseClicked() {
        updateButtonsCSS();
    }

    @FXML
    void inputToEncryptDecryptOnMousePressed() {
        updateButtonsCSS();
    }

    private void updateButtonsCSS() {
        encryptionDecryptionInputButton.getStyleClass().add("chosen-button");
        resetMachineStateButton.getStyleClass().remove("chosen-button");
    }

    public void updateMachineStateAndStatistics(String currentMachineState) {
        machineStateModel.setCurrentMachineState(currentMachineState);
        currentMachineInitialStateLabel.setText(AppController.getModelMain().getMachineStatisticsAndHistory());
        currentMachineStateComponentController.setInitializedControllerComponents(AppController.getModelMain().getEngine().getEngineDTO());
    }

    public void resetMachineStateAndStatistics(boolean bool) {
        if (bool) {
            setCodeLabel.setText("Machine state has been successfully reset");
        }
        machineStateModel.setCurrentMachineState(machineStateModel.getFirstMachineState());
        currentMachineStateComponentController.resetMachineStateComponentComponent(AppController.getModelMain().getEngine().getEngineDTO());
    }
    class ClearStatusListener implements ChangeListener<String> {
        @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            setCodeLabel.setText("");
        }
    }
    public void updateLabelTextsToEmpty(Object component) {
        setCodeLabel.setText("");
        inputToEncryptDecryptInput.setText("");
        if (!component.getClass().getSimpleName().equals("ClearStatusListener") && lastOutputButton != null) {
            lastOutputButton.getStyleClass().remove("pressed-keyboard-button-output");
        }
        mouseInputTextField.setText("");
        mouseOutputTextField.setText("");
    }


    public void updateStylesheet(Number num) {
        firstMachineStateComponentController.updateStylesheet(num);
        currentMachineStateComponentController.updateStylesheet(num);
        mainScrollPane.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainScrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("encryptDecrypt/encryptDecryptStyleOne.css")).toString());
        } else if (num.equals(1)) {
            mainScrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("encryptDecrypt/encryptDecryptStyleTwo.css")).toString());
        } else {
            mainScrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("encryptDecrypt/encryptDecryptStyleThree.css")).toString());
        }
    }

    public void updateAnimation(Number num) {
        isAnimation = num.equals(1);
    }
}