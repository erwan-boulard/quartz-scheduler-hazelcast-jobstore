package com.bikeemotion.quartz.jobstore.hazelcast;


import com.bikeemotion.quartz.AbstractTest;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.spi.OperableTrigger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.quartz.Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
import static org.testng.Assert.assertEquals;


/**
 * https://github.com/FlavioF/quartz-scheduler-hazelcast-jobstore/blob/master/src/test/java/com/bikeemotion/quartz/jobstore/hazelcast/HazelcastJobStoreUngracefulShutdownTest.java<br>
 */
public class HazelcastJobStoreUngracefulShutdownTest extends AbstractTest {

    @AfterMethod
    public void tearDown() {
        Hazelcast.shutdownAll();
    }

    @Test
    public void testOneOfTwoInstancesCrashing()
            throws Exception {

        // Build node 1
        final HazelcastInstance hazelcast1 = createHazelcastInstance("testOneOfTwoInstancesCrashing");
        HazelcastJobStore.setHazelcastClient(hazelcast1);
        final HazelcastJobStore jobstore1 = createJobStore("jobstore1");
        jobstore1.setTriggerReleaseThreshold(450);
        jobstore1.setShutdownHazelcastOnShutdown(false);
        jobstore1.initialize(null, new SampleSignaler());

        // Build node 2
        HazelcastInstance hazelcast2 = createHazelcastInstance("testOneOfTwoInstancesCrashing");
        HazelcastJobStore.setHazelcastClient(hazelcast2);
        HazelcastJobStore jobstore2 = createJobStore("jobstore2");
        jobstore2.setShutdownHazelcastOnShutdown(false);
        jobstore2.setTriggerReleaseThreshold(450);
        jobstore2.initialize(null, new SampleSignaler());

        // Add a job and its trigger to the scheduler
        JobDetail job = JobBuilder.newJob(TestSlowJob.class).withIdentity("job1", "jobGroup1").build();
        OperableTrigger trigger = buildAndComputeTrigger("trigger1", "triggerGroup1", job, new Date().getTime());
        trigger.setMisfireInstruction(MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY);
        jobstore1.storeJobAndTrigger(job, (OperableTrigger) trigger);
        final long firstFireTime = new Date(trigger.getNextFireTime().getTime()).getTime();

        // Create a thread for acquiring next triggers on node 1
        // Code non compatible JDK 7
        //Thread acquireThread = new Thread(() -> {
        // Code compatible JDK 7
        final Thread acquireThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    List<OperableTrigger> triggers1 = jobstore1.acquireNextTriggers(firstFireTime + 150, 1, 0L);
                    // Code non compatible JDK 7
                    //triggers1.forEach(jobstore1::releaseAcquiredTrigger);
                    // Code compatible JDK 7
                    for (OperableTrigger t : triggers1) {
                        jobstore1.releaseAcquiredTrigger(t);
                    }
                } catch (JobPersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        //}, "acquireThread");

        // Create a thread for terminating Hazelcast on node 1
        // Code non compatible JDK 7
        // Thread terminateThread = new Thread(hazelcast1.getLifecycleService()::terminate, "terminateThread");
        // Code compatible JDK 7
        Thread terminateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                hazelcast1.getLifecycleService().terminate();
            }
        });

        // Start acquiring next triggers and right after start terminating Hazelcast
        acquireThread.start();
        long waitTime = ThreadLocalRandom.current().nextInt(1,51);
        Thread.sleep(waitTime);
        terminateThread.start();

        // Wait a bit
        Thread.sleep(500);

        // Acquire next triggers on node 2, we should get our trigger here!
        List<OperableTrigger> triggers2 = jobstore2.acquireNextTriggers(firstFireTime + 150 + 6000, 10, 0L);
        System.err.println("-------------------------> VAL " + triggers2.size());
        assertEquals(triggers2.size(),
                1,
                "Should find 1 trigger on node 2 after node 1 crashed when failing after "+waitTime+"ms");
    }
}