package org.jload.output;

import org.jload.model.ResponseStat;
import org.jload.model.JMeterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

/*
Write result to csv file
 */
public class CsvOutput {
    private static final Logger logger = LoggerFactory.getLogger(CsvOutput.class);
    private static final String HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect\n";
    private static FileWriter writer;

    public static FileWriter getWriter() {
        return writer;
    }

    public static void createFile(String filePath) throws IOException {
            writer = new FileWriter(filePath);
            try {
                writer.append(HEADERS);
                logger.info("CSV file created successfully with headers.");
            } catch (IOException e) {
                logger.error("Error in CsvOutput: {}", e.getMessage(), e);
            }
    }

    public static synchronized void writeToCsv(ResponseStat responseStat) {
            if (writer != null) {
                JMeterOutput jMeterOutput = new JMeterOutput(responseStat.timeStamp(), responseStat.elapsed(),
                        responseStat.label(), responseStat.responseCode(), responseStat.statusInfo(),
                        responseStat.requestID(), responseStat.dataType(), responseStat.success(),
                        responseStat.failureMessage(), responseStat.bytesSent(), responseStat.bytesReceived(),
                        0, 0, responseStat.host(), responseStat.latency(),
                        responseStat.idleTime(), responseStat.connect());

                try {
                    writer.append(jMeterOutput.toString());
                } catch (IOException e) {
                    logger.error("Error in CsvOutput: {}", e.getMessage(), e);
                }
            } else {
                logger.error("CSV writer not initialized. Please create the file first.");
            }
    }

    public static void closeFile() {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                logger.error("Error in CsvOutput: {}", e.getMessage(), e);
            }
    }
}


