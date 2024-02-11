package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.ShortBookingDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ShortBookingDto> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
    }

    @Override
    public boolean isValid(ShortBookingDto dto, ConstraintValidatorContext context) {
        return dto.getEnd() != null && dto.getStart() != null && dto.getEnd().isAfter(dto.getStart());
    }
}

