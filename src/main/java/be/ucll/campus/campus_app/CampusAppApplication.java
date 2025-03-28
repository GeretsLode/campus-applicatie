package be.ucll.campus.campus_app;

import be.ucll.campus.campus_app.service.CliService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan("be.ucll.campus.campus_app")
public class CampusAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(CampusAppApplication.class, args);
		System.out.println("DEBUG: CampusAppApplication gestart.");
	}

	@Bean
	CommandLineRunner cliRunner(CliService cliService, Environment env) {
		return args -> {
			// CLI alleen starten als het actieve profiel niet "test" is
			if (!env.acceptsProfiles("test")) {
				cliService.start();
			}
		};
	}
}
