# README لوحة الأدمن والموظفين (الويب)

> للقراءة المنسقة داخل VS Code افتح معاينة Markdown بهذا الاختصار:

```text
Ctrl + Shift + V
```

هذا الملف هو المرجع المستقل لصاحب واجهة الويب الإدارية. يحتوي تسجيل دخول الموظف، الصلاحيات،
إدارة الموظفين والطلبات والمواطنين والمواعيد والإشعارات والإعدادات والوثائق وسجل التدقيق.

> هذا الملف خاص بلوحة الويب. تطبيق المواطن موثق في ‎`README_USER_APP_AR.md`‎.

يغطي هذا الدليل الدخول الآمن، إدارة الموظفين والصلاحيات، لوحة التحكم والطلبات الموحدة ودورة الوثائق الإضافية.

## إنشاء أول أدمن على السيرفر

لا يوجد endpoint عام لإنشاء أدمن. عند أول تشغيل فقط أضف الأسرار التالية إلى بيئة الاستضافة:

```text
APP_BOOTSTRAP_ADMIN_ENABLED=true
APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com
APP_BOOTSTRAP_ADMIN_PASSWORD=StrongAdminPassword123!
APP_BOOTSTRAP_ADMIN_FIRST_NAME=Admin
APP_BOOTSTRAP_ADMIN_LAST_NAME=User
```

بعد تشغيل التطبيق ونجاح تسجيل دخول الأدمن، غيّر ‎`APP_BOOTSTRAP_ADMIN_ENABLED`‎ إلى ‎`false`‎. لا تضع كلمة المرور داخل Git أو ملفات المشروع.

الأدمن والموظف يسجلان الدخول من نفس endpoint الحالي:

```http
POST /api/v1/auth/login
Content-Type: application/json

{"email":"admin@example.com","password":"StrongAdminPassword123!"}
```

## صلاحيات الموظف

الصلاحيات ترجع في ‎`permissions`‎ داخل بيانات الموظف وتُطبق فعلياً في الباك إند. أهمها:

- ‎`REQUEST_VIEW`‎, ‎`REQUEST_REVIEW`‎, ‎`REQUEST_APPROVE`‎, ‎`REQUEST_REJECT`‎
- ‎`CITIZEN_VIEW`‎, ‎`CITIZEN_EDIT`‎
- ‎`APPOINTMENT_VIEW`‎, ‎`APPOINTMENT_MANAGE`‎
- ‎`STAFF_VIEW`‎, ‎`STAFF_MANAGE`‎
- صلاحيات وثائق الهوية والجواز وشهادة الميلاد، والإشعارات والتقارير والتدقيق والإعدادات

إذا عدّل الأدمن صلاحيات موظف، تُلغى صلاحية access tokens والجلسات القديمة لذلك الموظف ويجب أن يسجل دخوله من جديد.

## إدارة الموظفين — للأدمن

كل المسارات التالية تحتاج ‎`Authorization: Bearer <accessToken>`‎ ودور ‎`ADMIN`‎.

### إنشاء موظف

```http
POST /api/v1/admin/staff-accounts
```

```json
{
  "email": "employee@example.com",
  "password": "TemporaryPassword123!",
  "firstName": "Ahmed",
  "lastName": "Ali",
  "phoneNumber": "+23599123456",
  "jobTitle": "Request reviewer",
  "permissions": ["REQUEST_VIEW", "REQUEST_REVIEW", "REQUEST_APPROVE", "REQUEST_REJECT"]
}
```

النجاح ‎`201 Created`‎. إذا حُذف حقل ‎`permissions`‎ يحصل الموظف على صلاحيات الموظف الافتراضية. لا يمكن لهذا المسار إنشاء أدمن.

### القائمة والتفاصيل

```http
GET /api/v1/admin/staff-accounts
GET /api/v1/admin/staff-accounts/{accountId}
```

### تعديل الملف والصلاحيات

```http
PATCH /api/v1/admin/staff-accounts/{accountId}
```

```json
{
  "firstName": "Ahmed",
  "lastName": "Ali",
  "phoneNumber": "+23599123456",
  "jobTitle": "Senior reviewer",
  "permissions": ["REQUEST_VIEW", "REQUEST_REVIEW", "REQUEST_APPROVE"]
}
```

النجاح ‎`204 No Content`‎، ويجب أن يعيد الموظف تسجيل الدخول.

### تفعيل أو تعطيل الموظف

```http
PATCH /api/v1/admin/staff-accounts/{accountId}/status
```

```json
{"status":"DISABLED"}
```

