import 'package:flutter/material.dart';
import 'package:frontend/data/datasource/identity_remote_datasource.dart';
import 'package:frontend/data/model/identity_model.dart';

class IdentityController {
  final IdentityRemoteDatasource _datasource = IdentityRemoteDatasource();
  IdentityModel? identity;

  Future<IdentityModel?> fetchIdentity(
    BuildContext context,
    String nationalId,
  ) async {
    try {
      identity = await _datasource.getIdentity(nationalId);
      if (!context.mounted) return null;
      if (identity == null) {
        _showMessage(context, "لم يتم العثور على بيانات الهوية");
      }
      return identity;
    } catch (e) {
      if (!context.mounted) return null;
      debugPrint("خطأ في جلب الهوية: $e");
      _showMessage(context, "حدث خطأ، حاول مرة أخرى");
      return null;
    }
  }

  void _showMessage(BuildContext context, String message) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }
}
