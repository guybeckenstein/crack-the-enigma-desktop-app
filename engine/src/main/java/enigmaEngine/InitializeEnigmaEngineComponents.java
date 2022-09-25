package enigmaEngine;

import dto.xmlDTO;
import enigmaEngine.exceptions.*;
import enigmaEngine.impl.InitializeEnigmaFromXML;
import enigmaEngine.interfaces.EnigmaEngine;
import enigmaEngine.interfaces.InitializeEnigma;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class InitializeEnigmaEngineComponents {
    public enum SourceMode{
        XML,
        JSON
    }

    InitializeEnigma enigmaEngineInitializer = null;

    public EnigmaEngine initializeEngine(SourceMode source, String path) throws InvalidRotorException, InvalidABCException, InvalidReflectorException, JAXBException, IOException, UnknownSourceException, InvalidMachineException, InvalidDecipherException, InvalidAgentsAmountException {

        switch (source) {
            case XML:
                enigmaEngineInitializer = new InitializeEnigmaFromXML();
                break;
            default:
                throw new UnknownSourceException("Unknown file extension source is given.");
        }
        return enigmaEngineInitializer.getEnigmaEngineFromSource(path);
    }
    public xmlDTO initializeBriefXML(SourceMode source, String path, EnigmaEngine newEnigmaEngine) throws UnknownSourceException, InvalidMachineException, InvalidAgentsAmountException, JAXBException, InvalidDecipherException, InvalidRotorException, IOException, InvalidABCException, InvalidReflectorException {
        return enigmaEngineInitializer.getBriefXMLFromSource(path, newEnigmaEngine);
    }
}