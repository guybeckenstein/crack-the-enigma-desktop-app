package enigmaEngine.interfaces;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;

public interface PlugBoard  {
    char returnCharacterPair(char character);
    void addPair(char a, char b);
    HashMap<Character, Character> getPairs();
    boolean containsPair(Pair<Character, Character> pair);
    List<Pair<Character,Character>> getPairList();
    PlugBoard clonePlugBoard();

}