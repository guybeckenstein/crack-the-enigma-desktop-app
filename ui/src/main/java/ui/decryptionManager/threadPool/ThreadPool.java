package ui.decryptionManager.threadPool;

import java.util.concurrent.*;

// Used for logging
public class ThreadPool extends ThreadPoolExecutor {
    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                      BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread th, Runnable r) {
        super.beforeExecute(th, r);
        System.out.println(th.getName() + " will run the decryption task.");
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        System.out.println(Thread.currentThread().getName() + " has finished the decryption task.");
    }
}
