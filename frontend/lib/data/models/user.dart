import 'dart:convert';

class User {
    final int id;
    final String name;
    final String password;
    final bool admin;

    User({
        required this.id,
        required this.name,
        required this.password,
        required this.admin,
    });

    factory User.fromRawJson(String str) => User.fromJson(json.decode(str));

    String toRawJson() => json.encode(toJson());

    factory User.fromJson(Map<String, dynamic> json) => User(
        id: json["id"],
        name: json["name"],
        password: json["password"],
        admin: json["admin"],
    );

    Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "password": password,
        "admin": admin,
    };
}
