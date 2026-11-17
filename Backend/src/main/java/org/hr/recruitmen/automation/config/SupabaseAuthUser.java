package org.hr.recruitmen.automation.config;

import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseAuthUser(
        UUID id,
        String email,
        @JsonProperty("app_metadata") Map<String, Object> appMetadata) {
}