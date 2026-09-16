import 'package:flutter/material.dart';
import 'package:frontend/controller/passport_controller.dart';
import 'package:frontend/data/model/passport_model.dart';
import 'package:frontend/data/model/user_model.dart';
import 'package:frontend/generated/l10n.dart';

class PassportRenewal extends StatefulWidget {
  const PassportRenewal({super.key});

  @override
  State<PassportRenewal> createState() => _PassportRenewal();
}

class _PassportRenewal extends State<PassportRenewal> {
  final PassportController _passportController = PassportController();
  final TextEditingController _idController = TextEditingController();

  PassportModel? passport;
  bool _isVerified = false;
  String? _selectedReason;
  UserModel? user;

  List<String> get _reasons => [
    S.of(context).passport_expired,
    S.of(context).passport_expiring_soon,
    S.of(context).other_reasons,
  ];

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    user = ModalRoute.of(context)?.settings.arguments as UserModel?;
    if (user != null) {
      _idController.text = user!.id;
    }
  }

  @override
  void dispose() {
    _idController.dispose();
    super.dispose();
  }

  Future<void> _checkPassport() async {
    if (_idController.text.isEmpty) return;
    final result = await _passportController.fetchPassport(
      context,
      _idController.text,
    );
    if (mounted) {
      setState(() {
        passport = result;
        _isVerified = result != null;
      });
    }
  }

  Widget _buildSectionCard({required Widget child}) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: child,
    );
  }

  Widget _buildSectionHeader({
    required String title,
    required String subtitle,
    required IconData icon,
    Color iconColor = const Color(0xFF002F6C),
  }) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: iconColor.withOpacity(0.1),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: iconColor, size: 20),
        ),
        Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              title,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Color(0xFF1a1a2e),
              ),
            ),
            Text(
              subtitle,
              style: const TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildDataRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            value.isEmpty ? "--" : value,
            style: const TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 14,
              color: Color(0xFF1a1a2e),
            ),
          ),
          Text(label, style: const TextStyle(color: Colors.grey, fontSize: 13)),
        ],
      ),
    );
  }

  Widget _buildReasonChip(String reason) {
    final isSelected = _selectedReason == reason;
    return GestureDetector(
      onTap: () => setState(() => _selectedReason = reason),
      child: Container(
        margin: const EdgeInsets.only(left: 8, bottom: 8),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF002F6C) : Colors.white,
          border: Border.all(
            color: isSelected ? const Color(0xFF002F6C) : Colors.grey.shade300,
          ),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
              size: 14,
              color: isSelected ? Colors.white : Colors.grey,
            ),
            const SizedBox(width: 6),
            Text(
              reason,
              style: TextStyle(
                color: isSelected ? Colors.white : const Color(0xFF1a1a2e),
                fontWeight: FontWeight.bold,
                fontSize: 13,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInputField({
    required String hint,
    required IconData icon,
    required TextEditingController controller,
  }) {
    return TextFormField(
      controller: controller,
      textAlign: TextAlign.right,
      decoration: InputDecoration(
        hintText: hint,
        hintStyle: const TextStyle(color: Colors.grey, fontSize: 12),
        prefixIcon: Icon(icon, color: const Color(0xFF002F6C), size: 18),
        filled: true,
        fillColor: const Color(0xFFF5F7FA),
        contentPadding: const EdgeInsets.symmetric(
          vertical: 12,
          horizontal: 12,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.grey.shade200),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.grey.shade200),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF002F6C)),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F4FF),
      appBar: AppBar(
        backgroundColor: const Color(0xFF002F6C),
        foregroundColor: Colors.white,
        title: Text(
          S.of(context).passport_renewal_title,
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // ===== 1. التحقق من الجواز =====
            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).passport_verification,
                    subtitle: S
                        .of(context)
                        .enter_national_id_for_passport_verification,
                    icon: Icons.verified_outlined,
                    iconColor: Colors.green,
                  ),
                  const SizedBox(height: 16),
                  _buildInputField(
                    hint: S.of(context).national_id_number,
                    icon: Icons.badge_outlined,
                    controller: _idController,
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: _checkPassport,
                      icon: const Icon(Icons.remove_red_eye_outlined),
                      label: Text(S.of(context).show_data),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF002F6C),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                  ),
                  if (_isVerified) ...[
                    const SizedBox(height: 12),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        vertical: 10,
                        horizontal: 16,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.green.shade50,
                        borderRadius: BorderRadius.circular(10),
                        border: Border.all(color: Colors.green.shade200),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.check_circle,
                            color: Colors.green.shade600,
                            size: 18,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            S.of(context).passport_found_successfully,
                            style: TextStyle(
                              color: Colors.green.shade700,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ],
              ),
            ),

            // ===== 2. البيانات الحالية =====
            if (_isVerified && passport != null)
              _buildSectionCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    _buildSectionHeader(
                      title: S.of(context).current_data,
                      subtitle: S.of(context).registered_in_passport,
                      icon: Icons.info_outline,
                      iconColor: Colors.blue,
                    ),
                    const SizedBox(height: 16),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF5F7FA),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: Colors.grey.shade200),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          _buildDataRow(
                            S.of(context).name,
                            "${passport!.firstName} ${passport!.lastName}",
                          ),
                          _buildDataRow(
                            S.of(context).passport_number,
                            passport!.passportNumber,
                          ),
                          _buildDataRow(
                            S.of(context).expiry_date,
                            passport!.expiryDate,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

            // ===== 3. سبب التجديد =====
            if (_isVerified)
              _buildSectionCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    _buildSectionHeader(
                      title: S.of(context).passport_renewal_reason,
                      subtitle: S.of(context).select_passport_renewal_reason,
                      icon: Icons.refresh_rounded,
                      iconColor: Colors.orange,
                    ),
                    const SizedBox(height: 16),
                    Wrap(
                      alignment: WrapAlignment.end,
                      children: _reasons
                          .map((r) => _buildReasonChip(r))
                          .toList(),
                    ),
                  ],
                ),
              ),

            // ===== 4. مستند داعم =====
            if (_isVerified)
              _buildSectionCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    _buildSectionHeader(
                      title: S.of(context).new_personal_photo_optional,
                      subtitle: S.of(context).attach_recent_personal_photo,
                      icon: Icons.upload_file_outlined,
                      iconColor: Colors.orange,
                    ),
                    const SizedBox(height: 16),
                    GestureDetector(
                      onTap: () {},
                      child: Container(
                        width: double.infinity,
                        padding: const EdgeInsets.symmetric(vertical: 20),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF5F7FA),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: Colors.grey.shade300),
                        ),
                        child: Column(
                          children: [
                            Icon(
                              Icons.cloud_upload_outlined,
                              color: Colors.grey.shade400,
                              size: 32,
                            ),
                            const SizedBox(height: 8),
                            Text(
                              S.of(context).attach_photo,
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF002F6C),
                              ),
                            ),
                            Text(
                              S.of(context).max_file_size,
                              style: TextStyle(
                                color: Colors.grey.shade500,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),

            // ===== زر التقديم =====
            if (_isVerified)
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: () {},
                  icon: const Icon(Icons.send),
                  label: Text(
                    S.of(context).submit_passport_renewal_request,
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  ),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF002F6C),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                ),
              ),

            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.lock_outline, size: 14, color: Colors.grey.shade500),
                const SizedBox(width: 4),
                Text(
                  S.of(context).request_result_notification,
                  style: TextStyle(color: Colors.grey.shade500, fontSize: 12),
                ),
              ],
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}
