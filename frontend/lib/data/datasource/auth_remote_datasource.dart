import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:frontend/data/model/user_model.dart';

class AuthRemoteDatasource {
  Future<bool> signup(UserModel user) async {
    try {
      final body = jsonEncode(user.toJson());
      print("request body: $body");

      final response = await http.post(
        Uri.parse("https://absherapp-production.up.railway.app/auth/register"),
        headers: {"Content-Type": "application/json"},
        body: body,
      );

      print("status code: ${response.statusCode}");
      print("response body: ${response.body}");

      if (response.statusCode == 200 || response.statusCode == 201) {
        return true;
      }

      return false;
    } catch (e) {
      print("error: $e");
      return false;
    }
  }

  Future<UserModel?> login(String id, String password) async {
    try {
      final body = jsonEncode({"nationalId": id, "password": password});

      print("request body: $body");

      final response = await http.post(
        Uri.parse("https://absherapp-production.up.railway.app/auth/login"),
        headers: {"Content-Type": "application/json"},
        body: body,
      );

      print("status code: ${response.statusCode}");
      print("response body: ${response.body}");

      if (response.statusCode == 200) {
        return UserModel.fromJson(jsonDecode(response.body));
      }

      return null;
    } catch (e) {
      print("error: $e");
      return null;
    }
  }
}
