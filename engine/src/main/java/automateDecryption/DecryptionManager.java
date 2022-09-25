package automateDecryption;

import enigmaEngine.MachineCode;
import enigmaEngine.interfaces.EnigmaEngine;
import enigmaEngine.interfaces.Reflector;
import dto.EngineDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class DecryptionManager implements Runnable {

    private final EnigmaEngine enigmaEngine;
    private long totalMissions, currentProgress;
    private final BlockingQueue<MachineCode> queue;
    //    private final BlockingQueue<Runnable> taskPoolQueue;
    private final Difficulty difficulty;
    private final String encryptedText;
    private MachineCode machineCode;
//    private final ExecutorService executor;

    private final long taskSize;
    private final EngineDTO engineDTO;

    public DecryptionManager(EnigmaEngine enigmaEngine, BlockingQueue<MachineCode> MachineCodeDTOQueue, Difficulty difficulty, String encryptedText, long taskSize) {
        this.enigmaEngine = enigmaEngine;
        this.queue = MachineCodeDTOQueue;
//        this.taskPoolQueue = taskPoolQueue;
        this.difficulty = difficulty;
        this.encryptedText = encryptedText;
//        this.executor = executor;
        this.taskSize = taskSize;
        currentProgress = 0;
        this.engineDTO = enigmaEngine.getEngineDTO();

        calculateMissionsNumber();
    }

    private void calculateMissionsNumber() {
        int ABCSize = enigmaEngine.getABCSize();
        switch (difficulty) {
            case EASY:
                totalMissions = (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size());
                break;
            case MEDIUM:
                totalMissions = (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * engineDTO.getTotalReflectors();
                break;
            case HARD:
                totalMissions = (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * engineDTO.getTotalReflectors() * factorial(engineDTO.getSelectedRotors().size());
                break;
            case IMPOSSIBLE:
                totalMissions = (long) Math.pow(ABCSize, engineDTO.getSelectedRotors().size()) * engineDTO.getTotalReflectors()
                        * factorial(engineDTO.getSelectedRotors().size()
                        * binomial(engineDTO.getSelectedRotors().size(), engineDTO.getTotalNumberOfRotors()));
                break;
            default:
        }
    }

    private long factorial(int size) {
        long result = 1;
        for (int i = 1; i <= size; i++) {
            result *= i;
        }
        return result;
    }

    private int binomial(int n, int k)
    {

        // Base Cases
        if (k > n)
            return 0;
        if (k == 0 || k == n)
            return 1;

        // Recur
        return binomial(n - 1, k - 1)
                + binomial(n - 1, k);
    }

    public void initializeMachineCode() {
        MachineCode tempMachineCode = this.enigmaEngine.getMachineCode();
        switch (difficulty) {
            case EASY:
                machineCode = new MachineCode(tempMachineCode.getRotorsIDInorder(), setStartingPositions(tempMachineCode.getStartingPositions()), tempMachineCode.getSelectedReflectorID(), tempMachineCode.getPlugBoard(), enigmaEngine.getABC());
                break;
            case MEDIUM:
                machineCode = new MachineCode(tempMachineCode.getRotorsIDInorder(), setStartingPositions(tempMachineCode.getStartingPositions()), tempMachineCode.getSelectedReflectorID(), tempMachineCode.getPlugBoard(), enigmaEngine.getABC());
                break;
            case HARD:
                machineCode = new MachineCode(tempMachineCode.getRotorsIDInorder(), setStartingPositions(tempMachineCode.getStartingPositions()), tempMachineCode.getSelectedReflectorID(), tempMachineCode.getPlugBoard(), enigmaEngine.getABC());
                break;
            case IMPOSSIBLE:
                machineCode = new MachineCode(tempMachineCode.getRotorsIDInorder(), setStartingPositions(tempMachineCode.getStartingPositions()), tempMachineCode.getSelectedReflectorID(), tempMachineCode.getPlugBoard(), enigmaEngine.getABC());
                break;
            default:
        }
        System.out.println("machineCode = " + machineCode);

    }

    private List<Character> setStartingPositions(List<Character> tempStartingPositions) {
        List<Character> res = new ArrayList<>();
        for (int i = 0; i < tempStartingPositions.size(); i++) {
            res.add(enigmaEngine.getABC().charAt(0));
        }

        return res;
    }
    
    @Override
    public void run() {
        while (currentProgress < totalMissions) { // (0 < totalMissions) {
            if (queue.offer(machineCode)) {
                // Makes overwriting on the thread pull with clones of machine codes.
                // TODO: replace with 'put' instead of 'offer' and adjust the code
                advanceMachineCode();
            }
        }
    }

    private void advanceMachineCode() {
        switch (difficulty) {
            case EASY:
                advanceMachineCodeEasy();
                break;
            case MEDIUM:
                advanceMachineCodeMedium();
                break;
            case HARD:
                advanceMachineCodeHard();
                break;
            case IMPOSSIBLE:
                advanceMachineCodeImpossible();
                break;
            default:
        }
    }

    private void advanceMachineCodeEasy() {
        synchronized (this) {
            for (int i = 0; i < taskSize; i++) {
                machineCode.increment();
                currentProgress++;
                // totalMissions--;
            }
        }
    }

    private void advanceMachineCodeMedium() {
        for (int i = 0; i < engineDTO.getTotalReflectors(); i++) {
            machineCode = new MachineCode(machineCode.getRotorsIDInorder(), machineCode.getStartingPositions(), Reflector.ReflectorID.values()[i], machineCode.getPlugBoard(), enigmaEngine.getABC());
            advanceMachineCodeEasy();
        }
    }

    private void advanceMachineCodeHard() {
        List<List<Integer>> permutations = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        for (int i = 1; i <= machineCode.getRotorsIDInorder().size(); i++) {
            temp.add(i);
        }
        permutations = permute(temp);

        for (int i = 0; i < permutations.size(); i++) {
            machineCode = new MachineCode(permutations.get(i), machineCode.getStartingPositions(), machineCode.getSelectedReflectorID(), machineCode.getPlugBoard(), enigmaEngine.getABC());
            advanceMachineCodeMedium();
        }
    }

    private void advanceMachineCodeImpossible() {
        List<List<Integer>> subsets = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        for (int i = 1; i <= engineDTO.getTotalNumberOfRotors(); i++) {
            temp.add(i);
        }
        subsets = getSubsets(temp, engineDTO.getSelectedRotors().size());
        for (List<Integer> subset : subsets) {
            machineCode = new MachineCode(subset, setStartingPositions(machineCode.getStartingPositions()), machineCode.getSelectedReflectorID(), machineCode.getPlugBoard(), enigmaEngine.getABC());
            advanceMachineCodeHard();
        }
    }

    private List<List<Integer>> getSubsets(List<Integer> temp, int size) {
        List<List<Integer>> res = new ArrayList<>();
        if (size == 0) {
            res.add(new ArrayList<>());
            return res;
        }
        if (temp.isEmpty()) {
            return res;
        }
        int first = temp.get(0);
        List<Integer> rest = temp.subList(1, temp.size());
        List<List<Integer>> subsetsWithoutFirst = getSubsets(rest, size);
        List<List<Integer>> subsetsWithFirst = getSubsets(rest, size - 1);
        for (List<Integer> subset : subsetsWithFirst) {
            subset.add(first);
        }
        res.addAll(subsetsWithoutFirst);
        res.addAll(subsetsWithFirst);
        return res;
    }

    private List<List<Integer>> permute(List<Integer> temp) {
        List<List<Integer>> res = new ArrayList<>();
        if (temp.size() == 1) {
            res.add(temp);
            return res;
        }
        for (int i = 0; i < temp.size(); i++) {
            List<Integer> temp2 = new ArrayList<>(temp);
            temp2.remove(i);
            List<List<Integer>> temp3 = permute(temp2);
            for (List<Integer> integers : temp3) {
                integers.add(0, temp.get(i));
            }
            res.addAll(temp3);
        }
        return res;
    }
}