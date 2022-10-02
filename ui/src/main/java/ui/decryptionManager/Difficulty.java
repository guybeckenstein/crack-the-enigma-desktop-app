package ui.decryptionManager;

import dto.EngineDTO;
import ui.Combinations;

public enum Difficulty {
    EASY,
    MEDIUM,
    HARD,
    IMPOSSIBLE;

    public static long translateDifficultyLevelToMissions(Difficulty difficulty, EngineDTO engineDTO, int numTotalReflectors, int ABCSize) {
        switch (difficulty.toString()) {
            case "EASY":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size());
            case "MEDIUM":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * numTotalReflectors;
            case "HARD":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * numTotalReflectors
                        * factorial(engineDTO.getSelectedRotors().size());
            case "IMPOSSIBLE":
                return (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * numTotalReflectors
                        * factorial(engineDTO.getSelectedRotors().size()) *
                        Combinations.generateAllCombinationsReturnNumber(engineDTO.getTotalNumberOfRotors(), engineDTO.getSelectedRotors().size());
            default:
                return 0;
        }

    }

    public static int factorial(int size) {
        int result = 1;
        for (int i = 1; i <= size; i++) {
            result *= i;
        }
        return result;
    }
}