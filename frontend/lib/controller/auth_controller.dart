import 'package:flutter/material.dart';
import 'package:frontend/core/api/api_client.dart';
import 'package:frontend/data/datasource/auth_remote_datasource.dart';
import 'package:frontend/data/model/user_model.dart';

class AuthController {
  final AuthRemoteDatasource _datasource = AuthRemoteDatasource();

  final TextEditingController idController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  final TextEditingController passController = TextEditingController();
  final TextEditingController confirmPassController = TextEditingController();
  final TextEditingController otpController = TextEditingController();

  String? _challengeId;
  bool get otpRequested => _challengeId != null;

  Future<bool> requestSignupOtp(BuildContext context) async {
    if (idController.text.trim().isEmpty) {
      _showMessage(context, 'أدخل الرقم الوطني أولاً');
      return false;
    }
    try {
      _challengeId = await _datasource.requestOtp(idController.text.trim());
      _showMessage(context, 'تم إرسال رمز التحقق. أدخله للمتابعة.');
      return true;
    } on ApiException catch (exception) {
      _showMessage(context, exception.message);
      return false;
    }
  }

  Future<bool> completeSignup(BuildContext context) async {
    if (_challengeId == null) {
      _showMessage(context, 'اطلب رمز التحقق أولاً');
      return false;
    }
    if (emailController.text.trim().isEmpty ||
        passController.text.isEmpty ||
        confirmPassController.text.isEmpty ||
        otpController.text.trim().length != 6) {
      _showMessage(context, 'أدخل البريد وكلمة المرور ورمز التحقق المكوّن من 6 أرقام');
      return false;
    }
    if (passController.text != confirmPassController.text) {
      _showMessage(context, 'كلمة المرور غير متطابقة');
      return false;
    }
    if (passController.text.length < 12) {
      _showMessage(context, 'كلمة المرور يجب أن تكون 12 حرفًا على الأقل');
      return false;
    }
    try {
      await _datasource.verifyOtp(_challengeId!, otpController.text.trim());
      await _datasource.signup(
        challengeId: _challengeId!,
        email: emailController.text.trim(),
        password: passController.text,
      );
      _showMessage(context, 'تم إنشاء الحساب. يمكنك تسجيل الدخول الآن.');
      return true;
    } on ApiException catch (exception) {
      _showMessage(context, exception.message);
      return false;
    }
  }

  Future<UserModel?> login(BuildContext context) async {
    if (idController.text.trim().isEmpty || passController.text.isEmpty) {
      _showMessage(context, 'أدخل الرقم الوطني وكلمة المرور');
      return null;
    }
    try {
      final user = await _datasource.login(
        idController.text.trim(),
        passController.text,
      );
      if (user == null) _showMessage(context, 'تعذر تسجيل الدخول');
      return user;
    } on ApiException catch (_) {
      _showMessage(context, 'الرقم الوطني أو كلمة المرور غير صحيحة');
      return null;
    }
  }

  Future<void> logout() => _datasource.logout();

  void _showMessage(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  void dispose() {
    idController.dispose();
    emailController.dispose();
    passController.dispose();
    confirmPassController.dispose();
    otpController.dispose();
  }
}
