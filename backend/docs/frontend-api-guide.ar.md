# دليل الـ Frontend — Chari API

هذا الملف هو عقد العمل الحالي بين الـFrontend والـBackend. استخدم فقط المسارات المذكورة هنا؛ المسارات القديمة محجوبة ولا يجب استهلاكها.

## 1. الإعداد الأساسي

### عنوان الـAPI المنشور للتجربة

```text
https://chari-api.onrender.com
```

استخدمه كـBase URL في تطبيق الجوال. قد يتأخر أول طلب بعد فترة خمول بسبب الخطة المجانية في Render.

### عنوان الـAPI المحلي

```text
http://localhost:8080
```

كل body يُرسل بصيغة JSON مع header:

```http
Content-Type: application/json
```

عند تشغيل Flutter كويب محليًا يسمح الـBackend بعنوان تطوير ديناميكي:

```text
http://localhost:*
```

الخدمة المنشورة على Render مهيأة أيضًا لاختبار `localhost` من المتصفح. عند نشر واجهة ويب حقيقية، أضف domain الواجهة الصريح إلى `APP_ALLOWED_ORIGINS` في Render.

### المصادقة

بعد تسجيل الدخول يحصل العميل على:

```json
{
  "accessToken": "jwt",
  "refreshToken": "random-secret",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

أرسل Access Token في كل request محمي:

```http
Authorization: Bearer <accessToken>
```

- Access Token قصير العمر: 15 دقيقة محليًا.
- Refresh Token يُستخدم فقط مع `/api/v1/auth/refresh`.
- عند refresh يصل Token pair جديد، والـrefresh token القديم يصبح غير صالح فورًا.
- عند `401` بسبب انتهاء access token: نفّذ refresh مرة واحدة ثم أعد الطلب الأصلي. إن فشل refresh، امسح الجلسة وانقل المستخدم إلى صفحة login.
- لا تطبع tokens في console ولا تضعها في URL أو logs أو analytics.

في تطبيق ويب يفضّل حفظ Access Token في الذاكرة. يحتاج تخزين Refresh Token إلى قرار أمني مشترك لاحقًا؛ لا تضعه في `localStorage` في production بدون مراجعة أمنية.

## 2. الأدوار والصلاحيات

| Role | المسارات المتاحة |
|---|---|
| غير مسجل | health، enrollment، login، refresh، logout |
| `CITIZEN` | `/api/v1/me/**` |
| `EMPLOYEE` | `/api/v1/operations/**` |
| `ADMIN` | operations و`/api/v1/admin/**` |

لا يرسل الـFrontend رقمًا وطنيًا أو `citizenId` للوصول إلى بيانات المواطن الشخصية؛ الـBackend يستخرج هوية المواطن من JWT.

## 3. التسجيل عبر OTP

### 3.1 طلب OTP

```http
POST /auth/enrollment/otp
```

```json
{
  "nationalId": "123456789",
  "phoneNumber": "+23599123456",
  "email": "citizen@example.com"
}
```

نجاح متوقع — `202 Accepted`:

```json
{
  "challengeId": "bf70abbf-d403-486f-af99-5d49c02ba969"
}
```

ملاحظات UI:

- أرسل الهاتف بصيغة دولية E.164، مثل `+23599123456`.
- يُفحص تكرار البريد هنا ويُفحص مرة أخرى عند إنشاء الحساب، ولا يُحفظ الهاتف كرقم موثّق إلا بعد نجاح OTP.
- لا تعتبر `202` إثباتًا أن الرقم الوطني موجود أو البريد متاح؛ الاستجابة متعمدة أن تكون متشابهة عند عدم إمكانية التسجيل.
- في بيئة التطوير وعلى Render حاليًا يظهر OTP فقط في Logs؛ لم يتم ربط مزود SMS حقيقي بعد.
- اطلب من المستخدم إدخال رمز من ستة أرقام.
- يجب تقييد زر إعادة الإرسال في الواجهة؛ الـBackend لديه rate limit أيضًا.

### 3.2 التحقق من OTP

```http
POST /auth/enrollment/otp/verify
```

```json
{
  "challengeId": "bf70abbf-d403-486f-af99-5d49c02ba969",
  "code": "843361"
}
```

النجاح: `204 No Content`.

بعد النجاح انتقل مباشرة إلى إنشاء الحساب. الرمز قصير العمر، له حد للمحاولات، ولا يمكن استخدامه مرتين.

### 3.3 إنشاء حساب مواطن

```http
POST /auth/enrollment/accounts
```

```json
{
  "challengeId": "bf70abbf-d403-486f-af99-5d49c02ba969",
  "email": "citizen@example.com",
  "password": "StrongPassword123!"
}
```

النجاح — `201 Created`:

```json
{
  "accountId": "36d94a14-5a05-4a64-9163-ba2b3119d00f"
}
```

شروط الإدخال:

- البريد الإلكتروني صالح وطوله حتى 254 حرفًا.
- كلمة المرور من 12 إلى 128 حرفًا على الأقل. اعرض متطلبات كلمة المرور في الواجهة.

بعد إنشاء الحساب، وجّه المستخدم إلى login؛ لا يصدر هذا endpoint tokens.

## 4. المصادقة والجلسة

### 4.1 تسجيل الدخول

```http
POST /api/v1/auth/login
```

```json
{
  "nationalId": "CID001",
  "password": "StrongPassword123!"
}
```

النجاح: `200 OK` مع Token pair. فشل الرقم الوطني/كلمة المرور يرجع `401` برسالة عامة؛ لا تحاول تمييز سبب الفشل في الواجهة. الحروف غير حساسة للحالة، وتزال المسافات والشرطات عند المطابقة. البريد يبقى مطلوبًا عند إنشاء الحساب. دخول البريد القديم مدعوم للتوافق؛ أرسل `nationalId` أو `email` وليس كليهما.

### 4.2 تجديد الجلسة

```http
POST /api/v1/auth/refresh
```

```json
{
  "refreshToken": "..."
}
```

النجاح: `200 OK` مع Token pair جديد.

### 4.3 تسجيل الخروج

```http
POST /api/v1/auth/logout
```

```json
{
  "refreshToken": "..."
}
```

النجاح: `204 No Content`. بعده امسح الـtokens محليًا وانتقل إلى login.

## 5. واجهة المواطن

كل المسارات التالية تتطلب role `CITIZEN` وBearer token.

### 5.1 الوثائق

| Method | Endpoint | النتيجة |
|---|---|---|
| `GET` | `/api/v1/me/documents/passport` | بيانات جواز المواطن الحالي |
| `GET` | `/api/v1/me/documents/national-identity` | بيانات الهوية الوطنية للمواطن الحالي |
| `GET` | `/api/v1/me/documents/birth-certificate` | بيانات شهادة الميلاد للمواطن الحالي |

مثال جواز:

```json
{
  "passportNumber": "P123456",
  "firstName": "Ahmed",
  "lastName": "Ali",
  "dateOfBirth": "1990-01-15",
  "placeOfBirth": "N'Djamena",
  "issuedOn": "2025-01-01",
  "expiresOn": "2030-01-01",
  "placeOfIssue": "N'Djamena",
  "issuingAuthority": "...",
  "profession": "...",
  "nationality": "...",
  "sex": "M"
}
```

إن لم توجد الوثيقة يرجع `404`. هذه المرحلة لا تتضمن رفع مرفقات أو إصدار وثيقة جديدة بعد.

### 5.2 إنشاء طلب جواز

```http
POST /api/v1/me/passport-requests
Authorization: Bearer <accessToken>
```

أرسل نوع الطلب، ومعه سبب عند الحاجة:

```json
{ "kind": "ISSUANCE", "reason": null }
```

النجاح — `201 Created`:

```json
{
  "id": "cd4166d7-275d-4418-b965-1fbba364f0c4",
  "citizenId": { "value": "uuid" },
  "status": "SUBMITTED",
  "reviewedBy": null,
  "submittedAt": "2026-08-29T08:11:26Z",
  "reviewedAt": null,
  "decisionReason": null
}
```

قواعد UI:

- يسمح النظام بطلب جواز مفتوح واحد فقط للمواطن.
- إذا كان هناك طلب `SUBMITTED` أو `UNDER_REVIEW` يرجع `409 Conflict`.
- بعد `APPROVED` أو `REJECTED` يمكن إنشاء طلب جديد.

### 5.3 قائمة طلباتي

```http
GET /api/v1/me/passport-requests
```

ترجع array من نفس شكل طلب الجواز، مرتبة من الأحدث.

### 5.4 سجل طلب جواز

```http
GET /api/v1/me/passport-requests/{requestId}/history
```

مثال response:

```json
[
  {
    "id": "uuid",
    "requestId": "uuid",
    "fromStatus": null,
    "toStatus": "SUBMITTED",
    "reason": null,
    "changedBy": { "value": "uuid" },
    "changedAt": "2026-08-29T08:11:26Z"
  },
  {
    "fromStatus": "SUBMITTED",
    "toStatus": "UNDER_REVIEW",
    "reason": null,
    "changedAt": "2026-08-29T08:12:19Z"
  }
]
```

اعرض الحالات كـtimeline. لا تعرض UUIDs للمستخدم النهائي.

### 5.5 مسارات المواطن الإضافية الحالية

| المجال | المسارات المتاحة |
|---|---|
| الصفحة الرئيسية والملف الشخصي | `GET /api/v1/me/home`، `GET /api/v1/me/profile`، `PATCH /api/v1/me/profile/password`، `POST /api/v1/me/profile/logout-all` |
| طلبات الهوية الوطنية | إنشاء، قائمة، وسجل تحت `/api/v1/me/national-identity-requests` |
| طلبات شهادة الميلاد | إنشاء، قائمة، سجل، وبيانات المولود تحت `/api/v1/me/birth-certificate-requests` |
| المرفقات | رفع وقائمة مرفقات لطلبات الجواز والهوية وشهادة الميلاد |
| المواعيد | slots، حجز، قائمة، وإلغاء تحت `/api/v1/me/appointments` |
| الإشعارات | قائمة، غير المقروءة، قراءة إشعار، وقراءة الكل تحت `/api/v1/me/notifications` |

التفاصيل والـbody لكل هذه المسارات موجودة في المرجع التفصيلي: `FRONTEND_ENDPOINTS_AR.md`.

## 6. واجهة الموظف

كل المسارات التالية تتطلب `EMPLOYEE` أو `ADMIN`.

### 6.1 قائمة طلبات الجواز حسب الحالة

```http
GET /api/v1/operations/passport-requests?status=SUBMITTED
```

الحالات المدعومة حاليًا:

```text
SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
```

### 6.2 بدء مراجعة طلب

```http
POST /api/v1/operations/passport-requests/{requestId}/review
```

لا يوجد body. عند النجاح يرجع `200 OK` مع الطلب وحالته `UNDER_REVIEW` وبيانات `reviewedBy` و`reviewedAt`.

### 6.3 اتخاذ قرار

```http
POST /api/v1/operations/passport-requests/{requestId}/decision
```

موافقة:

```json
{ "approved": true }
```

رفض:

```json
{
  "approved": false,
  "reason": "Missing supporting document"
}
```

قواعد مهمة:

- فقط الموظف الذي بدأ المراجعة يستطيع اتخاذ القرار.
- الرفض يجب أن يحتوي سببًا غير فارغ.
- الموظف المرتبط بنفس المواطن لا يستطيع مراجعة طلبه الشخصي.
- `APPROVED` حاليًا قرار إداري فقط؛ لا يصدر جوازًا تلقائيًا.

## 7. واجهة الإدارة

هذه المسارات تتطلب `ADMIN` فقط.

### 7.1 إنشاء حساب موظف

```http
POST /api/v1/admin/staff-accounts
```

```json
{
  "email": "employee@example.com",
  "password": "StrongPassword123!"
}
```

النجاح — `201 Created`:

```json
{
  "accountId": "uuid",
  "status": "ACTIVE"
}
```

### 7.2 تفعيل أو تعطيل موظف

```http
PATCH /api/v1/admin/staff-accounts/{accountId}/status
```

```json
{ "status": "DISABLED" }
```

القيم المسموح بها لهذا endpoint: `ACTIVE` أو `DISABLED` فقط.

النجاح: `204 No Content`.

تعطيل الحساب يلغي فعليًا صلاحية JWT الحالي للموظف؛ على الواجهة التعامل مع `401` بتسجيل الخروج.

## 8. التحقق من هاتف مواطن بواسطة الموظف

هذه الوظيفة لواجهة عمليات الموظف، وتتطلب `EMPLOYEE` أو `ADMIN`.

### طلب رمز للهاتف

```http
POST /api/v1/operations/citizens/{citizenId}/phone-verifications
```

```json
{ "phone": "+23590000001" }
```

النجاح: `202 Accepted` مع `challengeId`.

### تأكيد الهاتف

```http
POST /api/v1/operations/citizens/{citizenId}/phone-verifications/{challengeId}/confirm
```

```json
{ "code": "123456" }
```

النجاح: `204 No Content`. الرقم يجب أن يكون بتنسيق E.164، مثل `+23590000001`.

## 9. الأخطاء المتوقعة

أخطاء التحقق من حقول التسجيل ترجع بشكل منظّم:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "بيانات الطلب غير صحيحة",
  "status": 400,
  "timestamp": "2026-09-17T...",
  "fieldErrors": {
    "phoneNumber": "رقم الهاتف مطلوب بالصيغة الدولية مثل +23599123456",
    "email": "البريد الإلكتروني غير صالح"
  }
}
```

`fieldErrors` يظهر فقط عند وجود أخطاء حقول. استخدم `code` في منطق الواجهة، واعرض `message` أو رسالة الحقل للمستخدم.

| code | المعنى |
|---|---|
| `VALIDATION_ERROR` | حقل أو أكثر ناقص أو بصيغة غير صحيحة؛ اقرأ `fieldErrors`. |
| `MALFORMED_JSON` | JSON غير صحيح أو نوع إحدى القيم غير متوقع. |
| `WEAK_PASSWORD` | كلمة المرور لا تحقق شرط 12–128 حرفًا. |
| `INVALID_OTP` | رمز التحقق خاطئ أو منتهي. |
| `ENROLLMENT_PROOF_INVALID` | إثبات التسجيل منتهي أو مستخدم؛ ابدأ طلب OTP جديدًا. |
| `NATIONAL_ID_NOT_FOUND` | رقم الهوية غير موجود (`404`). |
| `CITIZEN_ACCOUNT_EXISTS` | يوجد حساب مرتبط بهذه الهوية (`409`). |
| `EMAIL_ALREADY_EXISTS` | البريد الإلكتروني مستخدم مسبقًا (`409`). |
| `ACCOUNT_ALREADY_EXISTS` | يوجد حساب مسجل بهذه البيانات (`409`). |

> تنبيه تجريبي: الأكواد الأربعة السابقة تكشف وجود الهوية أو الحساب لتسهيل الاختبار فقط. قبل الاستخدام الحقيقي يجب إعادتها إلى استجابة عامة لا تسمح بفحص بيانات المواطنين.

| Status | التعامل في الواجهة |
|---|---|
| `400` | استخدم `code` و`fieldErrors` لعرض أخطاء الإدخال والتحقق. |
| `401` | حاول refresh مرة واحدة؛ إن فشل امسح الجلسة وانتقل إلى login. |
| `403` | أظهر شاشة/رسالة عدم امتلاك صلاحية. |
| `404` | المورد أو الوثيقة أو رقم الهوية التجريبي غير موجود. |
| `409` | اعرض رسالة التعارض، مثل حساب/بريد موجود أو طلب مفتوح. |
| `429` | أوقف إعادة المحاولة، واقرأ `Retry-After` إن وُجد، ثم فعّل الزر لاحقًا. |
| `500` | رسالة عامة مع زر إعادة المحاولة؛ لا تعرض تفاصيل الخطأ التقنية للمستخدم. |

## 10. ما لا يجب على الـFrontend فعله

- لا تستخدم المسارات القديمة: `/passport/**` و`/birth-certificate/**` و`/api/national-ids/**`؛ هي محجوبة.
- لا ترسل الرقم الوطني في endpoints الخاصة بالوثائق أو الطلبات.
- لا تعتمد على `citizenId.value` المعاد في responses كوسيلة صلاحية أو عرض للمستخدم.
- لا تحاول إنشاء أو ترقية حساب `ADMIN` من الواجهة؛ هذا غير متاح عبر API.
- استخدم endpoint المرفقات المناسب فقط عند الحاجة، وبصيغة `multipart/form-data` والحقل `file`.

## 11. بيانات التجربة

لا تستخدم هذه البيانات خارج بيئة local:

| النوع | القيمة |
|---|---|
| API base URL المحلي | `http://localhost:8080` |
| مواطن جاهز محليًا | `987654321` / `LocalPass123!` |
| موظف جاهز | `employee@local.chari.test` / `LocalPass123!` |
| Admin جاهز | `admin@local.chari.test` / `LocalPass123!` |
| مواطن للتسجيل عبر OTP | national ID: `123456789` |

لإضافة هوية الاختبار الجديدة `CID001`، شغّل النسخة الجديدة مرة مع `APP_TEST_DATA_ENROLLMENT_CITIZEN_ENABLED=true` ثم أعد الإعداد إلى `false`. ينشأ سجل المواطن فقط دون حساب أو كلمة مرور؛ أكمل تدفق OTP واختر كلمة مرور للدخول بالهوية. لا يتغير المواطن السابق `123456789` أو حسابه. حساب الاختبار السابق الموثق بالبريد `test@example.com` وكلمة المرور `1234567891011` يستخدم الآن رقمه الوطني عند الدخول من التطبيق؛ هذه ليست بيانات حساب `CID001`. لا توجد حسابات موظف أو أدمن تجريبية مستضافة.

تطبيق الجوال لا يحتاج CORS. الواجهة المحلية على المتصفح مسموحة حاليًا عبر `http://localhost:*`. عند نشر واجهة ويب حقيقية، أضف domain الواجهة إلى `APP_ALLOWED_ORIGINS` في Render.

شغّل fixtures فقط محليًا:

```text
APP_DEMO_DATA_ENABLED=true
```

تتوفر أيضًا الملفات الجاهزة للتجربة:

```text
postman/Chari-Local.postman_collection.json
postman/Chari-Local.postman_environment.json
```
