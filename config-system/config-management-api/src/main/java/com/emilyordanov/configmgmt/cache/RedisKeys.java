package com.emilyordanov.configmgmt.cache;

public class RedisKeys {
    private RedisKeys() {
    }

    public static String latestConfig(String appName, String env) {
        return "config:latest:" + appName + ":" + env;
    }
}
