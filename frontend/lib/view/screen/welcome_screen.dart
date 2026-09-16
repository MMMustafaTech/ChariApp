import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/generated/l10n.dart';
import 'package:frontend/view/widget/button.dart';
import 'package:frontend/main.dart';
// import 'package:frontend/view/widget/textfield.dart';
// import 'package:frontend/view/screen/login_screen.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final isArabic = Localizations.localeOf(context).languageCode == 'ar';

    return Scaffold(
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        actions: [
          PopupMenuButton<String>(
            icon: const Icon(Icons.language, color: Colors.white),
            onSelected: (value) {
              if (value == 'ar') {
                MyApp.setLocale(context, const Locale('ar'));
              }

              if (value == 'en') {
                MyApp.setLocale(context, const Locale('en'));
              }

              if (value == 'fr') {
                MyApp.setLocale(context, const Locale('fr'));
              }
              if (value == 'tr') {
                MyApp.setLocale(context, const Locale('tr'));
              }
            },
            itemBuilder: (context) => const [
              PopupMenuItem(value: 'ar', child: Text('العربية')),
              PopupMenuItem(value: 'en', child: Text('English')),
              PopupMenuItem(value: 'fr', child: Text('Français')),
              PopupMenuItem(value: 'tr', child: Text('Türkçe')),
            ],
          ),
        ],
      ),
      body: Container(
        color: AppColors.background,
        padding: const EdgeInsets.symmetric(vertical: 15, horizontal: 20),
        child: ListView(
          children: [
            const SizedBox(height: 80),
            // هنا سنضع الشعار لاحقاً
            Column(
              children: isArabic
                  ? [
                      Text(
                        S.of(context).welcome_message,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 25,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),

                      Text(
                        S.of(context).app_name,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 35,
                          fontWeight: FontWeight.bold,
                          color: Colors.red,
                        ),
                      ),
                    ]
                  : [
                      Text(
                        S.of(context).app_name,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 35,
                          fontWeight: FontWeight.bold,
                          color: Colors.red,
                        ),
                      ),

                      Text(
                        S.of(context).welcome_message,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 25,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ],
            ),

            const SizedBox(height: 450),
            CustomButtonAuth(
              text: S.of(context).login_button,
              onPressed: () {
                Navigator.of(context).pushReplacementNamed("login");
              },
            ),
          ],
        ),
      ),
    );
  }
}
