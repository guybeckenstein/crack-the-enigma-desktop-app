package enigmaEngine;

import enigmaEngine.exceptions.InvalidABCException;
import enigmaEngine.exceptions.InvalidReflectorException;
import enigmaEngine.exceptions.InvalidRotorException;
import enigmaEngine.impl.ReflectorImpl;
import enigmaEngine.impl.RotorImpl;
import enigmaEngine.interfaces.CreateAndValidateEnigmaComponents;
import enigmaEngine.interfaces.Reflector;
import enigmaEngine.interfaces.Rotor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CreateAndValidateEnigmaComponentsImpl implements CreateAndValidateEnigmaComponents {

    private final String abc;
    private final HashMap<Character, Character> abcMap;

    private int currentRotorID, currentRotorNotch;

    public CreateAndValidateEnigmaComponentsImpl(String abc) {
        this.abc = abc.toUpperCase();
        this.abcMap = new HashMap<>();
    }

    /*
     * Creations
     * */
    @Override
    public Rotor createRotor(int id, int notch, List<Character> rightSide, List<Character> leftSide) throws InvalidRotorException {
        currentRotorID = id;
        currentRotorNotch = notch;
        validateRotor(rightSide, leftSide);
        return new RotorImpl(currentRotorID, currentRotorNotch, rightSide, leftSide);
    }

    @Override
    public Reflector createReflector(List<Integer> input, List<Integer> output, Reflector.ReflectorID id) throws InvalidReflectorException {
        for (int i = 0; i < input.size(); i++) {
            input.set(i, input.get(i) - 1);
            output.set(i, output.get(i) - 1);
        }

        validateReflector(input, output);

        return new ReflectorImpl(input, output, id);
    }

    @Override
    public void validateRotorsIDs(Map<Integer, Rotor> rotorMap) throws InvalidRotorException {
        if (rotorMap.keySet().stream().anyMatch(i -> i < 1 || i > rotorMap.size())) {
            throw new InvalidRotorException("All rotor IDs must be sequential, from 1 to " + rotorMap.size() + ".");
        }
    }

    @Override
    public void validateReflectorsIDs(Map<Reflector.ReflectorID, Reflector> reflectorsMap) throws InvalidReflectorException {
        if (reflectorsMap.keySet().stream().anyMatch(id -> id.ordinal() >= reflectorsMap.size())) {
            throw new InvalidReflectorException("All reflector IDs must be sequential, from 1 to " + reflectorsMap.size() + ".");
        }
    }

    /*
     * Validations
     * */
    @Override
    public void ValidateABC(String abc) throws InvalidABCException {
        if (abc.length() < 1) {
            throw new InvalidABCException("There machine does not contain ABC letters.");
        }
        else if (abc.length() % 2 == 1) {
            throw new InvalidABCException("The size of machine's ABC language must be an even number.");
        }

        for (int i = 0; i < abc.length(); i++) {
            if (this.abcMap.containsKey(abc.charAt(i))) {
                throw new InvalidABCException("File contains a duplicate letter in machine's ABC.");
            }

            this.abcMap.put(abc.charAt(i), abc.charAt(i));
        }
    }

    private void validateRotor(List<Character> rightSide, List<Character> leftSide) throws InvalidRotorException {
        validateRotorId();
        validateRotorNotch();
        validateRotorSides(rightSide, leftSide);
    }

    private void validateRotorSides(List<Character> rightSide, List<Character> leftSide) throws InvalidRotorException {
        validateRotorSide(rightSide, "Right");
        validateRotorSide(leftSide, "Left");
    }

    private void validateRotorSide(List<Character> side, String sideName) throws InvalidRotorException {
        if (new HashSet<>(side).size() != abc.length() || side.size() != abc.length()) {
            throw new InvalidRotorException("Rotor #" + currentRotorID + " - " + sideName + " side" + ": all characters must be unique.");
        }

        for (Character character : side) {
            if (!this.abcMap.containsKey(character)) {
                throw new InvalidRotorException("Rotor #" + currentRotorID + " - " + sideName + " side" + ": all characters must be in machine's ABC.");
            }
        }
    }

    private void validateRotorNotch() throws InvalidRotorException {
        if (currentRotorNotch < 0 || currentRotorNotch >= abc.length()) {
            throw new InvalidRotorException("Rotor #" + currentRotorID + " notch must appear in machine's ABC.");
        }
    }

    private void validateRotorId() throws InvalidRotorException {
        if (currentRotorID < 0) {
            throw new InvalidRotorException("Rotor ID must be natural number, but " + currentRotorID + " was given.");
        }
    }

    private void validateReflector(List<Integer> input, List<Integer> output) throws InvalidReflectorException {
        if (input.size() != output.size()) {
            throw new InvalidReflectorException("Input and output must have the same size.");
        }

        HashSet<Integer> inputSet = new HashSet<>(input);
        HashSet<Integer> outputSet = new HashSet<>(output);

        if (!inputSet.isEmpty() && !outputSet.isEmpty() && inputSet.stream().anyMatch(outputSet::contains)) {
            throw new InvalidReflectorException("The input and the output must be disjoint.");
        }

        inputSet.addAll(outputSet);
        if (inputSet.size() != abc.length()) {
            throw new InvalidReflectorException("Both input and output must be have same length as machine's ABC.");
        }

        if (inputSet.stream().anyMatch(i -> i < 0 || i >= abc.length())) {
            throw new InvalidReflectorException("Both input and output must be sequential, from 1 to " + abc.length() + ".");
        }
    }
}