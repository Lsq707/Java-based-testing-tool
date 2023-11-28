package org.jload.output;

import org.jload.model.RequestOutput;
import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCsvOutputFilter implements StatisticsFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestCsvOutputFilter.class);
    public static final Map<String, RequestOutput> requestOutputs = new ConcurrentHashMap<>();

    @Override
    public void process(ResponseStat responseStat) {
        try {

            String key = responseStat.rewrittenPath() == null ? responseStat.host() : responseStat.rewrittenPath() + ":" + responseStat.label();
            boolean isNewlyCreated = !requestOutputs.containsKey(key);
            RequestOutput tempRequestOutput = requestOutputs.computeIfAbsent(key, k -> generateRequestOutput(responseStat));
            if (!isNewlyCreated) {
                tempRequestOutput.update(responseStat);
            }
        } catch (Exception e) {
            logger.error("Error in RequestCsv {}", e.getMessage(), e);
        }
    }

    private static RequestOutput generateRequestOutput(ResponseStat responseStat) {
        RequestOutput tempRequestOutput = new RequestOutput();

        String name = responseStat.rewrittenPath() == null ? responseStat.host() : responseStat.rewrittenPath();
        tempRequestOutput.setName(name);
        tempRequestOutput.setType(responseStat.label());

        //At first the average values are their own metrics
        tempRequestOutput.setAverageResponseTime(responseStat.elapsed());
        tempRequestOutput.setAverageContentSize(responseStat.bytesReceived());

        LocalDateTime localDateTime = LocalDateTime.parse(responseStat.timeStamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        tempRequestOutput.setMaxTimeStamp(localDateTime);
        tempRequestOutput.setMinTimeStamp(localDateTime);
        tempRequestOutput.setMedianResponseTime(responseStat.elapsed());
        tempRequestOutput.setMinResponseTime(responseStat.elapsed());
        tempRequestOutput.setMaxResponseTime(responseStat.elapsed());
        tempRequestOutput.setRequestCount(1);

        if (!responseStat.success()) {
            tempRequestOutput.setFailureCount(1);
        }
        return tempRequestOutput;
    }
}
