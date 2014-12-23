<?php

if ( !function_exists('wp_new_user_notification') ) :
/**
 * Notify the blog admin of a new user, normally via email.
 *
 * @param int $user_id User ID
 * @param string $plaintext_pass Optional. The user's plaintext password
 */
function wp_new_user_notification($user_id, $plaintext_pass = '') {
	if (isset($_POST["cimy_uef_wp_PASSWORD"]))
		delete_user_meta($user_id, 'default_password_nag');

	$options = cimy_get_options();
	if (!is_multisite()) {
		if (!$options["confirm_email"])
			wp_new_user_notification_original($user_id, $plaintext_pass, $options["mail_include_fields"], false, cimy_wpml_translate_string("a_opt_welcome_email", $options["welcome_email"]));
		// if confirmation email is enabled delete the default_password_nag but checks first if has not been done on top of this function!
		else if (!isset($_POST["cimy_uef_wp_PASSWORD"]))
			delete_user_meta($user_id, 'default_password_nag');
	}
	else
		wp_new_user_notification_original($user_id, $plaintext_pass, $options["mail_include_fields"]);
}
endif;

function wp_new_user_notification_original($user_id, $plaintext_pass = '', $include_fields = false, $activation_data = false, $welcome_email = '') {
	$user = new WP_User($user_id);

	$user_login = stripslashes($user->user_login);
	$user_email = stripslashes($user->user_email);
	$admin_email = get_option('admin_email');

	// The blogname option is escaped with esc_html on the way into the database in sanitize_option
	// we want to reverse this for the plain text arena of emails.
	$blogname = wp_specialchars_decode(get_option('blogname'), ENT_QUOTES);
	// Get the site domain and get rid of www.
	$sitename = strtolower( $_SERVER['SERVER_NAME'] );
	if ( substr( $sitename, 0, 4 ) == 'www.' )
		$sitename = substr( $sitename, 4 );

	$from_email = 'wordpress@' . $sitename;
	$headers = sprintf("From: %s <%s>\r\n\\", $blogname, $from_email);

	$message  = sprintf(__('New user registration on your site %s:'), $blogname) . "\r\n\r\n";
	$message .= sprintf(__('Username: %s'), $user_login) . "\r\n\r\n";
	$message .= sprintf(__('E-mail: %s'), $user_email) . "\r\n";

	if ($include_fields)
		$message .= cimy_uef_mail_fields($user, $activation_data);

	@wp_mail($admin_email, sprintf(__('[%s] New User Registration'), $blogname), $message, $headers);

	if ( empty($plaintext_pass) )
		return;

	$welcome_email = str_replace("USERNAME", $user_login, $welcome_email);
	$welcome_email = str_replace("PASSWORD", $plaintext_pass, $welcome_email);

	if ($include_fields)
		$welcome_email .= cimy_uef_mail_fields($user, $activation_data);
	$welcome_email = str_replace("LOGINLINK", wp_login_url(), $welcome_email);

	wp_mail($user_email, sprintf(__('[%s] Your username and password'), $blogname), $welcome_email, $headers);
}

function cimy_uef_mail_fields($user = false, $activation_data = false) {
	global $wp_hidden_fields, $cimy_uef_domain, $fields_name_prefix, $wp_fields_name_prefix;
	$message = "";
	$meta = false;

	if ((empty($user)) && (empty($activation_data)))
		return $message;

	if (empty($user)) {
		$user_login = $activation_data["user_login"];
// 		$user_email = $activation_data["user_email"];
		if (!is_array($activation_data["meta"]))
			$meta = unserialize($activation_data["meta"]);
		else
			$meta = $activation_data["meta"];

		// neet to do it here, otherwise I pick up main options instead of blog's ones
		if (is_multisite())
			cimy_switch_to_blog($meta);
		$options = cimy_get_options();
		if (is_multisite())
			restore_current_blog();

		if (!$options["mail_include_fields"])
			return $message;

		$user = new WP_User($user_login);
	}

	if (empty($meta)) {
		// normal fields
		foreach ($wp_hidden_fields as $field) {
			if ((!empty($user->{$field["userdata_name"]})) && ($field["type"] != "password")) {
				$label = $field["label"];
				if ($field["type"] == "dropdown" || $field["type"] == "dropdown-multi") {
					$ret = cimy_dropDownOptions($label, "");
					$label = $ret['label'];
				}
				$message.= sprintf(__('%s: %s', $cimy_uef_domain), $label, $user->{$field["userdata_name"]}) . "\r\n";
			}
		}
	}
	else {
		$fields = get_cimyFields(true);
		foreach ($fields as $field) {
			if ((!empty($meta[$wp_fields_name_prefix.$field["NAME"]])) && ($field["TYPE"] != "password")) {
				$label = $field["LABEL"];
				if ($field["TYPE"] == "dropdown" || $field["TYPE"] == "dropdown-multi") {
					$ret = cimy_dropDownOptions($label, "");
					$label = $ret['label'];
				}
				$message.= sprintf(__('%s: %s', $cimy_uef_domain), $label, $meta[$wp_fields_name_prefix.$field["NAME"]]) . "\r\n";
			}
		}
	}

	$message.= "\r\n";

	// extra fields;
	if (empty($meta)) {
		$ef_data = get_cimyFieldValue($user->ID, false);

		foreach ($ef_data as $field)
			if (!empty($field["VALUE"]))
				$message.= sprintf(__('%s: %s', $cimy_uef_domain), $field["LABEL"], $field["VALUE"]) . "\r\n";
	}
	else {
		$extra_fields = get_cimyFields(false, true);
		foreach ($extra_fields as $field) {
			if (!empty($meta[$fields_name_prefix.$field["NAME"]]))
				$message.= sprintf(__('%s: %s', $cimy_uef_domain), $field["LABEL"], $meta[$fields_name_prefix.$field["NAME"]]) . "\r\n";
		}
	}

	return $message;
}

