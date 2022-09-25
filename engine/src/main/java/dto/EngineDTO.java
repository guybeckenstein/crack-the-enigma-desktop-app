package dto;

import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Engine->DTO->UI
public class EngineDTO {
    private final int totalNumberOfRotors;
    private final int totalReflectors;
    private final Map<Character, Character> plugBoard;
    private final String selectedReflector;
    private final List<Character> currentSelectedRotorsPositions;
    private final List<Pair<Integer,Integer>> selectedRotorsAndNotchesPosition;
    private final int messagesSentCounter;

    public EngineDTO(int rotorsNum,
                     int reflectorsNum,
                     Map<Character, Character> plugBoard,
                     String selectedReflector,
                     List<Character> currentSelectedRotorsPositions,
                     List<Pair<Integer,Integer>> selectedRotorsAndNotchesDistanceFromWindows,
                     int messagesSentCounter) {

        this.totalNumberOfRotors = rotorsNum;
        this.totalReflectors = reflectorsNum;
        this.plugBoard = plugBoard;
        this.selectedReflector = selectedReflector;
        this.currentSelectedRotorsPositions = currentSelectedRotorsPositions;
        this.selectedRotorsAndNotchesPosition = selectedRotorsAndNotchesDistanceFromWindows;
        this.messagesSentCounter = messagesSentCounter;
    }

    public int getTotalNumberOfRotors() {
        return this.totalNumberOfRotors;
    }

    public int getTotalReflectors() {
        return this.totalReflectors;
    }

    public Map<Character, Character> getPlugBoard() {
        return this.plugBoard;
    }

    public List<Integer> getSelectedRotors() {
        return this.selectedRotorsAndNotchesPosition.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    public String getSelectedReflector() {
        return this.selectedReflector;
    }

    public List<Character> getCurrentSelectedRotorsPositions() {
        return this.currentSelectedRotorsPositions;
    }

    public List<Pair<Integer,Integer>> getSelectedRotorsAndNotchesPosition() {
        return this.selectedRotorsAndNotchesPosition;
    }

    public int getMessagesSentCounter() {
        return messagesSentCounter;
    }
}