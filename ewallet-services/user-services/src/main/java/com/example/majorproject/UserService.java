package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    public void createUser(UserRequest userRequest) {

        User user = User.builder()
                .age(userRequest.getAge())
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .userName(userRequest.getUserName())
                .build();

        userRepository.save(user);


    }
    public User getUserByUserName(String userName) throws Exception{
        try {
            User user = userRepository.findByUserName(userName);
            if(user == null){
                throw new UserNotFoundException();
            }
            return user;
        }catch (Exception e){
            throw new UserNotFoundException();
        }
    }
}
