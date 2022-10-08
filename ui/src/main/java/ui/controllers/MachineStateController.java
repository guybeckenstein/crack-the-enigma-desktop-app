package ui.controllers;

import dto.EngineDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MachineStateController {
    @FXML private HBox mainHBox;
    @FXML private Label reflectorID;
    @FXML private HBox plugBoardTopRow;
    @FXML private HBox plugBoardBottomRow;
    private int lastAddedRotorVBox;
    private int lastAddedPlugBoardHBox;

    public MachineStateController() {

    }

    @FXML
    private void initialize() { }

    public void setInitializedControllerComponents(EngineDTO DTO) {
        removeOldConfiguration();
        // Modify collections types
        List<Pair<String, String>> selectedRotorsAndNotchesPosition = new ArrayList<>();
        for (Pair<Integer, Integer> p : DTO.getSelectedRotorsAndNotchesPosition()) {
            selectedRotorsAndNotchesPosition.add(new Pair<>(p.getKey().toString(), p.getValue().toString()));
        }
        List<String> selectedRotorsPositions = DTO.getCurrentSelectedRotorsPositions()
                .stream().map(ch -> Character.toString(ch))
                .collect(Collectors.toList());
        // Update configuration
        addNewRotorsConfiguration(DTO.getSelectedReflector(), selectedRotorsAndNotchesPosition, selectedRotorsPositions, DTO.getPlugBoard());
        lastAddedRotorVBox = selectedRotorsAndNotchesPosition.size();
        lastAddedPlugBoardHBox = DTO.getPlugBoard().size();
    }

    private void removeOldConfiguration() {
        for (int i = 0; i < lastAddedRotorVBox; i++) {
            mainHBox.getChildren().remove(2);
        }
        for (int i = 0; i < lastAddedPlugBoardHBox; i++) {
            plugBoardTopRow.getChildren().remove(0);
            plugBoardBottomRow.getChildren().remove(0);
        }
    }

    private void addNewRotorsConfiguration(String selectedReflector, List<Pair<String, String>> selectedRotorsAndNotchesPosition, List<String> selectedRotorsPositions, Map<Character, Character> enginePlugBoard) {
        for (int i = 0; i < selectedRotorsAndNotchesPosition.size(); i++) {
            Label headerLabel = new Label("Rotor");
            headerLabel.getStyleClass().add("header-label");

            addConfigurationRotorVBox(selectedRotorsAndNotchesPosition, selectedRotorsPositions, i, headerLabel);
        }
        enginePlugBoard.forEach((abcLetter1, abcLetter2) -> {
            plugBoardTopRow.getChildren().add(new Label(abcLetter1.toString()));
            plugBoardBottomRow.getChildren().add(new Label(abcLetter2.toString()));
        });
        reflectorID.setText(selectedReflector);
    }

    private void addConfigurationRotorVBox(List<Pair<String, String>> selectedRotorsAndNotchesPosition, List<String> selectedRotorsPositions, int i, Label headerLabel) {
        VBox rotorContainer = new VBox(
                headerLabel,
                new Label(selectedRotorsPositions.get(i) + "  " + selectedRotorsAndNotchesPosition.get(i).getValue()),
                new Label(selectedRotorsAndNotchesPosition.get(i).getKey()));
        rotorContainer.getStyleClass().add("vbox");
        mainHBox.getChildren().add(i + 2, rotorContainer);
    }

    public void resetMachineStateComponentComponent(EngineDTO initializedDTO) {
        setInitializedControllerComponents(initializedDTO);
    }

    public void updateStylesheet(Number num) {
        mainHBox.getStylesheets().remove(0);
        if (num.equals(0)) {
            mainHBox.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("machineDetails/machineStates/machineStateStyleOne.css")).toString());
        } else if (num.equals(1)) {
            mainHBox.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("machineDetails/machineStates/machineStateStyleTwo.css")).toString());
        } else {
            mainHBox.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("machineDetails/machineStates/machineStateStyleThree.css")).toString());
        }
    }
}
