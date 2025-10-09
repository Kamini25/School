package com.example.School;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.School.Entity.User;
import com.example.School.Repository.UserRepository;

@SpringBootApplication
public class SchoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchoolApplication.class, args);
		System.out.println("hello");
	}
	@Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername("admin")) {
                repo.save(User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(encoder.encode("admin123"))
                    .roles("USER,ADMIN")
                    .build());
            }
        };
    }
}


/***
 * 
 * If you use .roles("ROLE_USER,ROLE_ADMIN") here, but in your CustomUserDetailsService you split and use .roles(...),
 *  you should store roles as "USER,ADMIN" (without the ROLE_ prefix), 
 * because Spring Security will add the ROLE_ prefix automatically.
 * 
 * 
 */
/**
 * It’s a functional interface (part of org.springframework.boot) with a single method:
 * If any bean in your application implements CommandLineRunner,
*Spring Boot will automatically call its run() method once the application starts.
 * What happens here:
*When your Spring Boot app starts,
*Spring creates all beans (controllers, services, repositories, etc.)
*Then it calls run() on this class
*The method inserts a default admin user into the H2 database
 */