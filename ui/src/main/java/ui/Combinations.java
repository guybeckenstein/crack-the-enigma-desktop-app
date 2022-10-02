package ui;

import java.util.ArrayList;
import java.util.List;

public final class Combinations {
    private Combinations() { }
    public static long generateAllCombinationsReturnNumber(int n, int r) {
        long totalCombinations = 0;
        List<Integer> combination = new ArrayList<>();

        // initialize with lowest lexicographic combination
        for (int i = 0; i < r; i++) {
            combination.add(i);
        }

        while (combination.get(r - 1) < n) {
            totalCombinations++;
            extractedMethodCombination(n, r, combination);
        }

        return totalCombinations;
    }

    private static void extractedMethodCombination(int n, int r, List<Integer> combination) {
        // generates next combination in lexicographic order
        int t = r - 1;
        while (t != 0 && combination.get(t) == n - r + t) {
            t--;
        }
        combination.set(t, combination.get(t) + 1);
        for (int i = t + 1; i < r; i++) {
            combination.set(i, combination.get(i - 1) + 1);
        }
    }

    public static List<List<Integer>> generateAllCombinations(int n, int r) {
        List<List<Integer>> combinations = new ArrayList<>();
        List<Integer> combination = new ArrayList<>();

        // initialize with lowest lexicographic combination
        for (int i = 0; i < r; i++) {
            combination.add(i);
        }

        while (combination.get(r - 1) < n) {
            combinations.add(getClone(combination));

            // generates next combination in lexicographic order
            extractedMethodCombination(n, r, combination);
        }
        combinations.forEach(Combinations::raiseByOne);

        return combinations;
    }

    private static List<Integer> getClone(List<Integer> combination) {
        List<Integer> newCombination = new ArrayList<>();
        for (Integer element : combination) {
            newCombination.add(new Integer(element));
        }
        return newCombination;
    }

    private static void raiseByOne(List<Integer> combination) {
        for (int i = 0; i < combination.size(); i++) {
            combination.set(i, combination.get(i) + 1);
        }
    }
}