القيم المقبولة هنا ‎`ACTIVE`‎ أو ‎`DISABLED`‎ فقط. النجاح ‎`204`‎.

### إعادة تعيين كلمة المرور

```http
POST /api/v1/admin/staff-accounts/{accountId}/password-reset
```

```json
{"temporaryPassword":"NewTemporaryPassword123!"}
```

النجاح ‎`204`‎ ويلغي كل جلسات الموظف القديمة.

## ملف الموظف الحالي

هذه المسارات متاحة للموظف وللأدمن:

```http
GET   /api/v1/staff/me
PATCH /api/v1/staff/me
POST  /api/v1/staff/me/password
POST  /api/v1/staff/me/logout-all
```

Body تعديل الملف:

```json
{"firstName":"Ahmed","lastName":"Ali","phoneNumber":"+23599123456"}
```

Body تغيير كلمة المرور:

```json
{"currentPassword":"CurrentPassword123!","newPassword":"NewPassword123!"}
```

## ملاحظات للفرونت إند

- أخفِ الزر عندما لا تحتوي ‎`permissions`‎ على الصلاحية المطلوبة، لكن لا تعتمد على الإخفاء كحماية؛ الباك إند يعيد ‎`403 FORBIDDEN`‎ أيضاً.
- اعتبر كل status من ‎`200`‎ إلى ‎`299`‎ نجاحاً، خصوصاً ‎`201`‎ و‎`204`‎.
- عند ‎`401`‎ جرّب refresh مرة واحدة، وعند ‎`403`‎ اعرض رسالة عدم امتلاك الصلاحية.
- لا تسجل access token أو refresh token أو كلمات المرور في logs.

## Dashboard والطلبات الموحدة

### إحصائيات لوحة التحكم

```http
GET /api/v1/operations/dashboard
```

تحتاج صلاحية ‎`DASHBOARD_VIEW`‎. مثال مختصر للاستجابة:

```json
{
  "totalRequests": 25,
  "submitted": 8,
  "underReview": 4,
  "approved": 10,
  "rejected": 3,
  "submittedToday": 2,
  "requestsByService": {
    "PASSPORT": 9,
    "NATIONAL_IDENTITY": 8,
    "BIRTH_CERTIFICATE": 8
  },
  "requestsByStatus": {
    "SUBMITTED": 8,
    "UNDER_REVIEW": 4,
    "APPROVED": 10,
    "REJECTED": 3
  },
  "recentRequests": []
}
```

‎`recentRequests`‎ تحتوي آخر 10 طلبات من كل الخدمات.

### قائمة كل الطلبات

```http
GET /api/v1/operations/requests?page=0&size=20
```

تحتاج صلاحية ‎`REQUEST_VIEW`‎. معاملات التصفية اختيارية:

- ‎`serviceType`‎: إحدى ‎`PASSPORT`‎, ‎`NATIONAL_IDENTITY`‎, ‎`BIRTH_CERTIFICATE`‎.
- ‎`status`‎: إحدى ‎`SUBMITTED`‎, ‎`UNDER_REVIEW`‎, ‎`APPROVED`‎, ‎`REJECTED`‎.
- ‎`query`‎: بحث برقم الطلب أو الرقم الوطني أو نوع الخدمة أو نوع الطلب أو الحالة.
- ‎`submittedFrom`‎ و‎`submittedTo`‎: تاريخ ووقت ISO-8601، مثل ‎`2026-09-01T00:00:00Z`‎.
- ‎`page`‎: يبدأ من صفر.
- ‎`size`‎: من 1 إلى 100.

مثال:

```http
GET /api/v1/operations/requests?serviceType=PASSPORT&status=SUBMITTED&query=CID002&page=0&size=20
```

الاستجابة:

