package com.w3engineers.meshrnd.util;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Azizul Islam on 1/15/21.
 */
public class ThreadCheck extends AbstractExecutorService {
    private final int RUNNING = 0;
    private final int SHUTDOWN = 1;
    private final int TERMINATED  =2;
    final Lock lock = new ReentrantLock();

    @Override
    public void shutdown() {
        Condition condition = lock.newCondition();
       
    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {

    }
}
