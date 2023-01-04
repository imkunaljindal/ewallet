package com.example.majorproject;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    private String transactionId = UUID.randomUUID().toString();

    private String fromUser;

    private String toUser;

    private int amount;

    private TransactionStatus status;

    private String transactionTime;

}
