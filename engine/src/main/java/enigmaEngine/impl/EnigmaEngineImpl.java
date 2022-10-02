package enigmaEngine.impl;

import dto.EngineDTO;
import dto.ConfigurationDTO;
import decryptionManager.WordsDictionary;
import enigmaEngine.exceptions.InvalidCharactersException;
import enigmaEngine.exceptions.InvalidPlugBoardException;
import enigmaEngine.exceptions.InvalidReflectorException;
import enigmaEngine.exceptions.InvalidRotorException;
import enigmaEngine.interfaces.EnigmaEngine;
import enigmaEngine.interfaces.PlugBoard;
import enigmaEngine.interfaces.Reflector;
import enigmaEngine.interfaces.Rotor;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class EnigmaEngineImpl implements EnigmaEngine, Serializable {
    private final HashMap<Integer, Rotor> rotors;
    private final HashMap<Reflector.ReflectorID, Reflector> reflectors;
    private PlugBoard plugBoard;
    private final String machineABC;
    private final Map<Character, Character> machineABCMap;
    private List<Integer> selectedRotors;
    private Reflector selectedReflector;
    private List<Character> startingCharacters;
    private int messagesSentCounter;
    private final List<Rotor> selectedRotorsListRightToLeft;
    private final List<Rotor> selectedRotorsListLeftToRight;
    private WordsDictionary wordsDictionary;


    public EnigmaEngineImpl(HashMap<Integer, Rotor> rotors, HashMap<Reflector.ReflectorID, Reflector> reflectors, PlugBoard plugBoard, String abc) {
        this.rotors = rotors;
        this.reflectors = reflectors;
        this.plugBoard = plugBoard;
        this.machineABC = abc;
        this.startingCharacters = new ArrayList<>();
        this.machineABCMap = new HashMap<>();
        this.messagesSentCounter = 0;
        for (int i = 0; i < machineABC.length(); i++) {
            machineABCMap.put(machineABC.charAt(i), machineABC.charAt(i));
        }
        this.selectedRotorsListRightToLeft = new ArrayList<>();
        this.selectedRotorsListLeftToRight = new ArrayList<>();
        this.selectedRotors = new ArrayList<>();
    }

    @Override
    public HashMap<Integer, Rotor> getRotors() {
        return this.rotors;
    }

    @Override
    public int getABCSize() {
        return this.machineABC.length();
    }

    @Override
    public char activate(char input) {

        // Rotates the first rotor
        selectedRotorsListRightToLeft.get(0).rotate();

        char temp = plugBoard.returnCharacterPair(input);
        int index = machineABC.indexOf(temp);

        index = runRotorPipelineList(index, Rotor.Direction.LEFT);
        index = selectedReflector.findPairByIndex(index);
        index = runRotorPipelineList(index, Rotor.Direction.RIGHT);
        temp = machineABC.charAt(index);

        return plugBoard.returnCharacterPair(temp);
    }

    @Override
    public String processMessage(String input) throws InvalidCharactersException {

        input = input.toUpperCase();
        if (stringToList(input).stream().anyMatch(c -> !machineABCMap.containsKey(c))) {
            throw new InvalidCharactersException("Starting characters must be in the machine ABC");
        }
        StringBuilder output = new StringBuilder();
        messagesSentCounter++;
        for (int i = 0; i < input.length(); i++) {
            output.append(activate(input.charAt(i)));
        }

        return output.toString();
    }


    @Override
    public void setSelectedRotors(List<Integer> rotorsIDInorder, List<Character> startingPositions) throws InvalidCharactersException, InvalidRotorException {
        checkSelectedRotors(rotorsIDInorder);
        this.selectedRotors = rotorsIDInorder;
        setStartingCharacters(startingPositions);
    }

    @Override
    public void setStartingCharacters(List<Character> startingCharacters) throws InvalidCharactersException {
        checkStartingCharacters(startingCharacters);
        this.selectedRotorsListRightToLeft.clear();
        this.selectedRotorsListLeftToRight.clear();
        this.startingCharacters = startingCharacters;
        disconnectAllRotors();

        for (int i = 0; i < selectedRotors.size(); i++) {
            rotors.get(selectedRotors.get(i)).setStartIndex(startingCharacters.get(i));
        }

        connectRotors();
        this.selectedRotors.forEach(rotorID -> this.selectedRotorsListLeftToRight.add(this.rotors.get(rotorID)));
        this.selectedRotorsListRightToLeft.addAll(this.selectedRotorsListLeftToRight);
        Collections.reverse(this.selectedRotorsListRightToLeft);
    }

    @Override
    // Added more details if a reflector doesn't exist in Enigma machine's XML file.
    public void setSelectedReflector(Reflector.ReflectorID selectedReflectorID) throws InvalidReflectorException {
        if (!reflectors.containsKey(selectedReflectorID)) {
            throw new InvalidReflectorException(String.format("Reflector '%s' not found.", selectedReflectorID.name()));
        }
        this.selectedReflector = reflectors.get(selectedReflectorID);
    }

    @Override
    public void setPlugBoard(List<Pair<Character, Character>> plugBoard) throws InvalidPlugBoardException {
        checkPlugBoard(plugBoard);
        this.plugBoard = new PlugBoardImpl(plugBoard);
    }

    @Override
    public void reset() {
        this.rotors.forEach((rotorID, rotor) -> rotor.resetRotor());
        try {
            setSelectedRotors(this.selectedRotors, this.startingCharacters);
        } catch (InvalidCharactersException | InvalidRotorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EngineDTO getEngineDTO() {
        return new EngineDTO(rotors.size(),
                plugBoard.getPairs(),
                selectedReflector == null ? "" : selectedReflector.getReflectorID().toString(),
                charsAtWindows(),
                getSelectedRotorsAndNotchesDistances(),
                messagesSentCounter);
    }

    @Override
    public ConfigurationDTO getMachineCode() {
        return new ConfigurationDTO(selectedRotors, startingCharacters, selectedReflector.getReflectorID(), plugBoard.getPairList(), machineABC);
    }

    private List<Character> charsAtWindows() {
        List<Character> charsAtWindows = new ArrayList<>();
        for (Rotor rotor : selectedRotorsListRightToLeft) {
            charsAtWindows.add(rotor.peekWindow());
        }

        return charsAtWindows;
    }

    @Override
    public void randomSelectedComponents() {
        Random random = new Random();
        int numberOfSelectedRotors = random.nextInt(this.rotors.size() - 1) + 2;
        this.selectedRotors = new ArrayList<>(createRandomSelectedRotors(numberOfSelectedRotors).subList(0, numberOfSelectedRotors));
        this.startingCharacters = new ArrayList<>(numberOfSelectedRotors);
        this.selectedRotors.forEach(rotorID -> this.startingCharacters.add(machineABCMap.get(machineABC.charAt(random.nextInt(machineABC.length())))));
        try {
            setStartingCharacters(this.startingCharacters);
        } catch (InvalidCharactersException e) {
            throw new RuntimeException(e);
        }

        this.selectedReflector = this.reflectors.get(Reflector.ReflectorID.values()[random.nextInt(this.reflectors.size())]);
        this.plugBoard = new PlugBoardImpl();
        List<Character> plugBoardPairs = new ArrayList<>(this.machineABCMap.keySet());
        Collections.shuffle(plugBoardPairs);
        int abcPairs = random.nextInt((machineABC.length() / 2) + 1);
        plugBoardPairs = new ArrayList<>(plugBoardPairs.subList(0, abcPairs * 2));
        for (int i = 0; i < plugBoardPairs.size(); i += 2) {
            this.plugBoard.addPair(plugBoardPairs.get(i), plugBoardPairs.get(i + 1));
        }

    }

    @Override
    public void setEngineConfiguration(ConfigurationDTO configurationDTO) throws InvalidCharactersException, InvalidRotorException, InvalidReflectorException, InvalidPlugBoardException {
        setSelectedRotors(configurationDTO.getRotorsIDInorder(), configurationDTO.getStartingPositions());
        setSelectedReflector(configurationDTO.getSelectedReflectorID());
        setPlugBoard(configurationDTO.getPlugBoard());
    }

    @Override
    public EnigmaEngine deepClone() { // Serializing
        try {
            EnigmaEngine clone;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            clone = (EnigmaEngine) objectInputStream.readObject();
            return clone;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Integer> createRandomSelectedRotors(int numberOfSelectedRotors) {
        List<Integer> randomSelectedRotors = new ArrayList<>(numberOfSelectedRotors);
        IntStream.rangeClosed(1, this.rotors.size()).forEach(randomSelectedRotors::add);
        Collections.shuffle(randomSelectedRotors);

        return randomSelectedRotors;
    }

    private int runRotorPipelineList(int index, Rotor.Direction dir) {
        int outputIndex = index;
        if (dir == Rotor.Direction.LEFT) {
            for (Rotor rotor : selectedRotorsListRightToLeft) {
                outputIndex = rotor.getOutputIndex(outputIndex, dir);
            }
        } else {
            for (Rotor rotor : selectedRotorsListLeftToRight) {
                outputIndex = rotor.getOutputIndex(outputIndex, dir);
            }
        }

        return outputIndex;
    }


    private void connectRotors() {
        disconnectAllRotors();
        rotors.get(selectedRotors.get(0)).setRotateNextRotor(null);

        for (int i = selectedRotors.size() - 1; i > 0; i--) {
            rotors.get(selectedRotors.get(i)).setRotateNextRotor(rotors.get(selectedRotors.get(i - 1)));
        }
    }

    private void disconnectAllRotors() {
        this.rotors.keySet().forEach(key -> this.rotors.get(key).setRotateNextRotor(null));
    }

    private void checkStartingCharacters(List<Character> startingCharacters) throws InvalidCharactersException {
        if (startingCharacters.size() != this.selectedRotors.size()) {
            throw new InvalidCharactersException("Starting characters must be the same size as the number of selected rotors.");
        }
        if (startingCharacters.stream().anyMatch(c -> !this.machineABCMap.containsKey(c))) {
            throw new InvalidCharactersException("Starting characters must be valid.");
        }
    }

    private void checkSelectedRotors(List<Integer> rotorsIDs) throws InvalidRotorException {
        if (rotorsIDs.size() > rotors.size()) {
            throw new InvalidRotorException("Too many rotors have been selected.");
        }
        if (rotorsIDs.stream().anyMatch(rotorID -> !rotors.containsKey(rotorID))) {
            throw new InvalidRotorException("An invalid rotor is selected.");
        }
        if (new HashSet<>(rotorsIDs).size() != rotorsIDs.size()) {
            throw new InvalidRotorException("Duplicate rotors selected.");
        }
    }

    private List<Pair<Integer, Integer>> getSelectedRotorsAndNotchesDistances() {
        List<Pair<Integer, Integer>> notchPositionsByOrder = new ArrayList<>();
        for (Integer selectedRotorIdx : this.selectedRotors) {
            notchPositionsByOrder.add(new Pair<>(selectedRotorIdx, this.rotors.get(selectedRotorIdx).getNotchIndex()));
        }

        return notchPositionsByOrder;
    }

    private void checkPlugBoard(List<Pair<Character, Character>> plugBoard) throws InvalidPlugBoardException {
        this.plugBoard = new PlugBoardImpl();

        for (Pair<Character, Character> pair : plugBoard) {
            if (!machineABCMap.containsKey(pair.getKey()) || !machineABCMap.containsKey(pair.getValue()) || this.plugBoard.containsPair(pair)) {
                throw new InvalidPlugBoardException("Plug board contains invalid characters.");
            }

            this.plugBoard.addPair(pair.getKey(), pair.getValue());
        }
    }

    private List<Character> stringToList(String input) {
        List<Character> output = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            output.add(input.charAt(i));
        }

        return output;
    }

    public WordsDictionary getWordsDictionary() {
        return wordsDictionary;
    }

    @Override
    public void setWordsDictionary(WordsDictionary wordsDictionary) {
        this.wordsDictionary = wordsDictionary;
    }
}