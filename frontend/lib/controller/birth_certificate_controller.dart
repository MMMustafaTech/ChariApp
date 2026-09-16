import 'package:flutter/material.dart';
import 'package:frontend/data/datasource/birth_certificate_remote_datasource.dart';
import 'package:frontend/data/model/birth_certificate_model.dart';

class BirthCertificateController {
  final BirthCertificateRemoteDatasource _datasource =
      BirthCertificateRemoteDatasource();
  BirthCertificateModel? birthCertificate;

  Future<BirthCertificateModel?> fetchBirthCertificate(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      birthCertificate = await _datasource.getBirthCertificate(nationalId);
      if (!context.mounted) return null;
      if (birthCertificate == null) {
        _showMessage(context, "لم يتم العثور على بيانات الجواز");
      }
      return birthCertificate;
    } catch (e) {
      if (!context.mounted) return null;
      debugPrint("خطأ في جلب الجواز: $e");
      _showMessage(context, "حدث خطأ، حاول مرة أخرى");
      return null;
    }
  }

  Future<bool> issuanceOfBirthCertificate(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      final success = await _datasource.issuanceOfBirthCertificate(nationalId);
      if (!context.mounted) return false;
      if (success) {
        _showMessage(context, "تم تقديم طلب استخراج شهادة ميلاد");
      } else {
        _showMessage(context, "حدث خطأ، حاول مرة أخرى");
      }
      return success;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }

  // طلب تجديد الجواز
  Future<bool> newBornRegistration(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      final success = await _datasource.newBornRegistration(nationalId);
      if (!context.mounted) return false;
      if (success) {
        _showMessage(context, "تم تقديم طلب اضافة مولود جديد بنجاح");
      } else {
        _showMessage(context, "حدث خطأ، حاول مرة أخرى");
      }
      return success;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }

  Future<bool> requestForDataCorrection(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      final success = await _datasource.requestForDataCorrection(nationalId);
      if (!context.mounted) return false;
      if (success) {
        _showMessage(context, "تم تقديم طلب تعديل بيانات شهادة الميلاد بنجاح");
      } else {
        _showMessage(context, "حدث خطأ، حاول مرة أخرى");
      }
      return success;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }

  void _showMessage(BuildContext context, String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }
}
