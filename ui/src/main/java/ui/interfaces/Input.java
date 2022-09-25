package ui.interfaces;

import automateDecryption.Difficulty;
import automateDecryption.DecryptionManagerTask;
import dto.xmlDTO;
import enigmaEngine.exceptions.*;
import ui.historyAndStatistics.MachineHistoryAndStatistics;
import enigmaEngine.interfaces.EnigmaEngine;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Set;

public interface Input {

    // Only full path is given, not just file

    String getCurrentMachineStateAsString();
    xmlDTO getXmlDTO();
    DecryptionManagerTask getBruteForceTaskManager();
    int getMessageCounter();
    MachineHistoryAndStatistics getMachineHistoryStates();
    EnigmaEngine getEngine();
    void readMachineFromXMLFile(String path) throws JAXBException, InvalidRotorException, IOException, InvalidABCException, UnknownSourceException, InvalidReflectorException, InvalidMachineException, InvalidDecipherException, InvalidAgentsAmountException;
    void initializeEnigmaCodeManually(String rotors, String startingPositions, String plugBoardPairs, String reflectorID) throws InvalidRotorException, InvalidReflectorException, InvalidPlugBoardException, InvalidCharactersException; // Changed to boolean. false - if player exits this option in the middle, true if he added all input
    void initializeEnigmaCodeAutomatically();
    String getMessageAndProcessIt(String messageInput, boolean bool) throws InvalidCharactersException;
    void resetMachine();
    String getMachineStatisticsAndHistory();
    Set<String> getWordsDictionary();
    void setDMProperties(int agents, int missionSize, Difficulty difficulty);
    void startResumeDM();
    void setEncryptedText(String text);
}