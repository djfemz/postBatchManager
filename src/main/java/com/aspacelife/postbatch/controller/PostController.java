package com.aspacelife.postbatch.controller;

import com.aspacelife.postbatch.dto.PageResponse;
import com.aspacelife.postbatch.dto.request.BatchInsertRequest;
import com.aspacelife.postbatch.dto.response.BaseResponse;
import com.aspacelife.postbatch.exception.BatchInsertException;
import com.aspacelife.postbatch.model.Post;
import com.aspacelife.postbatch.service.PostService;
import com.aspacelife.postbatch.util.AppUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.aspacelife.postbatch.util.AppUtil.buildSuccessResponse;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PostController {

    private final PostService postService;

    /**
     * Endpoint to batch insert posts from external API
     * POST /api/v1/posts/batch_insert
     * Request body: {"postNumber": 24}
     */
    @PostMapping("/batch_insert")
    public CompletableFuture<ResponseEntity<BaseResponse<?>>> batchInsert(
            @Valid @RequestBody BatchInsertRequest request) throws BatchInsertException {
        
        log.info("Received batch insert request for {} posts", request.getPostNumber());
        
        return postService.batchInsertPosts(request.getPostNumber())
            .thenApply(savedCount -> buildSuccessResponse(request, savedCount))
            .exceptionally(AppUtil::buildErrorResponseOnFailure);
    }

    /**
     * Endpoint to fetch paginated posts from database
     * GET /api/v1/posts/fetch_record?page=1 & size=10
     */
    @GetMapping("/fetch_record")
    public ResponseEntity<BaseResponse<?>> fetchRecord(
            @RequestParam(defaultValue = "0") @Min(value = 1, message = "minimum page = 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "minimum page size = 1")
            @Max(value = 100, message = "maximum page size = 100") int size) {
        log.info("Received fetch record request - Page: {}, Size: {}", page, size);
        try {
            PageResponse<Post> pageResponse = postService.fetchRecords(page, size);
            return ResponseEntity.ok(new BaseResponse<>(pageResponse, TRUE, "records fetched successfully"));
        } catch (Exception e) {
            log.error("Error fetching records: {}", e.getMessage(), e);
            BaseResponse<String> errorResponse = new BaseResponse<>(null, FALSE, "Failed to fetch records");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Post Batch Manager");
        return ResponseEntity.ok(response);
    }
}
