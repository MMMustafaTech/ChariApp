import 'package:flutter/material.dart';
import 'package:frontend/generated/l10n.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:frontend/view/screen/national_id_services/NationalIdLostOrDamaged.dart';
import 'package:frontend/view/screen/national_id_services/NationalIdRenewal.dart';
import 'package:frontend/view/screen/passport_services/PassportIssuance.dart';
import 'package:frontend/view/screen/passport_services/PassportLostOrDamaged.dart';
import 'package:frontend/view/screen/passport_services/PassportRenewal.dart';

import 'package:frontend/view/screen/welcome_screen.dart';
import 'package:frontend/view/screen/login_screen.dart';
import 'package:frontend/view/screen/signup_screen.dart';
import 'package:frontend/view/screen/home_screen.dart';
import 'package:frontend/view/screen/bottomNav/other_services.dart';
import 'package:frontend/view/screen/bottomNav/my_requests.dart';
import 'package:frontend/view/screen/bottomNav/my_account.dart';

import 'package:frontend/view/screen/national_id_services/national_id_services.dart';
import 'package:frontend/view/screen/national_id_services/national_id_show_screen.dart';

import 'package:frontend/view/screen/passport_services/passport_services_screen.dart';
import 'package:frontend/view/screen/passport_services/passport_screen.dart';

import 'package:frontend/view/screen/birt_certificate_services/birth_certificate_services_screen.dart';
import 'package:frontend/view/screen/birt_certificate_services/birth_certificat_show.dart';
import 'package:frontend/view/screen/birt_certificate_services/issuance_of_birth_certificate.dart';
import 'package:frontend/view/screen/birt_certificate_services/newborn_registration.dart';
import 'package:frontend/view/screen/birt_certificate_services/request_for_data_correction.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  static void setLocale(BuildContext context, Locale locale) {
    final state = context.findAncestorStateOfType<_MyAppState>();
    state?.changeLanguage(locale);
  }

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Locale _locale = const Locale('ar');

  void changeLanguage(Locale locale) {
    setState(() {
      _locale = locale;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,

      locale: _locale,

      localizationsDelegates: [
        S.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],

      supportedLocales: S.delegate.supportedLocales,

      home: const WelcomeScreen(),

      routes: {
        "welcome": (context) => const WelcomeScreen(),
        "login": (context) => LoginScreen(),
        "signup": (context) => SignupScreen(),
        "home": (context) => HomeScreen(),
        "OtherServices": (context) => OtherServices(),
        "MyRequests": (context) => MyRequests(),
        "MyAccount": (context) => MyAccount(),
        "passportServices": (context) => PassportServicesScreen(),
        "showpassport": (context) => PassportScreen(),
        "Passportlostordamaged": (context) => Passportlostordamaged(),
        "birthCertificateServicesScreen": (context) =>
            BirthCertificateServicesScreen(),
        "RequestForDataCorrection": (context) => RequestForDataCorrection(),
        "NewbornRegistration": (context) => NewbornRegistration(),
        "IssuanceOfBirthCertificate": (context) => IssuanceOfBirthCertificate(),
        "birthcertificate": (context) => BirthCertificatShow(),
        "NationalIdServices": (context) => NationalIdServices(),
        "NationalIdShowScreen": (context) => NationalIdShowScreen(),
        "NationalIdRenewal": (context) => NationalIdRenewal(),
        "NationalIdLostOrDamaged": (context) => NationalIdLostOrDamaged(),
        "PassportRenewal": (context) => PassportRenewal(),
        "PassportIssuance": (context) => PassportIssuance(),
      },
    );
  }
}
