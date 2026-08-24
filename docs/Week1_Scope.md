# Factory Safety Incident Tracker - MVP Scope

## Problem Statement
Factory safety incidents are currently recorded on paper forms, Excel spreadsheets, and informal WhatsApp messages. This fragmented process leads to lost reports, delayed responses, and lack of visibility for plant management, ultimately compromising worker safety and delaying corrective actions.

## Stakeholders
- **Shop-floor Worker**: Reports incidents. Needs a fast, simple way to log issues.
- **Safety Officer**: Reviews and investigates incidents. Needs to see details and update status.
- **Plant Manager**: Views dashboard. Needs high-level summary and trends.
- **IT/Admin**: System owner. Needs easy deployment and maintenance.

## Objectives
1. Incident can be logged in under 2 minutes.
2. Dashboard loads in under 3 seconds.
3. 100% of incidents are searchable by date, status, and severity.

## Constraints
- Team size: 1 (solo project).
- Timeline: 15 weeks.
- Tech skill level: Beginner/Intermediate.
- Budget: $0 (use open source / free tools).
- Hosting: Must run on Tomcat/Nginx.

## Approved MVP Scope (Frozen)
1. Incident/event entry form.
2. Searchable dashboard (filter by date, status, severity, location).
3. Summary indicators (counts: open, closed, high-severity).
4. Status drill-down (click a summary card -> filtered list -> detail view).
5. Alert/exception view (overdue or unresolved high-severity incidents).
