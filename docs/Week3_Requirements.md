# Requirements, Architecture and Technology Setup

## SRS Summary
**Functional Requirements:**
- Incident logging form.
- Dashboard with search and filtering.
- Summary indicators.
- Incident detail view and status update.
- Alerts for unresolved high-severity incidents.

**Non-Functional Requirements:**
- Must load fast (Dashboard < 3s).
- Must run locally with minimal resources.
- Code should be clean and easily containerized.

## Architecture Diagram
```
Client Browser -> Controller (Spring MVC) -> Service Layer -> Repository (Spring Data JPA) -> H2 Database
```

## Data Model & API Endpoints
**Entity: Incident**
- `id` (Long, PK)
- `title` (String)
- `description` (String)
- `severity` (String: LOW, MEDIUM, HIGH)
- `status` (String: OPEN, IN_PROGRESS, CLOSED)
- `location` (String)
- `reportedBy` (String)
- `reportedAt` (Timestamp)
- `resolvedAt` (Timestamp)

**API Endpoints:**
- `GET /incidents` - List all incidents (with filters)
- `POST /incidents` - Create new incident
- `GET /incidents/{id}` - Get incident details
- `PUT /incidents/{id}` - Update incident status
- `GET /incidents/alerts` - Get unresolved high-severity incidents
- `GET /incidents/summary` - Get summary counts
