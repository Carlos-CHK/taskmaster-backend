package com.taskmaster.service;

import com.taskmaster.model.Task;
import com.taskmaster.model.User;
import com.taskmaster.repository.TaskRepository;
import com.taskmaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository);
    }

    @Test
    void createTask_shouldSucceed_andSetDefaults() {
        Task input = new Task();
        input.setTitle("Test");

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task created = taskService.createTask(input);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        Task saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("TODO");
        assertThat(saved.getPriority()).isEqualTo("MEDIUM");
        assertThat(created.getTitle()).isEqualTo("Test");
    }

    @Test
    void updateStatus_shouldThrowException_whenTaskNotFound() {
        when(taskRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(123L, "IN_PROGRESS"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void updateAssignee_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateAssignee(1L, "nope@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateAssignee_shouldSetUser() {
        User user = new User(1L, "user@example.com", "hash");
        Task task = new Task();
        task.setId(10L);
        task.setTitle("T");
        task.setStatus("TODO");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = taskService.updateAssignee(10L, "user@example.com");

        assertThat(updated.getAssignee()).isNotNull();
        assertThat(updated.getAssignee().getEmail()).isEqualTo("user@example.com");
        verify(taskRepository).save(task);
    }
}
