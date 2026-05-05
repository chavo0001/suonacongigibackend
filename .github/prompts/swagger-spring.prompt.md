---
description: "Generate Swagger/OpenAPI 3 comments for Spring Boot controllers and DTOs"
---

## Task
Add OpenAPI 3 / Swagger documentation comments to all provided code.

## Input
The user may provide:
- One or more files via `file=`
- One or more folders via `folder=`

If a folder is provided:
- Recursively scan all `.java` files inside it
- Process DTOs and Controllers automatically

## Rules
- Use `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Parameter`, `@Schema`, `@Tag`
- For DTOs:
  - Add `@Schema(description = "...")` above the class
  - Add `@Schema(description = "...", example = "...")` above each field
- For Controllers:
  - Add `@Tag(name = "...")` at class level
  - Add `@Operation(summary = "...", description = "...")`
  - Add `@Parameter` for path/query/body parameters
  - Add `@ApiResponses` (200, 400, 404, 500)
- Use realistic examples
- Do NOT invent endpoints or fields

## Output
Return the updated code for all processed files.
