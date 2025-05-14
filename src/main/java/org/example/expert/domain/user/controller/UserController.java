package org.example.expert.domain.user.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.example.expert.security.CustomUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        Long userId = Long.parseLong(userPrincipal.getUsername());
        userService.changePassword(userId, userChangePasswordRequest);
    }

    @PostMapping("/users/profileImage")
    public void imageUpload(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, HttpServletRequest request) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(request.getContentType());
        metadata.setContentLength(request.getContentLengthLong());

        AmazonS3Client amazonS3Client = new AmazonS3Client();
        String key = "myfolder/" + userPrincipal.getUsername();
        amazonS3Client.putObject("chaejinkim-bucket", key, request.getInputStream(), metadata);
    }
}
