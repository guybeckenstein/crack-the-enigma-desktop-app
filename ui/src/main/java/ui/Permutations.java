package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Class for finding all permutation, using some algorithm
public final class Permutations {
    private Permutations() { }

    public static List<List<Integer>> getAllPermutationsIterative(List<Integer> source, List<List<Integer>> destination) {
        List<Integer> indexes = new ArrayList(source.size());
        for (int i = 0; i < source.size(); i++) {
            indexes.add(0);
        }

        addNewPermutation(source, destination);

        int i = 0;
        while (i < source.size()) {
            if (indexes.get(i) < i) {
                Collections.swap(source, i % 2 == 0 ?  0: indexes.get(i), i);
                addNewPermutation(source, destination);
                indexes.set(i, indexes.get(i) + 1);
                i = 0;
            }
            else {
                indexes.set(i, 0);
                i++;
            }
        }

        return destination;
    }
    private static void addNewPermutation(List<Integer> source, List<List<Integer>> destination) {
        List<Integer> newPermutation = new ArrayList<>(source.size());
        for (Integer element : source) {
            newPermutation.add(new Integer(element));
        }
        destination.add(newPermutation);
    }
}
