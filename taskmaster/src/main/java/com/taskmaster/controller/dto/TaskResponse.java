package com.taskmaster.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDate dueDate,
        String assignee,
        LocalDateTime createdAt
) {}
