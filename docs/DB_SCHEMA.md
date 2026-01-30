# Database Schema Specification
> 2026.01.30 Current State

This document defines the database schemas for the **Continue Bank** ecosystem, including the Entrusting Client, SSAP (Trustee), Call Center, and Issuer services.

---

## 1. Entrusting Client DB (`entrusting_db`)
**Port**: 3306 (MySQL)
**Purpose**: Stores customer data, accounts, and compliance logs for Continue Bank.

### `site_users` Table
Stores user profile and consent information.
| Field | Type | Attributes | Description | Compliance Note |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, Auto Inc | User ID | |
| `username` | VARCHAR | Unique | Login ID | |
| `password` | VARCHAR | | Encrypted Password | |
| `name` | VARCHAR(500) | | **Encrypted** (AES-256) Name | 2026 Data Protection |
| `phone_number` | VARCHAR(500) | | **Encrypted** (AES-256) Phone | 2026 Data Protection |
| `ci` | VARCHAR(120) | Unique | Connecting Information | For User Dup Check |
| `di` | VARCHAR(90) | | Duplication Info | |
| `is_verified` | BOOLEAN | | Identity Verified Status | |
| `terms_agreed` | BOOLEAN | | Essential Terms Agreement | |
| `privacy_agreed` | BOOLEAN | | Personal Info Agreement | |
| `unique_id_agreed` | BOOLEAN | | Unique ID Processing | Required |
| `credit_info_agreed` | BOOLEAN | | Credit Info Inquiry | Required |
| `carrier_auth_agreed` | BOOLEAN | | Carrier Auth Service | Required |
| `electronic_finance_agreed` | BOOLEAN | | Elec. Finance Service | Required |
| `monitoring_agreed` | BOOLEAN | | Finance Monitoring/AML | Required |
| `ssap_provision_agreed` | BOOLEAN | | 3rd Party (TM) Provision | Starbucks Event Link |
| `third_party_provision_agreed` | BOOLEAN | | 3rd Party (General) | Optional |
| `marketing_agreed`| BOOLEAN | | Marketing Consent | Optional |
| `marketing_sms` | BOOLEAN | | SMS Marketing | |
| `marketing_email` | BOOLEAN | | Email Marketing | |
| `marketing_push` | BOOLEAN | | Push Marketing | |
| `marketing_personal_agreed` | BOOLEAN | | Personal Marketing | Optional |
| `privacy_agreed_at`| DATETIME | | Agreement Timestamp | |
| `terms_agreed_at` | DATETIME | | Terms Agreement Time | |
| `data_expire_at` | DATETIME | | Retention Expiry Date | Right to Erasure |
| `third_party_retention_until` | DATETIME | | 3rd Party Retention Expiry| 3-Month Limit (TM) |

### `account` Table
Stores banking account information.
| Field | Type | Attributes | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, Auto Inc | Account ID |
| `user_id` | BIGINT | FK | Owner User ID |
| `account_number` | VARCHAR | Unique | Account Number |
| `balance` | DECIMAL | | Current Balance |
| `account_type` | VARCHAR | | Type (CHECKING, SAVINGS) |

### `access_log` Table
Stores audit logs for all data access events.
| Field | Type | Attributes | Description | Compliance Note |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, Auto Inc | Log ID | |
| `user_id` | BIGINT | | Target User ID | |
| `accessor_type` | VARCHAR | | Type (SELF, ADMIN, TM_AGENT)| |
| `accessor_id` | VARCHAR | | Accessor Identifier | |
| `action` | VARCHAR | | Action (VIEW, UPDATE) | |
| `details` | TEXT | | Activity Details | |
| `accessed_at` | DATETIME | | Access Timestamp | Immutable Log |

---

## 2. SSAP (Trustee) DB (`trustee_db`)
**Port**: 3307 (MySQL)
**Purpose**: Manages ephemeral identity verification tokens and virtual carrier data.

### `auth_token` Table
Stores temporary authentication requests (TTL: 3 minutes).
| Field | Type | Attributes | Description | Compliance Note |
| :--- | :--- | :--- | :--- | :--- |
| `token_id` | UUID | PK | Unique Token ID (JTI) | |
| `auth_request_id` | VARCHAR | | Correlation ID | |
| `name` | VARCHAR(500) | | **Encrypted** (AES-256) | |
| `client_data` | VARCHAR(500) | | **Encrypted** Phone | |
| `otp` | VARCHAR(100) | | Hashed OTP | |
| `ci` | VARCHAR(120) | | Generated CI | |
| `di` | VARCHAR(90) | | Generated DI | |
| `status` | VARCHAR | | PENDING, VERIFIED, FAILED | |
| `created_at` | DATETIME | | Creation Time | Auto-delete after 3m |

---

## 3. Call Center DB (`callcenter_db`)
**Port**: 3308 (MySQL)
**Purpose**: Stores consultation results and audit logs (Stateless for customer PII).

### `call_results` Table
Stores outcomes of TM consultations.
| Field | Type | Attributes | Description | Compliance Note |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, Auto Inc | Result ID | |
| `agent_id` | VARCHAR | | Agent ID | |
| `target_id` | VARCHAR | | Reference ID (No PII) | Anonymized Ref |
| `masked_name` | VARCHAR | | Masked Name (Hong*Dong)| Minimized Data |
| `purpose` | VARCHAR | | Call Purpose | |
| `result` | VARCHAR | | Outcome (SUCCESS, FAIL) | |
| `recording_agreed`| BOOLEAN | | Recording Consent | |
| `retention_until` | DATETIME | | Retention Limit (3 Months) | Auto-Deletion |

### `audit_log` Table
Tracks agent actions within the Call Center portal.
| Field | Type | Attributes | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK | Log ID |
| `agent_id` | VARCHAR | | Agent ID |
| `action` | VARCHAR | | Action (SEARCH, VIEW) |
| `target` | VARCHAR | | Target (Masked) |
| `timestamp` | DATETIME | | Event Time |

---

## 4. Issuer DB (`issuer_db`) (In-Memory)
**Port**: 8081 (H2 Console)
**Purpose**: Simulates legacy Card Issuer responses.

### `cards` Table
| Field | Type | Attributes | Description |
| :--- | :--- | :--- | :--- |
| `card_id` | VARCHAR | PK | Unique Card ID |
| `card_number` | VARCHAR | | Masked Card Number |
| `cvc` | VARCHAR | | CVC Code |
| `status` | VARCHAR | | NORMAL, LOST, STOPPED |

### `customers` Table
| Field | Type | Attributes | Description |
| :--- | :--- | :--- | :--- |
| `customer_id` | VARCHAR | PK | UUID form |
| `name` | VARCHAR | | Customer Name |
| `phone` | VARCHAR | | Phone Number |
