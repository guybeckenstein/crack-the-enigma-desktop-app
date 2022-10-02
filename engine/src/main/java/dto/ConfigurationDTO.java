package dto;

import enigmaEngine.interfaces.Reflector;
import javafx.util.Pair;

import java.io.*;
import java.util.List;

public class ConfigurationDTO implements Serializable {
    private List<Integer> rotorsIDInorder;
    private final List<Character> startingPositions;
    private Reflector.ReflectorID selectedReflectorID;
    private final List<Pair<Character, Character>> plugBoard;
    private final int abcSize;
    private final String abc;

    public ConfigurationDTO(List<Integer> rotorsIDInorder, List<Character> startingPositions, Reflector.ReflectorID selectedReflectorID, List<Pair<Character, Character>> plugBoard, String abc) {
        this.rotorsIDInorder = rotorsIDInorder;
        this.startingPositions = startingPositions;
        this.selectedReflectorID = selectedReflectorID;
        this.plugBoard = plugBoard;
        this.abc = abc;
        abcSize = abc.length();
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

    public void incrementStartingPositions() {
        int i = startingPositions.size() - 1;
        while (i >= 0) {
            int index = abc.indexOf(startingPositions.get(i));
            if (index == abcSize - 1) {
                startingPositions.set(i, abc.charAt(0));
                i--;
            } else {
                startingPositions.set(i, abc.charAt(index + 1));
                break;
            }
        }
    }

    public void incrementSelectedReflector(int totalReflectors) {
        int incrementedReflectorNumericID = (selectedReflectorID.ordinal() + 1) % totalReflectors;
        selectedReflectorID = Reflector.ReflectorID.values()[incrementedReflectorNumericID];
    }

    public void setRotorsIDInorder(List<Integer> rotorsIDInorder) {
        this.rotorsIDInorder = rotorsIDInorder;
    }

    @Override
    public String toString() {
        return "rotorsIDInorder=" + rotorsIDInorder +
                ", startingPositions=" + startingPositions +
                ", selectedReflectorID=" + selectedReflectorID;
    }

    public ConfigurationDTO deepClone() { // Serializing
        ConfigurationDTO clone;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            clone = (ConfigurationDTO) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return clone;
    }
}