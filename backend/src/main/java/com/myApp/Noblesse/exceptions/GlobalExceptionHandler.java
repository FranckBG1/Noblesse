package com.myApp.Noblesse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //  Ressource non trouvée (ex: mauvaise route)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), "Le chemin demandée est introuvable."),
                HttpStatus.NOT_FOUND
        );
    }

    //  JSON mal formé ou champ manquant
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Format de requête invalide ou données manquantes."),
                HttpStatus.BAD_REQUEST
        );
    }

    //  Mauvaise méthode HTTP
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "Méthode HTTP non autorisée pour cette route."),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    //  Validation d’un champ via @Valid échouée
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            erreurs.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(erreurs, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLogin(IdentifiantsInvalidesException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(CompteVerrouilleException.class)
    public ResponseEntity<ApiErrorResponse> handleLockedAccount(CompteVerrouilleException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }
    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(RessourceIntrouvableException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(RequeteInvalidException.class)
    public ResponseEntity<ApiErrorResponse> handleRequeteInvalide(RequeteInvalidException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    //  Toutes les autres erreurs
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        String message = (ex.getMessage() != null && !ex.getMessage().isEmpty()) ? ex.getMessage() : "Une erreur interne est survenue.";
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
