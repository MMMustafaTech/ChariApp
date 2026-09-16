import 'dart:convert';
import 'dart:typed_data';

import 'package:frontend/core/api/api_client.dart';
import 'package:frontend/core/api/session_store.dart';

class ChariApi {
  ChariApi({ApiClient? client}) : _client = client ?? ApiClient();

  final ApiClient _client;

  Future<String> requestEnrollmentOtp(String nationalId) async {
    final json = await _object(await _client.request(
      'POST',
      '/auth/enrollment/otp',
      body: {'nationalId': nationalId},
    ));
    return json['challengeId'] as String;
  }

  Future<void> verifyEnrollmentOtp(String challengeId, String code) async {
    await _client.request(
      'POST',
      '/auth/enrollment/otp/verify',
      body: {'challengeId': challengeId, 'code': code},
    );
  }

  Future<void> createEnrollmentAccount({
    required String challengeId,
    required String email,
    required String password,
  }) async {
    await _client.request(
      'POST',
      '/auth/enrollment/accounts',
      body: {
        'challengeId': challengeId,
        'email': email,
        'password': password,
      },
    );
  }

  Future<SessionTokens> login(String nationalId, String password) async {
    final json = await _object(await _client.request(
      'POST',
      '/api/v1/auth/login',
      body: {'nationalId': nationalId, 'password': password},
    ));
    final tokens = SessionTokens.fromJson(json);
    SessionStore.save(tokens);
    return tokens;
  }

  Future<void> logout() async {
    final token = SessionStore.tokens?.refreshToken;
    if (token != null) {
      try {
        await _client.request(
          'POST',
          '/api/v1/auth/logout',
          body: {'refreshToken': token},
        );
      } finally {
        SessionStore.clear();
      }
    }
  }

  Future<Map<String, dynamic>> home() => _getObject('/api/v1/me/home');
  Future<Map<String, dynamic>> profile() => _getObject('/api/v1/me/profile');

  Future<void> changePassword(String currentPassword, String newPassword) async {
    await _client.request(
      'PATCH',
      '/api/v1/me/profile/password',
      authenticated: true,
      body: {
        'currentPassword': currentPassword,
        'newPassword': newPassword,
      },
    );
  }

  Future<Map<String, dynamic>> logoutAll() =>
      _postObject('/api/v1/me/profile/logout-all');

  Future<Map<String, dynamic>> passportDocument() =>
      _getObject('/api/v1/me/documents/passport');
  Future<Map<String, dynamic>> nationalIdentityDocument() =>
      _getObject('/api/v1/me/documents/national-identity');
  Future<Map<String, dynamic>> birthCertificateDocument() =>
      _getObject('/api/v1/me/documents/birth-certificate');

  Future<Map<String, dynamic>> submitPassportRequest(
    String kind, {
    String? reason,
  }) =>
      _postObject('/api/v1/me/passport-requests', {
        'kind': kind,
        'reason': reason,
      });

  Future<List<Map<String, dynamic>>> passportRequests() =>
      _getList('/api/v1/me/passport-requests');
  Future<List<Map<String, dynamic>>> passportRequestHistory(String id) =>
      _getList('/api/v1/me/passport-requests/$id/history');

  Future<Map<String, dynamic>> submitNationalIdentityRequest(
    String kind, {
    String? reason,
  }) =>
      _postObject('/api/v1/me/national-identity-requests', {
        'kind': kind,
        'reason': reason,
      });

  Future<List<Map<String, dynamic>>> nationalIdentityRequests() =>
      _getList('/api/v1/me/national-identity-requests');
  Future<List<Map<String, dynamic>>> nationalIdentityRequestHistory(String id) =>
      _getList('/api/v1/me/national-identity-requests/$id/history');

  Future<Map<String, dynamic>> submitBirthCertificateRequest(
    String kind, {
    String? reason,
    Map<String, dynamic>? newbornRegistration,
  }) =>
      _postObject('/api/v1/me/birth-certificate-requests', {
        'kind': kind,
        'reason': reason,
        if (newbornRegistration != null) 'newbornRegistration': newbornRegistration,
      });

