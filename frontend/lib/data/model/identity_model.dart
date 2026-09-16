class IdentityModel {
  final String nationalId;
  final String firstName;
  final String lastName;
  final String fatherName;
  final String motherName;
  final String gender;
  final String dateOfBirth;
  final String placeOfBirth;
  final String address;
  final String profession;
  final String bloodGroup;
  final String cardSerial;
  final String issueDetails;
  final String dateOfExpiry;

  IdentityModel({
    required this.nationalId,
    required this.firstName,
    required this.lastName,
    required this.fatherName,
    required this.motherName,
    required this.gender,
    required this.dateOfBirth,
    required this.placeOfBirth,
    required this.address,
    required this.profession,
    required this.bloodGroup,
    required this.cardSerial,
    required this.issueDetails,
    required this.dateOfExpiry,
  });

  factory IdentityModel.fromJson(Map<String, dynamic> json) => IdentityModel(
    nationalId: json["nationalId"] ?? "",
    firstName: json["firstName"] ?? "",
    lastName: json["lastName"] ?? "",
    fatherName: json["fatherName"] ?? "",
    motherName: json["motherName"] ?? "",
    gender: json["gender"] ?? "",
    dateOfBirth: json["dateofBirth"] ?? "",
    placeOfBirth: json["placeOfBirth"] ?? "",
    address: json["address"] ?? "",
    profession: json["profession"] ?? "",
    bloodGroup: json["bloodGroup"] ?? "",
    cardSerial: json["cardSerial"] ?? "",
    issueDetails: json["issueDetails"] ?? "",
    dateOfExpiry: json["dateOfExpiry"] ?? "",
  );

  Map<String, dynamic> toJson() => {
    "nationalId": nationalId,
    "firstName": firstName,
    "lastName": lastName,
    "fatherName": fatherName,
    "motherName": motherName,
    "gender": gender,
    "dateofBirth": dateOfBirth,
    "placeOfBirth": placeOfBirth,
    "address": address,
    "profession": profession,
    "bloodGroup": bloodGroup,
    "cardSerial": cardSerial,
    "issueDetails": issueDetails,
    "dateOfExpiry": dateOfExpiry,
  };
}
