package com.scatterrr.distributednode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

// Class to hold upload response data
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class UploadResponse {
    private int statusCode;
    private String message;
}
