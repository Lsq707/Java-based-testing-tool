package org.jload.output;

import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;


public class RequestCsvOutputFilter implements StatisticsFilter {
    @Override
    public void process(ResponseStat responseStat) {
        CsvOutput.addRequestStat(responseStat);
    }
}
