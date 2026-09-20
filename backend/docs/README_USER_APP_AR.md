# README تطبيق المواطن (الجوال)

هذا الملف هو المرجع المستقل لصاحب تطبيق المواطن. يحتوي التسجيل وتسجيل الدخول، الملف الشخصي،
طلبات الجواز والهوية وشهادة الميلاد، المرفقات، المواعيد، الإشعارات، الوثائق ورسائل الخطأ.

> هذا الملف خاص بتطبيق المواطن فقط. لوحة الموظف والأدمن موثقة في `README_ADMIN_EMPLOYEE_AR.md`.

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
| `POST /api/v1/me/passport-requests` | body الكامل موضح أدناه | الطلب المنشأ (`201`) |
| `GET /api/v1/me/passport-requests` | - | قائمة طلبات الجواز للمستخدم |
| `GET /api/v1/me/passport-requests/{requestId}/history` | - | قائمة تغيّرات الحالة |

### الهوية الوطنية

| الطريقة والمسار | Body عند الحاجة | يرجع |
|---|---|---|
| `POST /api/v1/me/national-identity-requests` | body الكامل موضح أدناه | الطلب المنشأ (`201`) |
| `GET /api/v1/me/national-identity-requests` | - | قائمة طلبات الهوية للمستخدم |
| `GET /api/v1/me/national-identity-requests/{requestId}/history` | - | قائمة تغيّرات الحالة |

قيم `kind` للجواز والهوية: `ISSUANCE`, `RENEWAL`, `LOST`, `DAMAGED`, `DATA_CORRECTION`.
حقل `reason` مطلوب لـ `LOST`, `DAMAGED`, `DATA_CORRECTION`.

نفس شكل الـbody يُستخدم للجواز والهوية:

```json
{
  "kind": "ISSUANCE",
  "reason": null,
  "beneficiaryType": "SELF",
  "dependentBirthCertificateId": null,
  "paymentReference": "PAY-2026-0001",
  "lossReportNumber": null
}
```

القواعد:

- `beneficiaryType`: إما `SELF` أو `DEPENDENT_CHILD`.
- `SELF` يتطلب أن تكون `dependentBirthCertificateId` بقيمة `null`.
- `DEPENDENT_CHILD` متاح للإصدار الأول فقط، ويتطلب معرف شهادة ميلاد تابعة يملكها صاحب الحساب.
- `paymentReference` مطلوب وبحد أقصى 128 حرفًا.
- `LOST` يتطلب `reason` و`lossReportNumber`. الأنواع الأخرى يجب أن ترسل `lossReportNumber: null`.
- تفاصيل الدفع تُخزن مشفرة ولا تظهر في سجل التدقيق.
- قبول وإصدار وثيقة `DEPENDENT_CHILD` النهائي غير مفعّل بعد؛ الباك إند يمنع إصدارها باسم الوالد ويرجع `409 DOCUMENT_ISSUANCE_DATA_UNAVAILABLE` إلى أن تضاف وثائق التابعين.
- المرفقات تُرفع منفصلة بعد إنشاء الطلب، ويجب إرسال `documentType` مع كل ملف كما هو موضح في قسم المرفقات.

- `ISSUANCE` مع وجود الوثيقة مسبقًا يرجع `409` و`code: DOCUMENT_ALREADY_EXISTS`.
- `RENEWAL`, `LOST`, `DAMAGED`, `DATA_CORRECTION` دون وثيقة موجودة يرجع `404` و`code: DOCUMENT_NOT_FOUND`.

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

يسمح للمواطن بطلب مفتوح واحد من كل `kind` في الوقت نفسه؛ لذلك يمكن أن يكون لديه `CERTIFICATE_EXTRACT` و`NEWBORN_REGISTRATION` مفتوحان معًا. تكرار النوع المفتوح نفسه يرجع `409` مع `code: OPEN_REQUEST_EXISTS`. استخراج شهادة أو تصحيحها يتطلب وجود شهادة مخزنة، وإلا يرجع `404` مع `code: DOCUMENT_NOT_FOUND`. تسجيل المولود لا يُمنع بسبب امتلاك صاحب الحساب شهادة؛ لأن الشهادة تخص الطفل.

### استجابة تاريخ الطلب

كل عنصر في التاريخ يرجع: `id`, `requestId`, `fromStatus`, `toStatus`, `reason`, `changedBy`, `changedAt`.

## 4. المرفقات

