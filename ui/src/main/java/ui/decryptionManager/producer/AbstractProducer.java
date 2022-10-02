package ui.decryptionManager.producer;

import dto.ConfigurationDTO;
import ui.decryptionManager.Difficulty;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractProducer implements Runnable {
    protected final BlockingQueue<ConfigurationDTO> queue;
    protected final int missionSize;
    protected final long total; // total machine codes to be inserted
    protected final Difficulty difficulty;
    protected final int totalReflectors;
    protected final int totalRotors;

    public AbstractProducer(BlockingQueue<ConfigurationDTO> queue, int missionSize, long total, Difficulty difficulty, int totalReflectors, int totalRotors) {
        this.queue = queue;
        this.missionSize = missionSize;
        this.total = total;
        this.difficulty = difficulty;
        this.totalReflectors = totalReflectors;
        this.totalRotors = totalRotors;
    }
}