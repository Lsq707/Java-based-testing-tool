package org.jload.user;

import java.util.Random;

public interface WaitTime {
    long getWaitTime();
}

class Between implements WaitTime {
    private final double minWait;
    private final double maxWait;
    private final Random random;

    public Between(double minWait, double maxWait) {
        this.minWait = minWait * 1000;
        this.maxWait = maxWait * 1000;
        this.random = new Random();
    }

    @Override
    public long getWaitTime() {
        return (long) (minWait + random.nextDouble() * (maxWait - minWait));
    }
}

// Class for a constant wait time
class Constant implements WaitTime {
    private final long waitTime;

    public Constant(long waitTime) {
        this.waitTime = waitTime * 1000;
    }

    @Override
    public long getWaitTime() {
        return waitTime;
    }
}

// Class for constant pacing wait time
class ConstantPacing implements WaitTime {
    private final long waitTime;
    private long lastRunTime;
    private long lastWaitTime;

    public ConstantPacing(long waitTime) {
        this.waitTime = waitTime * 1000;
        this.lastRunTime = System.currentTimeMillis();
        this.lastWaitTime = waitTime;
    }

    @Override
    public long getWaitTime() {
        long currentTime = System.currentTimeMillis();
        long runTime = currentTime - lastRunTime - lastWaitTime;
        lastWaitTime = Math.max(0, waitTime - runTime);
        lastRunTime = currentTime;
        return lastWaitTime;
    }
}

// Class for constant throughput wait time
class ConstantThroughput implements WaitTime {
    private final double taskRunsPerSecond;
    private ConstantPacing constantPacing;

    public ConstantThroughput(double taskRunsPerSecond) {
        this.taskRunsPerSecond = taskRunsPerSecond * 1000;
        this.constantPacing = new ConstantPacing((long) (1000 / taskRunsPerSecond));
    }

    @Override
    public long getWaitTime() {
        return constantPacing.getWaitTime();
    }
}
