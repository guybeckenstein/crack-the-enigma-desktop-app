package ui.controllers;

import automateDecryption.Difficulty;
import enigmaEngine.interfaces.EnigmaEngine;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import ui.impl.Trie;
import ui.impl.models.MachineStateConsole;
import dto.EngineDTO;
import enigmaEngine.exceptions.InvalidCharactersException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class BruteForceController {
    // Buttons component
    @FXML VBox dmButtonsVBox;
    @FXML public Button startResumeDM;
    @FXML public Button pauseDM;
    @FXML public Button stopDM;
    // Main component
    private AppController mainController;
    // Models
    private MachineStateConsole machineStatesConsole;
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
    @FXML private Button setDMProperties;
    @FXML private Label totalMissionsLabel;
    private int dmMissionSize;
    private Difficulty dmDifficultyLevel;
    // DM Output
    @FXML private TextArea finalCandidatesTextArea;
    StringProperty finalCandidates;
    boolean existingInput = false;
    @FXML ProgressBar progressBar;
    @FXML Label progressPercentLabel;
    @FXML Label averageTime;
    private EnigmaEngine taskCurrentConfiguration;
    private ui.decryptionManager.DecryptionManagerTask dmTask;
    private BooleanProperty dmResult;
    private String timeElapsed = "";
    @FXML private ScrollPane mainScrollPane;


    public BruteForceController() {
        finalCandidates = new SimpleStringProperty("");
        machineStatesConsole = new MachineStateConsole(); // Model
    }

    @FXML
    private void initialize() {
        // finalCandidatesTextArea.textProperty().bindBidirectional(finalCandidates);
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
            if (!missionSizeLabel.equals("")) {
                updateMissionsLabel();
            }
        });

        // Updates difficulty level
        difficultyLevelInput.getItems().addAll(Arrays.stream(Difficulty.values()).map(Enum::name).toArray(String[]::new));
        difficultyLevelInput.setValue("EASY");
        dmDifficultyLevel = Difficulty.EASY;
        difficultyLevelInput.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    dmDifficultyLevel = Difficulty.EASY;
                    updateMissionsLabel();
                    break;
                case 1:
                    dmDifficultyLevel = Difficulty.MEDIUM;
                    updateMissionsLabel();
                    break;
                case 2:
                    dmDifficultyLevel = Difficulty.HARD;
                    updateMissionsLabel();
                    break;
                case 3:
                    dmDifficultyLevel = Difficulty.IMPOSSIBLE;
                    updateMissionsLabel();
                    break;
            }

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

    @FXML
    public void exit() {

        if (dmTask != null) {
            dmTask.cancel();
        }
    }

    private void updateMissionsLabel() {
        long missionSize;
        try {
            missionSize = Long.parseLong(missionSizeInput.getText());
        } catch (NumberFormatException e) {
            totalMissionsLabel.setText("NaN");
            missionSizeLabel.setText("Invalid input");
            return;
        }
        totalMissionsLabel.setText(Long.toString(
                translateDifficultyLevelToMissions()
                        / (missionSize
                        * (long)agentsSliderInput.getValue())));
    }

    private Long translateDifficultyLevelToMissions() {
        EngineDTO engineDTO = AppController.getConsoleApp().getEngine().getEngineDTO();
        int ABCSize = AppController.getConsoleApp().getEngine().getABCSize();
        switch (difficultyLevelInput.getValue().toUpperCase()) {
            case "EASY":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size());
            case "MEDIUM":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * engineDTO.getTotalReflectors();
            case "HARD":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * engineDTO.getTotalReflectors() * factorial(engineDTO.getSelectedRotors().size());
            case "IMPOSSIBLE":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * engineDTO.getTotalReflectors()
                        * factorial(engineDTO.getSelectedRotors().size()
                        * binomial(engineDTO.getSelectedRotors().size(), engineDTO.getTotalNumberOfRotors()));
            default:
                return (long)0;
        }
    }

    private long factorial(int size) {
        long result = 1;
        for (int i = 1; i <= size; i++) {
            result *= i;
        }
        return result;
    }

    private int binomial(int n, int k)
    {

        // Base Cases
        if (k > n)
            return 0;
        if (k == 0 || k == n)
            return 1;

        // Recur
        return binomial(n - 1, k - 1)
                + binomial(n - 1, k);
    }

    @FXML
    void StartResumeDMActionListener(ActionEvent actionEvent) {
        toggleTaskButtons(true);

        if (startResumeDM.getText().contains("Resume")) {
            synchronized (dmTask) {
                dmTask.setPaused(false);
                dmTask.notifyAll();
            }
        } else if (startResumeDM.getText().contains("Start")) {
            dmTask = new ui.decryptionManager.DecryptionManagerTask(
                    enigmaOutputTextField.getText(), Long.parseLong(totalMissionsLabel.getText()),
                    Integer.parseInt(totalAgentsLabel.getText()), this,
                    () -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This took " + timeElapsed + " seconds.");
                        alert.setTitle("Done!");
                        alert.setHeaderText("Done!");
                        alert.showAndWait();
                        dmTask.cancel();
                        dmTask = null;
                        startResumeDM.setText(startResumeDM.getText().replace("Resume", "Start"));
            });

            Thread th = new Thread(dmTask);
            th.start();
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
    void PauseDMActionListener(ActionEvent actionEvent) {
        dmTask.setPaused(true);
        startResumeDM.setDisable(false);
        pauseDM.setDisable(true);
    }

    @FXML
    void StopDMActionListener(ActionEvent actionEvent) {
        startResumeDM.setText(startResumeDM.getText().replace("Resume", "Start"));
        toggleTaskButtons(false);
        if (dmTask != null) {
            dmTask.cancel();
        }

    }

    @FXML
    void setDMPropertiesActionListener(ActionEvent event) {
        try {
            if (missionSizeLabel.getText().equals("NaN") || missionSizeLabel.getText().equals("Invalid input")) {
                throw new InputMismatchException("There is no valid mission size input");
            }
            mainController.setDMProperties(Integer.parseInt(missionSizeLabel.getText()), Integer.parseInt(missionSizeLabel.getText()), dmDifficultyLevel);
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
        bruteForceVBox.setDisable(bool);
    }

    public void updateAgentsMaxSizeAndDictionary(int amountAgents) {
        agentsSliderInput.setMax(amountAgents);

        List<String> allWords = mainController.getDictionary().stream().map((word) -> word.trim()) // Sorted dictionary list
                .sorted().collect(Collectors.toList());
        searchDictionaryWordsListView.getItems().addAll(allWords); // Sorted dictionary list view

        System.out.println("Dictionary trie created");
        dictionaryTrie = new Trie();
        allWords.forEach(dictionaryTrie::insert);
    }

    @FXML
    void enterCurrentKeyboardInputButtonActionListener(ActionEvent event) {
        try {
            String messageInput = inputToEncryptDecryptInput.getText().toUpperCase(), messageOutput;
            if (messageInput.equals("")) {
                throw new InputMismatchException("No encryption message was written.");
            }
            if (messageInput.charAt(messageInput.length() - 1) == ' ') {
                messageInput = messageInput.substring(0, messageInput.length() - 1);
            }
            messageOutput = AppController.getConsoleApp().getMessageAndProcessIt(messageInput, true);

            new Alert(Alert.AlertType.CONFIRMATION, "Processed message: " + messageInput + " -> " + messageOutput).show();
            enigmaOutputTextField.setText(messageOutput);
            mainController.updateScreens(AppController.getConsoleApp().getCurrentMachineStateAsString());
            decryptionManagerGridPane.setDisable(false);
            mainController.updateLabelTextsToEmpty(this);
            taskCurrentConfiguration = AppController.getConsoleApp().getEngine().deepClone();
            existingInput = true;
        } catch (InvalidCharactersException | InputMismatchException e) {
            new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).show();
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
        AppController.getConsoleApp().resetMachine();
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
        finalCandidates.setValue("");
        averageTime.setText("");
    }


    public void updateStylesheet(Number num) {
        mainScrollPane.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainScrollPane.getStylesheets().add(getClass().getClassLoader().getResource("bruteForce/bruteForceStyleOne.css").toString());
        } else if (num.equals(1)) {
            mainScrollPane.getStylesheets().add(getClass().getClassLoader().getResource("bruteForce/bruteForceStyleTwo.css").toString());
        } else {
            mainScrollPane.getStylesheets().add(getClass().getClassLoader().getResource("bruteForce/bruteForceStyleThree.css").toString());
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
        // aTask.valueProperty().addListener((observable, oldValue, newValue) -> finalCandidatesTextArea.setText(newValue));
        // finalCandidatesTextArea.textProperty().bind(aTask.valueProperty());
    }

    public void unbindTaskFromUIComponents(String timeElapsed) {
        progressBar.progressProperty().unbind();
        progressPercentLabel.textProperty().unbind();
        toggleTaskButtons(false);
        this.timeElapsed = timeElapsed;
    }

    public void onTaskFinished(Optional<Runnable> onFinish) {
        // this.finalCandidatesTextArea.textProperty().unbind();
        this.progressBar.progressProperty().unbind();
        this.progressPercentLabel.textProperty().unbind();
        onFinish.ifPresent(Runnable::run);
    }

    public int getTotalAgentsLabel() {
        return Integer.parseInt(totalAgentsLabel.getText());
    }
}