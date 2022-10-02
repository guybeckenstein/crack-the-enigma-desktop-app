package enigmaEngine.interfaces;

import dto.ConfigurationDTO;
import decryptionManager.WordsDictionary;
import enigmaEngine.exceptions.InvalidCharactersException;
import enigmaEngine.exceptions.InvalidPlugBoardException;
import enigmaEngine.exceptions.InvalidReflectorException;
import enigmaEngine.exceptions.InvalidRotorException;
import dto.EngineDTO;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public interface EnigmaEngine extends Serializable {
    HashMap<Integer, Rotor> getRotors();

    int getABCSize();

    char activate(char input);

    String processMessage(String input) throws InvalidCharactersException;

    void setSelectedRotors(List<Integer> rotorsIDInorder, List<Character> startingPositions) throws InvalidCharactersException, InvalidRotorException;

    void setStartingCharacters(List<Character> startingCharacters) throws InvalidCharactersException;

    void setSelectedReflector(Reflector.ReflectorID selectedReflectorID) throws InvalidReflectorException;

    void setPlugBoard(List<Pair<Character,Character>> plugBoard) throws InvalidPlugBoardException;

    void reset();

    EngineDTO getEngineDTO();

    ConfigurationDTO getMachineCode();

    void randomSelectedComponents();

    void setEngineConfiguration(ConfigurationDTO configurationDTO) throws InvalidCharactersException, InvalidRotorException, InvalidReflectorException, InvalidPlugBoardException;

    WordsDictionary getWordsDictionary();
    void setWordsDictionary(WordsDictionary wordsDictionary);

    EnigmaEngine deepClone();
}