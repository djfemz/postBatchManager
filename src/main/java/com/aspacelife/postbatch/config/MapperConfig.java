package com.aspacelife.postbatch.config;

import com.aspacelife.postbatch.dto.response.PostApiResponse;
import com.aspacelife.postbatch.model.Post;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(PostApiResponse.class, Post.class)
                .addMapping(PostApiResponse::getId,
                        Post::setExternalId);
        return modelMapper;
    }
}
