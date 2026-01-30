# Business Logic & Workflows
> 2026.01.30 Current State

This document outlines the core business logic and workflows of the Continue Bank ecosystem.

---

## 1. Unified Marketing Consent System
**Goal**: Integrate user marketing preferences across Entrusting Client (Bank) and Trustee (TM Center/SSAP) to enable compliant telemarketing.

### Flow Step-by-Step
1.  **User Registration (Entrusting Client)**
    *   User inputs personal info.
    *   **Terms Agreement**: User explicitly agrees to:
        *   Essential: Terms of Service, Privacy Policy.
        *   **Optional**: Marketing Consent, **Third-Party Provision (to TM Center)**.
    *   System saves `third_party_provision_agreed = true` in `entrusting_db`.

2.  **Data Synchronization (S2S)**
    *   **Batch/Real-time**: TM Center requests list of consented users via S2S API (`/api/v1/s2s/marketing-consented`).
    *   **Validation**: Entrusting Client checks `marketing_agreed` AND `third_party_provision_agreed`.
    *   **Encryption**: Data is transferred encrypted (AES-256) or masked (`Hong*Dong`).

3.  **Outbound Call (TM Center)**
    *   Agent views "Marketing Target List" (fetched from S2S).
    *   Agent initiates call. Protocol requires re-confirming identity via SSAP if sensitive actions are needed.

---

## 2. SSAP Identity Verification (V-PASS)
**Goal**: Securely verify user identity using a simulated "Virtual Carrier" system without storing PII permanently.

### Flow Step-by-Step
1.  **Initiation**: User requests verification (e.g., during Sign-up or ID Retrieval).
2.  **Request Handover**: Entrusting Client sends `name`, `phone` (Encrypted) to SSAP (`Trustee Provider`).
3.  **OTP Generation**:
    *   SSAP generates a 6-digit OTP.
    *   Stores `tokenId`, `otp`, and `expiry` (3 mins) in `trustee_db`.
    *   Simulates SMS sending.
4.  **Verification**:
    *   User enters OTP on Entrusting Client UI.
    *   Entrusting Client forwards OTP to SSAP.
    *   SSAP validates OTP and returns `CI` (Connecting Info) and `DI`.
5.  **Completion**:
    *   Entrusting Client marks user as `is_verified = true`.
    *   SSAP deletes auth data (immediately or via TTL job).

---

## 3. Call Center Inbound Flow (Stateless)
**Goal**: Handle inbound customer calls and perform sensitive tasks (e.g., Card Suspension) without storing customer PII in the Call Center DB.

### Flow Step-by-Step
1.  **Customer Call**: Customer calls agent. Agent opens `CallCenter Web`.
2.  **Member Lookup (S2S)**:
    *   Agent enters Caller ID (ANI).
    *   System queries Entrusting Client (`/s2s/members/lookup`).
    *   **Result**: "Member Found (Hong*Dong)" or "Anonymous".
3.  **Identity Verification (On-Call)**:
    *   Agent clicks "Verify Identity".
    *   System triggers SSAP Auth Request for the customer's phone number.
    *   Customer receives OTP and reads it to Agent (or inputs via ARS - simulated).
    *   Agent enters OTP -> Verified.
4.  **Service Execution**:
    *   **Mock Scenario**: Lost Card Suspension.
    *   Call Center sends "Suspend Card" command to **Issuer WAS** (`8081`).
    *   Issuer WAS updates Card Status in `issuer_db`.
5.  **Audit**:
    *   Action is logged in `entrusting_db` (Access Log) and `callcenter_db` (Audit Log).
    *   Call result (Success/Fail) saved in `callcenter_db` (retention 3 months).

---

## 4. Card Issuance & Management (Issuer)
**Goal**: Simulate a legacy financial system (Core Banking) that holds the "Source of Truth" for financial products.

### Logic
*   **Issuance**: When a user opens an account in Entrusting Client, a request is sent to Issuer to create a virtual card.
*   **Status Check**: Call Center allows agents to view Card Status (Active/Lost) by querying Issuer API.
*   **Suspension**: Only authorized agents (verified via Token) can hit the `/cards/{id}/suspend` endpoint.
