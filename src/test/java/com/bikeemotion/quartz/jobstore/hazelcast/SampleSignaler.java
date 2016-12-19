package com.bikeemotion.quartz.jobstore.hazelcast;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.SchedulerSignaler;

public class SampleSignaler implements SchedulerSignaler {

    volatile int fMisfireCount = 0;

    @Override
    public void notifyTriggerListenersMisfired(Trigger trigger) {
        fMisfireCount++;
    }

    @Override
    public void signalSchedulingChange(long candidateNewNextFireTime) {

    }

    @Override
    public void notifySchedulerListenersFinalized(Trigger trigger) {

    }

    @Override
    public void notifySchedulerListenersJobDeleted(JobKey jobKey) {

    }

    @Override
    public void notifySchedulerListenersError(String string,
                                              SchedulerException jpe) {

    }
}