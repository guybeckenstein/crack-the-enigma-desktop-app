package ui.decryptionManager;

import dto.ConfigurationDTO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Pair;
import ui.controllers.BruteForceController;
import ui.decryptionManager.agent.AgentThreadFactory;
import ui.decryptionManager.producer.BlockingProducer;
import ui.decryptionManager.threadPool.AgentTask;
import ui.decryptionManager.threadPool.ThreadPool;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DecryptionManagerTask extends Task<Boolean> {

    private final static int BLOCKING_QUEUE_SIZE = 1000; // limit so the thread pool won't be overflowed
    private final String userOutput; // original configuration from user
    private final int agents; // num of agents
    private final long totalMissions; // total missions for the agents
    private final int missionSize; // each mission size for agent
    private final int totalReflectors; // Difficulty level: Medium-Impossible
    private final int totalRotors; // Difficulty level: Impossible
    private final Difficulty difficulty;
    private final BruteForceController controller; // GUI to update
    private double averageTimeTook;
    private final DecimalFormat df;
    private final Queue<String> candidates;
    private Instant taskStart;
    private Instant taskEnd;
    private boolean paused;

    public DecryptionManagerTask(String userOutput, long limit, int agents, int missionSize, Difficulty difficulty,
                                 int totalReflectors, int totalRotors, BruteForceController controller, Runnable onFinish) {
        this.userOutput = userOutput;
        this.agents = agents;
        this.missionSize = missionSize;
        this.difficulty = difficulty;
        this.totalReflectors = totalReflectors;
        this.totalRotors = totalRotors;

        this.controller = controller;
        this.controller.bindTaskToUIComponents(this, onFinish);

        averageTimeTook = 0;
        df = new DecimalFormat("#.#######");
        candidates = new LinkedList<>();
        totalMissions = agents * limit;
        updateProgress(0, totalMissions);
        paused = false;

        Platform.setImplicitExit(false);
    }

    @Override
    protected Boolean call() throws Exception {
        BlockingQueue<ConfigurationDTO> dmBlockingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_SIZE);
        BlockingProducer dmBlockingProducer = new BlockingProducer(
                dmBlockingQueue, missionSize, totalMissions, difficulty, totalReflectors, totalRotors, controller.getTaskCurrentConfiguration().getMachineCode());
        Thread blockingProducerThread = new Thread(dmBlockingProducer, "BLOCKING -->");
        blockingProducerThread.start();

        String averageElapsedTime = "";
        taskStart = Instant.now();
        final int[] numCandidates = {0};
        final AtomicLong iterations = new AtomicLong(1);

        ThreadPoolExecutor dmExecutor = new ThreadPool(agents, agents, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(agents), // thread pool's blocking queue for consumers
                new AgentThreadFactory("Agent #"), new CustomRejectionHandler());
        dmExecutor.prestartAllCoreThreads();

        List<Pair<String, Double>> candidatesPairs = new ArrayList<>();
        while (iterations.get() <= totalMissions) {
            checkPaused(); // if user paused the brute force process
            ConfigurationDTO configurationDTO = dmBlockingQueue.take();
            dmExecutor.getQueue().put(new AgentTask(controller.getTaskCurrentConfiguration().deepClone(),
                    configurationDTO, userOutput, missionSize, candidatesPairs));
            Thread.sleep(1);
            try {
                List<Pair<String, Double>> toRemove = new ArrayList<>();
                for (Pair<String, Double> candidate : candidatesPairs) {
                    candidates.add(candidate.getKey());

                    numCandidates[0]++;
                    averageTimeTook = (averageTimeTook * (numCandidates[0] - 1) / numCandidates[0]) + (candidate.getValue() / numCandidates[0]); // updates the average time it takes to encrypt
                    averageElapsedTime = df.format(averageTimeTook);
                    toRemove.add(candidate);
                }
                candidatesPairs.removeAll(toRemove);

                updateProgress(iterations.get(), totalMissions);
                iterations.incrementAndGet();
                String finalAverageElapsedTime = averageElapsedTime;
                Platform.runLater(() -> controller.updateValues(candidates, finalAverageElapsedTime));
            } catch (RejectedExecutionException e) {
                System.err.println("task rejected " + e.getMessage());
            } catch (Exception e) { // Interrupts if there is any possible error
                e.printStackTrace();
            }
        }
        dmExecutor.shutdownNow();
        blockingProducerThread.interrupt();
        taskEnd = Instant.now();

        String finalAverageElapsedTime = averageElapsedTime;
        Platform.runLater(() -> {
            controller.updateValues(candidates, finalAverageElapsedTime);
            controller.unbindTaskFromUIComponents(getTimeElapsed(), true); // If finished we unbind tasks from UI
        });
        return true;
    }

    private void checkPaused() {
        synchronized (this) {
            while (paused) {
                System.out.println("Waiting...");
                try {
                    this.wait();
                    System.out.println("No more waiting...");
                } catch (InterruptedException e) {
                    System.out.println("User pressed STOP after PAUSE -> interrupted exception was raised");
                }
            }
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getTimeElapsed() {
        taskEnd = Instant.now();
        return Double.toString((double)Duration.between(taskStart, taskEnd).toMillis() / 1_000);
    }

    private static class CustomRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) { // Logging
            System.out.println(Thread.currentThread().getName() + " is rejected.");
        }
    }
}
