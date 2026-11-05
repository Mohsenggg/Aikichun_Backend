package com.aikichun.domain.check.service;

import com.aikichun.common.exception.ConflictException;
import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.check.dto.CheckDefinitionRequest;
import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.check.repository.CheckDefinitionRepository;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckDefinitionService {

    private final CheckDefinitionRepository checkDefinitionRepository;
    private final TaskCheckAssignmentRepository taskCheckAssignmentRepository;

    @Transactional
    public CheckDefinitionResponse createCheckDefinition(CheckDefinitionRequest request) {
        String name = request.getName().trim();
        if (checkDefinitionRepository.existsByNameIgnoreCaseAndNotId(name, null)) {
            throw new ConflictException("Check Definition '" + name + "' already exists.");
        }

        String desc = request.getDescription() != null && !request.getDescription().trim().isEmpty()
                ? request.getDescription().trim()
                : null;

        CheckDefinition entity = CheckDefinition.builder()
                .name(name)
                .description(desc)
                .build();

        CheckDefinition saved = checkDefinitionRepository.save(entity);
        return CheckDefinitionResponse.fromEntity(saved, 0);
    }

    @Transactional
    public CheckDefinitionResponse updateCheckDefinition(Long id, CheckDefinitionRequest request) {
        CheckDefinition entity = findCheckDefinitionEntity(id);
        String name = request.getName().trim();

        if (checkDefinitionRepository.existsByNameIgnoreCaseAndNotId(name, id)) {
            throw new ConflictException("Another Check Definition with name '" + name + "' already exists.");
        }

        String desc = request.getDescription() != null && !request.getDescription().trim().isEmpty()
                ? request.getDescription().trim()
                : null;

        entity.setName(name);
        entity.setDescription(desc);

        CheckDefinition updated = checkDefinitionRepository.save(entity);
        long count = taskCheckAssignmentRepository.countByCheckDefinitionId(id);
        return CheckDefinitionResponse.fromEntity(updated, count);
    }

    @Transactional(readOnly = true)
    public List<CheckDefinitionResponse> getAllCheckDefinitions() {
        return checkDefinitionRepository.findAllByOrderByNameAsc().stream()
                .map(entity -> {
                    long count = taskCheckAssignmentRepository.countByCheckDefinitionId(entity.getId());
                    return CheckDefinitionResponse.fromEntity(entity, count);
                })
                .toList();
    }

    @Transactional
    public void deleteCheckDefinition(Long id) {
        CheckDefinition entity = findCheckDefinitionEntity(id);
        long count = taskCheckAssignmentRepository.countByCheckDefinitionId(id);
        if (count > 0) {
            throw new ConflictException("Cannot delete Check Definition '" + entity.getName() + "' because it is currently assigned to " + count + " task(s).");
        }
        checkDefinitionRepository.delete(entity);
    }

    public CheckDefinition findCheckDefinitionEntity(Long id) {
        return checkDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check Definition not found with ID: " + id));
    }
}
