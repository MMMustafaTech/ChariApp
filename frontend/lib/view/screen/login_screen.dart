import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/view/widget/button.dart';
import 'package:frontend/view/widget/textfield.dart';
import 'package:frontend/controller/auth_controller.dart';
import 'package:frontend/generated/l10n.dart';

// import 'package:frontend/main.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final AuthController controller = AuthController();

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Container(
          decoration: BoxDecoration(color: AppColors.background),
          margin: EdgeInsets.only(top: 90),
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.only(
                topLeft: Radius.circular(50),
                topRight: Radius.circular(50),
              ),
            ),
            height: MediaQuery.of(context).size.height,
            width: MediaQuery.of(context).size.width,
            padding: EdgeInsets.symmetric(horizontal: 20),
            child: Column(
              children: [
                SizedBox(height: 20),
                Text(
                  S.of(context).login_title,
                  style: TextStyle(
                    color: Colors.black,
                    fontSize: 30,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                SizedBox(height: 90),
                CustomTextFormAuth(
                  hinttext: S.of(context).national_id,
                  isPassword: false,
                  controller: controller.idController,
                ),
                SizedBox(height: 12),
                CustomTextFormAuth(
                  hinttext: S.of(context).password,
                  isPassword: true,
                  controller: controller.passController,
                ),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: () {},
                    child: Text(
                      S.of(context).forgot_password,
                      style: TextStyle(
                        color: AppColors.error,
                        fontSize: 19,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ),
                SizedBox(height: 40),
                CustomButtonAuth(
                  text: S.of(context).login_button,
                  onPressed: () async {
                    final user = await controller.login(context);
                    if (user != null) {
                      Navigator.of(context).pushNamed("home", arguments: user);
                    }
                  },
                ),
                SizedBox(height: 30),
                CustomButtonAuth(
                  text: S.of(context).create_account,
                  textColor: Colors.white,
                  color: Colors.grey[400],
                  onPressed: () {
                    Navigator.of(context).pushNamed("signup");
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
