package org.hr.recruitmen.automation.config;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseUpdateUserPayload(@JsonProperty("app_metadata") Map<String, Object> appMetadata) {
}