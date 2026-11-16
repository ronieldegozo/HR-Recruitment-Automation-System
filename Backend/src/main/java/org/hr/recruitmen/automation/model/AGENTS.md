# Model rules

- Map the live Supabase schema exactly; do not invent fields or change database types.
- Phase 2 entities are immutable/read-only and must not expose persistence write helpers.
- Keep JSONB mappings explicit and keep API serialization out of the entity.
