<?php
/*
Plugin Name: Cimy User Extra Fields
Plugin URI: http://www.marcocimmino.net/cimy-wordpress-plugins/cimy-user-extra-fields/
Description: Add some useful fields to registration and user's info
Version: 2.5.3
Author: Marco Cimmino
Author URI: mailto:cimmino.marco@gmail.com
License: GPL2

Cimy User Extra Fields - Allows adding mySQL Data fields to store/add more user info
Copyright (c) 2006-2013 Marco Cimmino

Code for drop-down support is in part from Raymond Elferink raymond@raycom.com
Code for regular expression under equalTo rule is in part from Shane Hartman shane@shanehartman.com

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


The full copy of the GNU General Public License is available here: http://www.gnu.org/licenses/gpl.txt

*/

// added for WordPress >=2.5 compatibility
global $wpdb, $old_wpdb_data_table, $wpdb_data_table, $old_wpdb_fields_table, $wpdb_fields_table, $wpdb_wp_fields_table, $cimy_uef_options, $cimy_uef_version, $cuef_upload_path, $cimy_uef_domain, $cimy_uef_plugins_dir;

if (!function_exists('cimy_uef_set_tables')) :
function cimy_uef_set_tables() {
	global $wpdb, $old_wpdb_data_table, $wpdb_data_table, $old_wpdb_fields_table, $wpdb_fields_table, $wpdb_wp_fields_table, $cimy_uef_options, $cimy_uef_version, $cuef_upload_path, $cimy_uef_domain, $cimy_uef_plugins_dir;
	$prefix = $wpdb->prefix;

	if (is_multisite()) {
		$cimy_uef_plugins_dir = __FILE__;
		if (!stristr($cimy_uef_plugins_dir, "mu-plugins") === false)
		{
			$prefix = $wpdb->base_prefix;
			$cimy_uef_plugins_dir = "mu-plugins";
		}
		else
			$cimy_uef_plugins_dir = "plugins";
	}

	$old_wpdb_data_table = $prefix."cimy_data";
	$old_wpdb_fields_table = $prefix."cimy_fields";

	$wpdb_data_table = $prefix."cimy_uef_data";
	$wpdb_fields_table = $prefix."cimy_uef_fields";
	$wpdb_wp_fields_table = $prefix."cimy_uef_wp_fields";
}
endif;

cimy_uef_set_tables();

$cimy_uef_options = "cimy_uef_options";
$cimy_uef_options_descr = "Cimy User Extra Fields options are stored here and modified only by admin";

/*

RULES (stored into an associative array and serialized):

- 'min_length':			[int]		=> specify min length
[only for text, textarea, textarea-rich, password, picture, picture-url, avatar, file]

- 'exact_length':		[int]		=> specify exact length
[only for text, textarea, textarea-rich, password, picture, picture-url, avatar, file]

- 'max_length':			[int]		=> specify max length
[only for text, textarea, textarea-rich, password, picture, picture-url, avatar, file]

- 'email':			[true | false]	=> check or not for email syntax
[only for text, textarea, textarea-rich, password]

- 'can_be_empty':		[true | false]	=> field can or cannot be empty
[only for text, textarea, textarea-rich, password, picture, picture-url, dropdown, dropdown-multi, avatar, file]

- 'edit':
	'ok_edit' 				=> field can be modified
	'edit_only_if_empty' 			=> field can be modified if it's still empty
	'edit_only_by_admin' 			=> field can be modified only by administrator
	'edit_only_by_admin_or_if_empty' 	=> field can be modified only by administrator or if it's still empty
	'no_edit' 				=> field cannot be modified
[only for text, textarea, textarea-rich, password, picture, picture-url, checkbox, radio, dropdown, dropdown-multi, avatar, file]
[for radio and checkbox 'edit_only_if_empty' has no effects and 'edit_only_by_admin_or_if_empty' has the same effect as edit_only_by_admin]

- 'equal_to':			[string] => field should be equal to a specify string
[all except avatar]

- 'equal_to_case_sensitive':	[true | false] => equal_to if selected can be case sensitive or not
[only for text, textarea, textarea-rich, password, dropdown, dropdown-multi]

- 'equal_to_regex':             [true | false] => equal_to if selected must match regular expression specified in value
[only for text, textarea, textarea-rich, password, dropdown, dropdown-multi]

- 'show_in_reg':		[true | false]	=> field is visible or not in the registration
[all]

- 'show_in_profile':		[true | false]	=> field is visible or not in user's profile
[all]

- 'show_in_aeu':		[true | false]	=> field is visible or not in Users Extended page
[all]

TYPE can be:
- 'text'
- 'textarea'
- 'textarea-rich'
- 'password'
- 'checkbox'
- 'radio'
- 'dropdown'
- 'dropdown-multi'
- 'picture'
- 'picture-url'
- 'registration-date'
- 'avatar'
- 'file'

*/

