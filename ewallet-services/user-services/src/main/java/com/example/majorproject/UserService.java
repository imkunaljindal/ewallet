package com.example.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    private final String REDIS_PREFIX_USER = "user::";
    private final String CREATE_WALLET_TOPIC = "create_wallet";
    public void createUser(UserRequest userRequest) {

        User user = User.builder()
                .age(userRequest.getAge())
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .userName(userRequest.getUserName())
                .build();

        userRepository.save(user);
        saveInCache(user);

        // kafka
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",user.getUserName());

        String message = jsonObject.toString();
        kafkaTemplate.send(CREATE_WALLET_TOPIC,message);

    }

    public void saveInCache(User user){
        Map map = objectMapper.convertValue(user,Map.class);
        redisTemplate.opsForHash().putAll(REDIS_PREFIX_USER+user.getUserName(),map);
        redisTemplate.expire(REDIS_PREFIX_USER+user.getUserName(), Duration.ofHours(12));
    }
    public User getUserByUserName(String userName) throws Exception{

        Map map = redisTemplate.opsForHash().entries(REDIS_PREFIX_USER+userName);

        if(map==null || map.size()==0){
            // cache miss -> search in DB
            User user = userRepository.findByUserName(userName);

            if(user!=null){
                saveInCache(user);
            }
            else { //Throw an error
                throw new UserNotFoundException();
            }
            return user;
        }
        else{
            return objectMapper.convertValue(map,User.class);
        }
    }
}
