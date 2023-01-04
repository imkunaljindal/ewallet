package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void createTransaction(TransactionRequest transactionRequest){

        Transaction transaction = Transaction.builder()
                .fromUser(transactionRequest.getFromUser())
                .toUser(transactionRequest.getToUser())
                .amount(transactionRequest.getAmount())
                .status(TransactionStatus.PENDING)
                .transactionId(String.valueOf(UUID.randomUUID()))
                .transactionTime(String.valueOf(new Date())).build();

        // save in DB
        transactionRepository.save(transaction);

        // communicate with the wallet-service
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fromUser",transactionRequest.getFromUser());
        jsonObject.put("toUser",transactionRequest.getToUser());
        jsonObject.put("amount",transactionRequest.getAmount());
        jsonObject.put("transactionId",transaction.getTransactionId());

        String message = jsonObject.toString();
        kafkaTemplate.send("create_transaction",message);

    }

    @KafkaListener(topics={"update_transaction"},groupId="avengers")
    public void updateTransaction(String message) throws JsonProcessingException {
        JSONObject transactionRequest = objectMapper.readValue(message, JSONObject.class);

        String status = (String) transactionRequest.get("status");
        String transactionid = (String) transactionRequest.get("transactionId");

        Transaction transaction = transactionRepository.findByTransactionId(transactionid);

        if(status=="SUCCESS")
        transaction.setStatus(TransactionStatus.SUCCESS);
        else transaction.setStatus(TransactionStatus.FAILED);

        transactionRepository.save(transaction);
    }
}
