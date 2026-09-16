import 'package:flutter/material.dart';
import 'package:frontend/controller/identity_controller.dart';
import 'package:frontend/data/model/identity_model.dart';
import 'package:frontend/data/model/user_model.dart';
import 'package:frontend/generated/l10n.dart';

class NationalIdShowScreen extends StatefulWidget {
  const NationalIdShowScreen({super.key});

  @override
  State<NationalIdShowScreen> createState() => _NationalIdShowScreenState();
}

class _NationalIdShowScreenState extends State<NationalIdShowScreen> {
  final IdentityController _identityController = IdentityController();
  IdentityModel? identity;
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
    final result = await _identityController.fetchIdentity(context, id);
    if (mounted) {
      setState(() {
        identity = result;
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F7FA),
      appBar: AppBar(
        backgroundColor: const Color(0xFF002F6C),
        foregroundColor: Colors.white,
        title: Text(S.of(context).view_national_id),
        centerTitle: true,
        elevation: 0,
      ),
      body: isLoading
          ? const Center(
              child: CircularProgressIndicator(color: Color(0xFF002F6C)),
            )
          : SingleChildScrollView(
              padding: const EdgeInsets.symmetric(vertical: 40, horizontal: 16),
              child: Column(
                children: [
                  _buildIdentityCard(context),
                  const SizedBox(height: 30),
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

  Widget _buildIdentityCard(BuildContext context) {
    return AspectRatio(
      aspectRatio: 1.42,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final w = constraints.maxWidth;
          final h = constraints.maxHeight;

          return Stack(
            children: [
              // ===== صورة القالب الخلفية =====
              Positioned.fill(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: Image.asset(
                    "images/national_id_tamplate.jpeg",
                    fit: BoxFit.fill,
                  ),
                ),
              ),

              // رقم البطاقة
              Positioned(
                top: h * 0.12,
                left: w * 0.29,
                child: Row(
                  children: [
                    // _buildText("ID", w * 0.03, bold: true),
                    SizedBox(width: w * 0.10),
                    // _buildText("TCD", w * 0.03, bold: true),
                    SizedBox(width: w * 0.21),
                    // _buildText(
                    //   identity?.cardSerial ?? "---------",
                    //   w * 0.03,
                    //   bold: true,
                    //   isRed: true,
                    // ),
                  ],
                ),
              ),

              // الاسم الأول
              Positioned(
                top: h * 0.21,
                left: w * 0.46,
                child: _buildText(
                  (identity?.firstName ?? "Loading...").toUpperCase(),
                  w * 0.031,
                  bold: true,
                ),
              ),

              // اللقب
              Positioned(
                top: h * 0.27,
                left: w * 0.48,
                child: _buildText(
                  (identity?.lastName ?? "Loading...").toUpperCase(),
                  w * 0.031,
                  bold: true,
                ),
              ),

              // رقم الهوية NNI
              Positioned(
                top: h * 0.40,
                left: w * 0.51,
                child: _buildText(
                  identity?.nationalId ?? "----------",
                  w * 0.03,
                  bold: true,
                ),
              ),

              // اسم الأب
              Positioned(
                top: h * 0.27,
                left: w * 0.61,
                child: _buildText(
                  (identity?.fatherName ?? "").toUpperCase(),
                  w * 0.03,
                  bold: true,
                ),
              ),

              // تاريخ الميلاد
              Positioned(
                top: h * 0.46,
                left: w * 0.61,
                child: _buildText(
                  identity?.dateOfBirth ?? "--/--/----",
                  w * 0.03,
                  bold: true,
                ),
              ),

              // الجنس
              Positioned(
                top: h * 0.34,
                right: w * 0.45,
                child: _buildText(
                  identity?.gender ?? "M",
                  w * 0.03,
                  bold: true,
                ),
              ),

              // مكان الميلاد
              // Positioned(
              //   top: h * 0.49,
              //   left: w * 0.19,
              //   child: _buildText(
              //     (identity?.placeOfBirth ?? "").toUpperCase(),
              //     w * 0.03,
              //     bold: true,
              //   ),
              // ),

              // // المهنة
              // Positioned(
              //   top: h * 0.50,
              //   right: w * 0.15,
              //   child: _buildText(
              //     (identity?.profession ?? "").toUpperCase(),
              //     w * 0.03,
              //     bold: true,
              //   ),
              // ),

              // تفاصيل الإصدار
              Positioned(
                top: h * 0.72,
                left: w * 0.60,
                child: _buildText(
                  identity?.issueDetails ?? "--/--/----",
                  w * 0.03,
                  bold: true,
                ),
              ),

              // // فصيلة الدم
              // Positioned(
              //   top: h * 0.57,
              //   right: w * 0.25,
              //   child: _buildText(
              //     identity?.bloodGroup ?? "",
              //     w * 0.03,
              //     bold: true,
              //   ),
              // ),

              // تاريخ الانتهاء
              Positioned(
                top: h * 0.65,
                left: w * 0.55,
                child: _buildText(
                  identity?.dateOfExpiry ?? "--/--/----",
                  w * 0.03,
                  bold: true,
                ),
              ),

              // العنوان
              // Positioned(
              //   top: h * 0.95,
              //   right: w * 0.10,
              //   child: _buildText(
              //     (identity?.address ?? "").toUpperCase(),
              //     w * 0.022,
              //     bold: true,
              //   ),
              // ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildText(
    String text,
    double size, {
    bool bold = false,
    bool isRed = false,
  }) {
    return Text(
      text,
      style: TextStyle(
        fontSize: size,
        fontFamily: 'monospace',
        fontWeight: bold ? FontWeight.bold : FontWeight.normal,
        color: isRed ? Colors.red.shade900 : Colors.black87,
        letterSpacing: 0.5,
      ),
    );
  }
}
