package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new curated problem set (admin use).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProblemSetRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String description;

    private String examId;

    @Builder.Default
    private Boolean isPremium = false;
}
