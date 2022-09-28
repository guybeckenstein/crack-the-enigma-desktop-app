package ui.decryptionManager;

import enigmaEngine.MachineCode;
import enigmaEngine.interfaces.EnigmaEngine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Pair;
import ui.controllers.BruteForceController;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class DecryptionManagerTask extends Task<Boolean> {

    private final static int BLOCKING_QUEUE_SIZE = 1000; // limit so the thread pool won't be overflowed
    private final String userOutput; // original configuration from user
    private final long limit; // num of total configurations available
    private final int agents; // num of agents
    private final BruteForceController controller; // GUI to update
    private double averageTimeTook;
    private final DecimalFormat df;
    private boolean paused = false;
    private Queue<String> candidates;

    public DecryptionManagerTask(String userOutput, long limit, int agents, BruteForceController controller, Runnable onFinish) {
        this.userOutput = userOutput;
        this.limit = limit;
        this.agents = agents;
        this.controller = controller;

        this.controller.bindTaskToUIComponents(this, onFinish);
        averageTimeTook = 0;
        df = new DecimalFormat("#.#####");
        candidates = new LinkedList<>();

        updateProgress(0, agents * limit);
        Platform.setImplicitExit(false);
    }

    @Override
    protected Boolean call() throws Exception {
        EnigmaEngine currentConfiguration = controller.getTaskCurrentConfiguration().deepClone();
        String averageElapsedTime = "", totalElapsedTime = "";

        final int[] numCandidates = {0};
        final AtomicLong finalValue = new AtomicLong(1);

        MachineCode tmpMachineCode = currentConfiguration.getMachineCode();
        tmpMachineCode.increment();
        final MachineCode currMachineCode = new MachineCode(tmpMachineCode.getRotorsIDInorder(), setStartingPositions(tmpMachineCode.getStartingPositions(), currentConfiguration),
                tmpMachineCode.getSelectedReflectorID(), tmpMachineCode.getPlugBoard(), currentConfiguration.getABC());
        currentConfiguration.setEngineConfiguration(currMachineCode);

        Instant taskStart = Instant.now();

        for (int i = 0; i < agents; i++) {
            final AtomicLong currRange = new AtomicLong(1);

            while (currRange.get() <= limit) {
                if (isCancelled()) {
                    break;
                }

                synchronized (this) {
                    while (paused) {
                        System.out.println("Waiting...");
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            System.out.println("User pressed STOP after PAUSE -> interrupted exception was raised");
                        }
                        System.out.println("No more waiting...");
                    }
                }

                updateProgress((i * limit) + currRange.get(), agents * limit);

                long start = System.nanoTime();

                String decipheredOutput = currentConfiguration.processMessage(userOutput);
                List<String> outputWords = Arrays.asList(decipheredOutput.split(" "));

                isOriginalConfigurationFound(currentConfiguration, i, currRange.get());

                if (currentConfiguration.getWordsDictionary().getWords().containsAll(outputWords) && outputWords.size() > 0) {
                    numCandidates[0]++;

                    double timeTook = (double) (System.nanoTime() - start) / 1_000_000_000;
                    String elapsedTime = df.format(timeTook);
                    updateAverageTimeTook(timeTook, numCandidates[0]);
                    averageElapsedTime = df.format(averageTimeTook);

                    candidates.add("<" + currMachineCode + ">, " + outputWords + ", " + elapsedTime + " seconds, agent " + (i + 1));
                    if (i % 100 == 0) {
                        String finalAverageElapsedTime = averageElapsedTime;
                        Platform.runLater(() -> controller.updateValues(candidates, finalAverageElapsedTime));
                    }
                }
                currMachineCode.increment();
                currentConfiguration.setEngineConfiguration(currMachineCode);
                currRange.incrementAndGet();
            }
        }

        Instant taskEnd = Instant.now();

        String finalAverageElapsedTime = averageElapsedTime;
        Platform.runLater(() -> {
            controller.updateValues(candidates, finalAverageElapsedTime);
            controller.unbindTaskFromUIComponents(Long.toString(Duration.between(taskStart, taskEnd).toMillis() / 1_000)); // If finished we unbind tasks from UI
        });
        System.out.println(finalValue.get());
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

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    // Log function
    private void isOriginalConfigurationFound(EnigmaEngine currentConfiguration, int i, long range) {
        boolean equal = true;
        List<Character> startingPositions1 = controller.getTaskCurrentConfiguration().getMachineCode().getStartingPositions();
        List<Character> startingPositions2 = currentConfiguration.getMachineCode().getStartingPositions();
        for (int k = 0; k < startingPositions1.size() && equal == true; k++) {
            if (!startingPositions1.get(k).equals(startingPositions2.get(k))) {
                equal = false;
            }
        }
        if (equal == true) {
            System.out.println("Original configuration in iteration num. " + ((i * limit) + range));
        }
    }
}
