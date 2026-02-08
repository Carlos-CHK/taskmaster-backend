package com.taskmaster.service;

import com.taskmaster.model.Task;
import com.taskmaster.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserStore userStore; // Wstrzykujemy nasz store

    // US-04, US-08: Pobieranie z filtrowaniem
    public List<Task> getAllTasks(String status, String priority) {
        return taskRepository.findByIsDeletedFalse().stream()
                .filter(t -> status == null || t.getStatus().equals(status))
                .filter(t -> priority == null || (t.getPriority() != null && t.getPriority().equals(priority)))
                .collect(Collectors.toList());
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id).filter(t -> !t.isDeleted());
    }

    public Task createTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        task.setStatus("TODO"); // Domyślny status
        if (task.getPriority() == null) task.setPriority("MEDIUM"); // Domyślny priorytet

        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task details) {
        Task task = getTaskById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(details.getTitle());
        task.setDescription(details.getDescription());
        task.setPriority(details.getPriority());
        task.setDueDate(details.getDueDate());

        return taskRepository.save(task);
    }

    public Task updateStatus(Long id, String newStatus) {
        // US-06: Walidacja statusu
        if (!List.of("TODO", "IN_PROGRESS", "DONE").contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        Task task = getTaskById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    public Task updateAssignee(Long id, String assigneeEmail) {
        // US-07: Walidacja czy user istnieje
        if (!userStore.userExists(assigneeEmail) && !"admin@demo.local".equals(assigneeEmail)) {
            throw new RuntimeException("User not found: " + assigneeEmail); // Rzuci wyjątek, który Controller zamieni na 404
        }

        Task task = getTaskById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setAssignee(assigneeEmail);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        Task task = getTaskById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setDeleted(true); // Soft delete
        taskRepository.save(task);
    }
}
