package org.jload.response;

import org.jload.model.ResponseStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/*
Process the response msg
 */
public class Statistics {
    private static final Logger logger = LoggerFactory.getLogger(Statistics.class);
    //For the different output filters
    private static final List<StatisticsFilter> filters = new ArrayList<>();
    //For the thread safe
    private static final ConcurrentHashMap<Long, ResponseStat> statistics = new ConcurrentHashMap<>();
    //For unique keys
    private static final AtomicLong counter = new AtomicLong(0);

    public static void registerFilter(StatisticsFilter filter) {
        filters.add(filter);
    }

    /*
    OverLoad if different types
    */
    public static synchronized void addStatistic(ResponseStat responseStat) {
        long key = counter.incrementAndGet(); // Get a unique key
        statistics.put(key, responseStat);
        applyFilters(responseStat);
    }

    private static void applyFilters(ResponseStat responseStat) {
        for (StatisticsFilter filter : filters) {
            filter.process(responseStat);
        }
    }
}





