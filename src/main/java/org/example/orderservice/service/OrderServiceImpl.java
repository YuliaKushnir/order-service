package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.data.*;
import org.example.orderservice.data.enums.OrderStatus;
import org.example.orderservice.data.enums.Priority;
import org.example.orderservice.dto.UserPersonalData;
import org.example.orderservice.dto.item.CreateOrderItemRequest;
import org.example.orderservice.dto.item.OrderItemDto;
import org.example.orderservice.dto.item.UpdateOrderItemRequest;
import org.example.orderservice.dto.order.CreateOrderRequest;
import org.example.orderservice.dto.order.OrderDto;
import org.example.orderservice.dto.order.UpdateOrderFullRequest;
import org.example.orderservice.dto.order.UpdateOrderRequest;
import org.example.orderservice.dto.print.CreatePrintRequest;
import org.example.orderservice.dto.print.PrintDto;
import org.example.orderservice.dto.print.UpdatePrintRequest;
import org.example.orderservice.exceptions.CreationException;
import org.example.orderservice.messaging.EmailMessage;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserValidationService userValidationService;
    private final FileService fileService;
    private final OrderNotificationService orderNotificationService;

    /** Create order */
    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, String authorizationHeader) {
//        boolean isValidUser = userValidationService.validateUser(request.getUserId(), authorizationHeader);
        UserPersonalData user = userValidationService.getUserById(request.getUserId(), authorizationHeader);
        if (user == null) {
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }

        String name = user.getFirstName();
        List<String> managersEmails = userValidationService.getManagersEmails(authorizationHeader);

        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setStatus(request.getStatus() != null ? request.getStatus() : OrderStatus.NEW);
        order.setPriority(resolvePriority(request.getDeadline()));
        order.setCreatedAt(LocalDateTime.now());
        order.setDeadline(request.getDeadline());
        order.setExecutionDate(request.getExecutionDate());
        order.setManagerId(request.getManagerId());
        order.setWorkerId(request.getWorkerId());
        order.setUserNote(request.getUserNote());
        order.setInternalNote(request.getInternalNote());

        List<OrderItem> items = new ArrayList<>();

        if (request.getItems() != null) {
            for (CreateOrderItemRequest itemRequest : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProductId(itemRequest.getProductId());
                item.setProductName(itemRequest.getProductName());
                item.setBasePrice(nvl(itemRequest.getBasePrice()));
                item.setTextileColor(itemRequest.getTextileColor());
                item.setSize(itemRequest.getSize());
                item.setQuantity(itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1);
                item.setManualTotal(itemRequest.getManualTotal() != null ? itemRequest.getManualTotal() : BigDecimal.ZERO);
                item.setComment(itemRequest.getComment() != null ? itemRequest.getComment() : "");
                List<String> previewUrls = new ArrayList<>();

                if (itemRequest.getPreviewUrls() != null) {
                    previewUrls.addAll(itemRequest.getPreviewUrls());
                }

                if (itemRequest.getPreviewFiles() != null) {
                    for (MultipartFile file : itemRequest.getPreviewFiles()) {
                        if (file != null && !file.isEmpty()) {
                            previewUrls.add(fileService.upload(file));
                        }
                    }
                }

                item.setPreviewUrls(previewUrls);

                List<Print> prints = new ArrayList<>();
                if (itemRequest.getPrints() != null) {
                    for (CreatePrintRequest printRequest : itemRequest.getPrints()) {
                        Print print = new Print();
                        print.setOrderItem(item);
                        print.setTypeId(printRequest.getTypeId());
                        print.setTypeName(printRequest.getTypeName());
                        print.setSize(printRequest.getSize());
                        print.setPlacement(printRequest.getPlacement());
                        print.setQuantity(printRequest.getQuantity() != null ? printRequest.getQuantity() : 1);
                        print.setPrice(nvl(printRequest.getPrice()));
                        print.setManualTotal(printRequest.getManualTotal() != null ? printRequest.getManualTotal() : BigDecimal.ZERO);
                        print.setColorCount(printRequest.getColorCount() != null ? printRequest.getColorCount() : 0);
                        print.setColors(printRequest.getColors() != null ? printRequest.getColors() : new ArrayList<>());

                        List<String> fileUrls = new ArrayList<>();

                        if (printRequest.getFileUrls() != null) {
                            fileUrls.addAll(printRequest.getFileUrls());
                        }

                        if (printRequest.getFilesForPrint() != null) {
                            for (MultipartFile file : printRequest.getFilesForPrint()) {
                                if (file != null && !file.isEmpty()) {
                                    fileUrls.add(fileService.upload(file));
                                }
                            }
                        }

                        print.setFileUrls(fileUrls);
                        prints.add(print);
                    }
                }

                item.setPrints(prints);
                item.setFinalPrice(itemRequest.getFinalPrice() != null ? itemRequest.getFinalPrice() : BigDecimal.ZERO);
                items.add(item);
            }
        }

        order.setItems(items);

        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        } else {
            order.setTotalPrice(
                    items.stream()
                            .map(OrderItem::getFinalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
        }

        Order saved  = orderRepository.save(order);

        if(saved == null || saved.getId() == null) {
            throw new CreationException("Failed to create order");
        }

        String formattedDate = saved.getCreatedAt().format(DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("uk", "UA")));
        String subject  = "Нове замовлення";
        String content = MessageFormat.format("Оформлено нове замовлення №{0} користувачем {1}, {2}.", saved.getId(), name, formattedDate);
        sendNotification(managersEmails, subject, content);

        return mapOrderToOrderDto(saved);
    }

    /** Update order items */
