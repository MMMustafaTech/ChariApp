import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';

class IssuanceOfBirthCertificate extends StatefulWidget {
  const IssuanceOfBirthCertificate({super.key});

  @override
  State<IssuanceOfBirthCertificate> createState() =>
      _IssuanceOfBirthCertificate();
}

class _IssuanceOfBirthCertificate extends State<IssuanceOfBirthCertificate> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        title: Text(
          "اصدار شهادة الميلاد",
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        foregroundColor: AppColors.primary,
        centerTitle: true,
      ),
    );
  }
}
