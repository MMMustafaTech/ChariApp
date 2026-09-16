import 'package:flutter/material.dart';
import 'package:frontend/controller/auth_controller.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/view/widget/button.dart';
import 'package:frontend/view/widget/textfield.dart';
import 'package:frontend/generated/l10n.dart';

class SignupScreen extends StatefulWidget {
  const SignupScreen({super.key});

  @override
  State<SignupScreen> createState() => _SignupScreenState();
}

class _SignupScreenState extends State<SignupScreen> {
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
        child: LayoutBuilder(
          builder: (context, constraints) {
            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: constraints.maxHeight),
                child: IntrinsicHeight(
                  child: Column(
                    children: [
                      const SizedBox(height: 90),
                      Expanded(
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 20,
                            vertical: 50,
                          ),
                          decoration: const BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.only(
                              topLeft: Radius.circular(55),
                              topRight: Radius.circular(55),
                            ),
                          ),
                          child: Column(
                            children: [
                              const SizedBox(height: 20),
                              Text(
                                S.of(context).create_account,
                                style: TextStyle(
                                  color: Colors.black,
                                  fontSize: 30,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              const SizedBox(height: 10),
                              CustomTextFormAuth(
                                hinttext: S.of(context).national_id,
                                controller: controller.idController,
                                isPassword: false,
                              ),
                              const SizedBox(height: 12),
                              CustomTextFormAuth(
                                hinttext: S.of(context).email,
                                controller: controller.emailController,
                                isPassword: false,
                              ),
                              const SizedBox(height: 12),
                              CustomTextFormAuth(
                                hinttext: S.of(context).password,
                                controller: controller.passController,
                                isPassword: true,
                              ),
                              const SizedBox(height: 12),
                              CustomTextFormAuth(
                                hinttext: S.of(context).confirm_password,
                                controller: controller.confirmPassController,
                                isPassword: true,
                              ),
                              const SizedBox(height: 10),
                              CustomButtonAuth(
                                text: S.of(context).create_account,
                                onPressed: () async {
                                  final success = await controller.signup(
                                    context,
                                  );
                                  if (success) {
                                    Navigator.of(context).pushNamed("login");
                                  }
                                },
                              ),
                              SizedBox(height: 30),
                              CustomButtonAuth(
                                text: S.of(context).login_title,
                                textColor: Colors.white,
                                color: Colors.grey[400],
                                onPressed: () {
                                  Navigator.of(context).pushNamed("login");
                                },
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
