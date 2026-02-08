package com.taskmaster.service;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStore {
    // Zbiór zarejestrowanych emaili (działa jak prosta baza userów)
    private final Set<String> registeredEmails = ConcurrentHashMap.newKeySet();

    public void addUser(String email) {
        registeredEmails.add(email);
    }

    public boolean userExists(String email) {
        return registeredEmails.contains(email);
    }
}
