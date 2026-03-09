package com.myApp.Noblesse;

import com.myApp.Noblesse.Entities.Users;
import com.myApp.Noblesse.Repositories.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class NoblesseApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoblesseApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Créer admin1 uniquement si la base de données est totalement vide
			if (usersRepository.count() == 0) {
				Users admin = new Users();
				admin.setIdUsers("admin1");
				admin.setNom("admin");
				admin.setAdmin(true);
				admin.setMotDePasse(passwordEncoder.encode("admin"));
				usersRepository.save(admin);
				System.out.println("Utilisateur admin par défaut créé : admin1 / admin");
			}
		};
	}

}