// pre 2.6 compatibility or if not defined
if (!defined("WP_CONTENT_DIR"))
	define("WP_CONTENT_DIR", ABSPATH."/wp_content");

$cuef_plugin_name = basename(__FILE__);
$cuef_plugin_path = plugin_basename(dirname(__FILE__))."/";
$cuef_upload_path = WP_CONTENT_DIR."/Cimy_User_Extra_Fields/";
$cuef_upload_webpath = content_url("Cimy_User_Extra_Fields/");
// this is more accurate to detect plug-in path, some people might even rename /plugins/
$cuef_plugin_dir = plugin_dir_path(__FILE__);
$cimy_uef_plugins_dirprefix = "";
if ($cimy_uef_plugins_dir == "mu-plugins")
	$cimy_uef_plugins_dirprefix = "cimy-user-extra-fields/";

$cuef_plugin_dir.= $cimy_uef_plugins_dirprefix;

// let's use plugins_url function to build urls, takes in account https too
$cuef_css_webpath = plugins_url($cimy_uef_plugins_dirprefix."css", __FILE__);
$cuef_js_webpath = plugins_url($cimy_uef_plugins_dirprefix."js", __FILE__);
$cuef_securimage_webpath = plugins_url($cimy_uef_plugins_dirprefix."securimage", __FILE__);

require_once($cuef_plugin_dir.'/cimy_uef_init.php');
require_once($cuef_plugin_dir.'/cimy_uef_email_handler.php');
require_once($cuef_plugin_dir.'/cimy_uef_db.php');
require_once($cuef_plugin_dir.'/cimy_uef_register.php');
require_once($cuef_plugin_dir.'/cimy_uef_functions.php');
require_once($cuef_plugin_dir.'/cimy_uef_profile.php');

add_action('admin_init', 'cimy_uef_admin_init');
add_action('init', 'cimy_uef_init');

$cimy_uef_name = "Cimy User Extra Fields";
$cimy_uef_version = "2.5.3";
$cimy_uef_url = "http://www.marcocimmino.net/cimy-wordpress-plugins/cimy-user-extra-fields/";
$cimy_project_url = "http://www.marcocimmino.net/cimy-wordpress-plugins/support-the-cimy-project-paypal/";

$start_cimy_uef_comment = "<!--\n";
$start_cimy_uef_comment .= "\tStart code from ".$cimy_uef_name." ".$cimy_uef_version."\n";
$start_cimy_uef_comment .= "\tCopyright (c) 2006-2013 Marco Cimmino\n";
$start_cimy_uef_comment .= "\t".$cimy_uef_url."\n";
$start_cimy_uef_comment .= "-->\n";

$end_cimy_uef_comment = "\n<!--\n";
$end_cimy_uef_comment .= "\tEnd of code from ".$cimy_uef_name."\n";
$end_cimy_uef_comment .= "-->\n";

$cimy_uef_domain = 'cimy_uef';
$cimy_uef_i18n_is_setup = false;
cimy_uef_i18n_setup();

// if (is_multisite())
// 	$wp_password_description = "";
// else
// 	$wp_password_description = __('<strong>Note:</strong> this website let you personalize your password; after the registration you will receive an e-mail with another password, do not care about that!', $cimy_uef_domain);

