package com.moodboard.service;

import com.moodboard.entity.AppUser;
import com.moodboard.repository.AppUserRepository;
import com.moodboard.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final AppUserRepository userRepo;
    private final UserProfileRepository profileRepo;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public AuthService(AppUserRepository userRepo, UserProfileRepository profileRepo) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    public AppUser register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
        }
        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
        }
        AppUser user = new AppUser();
        user.username = username;
        user.password = password;
        return userRepo.save(user);
    }

    public Map<String, Object> login(String username, String password) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误"));
        if (!user.password.equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, user.id);
        boolean needOnboarding = profileRepo.findByUserId(user.id).isEmpty();
        return Map.of("token", token, "userId", user.id, "needOnboarding", needOnboarding);
    }

    public Long currentUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少登录令牌");
        }
        String token = authorization.replace("Bearer", "").trim();
        Long userId = tokens.get(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        return userId;
    }
}
