package ui.controllers;

import ui.decryptionManager.Difficulty;
import enigmaEngine.interfaces.EnigmaEngine;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.layout.GridPane;
import ui.impl.Trie;
import ui.impl.models.MachineStateModel;
import enigmaEngine.exceptions.InvalidCharactersException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

// Third screen
public class BruteForceController {
    // Buttons component
    @FXML VBox dmButtonsVBox;
    @FXML public Button startResumeDM;
    @FXML public Button pauseDM;
    @FXML public Button stopDM;
    // Main component
    private AppController mainController;
    // Models
    private final MachineStateModel machineStatesConsole;
    //
    @FXML private VBox bruteForceVBox;
    // Machine states
    @FXML private Label firstMachineStateLabel;
    @FXML private Label currentMachineStateLabel;
    // Search for words
    @FXML private VBox searchVBox; // Only for binding the ENTER key to the input text field
    @FXML private TextField searchInputTextField;
    @FXML private ListView<String> searchDictionaryWordsListView;
    Trie dictionaryTrie;
    // Input to encrypt / decrypt
    @FXML private VBox keyboardInputVBox; // Only for binding the ENTER key to the input text field
    @FXML private TextField inputToEncryptDecryptInput;
    @FXML private TextField enigmaOutputTextField;
    @FXML private Button enterCurrentKeyboardInputButton;

    // DM Operational component
    @FXML private GridPane decryptionManagerGridPane;
    @FXML private Label totalAgentsLabel;
    @FXML private Slider agentsSliderInput;
    @FXML private ChoiceBox<String> difficultyLevelInput;
    @FXML private Label difficultyLevelLabel;
    @FXML private Label missionSizeLabel;
    @FXML private TextField missionSizeInput;
    @FXML private Label totalMissionsLabel;
    private Difficulty dmDifficultyLevel;
    // DM Output
    @FXML private TextArea finalCandidatesTextArea;
    @FXML ProgressBar progressBar;
    @FXML Label progressPercentLabel;
    @FXML Label averageTime;
    private EnigmaEngine taskCurrentConfiguration;
    private ui.decryptionManager.DecryptionManagerTask dmTask;
    private String timeElapsed = "";
    @FXML private ScrollPane mainScrollPane;


    public BruteForceController() {
        machineStatesConsole = new MachineStateModel(); // Model
    }

