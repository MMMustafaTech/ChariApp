# مسارات تطبيق المواطن - Frontend API Reference

**Base URL المنشور للتجربة:** `https://chari-api.onrender.com`  
**Base URL المحلي:** `http://localhost:8080`  
هوية الاختبار الجديدة `CID001`: ينشئها الباك اند عند تشغيل `APP_TEST_DATA_ENROLLMENT_CITIZEN_ENABLED=true` مرة، ثم يُعاد الإعداد إلى `false`. أنشئ حسابها عبر OTP واختر كلمة مرور؛ لا توجد كلمة مرور افتراضية. الهوية القديمة `123456789` وحسابها لا يتغيران. قد يتأخر أول طلب بعد السكون لأن Render يستخدم الخطة المجانية.
كل مسار يبدأ بـ `/api/v1/me/` يحتاج Header:

```http
Authorization: Bearer <accessToken>
```

تطبيق الجوال لا يحتاج CORS. وعند تشغيل Flutter كويب محليًا، يسمح Render حاليًا بـ `http://localhost:*`. قبل نشر واجهة ويب حقيقية أضف domain الواجهة الصريح إلى `APP_ALLOWED_ORIGINS`.

## 1. الدخول والتسجيل

| الطريقة والمسار | Body | يرجع |
|---|---|---|
| `POST /auth/enrollment/otp` | `{"nationalId":"123456789","phoneNumber":"+23599123456","email":"citizen@example.com"}` | `202` مع `{"challengeId":"uuid"}` |
| `POST /auth/enrollment/otp/verify` | `{"challengeId":"uuid","code":"123456"}` | `204 No Content` |
| `POST /auth/enrollment/accounts` | `{"challengeId":"uuid","email":"...","password":"..."}` | `201` مع `{"accountId":"uuid"}` |
| `POST /api/v1/auth/login` | `{"nationalId":"CID001","password":"..."}` | `200` مع `accessToken`, `refreshToken`, `tokenType`, `expiresIn` |
| `POST /api/v1/auth/refresh` | `{"refreshToken":"..."}` | Token pair جديد بنفس حقول login |
| `POST /api/v1/auth/logout` | `{"refreshToken":"..."}` | `204 No Content` |

دخول المواطن بالرقم الوطني وكلمة المرور؛ البريد مطلوب عند التسجيل فقط. دخول البريد القديم مدعوم للتوافق، لكن لا ترسل البريد والرقم الوطني معًا.

مثال login/refresh:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

## 2. الصفحة الرئيسية والملف الشخصي

| الطريقة والمسار | يرجع |
|---|---|
| `GET /api/v1/me/home` | `unreadNotificationCount`, `openRequestCount`, `upcomingAppointment`, `recentRequests` |
| `GET /api/v1/me/profile` | `email`, `maskedNationalId`, `maskedVerifiedPhone`, `phoneVerified`, `phoneVerifiedAt`, `accountCreatedAt` |
| `PATCH /api/v1/me/profile/password` | `204 No Content` |
| `POST /api/v1/me/profile/logout-all` | `{"revokedSessions": 2}` |

تغيير كلمة المرور:

```json
{ "currentPassword": "OldPassword123!", "newPassword": "NewPassword123!" }
```

مثال استجابة الـHome:

```json
{
  "unreadNotificationCount": 2,
  "openRequestCount": 1,
  "upcomingAppointment": {
    "id": "uuid",
    "serviceType": "PASSPORT",
    "officeName": "N'Djamena Office",
    "startsAt": "2026-09-01T09:00:00Z",
    "endsAt": "2026-09-01T09:15:00Z"
  },
  "recentRequests": [
    { "id": "uuid", "serviceType": "PASSPORT", "kind": "ISSUANCE", "status": "SUBMITTED", "submittedAt": "2026-08-29T10:00:00Z" }
  ]
}
```

> بعد تغيير كلمة المرور أو `logout-all`: امسح الـtokens من الجهاز وانتقل إلى شاشة الدخول.

## 3. الطلبات

### شكل الاستجابة الموحد للطلب

طلبات الجواز والهوية وشهادة الميلاد ترجع كائنًا يحتوي: `id`, `citizenId`, `kind`, `requestReason`, `status`, `reviewedBy`, `submittedAt`, `reviewedAt`, `decisionReason`.

الحالات: `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`.

### الجواز

| الطريقة والمسار | Body عند الحاجة | يرجع |
|---|---|---|
| `POST /api/v1/me/passport-requests` | `{"kind":"ISSUANCE","reason":null}` | الطلب المنشأ (`201`) |
| `GET /api/v1/me/passport-requests` | - | قائمة طلبات الجواز للمستخدم |
| `GET /api/v1/me/passport-requests/{requestId}/history` | - | قائمة تغيّرات الحالة |

### الهوية الوطنية

| الطريقة والمسار | Body عند الحاجة | يرجع |
|---|---|---|
| `POST /api/v1/me/national-identity-requests` | `{"kind":"ISSUANCE","reason":null}` | الطلب المنشأ (`201`) |
| `GET /api/v1/me/national-identity-requests` | - | قائمة طلبات الهوية للمستخدم |
| `GET /api/v1/me/national-identity-requests/{requestId}/history` | - | قائمة تغيّرات الحالة |

قيم `kind` للجواز والهوية: `ISSUANCE`, `RENEWAL`, `LOST`, `DAMAGED`, `DATA_CORRECTION`.  
حقل `reason` مطلوب لـ `LOST`, `DAMAGED`, `DATA_CORRECTION`.

### شهادة الميلاد

