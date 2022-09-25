package enigmaEngine.interfaces;

import dto.xmlDTO;
import enigmaEngine.exceptions.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface InitializeEnigma {
    EnigmaEngine getEnigmaEngineFromSource(String source) throws InvalidABCException, InvalidReflectorException, InvalidRotorException, IOException, JAXBException, InvalidMachineException, InvalidDecipherException, InvalidAgentsAmountException;
    xmlDTO getBriefXMLFromSource(String path, EnigmaEngine newEnigmaEngine) throws JAXBException, IOException, InvalidDecipherException, InvalidAgentsAmountException;

}