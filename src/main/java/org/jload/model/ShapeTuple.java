package org.jload.model;

/*
The return type of customized testing strategy per seconds
 */
public class ShapeTuple {
    private final String userCls;
    private final int clsTotalNum;
    private int spawnRate;

    /*
    param:
        userCls: The name of user class
        clsTotalNum: Total number of user wanted
        spawnRate: Number of users changed per second
     */
    public ShapeTuple(String userCls, int clsTotalNum, int spawnRate) {
        this.userCls = userCls;
        this.clsTotalNum = clsTotalNum;
        this.spawnRate = spawnRate;
    }

    public ShapeTuple(String userCls) {
        this(userCls, 0, 0);
    }

    public String getUserCls() {
        return userCls;
    }

    public int getClsTotalNum() {
        return clsTotalNum;
    }

    public int getSpawnRate() {
        return spawnRate;
    }

    public void setSpawnRate(int spawnRate) {
        this.spawnRate = spawnRate;
    }

    @Override
    public String toString() {
        return "ShapeTuple{" +
                "userCls='" + userCls + '\'' +
                ", clsTotalNum=" + clsTotalNum +
                ", spawnRate=" + spawnRate +
                '}';
    }
}
