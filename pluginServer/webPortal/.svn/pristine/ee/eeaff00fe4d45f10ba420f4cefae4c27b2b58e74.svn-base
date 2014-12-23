<?php

function cimy_register_user_extra_hidden_fields_stage2() {
	global $start_cimy_uef_comment, $end_cimy_uef_comment;

	if (empty($_POST))
		return;

	echo "\n".$start_cimy_uef_comment;
	foreach ($_POST as $name=>$value) {
		if (!(stristr($name, "cimy_uef_")) === FALSE) {
			// dropdown-multi will be an Array of selected elements
			if (is_array($value))
				$value = implode(',', $value);
			echo "\t\t<input type=\"hidden\" name=\"".$name."\" value=\"".esc_attr($value)."\" />\n";
		} else if ($name == "blog_id" || $name == "from_blog_id") {
			echo "\t\t<input type=\"hidden\" name=\"".$name."\" value=\"".esc_attr($value)."\" />\n";
		}
	}
	wp_nonce_field('confirm_form', 'confirm_form_nonce');
	echo $end_cimy_uef_comment;
}

function cimy_register_user_extra_fields_signup_meta($meta) {

	foreach ($_POST as $name=>$value) {
		if (!(stristr($name, "cimy_uef_")) === FALSE) {
			// dropdown-multi will be an Array of selected elements
			if (is_array($value))
				$value = implode(',', $value);
			$meta[$name] = $value;
		} else if ($name == "blog_id" || $name == "from_blog_id") {
			$meta[$name] = $value;
		}
	}

	return $meta;
}

function cimy_register_user_extra_fields_mu_wrapper($blog_id, $user_id, $password, $signup, $meta) {
	cimy_register_user_extra_fields($user_id, $password, $meta);
}

function cimy_register_overwrite_password($password) {
	global $wpdb;

	if (!is_multisite()) {
		if (isset($_POST["cimy_uef_wp_PASSWORD"]))
			$password = $_POST["cimy_uef_wp_PASSWORD"];
	}
	else {
		if (!empty($_GET['key']))
			$key = $_GET['key'];
		else if (!empty($_POST['key']))
			$key = $_POST['key'];

		if (!empty($key)) {
			// seems useless since this code cannot be reached with a bad key anyway you never know
			$key = $wpdb->escape($key);

			$sql = "SELECT active, meta FROM ".$wpdb->signups." WHERE activation_key='".$key."'";
			$data = $wpdb->get_results($sql);

			// is there something?
			if (isset($data[0])) {
				// if not already active
				if (!$data[0]->active) {
					$meta = unserialize($data[0]->meta);

					if (!empty($meta["cimy_uef_wp_PASSWORD"])) {
						$password = $meta["cimy_uef_wp_PASSWORD"];
					}
				}
			}
		}
	}

	return $password;
}

