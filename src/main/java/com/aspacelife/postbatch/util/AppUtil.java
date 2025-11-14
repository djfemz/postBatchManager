package com.aspacelife.postbatch.util;

import com.aspacelife.postbatch.dto.request.BatchInsertRequest;
import com.aspacelife.postbatch.dto.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Slf4j
public class AppUtil {
    private AppUtil() {}

    public static ResponseEntity<BaseResponse<?>> buildErrorResponseOnFailure(Throwable ex) {
        log.error("Batch insert failed: {}", ex.getMessage(), ex);
        BaseResponse<?> errorResponse = new BaseResponse<>(ex.getMessage(), FALSE, "Failed to fetch and save posts");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    public static ResponseEntity<BaseResponse<?>> buildSuccessResponse(BatchInsertRequest request, Integer savedCount) {
        BaseResponse<Integer> response = new BaseResponse<>(request.getPostNumber(), TRUE, "Posts fetched and saved successfully");
        response.setSavedPost(savedCount);
        log.info("Batch insert completed successfully. Saved {} posts", savedCount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
