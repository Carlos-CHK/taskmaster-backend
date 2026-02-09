package com.taskmaster.controller.dto;

public record AuthResponse(String token, String type, String email) {}