function cimy_register_user_extra_fields($user_id, $password="", $meta=array()) {
	global $wpdb_data_table, $wpdb, $max_length_value, $fields_name_prefix, $wp_fields_name_prefix, $wp_hidden_fields, $cimy_uef_file_types, $user_level, $cimy_uef_file_images_types;

	if (isset($meta["blog_id"]) || isset($meta["from_blog_id"]))
		cimy_switch_to_blog($meta);

	// avoid to save stuff if user created from wp_create_user function
	if ((empty($meta)) && (empty($_POST)))
		return;

	// avoid to save stuff if user is being added from: /wp-admin/user-new.php and shit WP 3.1 changed the value just to create new bugs :@
	if (!empty($_POST["action"]) && ($_POST["action"] == "adduser" || $_POST["action"] == "createuser"))
		return;

	$my_user_level = $user_level;

	// -1 == anonymous
	if (!is_user_logged_in())
		$my_user_level = -1;

	$options = cimy_get_options();
	$extra_fields = get_cimyFields(false, true);
	$wp_fields = get_cimyFields(true);

	$user_signups = false;
	if ((!is_multisite()) && ($options["confirm_email"]) && (empty($meta)))
		$user_signups = true;

	// ok ok this is yet another call from wp_create_user function under cimy_uef_activate_signup, we are not yet ready for this, aboooort!
	if ($user_signups) {
		$user = new WP_User((int) $user_id);
		$signup = $wpdb->get_row($wpdb->prepare("SELECT * FROM ".$wpdb->prefix."signups WHERE user_login = %s AND active = 0", $user->user_login));
		if (!empty($signup))
			return;
	}
	if (!empty($meta)) {
		$user = new WP_User((int) $user_id);
		$meta_db = $wpdb->get_var($wpdb->prepare("SELECT meta FROM ".$wpdb->prefix."signups WHERE user_login = %s", $user->user_login));
		$meta_db = unserialize($meta_db);
		// password detected, kill it!
		if (!empty($meta_db['cimy_uef_wp_PASSWORD'])) {
			unset($meta_db['cimy_uef_wp_PASSWORD']);
			if (!empty($meta_db['cimy_uef_wp_PASSWORD2']))
				unset($meta_db['cimy_uef_wp_PASSWORD2']);
			$wpdb->update($wpdb->prefix."signups", array('meta' => serialize($meta_db)), array('user_login' => $user->user_login));
		}
	}

	$i = 1;

	// do first for the WP fields then for EXTRA fields
	while ($i <= 2) {
		if ($i == 1) {
			$are_wp_fields = true;
			$fields = $wp_fields;
			$prefix = $wp_fields_name_prefix;
		}
		else {
			$are_wp_fields = false;
			$fields = $extra_fields;
			$prefix = $fields_name_prefix;
		}

		foreach ($fields as $thisField) {
			$type = $thisField["TYPE"];
			$name = $thisField["NAME"];
			$field_id = $thisField["ID"];
			$label = $thisField["LABEL"];
			$rules = $thisField["RULES"];
			$unique_id = $prefix.$field_id;
			$input_name = $prefix.esc_attr($name);
			$field_id_data = $input_name."_".$field_id."_data";
			$advanced_options = cimy_uef_parse_advanced_options($rules["advanced_options"]);

			// if the current user LOGGED IN has not enough permissions to see the field, skip it
			if ($rules['show_level'] == 'view_cimy_extra_fields')
			{
				if (!current_user_can($rules['show_level']))
					continue;
			}
			else if ($my_user_level < $rules['show_level'])
				continue;

			// if show_level == anonymous then do NOT ovverride other show_xyz rules
			if ($rules['show_level'] == -1) {
				// if flag to show the field in the registration is NOT activated, skip it
				if (!$rules['show_in_reg'])
					continue;
			}

			// uploading a file is not supported when confirmation email is enabled (on MS is turned on by default yes)
			if (((is_multisite()) || ($options["confirm_email"])) && (in_array($type, $cimy_uef_file_types)))
				continue;

			if (isset($meta[$input_name]))
				$data = stripslashes($meta[$input_name]);
			else if (isset($_POST[$input_name])) {
				// if form confirmation is enabled then there is no more an array but a string!
				if (($type == "dropdown-multi") && (is_array($_POST[$input_name])))
					$data = stripslashes(implode(",", $_POST[$input_name]));
				else
					$data = stripslashes($_POST[$input_name]);
			}
			else
				$data = "";

			if ($type == "avatar") {
				// since avatars are drawn max to 512px then we can save bandwith resizing, do it!
				$rules['equal_to'] = 512;
			}

			if (in_array($type, $cimy_uef_file_types)) {
				$user_login_sanitized = sanitize_user($_POST['user_login']);
				if ((isset($_POST["register_confirmation"])) && ($_POST["register_confirmation"] == 2)) {
					$temp_user_login = $_POST["temp_user_login"];
					$temp_dir = cimy_uef_get_dir_or_filename($temp_user_login);
					$final_dir = cimy_uef_get_dir_or_filename($user_login_sanitized);
					rename($temp_dir, $final_dir);
					$data = str_replace("/".$temp_user_login."/", "/".$user_login_sanitized."/", $data);
					$file_on_server = cimy_uef_get_dir_or_filename($user_login_sanitized, $data, false);

					if (in_array($type, $cimy_uef_file_images_types))
						cimy_uef_crop_image($file_on_server, $field_id_data);
				}
				else
					$data = cimy_manage_upload($input_name, $user_login_sanitized, $rules, false, false, $type, (!empty($advanced_options["filename"])) ? $advanced_options["filename"] : "");
			}
			else {
				if ($type == "picture-url")
					$data = str_replace('../', '', $data);
					
				if (isset($rules['max_length']))
					$data = substr($data, 0, $rules['max_length']);
				else
					$data = substr($data, 0, $max_length_value);
			}
		
			$data = $wpdb->escape($data);

			if ($user_signups)
				$meta[$input_name] = $data;
			else if (!$are_wp_fields) {
				$sql = "INSERT INTO ".$wpdb_data_table." SET USER_ID = ".$user_id.", FIELD_ID=".$field_id.", ";
	
				switch ($type) {
					case 'avatar':
					case 'picture-url':
					case 'picture':
					case 'textarea':
					case 'textarea-rich':
					case 'dropdown':
					case 'dropdown-multi':
					case 'password':
					case 'text':
					case 'file':
						$field_value = $data;
						break;
		
					case 'checkbox':
						$field_value = $data == '1' ? "YES" : "NO";
						break;
		
					case 'radio':
						$field_value = $data == $field_id ? "selected" : "";
						break;
						
					case 'registration-date':
						$field_value = time();
						break;
				}
		
				$sql.= "VALUE='".$field_value."'";
				$wpdb->query($sql);
			}
			else {
				$f_name = strtolower($thisField['NAME']);
				
				$userdata = array();
				$userdata['ID'] = $user_id;
				$userdata[$wp_hidden_fields[$f_name]['userdata_name']] = $data;
				
				wp_update_user($userdata);
			}
		}
		$i++;
	}

	if ($user_signups) {
		$sql = $wpdb->prepare("SELECT * FROM $wpdb->users WHERE ID=%d", $user_id);
		$saved_user = array_shift($wpdb->get_results($sql));
		$key = substr( md5( time() . rand() . $saved_user->user_email ), 0, 16 );

		$wpdb->insert($wpdb->prefix."signups", array(
			'user_login' => $saved_user->user_login,
			'user_email' => $saved_user->user_email,
			'registered' => $saved_user->user_registered,
			'active' => '0',
			'activation_key' => $key,
			'meta' => serialize($meta),
		));
		$sql = $wpdb->prepare("DELETE FROM $wpdb->users WHERE ID=%d", $user_id);
		$wpdb->query($sql);

		$sql = $wpdb->prepare("DELETE FROM $wpdb->usermeta WHERE user_id=%d", $user_id);
		$wpdb->query($sql);

		cimy_signup_user_notification($saved_user->user_login, $saved_user->user_email, $key, serialize($meta));
	}

	cimy_switch_current_blog(true);
}

function cimy_registration_check_mu_wrapper($data) {
	$user_login = $data['user_name'];
	$user_email = $data['user_email'];
	$errors = $data['errors'];

	// no we don't want to check again at this stage
	if ((!empty($_REQUEST['stage']) && $_REQUEST['stage'] == "validate-blog-signup") && !empty($_REQUEST['confirm_form_nonce']) && ($_REQUEST['confirm_form_nonce'] == wp_create_nonce('confirm_form', 'confirm_form_nonce')))
		return $data;

	$errors = cimy_registration_check($user_login, $user_email, $errors);
	$errors = cimy_registration_captcha_check($user_login, $user_email, $errors);
	$data['errors'] = $errors;

	return $data;
}

// added for profile rules check
function cimy_profile_check_wrapper($errors, $update, $user) {
	$errors = cimy_registration_check($user->user_login, $user->user_email, $errors);

	if (!empty($errors))
		$update = false;
}

