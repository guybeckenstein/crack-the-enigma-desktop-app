package ui.impl;

import java.util.*;

// A class to store a Trie node
public class Trie
{
    private boolean isLeaf;
    private Map<Character, Trie> children;
    private List<String> allStrings;

    // Constructor
    public Trie()
    {
        isLeaf = false;
        children = new HashMap<>();
        allStrings = new ArrayList<>();
    }

    // Iterative function to insert a string into a Trie
    public void insert(String key)
    {

        // start from the root node
        Trie curr = this;

        // do for each character of the key
        for (char c: key.toCharArray())
        {
            // create a new node if the path doesn't exist
            curr.children.putIfAbsent(c, new Trie());

            // go to the next node
            curr = curr.children.get(c);
        }

        // mark the current node as a leaf
        curr.isLeaf = true;
    }

    public List<String> getWordsWithPrefix(String prefix) {
        Trie curr = this;
        for (char c : prefix.toCharArray()) {
            curr = curr.children.get(c);

            if (curr == null) {
                return null;
            }
        }

        allStrings = new ArrayList<>();
        recursiveMethod(curr, prefix);

        return allStrings;
    }

    private void recursiveMethod(Trie curr, String str) {
        if (curr.isLeaf) {
            allStrings.add(str);
        }
        for (Character c : curr.children.keySet()) {
            recursiveMethod(curr.children.get(c), str + c);
        }
    }
}