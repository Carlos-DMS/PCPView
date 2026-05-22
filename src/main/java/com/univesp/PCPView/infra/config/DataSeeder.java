package com.univesp.PCPView.infra.config;

import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.models.UserModel;
import com.univesp.PCPView.models.enums.RoleEnum;
import com.univesp.PCPView.repository.MachineRepository;
import com.univesp.PCPView.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedInitialData(
            UserRepository userRepository,
            MachineRepository machineRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.findByEmail("admin@equipsea.com") == null) {
                userRepository.save(new UserModel(
                        "ADMINISTRADOR MASTER",
                        "admin@equipsea.com",
                        passwordEncoder.encode("123456"),
                        RoleEnum.ADMIN
                ));
            }

            if (userRepository.findByEmail("operador@equipsea.com") == null) {
                userRepository.save(new UserModel(
                        "OPERADOR DE MAQUINA",
                        "operador@equipsea.com",
                        passwordEncoder.encode("123456"),
                        RoleEnum.USER
                ));
            }

            if (!machineRepository.existsById("MAQ-001")) {
                machineRepository.save(new MachineModel("MAQ-001", "MANDRILHADORA"));
            }
        };
    }
}
