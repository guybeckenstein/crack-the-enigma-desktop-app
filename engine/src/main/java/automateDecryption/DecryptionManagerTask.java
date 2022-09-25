package automateDecryption;

import enigmaEngine.MachineCode;
import enigmaEngine.WordsDictionary;
import enigmaEngine.interfaces.EnigmaEngine;
import javafx.concurrent.Task;
import javafx.util.Pair;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class DecryptionManagerTask extends Task<Boolean> {
    volatile boolean halt = false;
    private static final int MIN_NUMBER_OF_AGENTS = 2;
    private static final int MAX_NUMBER_OF_AGENTS = 50;
    private static final int BLOCKINGQUEUE_SIZE = 1000;

    private int numberOfAgents;
    private final BlockingQueue<MachineCode> machineCodeBlockingQueue;
    private final Queue<Pair<List<String>, MachineCode>> outputQueue;
    private String encryptedText;
    private final ExecutorService tasksPool;
    private long taskSize = 100;
    private EnigmaEngine enigmaEngine;
    private WordsDictionary wordsDictionary;
    private Difficulty difficulty;
    private Consumer<Runnable> onCancel;
    private Consumer<String> onCandidateWordsFound;
    private boolean paused = false;
    private Object pauseLock = new Object();

    private PropertyChangeSupport progress = new PropertyChangeSupport(this);

    public DecryptionManagerTask() {
        this.numberOfAgents = 10;
        this.machineCodeBlockingQueue = new ArrayBlockingQueue<>(BLOCKINGQUEUE_SIZE);
        this.outputQueue = new ConcurrentLinkedQueue<>();
        this.encryptedText = null;
        this.tasksPool = Executors.newFixedThreadPool(numberOfAgents);
    }

    public DecryptionManagerTask(int numberOfAgents, String encryptedText) {
        this.numberOfAgents = numberOfAgents;
        this.machineCodeBlockingQueue = new ArrayBlockingQueue<>(BLOCKINGQUEUE_SIZE);
        this.outputQueue = new ConcurrentLinkedQueue<>();
        this.encryptedText = encryptedText;
        tasksPool = Executors.newFixedThreadPool(numberOfAgents);
    }

    public void initialize(EnigmaEngine enigmaEngine, Difficulty difficulty) {
        this.enigmaEngine = enigmaEngine;
        this.wordsDictionary = enigmaEngine.getWordsDictionary();
        this.difficulty = difficulty;
        System.out.println("Number of agents: " + numberOfAgents);
    }

    @Override
    protected Boolean call() throws Exception {
        System.out.println("Starting TasksManager");
        DecryptionManager decryptionManager = new DecryptionManager(enigmaEngine, machineCodeBlockingQueue, difficulty, encryptedText, taskSize);
        decryptionManager.initializeMachineCode();
        Thread candidateWordsOutputThread = getThread(decryptionManager);

        while (!tasksPool.isTerminated()) {
            try {
                Thread.sleep(200);  //TODO: very bad practice
            } catch (InterruptedException e) {
                halt = true;
                System.out.println("Stopped");
            }
        }

        candidateWordsOutputThread.interrupt();
        return true;
    }

    private Thread getThread(DecryptionManager decryptionManager) {
        Thread decryptionManagerThread = new Thread(decryptionManager);
        decryptionManagerThread.start();

        List<Future<?>> futures = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfAgents; i++) {
            futures.add(tasksPool.submit(new Agent(i, enigmaEngine, machineCodeBlockingQueue, encryptedText, outputQueue, taskSize)));
        }

        Thread candidateWordsOutputThread = new Thread(new CandidateWords(outputQueue, onCandidateWordsFound));
        candidateWordsOutputThread.start();

        tasksPool.shutdown();
        if (tasksPool.isShutdown()) {
            System.out.println("ExecutorService is shutdown");
        }
        return candidateWordsOutputThread;
    }

    public void test() {
        System.out.println("Starting TasksManager");
        DecryptionManager decryptionManager = new DecryptionManager(enigmaEngine.deepClone(), machineCodeBlockingQueue, difficulty, encryptedText, taskSize);
        decryptionManager.initializeMachineCode();
        Thread candidateWordsOutputThread = getThread(decryptionManager);

        if (isPaused()) {
            synchronized (pauseLock) {
                if (isPaused()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        halt = true;
                        // e.printStackTrace();
                    }
                }
            }
        }


        while (!tasksPool.isTerminated()) {
            try {
                Thread.sleep(200);  //TODO: very bad practice
            } catch (InterruptedException e) {
                halt = true;
                System.out.println("DM Stopped");
            }
        }

        candidateWordsOutputThread.interrupt();
    }

    public void setNumberOfAgents(int numberOfAgents) {
        this.numberOfAgents = numberOfAgents;
    }

    public long getTaskSize() {
        return taskSize;
    }

    public void setEncryptedText(String encryptedText) {
        this.encryptedText = encryptedText;
    }

    public void setTaskSize(long taskSize) {
        this.taskSize = taskSize;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setOnCandidateWordsFound(Consumer<String> onCandidateWordsFound) {
        this.onCandidateWordsFound = onCandidateWordsFound; }
}