استخدم `multipart/form-data` والحقل اسمه **`file`**، وأرسل نوع المستند في query parameter باسم `documentType`. المسموح JPEG وPNG وPDF فقط، حتى 5MB، والرفع متاح فقط عندما تكون حالة الطلب `SUBMITTED`.

| العملية | الجواز | الهوية | شهادة الميلاد | يرجع |
|---|---|---|---|---|
| رفع | `POST /api/v1/me/passport-requests/{id}/attachments?documentType=PERSONAL_PHOTO` | `POST /api/v1/me/national-identity-requests/{id}/attachments?documentType=PERSONAL_PHOTO` | `POST /api/v1/me/birth-certificate-requests/{id}/attachments?documentType=PERSONAL_PHOTO` | `201` مع metadata للمرفق |
| حالة المتطلبات | `GET /api/v1/me/passport-requests/{id}/attachment-requirements` | `GET /api/v1/me/national-identity-requests/{id}/attachment-requirements` | `GET /api/v1/me/birth-certificate-requests/{id}/attachment-requirements` | `required` و`uploaded` و`missing` و`complete` |
| القائمة | `GET /api/v1/me/passport-requests/{id}/attachments` | `GET /api/v1/me/national-identity-requests/{id}/attachments` | `GET /api/v1/me/birth-certificate-requests/{id}/attachments` | قائمة metadata |
| تنزيل المحتوى | `GET /api/v1/me/passport-requests/{id}/attachments/{attachmentId}/content` | `GET /api/v1/me/national-identity-requests/{id}/attachments/{attachmentId}/content` | `GET /api/v1/me/birth-certificate-requests/{id}/attachments/{attachmentId}/content` | الملف نفسه كـbinary stream |

قيم `documentType`:

- `POPULATION_REGISTRY_EXTRACT`
- `BIRTH_CERTIFICATE_COPY`
- `PERSONAL_PHOTO`
- `PROFESSION_PROOF`
- `PAYMENT_RECEIPT`
- `OLD_DOCUMENT`
- `LOSS_OR_THEFT_REPORT`
- `DATA_CORRECTION_PROOF`
- `GUARDIANSHIP_CERTIFICATE`
- `OTHER_SUPPORTING_DOCUMENT`

Metadata المرفق: `id`, `documentType`, `fileName`, `contentType`, `sizeBytes`, `uploadedAt`.

المطلوب للجواز والهوية لصاحب الحساب:

- `ISSUANCE`: مستخرج السكان، شهادة الميلاد، الصورة، إثبات المهنة وإيصال الدفع.
- `RENEWAL` أو `DAMAGED`: شهادة الميلاد، الصورة، إثبات المهنة، إيصال الدفع والوثيقة القديمة.
- `LOST`: شهادة الميلاد، الصورة، إثبات المهنة، إيصال الدفع وبلاغ الفقد أو السرقة.
- `DATA_CORRECTION`: شهادة الميلاد، الصورة، إثبات المهنة، إيصال الدفع وإثبات التصحيح.
- إصدار وثيقة لطفل: مستخرج السكان، شهادة الميلاد، الصورة وإيصال الدفع.

قبل إظهار زر الإرسال أو المتابعة، استدعِ endpoint المتطلبات واعرض عناصر `missing`. عند محاولة الموظف بدء مراجعة طلب جواز أو هوية ناقص يرجع `409 MISSING_REQUIRED_ATTACHMENTS`. تصنيف مرفقات شهادة الميلاد مدعوم، لكن لم تُفرض عليها قائمة إلزامية حتى نعتمد متطلباتها الرسمية.

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
| `GET /api/v1/me/documents/dependent-birth-certificates` | شهادات المواليد الناتجة عن طلبات تسجيل المواليد لهذا الحساب |

هذه المسارات تقرأ أحدث إصدار مخزن. عند الموافقة على طلب الجواز أو الهوية أو مستخرج/تصحيح شهادة الميلاد يُنشئ الباك إند إصدارًا جديدًا تلقائيًا. تسجيل المولود ينشئ شهادة تابعة منفصلة حتى لا يستبدل شهادة ولي الأمر.

استجابة الجواز تطابق موديل Flutter:

```json
{
  "passportNumber": "P000001",
  "firstName": "Mohammed",
  "lastName": "Ali",
  "birthDate": "1995-03-12",
  "birthPlace": "N'Djamena",
  "issueDate": "2026-09-19",
  "expiryDate": "2036-09-19",
  "issuePlace": "N'Djamena",
  "issuingAuthority": "DG de la Police Nationale",
  "issueingAuthority": "DG de la Police Nationale",
  "profession": "Engineer",
  "nationality": "Chadian",
  "gender": "Male"
}
```

