import 'dart:convert';
import 'package:frontend/data/models/user.dart';
import 'package:http/http.dart' as http;

class UserApi {
  Future<List<User>?> getAllUsers() async {
    var client = http.Client();
    var baseUri = 'http://localhost:8080';
    var uri = Uri.parse('$baseUri/users');
    var basicAuth = toBase64('admin:8MumblingRastusNominee2');
    var response =
        await client.get(uri, headers: {'Authorization': 'Basic $basicAuth'});

    if (response.statusCode == 200) {
      List<dynamic> userList = jsonDecode(const Utf8Decoder().convert(response.bodyBytes));
      return userList.map((e) => User.fromJson(e)).toList();
    }
    return null;
  }

  String toBase64(String s) {
    return base64Encode(utf8.encode(s));
  }
}
