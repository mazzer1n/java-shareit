package ru.practicum.shareit.exception;

import javax.validation.ValidationException;

public class ItemValidationException extends ValidationException {
    public ItemValidationException(String message) {
        super(message);
    }
}
