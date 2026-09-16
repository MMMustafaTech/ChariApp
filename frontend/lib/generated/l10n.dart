// GENERATED CODE - DO NOT MODIFY BY HAND
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'intl/messages_all.dart';

// **************************************************************************
// Generator: Flutter Intl IDE plugin
// Made by Localizely
// **************************************************************************

// ignore_for_file: non_constant_identifier_names, lines_longer_than_80_chars
// ignore_for_file: join_return_with_assignment, prefer_final_in_for_each
// ignore_for_file: avoid_redundant_argument_values, avoid_escaping_inner_quotes

class S {
  S();

  static S? _current;

  static S get current {
    assert(
      _current != null,
      'No instance of S was loaded. Try to initialize the S delegate before accessing S.current.',
    );
    return _current!;
  }

  static const AppLocalizationDelegate delegate = AppLocalizationDelegate();

  static Future<S> load(Locale locale) {
    final name = (locale.countryCode?.isEmpty ?? false)
        ? locale.languageCode
        : locale.toString();
    final localeName = Intl.canonicalizedLocale(name);
    return initializeMessages(localeName).then((_) {
      Intl.defaultLocale = localeName;
      final instance = S();
      S._current = instance;

      return instance;
    });
  }

  static S of(BuildContext context) {
    final instance = S.maybeOf(context);
    assert(
      instance != null,
      'No instance of S present in the widget tree. Did you add S.delegate in localizationsDelegates?',
    );
    return instance!;
  }

  static S? maybeOf(BuildContext context) {
    return Localizations.of<S>(context, S);
  }

  /// `Chad Digital`
  String get app_name {
    return Intl.message('Chad Digital', name: 'app_name', desc: '', args: []);
  }

  /// `Welcome to the Platform`
  String get welcome_message {
    return Intl.message(
      'Welcome to the Platform',
      name: 'welcome_message',
      desc: '',
      args: [],
    );
  }

  /// `Login`
  String get login_button {
    return Intl.message('Login', name: 'login_button', desc: '', args: []);
  }

  /// `Sign In`
  String get login_title {
    return Intl.message('Sign In', name: 'login_title', desc: '', args: []);
  }

  /// `National ID Number`
  String get national_id {
    return Intl.message(
      'National ID Number',
      name: 'national_id',
      desc: '',
      args: [],
    );
  }

  /// `Password`
  String get password {
    return Intl.message('Password', name: 'password', desc: '', args: []);
  }

  /// `Confirm Password`
  String get confirm_password {
    return Intl.message(
      'Confirm Password',
      name: 'confirm_password',
      desc: '',
      args: [],
    );
  }

  /// `Forgot Password?`
  String get forgot_password {
    return Intl.message(
      'Forgot Password?',
      name: 'forgot_password',
      desc: '',
      args: [],
    );
  }

  /// `Create Account`
  String get create_account {
    return Intl.message(
      'Create Account',
      name: 'create_account',
      desc: '',
      args: [],
    );
  }

  /// `Email`
  String get email {
    return Intl.message('Email', name: 'email', desc: '', args: []);
  }

  /// `Other Services`
  String get other_services {
    return Intl.message(
      'Other Services',
      name: 'other_services',
      desc: '',
      args: [],
    );
  }

  /// `Home`
  String get home {
    return Intl.message('Home', name: 'home', desc: '', args: []);
  }

  /// `My Requests`
  String get my_requests {
    return Intl.message('My Requests', name: 'my_requests', desc: '', args: []);
  }

  /// `My Account`
  String get my_account {
    return Intl.message('My Account', name: 'my_account', desc: '', args: []);
  }

  /// `Hello`
  String get greeting {
    return Intl.message('Hello', name: 'greeting', desc: '', args: []);
  }

  /// `Welcome to Chad Digital Platform`
  String get platform_welcome {
    return Intl.message(
      'Welcome to Chad Digital Platform',
      name: 'platform_welcome',
      desc: '',
      args: [],
    );
  }

  /// `View All`
  String get view_all {
    return Intl.message('View All', name: 'view_all', desc: '', args: []);
  }

  /// `Digital Documents`
  String get digital_documents {
    return Intl.message(
      'Digital Documents',
      name: 'digital_documents',
      desc: '',
      args: [],
    );
  }

  /// `Quick Access`
  String get quick_access {
    return Intl.message(
      'Quick Access',
      name: 'quick_access',
      desc: '',
      args: [],
    );
  }

