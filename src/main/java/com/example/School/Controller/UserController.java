package com.example.School.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.example.School.Entity.UserDTO;
import com.example.School.Repository.UserRepository;
import com.example.School.Service.UserService;


    @RestController
    public class UserController {

        @Autowired
        private UserService userService;

        @GetMapping("api/users")
        public List<UserDTO> getAllUsers() {
            List<UserDTO> usersList = userService.getAllUsers();
            return usersList;


        }


}
