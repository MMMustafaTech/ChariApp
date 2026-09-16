import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:frontend/data/model/passport_model.dart';
import 'package:http/http.dart' as http;
// import 'package:frontend/data/model/user_model.dart';

class PassportRemoteDatasource {
  final String baseUrl = "https://absherapp-production.up.railway.app";

  Future<PassportModel?> getPassport(String nationalId) async {
    try {
      final response = await http.get(
        Uri.parse("$baseUrl/passport/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      debugPrint("status: ${response.statusCode}");
      debugPrint("body: ${response.body}");

      if (response.statusCode == 200) {
        return PassportModel.fromJson(jsonDecode(response.body));
      }
      return null;
    } catch (e) {
      debugPrint("error: $e");
      return null;
    }
  }

  Future<bool> requestNewPassport(String nationalId) async {
    try {
      final response = await http.post(
        Uri.parse("$baseUrl/passport/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      return response.statusCode == 200;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }

  Future<bool> renewPassport(String nationalId) async {
    try {
      final response = await http.post(
        Uri.parse("$baseUrl/passport/renew/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      return response.statusCode == 200;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }
}
