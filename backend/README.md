# دليل الـBackend لمبرمج تطبيق الجوال

هذا الدليل يشرح فقط ما يحتاجه تطبيق المواطن. لا تستخدم مسارات `operations` أو `admin` في تطبيق الجوال.

دليل المرحلة الأولى من لوحة الموظف والأدمن موجود في [`docs/ADMIN_EMPLOYEE_API_AR.md`](docs/ADMIN_EMPLOYEE_API_AR.md).

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

- طلبات `POST` و`PATCH` التي ترسل JSON تحتاج `Content-Type: application/json`.
- طلبات `GET` لا ترسل لها body.
- نجاح إنشاء الطلبات والمرفقات هو `201`، وليس `200` فقط.
- نجاح بعض العمليات دون response body يكون `204`، ويجب أن يعتبره الفرونت نجاحًا.

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

احفظ `challengeId` من استجابة `202 Accepted`. يُفحص تكرار البريد عند هذه الخطوة ويُفحص مرة أخرى عند إنشاء الحساب. لا يُحفظ الهاتف كرقم موثّق إلا بعد نجاح OTP. في الوضع التجريبي الحالي قد يرجع الباك إند أخطاء صريحة مثل `NATIONAL_ID_NOT_FOUND` أو `CITIZEN_ACCOUNT_EXISTS` أو `EMAIL_ALREADY_EXISTS`.

2. `POST /auth/enrollment/otp/verify`

```json
{ "challengeId": "uuid", "code": "123456" }
```

النجاح هو `204 No Content`. لا تنتظر body.

3. `POST /auth/enrollment/accounts`

```json
{
  "challengeId": "uuid",
  "email": "citizen@example.com",
  "password": "StrongPassword123!"
}
```

النجاح هو `201 Created` مع `accountId`. كلمة المرور من 12 إلى 128 حرفًا. لا ترسل `nationalId` أو `phoneNumber` في هذه الخطوة؛ تم ربطهما مسبقًا بـ`challengeId`.

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

مسارات الإنشاء ترجع `201 Created`. مسارات «طلباتي» و«التاريخ» هي `GET` ولا تحتاج body. مثال استجابة طلب:

```json
{
  "id": "request-uuid",
  "citizenId": { "value": "citizen-uuid" },
  "kind": "ISSUANCE",
  "requestReason": null,
  "status": "SUBMITTED",
  "reviewedBy": null,
  "submittedAt": "2026-09-18T18:00:00Z",
  "reviewedAt": null,
  "decisionReason": null
}
```

استخدم `id` لفتح التاريخ والمرفقات. لا تعرض `citizenId` ولا تعتمد عليه للصلاحيات.

### طلب الجواز أو الهوية

إصدار جديد لصاحب الحساب:

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

قيم `kind`: `ISSUANCE`, `RENEWAL`, `LOST`, `DAMAGED`, `DATA_CORRECTION`.

`reason` إلزامي في: `LOST` و`DAMAGED` و`DATA_CORRECTION`، وبحد أقصى 1000 حرف.

استخدم نفس الحقول لكل الأنواع وغيّر `kind` و`reason` فقط. في `LOST` يجب أيضًا إرسال `lossReportNumber`، مثل `POLICE-REPORT-1254`. للأنواع الأخرى أرسل `lossReportNumber: null`.

قواعد body:

- `beneficiaryType` هو `SELF` أو `DEPENDENT_CHILD`.
- الطفل يتطلب `dependentBirthCertificateId` تابعًا لصاحب الحساب، ويقبل `ISSUANCE` فقط.
- `paymentReference` إلزامي.
- تفاصيل الـbody الحساسة تُحفظ مشفرة في `request_payload`.
- المرفقات خارج هذا الـbody، وكل ملف يحتاج `documentType` عند الرفع.
- إصدار وثيقة الطفل النهائي غير مفعّل بعد؛ النظام يمنع إصدارها باسم الوالد ويرجع `DOCUMENT_ISSUANCE_DATA_UNAVAILABLE`.

قواعد مهمة:

- `ISSUANCE` مع وجود الجواز/الهوية مسبقًا يرجع `409 DOCUMENT_ALREADY_EXISTS`.
- الأنواع الأخرى دون وجود الوثيقة يرجع لها `404 DOCUMENT_NOT_FOUND`.
- وجود طلب مفتوح يمنع إنشاء طلب متعارض ويرجع `409 OPEN_REQUEST_EXISTS`.
- نجاح إنشاء الطلب يرجع `201 Created` مع بيانات الطلب وحالته `SUBMITTED`.

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

