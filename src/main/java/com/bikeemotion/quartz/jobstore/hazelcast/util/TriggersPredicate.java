package com.bikeemotion.quartz.jobstore.hazelcast.util;

import com.hazelcast.query.Predicate;
import org.quartz.TriggerKey;

import java.util.Map;

/**
 * Filter triggers with a given fire start time, end time and state.
 */
public class TriggersPredicate implements Predicate<TriggerKey, TriggerWrapper> {

    long noLaterThanWithTimeWindow;

    public TriggersPredicate(long noLaterThanWithTimeWindow) {
        this.noLaterThanWithTimeWindow = noLaterThanWithTimeWindow;
    }

    @Override
    public boolean apply(Map.Entry<TriggerKey, TriggerWrapper> entry) {
        if (entry.getValue() == null || entry.getValue().getNextFireTime() == null) {
            return false;
        }

        return entry.getValue().getNextFireTime() <= noLaterThanWithTimeWindow;
    }
}