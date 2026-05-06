package com.example.auth_service;

import com.example.auth_service.entity.Branch;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.BranchRepository;
import com.example.auth_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BranchRepository branchRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        Branch cabang1 = new Branch();
        cabang1.setCode("CBG-001");
        cabang1.setName("Cabang Jakarta Pusat");
        cabang1.setAddress("Jl. Sudirman No. 1, Jakarta");
        branchRepository.save(cabang1);

        Branch cabang2 = new Branch();
        cabang2.setCode("CBG-002");
        cabang2.setName("Cabang Surabaya");
        cabang2.setAddress("Jl. Pemuda No. 5, Surabaya");
        branchRepository.save(cabang2);

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);

        User pusat = new User();
        pusat.setUsername("pusat");
        pusat.setPassword(passwordEncoder.encode("pusat123"));
        pusat.setRole(User.Role.PUSAT);
        userRepository.save(pusat);

        User cabangUser = new User();
        cabangUser.setUsername("cabang001");
        cabangUser.setPassword(passwordEncoder.encode("cabang123"));
        cabangUser.setRole(User.Role.CABANG);
        cabangUser.setBranch(cabang1);
        userRepository.save(cabangUser);

        System.out.println("Data awal berhasil dibuat!");
    }
}