  /// `National ID`
  String get national_id_card {
    return Intl.message(
      'National ID',
      name: 'national_id_card',
      desc: '',
      args: [],
    );
  }

  /// `Passport`
  String get passport {
    return Intl.message('Passport', name: 'passport', desc: '', args: []);
  }

  /// `Driving License`
  String get driving_license {
    return Intl.message(
      'Driving License',
      name: 'driving_license',
      desc: '',
      args: [],
    );
  }

  /// `Birth Certificate`
  String get birth_certificate {
    return Intl.message(
      'Birth Certificate',
      name: 'birth_certificate',
      desc: '',
      args: [],
    );
  }

  /// `Civil Registry Appointments`
  String get civil_registry_appointments {
    return Intl.message(
      'Civil Registry Appointments',
      name: 'civil_registry_appointments',
      desc: '',
      args: [],
    );
  }

  /// `National ID Services`
  String get national_id_services {
    return Intl.message(
      'National ID Services',
      name: 'national_id_services',
      desc: '',
      args: [],
    );
  }

  /// `Birth Certificate Services`
  String get birth_certificate_services {
    return Intl.message(
      'Birth Certificate Services',
      name: 'birth_certificate_services',
      desc: '',
      args: [],
    );
  }

  /// `Passport Appointments`
  String get passport_appointments {
    return Intl.message(
      'Passport Appointments',
      name: 'passport_appointments',
      desc: '',
      args: [],
    );
  }

  /// `Passport Services`
  String get passport_services {
    return Intl.message(
      'Passport Services',
      name: 'passport_services',
      desc: '',
      args: [],
    );
  }

  /// `My Profile`
  String get profile {
    return Intl.message('My Profile', name: 'profile', desc: '', args: []);
  }

  /// `My Digital Documents`
  String get my_digital_documents {
    return Intl.message(
      'My Digital Documents',
      name: 'my_digital_documents',
      desc: '',
      args: [],
    );
  }

  /// `Logout`
  String get logout {
    return Intl.message('Logout', name: 'logout', desc: '', args: []);
  }

  /// `Other`
  String get other {
    return Intl.message('Other', name: 'other', desc: '', args: []);
  }

  /// `My Requests`
  String get my_demands {
    return Intl.message('My Requests', name: 'my_demands', desc: '', args: []);
  }

  /// `Passport Services`
  String get passport_services_title {
    return Intl.message(
      'Passport Services',
      name: 'passport_services_title',
      desc: '',
      args: [],
    );
  }

  /// `Welcome`
  String get welcome {
    return Intl.message('Welcome', name: 'welcome', desc: '', args: []);
  }

  /// `Select the desired service`
  String get select_service {
    return Intl.message(
      'Select the desired service',
      name: 'select_service',
      desc: '',
      args: [],
    );
  }

  /// `Passport Renewal`
  String get passport_renewal {
    return Intl.message(
      'Passport Renewal',
      name: 'passport_renewal',
      desc: '',
      args: [],
    );
  }

  /// `Passport Issuance`
  String get passport_issuance {
    return Intl.message(
      'Passport Issuance',
      name: 'passport_issuance',
      desc: '',
      args: [],
    );
  }

  /// `Data Correction Request`
  String get data_correction_request {
    return Intl.message(
      'Data Correction Request',
      name: 'data_correction_request',
      desc: '',
      args: [],
    );
  }

  /// `Lost or Damaged Passport`
  String get lost_or_damaged_passport {
    return Intl.message(
      'Lost or Damaged Passport',
      name: 'lost_or_damaged_passport',
      desc: '',
      args: [],
    );
  }

  /// `View Passport`
  String get view_passport {
    return Intl.message(
      'View Passport',
      name: 'view_passport',
      desc: '',
      args: [],
    );
  }

  /// `This document is official and digitally certified`
  String get official_digital_document {
    return Intl.message(
      'This document is official and digitally certified',
      name: 'official_digital_document',
      desc: '',
      args: [],
    );
  }

  /// `Loading...`
  String get loading {
    return Intl.message('Loading...', name: 'loading', desc: '', args: []);
  }

  /// `View National ID`
  String get view_national_id {
    return Intl.message(
      'View National ID',
      name: 'view_national_id',
      desc: '',
      args: [],
    );
  }

