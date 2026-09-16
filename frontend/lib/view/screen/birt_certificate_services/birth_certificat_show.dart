import 'package:flutter/material.dart';
import 'package:frontend/controller/birth_certificate_controller.dart';
import 'package:frontend/data/model/birth_certificate_model.dart';
import 'package:frontend/data/model/user_model.dart';
import 'package:frontend/generated/l10n.dart';

class BirthCertificatShow extends StatefulWidget {
  const BirthCertificatShow({super.key});

  @override
  State<BirthCertificatShow> createState() => _BirthCertificatShow();
}

class _BirthCertificatShow extends State<BirthCertificatShow> {
  final BirthCertificateController _controller = BirthCertificateController();
  BirthCertificateModel? birthCertificate;
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
    final result = await _controller.fetchBirthCertificate(context, id);

    print(result);
    print(result?.fullName);
    print(result?.certificatenumber);

    if (mounted) {
      setState(() {
        birthCertificate = result;
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
        title: Text(S.of(context).view_birth_certificate),
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
                  _buildBirthCertificateCard(context),
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

  Widget _buildBirthCertificateCard(BuildContext context) {
    return AspectRatio(
      aspectRatio: 0.60,
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
                    "images/birth_certificate.jpeg",
                    fit: BoxFit.fill,
                  ),
                ),
              ),

              Positioned(
                top: h * 0.23,
                left: w * 0.38,
                child: _buildText(
                  birthCertificate?.certificatenumber ?? "",
                  w * 0.025,
                  bold: true,
                ),
              ),
              Positioned(
                top: h * 0.42,
                left: w * 0.26,
                child: _buildText(
                  birthCertificate?.birthPlace ?? "",
                  w * 0.024,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.48,
                left: w * 0.20,
                child: _buildText(
                  birthCertificate?.birthDate ?? "",
                  w * 0.024,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.51,
                left: w * 0.25,
                child: _buildText(
                  birthCertificate?.fullName ?? "",
                  w * 0.024,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.54,
                left: w * 0.29,
                child: _buildText(
                  birthCertificate?.gender ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),
              Positioned(
                top: h * 0.56,
                left: w * 0.24,
                child: _buildText(
                  birthCertificate?.fathername ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.59,
                left: w * 0.20,
                child: _buildText(
                  birthCertificate?.fatherBirthDate ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.62,
                left: w * 0.24,
                child: _buildText(
                  birthCertificate?.fatherProfession ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.65,
                left: w * 0.24,
                child: _buildText(
                  birthCertificate?.motherName ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.68,
                left: w * 0.20,
                child: _buildText(
                  birthCertificate?.motherBirthDate ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),

              Positioned(
                top: h * 0.74,
                left: w * 0.24,
                child: _buildText(
                  birthCertificate?.motherprofession ?? "",
                  w * 0.022,
                  bold: true,
                ),
              ),

              // Positioned(
              //   top: h * 0.48,
              //   right: w * 0.08,
              //   child: _buildText(
              //     birthCertificate?.address ?? "",
              //     w * 0.022,
              //     bold: true,
              //   ),
              // ),

              // Positioned(
              //   top: h * 0.98,
              //   left: w * 0.20,
              //   child: _buildText(
              //     birthCertificate?.declarationDate ?? "",
              //     w * 0.020,
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
