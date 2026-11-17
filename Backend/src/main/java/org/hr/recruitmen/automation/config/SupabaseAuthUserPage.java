package org.hr.recruitmen.automation.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseAuthUserPage(List<SupabaseAuthUser> users) {
}