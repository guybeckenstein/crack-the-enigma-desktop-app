package ui.decryptionManager;

import enigmaEngine.MachineCode;
import enigmaEngine.interfaces.EnigmaEngine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.controllers.BruteForceController;

import java.text.DecimalFormat;
import java.util.*;

public class DecryptionManagerTask extends Task<Boolean> {

    private final static int BLOCKING_QUEUE_SIZE = 1000; // limit so the thread pool won't be overflowed
    private final String userOutput; // original configuration from user
    private final long limit; // num of total configurations available
    private final int agents; // num of agents
    private final BruteForceController controller; // GUI to update
    private double averageTimeTook;
    private final DecimalFormat df;

    public DecryptionManagerTask(String userOutput, long limit, int agents, BruteForceController controller, Runnable onFinish) {
        this.userOutput = userOutput;
        this.limit = limit;
        this.agents = agents;
        this.controller = controller;

        this.controller.bindTaskToUIComponents(this, onFinish);
        averageTimeTook = 0;
        df = new DecimalFormat("#.#####");

        updateProgress(0, limit);
        Platform.setImplicitExit(false);
    }

    @Override
    protected Boolean call() throws Exception { // TODO: make platform.runLater perform normally, add agents
        int j = 0;
        EnigmaEngine currentConfiguration = controller.getTaskCurrentConfiguration().deepClone();

        MachineCode tmpMachineCode = currentConfiguration.getMachineCode();
        final MachineCode currMachineCode = new MachineCode(tmpMachineCode.getRotorsIDInorder(), setStartingPositions(tmpMachineCode.getStartingPositions(), currentConfiguration),
                tmpMachineCode.getSelectedReflectorID(), tmpMachineCode.getPlugBoard(), currentConfiguration.getABC());

        Set<List<String>> allOutputs = new HashSet<>();
        try {
            final int[] numCandidates = {0};

            synchronized (Thread.class) {
                for (int i = 1; i <= limit; i++, j++) {
                    if (isCancelled()) {
                        break;
                    }

                    updateProgress(i, limit);

                    long start = System.nanoTime();

                    currentConfiguration.setEngineConfiguration(currMachineCode);

                    String decipheredOutput = currentConfiguration.processMessage(userOutput);
                    List<String> outputWords = Arrays.asList(decipheredOutput.split(" "));
                    allOutputs.add(outputWords);
                    if (currentConfiguration.getWordsDictionary().getWords().containsAll(outputWords) && outputWords.size() > 0) {
                        numCandidates[0]++;
                        double timeTook = (double)(System.nanoTime() - start) / 1_000_000_000;
                        String elapsedTime = df.format(timeTook);
                        updateAverageTimeTook(timeTook, numCandidates[0]);
                        String averageElapsedTime = df.format(averageTimeTook);
                        updateMessage("<" + currMachineCode + "> -> " + outputWords + ", " + elapsedTime + " seconds");
                        Platform.runLater(() -> controller.updateValues(getMessage(), averageElapsedTime));
                    }
                    currMachineCode.increment();
                    currentConfiguration.setEngineConfiguration(currMachineCode);
                }
                Platform.runLater(() -> controller.unbindTaskFromUIComponents()); // If finished we unbind tasks from UI
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return true;
    }

    private List<Character> setStartingPositions(List<Character> tempStartingPositions, EnigmaEngine enigmaEngine) {
        List<Character> res = new ArrayList<>();
        for (int i = 0; i < tempStartingPositions.size(); i++) {
            res.add(enigmaEngine.getABC().charAt(0));
        }

        return res;
    }

    private void updateAverageTimeTook(double elapsedTime, int numCandidates) {
        averageTimeTook = (averageTimeTook * (numCandidates - 1) / numCandidates) + (elapsedTime / numCandidates);
    }
}