| الطريقة والمسار | Body عند الحاجة | يرجع |
|---|---|---|
| `POST /api/v1/me/birth-certificate-requests` | موضح أدناه | الطلب المنشأ (`201`) |
| `GET /api/v1/me/birth-certificate-requests` | - | قائمة طلبات شهادة الميلاد للمستخدم |
| `GET /api/v1/me/birth-certificate-requests/{requestId}/history` | - | قائمة تغيّرات الحالة |
| `GET /api/v1/me/birth-certificate-requests/{requestId}/newborn-registration` | - | بيانات المولود للطلب المحدد |

لـ`NEWBORN_REGISTRATION`:

```json
{
  "kind": "NEWBORN_REGISTRATION",
  "reason": null,
  "newbornRegistration": {
    "childFirstName": "Ahmed",
    "childLastName": "Ali",
    "dateOfBirth": "2026-08-20",
    "placeOfBirth": "N'Djamena",
    "gender": "MALE",
    "fatherFullName": "...",
    "fatherNationalId": "123456789",
    "motherFullName": "...",
    "motherNationalId": "987654321"
  }
}
```

قيم النوع: `NEWBORN_REGISTRATION`, `CERTIFICATE_EXTRACT`, `DATA_CORRECTION`. قيم الجنس: `MALE`, `FEMALE`.

### استجابة تاريخ الطلب

كل عنصر في التاريخ يرجع: `id`, `requestId`, `fromStatus`, `toStatus`, `reason`, `changedBy`, `changedAt`.

## 4. المرفقات

استخدم `multipart/form-data` والحقل اسمه **`file`**. المسموح JPEG وPNG وPDF فقط، حتى 5MB، والرفع متاح فقط عندما تكون حالة الطلب `SUBMITTED`.

| العملية | الجواز | الهوية | شهادة الميلاد | يرجع |
|---|---|---|---|---|
| رفع | `POST /api/v1/me/passport-requests/{id}/attachments` | `POST /api/v1/me/national-identity-requests/{id}/attachments` | `POST /api/v1/me/birth-certificate-requests/{id}/attachments` | `201` مع metadata للمرفق |
| القائمة | `GET /api/v1/me/passport-requests/{id}/attachments` | `GET /api/v1/me/national-identity-requests/{id}/attachments` | `GET /api/v1/me/birth-certificate-requests/{id}/attachments` | قائمة metadata |
| تنزيل المحتوى | `GET /api/v1/me/passport-requests/{id}/attachments/{attachmentId}/content` | `GET /api/v1/me/national-identity-requests/{id}/attachments/{attachmentId}/content` | `GET /api/v1/me/birth-certificate-requests/{id}/attachments/{attachmentId}/content` | الملف نفسه كـbinary stream |

Metadata المرفق: `id`, `fileName`, `contentType`, `sizeBytes`, `uploadedAt`.

## 5. المواعيد

| الطريقة والمسار | Body عند الحاجة | يرجع |
|---|---|---|
| `GET /api/v1/me/appointment-slots?serviceType=PASSPORT` | - | قائمة slots المتاحة |
| `POST /api/v1/me/appointments` | `{"slotId":"uuid"}` | الموعد المحجوز (`201`) |
| `GET /api/v1/me/appointments` | - | قائمة مواعيد المستخدم |
| `POST /api/v1/me/appointments/{appointmentId}/cancel` | - | الموعد بعد أن تصبح حالته `CANCELLED` |

قيم `serviceType`: `PASSPORT`, `NATIONAL_IDENTITY`, `BIRTH_CERTIFICATE`.

الـslot يرجع: `id`, `serviceType`, `officeName`, `startsAt`, `endsAt`, `capacity`, `reservedCount`, `active`.  
الموعد يرجع: `id`, `slotId`, `serviceType`, `officeName`, `startsAt`, `endsAt`, `status`, `bookedAt`, `cancelledAt`, `completedAt`.

## 6. الإشعارات

| الطريقة والمسار | يرجع |
|---|---|
| `GET /api/v1/me/notifications` | قائمة إشعارات: `id`, `type`, `title`, `message`, `readAt`, `createdAt` |
| `GET /api/v1/me/notifications/unread-count` | `{"count": 3}` |
| `POST /api/v1/me/notifications/{notificationId}/read` | الإشعار بعد وضع `readAt` |
| `POST /api/v1/me/notifications/read-all` | `{"updatedCount": 3}` |

## 7. الوثائق الموجودة

| الطريقة والمسار | يرجع |
|---|---|
| `GET /api/v1/me/documents/passport` | بيانات الجواز: الرقم، الاسم، الميلاد، الإصدار، الانتهاء، مكان الإصدار وغيرها |
| `GET /api/v1/me/documents/national-identity` | بيانات الهوية: الرقم الوطني، الاسم، البطاقة، الميلاد، العنوان وغيرها |
| `GET /api/v1/me/documents/birth-certificate` | بيانات شهادة الميلاد: رقم الشهادة، بيانات الميلاد، الأب والأم والعنوان |

هذه المسارات تقرأ الوثائق المخزنة حاليًا. موافقة طلب جديد (`APPROVED`) لا تنشئ وثيقة جديدة تلقائيًا بعد.

## أخطاء مهمة

| الحالة | تصرف الـFrontend |
|---|---|
| `400` | اعرض رسالة تحقق من البيانات |
| `401` | جرّب refresh مرة واحدة، ثم امسح tokens إذا فشل |
| `403` | لا يملك المستخدم الصلاحية |
| `404` | الطلب/المرفق غير موجود أو لا يخصه |
| `409` | يوجد طلب أو موعد مفتوح، أو حالة الطلب لا تسمح بالفعل |
| `429` | انتظر قبل المحاولة مرة أخرى |
