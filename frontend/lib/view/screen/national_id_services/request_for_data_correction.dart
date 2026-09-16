import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/generated/l10n.dart';

class RequestForDataCorrection extends StatefulWidget {
  const RequestForDataCorrection({super.key});

  @override
  State<StatefulWidget> createState() => _RequestForDataCorrection();
}

class _RequestForDataCorrection extends State<RequestForDataCorrection> {
  final TextEditingController _certNumberController = TextEditingController();
  final TextEditingController _newValueController = TextEditingController();
  final TextEditingController _reasonController = TextEditingController();

  String? _selectedField;
  bool _isVerified = false;

  final List<Map<String, String>> _fields = [
    {"title": "İsim", "subtitle": "Çocuğun adı"},
    {"title": "Doğum Tarihi", "subtitle": "Gün / Ay / Yıl"},
    {"title": "Doğum Yeri", "subtitle": "Şehir veya hastane"},
    {"title": "Baba Adı", "subtitle": "Tam isim"},
    {"title": "Anne Adı", "subtitle": "Tam isim"},
    {"title": "Baba Mesleği", "subtitle": ""},
    {"title": "Anne Mesleği", "subtitle": ""},
    {"title": "Adres", "subtitle": ""},
  ];

  @override
  void dispose() {
    _certNumberController.dispose();
    _newValueController.dispose();
    _reasonController.dispose();
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

  Widget _buildFieldChip(Map<String, String> field) {
    final isSelected = _selectedField == field["title"];
    return GestureDetector(
      onTap: () => setState(() => _selectedField = field["title"]),
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
                  field["title"]!,
                  style: TextStyle(
                    color: isSelected ? Colors.white : const Color(0xFF1a1a2e),
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
            if (field["subtitle"]!.isNotEmpty)
              Text(
                field["subtitle"]!,
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
      backgroundColor: const Color(0xFFF0F4FF),
      appBar: AppBar(
        backgroundColor: const Color(0xFF002F6C),
        foregroundColor: Colors.white,
        title: Text(
          S.of(context).birth_certificate_data_correction,
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        centerTitle: true,
        actions: [
          Container(
            margin: const EdgeInsets.only(left: 12),
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(
              Icons.edit_document,
              color: Colors.white,
              size: 20,
            ),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            _buildSectionCard(
              child: Column(
                children: [
                  _buildSectionHeader(
                    title: S.of(context).document_verification,
                    subtitle: S.of(context).enter_document_number_to_start,
                    icon: Icons.verified_outlined,
                    iconColor: Colors.green,
                  ),
                  const SizedBox(height: 16),
                  _buildInputField(
                    hint: S.of(context).enter_document_number,
                    label: S.of(context).birth_certificate_number,
                    icon: Icons.badge_outlined,
                    controller: _certNumberController,
                  ),
                  const SizedBox(height: 4),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: () => setState(() => _isVerified = true),
                      icon: const Icon(Icons.remove_red_eye_outlined),
                      label: const Text("Verileri Göster"),
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
                            "Belge başarıyla bulundu",
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

            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).what_do_you_want_to_correct,
                    subtitle: S.of(context).select_data_to_correct,
                    icon: Icons.edit_outlined,
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    height: 70,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      reverse: true,
                      itemCount: _fields.length,
                      itemBuilder: (context, index) {
                        return _buildFieldChip(_fields[index]);
                      },
                    ),
                  ),
                ],
              ),
            ),

            if (_selectedField != null)
              _buildSectionCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    _buildSectionHeader(
                      title: S.of(context).current_value,
                      subtitle: S.of(context).registered_in_document,
                      icon: Icons.info_outline,
                      iconColor: AppColors.background,
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
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Icon(Icons.person, color: Colors.grey),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                                "$_selectedField mevcut:",
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 14,
                                ),
                              ),
                              Text(
                                S.of(context).currently_registered_value,
                                style: TextStyle(
                                  color: Colors.grey,
                                  fontSize: 13,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

            if (_selectedField != null)
              _buildSectionCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    _buildSectionHeader(
                      title: S.of(context).new_value,
                      subtitle: S.of(context).enter_correct_information,
                      icon: Icons.edit_note_outlined,
                    ),
                    _buildInputField(
                      hint: "Yeni $_selectedField değerini girin",
                      label: "",
                      icon: Icons.person_outline,
                      controller: _newValueController,
                    ),
                  ],
                ),
              ),

            _buildSectionCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  _buildSectionHeader(
                    title: S.of(context).correction_reason,
                    subtitle: S.of(context).helps_processing_request,
                    icon: Icons.chat_bubble_outline,
                    iconColor: AppColors.background,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _reasonController,
                    maxLines: 3,
                    textAlign: TextAlign.right,
                    decoration: InputDecoration(
                      hintText: S.of(context).name_correction_example,
                      hintStyle: const TextStyle(
                        color: Colors.grey,
                        fontSize: 13,
                      ),
                      filled: true,
                      fillColor: const Color(0xFFF5F7FA),
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
                        borderSide: BorderSide(color: Colors.grey.shade200),
                      ),
                    ),
                  ),
                ],
              ),
            ),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () {},
                icon: const Icon(Icons.send),
                label: Text(
                  S.of(context).submit_correction_request,
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
                  "Başvuru incelenecek ve sonuç size bildirilecektir",
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
    required String label,
    required IconData icon,
    required TextEditingController controller,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        if (label.isNotEmpty)
          Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.bold,
              color: Color(0xFF1a1a2e),
            ),
          ),
        if (label.isNotEmpty) const SizedBox(height: 6),
        TextFormField(
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
        const SizedBox(height: 8),
      ],
    );
  }
}
