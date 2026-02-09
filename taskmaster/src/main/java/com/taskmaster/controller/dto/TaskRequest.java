package com.taskmaster.controller.dto;

import java.time.LocalDate;

/**
 * DTO wejściowe, żeby nie wymuszać na kliencie wysyłania zagnieżdżonego obiektu User.
 */
public record TaskRequest(
        String title,
        String description,
        String priority,
        LocalDate dueDate,
        String assignee
) {}
