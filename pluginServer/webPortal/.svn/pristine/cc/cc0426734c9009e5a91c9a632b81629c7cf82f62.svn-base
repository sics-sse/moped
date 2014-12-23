<?php

function cimy_uef_i18n_setup() {
	global $cimy_uef_domain, $cimy_uef_i18n_is_setup, $cuef_plugin_path, $cimy_uef_plugins_dir;

	if ($cimy_uef_i18n_is_setup)
		return;

	// Stupid function, from relative path I need to go down because starts from WP_PLUGIN_DIR!
	if (is_multisite())
		load_plugin_textdomain($cimy_uef_domain, false, '../'.$cimy_uef_plugins_dir.'/'.$cuef_plugin_path.'langs');
	else
		load_plugin_textdomain($cimy_uef_domain, false, $cuef_plugin_path.'langs');
}

function cimy_admin_menu_custom() {
	global $cimy_uef_name, $cimy_uef_domain, $cimy_top_menu;

	$aue_page = "";
	if (isset($cimy_top_menu) && (!is_multisite())) {
		add_submenu_page('cimy_series.php', $cimy_uef_name.": ".__("Options"), "UEF: ".__("Options"), 'manage_options', "user_extra_fields_options", 'cimy_show_options_notembedded');
		add_submenu_page('cimy_series.php', $cimy_uef_name.": ".__("Fields", $cimy_uef_domain), "UEF: ".__("Fields", $cimy_uef_domain), 'manage_options', "user_extra_fields", 'cimy_admin_define_extra_fields');
		$aue_page = add_submenu_page('profile.php', __('Users Extended', $cimy_uef_domain), __('Users Extended', $cimy_uef_domain), 'list_users', "users_extended", 'cimy_admin_users_list_page');
	}
	else {
		if (cimy_uef_is_multisite_unique_installation()) {
// 			$aue_page = add_submenu_page('wpmu-admin.php', __("Users Extended", $cimy_uef_domain), __("Users Extended", $cimy_uef_domain), 'list_users', "users_extended", 'cimy_admin_users_list_page');
// 			add_submenu_page('wpmu-admin.php', $cimy_uef_name, $cimy_uef_name, 'manage_options', "user_extra_fields", 'cimy_admin_define_extra_fields');

			// Since WP 3.1 we have network admin and everything seems changed
			$aue_page = add_submenu_page('users.php', __("Users Extended", $cimy_uef_domain), __("Users Extended", $cimy_uef_domain), 'list_users', "users_extended", 'cimy_admin_users_list_page');
			$admin = add_submenu_page('settings.php', $cimy_uef_name, $cimy_uef_name, 'manage_options', "user_extra_fields", 'cimy_admin_define_extra_fields');
		}
		else {
			$admin = add_options_page($cimy_uef_name, $cimy_uef_name, 'manage_options', "user_extra_fields", 'cimy_admin_define_extra_fields');
			$aue_page = add_submenu_page('profile.php', __('Users Extended', $cimy_uef_domain), __('Users Extended', $cimy_uef_domain), 'list_users', "users_extended", 'cimy_admin_users_list_page');
		}
	}
	if (!empty($aue_page))
		add_action('admin_print_scripts-'.$aue_page, 'cimy_uef_admin_ajax_edit');
	if (!empty($admin))
		add_action('admin_print_scripts-'.$admin, 'cimy_uef_admin_init_js');
}

function cimy_uef_admin_init() {
	global $cuef_js_webpath, $cuef_plugin_dir;
	require_once($cuef_plugin_dir.'/cimy_uef_admin.php');
	require_once($cuef_plugin_dir.'/cimy_uef_options.php');
	// add code to handle new value from ajax code in A&U Extended
	add_action('wp_ajax_save-extra-field-new-value', 'cimy_uef_admin_ajax_save_ef_new_value');

	wp_register_script("cimy_uef_invert_sel", $cuef_js_webpath."/invert_sel.js", array(), false);
	wp_register_script("cimy_uef_ajax_new_value", $cuef_js_webpath."/ajax_new_value.js", array(), false);
}

