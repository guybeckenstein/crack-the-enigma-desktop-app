package automateDecryption;

import enigmaEngine.MachineCode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Pair;

import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class CandidateWords extends Task<Boolean> {
    private final Queue<Pair<List<String>, MachineCode>> outputQueue;
    private Consumer<String> onCandidateWordsFound;

    public CandidateWords(Queue<Pair<List<String>, MachineCode>> outputQueue, Consumer<String> onCandidateWordsFound) {

        this.outputQueue = outputQueue;
        this.onCandidateWordsFound = onCandidateWordsFound;
    }

    @Override
    protected synchronized Boolean call() {
        while (true) {
            Pair<List<String>, MachineCode> pair;
            pair = outputQueue.poll();
            if (pair != null) {
//                System.out.println("Candidate Words: " + pair.getKey() + "\nMachine Code: " + pair.getValue().toString());
                Pair<List<String>, MachineCode> finalPair = pair;
                Platform.runLater(() -> {
                    onCandidateWordsFound.accept("Candidate Words: " +
                            finalPair.getKey() + "\nMachine Code: " + finalPair.getValue().toString());
                });
                System.out.println("Candidate Words: " +
                        finalPair.getKey() + "\nMachine Code: " + finalPair.getValue().toString());
            }

            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Candidate Words Thread is Terminated");
                break;
            }
//            System.out.println("candidate words thread is running");
        }
        return Boolean.TRUE;
    }
}
