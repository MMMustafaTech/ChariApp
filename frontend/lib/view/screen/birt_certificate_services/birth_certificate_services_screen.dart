import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/generated/l10n.dart';

class BirthCertificateServicesScreen extends StatefulWidget {
  const BirthCertificateServicesScreen({super.key});

  @override
  State<BirthCertificateServicesScreen> createState() =>
      _BirthCertificateServicesScreenState();
}

class _BirthCertificateServicesScreenState
    extends State<BirthCertificateServicesScreen> {
  Widget _buildServiceCard({
    required String title,
    required IconData icon,
    required VoidCallback onTap,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(15),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        onTap: onTap,
        leading: const Icon(
          Icons.arrow_back_ios_new,
          size: 18,
          color: Colors.grey,
        ),
        title: Text(
          title,
          textAlign: TextAlign.right,
          style: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: Color(0xFF2D3142),
          ),
        ),
        trailing: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: const Color(0xFF002F6C).withOpacity(0.1),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: const Color(0xFF002F6C)),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        backgroundColor: const Color(0xFF002F6C),
        foregroundColor: Colors.white,
        title: Text(
          S.of(context).birth_certificate_services,
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            const SizedBox(height: 30),
            Text(
              S.of(context).welcome,
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
                color: AppColors.textMain,
              ),
            ),
            Text(
              S.of(context).select_service,
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 30),

            _buildServiceCard(
              title: S.of(context).newborn_registration,
              icon: Icons.child_care_rounded,
              onTap: () =>
                  Navigator.of(context).pushNamed("NewbornRegistration"),
            ),

            _buildServiceCard(
              title: S.of(context).birth_certificate_view,
              icon: Icons.assignment_turned_in_rounded,
              onTap: () {
                Navigator.of(context).pushNamed("IssuanceOfBirthCertificate");
              },
            ),

            _buildServiceCard(
              title: S.of(context).data_correction_request,
              icon: Icons.edit_note_rounded,
              onTap: () {
                Navigator.of(context).pushNamed("RequestForDataCorrection");
              },
            ),
          ],
        ),
      ),
    );
  }
}
