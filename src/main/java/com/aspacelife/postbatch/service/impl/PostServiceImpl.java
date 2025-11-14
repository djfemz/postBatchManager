package com.aspacelife.postbatch.service.impl;

import com.aspacelife.postbatch.dto.PageResponse;
import com.aspacelife.postbatch.dto.response.PostApiResponse;
import com.aspacelife.postbatch.exception.BatchInsertException;
import com.aspacelife.postbatch.model.Post;
import com.aspacelife.postbatch.repository.PostRepository;
import com.aspacelife.postbatch.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final WebClient webClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CompletableFuture<Integer> batchInsertPosts(Integer numberOfPosts) {
        log.info("Starting batch insert for {} posts", numberOfPosts);
        List<CompletableFuture<Post>> futures = new ArrayList<>();
        IntStream.rangeClosed(1, numberOfPosts)
                 .forEach(postId->schedulePostFetchTask(postId, futures));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .thenApply(v -> persistCompletedPosts(futures))
                                .exceptionally(ex -> {
                                    log.error("Error during batch insert: {}", ex.getMessage(), ex);
                                    throw new BatchInsertException(String.format("Failed to complete batch insert with message: %s", ex));
                                });
    }

    private int persistCompletedPosts(List<CompletableFuture<Post>> futures) {
        List<Post> posts = futures.stream()
                                  .map(CompletableFuture::join)
                                  .filter(post -> post != null)
                                  .collect(Collectors.toList());

        log.info("Completed posts to be persisted: {}", posts);

        log.info("Successfully fetched {} posts. Saving to database...", posts.size());
        List<Post> savedPosts = postRepository.saveAll(posts);
        log.info("POSTS:: {}", savedPosts);
        log.info("Successfully saved {} posts to database", savedPosts.size());
        return savedPosts.size();
    }

    private void schedulePostFetchTask(int postId, List<CompletableFuture<Post>> futures) {
        CompletableFuture<Post> future = CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Fetching post with ID: {}", postId);
                PostApiResponse response = webClient.get()
                                                    .uri("/posts/{id}", postId)
                                                    .retrieve()
                                                    .bodyToMono(PostApiResponse.class)
                                                    .block();
                if (response != null) return convertApiResponseToPost(postId, response);
                else throw new BatchInsertException("Received null response for post ID: " + postId);
            } catch (Exception e) {
                log.error("Error fetching post with ID {}: {}", postId, e.getMessage());
                throw new BatchInsertException("Failed to fetch post with ID " + postId, e);
            }
        });
        futures.add(future);
    }

    private Post convertApiResponseToPost(int postId, PostApiResponse response) {
        log.debug("Successfully fetched post with ID: {}", postId);
        return modelMapper.map(response, Post.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Post> fetchRecords(int page, int size) {
        if (page < 1) page = 1;
        page = page - 1;
        if (size < 1) size = 1;
        if (size > 100) size = 100;
        log.info("Fetching posts - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postId"));
        Page<Post> postPage = postRepository.findAll(pageable);
        log.info("Retrieved {} posts out of {} total", postPage.getNumberOfElements(), postPage.getTotalElements());
        return buildPageResponseFrom(postPage);

    }

    private static PageResponse<Post> buildPageResponseFrom(Page<Post> postPage) {
        PageResponse<Post> response = new PageResponse<>();
        response.setContent(postPage.getContent());
        response.setPageNumber(postPage.getNumber());
        response.setPageSize(postPage.getSize());
        response.setTotalElements(postPage.getTotalElements());
        response.setTotalPages(postPage.getTotalPages());
        response.setFirst(postPage.isFirst());
        response.setLast(postPage.isLast());
        response.setEmpty(postPage.isEmpty());
        return response;
    }
}
