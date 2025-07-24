# OSV to Slack Notifier

## Overview
Monitors a `package.json` for changes, scans new/updated npm packages with OSV, and sends Slack DMs.

## Setup
1. Update `application.properties` with:
   - Slack Bot Token
   - Slack User ID
   - package.json path

2. Run the application using:
```bash
./mvnw spring-boot:run
```

3. Ensure `package.json` is present in root or as configured.

## Example Notification
```
📦 lodash@4.17.21
❗ CVE-2020-8203: Prototype Pollution
🕒 2025-07-24T12:30
```

## AI Assistance
This project was generated using ChatGPT to meet the SDE assignment requirements.
