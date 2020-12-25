package com.example.demo.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeDTO {
    private long id;
    private long codeValue;
    private String codeStatus;
    private long shopId;
}
