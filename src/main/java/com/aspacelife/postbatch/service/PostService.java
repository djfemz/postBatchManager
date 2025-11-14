package com.aspacelife.postbatch.service;

import com.aspacelife.postbatch.dto.response.PageResponse;
import com.aspacelife.postbatch.model.Post;

import java.util.concurrent.CompletableFuture;

public interface PostService {
    
    /**
     * Fetch posts from external API using CompletableFuture and save to database
     * @param numberOfPosts Number of posts to fetch
     * @return CompletableFuture with number of saved posts
     */
    CompletableFuture<Integer> batchInsertPosts(Integer numberOfPosts);
    
    /**
     * Fetch paginated posts from database
     * @param page Page number
     * @param size Page size
     * @return PageResponse containing posts
     */
    PageResponse<Post> fetchRecords(int page, int size);
}