```json
{
  "content": [
    {
      "id": "request-uuid",
      "serviceType": "PASSPORT",
      "kind": "ISSUANCE",
      "status": "SUBMITTED",
      "citizenId": "citizen-uuid",
      "citizenNationalId": "CID002",
      "requestReason": null,
      "submissionDetails": {
        "beneficiaryType": "SELF",
        "dependentBirthCertificateId": null,
        "paymentReference": "PAY-2026-0001",
        "lossReportNumber": null
      },
      "reviewedBy": null,
      "submittedAt": "2026-09-19T08:00:00Z",
      "reviewedAt": null,
      "decisionReason": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

طلبات الجواز والهوية الجديدة ترجع ‎`submissionDetails`‎، وتشمل المستفيد ومرجع الدفع ورقم بلاغ الفقد عند الحاجة. الطلبات القديمة قد ترجع ‎`submissionDetails: null`‎. تُخزّن هذه التفاصيل مشفّرة ولا يُكتب مرجع الدفع في سجل التدقيق.

إذا كان ‎`beneficiaryType`‎ يساوي ‎`DEPENDENT_CHILD`‎ فترجع ‎`dependentBirthCertificateId`‎ لشهادة المولود التابعة لولي الأمر. إصدار وثيقة الطفل عند الموافقة مؤجل حالياً؛ يمنع الباك إند الموافقة النهائية بدل إصدار الوثيقة باسم ولي الأمر بالخطأ.

### تفاصيل طلب واحد

```http
GET /api/v1/operations/requests/{serviceType}/{requestId}
```

مثال:

```http
GET /api/v1/operations/requests/PASSPORT/550e8400-e29b-41d4-a716-446655440000
```

يرجع نفس نموذج العنصر الموجود في ‎`content`‎ مع سبب الطلب، الموظف المراجع، وقت المراجعة وسبب القرار عند وجودها.

ولعرض التسلسل الكامل لتغيّر حالة الطلب:

```http
GET /api/v1/operations/requests/{serviceType}/{requestId}/history
```

يرجع قائمة تحتوي ‎`fromStatus`‎, ‎`toStatus`‎, ‎`reason`‎, ‎`changedBy`‎, و‎`changedAt`‎ مرتبة حسب التخزين التاريخي للطلب.

### اكتمال المرفقات قبل المراجعة

قبل بدء مراجعة طلب جواز أو هوية، يستطيع الموظف فحص الملفات المطلوبة والمرفوعة والناقصة:

```http
GET /api/v1/operations/passport-requests/{requestId}/attachment-requirements
GET /api/v1/operations/national-identity-requests/{requestId}/attachment-requirements
GET /api/v1/operations/birth-certificate-requests/{requestId}/attachment-requirements
```

مثال:

```json
{
  "required": ["BIRTH_CERTIFICATE_COPY", "PERSONAL_PHOTO", "PROFESSION_PROOF", "PAYMENT_RECEIPT", "OLD_DOCUMENT"],
  "uploaded": ["BIRTH_CERTIFICATE_COPY", "PERSONAL_PHOTO"],
  "missing": ["PROFESSION_PROOF", "PAYMENT_RECEIPT", "OLD_DOCUMENT"],
  "complete": false
}
```

قائمة المرفقات ترجع ‎`documentType`‎ مع بيانات الملف. لا يمكن بدء مراجعة طلب جواز أو هوية عندما تكون ‎`complete=false`‎، ويرجع الباك إند ‎`409`‎ و‎`code: MISSING_REQUIRED_ATTACHMENTS`‎. شهادة الميلاد تدعم تصنيف الملفات، لكن لا توجد لها قائمة إلزامية حتى اعتماد متطلباتها الرسمية.

## طلب وثائق إضافية من المواطن

تُستخدم هذه الدورة عندما يبدأ الموظف مراجعة الطلب ثم يكتشف أن بعض الوثائق ناقصة. تعمل مع الخدمات الثلاث:
‎`PASSPORT`‎ و‎`NATIONAL_IDENTITY`‎ و‎`BIRTH_CERTIFICATE`‎.

### 1. الموظف يحدد الوثائق الناقصة

```http
POST /api/v1/operations/requests/{serviceType}/{requestId}/additional-documents
Content-Type: application/json
```

تحتاج صلاحية ‎`REQUEST_ADDITIONAL_DOCUMENTS`‎. يجب أن يكون الطلب الأصلي بحالة ‎`UNDER_REVIEW`‎ وأن يكون الموظف الحالي هو الموظف الذي بدأ مراجعته.

```json
{
  "message": "يرجى رفع النسخ الواضحة التالية",
  "requiredDocuments": [
    "نسخة شهادة الميلاد",
    "صورة شخصية 4x4"
  ]
}
```

النجاح ‎`201 Created`‎، ويُنشأ إشعار للمواطن. أسماء الوثائق هنا هي القيم نفسها التي يجب أن يرسلها تطبيق المواطن في ‎`documentName`‎.

### 2. المواطن يعرض الطلب ويرفع الملفات

```http
GET /api/v1/me/additional-document-requests
```

حالات طلب الوثائق:

- ‎`REQUESTED`‎: ينتظر رفع المواطن.
- ‎`SUBMITTED`‎: أرسل المواطن كل الوثائق وينتظر الموظف.
- ‎`RESOLVED`‎: راجع الموظف الوثائق وأغلق الطلب.

رفع كل ملف يكون بصيغة ‎`multipart/form-data`‎:

```http
POST /api/v1/me/additional-document-requests/{additionalRequestId}/attachments
```

الحقول:

- ‎`documentName`‎: اسم مطابق تمامًا لعنصر من ‎`requiredDocuments`‎.
- ‎`file`‎: ملف JPEG أو PNG أو PDF، وبحد أقصى 5MB.

بعد رفع ملف واحد على الأقل لكل اسم مطلوب:

```http
POST /api/v1/me/additional-document-requests/{additionalRequestId}/submit
```

يرجع ‎`200 OK`‎ وحالة ‎`SUBMITTED`‎. إذا بقيت وثيقة بلا ملف يرجع ‎`409 ADDITIONAL_DOCUMENTS_INCOMPLETE`‎.

يمكن للمواطن عرض الملفات أو تنزيلها:

```http
GET /api/v1/me/additional-document-requests/{additionalRequestId}/attachments
GET /api/v1/me/additional-document-requests/{additionalRequestId}/attachments/{attachmentId}/content
```

### 3. الموظف يراجع الوثائق ويغلق الطلب

```http
GET /api/v1/operations/requests/{serviceType}/{requestId}/additional-documents
GET /api/v1/operations/additional-document-requests/{additionalRequestId}/attachments
GET /api/v1/operations/additional-document-requests/{additionalRequestId}/attachments/{attachmentId}/content
```

بعد فحصها:

```http
POST /api/v1/operations/additional-document-requests/{additionalRequestId}/resolve
```

النجاح ‎`200 OK`‎ وحالة ‎`RESOLVED`‎. لا يمكن قبول أو رفض الطلب الأصلي قبل إغلاق كل طلب وثائق إضافية مفتوح؛ محاولة ذلك ترجع ‎`409 ADDITIONAL_DOCUMENTS_UNRESOLVED`‎.

### أخطاء دورة الوثائق الإضافية

كلها ترجع بالشكل الموحد ‎`{code, message, status, timestamp}`‎:

| HTTP | ‎`code`‎ | المعنى |
|---:|---|---|
| ‎`400`‎ | ‎`VALIDATION_ERROR`‎ | ‎`requiredDocuments`‎ فارغة أو أحد الحقول يتجاوز الحد |
| ‎`400`‎ | ‎`INVALID_REQUEST`‎ | اسم الوثيقة غير مطلوب أو بيانات الرفع غير صحيحة |
| ‎`400`‎ | ‎`ATTACHMENT_INVALID`‎ | نوع الملف أو حجمه أو توقيعه غير مسموح |
| ‎`403`‎ | ‎`FORBIDDEN`‎ | الموظف لا يملك الصلاحية |
| ‎`404`‎ | ‎`NOT_FOUND`‎ | الطلب أو الملف غير موجود/لا يخص المواطن |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_ALREADY_REQUESTED`‎ | يوجد طلب وثائق مفتوح لنفس الطلب |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_INCOMPLETE`‎ | لم يُرفع ملف لكل وثيقة مطلوبة |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_UPLOAD_CLOSED`‎ | انتهت مرحلة الرفع |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_NOT_AWAITING_UPLOAD`‎ | طلب الوثائق ليس بحالة ‎`REQUESTED`‎ |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_NOT_SUBMITTED`‎ | المواطن لم يرسل الوثائق بعد |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_UNRESOLVED`‎ | لا يمكن اتخاذ القرار قبل إغلاق الوثائق |
| ‎`409`‎ | ‎`ADDITIONAL_DOCUMENTS_REVIEWER_MISMATCH`‎ | الموظف الحالي ليس مراجع الطلب |

## دليل المواطنين

### قائمة المواطنين والبحث

```http
GET /api/v1/operations/citizens?page=0&size=20
```

تحتاج صلاحية ‎`CITIZEN_VIEW`‎. معاملات التصفية:

- ‎`query`‎: يبحث في الاسم، الرقم الوطني، البريد، الهاتف أو UUID الخاص بالمواطن.
- ‎`status`‎: حالة حساب المواطن مثل ‎`ACTIVE`‎ أو ‎`DISABLED`‎.
- ‎`page`‎: يبدأ من صفر.
- ‎`size`‎: من 1 إلى 100.

مثال:

```http
GET /api/v1/operations/citizens?query=CID002&status=ACTIVE&page=0&size=20
```

```json
{
  "content": [
    {
      "citizenId": "citizen-uuid",
      "accountId": "account-uuid",
      "nationalId": "CID002",
      "fullName": "Mohammed Ali",
      "email": "citizen@example.com",
      "phoneNumber": "+23599123456",
      "phoneVerified": true,
      "accountStatus": "ACTIVE",
      "createdAt": "2026-09-19T08:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

قد تكون ‎`accountId`‎ أو معلومات الحساب فارغة للمواطنين المرحّلين الذين لا يملكون حساب تطبيق بعد. الاسم يُؤخذ من الهوية، ثم الجواز، ثم شهادة الميلاد حسب المتاح.

### ملف المواطن الكامل

```http
GET /api/v1/operations/citizens/{citizenId}
```

تحتاج صلاحية ‎`CITIZEN_VIEW`‎. الاستجابة تجمع في طلب واحد:

- بيانات المواطن والحساب.
- ‎`nationalIdentity`‎ و‎`passport`‎ و‎`birthCertificate`‎، وتكون قيمة الوثيقة ‎`null`‎ إذا لم توجد.
- ‎`requests`‎ لكل خدماته مرتبة من الأحدث.
- ‎`appointments`‎ مرتبة حسب وقت الموعد.

### تفعيل أو تعطيل حساب المواطن

```http
PATCH /api/v1/operations/citizens/{citizenId}/status
Content-Type: application/json

{"status":"DISABLED"}
```

تحتاج صلاحية ‎`CITIZEN_EDIT`‎. القيم المقبولة ‎`ACTIVE`‎ أو ‎`DISABLED`‎ فقط، والنجاح ‎`200 OK`‎. تعطيل الحساب يمنع المواطن من استخدام توكناته السابقة لأن نسخة الصلاحية تتغير، وتُسجّل العملية في سجل التدقيق.

لا يسمح هذا المسار بتعديل الرقم الوطني أو الوثائق الرسمية أو وضع رقم هاتف غير متحقق؛ هذه البيانات تحتاج إجراءات تحقق منفصلة.

## التقارير وسجل التدقيق

### تقرير الطلبات

```http
GET /api/v1/operations/reports/requests?from=2026-09-01T00:00:00Z&to=2026-09-30T23:59:59Z
```

يحتاج صلاحية ‎`REPORT_VIEW`‎. التاريخان اختياريان ويحددان فترة حسب وقت تقديم الطلب. ترجع الاستجابة:

```json
{
  "from": "2026-09-01T00:00:00Z",
  "to": "2026-09-30T23:59:59Z",
  "totalRequests": 42,
  "requestsByService": {
    "PASSPORT": 15,
    "NATIONAL_IDENTITY": 17,
    "BIRTH_CERTIFICATE": 10
  },
  "requestsByStatus": {
    "SUBMITTED": 5,
    "UNDER_REVIEW": 7,
    "APPROVED": 25,
    "REJECTED": 5
  },
  "submissionsByDay": {
    "2026-09-18": 8,
    "2026-09-19": 6
  },
  "averageReviewHours": 12.5
}
```

‎`averageReviewHours`‎ يحسب الطلبات التي بدأت مراجعتها فقط، ويكون صفرًا إذا لم توجد طلبات تمت مراجعتها.

### سجل التدقيق

```http
GET /api/v1/operations/audit-events?action=APPROVED&targetType=SERVICE_REQUEST&occurredFrom=2026-09-01T00:00:00Z&page=0&size=20
```

يحتاج صلاحية ‎`AUDIT_VIEW`‎. الفلاتر الاختيارية:

- ‎`actorAccountId`‎: معرف حساب منفذ العملية.
- ‎`action`‎: بحث جزئي باسم العملية، مثل ‎`APPROVED`‎ أو ‎`APPOINTMENT`‎.
- ‎`targetType`‎ و‎`targetId`‎: نوع ومعرف العنصر المتأثر.
- ‎`occurredFrom`‎ و‎`occurredTo`‎: الفترة الزمنية.
- ‎`page`‎ و‎`size`‎: تقسيم الصفحات، والحد الأقصى للحجم 100.

النتيجة صفحة موحدة، وكل عنصر يحتوي: ‎`id`‎, ‎`actorAccountId`‎, ‎`action`‎, ‎`targetType`‎, ‎`targetId`‎, ‎`result`‎, ‎`metadata`‎, ‎`occurredAt`‎. السجل للقراءة فقط ولا يوجد مسار لتعديله أو حذفه.

## إدارة المواعيد

عرض المواعيد يحتاج ‎`APPOINTMENT_VIEW`‎، أما الإنشاء والتعديل والإلغاء والإكمال فتحتاج ‎`APPOINTMENT_MANAGE`‎.

### إنشاء خانة موعد

```http
POST /api/v1/operations/appointment-slots
Content-Type: application/json

{
  "department": "PASSPORT",
  "officeName": "Main office",
  "startsAt": "2026-09-21T09:00:00Z",
  "endsAt": "2026-09-21T09:30:00Z",
  "capacity": 20
}
```

قيم ‎`department`‎: ‎`PASSPORT`‎, ‎`CIVIL_STATUS`‎. موعد ‎`CIVIL_STATUS`‎ عام للأحوال المدنية ولا يحدد نوع المعاملة. النجاح ‎`201 Created`‎.

### قائمة خانات المواعيد

```http
GET /api/v1/operations/appointment-slots?department=PASSPORT&active=true&startsFrom=2026-09-20T00:00:00Z&startsTo=2026-09-30T23:59:59Z&office=Main&page=0&size=20
```

كل الفلاتر اختيارية. ترجع الاستجابة صفحة موحدة فيها ‎`content`‎, ‎`page`‎, ‎`size`‎, ‎`totalElements`‎, ‎`totalPages`‎.

### تعديل خانة موعد

```http
PATCH /api/v1/operations/appointment-slots/{slotId}
Content-Type: application/json

{
  "department": "PASSPORT",
  "officeName": "Main office",
  "startsAt": "2026-09-21T09:00:00Z",
  "endsAt": "2026-09-21T09:30:00Z",
  "capacity": 25
}
```

إذا كان في الخانة حجوزات، يمكن تغيير السعة فقط بشرط ألا تصبح أقل من ‎`reservedCount`‎. لا يمكن تغيير الخدمة أو المكتب أو الوقت حتى تُلغى الحجوزات. كما يجب أن يبقى وقت البداية في المستقبل.

### تعطيل خانة موعد

```http
POST /api/v1/operations/appointment-slots/{slotId}/deactivate
```

لا يمكن تعطيل خانة عليها حجوزات؛ يجب إلغاء الحجوزات أولًا.

### البحث في الحجوزات

```http
GET /api/v1/operations/appointments/search?status=BOOKED&department=PASSPORT&startsFrom=2026-09-20T00:00:00Z&startsTo=2026-09-30T23:59:59Z&query=CID002&page=0&size=20
```

كل الفلاتر اختيارية. ‎`query`‎ يبحث بالرقم الوطني أو معرف المواطن أو معرف الموعد أو اسم المكتب. حالات الموعد: ‎`BOOKED`‎, ‎`CANCELLED`‎, ‎`COMPLETED`‎.

كل عنصر يرجع:

- معرف الموعد والمواطن والخانة: ‎`id`‎, ‎`citizenId`‎, ‎`slotId`‎.
- ‎`citizenNationalId`‎, ‎`department`‎, ‎`officeName`‎, ‎`startsAt`‎, ‎`endsAt`‎.
- ‎`status`‎, ‎`bookedAt`‎, ‎`cancelledAt`‎, ‎`completedAt`‎, ‎`completedBy`‎.

المسار القديم التالي ما زال متاحًا للتوافق، لكنه يرجع قائمة غير مقسمة إلى صفحات لحالة واحدة:

```http
GET /api/v1/operations/appointments?status=BOOKED
```

### إلغاء أو إكمال حجز

```http
POST /api/v1/operations/appointments/{appointmentId}/cancel
POST /api/v1/operations/appointments/{appointmentId}/complete
```

إلغاء الموظف يعيد المقعد إلى الخانة ويرسل إشعارًا للمواطن. الإكمال يغيّر الحالة إلى ‎`COMPLETED`‎ ويسجل الموظف المنفذ. لا يمكن تنفيذ انتقال غير صالح، مثل إلغاء موعد ملغى أو مكتمل.

تعارضات المواعيد ترجع:

```json
{
  "code": "APPOINTMENT_CONFLICT",
  "message": "يوجد تعارض مع الموعد المطلوب",
  "status": 409,
  "timestamp": "2026-09-19T12:00:00"
}
```

## إصدار الوثيقة عند الموافقة

لا يحتاج فرونت إند الموظف endpoint إضافيًا. نفس قرار الموافقة الحالي:

```http
POST /api/v1/operations/passport-requests/{requestId}/decision
POST /api/v1/operations/national-identity-requests/{requestId}/decision
POST /api/v1/operations/birth-certificate-requests/{requestId}/decision
Content-Type: application/json

{"approved":true,"reason":null}
```

ينفذ الباك إند الآن عمليتين داخل معاملة واحدة:

1. ينشئ نسخة مشفرة جديدة من الوثيقة برقم جديد وتاريخ إصدار جديد.
2. يغيّر حالة الطلب إلى ‎`APPROVED`‎ ويسجل السجل والإشعار.

إذا فشل إنشاء الوثيقة تتراجع العملية كلها ويبقى الطلب ‎`UNDER_REVIEW`‎.

- الإصدار الأول للجواز يعتمد على بيانات الهوية، ثم شهادة الميلاد عند عدم وجود الهوية.
- تجديد/استبدال الجواز أو الهوية ينسخ البيانات الرسمية الموجودة ويولّد رقم نسخة وتواريخ صلاحية جديدة.
- مستخرج أو تصحيح شهادة الميلاد ينشئ نسخة جديدة من شهادة المواطن.
- ‎`NEWBORN_REGISTRATION`‎ ينشئ شهادة مولود تابعة مرتبطة بولي الأمر والطلب، ولا يستبدل شهادة ميلاد ولي الأمر.

يستطيع تطبيق المواطن عرض شهادات المواليد التابعة من:

```http
GET /api/v1/me/documents/dependent-birth-certificates
```

وتظهر أيضًا في حقل ‎`dependentBirthCertificates`‎ عند طلب ملف المواطن الكامل:

```http
GET /api/v1/operations/citizens/{citizenId}
```

إذا لم تتوفر وثيقة مصدر أو بيانات رسمية كافية يرجع:

```json
{
  "code": "DOCUMENT_ISSUANCE_DATA_UNAVAILABLE",
  "message": "لا تتوفر بيانات رسمية كافية لإصدار الوثيقة",
  "status": 409,
  "timestamp": "2026-09-19T12:00:00"
}
```

## إدارة الإشعارات

هذه المسارات مخصصة للويب الإداري، وتحتاج ‎`Bearer token`‎ لموظف أو أدمن فعال.

### عرض سجل الإشعارات

```http
GET /api/v1/operations/notifications?citizenId={citizenId}&type=GENERAL&read=false&page=0&size=20
```

كل الفلاتر اختيارية، والحد الأقصى لـ‎`size`‎ هو ‎`100`‎. يحتاج المستخدم صلاحية ‎`NOTIFICATION_VIEW`‎.
الاستجابة صفحة موحدة تحتوي ‎`content`‎, ‎`page`‎, ‎`size`‎, ‎`totalElements`‎, ‎`totalPages`‎.

### إرسال إشعار يدوي

```http
POST /api/v1/operations/notifications
Content-Type: application/json

{
  "citizenId": "00000000-0000-0000-0000-000000000000",
  "type": "GENERAL",
  "title": "تحديث على طلبك",
  "message": "تم تحديث حالة طلبك، يرجى مراجعة التطبيق."
}
```

يحتاج المستخدم صلاحية ‎`NOTIFICATION_SEND`‎، ويرجع ‎`201 Created`‎. ‎`title`‎ بحد أقصى 160 حرفًا
و‎`message`‎ بحد أقصى 1000 حرف. يُسجل الإرسال في سجل التدقيق دون تخزين نص الرسالة داخله.

الأنواع المتاحة تشمل ‎`GENERAL`‎، وأنواع تحديثات الطلبات والمواعيد والمستندات الإضافية الموجودة في ‎`NotificationType`‎.

إذا كان المواطن غير موجود يرجع ‎`404 RESOURCE_NOT_FOUND`‎. وإذا أوقف الأدمن الإشعارات يرجع:

```json
{
  "code": "NOTIFICATIONS_DISABLED",
  "message": "إرسال الإشعارات متوقف مؤقتًا",
  "status": 503,
  "timestamp": "2026-09-20T08:00:00"
}
```

## إعدادات النظام

هذه المسارات للأدمن فقط، لأنها تحتاج صلاحية ‎`SETTINGS_MANAGE`‎:

```http
GET /api/v1/operations/settings
```

```http
PUT /api/v1/operations/settings
Content-Type: application/json

{
  "requestSubmissionsEnabled": true,
  "notificationsEnabled": true,
  "maintenanceMessage": null
}
```

- ‎`requestSubmissionsEnabled`‎: عند إيقافه تُرفض طلبات الجواز والهوية وشهادة الميلاد الجديدة مؤقتًا.
- ‎`notificationsEnabled`‎: عند إيقافه لا تُنشأ إشعارات آلية جديدة، كما يُمنع الإرسال اليدوي من لوحة الموظف.
- ‎`maintenanceMessage`‎: رسالة اختيارية للواجهة، وبحد أقصى 500 حرف. أرسل ‎`null`‎ لمسحها.

تعديل الإعدادات يُسجل في سجل التدقيق مع القيم السابقة والجديدة، دون نسخ نص رسالة الصيانة إلى السجل.
عند إيقاف استقبال الطلبات يرجع الباك إند:

```json
{
  "code": "REQUEST_SUBMISSIONS_DISABLED",
  "message": "استقبال الطلبات متوقف مؤقتًا",
  "status": 503,
  "timestamp": "2026-09-20T08:00:00"
}
```

## إدارة وثائق المواطن يدويًا

المسارات التالية للويب الإداري، ويستبدل ‎`{citizenId}`‎ بمعرف المواطن UUID من دليل المواطنين.
‎`GET`‎ يحتاج صلاحية العرض، و‎`POST`‎ يحتاج صلاحية الإنشاء، و‎`PUT`‎ يحتاج صلاحية التعديل الخاصة بنوع الوثيقة.

```http
GET|POST|PUT /api/v1/operations/citizens/{citizenId}/documents/passport
GET|POST|PUT /api/v1/operations/citizens/{citizenId}/documents/national-identity
GET|POST|PUT /api/v1/operations/citizens/{citizenId}/documents/birth-certificate
```

- ‎`POST`‎ يرجع ‎`201 Created`‎، ويُرفض بـ‎`409 DOCUMENT_ALREADY_EXISTS`‎ إذا كانت الوثيقة موجودة.
- ‎`PUT`‎ يعدّل أحدث نسخة مشفرة ويحافظ على رقمها إذا أرسل الفرونت نفس الرقم.
- ‎`GET`‎ و‎`PUT`‎ يرجعان ‎`404 DOCUMENT_NOT_FOUND`‎ عندما لا توجد الوثيقة.
- تكرار رقم وثيقة تابع لمواطن آخر يرجع ‎`409 DOCUMENT_ALREADY_EXISTS`‎.
- كل إنشاء أو تعديل يسجل الموظف والعملية في سجل التدقيق، دون نسخ البيانات الشخصية إلى سجل التدقيق.

Body الجواز:

```json
{
  "passportNumber": "P000001",
  "firstName": "Ahmed",
  "lastName": "Ali",
  "dateOfBirth": "1995-03-12",
  "placeOfBirth": "N'Djamena",
  "issuedOn": "2026-09-20",
  "expiresOn": "2036-09-20",
  "placeOfIssue": "N'Djamena",
  "issuingAuthority": "DG de la Police Nationale",
  "profession": "Engineer",
  "nationality": "Chadian",
  "sex": "Male"
}
```

Body الهوية:

```json
{
  "nationalId": "CID002",
  "firstName": "Ahmed",
  "lastName": "Ali",
  "gender": "Male",
  "placeOfBirth": "N'Djamena",
  "dateOfBirth": "1995-03-12",
  "cardSerial": "AA123456",
  "placeOfIssue": "N'Djamena",
  "issuedOn": "2026-09-20",
  "expiresOn": "2036-09-20",
  "profession": "Engineer",
  "fatherName": "Omar Ali",
  "motherName": "Fatma Ali",
  "address": "N'Djamena",
  "bloodGroup": "A+"
}
```

‎`nationalId`‎ يجب أن يطابق رقم المواطن الموجود أصلًا في سجل المواطنين؛ لا يستطيع الموظف نقله إلى هوية مختلفة عبر هذا المسار.

Body شهادة الميلاد:

```json
{
  "certificateNumber": "BC00001",
  "fullName": "Ahmed Ali",
  "gender": "Male",
  "birthDate": "1995-03-12",
  "birthPlace": "N'Djamena",
  "fatherName": "Omar Ali",
  "fatherBirthDate": "1970-05-10",
  "fatherBirthPlace": "N'Djamena",
  "fatherProfession": "Teacher",
  "motherName": "Fatma Ali",
  "motherBirthDate": "1975-03-12",
  "motherBirthPlace": "Moundou",
  "motherProfession": "Nurse",
  "declarationDate": "1995-03-13",
  "address": "N'Djamena"
}
```

تواريخ الإصدار والانتهاء مطلوبة للجواز والهوية، ويجب أن يكون الانتهاء بعد الإصدار. تاريخ التصريح في شهادة الميلاد لا يمكن أن يسبق تاريخ الميلاد.
