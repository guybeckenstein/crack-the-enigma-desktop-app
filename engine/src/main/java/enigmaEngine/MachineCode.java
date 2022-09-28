package enigmaEngine;

import dto.EngineDTO;
import enigmaEngine.interfaces.EnigmaEngine;
import enigmaEngine.interfaces.Reflector;
import javafx.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class MachineCode {
    private final List<Integer> rotorsIDInorder;
    private final List<Character> startingPositions;
    private final Reflector.ReflectorID selectedReflectorID;
    private final List<Pair<Character, Character>> plugBoard;
    private final int ABCSize;
    private final String ABC;

    public MachineCode(List<Integer> rotorsIDInorder, List<Character> startingPositions, Reflector.ReflectorID selectedReflectorID, List<Pair<Character, Character>> plugBoard, String abc) {
        this.rotorsIDInorder = rotorsIDInorder;
        this.startingPositions = startingPositions;
        this.selectedReflectorID = selectedReflectorID;
        this.plugBoard = plugBoard;
        ABC = abc;
        ABCSize = abc.length();
    }

    public List<Integer> getRotorsIDInorder() {
        return rotorsIDInorder;
    }
    public List<Character> getStartingPositions() {
        return startingPositions;
    }

    public Reflector.ReflectorID getSelectedReflectorID() {
        return selectedReflectorID;
    }

    public List<Pair<Character, Character>> getPlugBoard() {
        return plugBoard;
    }

    public void increment() {
        int i = startingPositions.size() - 1;
        while (i >= 0) {
            int index = ABC.indexOf(startingPositions.get(i));
            if (index == ABCSize - 1) {
                startingPositions.set(i, ABC.charAt(0));
                i--;
            } else {
                startingPositions.set(i, ABC.charAt(index + 1));
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "rotorsIDInorder=" + rotorsIDInorder +
                ", startingPositions=" + startingPositions +
                ", selectedReflectorID=" + selectedReflectorID;
    }
}