  /// `Birth Certificate Issuance`
  String get birth_certificate_issuance {
    return Intl.message(
      'Birth Certificate Issuance',
      name: 'birth_certificate_issuance',
      desc: '',
      args: [],
    );
  }

  /// `Newborn Registration`
  String get newborn_registration {
    return Intl.message(
      'Newborn Registration',
      name: 'newborn_registration',
      desc: '',
      args: [],
    );
  }

  /// `Birth Certificate Issuance`
  String get birth_certificate_view {
    return Intl.message(
      'Birth Certificate Issuance',
      name: 'birth_certificate_view',
      desc: '',
      args: [],
    );
  }

  /// `View Birth Certificate`
  String get view_birth_certificate {
    return Intl.message(
      'View Birth Certificate',
      name: 'view_birth_certificate',
      desc: '',
      args: [],
    );
  }

  /// `Birth Certificate Data Correction`
  String get birth_certificate_data_correction {
    return Intl.message(
      'Birth Certificate Data Correction',
      name: 'birth_certificate_data_correction',
      desc: '',
      args: [],
    );
  }

  /// `Document Verification`
  String get document_verification {
    return Intl.message(
      'Document Verification',
      name: 'document_verification',
      desc: '',
      args: [],
    );
  }

  /// `Enter the document number to begin`
  String get enter_document_number_to_start {
    return Intl.message(
      'Enter the document number to begin',
      name: 'enter_document_number_to_start',
      desc: '',
      args: [],
    );
  }

  /// `Enter Document Number`
  String get enter_document_number {
    return Intl.message(
      'Enter Document Number',
      name: 'enter_document_number',
      desc: '',
      args: [],
    );
  }

  /// `Birth Certificate Number *`
  String get birth_certificate_number {
    return Intl.message(
      'Birth Certificate Number *',
      name: 'birth_certificate_number',
      desc: '',
      args: [],
    );
  }

  /// `Show Data`
  String get show_data {
    return Intl.message('Show Data', name: 'show_data', desc: '', args: []);
  }

  /// `Document found successfully`
  String get document_found_successfully {
    return Intl.message(
      'Document found successfully',
      name: 'document_found_successfully',
      desc: '',
      args: [],
    );
  }

  /// `What would you like to correct?`
  String get what_do_you_want_to_correct {
    return Intl.message(
      'What would you like to correct?',
      name: 'what_do_you_want_to_correct',
      desc: '',
      args: [],
    );
  }

  /// `Select the data to be corrected`
  String get select_data_to_correct {
    return Intl.message(
      'Select the data to be corrected',
      name: 'select_data_to_correct',
      desc: '',
      args: [],
    );
  }

  /// `Child Name`
  String get child_name {
    return Intl.message('Child Name', name: 'child_name', desc: '', args: []);
  }

  /// `Date of Birth`
  String get date_of_birth {
    return Intl.message(
      'Date of Birth',
      name: 'date_of_birth',
      desc: '',
      args: [],
    );
  }

  /// `Place of Birth`
  String get place_of_birth {
    return Intl.message(
      'Place of Birth',
      name: 'place_of_birth',
      desc: '',
      args: [],
    );
  }

  /// `Father Name`
  String get father_name {
    return Intl.message('Father Name', name: 'father_name', desc: '', args: []);
  }

  /// `Mother Name`
  String get mother_name {
    return Intl.message('Mother Name', name: 'mother_name', desc: '', args: []);
  }

  /// `Father Profession`
  String get father_profession {
    return Intl.message(
      'Father Profession',
      name: 'father_profession',
      desc: '',
      args: [],
    );
  }

  /// `Mother Profession`
  String get mother_profession {
    return Intl.message(
      'Mother Profession',
      name: 'mother_profession',
      desc: '',
      args: [],
    );
  }

  /// `Address`
  String get address {
    return Intl.message('Address', name: 'address', desc: '', args: []);
  }

  /// `Current Value`
  String get current_value {
    return Intl.message(
      'Current Value',
      name: 'current_value',
      desc: '',
      args: [],
    );
  }

  /// `Value registered in the document`
  String get registered_in_document {
    return Intl.message(
      'Value registered in the document',
      name: 'registered_in_document',
      desc: '',
      args: [],
    );
  }

  /// `Currently registered value`
  String get currently_registered_value {
    return Intl.message(
      'Currently registered value',
      name: 'currently_registered_value',
      desc: '',
      args: [],
    );
  }

