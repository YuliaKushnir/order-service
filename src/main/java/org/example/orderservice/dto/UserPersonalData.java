package org.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPersonalData {
        private String id;
        private String keycloakId;
        private String firstName;
        private String lastName;
        private String email;
        private String telephone;
        private String company;
        private String role;
}
