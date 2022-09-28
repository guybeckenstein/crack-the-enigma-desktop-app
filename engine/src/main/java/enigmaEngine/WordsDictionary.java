package enigmaEngine;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WordsDictionary implements Serializable {
    private final Set<String> words;
    private final Set<Character> excludedCharacters;

    public WordsDictionary(String dictionaryWords, String excludedCharacters) {
        dictionaryWords.replaceAll("\\n\\t", "");
        this.excludedCharacters = new HashSet<>(excludedCharacters.toUpperCase().chars().mapToObj(c -> (char) c).collect(Collectors.toList()));
        Set<String> tmpWords = new HashSet<>(Arrays.asList(dictionaryWords.toUpperCase().split(" ")));
        words = new HashSet<>();
        for (String singleWord : tmpWords) {
            words.add(myReplaceAll(singleWord, excludedCharacters));
        }
    }

    private String myReplaceAll(String str, String excluded) { // Better than String.replaceAll(...)
        String result = "";
        for (char ch : str.toCharArray()) {
            if (excluded.indexOf(ch) == -1) {
                result += ch;
            }
        }
        return result.trim();
    }

    public WordsDictionary(Set<String> words, Set<Character> excludedCharacters) {
        this.words = words.stream().map(String::toUpperCase).collect(Collectors.toSet());
        this.excludedCharacters = excludedCharacters.stream().map(Character::toUpperCase).collect(Collectors.toSet());
    }

    public Set<String> getWords() {
        return words;
    }

    public List<String> candidateWords(String processedText) {
        List<String> cleanedProcessedText = cleanText(processedText.toUpperCase());
        return words.stream().filter(cleanedProcessedText::contains).collect(Collectors.toList());
    }

    private List<String> cleanText(String processedText) {
        return Arrays.stream(processedText.split(" ")).
                map(word -> word.replaceAll(excludedCharacters.toString(), "")).
                collect(Collectors.toList());
    }

    public boolean isCandidateString(String processedText) {
        return words.containsAll(cleanText(processedText.toUpperCase()));
    }

    public WordsDictionary cloneWordsDictionary() {
        return new WordsDictionary(this.words, this.excludedCharacters);
    }
}
