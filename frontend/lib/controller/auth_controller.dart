import 'package:flutter/material.dart';
import 'package:frontend/data/datasource/auth_remote_datasource.dart';
import 'package:frontend/data/model/user_model.dart';

class AuthController {
  final AuthRemoteDatasource _datasource = AuthRemoteDatasource();

  final TextEditingController idController = TextEditingController();
  final TextEditingController fullNameController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  final TextEditingController passController = TextEditingController();
  final TextEditingController confirmPassController = TextEditingController();

  Future<bool> signup(BuildContext context) async {
    print("id: ${idController.text}");
    print("email: ${emailController.text}");
    print("pass: ${passController.text}");
    print("confirmPass: ${confirmPassController.text}");

    if (idController.text.isEmpty ||
        emailController.text.isEmpty ||
        passController.text.isEmpty ||
        confirmPassController.text.isEmpty) {
      _showMessage(context, "الرجاء إدخال جميع الحقول المطلوبة");
      return false;
    }

    if (passController.text != confirmPassController.text) {
      _showMessage(context, "كلمة المرور غير متطابقة");
      return false;
    }

    final user = UserModel(
      id: idController.text,
      email: emailController.text,
      password: passController.text,
    );

    final success = await _datasource.signup(user);

    if (!success) {
      _showMessage(context, "فشل إنشاء الحساب");
    }

    return success;
  }

  Future<UserModel?> login(BuildContext context) async {
    if (idController.text.isEmpty || passController.text.isEmpty) {
      _showMessage(context, "الرجاء إدخال جميع الحقول المطلوبة");
      return null;
    }

    final user = await _datasource.login(
      idController.text,
      passController.text,
    );

    if (user == null) {
      _showMessage(context, "رقم الهوية أو كلمة المرور غير صحيحة");
    }

    return user;
  }

  void _showMessage(BuildContext context, String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  void dispose() {
    idController.dispose();
    emailController.dispose();
    passController.dispose();
    confirmPassController.dispose();
  }
}
