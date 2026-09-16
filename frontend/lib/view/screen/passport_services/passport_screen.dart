import 'package:flutter/material.dart';
import 'package:frontend/data/model/passport_model.dart';
import 'package:frontend/data/model/user_model.dart';
import 'package:frontend/controller/passport_controller.dart';
import 'package:frontend/generated/l10n.dart';

class PassportScreen extends StatefulWidget {
  const PassportScreen({super.key});

  @override
  State<PassportScreen> createState() => _PassportScreenState();
}

class _PassportScreenState extends State<PassportScreen> {
  final PassportController _passportController = PassportController();
  PassportModel? passport;
  bool isLoading = true;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final user = ModalRoute.of(context)?.settings.arguments as UserModel?;
    if (user != null && isLoading) {
      _loadData(user.id);
    }
  }

  Future<void> _loadData(String id) async {
    final result = await _passportController.fetchPassport(context, id);
    if (mounted) {
      setState(() {
        passport = result;
        isLoading = false;
      });
    }
    print("the national id $id");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      appBar: AppBar(
        backgroundColor: const Color(0xFF002F6C),
        foregroundColor: Colors.white,
        title: Text(
          S.of(context).view_passport,
          style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 1.2),
        ),
        centerTitle: true,
        elevation: 8,
        shadowColor: Colors.black38,
      ),
      body: isLoading
          ? const Center(
              child: CircularProgressIndicator(color: Color(0xFF002F6C)),
            )
          : SingleChildScrollView(
              padding: const EdgeInsets.symmetric(vertical: 30, horizontal: 12),
              child: Column(
                children: [
                  Container(
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(15),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.3),
                          blurRadius: 20,
                          offset: const Offset(0, 10),
                        ),
                      ],
                    ),
                    child: _buildPassportCard(context, passport),
                  ),
                  const SizedBox(height: 40),

                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.security, color: Colors.grey, size: 18),
                      SizedBox(width: 8),
                      Text(
                        S.of(context).official_digital_document,
                        style: TextStyle(color: Colors.grey, fontSize: 13),
                      ),
                    ],
                  ),
                ],
              ),
            ),
    );
  }

  Widget _buildPassportCard(BuildContext context, PassportModel? passport) {
    return AspectRatio(
      aspectRatio: 1.42,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final w = constraints.maxWidth;
          final h = constraints.maxHeight;

          return Stack(
            children: [
              Positioned.fill(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: Image.asset(
                    "images/passport_template.jpeg",
                    fit: BoxFit.fill,
                  ),
                ),
              ),

              // النوع + الكود + رقم الجواز
              Positioned(
                top: h * 0.12,
                left: w * 0.29,
                child: Row(
                  children: [
                    _buildPassportText("P", w * 0.035),
                    SizedBox(width: w * 0.09),
                    _buildPassportText("TCD", w * 0.035),
                    SizedBox(width: w * 0.20),
                    _buildPassportText(
                      passport?.passportNumber ?? "---------",
                      w * 0.038,
                      isRed: true,
                    ),
                  ],
                ),
              ),

              Positioned(
                top: h * 0.19,
                left: w * 0.29,
                child: _buildPassportText(
                  (passport?.lastName ?? S.of(context).loading).toUpperCase(),
                  w * 0.033,
                ),
              ),

              Positioned(
                top: h * 0.26,
                left: w * 0.29,
                child: _buildPassportText(
                  (passport?.firstName ?? S.of(context).loading).toUpperCase(),
                  w * 0.033,
                ),
              ),

              Positioned(
                top: h * 0.35,
                left: w * 0.29,
                child: _buildPassportText(
                  (passport?.nationality ?? "CHAD").toUpperCase(),
                  w * 0.03,
                ),
              ),

              // رقم الهوية NNI
              Positioned(
                top: h * 0.35,
                right: w * 0.08,
                child: _buildPassportText("1234567890", w * 0.032),
              ),

              Positioned(
                top: h * 0.42,
                left: w * 0.29,
                child: _buildPassportText(
                  passport?.birthDate ?? "--/--/----",
                  w * 0.03,
                ),
              ),

              Positioned(
                top: h * 0.42,
                right: w * 0.20,
                child: _buildPassportText(passport?.gender ?? "M", w * 0.032),
              ),

              Positioned(
                top: h * 0.49,
                left: w * 0.29,
                child: _buildPassportText(
                  passport?.birthPlace ?? "N'DJAMENA",
                  w * 0.03,
                ),
              ),

              Positioned(
                top: h * 0.50,
                right: w * 0.24,
                child: _buildPassportText(
                  passport?.profession ?? "STUDENT",
                  w * 0.028,
                ),
              ),

              Positioned(
                top: h * 0.58,
                left: w * 0.29,
                child: _buildPassportText(
                  passport?.issueDate ?? "--/--/----",
                  w * 0.03,
                ),
              ),

              Positioned(
                top: h * 0.57,
                right: w * 0.24,
                child: _buildPassportText(
                  passport?.issuePlace ?? "N'DJAMENA",
                  w * 0.03,
                ),
              ),

              Positioned(
                top: h * 0.65,
                left: w * 0.29,
                child: _buildPassportText(
                  passport?.expiryDate ?? "--/--/----",
                  w * 0.03,
                ),
              ),

              Positioned(
                top: h * 0.65,
                right: w * 0.04,
                child: _buildPassportText(
                  (passport?.issueingAuthority ?? "DG DE LA POLICE NATIONALE")
                      .toUpperCase(),
                  w * 0.022,
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildPassportText(String text, double size, {bool isRed = false}) {
    return Text(
      text,
      style: TextStyle(
        fontSize: size,
        fontWeight: FontWeight.w700,
        fontFamily: 'Courier',
        color: isRed ? Colors.red.shade900 : Colors.black87,
        letterSpacing: 0.5,
      ),
    );
  }
}