function cimy_registration_check($user_login, $user_email, $errors) {
	global $wpdb, $rule_canbeempty, $rule_email, $rule_maxlen, $fields_name_prefix, $wp_fields_name_prefix, $rule_equalto_case_sensitive, $apply_equalto_rule, $cimy_uef_domain, $cimy_uef_file_types, $rule_equalto_regex, $user_level, $cimy_uef_file_images_types, $wp_hidden_fields;

	if (cimy_is_at_least_wordpress35())
		cimy_switch_to_blog();
	$options = cimy_get_options();
	if (!in_array("username", $options["wp_hidden_fields"])) {
		// ok username is empty, we are replacing it with the email, don't bother
		if (isset($errors->errors['empty_username']))
			unset($errors->errors['empty_username']);
		// remove username exists error only if email exists error is there covering for us
		if (isset($errors->errors['username_exists']) && isset($errors->errors['email_exists']))
			unset($errors->errors['username_exists']);
	}

	// code for confirmation email check
	if ((!is_multisite()) && ($options["confirm_email"])) {
		$errors = cimy_check_user_on_signups($errors, $user_login, $user_email);
	}
	// avoid to save stuff if user is being added from: /wp-admin/user-new.php and shit WP 3.1 changed the value just to create new bugs :@
	if (!empty($_POST["action"]) && ($_POST["action"] == "adduser" || $_POST["action"] == "createuser"))
		return $errors;

	$my_user_level = $user_level;

	// -1 == anonymous
	if (!is_user_logged_in())
		$my_user_level = -1;

	$extra_fields = get_cimyFields(false, true);
	$wp_fields = get_cimyFields(true);
	$from_profile = false;
	if (!empty($_POST["from"]) && $_POST["from"] == "profile")
		$from_profile = true;
	$i = 1;

	// do first for the WP fields then for EXTRA fields
	while ($i <= 2) {
		if ($i == 1) {
			$fields = $wp_fields;
			$prefix = $wp_fields_name_prefix;
		}
		else {
			$fields = $extra_fields;
			$prefix = $fields_name_prefix;
		}

		foreach ($fields as $thisField) {
			$field_id = $thisField['ID'];
			$name = $thisField['NAME'];
			$rules = $thisField['RULES'];
			$type = $thisField['TYPE'];
			$label = esc_html($thisField['LABEL']);
			$description = $thisField['DESCRIPTION'];
			$unique_id = $prefix.$field_id;
			// Usernames cannot be changed after the registration
			if ($i == 1 && $name == "USERNAME" && $from_profile)
				continue;

			// use WP input name for the username (always) or when updating the profile
			if ($i == 1 && ($name == "USERNAME" || $from_profile))
				$input_name = $wp_hidden_fields[strtolower($name)]['post_name'];
			else
				$input_name = $prefix.esc_attr($name);
			$field_id_data = $input_name."_".$field_id."_data";

			// if the current user LOGGED IN has not enough permissions to see the field, skip it
			if ($rules['show_level'] == 'view_cimy_extra_fields')
			{
				if (!current_user_can($rules['show_level']))
					continue;
			}
			else if ($my_user_level < $rules['show_level'])
				continue;

			// if show_level == anonymous then do NOT ovverride other show_xyz rules
			if ($rules['show_level'] == -1) {
				// if we are updating the profile check correct rule
				if ($from_profile) {
					// if flag to show the field in the profile is NOT activated, skip it
					if (!$rules['show_in_profile'])
						continue;
				} else { // we are registering new user
					// if flag to show the field in the registration is NOT activated, skip it
					if (!$rules['show_in_reg'])
						continue;
				}
			}

			// uploading a file is not supported when confirmation email is enabled (on MS is turned on by default yes)
			if (((is_multisite()) || ($options["confirm_email"])) && (in_array($type, $cimy_uef_file_types)))
				continue;

			if ($from_profile) {
				if ($i == 1) {
					// Do not bother with the rules if encountered an empty password field on profile update
					if ($type == "password")
						continue;
				}
				else {
					$old_value = $_POST[$input_name."_".$field_id."_prev_value"];
					// Hey, no need to check for rules if anyway I can't edit due to low permissions, neeeext!
					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						continue;
				}
			}

			if (isset($_POST[$input_name])) {
				if ($type == "dropdown-multi")
					$value = stripslashes(implode(",", $_POST[$input_name]));
				else
					$value = stripslashes($_POST[$input_name]);
			}
			else
				$value = "";

			if ($type == "dropdown") {
				$ret = cimy_dropDownOptions($label, $value);
				$label = esc_html($ret['label']);
				$html = $ret['html'];
			}

			// upload of a file, avatar or picture
			if (in_array($type, $cimy_uef_file_types)) {
				// confirmation page
				if ((!empty($_POST["register_confirmation"])) && ($_POST["register_confirmation"] == 2)) {
					$file_size = $_POST[$field_id_data."_size"];
					$file_type1 = $_POST[$field_id_data."_type"]; // this can be faked!
					$old_file = "";
					$del_old_file = "";
				}
				else if (!empty($_FILES[$input_name])) {
					// filesize in Byte transformed in KiloByte
					$file_size = $_FILES[$input_name]['size'] / 1024;
					$file_type1 = $_FILES[$input_name]['type']; // this can be faked!
					$value = $_FILES[$input_name]['name'];
					$old_file = $from_profile && !empty($_POST[$input_name."_".$field_id."_prev_value"]) ? $_POST[$input_name."_".$field_id."_prev_value"] : '';
					$del_old_file = $from_profile && !empty($_POST[$input_name."_del"]) ? $_POST[$input_name."_del"] : '';
				}
				else {
					$file_size = 0;
					$file_type1 = "";
					$value = "";
					$old_file = $from_profile ? $_POST[$input_name."_".$field_id."_prev_value"] : '';
					$del_old_file = $from_profile ? $_POST[$input_name."_del"] : '';
				}
			}

			switch ($type) {
				case 'checkbox':
					$value == 1 ? $value = "YES" : $value = "NO";
					break;
				case 'radio':
					intval($value) == intval($field_id) ? $value = "YES" : $value = "NO";
					break;
			}

			// if the flag can be empty is NOT set OR the field is not empty then other check can be useful, otherwise skip all
			if ((!$rules['can_be_empty']) || (!empty($value))) {
				if (($i == 1) && ($input_name == ($prefix."PASSWORD2"))) {
					if ($value != $_POST[$prefix."PASSWORD"])
						$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('does not match.', $cimy_uef_domain));
				}
				if (($rules['email']) && (in_array($type, $rule_email))) {
					if (!is_email($value))
						$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('hasn&#8217;t a correct email syntax.', $cimy_uef_domain));
				}

				if ((!$rules['can_be_empty']) && (in_array($type, $rule_canbeempty)) && (empty($value))) {
					$empty_error = true;

					// IF   1. it's a file type
					// AND  2. there is an old one uploaded
					// AND  3. this old one is not gonna be deleted
					// THEN   do not throw the empty error.
					if ((in_array($type, $cimy_uef_file_types)) && (!empty($old_file)) && (empty($del_old_file)))
						$empty_error = false;

					if ($empty_error)
						$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t be empty.', $cimy_uef_domain));
				}

				if ((isset($rules['equal_to'])) && (in_array($type, $apply_equalto_rule))) {
					$equalTo = $rules['equal_to'];
					// 	if the type is not allowed to be case sensitive
					// 	OR if case sensitive is not checked
					// AND
					// 	if the type is not allowed to be a regex
					// 	OR if regex rule is not set
					// THEN switch to uppercase
					if (((!in_array($type, $rule_equalto_case_sensitive)) || (!$rules['equal_to_case_sensitive'])) && ((!in_array($type, $rule_equalto_regex)) || (!$rules['equal_to_regex']))) {
						$value = strtoupper($value);
						$equalTo = strtoupper($equalTo);
					}

					if ($rules['equal_to_regex']) {
						$equalTo = (($rules['equal_to_case_sensitive']) ? $equalTo.'u' : $equalTo.'iu');
						if (!preg_match($equalTo, $value)) {
							$equalmsg = " ".__("isn&#8217;t correct", $cimy_uef_domain);
							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.$equalmsg.'.');
						}
					}
					else if ($value != $equalTo) {
						if (($type == "radio") || ($type == "checkbox"))
							$equalTo == "YES" ? $equalTo = __("YES", $cimy_uef_domain) : __("NO", $cimy_uef_domain);

						if ($type == "password")
							$equalmsg = " ".__("isn&#8217;t correct", $cimy_uef_domain);
						else
							$equalmsg = ' '.__("should be", $cimy_uef_domain).' '.esc_html($equalTo);

						$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.$equalmsg.'.');
					}
				}

				// CHECK IF IT IS A REAL PICTURE
				if (in_array($type, $cimy_uef_file_images_types)) {
					$allowed_mime_types = get_allowed_mime_types();
					$validate = wp_check_filetype($value, $allowed_mime_types);
					$file_type2 = "";
					if (!empty($validate['type']))
						$file_type2 = $validate['type'];

					if (((stristr($file_type1, "image/") === false) || (stristr($file_type2, "image/") === false)) && (!empty($value))) {
						$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('should be an image.', $cimy_uef_domain));
					}
				}
				else if (in_array($type, $cimy_uef_file_types)) {
					$allowed_mime_types = get_allowed_mime_types();
					$validate = wp_check_filetype($value, $allowed_mime_types);
					$file_type2 = "";
					if (!empty($validate['type']))
						$file_type2 = $validate['type'];

					if (empty($file_type2) && !empty($value)) {
						$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('does not accept this file type.', $cimy_uef_domain));
					}
				}

				// MIN LEN
				if (isset($rules['min_length'])) {
					$minlen = intval($rules['min_length']);

					if (in_array($type, $cimy_uef_file_types)) {
						if ($file_size < $minlen) {

							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t have size less than', $cimy_uef_domain).' '.$minlen.' KB.');
						}
					}
					else {
						if (mb_strlen($value) < $minlen) {

							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t have length less than', $cimy_uef_domain).' '.$minlen.'.');
						}
					}
				}

				// EXACT LEN
				if (isset($rules['exact_length'])) {
					$exactlen = intval($rules['exact_length']);

					if (in_array($type, $cimy_uef_file_types)) {
						if ($file_size != $exactlen) {

							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t have size different than', $cimy_uef_domain).' '.$exactlen.' KB.');
						}
					}
					else {
						if (mb_strlen($value) != $exactlen) {

							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t have length different than', $cimy_uef_domain).' '.$exactlen.'.');
						}
					}
				}

				// MAX LEN
				if (isset($rules['max_length'])) {
					$maxlen = intval($rules['max_length']);

					if (in_array($type, $cimy_uef_file_types)) {
						if ($file_size > $maxlen) {
							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t have size more than', $cimy_uef_domain).' '.$maxlen.' KB.');
						}
					}
					else {
						if (mb_strlen($value) > $maxlen) {
							$errors->add($unique_id, '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.$label.' '.__('couldn&#8217;t have length more than', $cimy_uef_domain).' '.$maxlen.'.');
						}
					}
				}
			}
		}
		$i++;
	}

	if ($options['confirm_form']) {
		if ((empty($errors->errors)) && (isset($_POST["register_confirmation"])) && ($_POST["register_confirmation"] == 1)) {
			$errors->add('register_confirmation', 'true');
		}
	}

	cimy_switch_current_blog();
	return $errors;
}