//    @Override
//    @Transactional
//    public OrderDto updateOrder(Long id, UpdateOrderFullRequest request) {
//        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
//
//        // Метадані
//        order.setUserId(request.getUserId());
//        order.setStatus(request.getStatus() != null ? request.getStatus() : order.getStatus());
//
//        order.setDeadline(request.getDeadline());
//        order.setExecutionDate(request.getExecutionDate());
//
//        order.setManagerId(request.getManagerId());
//        order.setWorkerId(request.getWorkerId());
//
//        order.setUserNote(request.getUserNote());
//        order.setInternalNote(request.getInternalNote());
//
//        if (request.getDeadline() != null) {
//            order.setPriority(resolvePriority(request.getDeadline()));
//        }
//
//        // очищення попередніх items
//        order.getItems().clear();
//        BigDecimal orderTotal = BigDecimal.ZERO;
//
//        // додавання нових items
//        if (request.getItems() != null) {
//
//            for (UpdateOrderItemRequest itemReq : request.getItems()) {
//
//                OrderItem item = new OrderItem();
//                item.setOrder(order);
//                item.setProductId(itemReq.getProductId());
//                item.setProductName(itemReq.getProductName());
//                item.setBasePrice(nvl(itemReq.getBasePrice()));
//                item.setTextileColor(itemReq.getTextileColor());
//                item.setSize(itemReq.getSize());
//                item.setQuantity(itemReq.getQuantity() == null ? 1 : itemReq.getQuantity());
//                item.setManualTotal(itemReq.getManualTotal());
//                item.setComment(itemReq.getComment());
//
//                // preview files
//                List<String> previewUrls = new ArrayList<>();
//
//                if (itemReq.getPreviewUrls() != null) {
//                    previewUrls.addAll(itemReq.getPreviewUrls());
//                }
//
//                if (itemReq.getPreviewFiles() != null) {
//                    for (MultipartFile file : itemReq.getPreviewFiles()) {
//                        if (file != null && !file.isEmpty()) {
//                            previewUrls.add(fileService.upload(file));
//                        }
//                    }
//                }
//
//                item.setPreviewUrls(previewUrls);
//
//                // Принти
//                BigDecimal printsTotal = BigDecimal.ZERO;
//                if (itemReq.getPrints() != null) {
//                    for (UpdatePrintRequest printReq : itemReq.getPrints()) {
//                        Print print = new Print();
//                        print.setOrderItem(item);
//                        print.setTypeId(printReq.getTypeId());
//                        print.setTypeName(printReq.getTypeName());
//                        print.setSize(printReq.getSize());
//                        print.setPlacement(printReq.getPlacement());
//                        print.setQuantity(printReq.getQuantity() == null ? 1 : printReq.getQuantity());
//                        print.setPrice(nvl(printReq.getPrice()));
//                        print.setManualTotal(printReq.getManualTotal());
//
//                        print.setColorCount(printReq.getColorCount());
//                        print.setColors(printReq.getColors() != null ? printReq.getColors() : new ArrayList<>());
//
//                        // Print files
//                        List<String> fileUrls = new ArrayList<>();
//
//                        if (printReq.getFileUrls() != null) {
//                            fileUrls.addAll(printReq.getFileUrls());
//                        }
//                        if (printReq.getFilesForPrint() != null) {
//                            for (MultipartFile file : printReq.getFilesForPrint()) {
//                                if (file != null && !file.isEmpty()) {
//                                    fileUrls.add(fileService.upload(file));
//                                }
//                            }
//                        }
//
//                        print.setFileUrls(fileUrls);
//                        item.getPrints().add(print);
//                        BigDecimal printTotal =
//                                printReq.getManualTotal() != null
//                                        ? printReq.getManualTotal()
//                                        : nvl(printReq.getPrice()).multiply(
//                                        BigDecimal.valueOf(print.getQuantity())
//                                );
//
//                        printsTotal = printsTotal.add(printTotal);
//                    }
//                }
//
//                BigDecimal textileTotal =
//                        itemReq.getManualTotal() != null
//                                ? itemReq.getManualTotal()
//                                : nvl(itemReq.getBasePrice()).multiply(
//                                BigDecimal.valueOf(item.getQuantity())
//                        );
//
//                BigDecimal finalPrice = textileTotal.add(printsTotal);
//                item.setFinalPrice(finalPrice);
//
//                order.getItems().add(item);
//
//                orderTotal = orderTotal.add(finalPrice);
//            }
//        }
//
//        if (request.getTotalPrice() != null) {
//            order.setTotalPrice(request.getTotalPrice());
//        } else {
//            order.setTotalPrice(orderTotal);
//        }
//        return mapOrderToOrderDto(orderRepository.save(order));
//    }
    @Override
    @Transactional
    public OrderDto updateOrder(Long id, UpdateOrderFullRequest request, String authorizationHeader) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Метадані
        if (request.getUserId() != null) {
            order.setUserId(request.getUserId());
        }

        if (request.getStatus() != null && request.getStatus() != order.getStatus()) {
            // замовлення виконано - сповіщаємо клієнта
            if (request.getStatus() == OrderStatus.COMPLETED) {
                if (order.getUserId() != null) {
                    sendExecutedOrderNotification(order.getUserId(), order.getId(), authorizationHeader);
                }
            }

            // підтверджено - повідомляємо воркера
            if (request.getStatus() == OrderStatus.CONFIRMED) {
                if (request.getWorkerId() != null) {
                    sendConfirmedOrderNotification(request.getWorkerId(), order.getId(), order.getDeadline(), authorizationHeader);
                }
            }

            order.setStatus(request.getStatus());
        }

        order.setDeadline(request.getDeadline());
        order.setExecutionDate(request.getExecutionDate());

        order.setManagerId(request.getManagerId());

        if (request.getWorkerId() != null) {
            boolean changed = !request.getWorkerId().equals(order.getWorkerId());

            if (changed && request.getStatus() == OrderStatus.CONFIRMED) {
                UserPersonalData user = userValidationService.getUserById(request.getWorkerId(), "");

                if (user != null) {
                    sendConfirmedOrderNotification(request.getWorkerId(), order.getId(), order.getDeadline(), authorizationHeader);
                }
            }

            order.setWorkerId(request.getWorkerId());
        }

        order.setUserNote(request.getUserNote());
        order.setInternalNote(request.getInternalNote());

        if (request.getDeadline() != null) {
            order.setPriority(resolvePriority(request.getDeadline()));
        }

        // позиції замовлення
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        } else {
            order.getItems().clear();
        }

        BigDecimal orderTotal = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (UpdateOrderItemRequest itemReq : request.getItems()) {
                if (itemReq == null) continue;

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setBasePrice(nvl(itemReq.getBasePrice()));
                item.setTextileColor(itemReq.getTextileColor());
                item.setSize(itemReq.getSize());
                item.setQuantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1);
                item.setManualTotal(itemReq.getManualTotal());
                item.setComment(itemReq.getComment());

                List<String> previewUrls = new ArrayList<>();

                if (itemReq.getPreviewUrls() != null) {
                    previewUrls.addAll(itemReq.getPreviewUrls());
                }

                if (itemReq.getPreviewFiles() != null) {
                    for (MultipartFile file : itemReq.getPreviewFiles()) {
                        if (file != null && !file.isEmpty()) {
                            previewUrls.add(fileService.upload(file));
                        }
                    }
                }

                item.setPreviewUrls(previewUrls);

                // prints
                List<Print> prints = new ArrayList<>();
                BigDecimal printsTotal = BigDecimal.ZERO;

                if (itemReq.getPrints() != null) {
                    for (UpdatePrintRequest printReq : itemReq.getPrints()) {

                        if (printReq == null) continue;

                        Print print = new Print();
                        print.setOrderItem(item);
                        print.setTypeId(printReq.getTypeId());
                        print.setTypeName(printReq.getTypeName());
                        print.setSize(printReq.getSize());
                        print.setPlacement(printReq.getPlacement());
                        print.setQuantity(printReq.getQuantity() != null ? printReq.getQuantity() : 1);
                        print.setPrice(nvl(printReq.getPrice()));
                        print.setManualTotal(printReq.getManualTotal());

                        print.setColorCount(printReq.getColorCount());
                        print.setColors(printReq.getColors() != null ? printReq.getColors() : new ArrayList<>());

                        List<String> fileUrls = new ArrayList<>();

                        if (printReq.getFileUrls() != null) {
                            fileUrls.addAll(printReq.getFileUrls());
                        }

                        if (printReq.getFilesForPrint() != null) {
                            for (MultipartFile file : printReq.getFilesForPrint()) {
                                if (file != null && !file.isEmpty()) {
                                    fileUrls.add(fileService.upload(file));
                                }
                            }
                        }

                        print.setFileUrls(fileUrls);
                        prints.add(print);

                        BigDecimal printTotal =
                                printReq.getManualTotal() != null
                                        ? printReq.getManualTotal()
                                        : nvl(printReq.getPrice()).multiply(
                                        BigDecimal.valueOf(print.getQuantity())
                                );

                        printsTotal = printsTotal.add(printTotal);
                    }
                }

                item.setPrints(prints);

                BigDecimal textileTotal =
                        itemReq.getManualTotal() != null
                                ? itemReq.getManualTotal()
                                : nvl(itemReq.getBasePrice()).multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        );

                BigDecimal finalPrice = textileTotal.add(printsTotal);
                item.setFinalPrice(finalPrice);

                order.getItems().add(item);
                orderTotal = orderTotal.add(finalPrice);
            }
        }

        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        } else {
            order.setTotalPrice(orderTotal);
        }

        return mapOrderToOrderDto(orderRepository.save(order));
    }

    /** Get order by id */
    @Override
    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        String currentUserId = getCurrentUserId();
        boolean isPrivileged = hasRole("ROLE_MANAGER", "ROLE_ADMIN", "ROLE_EXECUTOR");

        if (!isPrivileged) {
            checkAccess(currentUserId, order.getUserId());
        }

        return mapOrderToOrderDto(order);
    }

    /**
     * Оновлення метаданих замовлення
     */
    @Override
    @Transactional
    public OrderDto patchOrder(Long id, UpdateOrderRequest request, String authorizationHeader) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Зміна статуса
        if (request.getStatus() != null && request.getStatus() != order.getStatus()) {
            // замовлення виконано - сповіщаємо клієнта
            if (request.getStatus() == OrderStatus.COMPLETED) {
                if (order.getUserId() != null) {
                    sendExecutedOrderNotification(order.getUserId(), order.getId(), authorizationHeader);
                }
            }

            // підтверджено - повідомляємо воркера
            if (request.getStatus() == OrderStatus.CONFIRMED) {
                if (order.getWorkerId() != null) {
                    sendConfirmedOrderNotification(order.getWorkerId(), order.getId(), order.getDeadline(), authorizationHeader);
                }
            }

            order.setStatus(request.getStatus());
        }

        if (request.getDeadline() != null) {
            order.setDeadline(request.getDeadline());
            order.setPriority(resolvePriority(request.getDeadline()));
        }

        if (request.getExecutionDate() != null) {
            order.setExecutionDate(request.getExecutionDate());
        }

        if (request.getManagerId() != null) {
            order.setManagerId(request.getManagerId());
        }

        if (request.getWorkerId() != null) {
            boolean changed = !request.getWorkerId().equals(order.getWorkerId());

            // якщо замовлення узгоджене - сповіщаємо призначеного виконавця
            if (changed && request.getStatus() == OrderStatus.CONFIRMED) {
                UserPersonalData user = userValidationService.getUserById(request.getWorkerId(), "");

                if (user != null) {
                    sendConfirmedOrderNotification(request.getWorkerId(), order.getId(), order.getDeadline(), authorizationHeader);
                }
            }

            order.setWorkerId(request.getWorkerId());
        }

        if (request.getInternalNote() != null) {
            order.setInternalNote(request.getInternalNote());
        }

        return mapOrderToOrderDto(orderRepository.save(order));
    }

    /**
     * Get all orders filtered by status, user or date
     */
    public List<OrderDto> getAllOrders(OrderStatus status, String userId, LocalDateTime dateFrom, LocalDateTime dateTo, LocalDate deadlineDate) {
        List<Order> orders = orderRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return orders.stream()
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> userId == null || Objects.equals(o.getUserId(), userId))
                .filter(o -> dateFrom == null || !o.getCreatedAt().isBefore(dateFrom))
                .filter(o -> dateTo == null || !o.getCreatedAt().isAfter(dateTo))
                .filter(o -> deadlineDate == null || (o.getDeadline() != null && o.getDeadline().toLocalDate().equals(deadlineDate)))
                .map(this::mapOrderToOrderDto)
                .toList();
    }

    /** Get orders by user id */
    @Override
    public List<OrderDto> getUserOrders(String userId) {
        String currentUserId = getCurrentUserId();
        boolean isPrivileged = hasRole("ROLE_MANAGER", "ROLE_ADMIN", "ROLE_EXECUTOR");

        if (!isPrivileged) {
            checkAccess(currentUserId, userId);
        }

        return orderRepository.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::mapOrderToOrderDto)
                .toList();
    }

    /** Delete order */
    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        orderRepository.delete(order);
    }

    /**
     * Check is price not null
     */
    private BigDecimal nvl(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    private Priority resolvePriority(LocalDateTime deadline) {
        if (deadline == null) return Priority.NORMAL;

        LocalDate today = LocalDate.now();
        LocalDate deadlineDate = deadline.toLocalDate();

        if (deadlineDate.isBefore(today)) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
        if (deadlineDate.isEqual(today)) {
            return Priority.URGENT;
        }
        if (deadlineDate.isEqual(today.plusDays(1))) {
            return Priority.HIGH;
        }

        return Priority.NORMAL;
    }

    /**
     * Mappers to dto
     */
    private OrderDto mapOrderToOrderDto(Order order) {

        List<OrderItemDto> itemDtos = order.getItems()
                .stream()
                .map(this::mapItemToDto)
                .toList();

        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getPriority(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getDeadline(),
                order.getExecutionDate(),
                order.getManagerId(),
                order.getWorkerId(),
                order.getUserNote(),
                order.getInternalNote(),
                itemDtos
        );
    }

    private OrderItemDto mapItemToDto(OrderItem item) {
        List<PrintDto> printDtos = item.getPrints()
                .stream()
                .map(this::mapPrintToDto)
                .toList();

        return new OrderItemDto(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getBasePrice(),
                item.getTextileColor(),
                item.getSize(),
                item.getQuantity(),
                item.getManualTotal(),
                item.getFinalPrice(),
                item.getComment(),
                item.getPreviewUrls(),
                printDtos
        );
    }

    private PrintDto mapPrintToDto(Print print) {
        return new PrintDto(
                print.getId(),
                print.getTypeId(),
                print.getTypeName(),
                print.getSize(),
                print.getPlacement(),
                print.getQuantity(),
                print.getPrice(),
                print.getManualTotal(),
                print.getColorCount(),
                print.getColors(),
                print.getFileUrls()
        );
    }

    private void sendNotification(List<String> recipients, String subject, String content) {
        if(recipients == null || recipients.isEmpty()) {
            return;
        }

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(subject)
                .content(content)
                .recipientsEmails(recipients)
                .build();

        orderNotificationService.sendOrderCreatedNotification(emailMessage);
    }

    private void checkAccess(String userIdFromToken, String orderUserId) {
        if (!userIdFromToken.equals(orderUserId)) {
            throw new RuntimeException("Access denied");
        }
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    private boolean hasRole(String... roles) {
        var authorities = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities();

        return authorities.stream()
                .anyMatch(a -> List.of(roles).contains(a.getAuthority()));
    }

    private void sendExecutedOrderNotification(String userId, Long orderId, String authorizationHeader) {
        UserPersonalData user = userValidationService.getUserById(userId, authorizationHeader);

        if (user != null) {
            String orderConfirmedSubject = "Замовлення виконано";
            String orderConfirmedContent = MessageFormat.format("Ваше замовлення №{0} виконано і готове до отримання. \nДякуємо за замовлення", orderId);
            sendNotification(List.of(user.getEmail()), orderConfirmedSubject, orderConfirmedContent);
        }
    }

    private void sendConfirmedOrderNotification(String workerId, Long orderId, LocalDateTime deadline, String authorizationHeader) {
        System.out.println("worker id = " + workerId);
        UserPersonalData user = userValidationService.getUserById(workerId, authorizationHeader);

        System.out.println("user = " + user);
        if (user != null) {
            String formattedDate = deadline == null ? "не встановлено" : deadline.format(DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("uk", "UA")));

            String orderConfirmedSubject = "Замовлення підтверджено";
            String orderConfirmedContent = MessageFormat.format("Замовлення №{0} підтверджено і готове до друку. \nДедлайн: {1}", orderId, formattedDate);
            sendNotification(List.of(user.getEmail()), orderConfirmedSubject, orderConfirmedContent);
        }
    }

}