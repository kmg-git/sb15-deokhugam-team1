package com.example.sb15deokhugamteam1.dto.request;

import com.example.sb15deokhugamteam1.dto.request.response.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiErrorResponse erro;

    private ApiResponse(
            boolean success,
            T data,
            ApiErrorResponse  erro
    ) {
        this.success = success;
        this.data = data;
        this.erro = erro;
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data,null);

    }
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }
    public static ApiResponse<Void> failure(
            String code,
            String message
    ) {
        return new ApiResponse<>(false,null, new ApiErrorResponse (code, message));

    }

}
