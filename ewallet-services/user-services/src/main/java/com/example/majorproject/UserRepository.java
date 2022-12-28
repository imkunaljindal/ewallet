package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {

    //Define all these function in such manner

    User findByUserName(String userName); //Define in

    List<User> findAllByUserNameAndAge(String userName,int age);
    boolean existsByUserName(String userName);
}
