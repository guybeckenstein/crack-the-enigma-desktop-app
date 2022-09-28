package ui.interfaces;

import java.util.List;

public interface TrieInterface {
    void insert(String key);
    List<String> getWordsWithPrefix(String prefix);
}
