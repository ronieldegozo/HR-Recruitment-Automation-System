# Model rules

- Map the live Supabase schema exactly; do not invent fields or change database types.
- Existing Phase 2 entities are immutable/read-only and must not expose persistence write helpers. Phase 2.A writable entities belong only in `organization/model/`.
- Keep JSONB mappings explicit and keep API serialization out of the entity.
