package com.viefood.base.common;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({"error", "description", "data"})
public class BaseResponse<T> {
//    private String error;
//    private String description;
    private T data;

    public static <T> BaseResponse<T> of(T data) {
        return BaseResponse.<T>builder()
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> error(
            String error,
            String description
    ) {
        return BaseResponse.<T>builder()
//                .error(error)
//                .description(description)
                .data(null)
                .build();
    }
}