يجب إرسال التاريخ بصيغة `yyyy-MM-dd`، مثل `2026-08-20`.

استخراج شهادة موجودة:

```json
{
  "kind": "CERTIFICATE_EXTRACT",
  "reason": null,
  "newbornRegistration": null
}
```

تصحيح بيانات شهادة موجودة:

```json
{
  "kind": "DATA_CORRECTION",
  "reason": "يوجد خطأ في اسم الأم",
  "newbornRegistration": null
}
```

يُمنع تكرار طلب شهادة ميلاد مفتوح من نفس `kind` فقط. يمكن للمواطن إبقاء طلب `CERTIFICATE_EXTRACT` وطلب `NEWBORN_REGISTRATION` مفتوحين معًا. يرجع التكرار `409` مع `code: OPEN_REQUEST_EXISTS`. استخراج الشهادة أو تصحيحها يتطلب وجود شهادة مخزنة، وإلا يرجع `404 DOCUMENT_NOT_FOUND`. تسجيل مولود لا يُمنع بسبب امتلاك صاحب الحساب شهادة ميلاد، لأن الشهادة تخص الطفل.

> عند نجاح قرار `APPROVED` تُنشأ نسخة وثيقة جديدة مشفّرة ضمن المعاملة نفسها. إذا لم تتوفر بيانات رسمية كافية يفشل القرار بـ`409 DOCUMENT_ISSUANCE_DATA_UNAVAILABLE` وتبقى حالة الطلب `UNDER_REVIEW`.

## المرفقات

الجواز والهوية وشهادة الميلاد تدعم نفس النمط. استخدم `multipart/form-data` باسم الحقل **`file`**، وليس JSON، وأرسل `documentType` في رابط الرفع.

| العملية | النمط |
|---|---|
| رفع | `POST /api/v1/me/{request-path}/{requestId}/attachments?documentType=PERSONAL_PHOTO` |
| القائمة | `GET /api/v1/me/{request-path}/{requestId}/attachments` |
| حالة المطلوب والناقص | `GET /api/v1/me/{request-path}/{requestId}/attachment-requirements` |
| تنزيل الملف | `GET /api/v1/me/{request-path}/{requestId}/attachments/{attachmentId}/content` |

استبدل `{request-path}` بأحد: `passport-requests` أو `national-identity-requests` أو `birth-certificate-requests`.

- الملفات المقبولة: JPEG وPNG وPDF.
- الحد الأقصى: 5MB.
- استجابة المرفق تتضمن `documentType`، واستجابة المتطلبات تتضمن `required` و`uploaded` و`missing` و`complete`.
- لا يستطيع الموظف بدء مراجعة طلب جواز أو هوية ناقص؛ يرجع `409 MISSING_REQUIRED_ATTACHMENTS`.
- في الاستضافة اضبط `APP_ATTACHMENTS_LOCAL_ROOT` على مسار volume دائم، مثل `/data/attachments`. دون volume دائم قد تضيع الملفات عند استبدال نسخة السيرفر.
- المرفقات الموجودة قبل migration رقم 21 تُصنف تلقائيًا `OTHER_SUPPORTING_DOCUMENT`، ولذلك يجب إعادة رفع الأنواع المطلوبة للطلبات القديمة التي ما زالت `SUBMITTED`.
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
| شهادات المواليد التابعة | `GET /api/v1/me/documents/dependent-birth-certificates` |

استجابة الجواز تستخدم الحقول `passportNumber`, `firstName`, `lastName`, `birthDate`, `birthPlace`, `issueDate`, `expiryDate`, `issuePlace`, `issuingAuthority`, `profession`, `nationality`, `gender`. واستجابة الهوية تستخدم الحقول الموجودة في موديل Flutter، ومنها `dateOfBirth` و`issueDetails` و`dateOfExpiry`. توجد مؤقتًا أيضًا الأسماء القديمة `issueingAuthority` و`dateofBirth` للتوافق.

الإشعارات حاليًا داخل التطبيق عبر API؛ لا توجد Push Notifications أو SMS حقيقية بعد. OTP أيضًا مؤقت ويظهر في Render Logs فقط.

### الوثائق الإضافية المطلوبة أثناء المراجعة

إذا طلب الموظف وثائق إضافية، يتعامل تطبيق المواطن معها بهذا الترتيب:

| الغرض | الطريقة والمسار |
|---|---|
| عرض الطلبات | `GET /api/v1/me/additional-document-requests` |
| رفع ملف | `POST /api/v1/me/additional-document-requests/{id}/attachments` |
| إرسال كل الوثائق | `POST /api/v1/me/additional-document-requests/{id}/submit` |
| عرض الملفات | `GET /api/v1/me/additional-document-requests/{id}/attachments` |
| تنزيل ملف | `GET /api/v1/me/additional-document-requests/{id}/attachments/{attachmentId}/content` |

