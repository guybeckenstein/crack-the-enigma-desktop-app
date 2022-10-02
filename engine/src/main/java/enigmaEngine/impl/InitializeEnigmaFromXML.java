package enigmaEngine.impl;

import enigmaEngine.CreateAndValidateEnigmaComponentsImpl;
import decryptionManager.WordsDictionary;
import dto.XmlDTO;
import enigmaEngine.exceptions.*;
import enigmaEngine.interfaces.*;
import enigmaEngine.schemaBinding.*;
import javafx.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class InitializeEnigmaFromXML implements InitializeEnigma {
    private CreateAndValidateEnigmaComponents createAndValidateEnigmaComponents;

    @Override
    public EnigmaEngine getEnigmaEngineFromSource(String path) throws IOException, JAXBException, RuntimeException, InvalidABCException, InvalidReflectorException, InvalidRotorException, InvalidMachineException {
        CTEEnigma xmlOutput = getSourceFromXML(path);
        return getEnigmaEngine(xmlOutput);
    }

    private static CTEEnigma getSourceFromXML(String path) throws IOException, JAXBException {
        CTEEnigma xmlOutput;

        if (!path.contains(".xml")) {
            throw new FileNotFoundException("File given is not of XML type.");
        }
        InputStream xmlFile = Files.newInputStream(Paths.get(path));
        JAXBContext jaxbContext = JAXBContext.newInstance("enigmaEngine.schemaBinding");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        xmlOutput = (CTEEnigma) jaxbUnmarshaller.unmarshal(xmlFile);

        assert xmlOutput != null;
        return xmlOutput;
    }


    @SuppressWarnings("unchecked")
    private EnigmaEngine getEnigmaEngine(CTEEnigma xmlOutput) throws RuntimeException, InvalidABCException, InvalidReflectorException, InvalidRotorException, InvalidMachineException {
        // Machine
        CTEMachine machine = xmlOutput.getCTEMachine();
        if (machine == null) {
            throw new InvalidMachineException("In the given XML, no machine is given.");
        }

        // Machine's ABC
        if (machine.getABC() == null) {
            throw new InvalidABCException("In the given XML, there is no ABC language.");
        }
        String cteMachineABC = machine.getABC().trim();
        createAndValidateEnigmaComponents = new CreateAndValidateEnigmaComponentsImpl(cteMachineABC);
        createAndValidateEnigmaComponents.ValidateABC(cteMachineABC);

        // Rotors
        int cteRotorsCount = machine.getRotorsCount();
        if (cteRotorsCount < 2) {
            throw new InvalidRotorException("In the given XML, rotors count is less than 2.");
        }
        CTERotors cteRotors = machine.getCTERotors();
        if (cteRotorsCount > cteRotors.getCTERotor().size()) {
            throw new InvalidRotorException("In the given XML, rotors count is larger than actual rotors.");
        }
        HashMap<Integer, Rotor> rotors = (HashMap<Integer, Rotor>)importCTERotors(cteRotors, new HashMap<>());
        createAndValidateEnigmaComponents.validateRotorsIDs(rotors);

        // Reflectors
        if (machine.getCTEReflectors() == null || machine.getCTEReflectors().getCTEReflector().size() < 1) {
            throw new InvalidReflectorException("In the given XML, there are no reflectors.");
        }
        List<CTEReflector> cteReflectors = new ArrayList<>(machine.getCTEReflectors().getCTEReflector());
        HashMap<Reflector.ReflectorID, Reflector> reflectors = (HashMap<Reflector.ReflectorID, Reflector>)importCTEReflectors(cteReflectors, new HashMap<>());
        createAndValidateEnigmaComponents.validateReflectorsIDs(reflectors);

        return new EnigmaEngineImpl(rotors, reflectors, new PlugBoardImpl(), cteMachineABC);
    }

    private HashMap<?, ?> importCTERotors(CTERotors cteRotors, HashMap<Object, Object> rotors) throws InvalidRotorException {
        for (CTERotor rotor : cteRotors.getCTERotor()) {
            int id = rotor.getId(), notch = rotor.getNotch() - 1;
            Pair<List<Character>, List<Character>> rightAndLeft = getCTERotorRightAndLeftPairs(rotor);

            if (rotors.containsKey(id)) {
                throw new InvalidRotorException("two rotors have the same ID.");
            } else
                rotors.put(id, createAndValidateEnigmaComponents.createRotor(id, notch, rightAndLeft.getKey(), rightAndLeft.getValue()));
        }
        return rotors;
    }

    private Pair<List<Character>, List<Character>> getCTERotorRightAndLeftPairs(CTERotor rotor) {
        List<Character> right = new ArrayList<>();
        List<Character> left = new ArrayList<>();
        for (CTEPositioning pair : rotor.getCTEPositioning()) {
            right.add(pair.getRight().charAt(0));
            left.add(pair.getLeft().charAt(0));
        }
        return new Pair<>(right, left);
    }

    private HashMap<?, ?> importCTEReflectors(List<CTEReflector> cteReflectors, HashMap<Object, Object> reflectors) throws InvalidReflectorException {
        for (CTEReflector reflector : cteReflectors) {
            Reflector.ReflectorID id = getCTEReflectorID(reflector.getId().toUpperCase());
            Pair<List<Integer>, List<Integer>> inputAndOutput = getCTEReflectorInputAndOutputPairs(reflector);

            if (reflectors.containsKey(id)) {
                throw new InvalidReflectorException("two reflectors have the same ID.");
            } else
                reflectors.put(id, createAndValidateEnigmaComponents.createReflector(inputAndOutput.getKey(), inputAndOutput.getValue(), id));
        }
        return reflectors;
    }

    private Reflector.ReflectorID getCTEReflectorID(String stringID) {
        try {
            return Reflector.ReflectorID.valueOf(stringID);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid ID for enum "
                    + Reflector.ReflectorID.class.getSimpleName()
                    + " of a given reflector: " + stringID
                    + ". Valid IDs are only: " + Arrays.toString(Reflector.ReflectorID.values()));
        }
    }
    private Pair<List<Integer>, List<Integer>> getCTEReflectorInputAndOutputPairs(CTEReflector reflector) {
        List<Integer> input = new ArrayList<>();
        List<Integer> output = new ArrayList<>();
        for (CTEReflect pair : reflector.getCTEReflect()) {
            if (pair.getInput() == pair.getOutput()) {
                throw new RuntimeException("The XML that is given contains a reflector that maps a letter to itself.");
            }

            input.add(pair.getInput());
            output.add(pair.getOutput());
        }
        return new Pair<>(input, output);
    }

    @Override
    public XmlDTO getBriefXMLFromSource(String path, EnigmaEngine newEnigmaEngine) throws JAXBException, IOException, InvalidDecipherException, InvalidAgentsAmountException {
        CTEEnigma xmlOutput = getSourceFromXML(path);

        CTEDecipher decipher = xmlOutput.getCTEDecipher();
        if (decipher == null) { // Decipher
            throw new InvalidDecipherException("In the given XML, no decipher is given.");
        } else if (decipher.getCTEDictionary() == null || decipher.getCTEDictionary().getWords() == null) { // Dictionary
            throw new InvalidDecipherException("In the given XML, no dictionary is given.");
        } else if (decipher.getAgents() < 2 || 50 < decipher.getAgents()) { // Agents
            throw new InvalidAgentsAmountException("In the given XML, the amount of agents is invalid.");
        }

        CTEDictionary dictionary = decipher.getCTEDictionary();
        newEnigmaEngine.setWordsDictionary(new WordsDictionary(dictionary.getWords(), dictionary.getExcludeChars()));


        // Ex1
        List<Integer> rotorsFromXML = xmlOutput.getCTEMachine().getCTERotors().getCTERotor()
                .stream().map(CTERotor::getId).collect(Collectors.toList());
        List<String> reflectorsFromXML = xmlOutput.getCTEMachine().getCTEReflectors().getCTEReflector()
                .stream().map(CTEReflector::getId).collect(Collectors.toList());
        List<Character> ABCFromXML = xmlOutput.getCTEMachine()
                .getABC().trim()
                .chars().mapToObj(e -> (char) e).collect(Collectors.toList());

        Collections.sort(rotorsFromXML);
        Collections.sort(reflectorsFromXML);
        Collections.sort(ABCFromXML);
        // Ex2
        String excludedCharacters = decipher.getCTEDictionary().getExcludeChars();
        String nonSeparatedDictionaryWordsWithExcluded = decipher.getCTEDictionary().getWords().trim();
        String nonSeparatedDictionaryWordsValid = nonSeparatedDictionaryWordsWithExcluded;
        for (int i = 0; i < excludedCharacters.length(); i++) {
            nonSeparatedDictionaryWordsValid = nonSeparatedDictionaryWordsWithExcluded.replace(excludedCharacters.substring(i, i + 1), "");
        }
        Set<String> dictionaryWords = new HashSet<>(Arrays.asList(nonSeparatedDictionaryWordsValid.split(" ")));


        return new XmlDTO(rotorsFromXML, reflectorsFromXML, ABCFromXML, dictionaryWords, excludedCharacters, decipher.getAgents());
    }
}