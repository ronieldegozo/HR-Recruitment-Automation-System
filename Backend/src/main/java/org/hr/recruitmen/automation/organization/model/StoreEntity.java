package org.hr.recruitmen.automation.organization.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stores", schema = "recruitflow")
public class StoreEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "manager_user_id", nullable = false)
    private UUID managerUserId;

    @Column(name = "store_code", nullable = false, length = 32, unique = true)
    private String storeCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 300)
    private String location;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StoreEntity() { }

    public StoreEntity(UUID managerUserId, String storeCode, String name, String location) {
        this.id = UUID.randomUUID();
        this.managerUserId = managerUserId;
        this.storeCode = storeCode;
        this.name = name;
        this.location = location;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String name, String location) {
        this.name = name;
        this.location = location;
        this.updatedAt = Instant.now();
    }

    public void transferTo(UUID managerUserId) {
        this.managerUserId = managerUserId;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = "ARCHIVED";
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getManagerUserId() { return managerUserId; }
    public String getStoreCode() { return storeCode; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

