package com.bikeemotion.quartz.jobstore.hazelcast.util;

import com.hazelcast.query.Predicate;
import org.quartz.JobKey;

import java.util.Map;

/**
 * Filter triggers with a given job key
 */
public class TriggerByJobPredicate implements Predicate<JobKey, TriggerWrapper> {

    private JobKey key;

    public TriggerByJobPredicate(JobKey key) {
        this.key = key;
    }

    @Override
    public boolean apply(Map.Entry<JobKey, TriggerWrapper> entry) {
        return key != null && entry != null && key.equals(entry.getValue().jobKey);
    }
}