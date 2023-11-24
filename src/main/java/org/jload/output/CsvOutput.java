package org.jload.output;

import org.jload.model.RequestOutput;
import org.jload.model.ResponseStat;
import org.jload.model.JMeterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
Write result to csv file
 */
public class CsvOutput {
    private static final Logger logger = LoggerFactory.getLogger(CsvOutput.class);
    private static final String HTML_CSV_HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect\n";
    private static final String REQUEST_CSV_HEADERS = "Type,Name,Request Count,Failure Count,Median Response Time,Average Response Time,Min Response Time,Max Response Time,Average Content Size,Requests/s,Failures/s,50%,66%,75%,80%,90%,95%,98%,99%,99.9%,99.99%,100%\n";
    private static FileWriter HtmlCsvWriter;
    private static FileWriter RequestCsvWriter;
    private static List<RequestOutput> requestOutputs;

    public static void createHtmlCsvFile(String filePath) throws IOException {
            HtmlCsvWriter = new FileWriter(filePath);
            try {
                HtmlCsvWriter.append(HTML_CSV_HEADERS);
                logger.info("HTML CSV file created successfully with headers.");
            } catch (IOException e) {
                logger.error("Error in CsvOutput: {}", e.getMessage(), e);
            }
    }

    public static void createRequestCsvFile(String filePath) throws IOException {
        requestOutputs = new ArrayList<>();
        RequestCsvWriter = new FileWriter(filePath);
        try {
            RequestCsvWriter.append(REQUEST_CSV_HEADERS);
            logger.info("Request CSV file created successfully with headers.");
        } catch (IOException e) {
            logger.error("Error in CsvOutput: {}", e.getMessage(), e);
        }
    }

    public static synchronized void writeToHtmlCsv(ResponseStat responseStat) {
            if (HtmlCsvWriter != null) {
                JMeterOutput jMeterOutput = new JMeterOutput(responseStat.timeStamp(), responseStat.elapsed(),
                        responseStat.label(), responseStat.responseCode(), responseStat.statusInfo(),
                        responseStat.requestID(), responseStat.dataType(), responseStat.success(),
                        responseStat.failureMessage(), responseStat.bytesSent(), responseStat.bytesReceived(),
                        0, 0, responseStat.host(), responseStat.latency(),
                        responseStat.idleTime(), responseStat.connect());

                try {
                    HtmlCsvWriter.append(jMeterOutput.toString());
                } catch (IOException e) {
                    logger.error("Error in CsvOutput: {}", e.getMessage(), e);
                }
            } else {
                logger.error("CSV writer not initialized. Please create the file first.");
            }
    }

    public static void addRequestStat(ResponseStat responseStat){
        String name = responseStat.rewrittenPath() == null ? responseStat.host() : responseStat.rewrittenPath();
        RequestOutput tempRequestOutput = new RequestOutput();
        tempRequestOutput.setTimeStamp(responseStat.timeStamp());
        tempRequestOutput.setName(name);
        tempRequestOutput.setType(responseStat.label());
        //At first the average values are their own metrics
        tempRequestOutput.setAverageResponseTime(responseStat.elapsed());
        tempRequestOutput.setAverageContentSize(responseStat.bytesReceived());
        if(!responseStat.success())
            tempRequestOutput.setFailureCount(1);
        if(!hasDuplicate(tempRequestOutput)) {
            tempRequestOutput.setRequestCount(1);
            tempRequestOutput.setMaxResponseTime(responseStat.elapsed());
            tempRequestOutput.setMinResponseTime(responseStat.elapsed());
            tempRequestOutput.setMedianResponseTime(responseStat.elapsed());
            requestOutputs.add(tempRequestOutput);
        }
        else
            updateRequestList(tempRequestOutput);
    }

    private static boolean hasDuplicate(RequestOutput newOutput) {
        for (RequestOutput output : requestOutputs) {
            if (output.getName().equals(newOutput.getName()) && output.getType().equals(newOutput.getType())) {
                return true; // Found a duplicate
            }
        }
        return false; // No duplicates found
    }

    private static void updateRequestList(RequestOutput newOutput) {
        for (RequestOutput output : requestOutputs) {
            if (output.getName().equals(newOutput.getName()) && output.getType().equals(newOutput.getType())) {
                output.update(newOutput);
            }
        }
    }

    public static void closeFile() {
            try {
                if (HtmlCsvWriter != null) {
                    HtmlCsvWriter.close();
                }
                if (RequestCsvWriter != null) {
                    for(RequestOutput requestOutput : requestOutputs)
                        RequestCsvWriter.append(requestOutput.toString());
                    RequestCsvWriter.close();
                }
            } catch (IOException e) {
                logger.error("Error in CsvOutput: {}", e.getMessage(), e);
            }
    }
}


