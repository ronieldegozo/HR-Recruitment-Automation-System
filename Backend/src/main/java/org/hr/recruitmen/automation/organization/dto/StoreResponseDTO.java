package org.hr.recruitmen.automation.organization.dto;

import java.time.Instant;
import java.util.UUID;

import org.hr.recruitmen.automation.organization.model.StoreEntity;

public record StoreResponseDTO(UUID storeId, String storeCode, String name, String location,
        String status, Instant createdAt, Instant updatedAt) {
    public static StoreResponseDTO from(StoreEntity store) {
        return new StoreResponseDTO(store.getId(), store.getStoreCode(), store.getName(),
                store.getLocation(), store.getStatus(), store.getCreatedAt(), store.getUpdatedAt());
    }
}
