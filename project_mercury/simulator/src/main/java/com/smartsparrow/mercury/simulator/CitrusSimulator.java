
package com.smartsparrow.mercury.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitrusSimulator {

    public static void main(String[] args) {
        try {
            SpringApplication.run(CitrusSimulator.class, args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
