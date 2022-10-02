package ui.impl.models;

import dto.XmlDTO;
import ui.CodeGeneratorModel;
import dto.EngineDTO;
import enigmaEngine.exceptions.*;
import ui.historyAndStatistics.MachineCodeData;
import ui.historyAndStatistics.MachineHistoryAndStatistics;
import ui.interfaces.Input;
import enigmaEngine.InitializeEnigmaEngineComponents;
import enigmaEngine.interfaces.EnigmaEngine;
import enigmaEngine.interfaces.Reflector;
import javafx.util.Pair;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

public class ModelMain implements Input {
    private EnigmaEngine engine;
    private XmlDTO xmlDTO;
    private final MachineHistoryAndStatistics machineHistoryAndStatistics; // Screen 2 (machine encrypt / decrypt)

    public ModelMain() {
        this.machineHistoryAndStatistics = new MachineHistoryAndStatistics();
    }

    @Override
    public String getCurrentMachineStateAsString() {
        EngineDTO DTO = engine.getEngineDTO();
        return currentMachineState(DTO).toString();
    }

    @Override
    public XmlDTO getXmlDTO() {
        return xmlDTO;
    }

    @Override
    public int getMessageCounter() {
        EngineDTO DTO = engine.getEngineDTO();
        return DTO.getMessagesSentCounter();
    }

    @Override
    public MachineHistoryAndStatistics getMachineHistoryStates() {
        return machineHistoryAndStatistics;
    }

    @Override
    public EnigmaEngine getEngine() {
        return this.engine;
    }

    @Override
    public void readMachineFromXMLFile(String path) throws InvalidMachineException, JAXBException, InvalidRotorException, IOException, InvalidABCException, UnknownSourceException, InvalidReflectorException, InvalidDecipherException, InvalidAgentsAmountException {
        InitializeEnigmaEngineComponents initializeEnigmaEngineComponents = new InitializeEnigmaEngineComponents();
        this.engine = initializeEnigmaEngineComponents.initializeEngine(InitializeEnigmaEngineComponents.SourceMode.XML, path);
        this.xmlDTO = initializeEnigmaEngineComponents.initializeBriefXML(path, this.engine);
    }

    // Function changed.
    // Machine engine code output previously was: <1(0),2(1),...,n(n-1)><A,...,A><I><A|B,C|D,...,(N-1)|N>
    // I changed it to: <1,2,...,n><A(0),...,A(n-1)><I><A|B,C|D,...,(N-1)|N>
    private StringBuilder currentMachineState(EngineDTO DTO) {
        List<Pair<Integer, Integer>> selectedRotorsAndNotchesPosition = DTO.getSelectedRotorsAndNotchesPosition();
        List<Character> selectedRotorsPositions = DTO.getCurrentSelectedRotorsPositions();
        int size = selectedRotorsAndNotchesPosition.size();
        StringBuilder rotorsSBPart1 = new StringBuilder();
        StringBuilder rotorsSBPart2 = new StringBuilder();
        StringBuilder finalSB = new StringBuilder();
        rotorsSBPart1.append("<");
        rotorsSBPart2.append("<");
        for (int i = 0; i < size; i++) {
            rotorsSBPart1.append(selectedRotorsAndNotchesPosition.get(i).getKey()).append(','); // Eventually it becomes <1,2,...,n,
            rotorsSBPart2.append(selectedRotorsPositions.get(i))
                    .append('(').append(selectedRotorsAndNotchesPosition.get(i).getValue()).append("),"); // Eventually it becomes <A(1),...,A(n),
        }

        rotorsSBPart1.deleteCharAt(rotorsSBPart1.length() - 1).append(">"); // Eventually it becomes <1,2,...,n>
        rotorsSBPart2.deleteCharAt(rotorsSBPart2.length() - 1).append("><"); // Eventually it becomes <A(1),...,A(n)><
        finalSB.append(rotorsSBPart1).append(rotorsSBPart2)
                .append(DTO.getSelectedReflector()).append(">"); // Eventually it becomes <1,2,...,n><A(1),...,A(n)><I>

        if (!DTO.getPlugBoard().isEmpty()) {
            StringBuilder plugBoardSB = createPlugBoardPairsStringBuilder(DTO.getPlugBoard());
            finalSB.append(plugBoardSB);
        } // Eventually it becomes <1,2,...,n><A(1),...,A(n)><I><A|B,C|D,...,(N-1)|N>

        return finalSB;
    }

    private StringBuilder createPlugBoardPairsStringBuilder(Map<Character, Character> enginePlugBoard) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        List<Map.Entry<Character, Character>> plugBoard = new ArrayList<>(new HashMap<>(enginePlugBoard).entrySet());
        Set<Pair<Character, Character>> plugBoardSet = new HashSet<>();

        for (Map.Entry<Character, Character> entry : plugBoard) {
            Pair<Character, Character> pair = new Pair<>(entry.getValue(), entry.getKey());
            if (!plugBoardSet.contains(pair)) {
                plugBoardSet.add(new Pair<>(entry.getKey(), entry.getValue()));
            }
        }

        plugBoardSet.forEach(entry -> sb.append(entry.getKey()).append("|").append(entry.getValue()).append(","));
        sb.deleteCharAt(sb.length() - 1).append(">");

