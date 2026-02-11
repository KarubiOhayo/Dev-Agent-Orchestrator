package me.karubidev.devagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DevAgentOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevAgentOrchestratorApplication.class, args);
	}

}
