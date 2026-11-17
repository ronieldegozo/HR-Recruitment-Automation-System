# DTO rules

- DTOs define API input/output contracts and are not JPA entities.
- Never expose a JPA entity directly from a REST resource.
- Auth and admin user-management DTOs must not contain database credentials or Supabase secret keys. Applicant DTOs remain gated on a separate authorization design.
