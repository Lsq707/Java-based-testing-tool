package org.jload.output;

import org.jload.runner.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
Using jmeter to generate HTML report
*/
public class HtmlOutput {
    private static final Logger logger = LoggerFactory.getLogger(HtmlOutput.class);
    public static void generaHtml(String csvFile, String outputFile) {
        try {
            List<String> commands = new ArrayList<>();
            //Needs to have jmeter as system variables or set the full path to bin here
            commands.add("jmeter");
            commands.add("-Djmeter.save.saveservice.timestamp_format=yyyy-MM-dd HH:mm:ss");
            commands.add("-g");
            commands.add(csvFile);
            commands.add("-o");
            commands.add(outputFile);

            // Create a process builder
            ProcessBuilder builder = new ProcessBuilder(commands);

            // Start and wait for the process to complete
            Process process = builder.start();
            process.waitFor();

            // Handle the process's output or error streams
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            // Handle the streams
            printStream(inputStream);
            printStream(errorStream);

        } catch (IOException | InterruptedException e) {
            logger.error("Error getting in the HtmlOutput Class {}",  e.getMessage(), e);
        }
    }

    /*
    Get the response from cmd line
     */
    private static void printStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

}
