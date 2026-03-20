package com.satcom.platform.common;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IdRequest {
    @NotNull
    private Long id;
}
