package com.aspacelife.postbatch.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse <T>{
    private T data;
    private String message;
    private boolean success;
    private Integer savedPost;

    public BaseResponse(T data, boolean isSuccess, String message) {
        this.data = data;
        this.success = isSuccess;
        this.message = message;
    }
}