  Future<List<Map<String, dynamic>>> birthCertificateRequests() =>
      _getList('/api/v1/me/birth-certificate-requests');
  Future<List<Map<String, dynamic>>> birthCertificateRequestHistory(String id) =>
      _getList('/api/v1/me/birth-certificate-requests/$id/history');
  Future<Map<String, dynamic>> newbornRegistration(String id) =>
      _getObject('/api/v1/me/birth-certificate-requests/$id/newborn-registration');

  Future<List<Map<String, dynamic>>> appointmentSlots(String serviceType) =>
      _getList('/api/v1/me/appointment-slots?serviceType=$serviceType');
  Future<Map<String, dynamic>> bookAppointment(String slotId) =>
      _postObject('/api/v1/me/appointments', {'slotId': slotId});
  Future<List<Map<String, dynamic>>> appointments() =>
      _getList('/api/v1/me/appointments');
  Future<Map<String, dynamic>> cancelAppointment(String id) =>
      _postObject('/api/v1/me/appointments/$id/cancel');

  Future<List<Map<String, dynamic>>> notifications() =>
      _getList('/api/v1/me/notifications');
  Future<int> unreadNotificationCount() async =>
      (await _getObject('/api/v1/me/notifications/unread-count'))['count'] as int;
  Future<Map<String, dynamic>> markNotificationRead(String id) =>
      _postObject('/api/v1/me/notifications/$id/read');
  Future<Map<String, dynamic>> markAllNotificationsRead() =>
      _postObject('/api/v1/me/notifications/read-all');

  Future<Map<String, dynamic>> uploadPassportAttachment(
    String requestId,
    Uint8List bytes,
    String fileName,
    String contentType,
  ) => _upload('/api/v1/me/passport-requests/$requestId/attachments', bytes, fileName, contentType);

  Future<Map<String, dynamic>> uploadNationalIdentityAttachment(
    String requestId,
    Uint8List bytes,
    String fileName,
    String contentType,
  ) => _upload('/api/v1/me/national-identity-requests/$requestId/attachments', bytes, fileName, contentType);

  Future<Map<String, dynamic>> uploadBirthCertificateAttachment(
    String requestId,
    Uint8List bytes,
    String fileName,
    String contentType,
  ) => _upload('/api/v1/me/birth-certificate-requests/$requestId/attachments', bytes, fileName, contentType);

  Future<List<Map<String, dynamic>>> passportAttachments(String id) =>
      _getList('/api/v1/me/passport-requests/$id/attachments');
  Future<List<Map<String, dynamic>>> nationalIdentityAttachments(String id) =>
      _getList('/api/v1/me/national-identity-requests/$id/attachments');
  Future<List<Map<String, dynamic>>> birthCertificateAttachments(String id) =>
      _getList('/api/v1/me/birth-certificate-requests/$id/attachments');

  Future<Map<String, dynamic>> _getObject(String path) async =>
      _object(await _client.request('GET', path, authenticated: true));

  Future<List<Map<String, dynamic>>> _getList(String path) async =>
      _list(await _client.request('GET', path, authenticated: true));

  Future<Map<String, dynamic>> _postObject(
    String path, [
    Map<String, dynamic>? body,
  ]) async =>
      _object(await _client.request('POST', path, body: body, authenticated: true));

  Future<Map<String, dynamic>> _upload(
    String path,
    Uint8List bytes,
    String fileName,
    String contentType,
  ) async =>
      _object(await _client.upload(
        path,
        bytes: bytes,
        fileName: fileName,
        contentType: contentType,
      ));

  Future<Map<String, dynamic>> _object(dynamic response) async {
    final decoded = jsonDecode(response.body);
    return Map<String, dynamic>.from(decoded as Map);
  }

  Future<List<Map<String, dynamic>>> _list(dynamic response) async {
    final decoded = jsonDecode(response.body) as List;
    return decoded.map((item) => Map<String, dynamic>.from(item as Map)).toList();
  }
}