  /// `New Value`
  String get new_value {
    return Intl.message('New Value', name: 'new_value', desc: '', args: []);
  }

  /// `Enter the correct information`
  String get enter_correct_information {
    return Intl.message(
      'Enter the correct information',
      name: 'enter_correct_information',
      desc: '',
      args: [],
    );
  }

  /// `Reason for Correction`
  String get correction_reason {
    return Intl.message(
      'Reason for Correction',
      name: 'correction_reason',
      desc: '',
      args: [],
    );
  }

  /// `Helps process the request`
  String get helps_processing_request {
    return Intl.message(
      'Helps process the request',
      name: 'helps_processing_request',
      desc: '',
      args: [],
    );
  }

  /// `Example: Correcting the name to match the National ID`
  String get name_correction_example {
    return Intl.message(
      'Example: Correcting the name to match the National ID',
      name: 'name_correction_example',
      desc: '',
      args: [],
    );
  }

  /// `Submit Correction Request`
  String get submit_correction_request {
    return Intl.message(
      'Submit Correction Request',
      name: 'submit_correction_request',
      desc: '',
      args: [],
    );
  }

  /// `The request will be reviewed and you will be notified of the result`
  String get request_will_be_reviewed {
    return Intl.message(
      'The request will be reviewed and you will be notified of the result',
      name: 'request_will_be_reviewed',
      desc: '',
      args: [],
    );
  }

  /// `Applicant`
  String get applicant {
    return Intl.message('Applicant', name: 'applicant', desc: '', args: []);
  }

  /// `Select who will submit the request`
  String get select_applicant {
    return Intl.message(
      'Select who will submit the request',
      name: 'select_applicant',
      desc: '',
      args: [],
    );
  }

  /// `Father`
  String get father {
    return Intl.message('Father', name: 'father', desc: '', args: []);
  }

  /// `Mother`
  String get mother {
    return Intl.message('Mother', name: 'mother', desc: '', args: []);
  }

  /// `Newborn Registration Request`
  String get newborn_registration_request {
    return Intl.message(
      'Newborn Registration Request',
      name: 'newborn_registration_request',
      desc: '',
      args: [],
    );
  }

  /// `Parents Information`
  String get parent_identity {
    return Intl.message(
      'Parents Information',
      name: 'parent_identity',
      desc: '',
      args: [],
    );
  }

  /// `Enter the parents' ID numbers`
  String get enter_parent_ids {
    return Intl.message(
      'Enter the parents\' ID numbers',
      name: 'enter_parent_ids',
      desc: '',
      args: [],
    );
  }

  /// `Father National ID`
  String get father_national_id {
    return Intl.message(
      'Father National ID',
      name: 'father_national_id',
      desc: '',
      args: [],
    );
  }

  /// `Mother National ID`
  String get mother_national_id {
    return Intl.message(
      'Mother National ID',
      name: 'mother_national_id',
      desc: '',
      args: [],
    );
  }

  /// `Newborn Information`
  String get newborn_information {
    return Intl.message(
      'Newborn Information',
      name: 'newborn_information',
      desc: '',
      args: [],
    );
  }

  /// `Enter all information`
  String get enter_all_information {
    return Intl.message(
      'Enter all information',
      name: 'enter_all_information',
      desc: '',
      args: [],
    );
  }

  /// `Baby Name`
  String get baby_name {
    return Intl.message('Baby Name', name: 'baby_name', desc: '', args: []);
  }

  /// `Gender`
  String get gender {
    return Intl.message('Gender', name: 'gender', desc: '', args: []);
  }

  /// `Male`
  String get male {
    return Intl.message('Male', name: 'male', desc: '', args: []);
  }

  /// `Female`
  String get female {
    return Intl.message('Female', name: 'female', desc: '', args: []);
  }

  /// `Hospital`
  String get hospital {
    return Intl.message('Hospital', name: 'hospital', desc: '', args: []);
  }

  /// `City`
  String get city {
    return Intl.message('City', name: 'city', desc: '', args: []);
  }

  /// `Country`
  String get country {
    return Intl.message('Country', name: 'country', desc: '', args: []);
  }

  /// `Birth Date`
  String get birth_date {
    return Intl.message('Birth Date', name: 'birth_date', desc: '', args: []);
  }

