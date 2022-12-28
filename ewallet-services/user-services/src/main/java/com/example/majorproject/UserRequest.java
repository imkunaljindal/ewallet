package com.example.majorproject;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String userName;
    private String email;
    private String name;
    private int age;

}