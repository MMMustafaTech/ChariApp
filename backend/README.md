# دليل الـBackend لمبرمج تطبيق الجوال

هذا الدليل يشرح فقط ما يحتاجه تطبيق المواطن. لا تستخدم مسارات `operations` أو `admin` في تطبيق الجوال.

## عناوين التشغيل

للتجربة على الخدمة المنشورة استخدم:

```text
https://chari-api.onrender.com
```

أضف مسار الـendpoint إلى هذا العنوان، ولا تضف slash إضافية في النهاية. الخدمة المجانية قد تتأخر أول طلب بعد فترة خمول.

للتشغيل المحلي استخدم `http://localhost:8080`.

- جميع البيانات JSON وبتوقيت ISO-8601 UTC، مثال: `2026-08-29T12:00:00Z`.
- أي endpoint يبدأ بـ `/api/v1/me/` يحتاج:

```http
Authorization: Bearer <accessToken>
```

بيانات الحسابات التجريبية التالية تعمل محليًا فقط عند تشغيل `APP_DEMO_DATA_ENABLED=true`.

- مواطن موجود: الرقم الوطني `987654321` / `LocalPass123!`
- مواطن للتسجيل محليًا: الرقم الوطني `123456789`، ورمز OTP يظهر في Log الـBackend.

لإضافة سجل مواطن الاختبار `CID001` شغّل النسخة الجديدة مرة مع `APP_TEST_DATA_ENROLLMENT_CITIZEN_ENABLED=true` ثم أعده إلى `false`. هذا لا ينشئ حسابًا أو كلمة مرور، ولا يعدّل المواطن السابق `123456789`. أنشئ حساب الهوية الجديدة عبر OTP واختر كلمة مرورك. OTP مؤقت ويظهر في Render Logs فقط؛ لم يتم ربط SMS حقيقية بعد.

تطبيق الجوال يستطيع استدعاء الخدمة المنشورة مباشرة. واجهة الويب تحتاج إضافة أصلها (domain) إلى إعداد CORS في Render قبل اختبار الطلبات من المتصفح.

## الدخول والجلسة

### تسجيل حساب جديد

1. `POST /auth/enrollment/otp`

```json
{
  "nationalId": "123456789",
  "phoneNumber": "+23599123456",
  "email": "citizen@example.com"
}
```

احفظ `challengeId` من الاستجابة. يُفحص تكرار البريد عند هذه الخطوة ويُفحص مرة أخرى عند إنشاء الحساب. لا يُحفظ الهاتف كرقم موثّق إلا بعد نجاح OTP. الاستجابة `202 Accepted` تكون متشابهة حتى عند عدم إمكانية التسجيل؛ لا تعتمد عليها لمعرفة إن كان الرقم الوطني أو البريد متاحًا.

2. `POST /auth/enrollment/otp/verify`

```json
{ "challengeId": "uuid", "code": "123456" }
```

3. `POST /auth/enrollment/accounts`

```json
{
  "challengeId": "uuid",
  "email": "citizen@example.com",
  "password": "StrongPassword123!"
}
```

كلمة المرور من 12 إلى 128 حرفًا.

### تسجيل الدخول والتجديد

`POST /api/v1/auth/login`

```json
{ "nationalId": "987654321", "password": "LocalPass123!" }
```

دخول المواطن بالرقم الوطني وكلمة المرور. استخدم `CID001` بعد إنشاء حسابه مع كلمة المرور التي اخترتها. البريد يبقى مطلوبًا عند إنشاء الحساب فقط. يدعم الباك اند دخول البريد القديم للتوافق؛ أرسل `nationalId` أو `email` وليس كليهما.

الاستجابة تتضمن `accessToken` و`refreshToken` و`expiresIn`. الـaccess token قصير العمر؛ عند 401 استخدم:

`POST /api/v1/auth/refresh`

```json
{ "refreshToken": "..." }
```

احفظ الـrefresh token الجديد بدل القديم فورًا، لأن القديم يصبح غير صالح. لتسجيل الخروج من الجهاز الحالي:

`POST /api/v1/auth/logout`

```json
{ "refreshToken": "..." }
```

بعد logout أو فشل refresh امسح tokens من التخزين الآمن وارجع لشاشة الدخول.

## الصفحة الرئيسية والملف الشخصي

| الغرض | الطريقة والمسار |
|---|---|
| بيانات الـHome | `GET /api/v1/me/home` |
| الملف الشخصي | `GET /api/v1/me/profile` |
| تغيير كلمة المرور | `PATCH /api/v1/me/profile/password` |
| الخروج من كل الأجهزة | `POST /api/v1/me/profile/logout-all` |

تغيير كلمة المرور:

```json
{ "currentPassword": "OldPassword123!", "newPassword": "NewPassword123!" }
```

بعد نجاح تغيير كلمة المرور أو الخروج من كل الأجهزة، امسح tokens الحالية وأظهر شاشة الدخول؛ كل الجلسات السابقة تصبح غير صالحة.

استجابة الـHome تحتوي على `unreadNotificationCount` و`openRequestCount` و`upcomingAppointment` و`recentRequests`. لا ترسل `citizenId` أو `nationalId` معها؛ الهوية تؤخذ من الـtoken.

## الطلبات

حالات كل الطلبات هي: `SUBMITTED` ثم `UNDER_REVIEW` ثم `APPROVED` أو `REJECTED`.