  /// `Submit Registration Request`
  String get submit_registration_request {
    return Intl.message(
      'Submit Registration Request',
      name: 'submit_registration_request',
      desc: '',
      args: [],
    );
  }

  /// `National ID Renewal`
  String get national_id_renewal {
    return Intl.message(
      'National ID Renewal',
      name: 'national_id_renewal',
      desc: '',
      args: [],
    );
  }

  /// `Identity Verification`
  String get identity_verification {
    return Intl.message(
      'Identity Verification',
      name: 'identity_verification',
      desc: '',
      args: [],
    );
  }

  /// `Enter the National ID number for verification`
  String get enter_national_id_for_verification {
    return Intl.message(
      'Enter the National ID number for verification',
      name: 'enter_national_id_for_verification',
      desc: '',
      args: [],
    );
  }

  /// `National ID Number`
  String get national_id_number {
    return Intl.message(
      'National ID Number',
      name: 'national_id_number',
      desc: '',
      args: [],
    );
  }

  /// `Show Data`
  String get show_data_botton {
    return Intl.message(
      'Show Data',
      name: 'show_data_botton',
      desc: '',
      args: [],
    );
  }

  /// `Identity found successfully`
  String get identity_found_successfully {
    return Intl.message(
      'Identity found successfully',
      name: 'identity_found_successfully',
      desc: '',
      args: [],
    );
  }

  /// `Current Data`
  String get current_data {
    return Intl.message(
      'Current Data',
      name: 'current_data',
      desc: '',
      args: [],
    );
  }

  /// `As registered in the National ID`
  String get registered_in_national_id {
    return Intl.message(
      'As registered in the National ID',
      name: 'registered_in_national_id',
      desc: '',
      args: [],
    );
  }

  /// `Name`
  String get name {
    return Intl.message('Name', name: 'name', desc: '', args: []);
  }

  /// `Card Number`
  String get card_number {
    return Intl.message('Card Number', name: 'card_number', desc: '', args: []);
  }

  /// `Expiry Date`
  String get expiry_date {
    return Intl.message('Expiry Date', name: 'expiry_date', desc: '', args: []);
  }

  /// `Renewal Reason`
  String get renewal_reason {
    return Intl.message(
      'Renewal Reason',
      name: 'renewal_reason',
      desc: '',
      args: [],
    );
  }

  /// `Select the reason for renewal`
  String get select_renewal_reason {
    return Intl.message(
      'Select the reason for renewal',
      name: 'select_renewal_reason',
      desc: '',
      args: [],
    );
  }

  /// `Card Expired`
  String get card_expired {
    return Intl.message(
      'Card Expired',
      name: 'card_expired',
      desc: '',
      args: [],
    );
  }

  /// `Update Personal Photo`
  String get update_personal_photo {
    return Intl.message(
      'Update Personal Photo',
      name: 'update_personal_photo',
      desc: '',
      args: [],
    );
  }

  /// `Other Reasons`
  String get other_reasons {
    return Intl.message(
      'Other Reasons',
      name: 'other_reasons',
      desc: '',
      args: [],
    );
  }

  /// `New Personal Photo (Optional)`
  String get new_personal_photo {
    return Intl.message(
      'New Personal Photo (Optional)',
      name: 'new_personal_photo',
      desc: '',
      args: [],
    );
  }

  /// `Attach a recent personal photo`
  String get attach_recent_photo {
    return Intl.message(
      'Attach a recent personal photo',
      name: 'attach_recent_photo',
      desc: '',
      args: [],
    );
  }

  /// `Attach Photo (JPG or PNG)`
  String get attach_photo {
    return Intl.message(
      'Attach Photo (JPG or PNG)',
      name: 'attach_photo',
      desc: '',
      args: [],
    );
  }

  /// `Maximum size: 5 MB`
  String get max_file_size {
    return Intl.message(
      'Maximum size: 5 MB',
      name: 'max_file_size',
      desc: '',
      args: [],
    );
  }

  /// `Submit Renewal Request`
  String get submit_renewal_request {
    return Intl.message(
      'Submit Renewal Request',
      name: 'submit_renewal_request',
      desc: '',
      args: [],
    );
  }

  /// `Your request will be reviewed and you will be notified of the result`
  String get request_result_notification {
    return Intl.message(
      'Your request will be reviewed and you will be notified of the result',
      name: 'request_result_notification',
      desc: '',
      args: [],
    );
  }

