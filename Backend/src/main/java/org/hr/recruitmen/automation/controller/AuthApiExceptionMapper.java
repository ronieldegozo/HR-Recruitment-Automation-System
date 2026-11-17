package org.hr.recruitmen.automation.controller;

import org.hr.recruitmen.automation.dto.ApiErrorDTO;
import org.hr.recruitmen.automation.security.AuthApiException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthApiExceptionMapper implements ExceptionMapper<AuthApiException> {
    @Override
    public Response toResponse(AuthApiException exception) {
        return Response.status(exception.status())
                .entity(new ApiErrorDTO(exception.getMessage()))
                .build();
    }
}