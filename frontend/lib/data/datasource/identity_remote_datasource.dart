import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:frontend/data/model/identity_model.dart';
import 'package:http/http.dart' as http;

class IdentityRemoteDatasource {
  final String baseUrl = "https://absherapp-production.up.railway.app";

  Future<IdentityModel?> getIdentity(String nationalId) async {
    try {
      final response = await http.get(
        Uri.parse("$baseUrl/api/national-ids/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      debugPrint("status: ${response.statusCode}");
      debugPrint("body: ${response.body}");

      if (response.statusCode == 200) {
        return IdentityModel.fromJson(jsonDecode(response.body));
      }
      return null;
    } catch (e) {
      debugPrint("error: $e");
      return null;
    }
  }
}
