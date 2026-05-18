package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.config.RabbitConfig;
import org.example.orderservice.messaging.EmailMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for sending post creation notifications.
 * Publishes {@link EmailMessage} events to RabbitMQ exchange.
 */
@Service
@RequiredArgsConstructor
public class OrderNotificationServiceImpl implements OrderNotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends a notification about a newly created order.
     * Publishes the message to the "order-processing-exchange" exchange with routing key "order.created.#".
     *
     * @param emailMessage the email message to send
     */
    @Override
    public void sendOrderCreatedNotification(EmailMessage emailMessage){
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, emailMessage);
    }

}