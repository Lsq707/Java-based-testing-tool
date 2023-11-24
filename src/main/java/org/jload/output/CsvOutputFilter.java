package org.jload.output;

import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;

public class CsvOutputFilter implements StatisticsFilter {
    @Override
    public void process(ResponseStat responseStat) {
        CsvOutput.queue(responseStat);
    }
}
