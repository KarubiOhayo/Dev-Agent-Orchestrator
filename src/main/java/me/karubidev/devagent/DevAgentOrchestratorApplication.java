package me.karubidev.devagent;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DevAgentOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(DevAgentOrchestratorApplication.class);
		boolean cliMode = isCliMode(args);
		if (cliMode) {
			application.setWebApplicationType(WebApplicationType.NONE);
			application.setBannerMode(Banner.Mode.OFF);
			application.setLogStartupInfo(false);
		}
		ConfigurableApplicationContext context = application.run(args);
		if (cliMode) {
			int exitCode = SpringApplication.exit(context);
			System.exit(exitCode);
		}
	}

	private static boolean isCliMode(String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		String first = args[0] == null ? "" : args[0].trim().toLowerCase();
		if ("devagent".equals(first)) {
			return true;
		}
		return "generate".equals(first) || "spec".equals(first) || "help".equals(first);
	}

}
