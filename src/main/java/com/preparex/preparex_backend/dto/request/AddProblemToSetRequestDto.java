package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for adding a problem to an existing problem set (admin use).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProblemToSetRequestDto {

    @NotNull(message = "Problem ID is required")
    private UUID problemId;

    @Builder.Default
    private Integer position = 0;
}
