class BirthCertificateModel {
  final String certificatenumber;
  final String fullName;
  final String gender;
  final String birthDate;
  final String birthPlace;

  final String fathername;
  final String fatherBirthDate;
  final String fatherBirthPlace;
  final String fatherProfession;

  final String motherName;
  final String motherBirthDate;
  final String motherBirthplace;
  final String motherprofession;

  final String declarationDate;
  final String address;

  BirthCertificateModel({
    required this.certificatenumber,
    required this.fullName,
    required this.gender,
    required this.birthDate,
    required this.birthPlace,
    required this.fathername,
    required this.fatherBirthDate,
    required this.fatherBirthPlace,
    required this.fatherProfession,
    required this.motherName,
    required this.motherBirthDate,
    required this.motherBirthplace,
    required this.motherprofession,
    required this.declarationDate,
    required this.address,
  });

  factory BirthCertificateModel.fromJson(Map<String, dynamic> json) =>
      BirthCertificateModel(
        certificatenumber: json["certificateNumber"] ?? "",
        fullName: json["fullName"] ?? "",
        gender: json["gender"] ?? "",
        birthDate: json["birthDate"] ?? "",
        birthPlace: json["birthPlace"] ?? "",
        fathername: json["fatherName"] ?? "",
        fatherBirthDate: json["fatherBirthDate"] ?? "",
        fatherBirthPlace: json["fatherBirthPlace"] ?? "",
        fatherProfession: json["fatherProfession"] ?? "",
        motherName: json["motherName"] ?? "",
        motherBirthDate: json["motherBirthDate"] ?? "",
        motherBirthplace: json["motherBirthPlace"] ?? "",
        motherprofession: json["motherProfession"] ?? "",
        declarationDate: json["declarationDate"] ?? "",
        address: json["address"] ?? "",
      );

  Map<String, dynamic> toJson() => {
    "certificateNumber": certificatenumber,
    "fullName": fullName,
    "gender": gender,
    "birthDate": birthDate,
    "birthPlace": birthPlace,
    "fatherName": fathername,
    "fatherBirthDate": fatherBirthDate,
    "fatherBirthPlace": fatherBirthPlace,
    "fatherProfession": fatherProfession,
    "motherName": motherName,
    "motherBirthDate": motherBirthDate,
    "motherBirthPlace": motherBirthplace,
    "motherProfession": motherprofession,
    "declarationDate": declarationDate,
    "address": address,
  };
}
