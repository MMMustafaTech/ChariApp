import 'package:flutter/material.dart';
import 'package:frontend/core/constant/app_colors.dart';
import 'package:frontend/generated/l10n.dart';

class MyAccount extends StatefulWidget {
  const MyAccount({super.key});

  @override
  State<MyAccount> createState() => _MyAccount();
}

class _MyAccount extends State<MyAccount> {
  int _selectedIndex = 3;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: Text(S.of(context).my_account),
        centerTitle: true,
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.primary,
      ),
      bottomNavigationBar: _buildBottomNav(),
    );
  }

  Widget _buildBottomNav() {
    return Container(
      height: 75,
      decoration: const BoxDecoration(color: AppColors.background),
      child: Column(
        children: [
          SizedBox(
            height: 4,
            child: Row(
              children: [
                Expanded(
                  flex: 5,
                  child: Container(color: AppColors.background),
                ),
                Expanded(
                  flex: 5,
                  child: Container(color: AppColors.buttonColor),
                ),
                Expanded(flex: 5, child: Container(color: AppColors.error)),
              ],
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildNavIcon(Icons.home, 0, S.of(context).home),
              _buildNavIcon(Icons.apps_outlined, 1, ""),
              _buildNavIcon(
                Icons.assignment_outlined,
                2,
                S.of(context).my_requests,
              ),
              _buildNavIcon(Icons.person_outline, 3, S.of(context).my_account),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildNavIcon(IconData icon, int index, String label) {
    final isSelected = _selectedIndex == index;
    return InkWell(
      onTap: () {
        setState(() => _selectedIndex = index);
        switch (index) {
          case 0:
            Navigator.of(context).pushReplacementNamed("home");
            break;
          case 1:
            Navigator.of(context).pushReplacementNamed("OtherServices");
            break;
          case 2:
            Navigator.of(context).pushReplacementNamed("MyRequests");
            break;
          case 3:
            break;
        }
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              color: isSelected
                  ? AppColors.containerBackground
                  : Colors.white70,
              size: 26,
            ),
            const SizedBox(height: 3),
            Text(
              label,
              style: TextStyle(
                color: isSelected
                    ? AppColors.containerBackground
                    : Colors.white70,
                fontSize: 10,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