  /// `Lost or Damaged ID Replacement`
  String get lost_or_damaged_replacement {
    return Intl.message(
      'Lost or Damaged ID Replacement',
      name: 'lost_or_damaged_replacement',
      desc: '',
      args: [],
    );
  }

  /// `Request Reason`
  String get request_reason {
    return Intl.message(
      'Request Reason',
      name: 'request_reason',
      desc: '',
      args: [],
    );
  }

  /// `Select the reason for replacement`
  String get select_replacement_reason {
    return Intl.message(
      'Select the reason for replacement',
      name: 'select_replacement_reason',
      desc: '',
      args: [],
    );
  }

  /// `Lost`
  String get lost {
    return Intl.message('Lost', name: 'lost', desc: '', args: []);
  }

  /// `Damaged`
  String get damaged {
    return Intl.message('Damaged', name: 'damaged', desc: '', args: []);
  }

  /// `Loss Details`
  String get loss_details {
    return Intl.message(
      'Loss Details',
      name: 'loss_details',
      desc: '',
      args: [],
    );
  }

  /// `Damage Details`
  String get damage_details {
    return Intl.message(
      'Damage Details',
      name: 'damage_details',
      desc: '',
      args: [],
    );
  }

  /// `Brief description of the case (Optional)`
  String get brief_case_description {
    return Intl.message(
      'Brief description of the case (Optional)',
      name: 'brief_case_description',
      desc: '',
      args: [],
    );
  }

  /// `Example: The ID was lost on ...`
  String get lost_id_example {
    return Intl.message(
      'Example: The ID was lost on ...',
      name: 'lost_id_example',
      desc: '',
      args: [],
    );
  }

  /// `Example: The card was damaged because of ...`
  String get damaged_id_example {
    return Intl.message(
      'Example: The card was damaged because of ...',
      name: 'damaged_id_example',
      desc: '',
      args: [],
    );
  }

  /// `Loss Report (Optional)`
  String get loss_report_optional {
    return Intl.message(
      'Loss Report (Optional)',
      name: 'loss_report_optional',
      desc: '',
      args: [],
    );
  }

  /// `Damaged Card Photo (Optional)`
  String get damaged_card_photo_optional {
    return Intl.message(
      'Damaged Card Photo (Optional)',
      name: 'damaged_card_photo_optional',
      desc: '',
      args: [],
    );
  }

  /// `Attach a supporting document`
  String get attach_supporting_document {
    return Intl.message(
      'Attach a supporting document',
      name: 'attach_supporting_document',
      desc: '',
      args: [],
    );
  }

  /// `Attach File (Image or PDF)`
  String get attach_file {
    return Intl.message(
      'Attach File (Image or PDF)',
      name: 'attach_file',
      desc: '',
      args: [],
    );
  }

  /// `Submit Request`
  String get submit_request {
    return Intl.message(
      'Submit Request',
      name: 'submit_request',
      desc: '',
      args: [],
    );
  }

  /// `Passport Issuance`
  String get passport_issuance_title {
    return Intl.message(
      'Passport Issuance',
      name: 'passport_issuance_title',
      desc: '',
      args: [],
    );
  }

  /// `Linked to your account`
  String get linked_to_account {
    return Intl.message(
      'Linked to your account',
      name: 'linked_to_account',
      desc: '',
      args: [],
    );
  }

  /// `Personal Information`
  String get personal_information {
    return Intl.message(
      'Personal Information',
      name: 'personal_information',
      desc: '',
      args: [],
    );
  }

  /// `Enter your information as it should appear in the passport`
  String get passport_personal_information {
    return Intl.message(
      'Enter your information as it should appear in the passport',
      name: 'passport_personal_information',
      desc: '',
      args: [],
    );
  }

  /// `First Name`
  String get first_name {
    return Intl.message('First Name', name: 'first_name', desc: '', args: []);
  }

  /// `Last Name`
  String get last_name {
    return Intl.message('Last Name', name: 'last_name', desc: '', args: []);
  }

  /// `Required Documents`
  String get required_documents {
    return Intl.message(
      'Required Documents',
      name: 'required_documents',
      desc: '',
      args: [],
    );
  }

  /// `Attach the required documents`
  String get attach_required_documents {
    return Intl.message(
      'Attach the required documents',
      name: 'attach_required_documents',
      desc: '',
      args: [],
    );
  }

