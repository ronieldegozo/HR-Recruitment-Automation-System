package org.hr.recruitmen.automation.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Read-only JPA mapping for the existing Phase 1 Supabase table. */
@Entity
@Immutable
@Table(name = "job_applications", schema = "public")
public class JobApplicationEntity {

    @Id
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private UUID id;

    @Column(name = "applicant_email", nullable = false, insertable = false, updatable = false)
    private String applicantEmail;

    @Column(name = "applicant_name", insertable = false, updatable = false)
    private String applicantName;

    @Column(name = "gmail_message_id", nullable = false, insertable = false, updatable = false)
    private String gmailMessageId;

    @Column(name = "gmail_thread_id", insertable = false, updatable = false)
    private String gmailThreadId;

    @Column(name = "email_subject", insertable = false, updatable = false)
    private String emailSubject;

    @Column(name = "email_received_at", insertable = false, updatable = false)
    private Instant emailReceivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_files", columnDefinition = "jsonb", insertable = false, updatable = false)
    private JsonNode resumeFiles;

    @Column(name = "status", insertable = false, updatable = false)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "extracted_full_name", insertable = false, updatable = false)
    private String extractedFullName;

    @Column(name = "applicant_mobile_number", insertable = false, updatable = false)
    private String applicantMobileNumber;

    @Column(name = "home_address", insertable = false, updatable = false)
    private String homeAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_experience", columnDefinition = "jsonb", insertable = false, updatable = false)
    private JsonNode workExperience;

    @Column(name = "extraction_status", insertable = false, updatable = false)
    private String extractionStatus;

    @Column(name = "extracted_at", insertable = false, updatable = false)
    private Instant extractedAt;

    protected JobApplicationEntity() {
        // Required by JPA.
    }
}
