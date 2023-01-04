package com.example.majorproject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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

    @Autowired
    RestTemplate restTemplate;

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

        callNotificationService(transaction);
    }

    public void callNotificationService(Transaction transaction){

        String fromUser = transaction.getFromUser();
        String toUser = transaction.getToUser();

        URI url = URI.create("http://localhost:8076/user?userName="+fromUser);
        HttpEntity httpEntity = new HttpEntity(new HttpHeaders());
        JSONObject sender = restTemplate.exchange(url, HttpMethod.GET,httpEntity, JSONObject.class).getBody();
        String senderName = (String) sender.get("name");
        String senderEmail = (String) sender.get("email");

        url = URI.create("http://localhost:8076/user?userName="+toUser);
        JSONObject receiver = restTemplate.exchange(url, HttpMethod.GET,httpEntity, JSONObject.class).getBody();
        String receiverName = (String) receiver.get("name");
        String receiverEmail = (String) receiver.get("email");

        // kafka
        JSONObject senderEmailRequest = new JSONObject();
        senderEmailRequest.put("email",senderEmail);
        String senderMessage = String.format("Hi %s. Your transaction with transactionId %s to %s of amount %d is in %s status.",senderName,transaction.getTransactionId(),receiverName,transaction.getAmount(),transaction.getStatus());
        senderEmailRequest.put("message",senderMessage);
        String message = senderEmailRequest.toString();
        kafkaTemplate.send("send_email",message);

        if(transaction.getStatus()==TransactionStatus.FAILED)
            return;

        JSONObject receiverEmailRequest = new JSONObject();
        receiverEmailRequest.put("email",receiverEmail);
        String receiverMessage = String.format("Hi %s. You have received %d rupees from %s",receiverName,transaction.getAmount(),senderName);
        receiverEmailRequest.put("message",receiverMessage);
        message = receiverEmailRequest.toString();
        kafkaTemplate.send("send_email",message);
    }
}
