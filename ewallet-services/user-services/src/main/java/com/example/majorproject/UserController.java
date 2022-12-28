package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/user")
    public User getUserByUserName(@RequestParam("userName") String userName) throws Exception{
        return userService.getUserByUserName(userName);
    }

    @PostMapping("/user")
    public void createUser(@RequestBody UserRequest userRequest) {
        userService.createUser(userRequest);
    }
}
