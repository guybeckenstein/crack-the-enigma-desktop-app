package ui.controllers;

import dto.XmlDTO;
import enigmaEngine.exceptions.InvalidCharactersException;
import enigmaEngine.exceptions.InvalidPlugBoardException;
import enigmaEngine.exceptions.InvalidReflectorException;
import enigmaEngine.exceptions.InvalidRotorException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.ListSelectionView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputScreenController {
    private MachineDetailsController mainController;
    private XmlDTO xmlDTO;
    @FXML private ListSelectionView<Integer> rotorsListSelectionView;
    private List<String> rotorsStartingPositions;
    @FXML private ChoiceBox<String> reflectorChoiceBox;

    public InputScreenController() {
        xmlDTO = null;
        rotorsStartingPositions = new ArrayList<>();
    }

    @FXML
    private void initialize() {
    }

    public void setMainController(MachineDetailsController mainController) {
        this.mainController = mainController;
    }

    public void setXmlDTO(XmlDTO xmlDTO) {
        this.xmlDTO = xmlDTO;

        if (this.xmlDTO != null) {
            rotorsListSelectionView.getSourceItems().remove(0, rotorsListSelectionView.getSourceItems().size());
            rotorsListSelectionView.getTargetItems().remove(0, rotorsListSelectionView.getTargetItems().size());
            reflectorChoiceBox.getItems().addAll(this.xmlDTO.getReflectorsFromXML());
        }
        rotorsListSelectionView.getSourceItems().addAll(this.xmlDTO.getRotorsFromXML());
        reflectorChoiceBox.setValue("I");
    }

    @FXML
    void submitButtonActionListener() {
        if (rotorsListSelectionView.getTargetItems().size() < 2) {
            new Alert(Alert.AlertType.ERROR, "The configuration must have at least two rotors.").showAndWait();
        } else {
            final int[] allIterations = {0};
            for (int i = 0; i < rotorsListSelectionView.getTargetItems().size(); i++) {
                Stage stage = new Stage();

                ChoiceBox<String> chooseStartingPositionChoiceBox = new ChoiceBox<>();
                Label label = new Label("Choose the starting position of rotor #" + (i + 1) + ".");
                List<String> abcStrList = xmlDTO.getABCFromXML().stream().map(String::valueOf).collect(Collectors.toList());
                chooseStartingPositionChoiceBox.getItems().addAll(abcStrList);
                chooseStartingPositionChoiceBox.setValue(abcStrList.get(0));
                VBox vBox = new VBox(label, chooseStartingPositionChoiceBox);
                Scene scene = new Scene(vBox);
                stage.setScene(scene);
                stage.show();
                int finalI = i;
                stage.setOnCloseRequest(event -> {
                    System.out.println("Stage #" + (finalI + 1) + " is closing.");
                    rotorsStartingPositions.add(chooseStartingPositionChoiceBox.getValue());
                    if (++allIterations[0] == rotorsListSelectionView.getTargetItems().size()) {
                        setEngineConfiguration();
                        mainController.updateMachineStatesAndDisability(AppController.getModelMain().getMachineHistoryStates().getCurrentMachineCode(), false);
                        mainController.updateConfigurationsAndScreens();
                    }
                });
            }
        }
    }

    private void setEngineConfiguration() {
        List<String> chosenRotors = rotorsListSelectionView.getTargetItems()
                .stream().map(String::valueOf).collect(Collectors.toList());
        String chosenRotorsString = String.join(",", chosenRotors);
        String chosenRotorsStartingPositionsString = String.join("", rotorsStartingPositions);
        String reflectorID = reflectorChoiceBox.getValue();
        try {
            AppController.getModelMain().initializeEnigmaCodeManually(chosenRotorsString, chosenRotorsStartingPositionsString, "", reflectorID);
        } catch (InvalidRotorException | InvalidReflectorException | InvalidPlugBoardException |
                 InvalidCharactersException e) {
            throw new RuntimeException(e); // Will never happen because input is valid
        }
    }
}
