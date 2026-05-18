package org.example.orderservice.messaging;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Data Transfer Object representing an email notification message.
 */
@Getter
@Builder
@Jacksonized
public class EmailMessage {
    private String subject;
    private String content;
    private List<String> recipientsEmails;
}