$wp_hidden_fields = array(
			'username' => array(
						'name' => "USERNAME",
						'userdata_name' => "user_login",
						'post_name' => "user_login",
						'type' => "text",
						'label' => __("Username"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => false,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => true,
								'show_in_blog' => true,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'password' => array(
						'name' => "PASSWORD",
						'userdata_name' => "user_pass",
						'post_name' => "pass1",
						'type' => "password",
						'label' => __("Password"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => false,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => false,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'password2' => array(
						'name' => "PASSWORD2",
						'userdata_name' => "user_pass2",
						'post_name' => "pass2",
						'type' => "password",
						'label' => __("Password confirmation", $cimy_uef_domain),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => false,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => false,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'firstname' => array(
						'name' => "FIRSTNAME",
						'userdata_name' => "first_name",
						'post_name' => "first_name",
						'type' => "text",
						'label' => __("First name"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'lastname' => array(
						'name' => "LASTNAME",
						'userdata_name' => "last_name",
						'post_name' => "last_name",
						'type' => "text",
						'label' => __("Last name"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'nickname' => array(
						'name' => "NICKNAME",
						'userdata_name' => "nickname",
						'post_name' => "nickname",
						'type' => "text",
						'label' => __("Nickname"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'website' => array(
						'name' => "WEBSITE",
						'userdata_name' => "user_url",
						'post_name' => "url",
						'type' => "text",
						'label' => __("Website"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'aim' => array(
						'name' => "AIM",
						'userdata_name' => "aim",
						'post_name' => "aim",
						'type' => "text",
						'label' => __("AIM"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'yahoo' => array(
						'name' => "YAHOO",
						'userdata_name' => "yim",
						'post_name' => "yim",
						'type' => "text",
						'label' => __("Yahoo IM"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'jgt' => array(
						'name' => "JGT",
						'userdata_name' => "jabber",
						'post_name' => "jabber",
						'type' => "text",
						'label' => __("Jabber / Google Talk"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 100,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			'bio-info' => array(
						'name' => "BIO-INFO",
						'userdata_name' => "description",
						'post_name' => "description",
						'type' => "textarea",
						'label' => __("Biographical Info"),
						'desc' => '',
						'value' => '',
						'store_rule' => array(
								'max_length' => 5000,
								'can_be_empty' => true,
								'edit' => 'ok_edit',
								'email' => false,
								'show_in_reg' => true,
								'show_in_profile' => true,
								'show_in_aeu' => true,
								'show_in_search' => false,
								'show_in_blog' => false,
								'show_level' => -1,
								'advanced_options' => '',
								),
					),
			);

// all available types
$available_types = array("text", "textarea", "textarea-rich", "password", "checkbox", "radio", "dropdown", "dropdown-multi", "picture", "picture-url", "registration-date", "avatar", "file");

// types that should be pass registration check for equal to rule
$apply_equalto_rule = array("text", "textarea", "textarea-rich", "password", "checkbox", "radio", "dropdown", "dropdown-multi");

// types that can have 'can be empty' rule
$rule_canbeempty = array("text", "textarea", "textarea-rich", "password", "picture", "picture-url", "dropdown", "dropdown-multi", "avatar", "file");

// common for min, exact and max length
$rule_maxlen = array("text", "password", "textarea", "textarea-rich", "picture", "picture-url", "avatar", "file");

// common for min, exact and max length
$rule_maxlen_needed = array("text", "password", "picture", "picture-url", "avatar", "file");

// types that can have 'check for email syntax' rule
$rule_email = array("text", "textarea", "textarea-rich", "password");

// types that can have cannot be empty rule
$rule_cannot_be_empty = array("text", "textarea", "textarea-rich", "password", "dropdown", "dropdown-multi", "picture", "picture-url", "registration-date", "avatar", "file");

// types that can admit a default value if empty
$rule_profile_value = array("text", "textarea", "textarea-rich", "password", "picture", "picture-url", "avatar", "file", "checkbox", "radio", "dropdown", "dropdown-multi");

// types that can have 'equal to' rule
$rule_equalto = array("text", "textarea", "textarea-rich", "password", "checkbox", "radio", "dropdown", "dropdown-multi", "picture", "picture-url", "registration-date", "file");

// types that can have 'case (in)sensitive equal to' rule
$rule_equalto_case_sensitive = array("text", "textarea", "textarea-rich", "password", "dropdown", "dropdown-multi");

// types that can have regex equal to rule
$rule_equalto_regex  = array("text", "textarea", "textarea-rich", "password", "dropdown", "dropdown-multi");

// types that are file to be uploaded
$cimy_uef_file_types = array("picture", "avatar", "file");

// types that are images to be uploaded
$cimy_uef_file_images_types = array("picture", "avatar");

// types that are textarea and needs rows and cols attributes
$cimy_uef_textarea_types = array("textarea", "textarea-rich");

$max_length_name = 20;
$max_length_label = 50000;
$max_length_desc = 50000;
$max_length_value = 50000;
$max_length_fieldset_value = 1024;
$max_length_extra_fields_title = 100;

// max size in KiloByte
$max_size_file = 20000;

$fields_name_prefix = "cimy_uef_";
$wp_fields_name_prefix = "cimy_uef_wp_";

// added for WordPress MU support
if (is_multisite()) {
	if ($cimy_uef_plugins_dir == "mu-plugins") {
		// since WP 3.1 this is how is called the super admin menu
		add_action('network_admin_menu', 'cimy_admin_menu_custom');
	}
	else {
		// function that add all submenus
		add_action('admin_menu', 'cimy_admin_menu_custom');
	}

	// when blog is switched we need to re-set the table's names
	add_action('switch_blog', 'cimy_uef_blog_switched', 10, 2);

	// add action to delete all files/images when deleting a blog
	add_action('delete_blog', 'cimy_delete_blog_info', 10, 2);

	// add filter to modify signup URL for WordPress MU where plug-in is installed per blog
	add_filter('wp_signup_location', 'cimy_change_signup_location');

	// add extra fields to registration form
	add_action('signup_extra_fields', 'cimy_registration_form', 1);

	// add checks for extra fields in the registration form only
	if (!is_admin() && !is_network_admin())
		add_filter('wpmu_validate_user_signup', 'cimy_registration_check_mu_wrapper');

	// add custom login/registration css
	add_action('signup_header', 'cimy_uef_register_css');

	// FIXME seems not needed
	//add_action('signup_finished', 'cimy_register_user_extra_fields');

	// add extra fields to wp_signups waiting for confirmation
	add_filter('add_signup_meta', 'cimy_register_user_extra_fields_signup_meta');

	// add engine for hidden extra fields to 2nd stage user's registration
	add_action('signup_hidden_fields', 'cimy_register_user_extra_hidden_fields_stage2');

	// add engine for extra fields to 2nd stage blog's registration
	//add_action('signup_blogform', 'cimy_register_user_extra_fields_stage2');

	// FIXME: seems not needed
	//add_action('preprocess_signup_form', 'cimy_registration_check');

	// add update engine for extra fields to user's registration (user only)
	add_action('wpmu_activate_user', 'cimy_register_user_extra_fields', 10, 3);

	// add update engine for extra fields to user's registration (user and blog)
	add_action('wpmu_activate_blog', 'cimy_register_user_extra_fields_mu_wrapper', 10, 5);

	// filters for adding fields to user email notification
	add_filter('update_welcome_email', 'cimy_uef_welcome_blog_to_user', 10, 6);
	add_filter('update_welcome_user_email', 'cimy_uef_welcome_user_to_user', 10, 4);

	// filters for adding fields to admin email notification
	add_filter('newblog_notify_siteadmin', 'cimy_uef_welcome_blog_to_admin');
	add_filter('newuser_notify_siteadmin', 'cimy_uef_welcome_user_to_admin');

}
// if is NOT multisite
else {
	// function that add all submenus
	add_action('admin_menu', 'cimy_admin_menu_custom');

	// add checks for extra fields in the registration form
	add_action('register_post', 'cimy_registration_check', 10, 3);
	add_action('register_post', 'cimy_registration_captcha_check', 11, 3);
	
	// add extra fields to registration form
	add_action('register_form', 'cimy_registration_form', 1);

	// add custom login/registration css
	add_action('login_enqueue_scripts', 'cimy_uef_register_css');

	// add custom login/registration logo
	add_action('login_head', 'cimy_change_login_registration_logo');

	// add confirmation form
	// WARNING: this trick will trigger the registration twice!
	add_action('login_form_register', 'cimy_confirmation_form');

	// add filter for email activation
	add_filter('login_message', 'cimy_uef_activate');

	// add update engine for extra fields to user's registration
	add_action('user_register', 'cimy_register_user_extra_fields');

	// add filter to redirect after registration
	add_filter('registration_redirect', 'cimy_uef_registration_redirect');
	// this is needed only in the case where both redirection and email confirmation has been enabled
	add_action('login_form_cimy_uef_redirect', 'cimy_uef_redirect');

	// add filter to replace the username with the email
	add_filter('sanitize_user', 'cimy_uef_sanitize_username', 1, 3);
	add_filter('validate_username', 'cimy_uef_validate_username', 1, 2);
}

// with Theme My Login is more complicated, but we know how to workaround it
add_action('wp_enqueue_scripts', 'cimy_uef_theme_my_login_fix', 15);

// add javascripts to profile edit
add_action('admin_print_scripts-user-edit.php', 'cimy_uef_admin_profile_init_js');
add_action('admin_print_scripts-profile.php', 'cimy_uef_admin_profile_init_js');

// add filter for random generated password
add_filter('random_password', 'cimy_register_overwrite_password');

// add checks for extra fields in the profile form
add_action('user_profile_update_errors', 'cimy_profile_check_wrapper', 10, 3);

// add extra fields to user's profile
add_action('show_user_profile', 'cimy_extract_ExtraFields');

// add extra fields in users edit profiles (for ADMIN)
add_action('edit_user_profile', 'cimy_extract_ExtraFields');

// this hook is no more used since the one below is enough for all
//add_action('personal_options_update', 'cimy_update_ExtraFields');

// add update engine for extra fields to users edit profiles
add_action('profile_update', 'cimy_update_ExtraFields');

// function that is executed during activation of the plug-in
add_action('activate_'.$cuef_plugin_path.$cuef_plugin_name,'cimy_plugin_install');

// function that add all submenus
// add_action('admin_menu', 'cimy_admin_menu_custom');

// delete user extra fields data when a user is deleted
add_action('delete_user', 'cimy_delete_user_info');

// add avatar filter
add_filter('get_avatar', 'cimy_uef_avatar_filter', 1, 5);
