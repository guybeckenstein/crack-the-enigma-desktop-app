package ui.interfaces;

import dto.XmlDTO;
import enigmaEngine.exceptions.*;
import ui.historyAndStatistics.MachineHistoryAndStatistics;
import enigmaEngine.interfaces.EnigmaEngine;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Set;

public interface Input {

    String getCurrentMachineStateAsString();
    XmlDTO getXmlDTO();
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
}