| الخدمة | إنشاء | طلباتي | التاريخ |
|---|---|---|---|
| الجواز | `POST /api/v1/me/passport-requests` | `GET /api/v1/me/passport-requests` | `GET /api/v1/me/passport-requests/{id}/history` |
| الهوية | `POST /api/v1/me/national-identity-requests` | `GET /api/v1/me/national-identity-requests` | `GET /api/v1/me/national-identity-requests/{id}/history` |
| شهادة الميلاد | `POST /api/v1/me/birth-certificate-requests` | `GET /api/v1/me/birth-certificate-requests` | `GET /api/v1/me/birth-certificate-requests/{id}/history` |

### طلب الجواز أو الهوية

```json
{ "kind": "ISSUANCE", "reason": null }
```

قيم `kind`: `ISSUANCE`, `RENEWAL`, `LOST`, `DAMAGED`, `DATA_CORRECTION`.

`reason` إلزامي في: `LOST` و`DAMAGED` و`DATA_CORRECTION`، وبحد أقصى 1000 حرف.

### طلب شهادة الميلاد

قيم `kind`: `NEWBORN_REGISTRATION`, `CERTIFICATE_EXTRACT`, `DATA_CORRECTION`.

عند `NEWBORN_REGISTRATION` أرسل `newbornRegistration`:

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

قيم `gender`: `MALE` أو `FEMALE`. استخدم `GET /api/v1/me/birth-certificate-requests/{id}/newborn-registration` لعرض هذه البيانات لصاحب الطلب فقط.

> `APPROVED` تعني أن الطلب تمت الموافقة عليه، لكنها لا تنشئ وثيقة حكومية جديدة تلقائيًا في هذه المرحلة.

## المرفقات

الجواز والهوية وشهادة الميلاد تدعم نفس النمط. استخدم `multipart/form-data` باسم الحقل **`file`**، وليس JSON.

| العملية | النمط |
|---|---|
| رفع | `POST /api/v1/me/{request-path}/{requestId}/attachments` |
| القائمة | `GET /api/v1/me/{request-path}/{requestId}/attachments` |
| تنزيل الملف | `GET /api/v1/me/{request-path}/{requestId}/attachments/{attachmentId}/content` |

استبدل `{request-path}` بأحد: `passport-requests` أو `national-identity-requests` أو `birth-certificate-requests`.

- الملفات المقبولة: JPEG وPNG وPDF.
- الحد الأقصى: 5MB.
- الرفع مسموح فقط ما دامت حالة الطلب `SUBMITTED`.
- Endpoint التنزيل يرجع ملفًا ثنائيًا؛ استخدم `Content-Disposition` كاسم للملف ولا تخزّنه في cache عام.

## المواعيد

| الغرض | الطريقة والمسار |
|---|---|
| المواعيد المتاحة | `GET /api/v1/me/appointment-slots?serviceType=PASSPORT` |
| حجز موعد | `POST /api/v1/me/appointments` |
| مواعيدي | `GET /api/v1/me/appointments` |
| إلغاء موعد | `POST /api/v1/me/appointments/{appointmentId}/cancel` |

حجز موعد:

```json
{ "slotId": "uuid" }
```

قيم `serviceType`: `PASSPORT` أو `NATIONAL_IDENTITY` أو `BIRTH_CERTIFICATE`.

## الإشعارات والوثائق

| الغرض | الطريقة والمسار |
|---|---|
| كل الإشعارات | `GET /api/v1/me/notifications` |
| عدد غير المقروء | `GET /api/v1/me/notifications/unread-count` |
| تعليم إشعار كمقروء | `POST /api/v1/me/notifications/{id}/read` |
| تعليم الكل كمقروء | `POST /api/v1/me/notifications/read-all` |
| جوازي المخزن | `GET /api/v1/me/documents/passport` |
| هويتي المخزنة | `GET /api/v1/me/documents/national-identity` |
| شهادة الميلاد المخزنة | `GET /api/v1/me/documents/birth-certificate` |

الإشعارات حاليًا داخل التطبيق عبر API؛ لا توجد Push Notifications أو SMS حقيقية بعد. OTP أيضًا مؤقت ويظهر في Render Logs فقط.

## التعامل مع الأخطاء

- `400`: بيانات ناقصة أو غير صالحة. أخطاء التسجيل ترجع `code` ثابتًا، وقد ترجع `fieldErrors` لكل حقل عند `VALIDATION_ERROR`.
- `401`: token مفقود/منتهي/غير صالح؛ جرّب refresh مرة واحدة ثم سجّل الخروج.
- `403`: المستخدم لا يملك الصلاحية.
- `404`: المورد غير موجود أو لا يخص المستخدم.
- `409`: تعارض في حالة الطلب أو محاولة إنشاء طلب/موعد مفتوح مكرر.
- `429`: حاول لاحقًا؛ يوجد rate limit لمسارات الدخول وOTP.

استخدم `code` في منطق الواجهة، واعرض `message` أو رسالة الحقل. تبقى حالات الهوية/الحساب برسالة `ENROLLMENT_UNAVAILABLE` عامة لحماية الخصوصية. لا تسجل كلمات المرور أو OTP أو access/refresh tokens داخل logs أو analytics.

## قواعد مهمة

- لا تبنِ واجهة الجوال على المسارات القديمة مثل `/passport/**` أو `/birth-certificate/**`؛ هذه محجوبة.
- لا ترسل `nationalId` أو `citizenId` في المسارات المحمية؛ الـBackend يحدد المستخدم من الـJWT.
- خزّن الـtokens في Secure Storage، وليس SharedPreferences أو logs.
- لا تفترض نجاح إصدار الوثيقة من مجرد أن الطلب `APPROVED`.
