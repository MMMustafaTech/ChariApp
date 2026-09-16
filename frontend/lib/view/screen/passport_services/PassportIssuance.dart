import 'package:flutter/material.dart';
import 'package:frontend/data/model/user_model.dart';
import 'package:frontend/generated/l10n.dart';

class PassportIssuance extends StatefulWidget {
  const PassportIssuance({super.key});

  @override
  State<PassportIssuance> createState() => _PassportIssuance();
}

class _PassportIssuance extends State<PassportIssuance> {
  UserModel? user;

  final TextEditingController _idController = TextEditingController();
  final TextEditingController _firstNameController = TextEditingController();
  final TextEditingController _lastNameController = TextEditingController();
  final TextEditingController _birthDateController = TextEditingController();
  final TextEditingController _birthPlaceController = TextEditingController();
  final TextEditingController _professionController = TextEditingController();
  final TextEditingController _nationalityController = TextEditingController();

  String? _selectedGender;
  List<String> get _genders => [S.of(context).male, S.of(context).female];

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
    _firstNameController.dispose();
    _lastNameController.dispose();
    _birthDateController.dispose();
    _birthPlaceController.dispose();
    _professionController.dispose();
    _nationalityController.dispose();
    super.dispose();
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
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: iconColor.withOpacity(0.1),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: iconColor, size: 20),
        ),

        const SizedBox(width: 12),

        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                title,
                textAlign: TextAlign.right,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1a1a2e),
                ),
              ),
              const SizedBox(height: 2),
              Text(
                subtitle,
                textAlign: TextAlign.right,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(fontSize: 12, color: Colors.grey),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildGenderChip(String gender) {
    final isSelected = _selectedGender == gender;
    return GestureDetector(
      onTap: () => setState(() => _selectedGender = gender),
      child: Container(
        margin: const EdgeInsets.only(left: 8),
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
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
            Flexible(
              child: Text(
                gender,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: isSelected ? Colors.white : const Color(0xFF1a1a2e),
                  fontWeight: FontWeight.bold,
                  fontSize: 13,
                ),
              ),
            ),
            const SizedBox(width: 6),
            Icon(
              isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
              size: 14,
              color: isSelected ? Colors.white : Colors.grey,
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
    bool readOnly = false,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: TextFormField(
        controller: controller,
        readOnly: readOnly,
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
          S.of(context).passport_issuance_title,
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // ===== رقم الهوية =====
            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).national_id_number,
                    subtitle: S.of(context).linked_to_account,
                    icon: Icons.badge_outlined,
                    iconColor: Colors.green,
                  ),
                  const SizedBox(height: 16),
                  _buildInputField(
                    hint: S.of(context).national_id_number,
                    icon: Icons.badge_outlined,
                    controller: _idController,
                    readOnly: true,
                  ),
                ],
              ),
            ),

            // ===== البيانات الشخصية =====
            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).personal_information,
                    subtitle: S.of(context).passport_personal_information,
                    icon: Icons.person_outline,
                  ),
                  const SizedBox(height: 16),
                  _buildInputField(
                    hint: S.of(context).first_name,
                    icon: Icons.person,
                    controller: _firstNameController,
                  ),
                  _buildInputField(
                    hint: S.of(context).last_name,
                    icon: Icons.person_outline,
                    controller: _lastNameController,
                  ),

                  Align(
                    alignment: Alignment.centerRight,
                    child: Text(
                      S.of(context).gender,
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF1a1a2e),
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: _genders.map((g) => _buildGenderChip(g)).toList(),
                  ),
                  const SizedBox(height: 12),

                  _buildInputField(
                    hint: S.of(context).date_of_birth,
                    icon: Icons.calendar_today_outlined,
                    controller: _birthDateController,
                  ),
                  _buildInputField(
                    hint: S.of(context).place_of_birth,
                    icon: Icons.location_on_outlined,
                    controller: _birthPlaceController,
                  ),
                  _buildInputField(
                    hint: S.of(context).profession,
                    icon: Icons.work_outline,
                    controller: _professionController,
                  ),
                  _buildInputField(
                    hint: S.of(context).nationality,
                    icon: Icons.flag_outlined,
                    controller: _nationalityController,
                  ),
                ],
              ),
            ),

            // ===== المستندات =====
            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).required_documents,
                    subtitle: S.of(context).attach_required_documents,
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
                            Icons.add_a_photo_outlined,
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
                  const SizedBox(height: 12),
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
                            Icons.description_outlined,
                            color: Colors.grey.shade400,
                            size: 32,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            S.of(context).national_id_copy,
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
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () {},
                icon: const Icon(Icons.send),
                label: Text(
                  S.of(context).submit_passport_request,
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
