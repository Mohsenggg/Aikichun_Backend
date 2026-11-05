package com.aikichun.domain.task.service;

import com.aikichun.common.exception.ConflictException;
import com.aikichun.domain.check.repository.CheckDefinitionRepository;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.progress.repository.TraineeTaskProgressRepository;
import com.aikichun.domain.task.dto.TaskCreateRequest;
import com.aikichun.domain.task.dto.TaskResponse;
import com.aikichun.domain.task.entity.Task;
import com.aikichun.domain.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TraineeTaskProgressRepository traineeTaskProgressRepository;

    @Mock
    private TaskCheckAssignmentRepository taskCheckAssignmentRepository;

    @Mock
    private CheckDefinitionRepository checkDefinitionRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .title("Configure VLAN")
                .description("VLAN Setup Guide")
                .active(true)
                .build();
        sampleTask.setId(1L);
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("Configure VLAN")
                .description("VLAN Setup Guide")
                .active(true)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Configure VLAN", response.getTitle());
    }

    @Test
    @DisplayName("Should soft-deactivate task without deleting entity")
    void testDeactivateTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.deactivateTask(1L);

        assertNotNull(response);
        assertFalse(response.isActive());
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("Should reject physical DELETE if task has trainee progress records")
    void testDeleteTaskWithProgressFails() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(traineeTaskProgressRepository.existsByTaskId(1L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> taskService.deleteTask(1L));
        assertEquals("Cannot delete task with existing trainee progress. Deactivate the task instead.", ex.getMessage());

        verify(taskRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should physically delete task if no trainee progress exists")
    void testDeleteTaskWithoutProgressSucceeds() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(traineeTaskProgressRepository.existsByTaskId(1L)).thenReturn(false);

        taskService.deleteTask(1L);

        verify(taskRepository).delete(sampleTask);
    }
}