  /// `Copy of National ID`
  String get national_id_copy {
    return Intl.message(
      'Copy of National ID',
      name: 'national_id_copy',
      desc: '',
      args: [],
    );
  }

  /// `Submit Passport Request`
  String get submit_passport_request {
    return Intl.message(
      'Submit Passport Request',
      name: 'submit_passport_request',
      desc: '',
      args: [],
    );
  }

  /// `Nationality`
  String get nationality {
    return Intl.message('Nationality', name: 'nationality', desc: '', args: []);
  }

  /// `Profession`
  String get profession {
    return Intl.message('Profession', name: 'profession', desc: '', args: []);
  }

  /// `Passport Renewal`
  String get passport_renewal_title {
    return Intl.message(
      'Passport Renewal',
      name: 'passport_renewal_title',
      desc: '',
      args: [],
    );
  }

  /// `Passport Verification`
  String get passport_verification {
    return Intl.message(
      'Passport Verification',
      name: 'passport_verification',
      desc: '',
      args: [],
    );
  }

  /// `Enter the National ID number for verification`
  String get enter_national_id_for_passport_verification {
    return Intl.message(
      'Enter the National ID number for verification',
      name: 'enter_national_id_for_passport_verification',
      desc: '',
      args: [],
    );
  }

  /// `Passport found successfully`
  String get passport_found_successfully {
    return Intl.message(
      'Passport found successfully',
      name: 'passport_found_successfully',
      desc: '',
      args: [],
    );
  }

  /// `Current Passport Data`
  String get current_passport_data {
    return Intl.message(
      'Current Passport Data',
      name: 'current_passport_data',
      desc: '',
      args: [],
    );
  }

  /// `As registered in the passport`
  String get registered_in_passport {
    return Intl.message(
      'As registered in the passport',
      name: 'registered_in_passport',
      desc: '',
      args: [],
    );
  }

  /// `Passport Number`
  String get passport_number {
    return Intl.message(
      'Passport Number',
      name: 'passport_number',
      desc: '',
      args: [],
    );
  }

  /// `Renewal Reason`
  String get passport_renewal_reason {
    return Intl.message(
      'Renewal Reason',
      name: 'passport_renewal_reason',
      desc: '',
      args: [],
    );
  }

  /// `Select the reason for renewal`
  String get select_passport_renewal_reason {
    return Intl.message(
      'Select the reason for renewal',
      name: 'select_passport_renewal_reason',
      desc: '',
      args: [],
    );
  }

  /// `Passport Expired`
  String get passport_expired {
    return Intl.message(
      'Passport Expired',
      name: 'passport_expired',
      desc: '',
      args: [],
    );
  }

  /// `Passport Expiring Soon`
  String get passport_expiring_soon {
    return Intl.message(
      'Passport Expiring Soon',
      name: 'passport_expiring_soon',
      desc: '',
      args: [],
    );
  }

  /// `New Personal Photo (Optional)`
  String get new_personal_photo_optional {
    return Intl.message(
      'New Personal Photo (Optional)',
      name: 'new_personal_photo_optional',
      desc: '',
      args: [],
    );
  }

  /// `Attach a recent personal photo`
  String get attach_recent_personal_photo {
    return Intl.message(
      'Attach a recent personal photo',
      name: 'attach_recent_personal_photo',
      desc: '',
      args: [],
    );
  }

  /// `Submit Renewal Request`
  String get submit_passport_renewal_request {
    return Intl.message(
      'Submit Renewal Request',
      name: 'submit_passport_renewal_request',
      desc: '',
      args: [],
    );
  }
}

class AppLocalizationDelegate extends LocalizationsDelegate<S> {
  const AppLocalizationDelegate();

  List<Locale> get supportedLocales {
    return const <Locale>[
      Locale.fromSubtags(languageCode: 'en'),
      Locale.fromSubtags(languageCode: 'ar'),
      Locale.fromSubtags(languageCode: 'fr'),
      Locale.fromSubtags(languageCode: 'tr'),
    ];
  }

  @override
  bool isSupported(Locale locale) => _isSupported(locale);
  @override
  Future<S> load(Locale locale) => S.load(locale);
  @override
  bool shouldReload(AppLocalizationDelegate old) => false;

  bool _isSupported(Locale locale) {
    for (var supportedLocale in supportedLocales) {
      if (supportedLocale.languageCode == locale.languageCode) {
        return true;
      }
    }
    return false;
  }
}
