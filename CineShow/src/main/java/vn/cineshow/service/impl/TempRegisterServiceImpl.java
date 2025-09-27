package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.service.TempRegisterService;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TempRegisterServiceImpl implements TempRegisterService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Override
    public void saveRegisterData(EmailRegisterRequest req) {
        // Tạo token unique
        String token = UUID.randomUUID().toString();
        String key = "register:otp:" + token;

        // Lưu vào Redis với TTL = 15 phút
        redisTemplate.opsForValue().set(key, req, 15, TimeUnit.MINUTES);

    }

    @Override
    public EmailRegisterRequest getRegisterData(String email) {
        var keys = redisTemplate.keys("register:otp:*");
        if (keys == null || keys.isEmpty()) return null;

        for (String key : keys) {
            Object v = redisTemplate.opsForValue().get(key);
            EmailRegisterRequest req = null;
            if (v instanceof EmailRegisterRequest r) req = r;
            else if (v instanceof java.util.Map m) req = mapper.convertValue(m, EmailRegisterRequest.class);

            if (req != null && req.email().equalsIgnoreCase(email)) return req;
        }
        return null;
    }

    @Override
    public void deleteRegisterData(String email) {
        var keys = redisTemplate.keys("register:otp:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            Object v = redisTemplate.opsForValue().get(key);
            EmailRegisterRequest req = null;
            if (v instanceof EmailRegisterRequest r) req = r;
            else if (v instanceof java.util.Map m) req = mapper.convertValue(m, EmailRegisterRequest.class);

            if (req != null && req.email().equalsIgnoreCase(email)) {
                redisTemplate.delete(key);
                break;
            }
        }
    }

    @Override
    public boolean emailExists(String email) {
        // Lấy tất cả keys theo pattern register:otp:*
        var keys = redisTemplate.keys("register:otp:*");
        if (keys == null || keys.isEmpty()) return false;


        for (String key : keys) {
            Object v = redisTemplate.opsForValue().get(key);
            EmailRegisterRequest req = null;
            if (v instanceof EmailRegisterRequest r) req = r;
            else if (v instanceof java.util.Map m) req = mapper.convertValue(m, EmailRegisterRequest.class);

            if (req != null && req.email().equalsIgnoreCase(email)) return true;
        }

        return false;
    }
}
