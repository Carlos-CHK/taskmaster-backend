package com.taskmaster.controller;

import com.taskmaster.controller.dto.TaskRequest;
import com.taskmaster.controller.dto.TaskResponse;
import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    // GET z filtrami (US-04, US-08)
    @GetMapping
    public List<TaskResponse> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        return taskService.getAllTasks(status, priority).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) {
        try {
            Task task = new Task();
            task.setTitle(request.title());
            task.setDescription(request.description());
            task.setPriority(request.priority());
            task.setDueDate(request.dueDate());

            if (request.assignee() != null && !request.assignee().isBlank()) {
                User user = userRepository.findByEmail(request.assignee())
                        .orElseThrow(() -> new RuntimeException("User not found: " + request.assignee()));
                task.setAssignee(user);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(taskService.createTask(task)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); // 400 Bad Request
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        try {
            Task details = new Task();
            details.setTitle(request.title());
            details.setDescription(request.description());
            details.setPriority(request.priority());
            details.setDueDate(request.dueDate());
            Task updated = taskService.updateTask(id, details);
            // Assignee zmieniamy endpointem /assignee (żeby mieć spójny kontrakt API)
            return ResponseEntity.ok(toResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            return ResponseEntity.ok(toResponse(taskService.updateStatus(id, updates.get("status"))));
        } catch (IllegalArgumentException e) {
            // US-06: Zły status -> 400 Bad Request
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/assignee")
    public ResponseEntity<?> updateAssignee(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            return ResponseEntity.ok(toResponse(taskService.updateAssignee(id, updates.get("assignee"))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            // Jeśli user nie istnieje lub zadanie nie istnieje -> 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private TaskResponse toResponse(Task task) {
        String assigneeEmail = task.getAssignee() != null ? task.getAssignee().getEmail() : null;
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                assigneeEmail,
                task.getCreatedAt()
        );
    }
}
