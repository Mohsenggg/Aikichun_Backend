package com.aikichun.domain.check.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDefinitionRequest {

    @NotBlank(message = "Check definition name is required")
    @Size(max = 150, message = "Check definition name must not exceed 150 characters")
    private String name;

    private String description;
}
