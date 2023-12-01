package org.jload.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class EnvBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EnvBuilder.class);
    private static final int DEFAULT_LOOP_TIME = 0;
    private static final String DEFAULT_CSV_FILE_NAME = null;
    private static final String DEFAULT_HTML_FILE_PATH = null;
    private static final int DEFAULT_USER_NUM = -1;
    private static final int DEFAULT_SPAWN_RATE = 1;
    private static final int DEFAULT_TESTING_TIME = Integer.MAX_VALUE / 1000;
    private int loopTime = DEFAULT_LOOP_TIME;
    private String csvFileName = DEFAULT_CSV_FILE_NAME;
    private String htmlFile = DEFAULT_HTML_FILE_PATH;
    private int userNum = DEFAULT_USER_NUM;
    private int spawnRate = DEFAULT_SPAWN_RATE;
    private int testingTime = DEFAULT_TESTING_TIME;
    private String host;

    public EnvBuilder setTaskTags(String TaskTag) {
        Env.taskTag = new HashSet<>();
        String[] tags = TaskTag.split("\\s+");
        Env.taskTag.addAll(Arrays.asList(tags));
        return this;
    }

    public EnvBuilder setChosenUser(String chosenUser) {
        Env.chooseUser = true;
        Arrays.stream(chosenUser.split(" "))
                .forEach(user -> Env.chosenUsers.computeIfAbsent(user, k -> -1));
        return this;
    }

    public EnvBuilder setUserCount(String userAndCount) {
        String[] parts = userAndCount.split(" ");
        String key = parts[0];
        int value = Integer.parseInt(parts[1]);
        System.out.println(userAndCount);
        Env.chosenUsers.put(key, Env.chosenUsers.getOrDefault(key, 0) + value);
        return this;
    }

    public EnvBuilder setChosenShape(String chosenShape) {
        Env.chosenShape = chosenShape;
        return this;
    }

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

    public EnvBuilder setHtmlFile(String htmlFileName) {
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

    public EnvBuilder setCheckFailRatio(double failRatio) {
        Env.checkFailRatio = failRatio;
        return this;
    }

    public EnvBuilder setCheckAvgResponseTime(double avgResponseTime) {
        Env.checkAvgResponseTime = avgResponseTime;
        return this;
    }

    // Build method
    public Runner runnerBuild() throws IOException {

        if(userNum == -1 && Env.chosenUsers.isEmpty()){  //If the total user count is unknown, use the count of user class instead.
            userNum = Env.definedUsers.size();
        }
        else if(userNum == -1){ // the count of user class chosen
            if(assignedUserCount(Env.chosenUsers)){ // the count of user class + assigned user count
                userNum = getUseCount();
            }
            else userNum = Env.chosenUsers.size();
        }

        return new Runner(loopTime, userNum, spawnRate, testingTime);
    }

    private int getUseCount() {
        int userCount = 0;
        for (Integer value : Env.chosenUsers.values()) {
            if (value != -1) {
                userCount += value;
                continue;
            }
            userCount++;
        }
        return userCount;
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

    private boolean assignedUserCount(Map<?, Integer> map) {
        for (Integer value : map.values()) {
            if (value != -1) {
                return true; // Have assigned userCount
            }
        }
        return false; // No assigned userCount
    }
}