function cimy_registration_captcha_check($user_login, $user_email, $errors) {
	global $cimy_uef_domain;
	// no we don't want to check again at this stage
	if (!empty($_POST['register_confirmation']) && ($_POST['register_confirmation'] == 2) && (wp_verify_nonce($_REQUEST['confirm_form_nonce'], 'confirm_form')))
		return $errors;
	$options = cimy_get_options();
	if (($options['captcha'] == "recaptcha") && (!empty($options['recaptcha_private_key']))) {
		$recaptcha_code_ok = false;

		if (!empty($_POST["recaptcha_response_field"])) {
			global $cuef_plugin_dir;
			require_once($cuef_plugin_dir.'/recaptcha/recaptchalib.php');

			$recaptcha_resp = recaptcha_check_answer($options["recaptcha_private_key"],
							$_SERVER["REMOTE_ADDR"],
							$_POST["recaptcha_challenge_field"],
							$_POST["recaptcha_response_field"]);

			$recaptcha_code_ok = $recaptcha_resp->is_valid;
		}

		if (!$recaptcha_code_ok)
			$errors->add("recaptcha_code", '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.__('Typed code is not correct.', $cimy_uef_domain));
	}

	if ($options['captcha'] == "securimage") {
		global $cuef_plugin_dir;
		require_once($cuef_plugin_dir.'/securimage/securimage.php');
		$securimage = new Securimage();
		if ($securimage->check($_POST['securimage_response_field']) == false) {
			$errors->add("securimage_code", '<strong>'.__("ERROR", $cimy_uef_domain).'</strong>: '.__('Typed code is not correct.', $cimy_uef_domain));
		}
	}
	return $errors;
}

function cimy_uef_sanitize_username($username, $raw_username, $strict) {
	$options = cimy_get_options();
	if (!in_array("username", $options["wp_hidden_fields"]) && !empty($_POST['user_email']) && is_email($_POST['user_email']) && cimy_uef_is_register_page()) {
		$username = $_POST['user_email'];
	}
	return $username;
}

function cimy_uef_validate_username($valid, $username) {
	$options = cimy_get_options();
	if (!in_array("username", $options["wp_hidden_fields"]) && empty($username) && cimy_uef_is_register_page()) {
		return true;
	}
	return $valid;
}

// show_type == 0 - normal form
// show_type == 1 - search form, all fields are text, password fields are skipped
// show_type == 2 - confirmation form, all fields are plain text, images can be cropped
function cimy_registration_form($errors=null, $show_type=0) {
	global $wpdb, $start_cimy_uef_comment, $end_cimy_uef_comment, $rule_maxlen_needed, $fields_name_prefix, $wp_fields_name_prefix, $cuef_plugin_dir, $cimy_uef_file_types, $cimy_uef_textarea_types, $user_level, $cimy_uef_domain, $cimy_uef_file_images_types;

	if (cimy_is_at_least_wordpress35())
		cimy_switch_to_blog();

	$my_user_level = $user_level;

	// -1 == anonymous
	if (!is_user_logged_in())
		$my_user_level = -1;

	// needed by cimy_uef_init_mce.php
	$cimy_uef_register_page = true;
	$extra_fields = get_cimyFields(false, true);
	$wp_fields = get_cimyFields(true);

	if (is_multisite())
		$input_class = "cimy_uef_input_mu";
	else
		$input_class = "cimy_uef_input_27";

	$options = cimy_get_options();
	$tabindex = 21;
	
	echo $start_cimy_uef_comment;
	// needed to apply default values only first time and not in case of errors
	echo "\t<input type=\"hidden\" name=\"cimy_post\" value=\"1\" />\n";
	if ($options['confirm_form']) {
		if ($show_type == 0)
			echo "\t<input type=\"hidden\" name=\"register_confirmation\" value=\"1\" />\n";
		else if ($show_type == 2)
			echo "\t<input type=\"hidden\" name=\"register_confirmation\" value=\"2\" />\n";
	}
	$radio_checked = array();

	$i = 1;

	// confirmation page, all fields are plain text + hidden fields to carry over values
	if ($show_type == 2) {
		$user_email = $_POST["user_email"];
		if (in_array("username", $options["wp_hidden_fields"])) {
			$username = $_POST["user_login"];
?>
			<p id="user_login_p">
				<label for="user_login"><?php _e("Username"); ?> </label><?php echo esc_html($username); ?>
			</p>
<?php
		}
		else {
			$username = $user_email;
		}

		$upload_dir = cimy_uef_get_dir_or_filename("");
		$dirs = glob($upload_dir.".cimytemp_*.tmp");
		if (is_array($dirs)) {
			foreach ($dirs as $dir) {
				$diff = current_time('timestamp', true) - (filemtime($dir));
				// If older than two days delete!
				if ($diff > 172800) {
					cimy_rfr($dir."/", "*");
					if (is_dir($dir))
						rmdir($dir);
				}
			}
		}

		$temp_user_login = ".cimytemp_".sanitize_user($username).'_'.rand().'.tmp';
?>
		<input type="hidden" name="temp_user_login" value="<?php echo esc_attr($temp_user_login); ?>" />
		<input type="hidden" name="user_login" id="user_login" value="<?php echo esc_attr($username); ?>" />
		<p id="user_email_p">
			<label for="user_email"><?php _e("E-mail"); ?> </label><input type="hidden" name="user_email" id="user_email" value="<?php echo esc_attr($user_email); ?>" /><?php echo esc_html($user_email); ?>
		</p>
		<br />
<?php
	}

	// do first the WP fields then the EXTRA fields
	while ($i <= 2) {
		if ($i == 1) {
			$fields = $wp_fields;
			$prefix = $wp_fields_name_prefix;
		}
		else {
			$fields = $extra_fields;
			$prefix = $fields_name_prefix;
			$current_fieldset = -1;

			if (!empty($options['fieldset_title']))
				$fieldset_titles = explode(',', $options['fieldset_title']);
			else
				$fieldset_titles = array();
		}

		$tiny_mce_objects = "";
	
		foreach ($fields as $thisField) {
			$field_id = $thisField['ID'];
			$name = $thisField['NAME'];
			$rules = $thisField['RULES'];
			$type = $thisField['TYPE'];
			$old_type = $type;
			$label = cimy_wpml_translate_string($name."_label", $thisField["LABEL"]);
			$description = cimy_uef_sanitize_content(cimy_wpml_translate_string($name."_desc", $thisField["DESCRIPTION"]));
			$fieldset = empty($thisField['FIELDSET']) ? 0 : $thisField['FIELDSET'];
			$maxlen = 0;
			$unique_id = $prefix.$field_id;
			$input_name = $prefix.esc_attr($name);
			$field_id_data = $input_name."_".$field_id."_data";
			$advanced_options = cimy_uef_parse_advanced_options($rules["advanced_options"]);

			// do not dupe username
			if ($i == 1 && $name == "USERNAME")
				continue;
			// showing the search then there is no need for upload buttons
			if ($show_type == 1) {
				if ($type == "password")
					continue;

				if (in_array($type, $cimy_uef_file_types))
					$type = "text";
			}
			else if ($show_type == 2) {
				$type = "hidden";
			}

			// if the current user LOGGED IN has not enough permissions to see the field, skip it
			if ($rules['show_level'] == 'view_cimy_extra_fields')
			{
				if (!current_user_can($rules['show_level']))
					continue;
			}
			else if ($my_user_level < $rules['show_level'])
				continue;

			// if show_level == anonymous then do NOT ovverride other show_xyz rules
			if ($rules['show_level'] == -1) {
				if ($show_type == 0 || $show_type == 2) {
					// if flag to show the field in the registration is NOT activated, skip it
					if (!$rules['show_in_reg'])
						continue;
				} else if ($show_type == 1) {
					// if flag to show the field in the blog is NOT activated, skip it
					if (!$rules['show_in_search'])
						continue;
				}
			}

			// uploading a file is not supported when confirmation email is enabled (on MS is turned on by default yes)
			if (((is_multisite()) || ($options["confirm_email"])) && (in_array($type, $cimy_uef_file_types)))
				continue;

			if (isset($_POST[$input_name])) {
				if (($type == "dropdown-multi") || ($old_type == "dropdown-multi"))
					$value = stripslashes(implode(",", $_POST[$input_name]));
				else
					$value = stripslashes($_POST[$input_name]);
			}
			else if (isset($_GET[$name])) {
				if (($type == "dropdown-multi") || ($old_type == "dropdown-multi"))
					$value = stripslashes(implode(",", $_GET[$name]));
				else
					$value = stripslashes($_GET[$name]);
			}
			// if there is no value and not $_POST means is first visiting then put all default values
			else if (!isset($_POST["cimy_post"])) {
				$value = $thisField['VALUE'];
				
				switch($type) {
					case "radio":
						if ($value == "YES")
							$value = $field_id;
						else
							$value = "";
						
						break;
		
					case "checkbox":
						if ($value == "YES")
							$value = "1";
						else
							$value = "";
						
						break;
				}
			}
			else
				$value = "";

			if (($i != 1) && ($fieldset > $current_fieldset) && (isset($fieldset_titles[$fieldset]))) {
				$current_fieldset = $fieldset;

				if (isset($fieldset_titles[$current_fieldset]))
					echo "\n\t<h2>".esc_html(cimy_wpml_translate_string("a_opt_fieldset_title_".$current_fieldset, $fieldset_titles[$current_fieldset]))."</h2>\n";
			}

			if ((!empty($description)) && ($type != "registration-date")) {
				echo "\t";
				echo '<p id="'.$prefix.'p_desc_'.$field_id.'" class="description"><br />'.$description.'</p>';
				echo "\n";
			}

			echo "\t";
			echo '<p id="'.$prefix.'p_field_'.$field_id.'">';
			echo "\n\t";
	
			switch($type) {
				case "picture-url":
				case "password":
				case "text":
					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).'</label>';
					$obj_class = ' class="'.$input_class.'"';
					$obj_name = ' name="'.$input_name.'"';

					if ($type == "picture-url")
						$obj_type = ' type="text"';
					else
						$obj_type = ' type="'.$type.'"';

					$obj_value = ' value="'.esc_attr($value).'"';
					$obj_value2 = "";
					$obj_checked = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
					break;

				case "dropdown":
				case "dropdown-multi":
					// cimy_dropDownOptions uses cimy_uef_sanitize_content and esc_attr by itself
					$ret = cimy_dropDownOptions($label, $value);
					$label = $ret['label'];
					$html = $ret['html'];

					if ($type == "dropdown-multi") {
						$obj_name = ' name="'.$input_name.'[]" multiple="multiple" size="6"';
					}
					else {
						$obj_name = ' name="'.$input_name.'"';
					}

					$obj_label = '<label for="'.$unique_id.'">'.$label.'</label>';
					$obj_class = ' class="'.$input_class.'"';
					$obj_type = '';
					$obj_value = '';
					$obj_value2 = $html;
					$obj_checked = "";
					$obj_tag = "select";
					$obj_closing_tag = true;
					break;

				case "textarea":
					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).'</label>';
					$obj_class = ' class="'.$input_class.'"';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = "";
					$obj_value = "";
					$obj_value2 = esc_html($value);
					$obj_checked = "";
					$obj_tag = "textarea";
					$obj_closing_tag = true;
					break;

				case "textarea-rich":
					if (empty($tiny_mce_objects))
						$tiny_mce_objects = $fields_name_prefix.$field_id;
					else
						$tiny_mce_objects .= ",".$fields_name_prefix.$field_id;

					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).'</label>';
					$obj_class = ' class="'.$input_class.'"';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = "";
					$obj_value = "";
					$obj_value2 = esc_html($value);
					$obj_checked = "";
					$obj_tag = "textarea";
					$obj_closing_tag = true;
					break;

				case "checkbox":
					$obj_label = '<label class="cimy_uef_label_checkbox" for="'.$unique_id.'"> '.cimy_uef_sanitize_content($label).'</label><br />';
					$obj_class = ' class="cimy_uef_checkbox"';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="'.$type.'"';
					$obj_value = ' value="1"';
					$obj_value2 = "";
					$value == "1" ? $obj_checked = ' checked="checked"' : $obj_checked = '';
					$obj_tag = "input";
					$obj_closing_tag = false;
					break;
		
				case "radio":
					$obj_label = '<label class="cimy_uef_label_radio" for="'.$unique_id.'"> '.cimy_uef_sanitize_content($label).'</label>';
					$obj_class = ' class="cimy_uef_radio"';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="'.$type.'"';
					$obj_value = ' value="'.$field_id.'"';
					$obj_value2 = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
	
					// do not check if another check was done
					if ((intval($value) == intval($field_id)) && (!in_array($name, $radio_checked))) {
						$obj_checked = ' checked="checked"';
						$radio_checked += array($name => true);
					}
					else {
						$obj_checked = '';
					}

					break;

				case "avatar":
				case "picture":
				case "file":
					$allowed_exts = '';
					if (isset($rules['equal_to']))
						if ($rules['equal_to'] != "")
							$allowed_exts = "'".implode("', '", explode(",", $rules['equal_to']))."'";

					if ($type == "file") {
						// if we do not escape then some translations can break
						$warning_msg = esc_js(__("Please upload a file with one of the following extensions", $cimy_uef_domain));

						$obj_checked = ' onchange="uploadFile(\'registerform\', \''.$unique_id.'\', \''.$warning_msg.'\', Array('.$allowed_exts.'));"';
					}
					else {
						// if we do not escape then some translations can break
						$warning_msg = esc_js(__("Please upload an image with one of the following extensions", $cimy_uef_domain));
						$allowed_exts = "'".implode("','", cimy_uef_get_allowed_image_extensions())."'";
						$obj_checked = ' onchange="uploadFile(\'registerform\', \''.$unique_id.'\', \''.$warning_msg.'\', Array('.$allowed_exts.'));"';
					}

					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).' </label>';
					$obj_class = ' class="cimy_uef_picture"';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="file"';
					$obj_value = ' value="'.esc_attr($value).'"';
					$obj_value2 = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
					break;

				case "hidden":
					$obj_label = "";
					$obj_value2 = "";
					switch ($old_type) {
						case 'checkbox':
							$value == 1 ? $obj_value2 = __("YES", $cimy_uef_domain) : $obj_value2 = __("NO", $cimy_uef_domain);
							break;
						case 'radio':
							intval($value) == intval($field_id) ? $obj_value2 = __("YES", $cimy_uef_domain) : $obj_value2 = __("NO", $cimy_uef_domain);
							break;
						case 'dropdown':
						case 'dropdown-multi':
							$ret = cimy_dropDownOptions($label, $value);
							$label = $ret['label'];
							break;
						case 'picture':
						case 'avatar':
						case 'file':
							$value = cimy_manage_upload($input_name, $temp_user_login, $rules, false, false, $type, (!empty($advanced_options["filename"])) ? $advanced_options["filename"] : "");
							$file_on_server = cimy_uef_get_dir_or_filename($temp_user_login, $value, false);
							$file_thumb = cimy_uef_get_dir_or_filename($temp_user_login, $value, true);
							if ((!empty($advanced_options["no-thumb"])) && (is_file($file_thumb)))
								rename($file_thumb, $file_on_server);

							// yea little trick
							$obj_value2 = "&nbsp;";
							break;
					}
					if ($old_type != "password") {
						$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).' </label>';
						if (empty($obj_value2))
							$obj_value2 = cimy_uef_sanitize_content($value);
					}
					$obj_class = '';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="hidden"';
					$obj_value = ' value="'.esc_attr($value).'"';
					$obj_checked = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
					break;

				case "registration-date":
					$obj_label = '';
					$obj_class = '';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="hidden"';
					$obj_value = ' value="'.esc_attr($value).'"';
					$obj_value2 = "";
					$obj_checked = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
					break;
			}
	
			$obj_id = ' id="'.$unique_id.'"';

			// tabindex not used in MU, WordPress 3.5+ and Theme My Login  dropping...
			if (is_multisite() || cimy_is_at_least_wordpress35() || cimy_uef_is_theme_my_login_register_page())
				$obj_tabindex = "";
			else {
				$obj_tabindex = ' tabindex="'.strval($tabindex).'"';
				$tabindex++;
			}

			$obj_maxlen = "";
	
			if ((in_array($type, $rule_maxlen_needed)) && (!in_array($type, $cimy_uef_file_types))) {
				if (isset($rules['max_length'])) {
					$obj_maxlen = ' maxlength="'.$rules['max_length'].'"';
				} else if (isset($rules['exact_length'])) {
					$obj_maxlen = ' maxlength="'.$rules['exact_length'].'"';
				}
			}

			if (in_array($type, $cimy_uef_textarea_types))
				$obj_rowscols = ' rows="3" cols="25"';
			else
				$obj_rowscols = '';

			echo "\t";
			$form_object = '<'.$obj_tag.$obj_type.$obj_name.$obj_id.$obj_class.$obj_value.$obj_checked.$obj_maxlen.$obj_rowscols.$obj_tabindex;

			if ($obj_closing_tag)
				$form_object.= ">".$obj_value2."</".$obj_tag.">";
			else if ($type == "hidden") {
				$form_object.= " />".$obj_value2;
				if (in_array($old_type, $cimy_uef_file_types)) {
					$f_size = empty($_FILES[$input_name]['size']) ? 0 : $_FILES[$input_name]['size'];
					$f_type = empty($_FILES[$input_name]['type']) ? "" : $_FILES[$input_name]['type'];
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_size\" id=\"".esc_attr($field_id_data)."_size\" value=\"".esc_attr(strval($f_size / 1024))."\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_type\" id=\"".esc_attr($field_id_data)."_type\" value=\"".esc_attr(strval($f_type))."\" />";
				}
				if ((in_array($old_type, $cimy_uef_file_images_types)) && (is_file($file_on_server))) {
					echo '<img id="'.esc_attr($field_id_data).'" src="'.esc_attr($value).'" alt="picture" /><br />';
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_button\" id=\"".esc_attr($field_id_data)."_button\" value=\"1\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_x1\" id=\"".esc_attr($field_id_data)."_x1\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_y1\" id=\"".esc_attr($field_id_data)."_y1\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_x2\" id=\"".esc_attr($field_id_data)."_x2\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_y2\" id=\"".esc_attr($field_id_data)."_y2\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_w\" id=\"".esc_attr($field_id_data)."_w\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".esc_attr($field_id_data)."_h\" id=\"".esc_attr($field_id_data)."_h\" value=\"\" />";
					$imgarea_options = "handles: true, fadeSpeed: 200, onSelectChange: preview";
					if ((isset($advanced_options["crop_x1"])) && (isset($advanced_options["crop_y1"])) && (isset($advanced_options["crop_x2"])) && (isset($advanced_options["crop_y2"]))) {
						$imgarea_options.= ", x1: ".intval($advanced_options["crop_x1"]);
						$imgarea_options.= ", y1: ".intval($advanced_options["crop_y1"]);
						$imgarea_options.= ", x2: ".intval($advanced_options["crop_x2"]);
						$imgarea_options.= ", y2: ".intval($advanced_options["crop_y2"]);
					}
					if (!empty($advanced_options["crop_ratio"]))
						$imgarea_options.= ", aspectRatio: '".esc_js($advanced_options["crop_ratio"])."'";
					else if ($type == "avatar")
						$imgarea_options.= ", aspectRatio: '1:1'";
					echo "<script type='text/javascript'>jQuery(document).ready(function () { jQuery('#".esc_js($field_id_data)."').imgAreaSelect({ ".$imgarea_options." }); });</script>";
				}

			}
			else
				$form_object.= " />";

			if (($type != "radio") && ($type != "checkbox"))
				echo $obj_label;

			if (is_multisite() && is_wp_error($errors)) {
				if ( $errmsg = $errors->get_error_message($unique_id) ) {
					echo '<p class="error">'.$errmsg.'</p>';
				}
			}

			// TinceMCE needed and we have WordPress >= 3.3 yummy!
			if ($type == "textarea-rich" && function_exists("wp_editor")) {
		?>
				<script type='text/javascript'>
					var login_div = document.getElementById("login");
					login_div.style.width = "535px";
				</script>
		<?php
				$quicktags_settings = array( 'buttons' => 'strong,em,link,block,del,ins,img,ul,ol,li,code,spell,close' );
				$editor_settings = array(
					'textarea_name' => $input_name,
					'teeny' => false,
					'textarea_rows' => '10',
					'dfw' => false,
					'media_buttons' => true,
					'tinymce' => true,
					'quicktags' => $quicktags_settings,
				);
				if (!empty($obj_tabindex))
					$editor_settings['tabindex'] = $tabindex;
				wp_editor($value, $unique_id, $editor_settings);
			}
			// write to the html the form object built
			else
				echo $form_object;

			if (($show_type == 0) && ($i == 1) && ($options['password_meter'])) {
				if ($input_name == ($prefix."PASSWORD"))
					$pass1_id = $unique_id;

				if ($input_name == ($prefix."PASSWORD2")) {
					echo "\n\t\t<div id=\"pass-strength-result\">".__('Strength indicator')."</div>";
					echo "\n\t\t<p class=\"description indicator-hint\">".__('Hint: The password should be at least seven characters long. To make it stronger, use upper and lower case letters, numbers and symbols like ! " ? $ % ^ &amp; ).')."</p><br />";
					$pass2_id = $unique_id;
				}
			}

			if (!(($type != "radio") && ($type != "checkbox")))
				echo $obj_label;

			echo "\n\t</p>\n";

			if (($type == "textarea-rich") || (in_array($type, $cimy_uef_file_types)))
				echo "\t<br />\n";
		}

		$i++;
	}
	echo "\t<br />";

	if ($show_type == 0) {
		// WP 3.2 or lower (N)
		if (!empty($tiny_mce_objects) && !function_exists("wp_editor")) {
			require_once($cuef_plugin_dir.'/cimy_uef_init_mce.php');
		}
	}

	if (($show_type != 2) && ($options['captcha'] == "securimage")) {
		global $cuef_securimage_webpath;
		if (is_multisite()) {
			$width = 500;
			if (is_wp_error($errors) && $errmsg = $errors->get_error_message("securimage_code"))
				echo '<p class="error">'.$errmsg.'</p>';
		}
		else
			$width = 278;
?>
		<div style="width: <?php echo $width; ?>px; float: left; height: 80px; vertical-align: text-top;">
			<img id="captcha" align="left" style="padding-right: 5px; border: 0" src="<?php echo $cuef_securimage_webpath; ?>/securimage_show_captcha.php" alt="CAPTCHA Image" />
			<object type="application/x-shockwave-flash" data="<?php echo $cuef_securimage_webpath; ?>/securimage_play.swf?audio_file=<?php echo $cuef_securimage_webpath; ?>/securimage_play.php&#038;bgColor1=#fff&#038;bgColor2=#fff&#038;iconColor=#777&#038;borderWidth=1&#038;borderColor=#000" height="19" width="19"><param name="movie" value="<?php echo $cuef_securimage_webpath; ?>/securimage_play.swf?audio_file=<?php echo $cuef_securimage_webpath; ?>/securimage_play.php&#038;bgColor1=#fff&#038;bgColor2=#fff&#038;iconColor=#777&#038;borderWidth=1&#038;borderColor=#000" /></object>
			<br /><br /><br />
			<a align="right"<?php if (!empty($obj_tabindex)) echo " tabindex=\"".$tabindex."\""; $tabindex++; ?> style="border-style: none" href="#" onclick="document.getElementById('captcha').src = '<?php echo $cuef_securimage_webpath; ?>/securimage_show_captcha.php?' + Math.random(); return false"><img src="<?php echo $cuef_securimage_webpath; ?>/images/refresh.png" alt="<?php _e("Change image", $cimy_uef_domain); ?>" border="0" onclick="this.blur()" align="bottom" height="19" width="19" /></a>
		</div>
		<div style="width: <?php echo $width; ?>px; float: left; height: 50px; vertical-align: bottom; padding: 5px;">
			<?php _e("Insert the code:", $cimy_uef_domain); ?>&nbsp;<input type="text" name="securimage_response_field" size="12" maxlength="16"<?php if (!empty($obj_tabindex)) echo " tabindex=\"".$tabindex."\""; $tabindex++; ?> />
		</div>
<?php
	}

	if (($show_type != 2) && ($options['captcha'] == "recaptcha") && (!empty($options['recaptcha_public_key'])) && (!empty($options['recaptcha_private_key']))) {
		require_once($cuef_plugin_dir.'/recaptcha/recaptchalib.php');
		if (is_multisite() && is_wp_error($errors) && $errmsg = $errors->get_error_message("recaptcha_code")) {
			echo '<p class="error">'.$errmsg.'</p>';
		}
?>
		<script type='text/javascript'>
			var RecaptchaOptions = {
				lang: '<?php echo substr(get_locale(), 0, 2); ?>'
				<?php if (!empty($obj_tabindex)) echo ", tabindex: ".$tabindex; $tabindex++; ?>
			};
		</script>
<?php

		// no need if Tiny MCE is present already
		if (empty($tiny_mce_objects)) {
?>
			<script type='text/javascript'>
				var login_div = document.getElementById("login");
				login_div.style.width = "375px";
			</script>
<?php
		}
		echo recaptcha_get_html($options['recaptcha_public_key'], null, is_ssl());
	}

	cimy_switch_current_blog(true);

	echo $end_cimy_uef_comment;
}

