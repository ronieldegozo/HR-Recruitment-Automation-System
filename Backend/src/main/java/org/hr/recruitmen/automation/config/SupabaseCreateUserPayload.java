package org.hr.recruitmen.automation.config;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseCreateUserPayload(
        String email, String password,
        @JsonProperty("email_confirm") boolean emailConfirm,
        @JsonProperty("app_metadata") Map<String, Object> appMetadata) {
}