رفع الملف يستخدم `multipart/form-data` بحقل نصي `documentName` يطابق أحد عناصر `requiredDocuments` وحقل ملف اسمه `file`. الحالات هي `REQUESTED` ثم `SUBMITTED` ثم `RESOLVED`.

التفاصيل الكاملة لمسار الموظف، البودي، الصلاحيات وأكواد الأخطاء موجودة في `docs/ADMIN_EMPLOYEE_API_AR.md`.

### دليل المواطنين للموظف

| الغرض | الطريقة والمسار |
|---|---|
| القائمة والبحث | `GET /api/v1/operations/citizens` |
| الملف الكامل | `GET /api/v1/operations/citizens/{citizenId}` |
| تفعيل/تعطيل الحساب | `PATCH /api/v1/operations/citizens/{citizenId}/status` |

القائمة تدعم `query` و`status` و`page` و`size`. التفاصيل تجمع بيانات المواطن ووثائقه وطلباته ومواعيده. العرض يحتاج `CITIZEN_VIEW` وتغيير حالة الحساب يحتاج `CITIZEN_EDIT`.

### إدارة المواعيد للموظف

| الغرض | الطريقة والمسار |
|---|---|
| إنشاء خانة | `POST /api/v1/operations/appointment-slots` |
| القائمة والفلاتر | `GET /api/v1/operations/appointment-slots` |
| تعديل خانة | `PATCH /api/v1/operations/appointment-slots/{slotId}` |
| تعطيل خانة | `POST /api/v1/operations/appointment-slots/{slotId}/deactivate` |
| البحث في الحجوزات | `GET /api/v1/operations/appointments/search` |
| إلغاء حجز | `POST /api/v1/operations/appointments/{appointmentId}/cancel` |
| إكمال حجز | `POST /api/v1/operations/appointments/{appointmentId}/complete` |

القوائم تدعم الفلاتر وتقسيم النتائج إلى صفحات. لا يمكن تغيير وقت/مكتب/خدمة خانة عليها حجوزات أو تعطيلها قبل إلغاء حجوزاتها. إلغاء الموظف يعيد المقعد ويرسل إشعارًا للمواطن. التفاصيل والبودي الكاملان في `docs/ADMIN_EMPLOYEE_API_AR.md`.

### التقارير والتدقيق

| الغرض | الطريقة والمسار | الصلاحية |
|---|---|---|
| تقرير الطلبات حسب فترة | `GET /api/v1/operations/reports/requests` | `REPORT_VIEW` |
| سجل العمليات مع البحث والفلاتر | `GET /api/v1/operations/audit-events` | `AUDIT_VIEW` |

التقرير يرجع الإجمالي والتوزيع حسب الخدمة والحالة واليوم ومتوسط زمن بدء المراجعة. سجل التدقيق للقراءة فقط ويدعم الفلاتر والصفحات ولا يعرض كلمات مرور أو محتوى وثائق.

## التعامل مع الأخطاء

جميع الخدمات تستخدم الشكل نفسه:

```json
{
  "code": "OPEN_REQUEST_EXISTS",
  "message": "يوجد طلب مفتوح مسبقًا",
  "status": 409,
  "timestamp": "2026-09-18T18:00:00"
}
```

