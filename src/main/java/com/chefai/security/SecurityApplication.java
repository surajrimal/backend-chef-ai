package com.chefai.security;

import com.chefai.security.auth.AuthenticationService;
import com.chefai.security.auth.RegisterRequest;
import com.chefai.security.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import static com.chefai.security.user.Role.ADMIN;
import static com.chefai.security.user.Role.MANAGER;

@SpringBootApplication
@EnableMongoAuditing(auditorAwareRef = "auditorAware")
public class SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service,
			UserRepository repository
	) {
		return args -> {
			if (!repository.existsByEmail("admin@mail.com")) {
				var admin = RegisterRequest.builder()
						.firstname("Admin")
						.lastname("Admin")
						.email("admin@mail.com")
						.password("password")
						.role(ADMIN)
						.build();
				System.out.println("Admin token: " + service.register(admin).getAccessToken());
			}

			if (!repository.existsByEmail("manager@mail.com")) {
				var manager = RegisterRequest.builder()
						.firstname("Admin")
						.lastname("Admin")
						.email("manager@mail.com")
						.password("password")

						.role(MANAGER)
						.build();
				System.out.println("Manager token: " + service.register(manager).getAccessToken());
			}
		};
	}
}
