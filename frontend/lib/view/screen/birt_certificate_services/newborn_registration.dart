import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/generated/l10n.dart';

class NewbornRegistration extends StatefulWidget {
  const NewbornRegistration({super.key});

  @override
  State<NewbornRegistration> createState() => _NewbornRegistration();
}

class _NewbornRegistration extends State<NewbornRegistration> {
  String? _selectedField;
  String? _selectedGender;

  List<Map<String, String>> get _fields => [
    {
      "title": S.of(context).father,
      "subtitle": S.of(context).newborn_registration_request,
    },
    {
      "title": S.of(context).mother,
      "subtitle": S.of(context).newborn_registration_request,
    },
  ];

  List<Map<String, String>> get _genders => [
    {"title": S.of(context).male},
    {"title": S.of(context).female},
  ];

  final TextEditingController _fatherIdController = TextEditingController();
  final TextEditingController _motherIdController = TextEditingController();
  final TextEditingController _babyNameController = TextEditingController();
  final TextEditingController _hospitalController = TextEditingController();
  final TextEditingController _cityController = TextEditingController();
  final TextEditingController _countryController = TextEditingController();
  final TextEditingController _birthDateController = TextEditingController();

  @override
  void dispose() {
    _fatherIdController.dispose();
    _motherIdController.dispose();
    _babyNameController.dispose();
    _hospitalController.dispose();
    _cityController.dispose();
    _countryController.dispose();
    _birthDateController.dispose();
    super.dispose();
  }

  Widget _buildSectionHeader({
    required String title,
    required String subtitle,
  }) {
    return Column(
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
        if (subtitle.isNotEmpty)
          Text(
            subtitle,
            style: const TextStyle(fontSize: 12, color: Colors.grey),
          ),
      ],
    );
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

  Widget _buildChip({
    required String title,
    required bool isSelected,
    required VoidCallback onTap,
    String subtitle = "",
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(left: 8),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF002F6C) : Colors.white,
          border: Border.all(
            color: isSelected ? const Color(0xFF002F6C) : Colors.grey.shade300,
          ),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  isSelected
                      ? Icons.radio_button_checked
                      : Icons.radio_button_off,
                  size: 14,
                  color: isSelected ? Colors.white : Colors.grey,
                ),
                const SizedBox(width: 6),
                Text(
                  title,
                  style: TextStyle(
                    color: isSelected ? Colors.white : const Color(0xFF1a1a2e),
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
            if (subtitle.isNotEmpty)
              Text(
                subtitle,
                style: TextStyle(
                  color: isSelected ? Colors.white70 : Colors.grey,
                  fontSize: 10,
                ),
              ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      appBar: AppBar(
        foregroundColor: AppColors.primary,
        backgroundColor: AppColors.background,
        title: Text(
          S.of(context).newborn_registration,
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).applicant,
                    subtitle: S.of(context).select_applicant,
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    height: 70,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      reverse: true,
                      itemCount: _fields.length,
                      itemBuilder: (context, index) {
                        final field = _fields[index];
                        return _buildChip(
                          title: field["title"]!,
                          subtitle: field["subtitle"]!,
                          isSelected: _selectedField == field["title"],
                          onTap: () =>
                              setState(() => _selectedField = field["title"]),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),

            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).parent_identity,
                    subtitle: S.of(context).enter_parent_ids,
                  ),
                  const SizedBox(height: 12),
                  _buildInputField(
                    hint: S.of(context).father_national_id,
                    icon: Icons.person_outline,
                    controller: _fatherIdController,
                  ),
                  _buildInputField(
                    hint: S.of(context).mother_national_id,
                    icon: Icons.person_outline,
                    controller: _motherIdController,
                  ),
                ],
              ),
            ),

            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).newborn_information,
                    subtitle: S.of(context).enter_all_information,
                  ),
                  const SizedBox(height: 16),

                  _buildInputField(
                    hint: S.of(context).baby_name,
                    icon: Icons.person,
                    controller: _babyNameController,
                  ),
                  const SizedBox(height: 10),

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
                  SizedBox(
                    height: 50,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      reverse: true,
                      itemCount: _genders.length,
                      itemBuilder: (context, index) {
                        final gender = _genders[index];
                        return _buildChip(
                          title: gender["title"]!,
                          isSelected: _selectedGender == gender["title"],
                          onTap: () =>
                              setState(() => _selectedGender = gender["title"]),
                        );
                      },
                    ),
                  ),

                  const SizedBox(height: 25),

                  _buildInputField(
                    hint: S.of(context).hospital,
                    icon: Icons.local_hospital_outlined,
                    controller: _hospitalController,
                  ),

                  _buildInputField(
                    hint: S.of(context).city,
                    icon: Icons.location_city_outlined,
                    controller: _cityController,
                  ),

                  _buildInputField(
                    hint: S.of(context).country,
                    icon: Icons.flag_outlined,
                    controller: _countryController,
                  ),

                  _buildInputField(
                    hint: S.of(context).birth_date,
                    icon: Icons.calendar_today_outlined,
                    controller: _birthDateController,
                  ),
                ],
              ),
            ),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () {},
                label: Text(
                  S.of(context).submit_registration_request,
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.containerBackground,
                  foregroundColor: AppColors.background,
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
                Icon(Icons.lock_outline, size: 14, color: Colors.grey),
                const SizedBox(width: 4),
                Text(
                  S.of(context).request_will_be_reviewed,
                  style: TextStyle(color: Colors.grey, fontSize: 12),
                ),
              ],
            ),
            const SizedBox(height: 20),
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
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: TextFormField(
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
      ),
    );
  }
}
