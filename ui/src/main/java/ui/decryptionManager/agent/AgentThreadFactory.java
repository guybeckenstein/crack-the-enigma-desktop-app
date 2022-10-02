package ui.decryptionManager.agent;

import java.util.concurrent.ThreadFactory;

public class AgentThreadFactory implements ThreadFactory {
    private int counter = 1;
    private final String prefix;

    public AgentThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, prefix + counter++);
    }
}
