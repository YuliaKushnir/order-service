package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.UserPersonalData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId, String authorizationHeader) {
        try {
            Boolean response = userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(response);
        } catch (WebClientResponseException e) {
            throw new RuntimeException("User Validation Error");
        }
    }

    public List<String> getManagersEmails(String authorizationHeader) {
        try {
            List<String> response = userServiceWebClient.get()
                    .uri("/api/users/managers/emails")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .block();

            return response;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Managers Emails Error");
        }
    }

    public UserPersonalData getUserById(String userId, String authorizationHeader) {
        try {
            UserPersonalData response = userServiceWebClient.get()
                    .uri("/api/users/{userId}/short-info", userId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .bodyToMono(UserPersonalData.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND){
                throw new RuntimeException("User Not Found: " + userId);
            }

            else if (e.getStatusCode() == HttpStatus.BAD_REQUEST){
                throw new RuntimeException("Invalid Request: " + userId);
            }

            else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED){
                throw new RuntimeException("Unauthorized request to user-service");
            }

            throw new RuntimeException("User Service Error");
        }
    }
}
