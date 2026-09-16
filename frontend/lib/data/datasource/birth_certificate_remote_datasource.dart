import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:frontend/data/model/birth_certificate_model.dart';
import 'package:http/http.dart' as http;

class BirthCertificateRemoteDatasource {
  final String baseUrl =
      "https://absherapp-production.up.railway.app/birth-certificate";

  Future<BirthCertificateModel?> getBirthCertificate(String nationalId) async {
    try {
      print(Uri.parse("$baseUrl/birth-certificate/$nationalId"));
      final response = await http.get(
        Uri.parse("$baseUrl/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      debugPrint("status: ${response.statusCode}");
      debugPrint("body : ${response.body}");

      if (response.statusCode == 200) {
        return BirthCertificateModel.fromJson(jsonDecode(response.body));
      }
      return null;
    } catch (e) {
      debugPrint("error : $e");
      return null;
    }
  }

  Future<bool> issuanceOfBirthCertificate(String nationalId) async {
    try {
      final response = await http.post(
        Uri.parse("$baseUrl/birth-certificate/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      return response.statusCode == 200;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }

  Future<bool> newBornRegistration(String nationalId) async {
    try {
      final response = await http.post(
        Uri.parse("$baseUrl/birth-certificate/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      return response.statusCode == 200;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }

  Future<bool> requestForDataCorrection(String nationalId) async {
    try {
      final response = await http.post(
        Uri.parse("$baseUrl/birth-certificate/$nationalId"),
        headers: {"Content-Type": "application/json"},
      );

      return response.statusCode == 200;
    } catch (e) {
      debugPrint("error: $e");
      return false;
    }
  }
}
