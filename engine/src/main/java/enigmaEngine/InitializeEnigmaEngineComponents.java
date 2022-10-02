package enigmaEngine;

import dto.XmlDTO;
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

        if (source == SourceMode.XML) {
            enigmaEngineInitializer = new InitializeEnigmaFromXML();
        } else {
            throw new UnknownSourceException("Unknown file extension source is given.");
        }
        return enigmaEngineInitializer.getEnigmaEngineFromSource(path);
    }
    public XmlDTO initializeBriefXML(String path, EnigmaEngine newEnigmaEngine) throws InvalidAgentsAmountException, JAXBException, InvalidDecipherException, IOException {
        return enigmaEngineInitializer.getBriefXMLFromSource(path, newEnigmaEngine);
    }
}