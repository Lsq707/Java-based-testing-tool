package org.jload.output;

import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;

public class JMeterCsvOutputFilter implements StatisticsFilter {
    @Override
    public void process(ResponseStat responseStat) {
        if (responseStat.responseCode().equals("NA")) {
            return;
        }
        CsvOutput.addResponse(responseStat);
    }
}
