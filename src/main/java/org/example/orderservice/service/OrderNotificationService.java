package org.example.orderservice.service;

import org.example.orderservice.messaging.EmailMessage;
import org.springframework.stereotype.Service;

/**
 * Service Interface for sending notification about order.
 */
@Service
public interface OrderNotificationService {
    void sendOrderCreatedNotification(EmailMessage emailMessage);
}
