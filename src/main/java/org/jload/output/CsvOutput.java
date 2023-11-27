package org.jload.output;

import org.jload.model.RequestOutput;
import org.jload.model.ResponseStat;
import org.jload.model.JMeterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
Write result to csv file
 */
public class CsvOutput {
    private static final Logger logger = LoggerFactory.getLogger(CsvOutput.class);
    private static final String HTML_CSV_HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect\n";
    private static final String REQUEST_CSV_HEADERS = "Type,Name,Request Count,Failure Count,Median Response Time,Average Response Time,Min Response Time,Max Response Time,Average Content Size,Requests/s,Failures/s,50%,66%,75%,80%,90%,95%,98%,99%,99.9%,99.99%,100%\n";
    private static FileWriter HtmlCsvWriter;
    private static FileWriter RequestCsvWriter;
    private static BlockingQueue<ResponseStat> responseQueue;
    private static Thread HtmlCsvThread;
    private static boolean finished = false;

    public static void createHtmlCsvFile(String filePath) throws IOException {
        responseQueue = new LinkedBlockingQueue<>();
        HtmlCsvWriter = new FileWriter(filePath);
        try {
            HtmlCsvWriter.append(HTML_CSV_HEADERS);
            logger.info("HTML CSV file created successfully with headers.");
        } catch (IOException e) {
            logger.error("Error in CsvOutput: {}", e.getMessage(), e);
        }

        waitForResponseToHtmlCsv();
    }

    private static void waitForResponseToHtmlCsv() {
        HtmlCsvThread = Thread.ofVirtual().start(() -> {
            while (!finished || !responseQueue.isEmpty()) {
                try {
                    ResponseStat stat = responseQueue.poll(1, TimeUnit.SECONDS);
                    if (stat != null) {
                        writeToHtmlCsv(stat);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void addResponse(ResponseStat responseStat) {
        try {
            responseQueue.put(responseStat);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createRequestCsvFile(String filePath) throws IOException {
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
                    responseStat.grpThreads(), responseStat.allThreads(), responseStat.host(), responseStat.latency(),
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

    public static void closeFile() {
        try {
            finished = true;
            if (HtmlCsvWriter != null) {
                HtmlCsvThread.join();
                HtmlCsvWriter.close();
            }
            if (RequestCsvWriter != null) {
                for (RequestOutput requestOutput : RequestCsvOutputFilter.requestOutputs.values()) {
                    RequestCsvWriter.append(requestOutput.toString());
                }
                RequestCsvWriter.close();
            }
        } catch (IOException e) {
            logger.error("Error in CsvOutput: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


