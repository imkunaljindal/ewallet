package com.example.majorproject;

import lombok.*;

import javax.persistence.GeneratedValue;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRequest {

    private String userName;
    private int amount;
}
