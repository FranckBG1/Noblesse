package com.myApp.Noblesse.exceptions;

public record ApiErrorResponse(
     int status,
     String message
)
{}
