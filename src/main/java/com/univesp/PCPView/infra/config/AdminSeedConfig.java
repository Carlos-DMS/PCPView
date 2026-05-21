package com.univesp.PCPView.infra.config;

import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.models.enums.RoleEnum;
import com.univesp.PCPView.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeedConfig {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByEmail("adminEquipSea@gmail.com") == null) {
                UserModel admin = new UserModel(
                        "admin",
                        "adminEquipSea@gmail.com",
                        passwordEncoder.encode("admin123")
                );

                admin.setRole(RoleEnum.ADMIN);

                userRepository.save(admin);
                System.out.println("Usuário ADMIN inicial criado com sucesso!");
            }
        };
    }
}