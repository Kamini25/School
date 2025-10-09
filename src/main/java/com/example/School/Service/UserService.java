package com.example.School.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import com.example.School.Entity.User;
import com.example.School.Entity.UserDTO;
import com.example.School.Repository.UserRepository;

@Component
public class UserService {
    @Autowired
    UserRepository userRepository;
    public List<UserDTO> getAllUsers(){
        System.out.println("Fetching list of users");
        return userRepository.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRoles(user.getRoles());
            return dto;
        }).toList();
        
    }
    
}
