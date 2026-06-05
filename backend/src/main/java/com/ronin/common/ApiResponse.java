package com.ronin.common;

public record ApiResponse<T>(boolean success, T data, String message) {}
