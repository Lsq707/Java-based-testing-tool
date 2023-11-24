package org.jload.runner;

import java.io.IOException;

public class EnvBuilder {
    private static final int DEFAULT_LOOP_TIME = 0;
    private static final String DEFAULT_CSV_FILE_NAME = null;
    private static final String DEFAULT_HTML_FILE_PATH = null;
    private static final int DEFAULT_USER_NUM = 0;
    private static final int DEFAULT_SPAWN_RATE = 0;
    private static final int DEFAULT_TESTING_TIME = Integer.MAX_VALUE/1000;
    private int loopTime = DEFAULT_LOOP_TIME;
    private String csvFileName = DEFAULT_CSV_FILE_NAME;
    private String htmlFile = DEFAULT_HTML_FILE_PATH;
    private int userNum = DEFAULT_USER_NUM;
    private int spawnRate = DEFAULT_SPAWN_RATE;
    private int testingTime = DEFAULT_TESTING_TIME;
    private String host;

    // Setters for builder pattern
    public EnvBuilder setLoopTime(int loopTime) {
        this.loopTime = loopTime;
        return this;
    }

    public EnvBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public EnvBuilder setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
        return this;
    }

    public EnvBuilder setHtmlFile (String htmlFileName) {
        this.htmlFile = htmlFileName;
        return this;
    }

    public EnvBuilder setUserNum(int userNum) {
        this.userNum = userNum;
        return this;
    }

    public EnvBuilder setSpawnRate(int spawnRate) {
        this.spawnRate = spawnRate;
        return this;
    }

    public EnvBuilder setTestingTime(int testingTime) {
        this.testingTime = testingTime;
        return this;
    }

    // Build method
    public Runner runnerBuild() throws IOException {
        // You can add validation logic here if needed
        return new Runner(loopTime, userNum, spawnRate, testingTime,csvFileName, htmlFile);
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public String getHtmlFile() {
        return htmlFile;
    }

    public String getHost() {
        return host;
    }
}
