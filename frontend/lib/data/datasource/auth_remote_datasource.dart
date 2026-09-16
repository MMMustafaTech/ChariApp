import 'package:frontend/core/api/session_store.dart';
import 'package:frontend/data/datasource/chari_api.dart';
import 'package:frontend/data/model/user_model.dart';

class AuthRemoteDatasource {
  AuthRemoteDatasource({ChariApi? api}) : _api = api ?? ChariApi();

  final ChariApi _api;

  Future<String> requestOtp(String nationalId) => _api.requestEnrollmentOtp(nationalId);

  Future<void> verifyOtp(String challengeId, String code) =>
      _api.verifyEnrollmentOtp(challengeId, code);

  Future<void> signup({
    required String challengeId,
    required String email,
    required String password,
  }) =>
      _api.createEnrollmentAccount(
        challengeId: challengeId,
        email: email,
        password: password,
      );

  Future<UserModel?> login(String nationalId, String password) async {
    await _api.login(nationalId, password);
    final profile = await _api.profile();
    return UserModel(id: nationalId, email: profile['email'] as String? ?? '', password: '', name: '');
  }

  Future<void> logout() => _api.logout();

  bool get isAuthenticated => SessionStore.isAuthenticated;
}