`issueingAuthority` اسم قديم مكتوب خطأ وموجود مؤقتًا للتوافق. استخدم `issuingAuthority` في الكود الجديد.

استجابة الهوية:

```json
{
  "nationalId": "CID002",
  "firstName": "Ahmed",
  "lastName": "Saleh",
  "fatherName": "Saleh Ibrahim",
  "motherName": "Aisha Saleh",
  "gender": "Male",
  "dateOfBirth": "1992-07-21",
  "dateofBirth": "1992-07-21",
  "placeOfBirth": "Moundou",
  "address": "Moundou",
  "profession": "Teacher",
  "bloodGroup": "B+",
  "cardSerial": "AA6394722",
  "issueDetails": "Moundou/2014-02-11",
  "dateOfExpiry": "2024-02-11"
}
```

`dateofBirth` اسم قديم موجود مؤقتًا للتوافق. استخدم `dateOfBirth` في الموديل الجديد. لا ترسل الرقم الوطني في URL؛ هذه المسارات تستخرج صاحب الوثيقة من access token.

## أخطاء مهمة

كل الأخطاء ترجع الشكل نفسه: `code`, `message`, `status`, `timestamp`، وقد يظهر `fieldErrors` مع أخطاء الحقول.

| `code` | HTTP | المعنى |
|---|---:|---|
| `UNAUTHORIZED` | `401` | التوكن مفقود أو منتهي أو غير صالح |
| `FORBIDDEN` | `403` | المستخدم لا يملك الصلاحية |
| `RESOURCE_NOT_FOUND` | `404` | الطلب أو المرفق غير موجود أو لا يخص المستخدم |
| `DOCUMENT_NOT_FOUND` | `404` | الوثيقة المطلوبة غير موجودة |
| `DOCUMENT_ALREADY_EXISTS` | `409` | محاولة إصدار وثيقة موجودة مسبقًا |
| `OPEN_REQUEST_EXISTS` | `409` | يوجد طلب مفتوح متعارض |
| `REQUEST_ATTACHMENTS_CLOSED` | `409` | لا يمكن الرفع بعد بدء المراجعة |
| `SELF_REVIEW_NOT_ALLOWED` | `409` | الموظف يحاول مراجعة طلبه الشخصي |
| `REQUEST_NOT_AWAITING_REVIEW` | `409` | الطلب ليس بانتظار المراجعة |
| `REQUEST_NOT_UNDER_REVIEW` | `409` | الطلب ليس قيد المراجعة |
| `REQUEST_REVIEWER_MISMATCH` | `409` | الموظف الحالي ليس من بدأ المراجعة |
| `REJECTION_REASON_REQUIRED` | `400` | سبب الرفض مطلوب |
| `ATTACHMENT_INVALID` | `400` | المرفق غير صالح |
| `REQUEST_SUBMISSIONS_DISABLED` | `503` | الأدمن أوقف استقبال الطلبات مؤقتًا |
| `INTERNAL_SERVER_ERROR` | `500` | خطأ داخلي غير متوقع |

| الحالة | تصرف الـFrontend |
|---|---|
| `400` | اعرض رسالة تحقق من البيانات |
| `401` | جرّب refresh مرة واحدة، ثم امسح tokens إذا فشل |
| `403` | لا يملك المستخدم الصلاحية |
| `404` | الطلب/المرفق غير موجود أو لا يخصه |
| `409` | يوجد طلب أو موعد مفتوح، أو حالة الطلب لا تسمح بالفعل |
| `429` | انتظر قبل المحاولة مرة أخرى |
| `503` | اعرض رسالة التوقف المؤقت واترك المستخدم يعيد المحاولة لاحقًا |

## ملاحظات حالية

- الموافقة النهائية على جواز أو هوية لطفل تابع مؤجلة؛ لا تحاول اعتبار وثيقة ولي الأمر وثيقة للطفل.
- تخزين المرفقات الإنتاجي الدائم سيُربط لاحقًا؛ لا تعتمد على الملفات التجريبية كأرشيف دائم.
- لا ترسل `citizenId` أو رقم هوية مستخدم آخر لمسارات `/api/v1/me/**`؛ الباك إند يستخرج المواطن من الـaccess token.