function cimy_confirmation_form() {
	if (empty($_POST['register_confirmation']))
		return;
	$confirmation = false;
	$http_post = ('POST' == $_SERVER['REQUEST_METHOD']);
	$user_login = '';
	$user_email = '';

	if ($http_post) {
		$user_login = $_POST['user_login'];
		$user_email = $_POST['user_email'];

		if (function_exists("register_new_user")) {
			// fake registration to check if no errors then we'll proceed to confirmation phase
			$fake_errors = register_new_user($user_login, $user_email);
			// ok we can remove registration checks
// 			remove_action('register_post', 'cimy_registration_check');
// 			remove_action('register_post', 'cimy_registration_captcha_check');
		}
		// Might be Theme My Login, they have its own register_new_user but they don't have login_header seems so, so let's return for now!
		else
			return;

		if (!is_wp_error($fake_errors)) {
			$redirect_to = !empty( $_POST['redirect_to'] ) ? $_POST['redirect_to'] : 'wp-login.php?checkemail=registered';
			wp_safe_redirect( $redirect_to );
			exit();
		}
		else if ((count($fake_errors->errors) == 1) && (isset($fake_errors->errors["register_confirmation"]))) {
			$confirmation = true;
		}
	}
	if ($confirmation) {
		global $cimy_uef_domain;
		$redirect_to = apply_filters( 'registration_redirect', !empty( $_REQUEST['redirect_to'] ) ? $_REQUEST['redirect_to'] : '' );
		$message = new WP_Error();
		$message->add('confirmation', __('Confirm your registration', $cimy_uef_domain), 'message');

		login_header(__("Confirm your registration", $cimy_uef_domain), "", $message);
?>
		<form name="registerform" id="registerform" action="<?php echo site_url('wp-login.php?action=register', 'login_post') ?>" method="post">
<?php
		cimy_registration_form(null, 2);
?>
		<p id="reg_passmail"><?php _e('A password will be e-mailed to you.') ?></p>
		<br class="clear" />
		<input type="hidden" name="redirect_to" value="<?php echo esc_attr( $redirect_to ); ?>" />
		<?php wp_nonce_field('confirm_form', 'confirm_form_nonce'); ?>
		<p class="submit"><input type="submit" name="wp-submit" id="wp-submit" class="button-primary" value="<?php esc_attr_e('Register'); ?>" tabindex="100" /></p>
		</form>

		<p id="nav">
		<a href="javascript: history.go(-1)"><?php _e('&larr; Back', $cimy_uef_domain) ?></a>
		</p>
<?php
		login_footer("");
		exit(0);
	}
}

