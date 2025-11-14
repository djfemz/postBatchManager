package com.aspacelife.postbatch.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchInsertRequest {

    @NotNull(message = "postNumber is required")
    @Min(value = 1, message = "postNumber must be at least 1")
    @Max(value = 100, message = "postNumber cannot exceed 100")
    private Integer postNumber;
}
