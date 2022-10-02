package ui;

import enigmaEngine.exceptions.InvalidPlugBoardException;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

// Made this class final
// All it's functions are now static
// It has no constructors
// Another static class we use in Java is NIO.Files
final public class CodeGeneratorModel {

    private CodeGeneratorModel() {

    }
    public static ArrayList<Integer> createSelectedRotorsList(String selectedRotors) throws NumberFormatException {
        String[] stringRotors = selectedRotors.split(",");
        ArrayList<Integer> intRotors = Arrays.stream(stringRotors)
                .mapToInt(Integer::parseInt).boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        if (intRotors.size() == 1) {
            throw new InputMismatchException("The given input contains one rotor. It needs to contain at least 2.");
        }
        else if (intRotors.size() == 0) {
            throw new InputMismatchException("The given input contains zero rotors. It needs to contain at least 2.");
        }

        return intRotors;
    }

    public static List<Character> createStartingCharactersList(String startingCharacters) {
        return startingCharacters.toUpperCase().chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
    }

    public static List<Pair<Character, Character>> createPlugBoard(String abcString) throws InvalidPlugBoardException {
        char[] abcArr = abcString.toUpperCase().toCharArray();
        List<Pair<Character, Character>> abcPairs = new ArrayList<>();

        if (abcArr.length % 2 == 1) {
            throw new InvalidPlugBoardException("Plug board must have even number of pairs.");
        }
        if (getUniqueCharacters(abcArr) != abcArr.length) {
            throw new InvalidPlugBoardException("Plug board must have unique characters.");
        }
        for (int i = 0; i < abcArr.length; i += 2) {
            abcPairs.add(new Pair<>(abcArr[i], abcArr[i + 1]));
        }

        return abcPairs;
    }

    private static int getUniqueCharacters(char[] abcArr) {
        HashSet<Character> abcSet = new HashSet<>();
        for (char c : abcArr) {
            abcSet.add(c);
        }

        return abcSet.size();
    }


}