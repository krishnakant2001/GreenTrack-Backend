package com.greentrack.carbon_tracker_api.advice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(true, "Operation Successful", data);
    }

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(false, message, null);
    }

}