function cimy_uef_welcome_blog_to_user($welcome_email, $blog_id, $user_id, $password, $title, $meta) {
	$welcome_email = cimy_uef_welcome_user_to_user($welcome_email, $user_id, $password, $meta);

	return $welcome_email;
}

function cimy_uef_welcome_user_to_user($welcome_email, $user_id, $password, $meta) {
	$activation_data = array();
	// I know is a small lie, but it works!
	$activation_data["user_login"] = $user_id;
	$activation_data["meta"] = $meta;
	$welcome_email.= "\r\n\r\n".cimy_uef_mail_fields(false, $activation_data);

	return $welcome_email;
}

function cimy_uef_welcome_blog_to_admin($msg) {
	$lines = explode("\n", $msg);
	$second_line = explode("/", $lines[1]);
	$path = "";
	$domain = $second_line[2];
	$i = 3;

	while (isset($second_line[$i])) {
		$path.= "/".$second_line[$i];
		$i++;
	}

	if (substr($path, -1) != "/")
		$path.= "/";

	$res = cimy_uef_get_meta_from_url($domain, $path);
	$msg.= "\r\n\r\n".cimy_uef_mail_fields(false, $res);

	return $msg;
}

function cimy_uef_welcome_user_to_admin($msg) {
	$lines = explode("\n", $msg);
	$first_line = explode(":", $lines[0]);
	$user_login = trim($first_line[1]);

	$res = cimy_uef_get_meta_from_user_login($user_login);
	$msg.= "\r\n\r\n".cimy_uef_mail_fields(false, $res);

	return $msg;
}

function cimy_signup_user_notification($user, $user_email, $key, $meta = '') {
	global $cuef_plugin_path, $cimy_uef_domain;

	if ( !apply_filters('wpmu_signup_user_notification', $user, $user_email, $key, $meta) )
		return false;

	$redirect_to = "";
	// need to redirect?
	if (!empty($_POST["redirect_to"])) {
		$redirect_to = "&action=cimy_uef_redirect&redirect_to=".esc_attr($_POST["redirect_to"]);
		unset($_POST["redirect_to"]);
        }

	// Send email with activation link.
	$admin_email = get_site_option( 'admin_email' );
	if ( $admin_email == '' )
		$admin_email = 'support@' . $_SERVER['SERVER_NAME'];
	$from_name = get_site_option( 'blogname' ) == '' ? 'WordPress' : esc_html( get_site_option( 'blogname' ) );
	$message_headers = "From: \"{$from_name}\" <{$admin_email}>\n" . "Content-Type: text/plain; charset=\"" . get_option('blog_charset') . "\"\n";
	$message = sprintf( apply_filters( 'wpmu_signup_user_notification_email', __( "To activate your user, please click the following link:\n\n%s\n\nAfter you activate, you will receive *another email* with your login.\n\n", $cimy_uef_domain) ), site_url( "wp-login.php?cimy_key=$key$redirect_to" ), $key );
	// TODO: Don't hard code activation link.
	$subject = sprintf(
		apply_filters('wpmu_signup_user_notification_subject',
			__('[%1$s] Activate %2$s', $cimy_uef_domain),
			$user, $user_email, $key, $meta
		),
		$from_name,
		$user
	);
	wp_mail($user_email, $subject, $message, $message_headers);
	return true;
}