وعند أخطاء الحقول قد يظهر:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "بيانات الطلب غير صحيحة",
  "status": 400,
  "timestamp": "2026-09-18T18:00:00",
  "fieldErrors": {
    "kind": "نوع الطلب مطلوب"
  }
}
```

| HTTP | `code` | استخدامه في الفرونت |
|---:|---|---|
| `400` | `VALIDATION_ERROR` | اعرض رسائل `fieldErrors` بجانب الحقول |
| `400` | `MALFORMED_JSON` | قيمة enum أو تاريخ أو نوع حقل غير صحيح |
| `400` | `INVALID_REQUEST` | بيانات الطلب لا تحقق قواعد الخدمة |
| `400` | `BIRTH_REQUEST_VALIDATION_ERROR` | بيانات تسجيل المولود غير صحيحة |
| `400` | `REJECTION_REASON_REQUIRED` | سبب الرفض مطلوب |
| `400` | `ATTACHMENT_INVALID` | نوع/حجم/محتوى الملف غير صالح |
| `401` | `UNAUTHORIZED` | نفّذ refresh مرة واحدة، ثم سجّل الخروج إذا فشل |
| `403` | `FORBIDDEN` | أخفِ العملية أو اعرض رسالة عدم الصلاحية |
| `404` | `RESOURCE_NOT_FOUND` | الطلب أو المرفق غير موجود أو لا يخص المستخدم |
| `404` | `DOCUMENT_NOT_FOUND` | لا توجد وثيقة للتجديد/الاستخراج/التصحيح |
| `409` | `DOCUMENT_ALREADY_EXISTS` | لا يمكن إصدار وثيقة جديدة لأنها موجودة |
| `409` | `OPEN_REQUEST_EXISTS` | يوجد طلب مفتوح متعارض |
| `409` | `REQUEST_ATTACHMENTS_CLOSED` | لا يمكن رفع ملف بعد بدء المراجعة |
| `409` | `SELF_REVIEW_NOT_ALLOWED` | الموظف لا يستطيع مراجعة طلبه |
| `409` | `REQUEST_NOT_AWAITING_REVIEW` | لا يمكن بدء المراجعة من الحالة الحالية |
| `409` | `REQUEST_NOT_UNDER_REVIEW` | لا يمكن اتخاذ قرار قبل بدء المراجعة |
| `409` | `REQUEST_REVIEWER_MISMATCH` | الموظف الحالي ليس من بدأ المراجعة |
| `429` | حسب الاستجابة | انتظر واقرأ `Retry-After` قبل إعادة المحاولة |
| `500` | `INTERNAL_SERVER_ERROR` | اعرض رسالة عامة وزر إعادة المحاولة |

لا تستخدم رقم HTTP وحده لتحديد الرسالة؛ استخدم `code`. مثال Flutter:

```dart
final body = response.body.isEmpty
    ? <String, dynamic>{}
    : jsonDecode(response.body) as Map<String, dynamic>;

switch (body['code']) {
  case 'DOCUMENT_ALREADY_EXISTS':
    showMessage('الوثيقة موجودة مسبقًا');
    break;
  case 'DOCUMENT_NOT_FOUND':
    showMessage('الوثيقة غير موجودة');
    break;
  case 'OPEN_REQUEST_EXISTS':
    showMessage('يوجد طلب مفتوح مسبقًا');
    break;
  case 'UNAUTHORIZED':
    await refreshOrLogout();
    break;
  default:
    showMessage(body['message'] ?? 'تعذر إكمال العملية');
}
```

اعتبر أي status من `200` إلى `299` نجاحًا، بما فيه `201`, `202`, و`204`.

استخدم `code` في منطق الواجهة، واعرض `message` أو رسالة الحقل. في الوضع التجريبي الحالي ترجع حالات الهوية/الحساب أكواد `NATIONAL_ID_NOT_FOUND` و`CITIZEN_ACCOUNT_EXISTS` و`EMAIL_ALREADY_EXISTS` بشكل صريح. هذا يكشف معلومات من السجل ويجب إعادته إلى رسالة عامة قبل الاستخدام الحقيقي. لا تسجل كلمات المرور أو OTP أو access/refresh tokens داخل logs أو analytics.

## قواعد مهمة

- لا تبنِ واجهة الجوال على المسارات القديمة مثل `/passport/**` أو `/birth-certificate/**`؛ هذه محجوبة.
- لا ترسل `nationalId` أو `citizenId` في المسارات المحمية؛ الـBackend يحدد المستخدم من الـJWT.
- خزّن الـtokens في Secure Storage، وليس SharedPreferences أو logs.
- لا تفترض نجاح إصدار الوثيقة من مجرد أن الطلب `APPROVED`.

## مصدر بيانات المواطنين والوثائق

- جداول `person_records` و`national_identities` و`passports` و`birth_certificate` هي نموذج الاستيراد المطبّع 3NF، ولا تقرأ منها واجهات المستخدم مباشرة.
- عند تشغيل بيئة الإنتاج يُنشئ الترحيل أولًا سجلات `citizen_registry` المشفّرة للـCID الناقصة، ثم ينسخ الوثائق الناقصة إلى جداول `*_documents` المشفّرة.
- الترحيل idempotent: إعادة تشغيله لا تنشئ مواطنًا أو وثيقة مكررة.
- واجهات `/api/v1/me/documents/**` ومسار إصدار الوثائق بعد الموافقة يقرآن ويكتبان جداول `*_documents` فقط، وهي مصدر التشغيل المعتمد.
- يمكن تعطيل الترحيل بعد التأكد من اكتمال النقل عبر `APP_CITIZENS_BACKFILL_ENABLED=false` و`APP_DOCUMENTS_BACKFILL_ENABLED=false`.