function cimy_uef_registration_redirect($redirect_to) {
	if (empty($redirect_to)) {
		$options = cimy_get_options();

		if ($options["redirect_to"] == "source")
			$redirect_to = esc_attr($_SERVER["HTTP_REFERER"]);
	}

	return $redirect_to;
}

function cimy_uef_redirect() {
	if (isset($_GET["cimy_key"]))
		cimy_uef_activate("");

	if (!empty($_REQUEST["redirect_to"]))
		wp_safe_redirect($_REQUEST["redirect_to"]);

}

function cimy_change_signup_location($url) {
	global $current_site, $cimy_uef_plugins_dir;

	if ($cimy_uef_plugins_dir == "plugins")
		$attribute = "?from_blog_id=".get_current_blog_id();
	else
		$attribute = "";
	return network_site_url('wp-signup.php'.$attribute);
}

function cimy_change_login_registration_logo() {
	$options = cimy_get_options();

	if (!empty($options["registration-logo"])) {
		global $cuef_upload_webpath;
		list($logo_width, $logo_height, $logo_type, $logo_attr) = getimagesize($options["registration-logo"]);
		?>
		<style type="text/css">
		#login h1:first-child a:first-child {
			background: url(<?php echo esc_url($cuef_upload_webpath.basename($options["registration-logo"])); ?>) no-repeat top center;
			background-position: center top;
			background-size: <?php echo $logo_width; ?>px <?php echo $logo_height; ?>px;
			width: <?php echo max(328, $logo_width); ?>px;
			height: <?php echo $logo_height; ?>px;
			text-indent: -9999px;
			overflow: hidden;
			padding-bottom: 15px;
			display: block;
		}
		</style>
		<?php
	}
}
