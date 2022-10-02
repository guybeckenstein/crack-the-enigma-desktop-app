package decryptionManager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordsDictionary implements Serializable {
    private final Set<String> words;

    public WordsDictionary(String dictionaryWords, String excludedCharacters) {
        Set<String> tmpWords = new HashSet<>(Arrays.asList(dictionaryWords.toUpperCase().split(" ")));
        words = new HashSet<>();
        for (String singleWord : tmpWords) {
            words.add(replaceAll(singleWord, excludedCharacters));
        }
    }

    private String replaceAll(String str, String excluded) { // Better than String.replaceAll(...)
        StringBuilder result = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (excluded.indexOf(ch) == -1) {
                result.append(ch);
            }
        }
        return result.toString().trim();
    }

    public Set<String> getWords() {
        return words;
    }

    public boolean isConfigurationForCandidacy(List<String> outputWords) {
        boolean condition1 = this.getWords().containsAll(outputWords);
        boolean condition2 = outputWords.size() > 0;
        return condition1 && condition2;
    }
}