function cimy_uef_init() {
	if (!cimy_uef_is_register_page())
		return;
	$options = cimy_get_options();
	if ($options['captcha'] == "securimage")
		session_start();
}

function cimy_uef_admin_init_js() {
	wp_enqueue_script("cimy_uef_invert_sel");
	cimy_uef_init_upload_js();
}

function cimy_uef_init_upload_js() {
	global $cuef_js_webpath;
	wp_register_script("cimy_uef_upload_file", $cuef_js_webpath."/upload_file.js", array(), false);
	wp_enqueue_script('cimy_uef_upload_file');
}

function cimy_uef_theme_my_login_fix() {
	// Theme My Login spam with its css _all_ pages, we like it cleaner thanks!
	// Seems not needed any longer for TML >= 6.3.x
	if (!empty($GLOBALS['theme_my_login'])) {
		if ($GLOBALS['theme_my_login']->is_login_page())
			cimy_uef_register_css();
	}
	// Theme My Login - Themed Profiles module
	if (cimy_uef_is_theme_my_login_profile_page())
		cimy_uef_admin_profile_init_js();
}

function cimy_uef_register_css() {
	global $cuef_css_webpath;
	if (!cimy_uef_is_register_page())
		return;
	wp_register_style("cimy_uef_register", $cuef_css_webpath."/cimy_uef_register.css", false, false);
	wp_enqueue_style("cimy_uef_register");
	wp_register_style("cimy_uef_register_nousername", $cuef_css_webpath."/cimy_uef_register_nousername.css", false, false);

	if (!is_multisite()) {
		$options = cimy_get_options();
		if (!in_array("username", $options["wp_hidden_fields"])) {
			wp_enqueue_style("cimy_uef_register_nousername");
		}
		if (in_array("password", $options["wp_hidden_fields"])) {
			// this CSS will hide the label "A password will be e-mailed to you."
			wp_register_style("cimy_uef_register_nopasswordlabel", $cuef_css_webpath."/cimy_uef_register_nopasswordlabel.css", false, false);
			wp_enqueue_style("cimy_uef_register_nopasswordlabel");
		}
	}

	cimy_uef_init_javascripts("show_in_reg");
	// needed till they fix this bug: http://core.trac.wordpress.org/ticket/17916#comment:18
	wp_print_styles();
}

function cimy_uef_admin_profile_init_js() {
	cimy_uef_init_javascripts("show_in_profile");
}

function cimy_uef_init_javascripts($rule_name) {
	global $cuef_plugin_dir, $cuef_css_webpath, $cuef_js_webpath;

	$options = cimy_get_options();
	if ($options['image_fields'][$rule_name] > 0) {
		wp_enqueue_script('imgareaselect', "", array("jquery"));
		wp_enqueue_style('imgareaselect');
		wp_register_script('cimy_uef_img_selection', $cuef_js_webpath."/img_selection.js", array(), false);
		wp_enqueue_script('cimy_uef_img_selection');
	}

	if ($options['file_fields'][$rule_name] > 0) {
		cimy_uef_init_upload_js();
	}

	if ($rule_name == "show_in_profile") {
		if ($options['tinymce_fields'][$rule_name] > 0 && function_exists("wp_editor")) {
			wp_register_style("cimy_uef_tinymce", $cuef_css_webpath."/cimy_uef_tinymce.css", false, false);
			wp_enqueue_style('cimy_uef_tinymce');
		}
	}
	if ($rule_name == "show_in_reg") {
		// This is needed for registration form on WordPress >= 3.3
		if ($options['tinymce_fields'][$rule_name] > 0 && function_exists("wp_editor")) {
			wp_enqueue_script('jquery');
			wp_enqueue_script('utils');
		}

		if ($options['password_meter']) {
			wp_register_script("cimy_uef_password_strength_meter", $cuef_js_webpath."/password_strength_meter.js", array("password-strength-meter"), false);
			wp_enqueue_script('cimy_uef_password_strength_meter');
		}

		// damn WordPress bugs
		if (is_multisite())
			wp_print_scripts();
	}
}