    @FXML
    private void initialize() {
        // Only for binding the ENTER key to the input text field
        enterCurrentKeyboardInputButton.setOnAction(this::enterCurrentKeyboardInputButtonActionListener);
        keyboardInputVBox.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                enterCurrentKeyboardInputButton.fire();
                ev.consume();
            }
        });

        // Updates total agents
        totalAgentsLabel.setText(Integer.toString((int)agentsSliderInput.getValue()));
        setBruteForceDisability(true);
        agentsSliderInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            totalAgentsLabel.setText(Integer.toString((int)agentsSliderInput.getValue()));
            if (!missionSizeLabel.getText().equals("")) {
                updateMissionsLabel();
            }
        });

        // Updates difficulty level
        difficultyLevelInput.getItems().addAll(Arrays.stream(Difficulty.values()).map(Enum::name).toArray(String[]::new));
        difficultyLevelInput.setValue("EASY");
        dmDifficultyLevel = Difficulty.EASY;
        difficultyLevelInput.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                dmDifficultyLevel = Difficulty.EASY;
            } else if (newValue.intValue() == 1) {
                dmDifficultyLevel = Difficulty.MEDIUM;
            } else if (newValue.intValue() == 2) {
                dmDifficultyLevel = Difficulty.HARD;
            } else if (newValue.intValue() == 3) {
                dmDifficultyLevel = Difficulty.IMPOSSIBLE;
            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid difficulty level");
            }
            updateMissionsLabel();
            String difficultyLevel = dmDifficultyLevel.toString().toLowerCase();
            difficultyLevelLabel.setText(difficultyLevel.substring(0, 1).toUpperCase() + difficultyLevel.substring(1));
        });

        // Updates mission size
        missionSizeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                missionSizeLabel.setText(newValue);
                updateMissionsLabel();
            } catch (NumberFormatException e) {
                totalMissionsLabel.setText("NaN");
                missionSizeLabel.setText("Invalid input");
            }
        });

        decryptionManagerGridPane.setDisable(true);
        dmButtonsVBox.setDisable(true);

        searchInputTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDictionaryWordsListView.getItems().remove(0, searchDictionaryWordsListView.getItems().size());

            List<String> results = dictionaryTrie.getWordsWithPrefix(newValue.toUpperCase());
            if (results != null) {
                searchDictionaryWordsListView.getItems().addAll(results);
            }
        });

        searchDictionaryWordsListView.onMousePressedProperty().addListener((observable, oldValue, newValue) -> {
            String selectedWord = searchDictionaryWordsListView.getSelectionModel().getSelectedItem();
            if (selectedWord != null) {
                inputToEncryptDecryptInput.setText(selectedWord);
            }
        });

        searchDictionaryWordsListView.editableProperty().setValue(false);
        searchDictionaryWordsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedWord = searchDictionaryWordsListView.getSelectionModel().getSelectedItem();
            if (selectedWord != null && inputToEncryptDecryptInput.getText() != null) {
                inputToEncryptDecryptInput.setText(inputToEncryptDecryptInput.getText() + selectedWord + " "); // Added " "
            }
        });
        firstMachineStateLabel.textProperty().bind(machineStatesConsole.firstMachineStateProperty());
        currentMachineStateLabel.textProperty().bind(machineStatesConsole.currentMachineStateProperty());
    }

    private void updateMissionsLabel() {
        long missionSize;
        try {
            missionSize = Long.parseLong(missionSizeInput.getText());
            if (missionSize <= 0) {
                throw new ArithmeticException();
            }
        } catch (NumberFormatException | ArithmeticException e) {
            totalMissionsLabel.setText("NaN");
            missionSizeLabel.setText("Invalid input");
            return;
        }
        EnigmaEngine enigmaEngine = AppController.getModelMain().getEngine();
        totalMissionsLabel.setText(Long.toString(
                Difficulty.translateDifficultyLevelToMissions(dmDifficultyLevel, enigmaEngine.getEngineDTO(),
                        AppController.getModelMain().getXmlDTO().getReflectorsFromXML().size(), enigmaEngine.getABCSize())
                        / (missionSize
                        * (long)agentsSliderInput.getValue())));
    }

    @FXML
    void StartResumeDMActionListener() {
        toggleTaskButtons(true);

        if (startResumeDM.getText().contains("Resume")) {
            synchronized (dmTask) {
                dmTask.setPaused(false);
                dmTask.notifyAll();
            }
        } else if (startResumeDM.getText().contains("Start")) {
            dmTask = new ui.decryptionManager.DecryptionManagerTask(
                    enigmaOutputTextField.getText(), Long.parseLong(totalMissionsLabel.getText()),
                    Integer.parseInt(totalAgentsLabel.getText()), Integer.parseInt(missionSizeLabel.getText()),
                    dmDifficultyLevel, AppController.getModelMain().getXmlDTO().getReflectorsFromXML().size(),
                    AppController.getModelMain().getXmlDTO().getRotorsFromXML().size(), this, () -> dmOnFinished());

            Thread th = new Thread(dmTask);
            th.start();
            th.setDaemon(true);
            System.out.println("Starting the task...");
        } else {
            throw new RuntimeException();
        }

        if (startResumeDM.getText().contains("Start")) {
            startResumeDM.setText(startResumeDM.getText().replace("Start", "Resume"));
        }
    }

    public EnigmaEngine getTaskCurrentConfiguration() {
        return taskCurrentConfiguration;
    }

    public synchronized void updateValues(Queue<String> newCandidates, String averageElapsedTime) {
        while (!newCandidates.isEmpty()) {
            if (finalCandidatesTextArea.getText().equals("")) {
                finalCandidatesTextArea.setText(newCandidates.remove());
            } else {
                finalCandidatesTextArea.setText(finalCandidatesTextArea.getText() + "\n" + newCandidates.remove());
            }
        }
        averageTime.setText(averageElapsedTime);
    }

    private void toggleTaskButtons(boolean bool) {
        keyboardInputVBox.setDisable(bool);
        searchVBox.setDisable(bool);
        decryptionManagerGridPane.setDisable(bool);
        startResumeDM.setDisable(bool);
        stopDM.setDisable(!bool);
        pauseDM.setDisable(!bool);
    }

    @FXML
    void PauseDMActionListener() {
        dmTask.setPaused(true);
        startResumeDM.setDisable(false);
        pauseDM.setDisable(true);
    }

    @FXML
    void StopDMActionListener() {
        startResumeDM.setText(startResumeDM.getText().replace("Resume", "Start"));
        toggleTaskButtons(false);
        if (dmTask != null) {
            dmOnFinished();
        }

    }

    @FXML
    void setDMPropertiesActionListener() {
        try {
            if (missionSizeLabel.getText().equals("NaN") || missionSizeLabel.getText().equals("Invalid input")) {
                throw new InputMismatchException("There is no valid mission size input.");
            } else if (missionSizeLabel.getText().equals("0")) {
                throw new InputMismatchException("Each agent must perform at least 1 mission (task).");
            }
            dmButtonsVBox.setDisable(false);
            startResumeDM.setDisable(false);
            pauseDM.setDisable(true);
            stopDM.setDisable(true);

            finalCandidatesTextArea.setText("");
        } catch (InputMismatchException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid input");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void setBruteForceDisability(boolean bool) {
        // enable brute force top screen
        bruteForceVBox.setDisable(bool);
        // disable or remove brute force history
        if (dmTask != null) {
            dmTask.cancel();
            unbindTaskFromUIComponents("0", false);

            decryptionManagerGridPane.setDisable(!bool);
            dmButtonsVBox.setDisable(!bool);

            updateDecryptionManagerDetails();
        }

    }

    private void updateDecryptionManagerDetails() {
        startResumeDM.setText(startResumeDM.getText().replace("Resume", "Start"));

        progressBar.setProgress(0);
        progressPercentLabel.setText("0 %");
        averageTime.setText("0.000000");
        finalCandidatesTextArea.setText("");
        timeElapsed = "0";
    }

    public void updateAgentsMaxSizeAndDictionary(int amountAgents) {
        agentsSliderInput.setMax(amountAgents);

        List<String> allWords = mainController.getDictionary().stream().map(String::trim) // Sorted dictionary list
                .sorted().collect(Collectors.toList());
        searchDictionaryWordsListView.getItems().addAll(allWords); // Sorted dictionary list view

        System.out.println("Dictionary trie created");
        dictionaryTrie = new Trie();
        allWords.forEach(dictionaryTrie::insert);
    }

    @FXML
    void enterCurrentKeyboardInputButtonActionListener(ActionEvent event) {
        try {
            String messageInput = inputToEncryptDecryptInput.getText().toUpperCase().trim();
            if (messageInput.equals("")) {
                throw new InputMismatchException("No encryption message was written.");
            }
            invalidMessageInput(messageInput);
            if (messageInput.charAt(messageInput.length() - 1) == ' ') {
                messageInput = messageInput.substring(0, messageInput.length() - 1);
            }
            String messageOutput = AppController.getModelMain().getMessageAndProcessIt(messageInput, true);

            new Alert(Alert.AlertType.CONFIRMATION, "Processed message: " + messageInput + " -> " + messageOutput).show();
            enigmaOutputTextField.setText(messageOutput);
            mainController.updateScreens(AppController.getModelMain().getCurrentMachineStateAsString());
            decryptionManagerGridPane.setDisable(false);
            mainController.updateLabelTextsToEmpty(this);
            taskCurrentConfiguration = AppController.getModelMain().getEngine().deepClone();
        } catch (InvalidCharactersException | InputMismatchException e) {
            new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).show();
        }
    }

    private void invalidMessageInput(String messageInput) {
        Set<String> dictionaryWords = AppController.getModelMain().getEngine().getWordsDictionary().getWords();
        new ArrayList<>(Arrays.asList(AppController.getModelMain().getXmlDTO().getExcludedCharacters().split(""))).forEach((ch) -> {
            if (messageInput.contains(ch)) {throw new InputMismatchException("The encryption message \" " + messageInput
                        + " \" contains at least one dictionary's illegal characters: \" "
                        + AppController.getModelMain().getXmlDTO().getExcludedCharacters() + " \"");
            }});
        for (String splittedStr : messageInput.split(" ")) {
            if (!dictionaryWords.contains(splittedStr)) {
                throw new InputMismatchException("The encryption message must contain only dictionary words.");
            }
        }
    }

    public void initializeMachineStates(String machineStateConsoleString) {
        machineStatesConsole.setFirstMachineState(machineStateConsoleString);
        machineStatesConsole.setCurrentMachineState(machineStateConsoleString);
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void resetMachineStateButtonActionListener() {
        AppController.getModelMain().resetMachine();
        mainController.resetScreens(true, null);
    }

    public void updateMachineStateAndDictionary(String currentMachineState) {
        machineStatesConsole.setCurrentMachineState(currentMachineState);
    }

    public void resetMachineStateAndEnigmaOutput(boolean bool, Object controller) {
        if (bool && controller == null) {
            new Alert(Alert.AlertType.INFORMATION, "Machine state has been successfully reset.").show();
        }
        machineStatesConsole.setCurrentMachineState(machineStatesConsole.getFirstMachineState());
        enigmaOutputTextField.setText("NaN");
    }

    public void updateLabelTextsToEmpty() {
        inputToEncryptDecryptInput.setText("");
        averageTime.setText("");
    }


    public void updateStylesheet(Number num) {
        mainScrollPane.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainScrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("bruteForce/bruteForceStyleOne.css")).toString());
        } else if (num.equals(1)) {
            mainScrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("bruteForce/bruteForceStyleTwo.css")).toString());
        } else {
            mainScrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("bruteForce/bruteForceStyleThree.css")).toString());
        }
    }

    public void bindTaskToUIComponents(Task<Boolean> aTask, Runnable onFinish) {
        // task message
        // finalCandidatesTextArea.textProperty().bind(aTask.messageProperty());

        // task progress bar
        progressBar.progressProperty().bind(aTask.progressProperty());

        // task percent label
        progressPercentLabel.textProperty().bind(
                Bindings.concat(
                        Bindings.format(
                                "%.0f",
                                Bindings.multiply(
                                        aTask.progressProperty(),
                                        100)),
                        " %"));

        // task cleanup upon finish
        aTask.valueProperty().addListener((observable, oldValue, newValue) -> onTaskFinished(Optional.ofNullable(onFinish)));
    }

    public void unbindTaskFromUIComponents(String timeElapsed, boolean bool) {
        progressBar.progressProperty().unbind();
        progressPercentLabel.textProperty().unbind();
        toggleTaskButtons(false);
        if (bool) {
            this.timeElapsed = timeElapsed;
        }
    }

    public void onTaskFinished(Optional<Runnable> onFinish) {
        progressBar.progressProperty().unbind();
        progressPercentLabel.textProperty().unbind();
        onFinish.ifPresent(Runnable::run);
    }

    private void dmOnFinished() {
        timeElapsed = dmTask.getTimeElapsed();
        dmTask.cancel();
        dmTask = null;
        unbindTaskFromUIComponents(timeElapsed, true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This took " + timeElapsed + " seconds.");
        alert.setTitle("Done!");
        alert.setHeaderText("Done!");
        alert.showAndWait();

        updateDecryptionManagerDetails();
    }
}