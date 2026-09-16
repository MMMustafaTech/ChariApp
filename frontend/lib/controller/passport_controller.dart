import 'package:flutter/material.dart';
import 'package:frontend/data/datasource/passport_remote_datasource.dart';
import 'package:frontend/data/model/passport_model.dart';

class PassportController {
  final PassportRemoteDatasource _datasource = PassportRemoteDatasource();

  PassportModel? passport;

  // جلب بيانات الجواز
  Future<PassportModel?> fetchPassport(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      passport = await _datasource.getPassport(nationalId);
      if (!context.mounted) return null;
      if (passport == null) {
        _showMessage(context, "لم يتم العثور على بيانات الجواز");
      }
      return passport;
    } catch (e) {
      if (!context.mounted) return null;
      debugPrint("خطأ في جلب الجواز: $e");
      _showMessage(context, "حدث خطأ، حاول مرة أخرى");
      return null;
    }
  }

  // طلب استخراج جواز جديد
  Future<bool> requestNewPassport(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      final success = await _datasource.requestNewPassport(nationalId);
      if (!context.mounted) return false;
      if (success) {
        _showMessage(context, "تم تقديم طلب استخراج الجواز بنجاح");
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
  Future<bool> renewPassport(BuildContext context, String nationalId) async {
    try {
      final success = await _datasource.renewPassport(nationalId);
      if (!context.mounted) return false;
      if (success) {
        _showMessage(context, "تم تقديم طلب تجديد الجواز بنجاح");
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
