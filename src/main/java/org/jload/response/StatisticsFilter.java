package org.jload.response;

import org.jload.model.ResponseStat;

public interface StatisticsFilter {
    void process(ResponseStat responseStat);
}
