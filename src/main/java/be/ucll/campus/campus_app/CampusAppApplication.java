package be.ucll.campus.campus_app;

import be.ucll.campus.campus_app.service.CliService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@ComponentScan("be.ucll.campus.campus_app")
public class CampusAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(CampusAppApplication.class, args);
		System.out.println("DEBUG: CampusAppApplication gestart.");
	}

	@Bean
	@Profile("cli") // Alleen uitvoeren in profiel "cli"
	CommandLineRunner cliRunner(CliService cliService) {
		return args -> cliService.start();
	}
}