        return sb;
    }

    @Override
    // Changed method. Quitting is now case-insensitive +  I added a call to 'addEnigmaCode' and this adds the new Enigma code.
    public void initializeEnigmaCodeManually(String rotors, String startingPositions, String plugBoardPairs, String reflectorID) throws InputMismatchException, IllegalArgumentException, InvalidRotorException, InvalidReflectorException, InvalidPlugBoardException, InvalidCharactersException {
        List<Integer> selectedRotorsDeque;
        resetMachine();
        selectedRotorsDeque = getRotorsFromUserInput(rotors);
        getStartingPositionsFromUserInput(selectedRotorsDeque, startingPositions);
        getReflectorFromUserInput(reflectorID);
        getPlugBoardPairsFromUserInput(plugBoardPairs);

        addEnigmaCode("Manually");
    }

    // Added check case. For instance: now "2,2" or "0, 2" raises exception. Also added more information if a user inserts an invalid rotor ID.
    private List<Integer> getRotorsFromUserInput(String rotors) throws InvalidRotorException {
        System.out.println("Enter your desired rotor IDs starting from 1, separated by a comma without spaces.");
        final List<Integer> selectedRotorsDeque = CodeGeneratorModel.createSelectedRotorsList(rotors);
        if (new HashSet<>(selectedRotorsDeque).size() < selectedRotorsDeque.size()) {
            throw new InvalidRotorException("A rotor ID was inserted several times. Please insert only unique values.");
        }
        Integer invalidRotorID = selectedRotorsDeque
                .stream().filter(rotorID -> rotorID > selectedRotorsDeque.size() || rotorID < 1)
                .findFirst().orElse(null); // Now we can track the wrong input instead of just getting an exception.

        if (invalidRotorID != null) {
            throw new InvalidRotorException(String.format("Invalid rotor ID was selected - '%d'. Please insert an ID from 1 to %d.",
                    invalidRotorID, this.engine.getRotors().size()));
        }
        return selectedRotorsDeque;
    }

    private void getStartingPositionsFromUserInput(List<Integer> selectedRotorsDeque, String startingPositions) throws InvalidCharactersException, InvalidRotorException {
        System.out.println("Enter all your desired rotors starting positions without separation between them.");
        this.engine.setSelectedRotors(selectedRotorsDeque, CodeGeneratorModel.createStartingCharactersList(startingPositions));
    }

    private void getReflectorFromUserInput(String reflectorID) throws InvalidReflectorException {
        int reflectorNumber;
        System.out.println("Enter your desired reflector ID. Please enter the number using one of these numerals:\n"
                + "1. For reflector I\n" + "2. For reflector II\n" + "3. For reflector III\n" + "4. For reflector IV\n" + "5. For reflector V\n");
        System.out.println("If you are willing to go back to main menu, type '-1'.");
        reflectorNumber = Reflector.ReflectorID.valueOf(reflectorID).ordinal() + 1;
        if (reflectorNumber < 1 || reflectorNumber > 5) {
            throw new InvalidReflectorException(reflectorNumber + " is an invalid reflector ID.");
        }
        this.engine.setSelectedReflector(Reflector.ReflectorID.values()[reflectorNumber - 1]);
    }

    private void getPlugBoardPairsFromUserInput(String plugBoardPairs) throws InvalidPlugBoardException {
        System.out.println("Enter all your desired plug board pairs without separation between them.");
        this.engine.setPlugBoard(CodeGeneratorModel.createPlugBoard(plugBoardPairs));
    }

    // Function changed. I added a call to 'addEnigmaCode' and this adds the new Enigma code.
    @Override
    public void initializeEnigmaCodeAutomatically() {
        this.engine.randomSelectedComponents();
        addEnigmaCode("Automatically");
    }

    // Created this two-lines method for each time user creates a new Enigma engine code.
    private void addEnigmaCode(String message) {
        machineHistoryAndStatistics.add(new MachineCodeData(currentMachineState(this.engine.getEngineDTO()).toString()));
        System.out.println(message + " initialized code: " + machineHistoryAndStatistics.getCurrentMachineCode());
    }

    @Override
    public String getMessageAndProcessIt(String messageInput, boolean bool) throws InvalidCharactersException {
        int timeStart, timeEnd;
        String messageOutput;
        if (bool) {
            System.out.println("Enter your message to process.");
        }
        timeStart = (int) System.nanoTime();
        messageOutput = this.engine.processMessage(messageInput);
        timeEnd = (int) System.nanoTime();

        if (bool) {
            machineHistoryAndStatistics.addActivateDataToCurrentMachineCode(messageInput, messageOutput, timeEnd - timeStart);
        }
        return messageOutput;
    }

    @Override
    // Reset last
    public void resetMachine() {
        if (machineHistoryAndStatistics.isEmpty()) {
            System.out.println("This is the first Enigma code insertion.");
            return;
        }
        this.engine.reset();

        System.out.println("Machine successfully reset.");
    }

    @Override
    public String getMachineStatisticsAndHistory() {
        return this.machineHistoryAndStatistics.toString();
    }

    @Override
    public Set<String> getWordsDictionary() {
        return engine.getWordsDictionary().getWords();
    }
}