function cimy_uef_activate($message) {
	global $wpdb, $cimy_uef_domain;

	if (isset($_GET["cimy_key"])) {
		$result = cimy_uef_activate_signup($_GET["cimy_key"]);

		if ( is_wp_error($result) ) {
			if ( 'already_active' == $result->get_error_code()) {
				$signup = $result->get_error_data();
				$message = '<p class="message"><strong>'.__('Your account is now active!', $cimy_uef_domain).'</strong><br />';
				$message.= sprintf( __('Your site at <a href="%1$s">%2$s</a> is active. You may now log in to your site using your chosen username of &#8220;%3$s&#8221;.  Please check your email inbox at %4$s for your password and login instructions.  If you do not receive an email, please check your junk or spam folder.  If you still do not receive an email within an hour, you can <a href="%5$s">reset your password</a></p>.', $cimy_uef_domain), 'http://' . $signup->domain, $signup->domain, $signup->user_login, $signup->user_email, network_site_url( 'wp-login.php?action=lostpassword' ) );
			} else {
				$message = '<p class="message"><strong>'.__('An error occurred during the activation', $cimy_uef_domain).'</strong><br />';
				$message.= $result->get_error_message().'</p>';
			}
		} else {
			extract($result);
			$user = new WP_User( (int) $user_id);
			$message = '<p class="message"><strong>'.__('Your account is now active!', $cimy_uef_domain).'</strong><br />'.sprintf(__('Username: %s'), $user->user_login).'<br />'.sprintf(__('Password: %s'), $password).'</p>';
		}
	}
	return $message;
}

function cimy_uef_activate_signup($key) {
	global $wpdb, $current_site, $cimy_uef_domain;

	// seems no more required since WP 3.1
// 	require_once( ABSPATH . WPINC . '/registration.php');
	$signup = $wpdb->get_row( $wpdb->prepare("SELECT * FROM ".$wpdb->prefix."signups WHERE activation_key = %s", $key) );

	if ( empty($signup) )
		return new WP_Error('invalid_key', __('Invalid activation key.', $cimy_uef_domain));

	if ( $signup->active )
		return new WP_Error('already_active', __('The site is already active.', $cimy_uef_domain), $signup);

	$meta = unserialize($signup->meta);
	$user_login = $wpdb->escape($signup->user_login);
	$user_email = $wpdb->escape($signup->user_email);

	if (!empty($meta["cimy_uef_wp_PASSWORD"]))
		$password = $meta["cimy_uef_wp_PASSWORD"];
	else
		$password = wp_generate_password();

	$user_id = username_exists($user_login);

	$user_already_exists = false;
	if ( ! $user_id )
		$user_id = wp_create_user( $user_login, $password, $user_email );
	else
		$user_already_exists = true;

	if ( ! $user_id )
		return new WP_Error('create_user', __('Could not create user'), $signup);
	else
		cimy_register_user_extra_fields($user_id, $password, $meta);

	if ((empty($meta["cimy_uef_wp_PASSWORD"])) && ($user_already_exists))
		update_user_option( $user_id, 'default_password_nag', true, true ); //Set up the Password change nag.

	$now = current_time('mysql', true);

	$wpdb->update( $wpdb->prefix."signups", array('active' => 1, 'activated' => $now), array('activation_key' => $key) );

	if ($user_already_exists)
		return new WP_Error( 'user_already_exists', __( 'That username is already activated.', $cimy_uef_domain), $signup);

	$options = cimy_get_options();
	wp_new_user_notification_original($user_id, $password, $options["mail_include_fields"], $meta, cimy_wpml_translate_string("a_opt_welcome_email", $options["welcome_email"]));
	return array('user_id' => $user_id, 'password' => $password, 'meta' => $meta);
}

function cimy_check_user_on_signups($errors, $user_name, $user_email) {
	global $wpdb, $cimy_uef_domain;

	// Has someone already signed up for this username?
	$signup = $wpdb->get_row( $wpdb->prepare("SELECT * FROM ".$wpdb->prefix."signups WHERE user_login = %s", $user_name) );
	if ( $signup != null ) {
		$registered_at =  mysql2date('U', $signup->registered);
		$now = current_time( 'timestamp', true );
		$diff = $now - $registered_at;
		// If registered more than two days ago or already approved and then deleted, cancel registration and let this signup go through.
		if (($diff > 172800) || ($signup->active))
			$wpdb->query( $wpdb->prepare("DELETE FROM ".$wpdb->prefix."signups WHERE user_login = %s", $user_name) );
		else
			$errors->add('user_name', __('That username is currently reserved but may be available in a couple of days.', $cimy_uef_domain));

		if ( $signup->active == 0 && $signup->user_email == $user_email )
			$errors->add('user_email_used', __('username and email used', $cimy_uef_domain));
	}

	$signup = $wpdb->get_row( $wpdb->prepare("SELECT * FROM ".$wpdb->prefix."signups WHERE user_email = %s", $user_email) );
	if ( $signup != null ) {
		$diff = current_time( 'timestamp', true ) - mysql2date('U', $signup->registered);
		// If registered more than two days ago or already approved and then deleted, cancel registration and let this signup go through.
		if (($diff > 172800) || ($signup->active))
			$wpdb->query( $wpdb->prepare("DELETE FROM ".$wpdb->prefix."signups WHERE user_email = %s", $user_email) );
		else
			$errors->add('user_email', __('That email address has already been used. Please check your inbox for an activation email. It will become available in a couple of days if you do nothing.', $cimy_uef_domain));
	}

	return $errors;
}
