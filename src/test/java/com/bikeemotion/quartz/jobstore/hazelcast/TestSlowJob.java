package com.bikeemotion.quartz.jobstore.hazelcast;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * https://github.com/FlavioF/quartz-scheduler-hazelcast-jobstore/blob/master/src/test/java/com/bikeemotion/quartz/jobstore/hazelcast/TestSlowJob.java<br>
 */
public class TestSlowJob implements Job {

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            Thread.sleep(55);
        } catch (InterruptedException ex) {
        }
    }
}