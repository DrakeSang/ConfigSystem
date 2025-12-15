package com.emilyordanov.configsdk;

import com.emilyordanov.configsdk.client.ConfigClient;
import com.emilyordanov.configsdk.dto.ConfigurationDto;
import com.emilyordanov.configsdk.properties.ConfigClientProperties;

public class SdkDemo {
    public static void main(String[] args) {

        ConfigClient client =
                new ConfigClient(
                        new ConfigClientProperties("http://localhost:8080")
                );

        ConfigurationDto config =
                client.getLatest("orders-service", "prod");

        System.out.println("Version: " + config.getVersion());
        System.out.println("Data: " + config.getData());
    }
}
