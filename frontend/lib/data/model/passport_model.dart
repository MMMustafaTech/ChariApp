class PassportModel {
  final String passportNumber;
  final String firstName;
  final String lastName;
  final String birthDate;
  final String birthPlace;
  final String issueDate;
  final String expiryDate;
  final String issuePlace;
  final String issueingAuthority;
  final String profession;
  final String nationality;
  final String gender;

  PassportModel({
    required this.passportNumber,
    required this.firstName,
    required this.lastName,
    required this.birthDate,
    required this.birthPlace,
    required this.issueDate,
    required this.expiryDate,
    required this.issuePlace,
    required this.issueingAuthority,
    required this.profession,
    required this.nationality,
    required this.gender,
  });

  factory PassportModel.fromJson(Map<String, dynamic> json) => PassportModel(
    passportNumber: json["passportNumber"] ?? "",
    firstName: json["firstName"] ?? "",
    lastName: json["lastName"] ?? "",
    birthDate: json["birthDate"] ?? "",
    birthPlace: json["birthPlace"] ?? "",
    issueDate: json["issueDate"] ?? "",
    expiryDate: json["expiryDate"] ?? "",
    issuePlace: json["issuePlace"] ?? "",
    issueingAuthority: json["issueingAuthority"] ?? "",
    profession: json["profession"] ?? "",
    nationality: json["nationality"] ?? "",
    gender: json["gender"] ?? "",
  );

  Map<String, dynamic> toJson() => {
    "passportNumber": passportNumber,
    "firstName": firstName,
    "lastName": lastName,
    "birthDate": birthDate,
    "birthPlace": birthPlace,
    "issueDate": issueDate,
    "expiryDate": expiryDate,
    "issuePlace": issuePlace,
    "issueingAuthority": issueingAuthority,
    "profession": profession,
    "nationality": nationality,
    "gender": gender,
  };
}
