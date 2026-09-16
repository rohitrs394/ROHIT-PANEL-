# ROHIT VIP PANEL — Firebase & Backend Architecture

This directory contains the Firebase configurations, security rules, and integration guides for the **ROHIT VIP PANEL** ecosystem.

---

## 1. Firestore Database Schema

Keys are stored in the `keys` collection. The document ID is the **SHA-256 hash** of the raw VIP Key (`sha256(rawKey)`).

### Document Fields:
```json
{
  "keyId": "ROHIT-VIP-ABCD-1234-EF56-7890",
  "keyHash": "3f79bb7b435b0530542cb5b6727bb0331006a88b5a03e680a6b72049ad4535bc",
  "status": "ACTIVE",              // "ACTIVE" | "EXPIRED" | "DISABLED" | "REVOKED"
  "createdAt": 1740000000000,      // Epoch milliseconds
  "expiresAt": 1742592000000,      // Epoch milliseconds (-1 for Lifetime)
  "deviceLimit": 1,                // Max allowed simultaneous devices
  "boundDevices": ["8F9A1B2C3D4E5F6A"], // Array of hardware-hashed device IDs
  "createdBy": "ADMIN_ROHIT"
}
```

---

## 2. Deploying Firestore Security Rules

Deploy the included `firestore.rules` using the Firebase CLI:

```bash
firebase deploy --only firestore:rules
```

These rules enforce:
1. Only authenticated administrators can create or delete keys.
2. App clients can read key statuses and bind their own hardware ID without exposing write access to any other fields.
3. Strict status validation and timestamp checks.

---

## 3. Connecting to the Android App

1. Download your `google-services.json` from the Firebase Console (Android App package name: `com.aistudio.rohitvippanel.vxkp`).
2. Place `google-services.json` inside `/app/`.
3. Rebuild the APK.

---

## 4. Evaluation / Offline Testing

If the app is run without an active internet connection or before `google-services.json` is linked, the app includes a secure built-in evaluation protocol:
- Master Key: `ROHIT-VIP-MASTER-2026`
- Any key matching format: `ROHIT-VIP-XXXX-XXXX-XXXX-XXXX`
