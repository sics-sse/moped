<?php

function cimy_save_options() {
	global $wpdb, $cimy_uef_version, $wpdb_wp_fields_table, $max_length_fieldset_value, $cimy_uef_domain, $wp_hidden_fields, $max_length_extra_fields_title;

	if (!cimy_check_admin('manage_options'))
		return;
		
	if (isset($_POST['force_activation'])) {
		cimy_plugin_install();
		return;
	}

	if (!check_admin_referer('cimy_uef_options', 'cimy_uef_optionsnonce'))
		return;

	$results = array();
	$do_not_save_options = false;
	
	$options = cimy_get_options();
	
	$old_wp_hidden_fields = $options['wp_hidden_fields'];
	
	$options['aue_hidden_fields'] = array();
	$options['wp_hidden_fields'] = array();

	$options['welcome_email'] = stripslashes($_POST['welcome_email']);
	cimy_wpml_register_string("a_opt_welcome_email", $options['welcome_email']);
	$options['extra_fields_title'] = stripslashes($_POST['extra_fields_title']);
	$options['extra_fields_title'] = substr($options['extra_fields_title'], 0, $max_length_extra_fields_title);
	cimy_wpml_register_string("a_opt_extra_fields_title", $options['extra_fields_title']);

	$options['fieldset_title'] = stripslashes($_POST['fieldset_title']);
	$options['fieldset_title'] = substr($options['fieldset_title'], 0, $max_length_fieldset_value);
	$fieldset_titles = explode(",", $options['fieldset_title']);
	if (!empty($fieldset_titles)) {
		foreach ($fieldset_titles as $fset_key => $fieldset) {
			cimy_wpml_register_string("a_opt_fieldset_title_".$fset_key, $fieldset);
		}
	}

	$old_reg_log = $options['registration-logo'];
	$registration_logo = cimy_manage_upload("registration_logo", "", array(), empty($old_reg_log) ? false : basename($old_reg_log), isset($_POST['registration_logo_del']), "registration-logo");
	if ((!empty($registration_logo)) || (isset($_POST['registration_logo_del']))) {
		$options['registration-logo'] = $registration_logo;
	}

	if (isset($_POST['db_wp_fields_check'])) {
		switch ($_POST['db_wp_fields']) {
			case 'empty':
				cimy_manage_db('empty_wp_fields');
				$results['empty_wp_fields'] = __("WordPress Fields table emptied", $cimy_uef_domain);
				break;
				
			case 'delete':
				cimy_manage_db('drop_wp_fields');
				$results['empty_wp_fields'] = __("WordPress Fields table deleted", $cimy_uef_domain);
				break;
		}
	}
	
	if (isset($_POST['db_extra_fields_check'])) {
		switch ($_POST['db_extra_fields']) {
			case 'empty':
				cimy_manage_db('empty_extra_fields');
				$results['empty_extra_fields'] = __("Extra Fields table emptied", $cimy_uef_domain);
				break;
				
			case 'delete':
				cimy_manage_db('drop_extra_fields');
				$results['empty_extra_fields'] = __("Extra Fields table deleted", $cimy_uef_domain);
				break;
		}
	}

	if (isset($_POST['db_data_check'])) {
		switch ($_POST['db_data']) {
			case 'empty':
				cimy_manage_db('empty_data');
				$results['empty_data'] = __("Users Data table emptied", $cimy_uef_domain);
				break;
				
			case 'delete':
				cimy_manage_db('drop_data');
				$results['empty_data'] = __("Users Data table deleted", $cimy_uef_domain);
				break;
		}
	}

	if (isset($_POST['db_options_check'])) {
		
		switch ($_POST['db_options']) {
			case 'default':
				cimy_manage_db('default_options');
				$do_not_save_options = true;
				$results['results'] = __("Options set to default values", $cimy_uef_domain);
				break;
				
			case 'delete':
				cimy_manage_db('drop_options');
				$do_not_save_options = true;
				$results['results'] = __("Options deleted", $cimy_uef_domain);
				break;
		}
	}
	
	if (isset($_POST['do_not_save_options']))
		$do_not_save_options = true;

	if (isset($_POST['hide_username']))
		array_push($options['aue_hidden_fields'], 'username');

	if (isset($_POST['hide_name']))
		array_push($options['aue_hidden_fields'], 'name');

	if (isset($_POST['hide_posts']))
		array_push($options['aue_hidden_fields'], 'posts');
	
	if (isset($_POST['hide_email']))
		array_push($options['aue_hidden_fields'], 'email');
	
	if (isset($_POST['hide_website']))
		array_push($options['aue_hidden_fields'], 'website');
	
	if (isset($_POST['hide_role']))
		array_push($options['aue_hidden_fields'], 'role');
	
	$tot_wp_hidden_fields = count($old_wp_hidden_fields);
	$action = "add";

	(isset($_POST['confirm_email'])) ? $options['confirm_email'] = true : $options['confirm_email'] = false;
	(isset($_POST['confirm_form'])) ? $options['confirm_form'] = true : $options['confirm_form'] = false;
	if ($options['confirm_email'])
		cimy_force_signup_table_creation();
	(isset($_POST['redirect_to'])) ? $options['redirect_to'] = $_POST['redirect_to'] : $options['redirect_to'] = "";
	(isset($_POST['mail_include_fields'])) ? $options['mail_include_fields'] = true : $options['mail_include_fields'] = false;

	if (isset($_POST['captcha']))
		$options['captcha'] = $_POST['captcha'];

	if (isset($_POST['recaptcha_public_key'])) {
		$options['recaptcha_public_key'] = trim($_POST['recaptcha_public_key']);
	}

	if (isset($_POST['recaptcha_private_key'])) {
		$options['recaptcha_private_key'] = trim($_POST['recaptcha_private_key']);
	}

	if (!isset($results['empty_wp_fields'])) {
		if (isset($_POST['show_wp_password'])) {
			array_push($options['wp_hidden_fields'], 'password');
			
			if (!in_array("password", $old_wp_hidden_fields)) {
				$data = $wp_hidden_fields['password'];
				
				$data['num_fields'] = $tot_wp_hidden_fields;
				$tot_wp_hidden_fields++;
				
				cimy_save_field($action, $wpdb_wp_fields_table, $data);
			}

			if (isset($_POST['show_wp_password2'])) {
				array_push($options['wp_hidden_fields'], 'password2');
				
				if (!in_array("password2", $old_wp_hidden_fields)) {
					$data = $wp_hidden_fields['password2'];
					
					$data['num_fields'] = $tot_wp_hidden_fields;
					$tot_wp_hidden_fields++;
					
					cimy_save_field($action, $wpdb_wp_fields_table, $data);
				}
			}

			(isset($_POST['show_wp_password_meter'])) ? $options['password_meter'] = true : $options['password_meter'] = false;
		}
		else
			$options['password_meter'] = false;

		$db_wp_fields_independent = array("username", "firstname", "lastname", "nickname", "website", "aim", "yahoo", "jgt", "bio-info");
		foreach ($db_wp_fields_independent as $wp_field_independent) {
			if (isset($_POST['show_wp_'.$wp_field_independent])) {
				array_push($options['wp_hidden_fields'], $wp_field_independent);
				
				if (!in_array($wp_field_independent, $old_wp_hidden_fields)) {
					$data = $wp_hidden_fields[$wp_field_independent];
					
					$data['num_fields'] = $tot_wp_hidden_fields;
					$tot_wp_hidden_fields++;
					
					cimy_save_field($action, $wpdb_wp_fields_table, $data);
				}
			}
		}
	}

	$all_wp_fields = get_cimyFields(true);
	$sql = "DELETE FROM ".$wpdb_wp_fields_table." WHERE ";

	$k = (-1);
	$j = (-1);
	$msg = "";
	$not_del_old = "";
	$not_del_sql = "";

	foreach ($all_wp_fields as $wp_field) {
		$f_name = strtolower($wp_field['NAME']);
		$f_order = intval($wp_field['F_ORDER']);

		if (!in_array($f_name, $options['wp_hidden_fields'])) {
			if (in_array($f_name, $old_wp_hidden_fields)) {
				if ($k > (-1)) {
					$sql.= " OR ";
					$msg.= ", ";
				}
				else {
					$k = $f_order;
					$j = $f_order;
				}
	
				$sql.= "F_ORDER=".$f_order;
				$msg.= $f_order;
			}
		}
		// field to NOT be deleted, but order probably have to change, if j==(-1) then order is ok because deletions is after it!
		else {
			if ($j > (-1)) {
				if ($not_del_old != "") {
				
					$not_del_old.= ", ";
				}

				$not_del_sql.= " WHEN ".$f_order." THEN ".$j." ";
				$not_del_old.= $f_order;
				$j++;
			}
		}
	}

	// if at least one field was selected
	if ($k > (-1)) {
		// $sql WILL BE: DELETE FROM <table> WHERE F_ORDER=<value1> [OR F_ORDER=<value2> ...]
		$wpdb->query($sql);

		if ($not_del_sql != "") {
			$not_del_sql = "UPDATE ".$wpdb_wp_fields_table." SET F_ORDER=CASE F_ORDER".$not_del_sql."ELSE F_ORDER END WHERE F_ORDER IN(".$not_del_old.")";

			// $not_del_sql WILL BE: UPDATE <table> SET F_ORDER=CASE F_ORDER WHEN <oldvalue1> THEN <newvalue1> [WHEN ... THEN ...] ELSE F_ORDER END WHERE F_ORDER IN(<oldvalue1> [, <oldvalue2>...])
			$wpdb->query($not_del_sql);
		}
	}
	
	if (!$do_not_save_options) {
		cimy_set_options($options);
		
		$results['results'] = __("Options changed", $cimy_uef_domain);
	}
	
	return $results;
}

function cimy_show_options_notembedded() {
	$results = array();
	
	cimy_show_options($results, false);
}

function cimy_show_options($results, $embedded) {
	global $wpdb, $wpdb_wp_fields_table, $wpdb_fields_table, $wpdb_data_table, $max_length_fieldset_value, $cimy_uef_name, $cimy_uef_url, $cimy_project_url, $cimy_uef_version, $cimy_uef_domain, $cimy_top_menu, $max_length_extra_fields_title, $cuef_upload_path, $cuef_plugin_dir, $cimy_uef_plugins_dir;

	if (!cimy_check_admin('manage_options'))
		return;

	// save options engine
	if ((isset($_POST['cimy_options'])) && (isset($cimy_top_menu)))
		$results = cimy_save_options();

	$options = cimy_get_options();
	if (!empty($options['version']) && $cimy_uef_version != $options['version'])
		$options = cimy_plugin_install();

	$warning_msg = esc_js(__("Please upload an image with one of the following extensions", $cimy_uef_domain));

	if ($options) {
		if (is_writable(WP_CONTENT_DIR)) {
			if (!is_dir($cuef_upload_path)) {
				if (defined("FS_CHMOD_DIR"))
					@mkdir($cuef_upload_path, FS_CHMOD_DIR);
				else
					@mkdir($cuef_upload_path, 0777);
			}

			if (is_multisite()) {
				if ($cimy_uef_plugins_dir == "plugins") {
					if (!is_dir(WP_CONTENT_DIR.'/mu-plugins')) {
						if (defined("FS_CHMOD_DIR"))
							@mkdir(WP_CONTENT_DIR.'/mu-plugins', FS_CHMOD_DIR);
						else
							@mkdir(WP_CONTENT_DIR.'/mu-plugins', 0777);
					}
					if (!is_file(WP_CONTENT_DIR.'/mu-plugins/cimy_uef_mu_activation.php'))
						copy($cuef_plugin_dir.'/cimy_uef_mu_activation.php', WP_CONTENT_DIR.'/mu-plugins/cimy_uef_mu_activation.php');
				}
				else if ($cimy_uef_plugins_dir == "mu-plugins") {
					if (is_file(WP_CONTENT_DIR.'/mu-plugins/cimy_uef_mu_activation.php'))
						unlink(WP_CONTENT_DIR.'/mu-plugins/cimy_uef_mu_activation.php');
				}
			}
		}

		$options['fieldset_title'] = esc_attr($options['fieldset_title']);
		$welcome_email = $options['welcome_email'];
		isset($options['recaptcha_public_key']) ? $recaptcha_public_key = $options['recaptcha_public_key'] : $recaptcha_public_key = '';
		isset($options['recaptcha_private_key']) ? $recaptcha_private_key = $options['recaptcha_private_key'] : $recaptcha_private_key = '';

		$db_options = true;
	}
	else {
		$db_options = false;
		$options['fieldset_title'] = "";
		$welcome_email = '';
		$recaptcha_public_key = '';
		$recaptcha_private_key = '';
	}
	
	if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_wp_fields_table'") == $wpdb_wp_fields_table) {
		$sql = "SELECT id, COUNT(*) FROM ".$wpdb_wp_fields_table." GROUP BY id";
		$db_wp_fields = $wpdb->query($sql);
	}
	else
		$db_wp_fields = -1;
	
	if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_fields_table'") == $wpdb_fields_table) {
		$sql = "SELECT id, COUNT(*) FROM ".$wpdb_fields_table." GROUP BY id";
		$db_extra_fields = $wpdb->query($sql);
	}
	else
		$db_extra_fields = -1;
	
	if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_data_table'") == $wpdb_data_table)
		$db_users_data = true;
	else
		$db_users_data = false;
	
	$ret = array();
	
	$ret['db_options'] = $db_options;
	$ret['db_extra_fields'] = $db_extra_fields;
	$ret['db_wp_fields'] = count($options['wp_hidden_fields']);
	$ret['db_users_data'] = $db_users_data;

	if ((isset($cimy_top_menu)) && ($embedded))
		return $ret;

	$update_db_label = esc_js(__("This operation will create/update all missing tables/options, do you want to proceed?", $cimy_uef_domain));
	
	?>
	
	<div class="wrap" id="options">
	<?php
		if (function_exists("screen_icon"))
			screen_icon("options-general");
	?>
	<h2><?php _e("Options");
	
	if (!isset($cimy_top_menu)) {
		?> - <a href="#addfield"><?php _e("Add a new Field", $cimy_uef_domain); ?></a> - <a href="#extrafields"><?php _e("Extra Fields", $cimy_uef_domain); ?></a><?php
	}
	?></h2>
	<table class="form-table">
		<tr>
			<th scope="row" width="40%">
				<strong><a href="<?php echo $cimy_project_url; ?>"><?php _e("Support the Cimy Project", $cimy_uef_domain); ?></a></strong>
			</th>
			<td width="60%">
				<form style="text-align: left;" action="https://www.paypal.com/cgi-bin/webscr" method="post"> <input name="cmd" type="hidden" value="_s-xclick" />
				<input name="hosted_button_id" type="hidden" value="8774924" />
				<input alt="PayPal - The safer, easier way to pay online." name="submit" src="https://www.paypal.com/en_US/GB/i/btn/btn_donateCC_LG.gif" type="image" />
				<img src="https://www.paypal.com/it_IT/i/scr/pixel.gif" border="0" alt="" width="1" height="1" />
				</form>
				<?php _e("This plug-in is the results of hours of development to add new features, support new WordPress versions and fix bugs, please donate money if saved you from spending all these hours!", $cimy_uef_domain); ?>
			</td>
		</tr>
	</table>
<?php

	// print successes if there are some
	if (count($results) > 0) {
	?>
		<div class="updated">
		<h3><?php _e("SUCCESSFUL", $cimy_uef_domain); ?></h3>
		<ul>
			<?php 
			foreach ($results as $result)
				echo "<li>".$result."</li>";
			?>
		</ul>
		<br />
		</div>
	<?php
	}

	?><form method="post" action="#options" id="cimy_uef_options">
	<?php wp_nonce_field('cimy_uef_options', 'cimy_uef_optionsnonce', false); ?>
	<p class="submit" style="border-width: 0px;"><input class="button-primary" type="submit" name="Submit" value="<?php _e('Save Changes') ?>" /></p>
	<h3><?php _e("General"); ?></h3>
	<table class="form-table">
		<tr>
			<th scope="row" width="40%">
				<strong><a href="<?php echo $cimy_uef_url; ?>"><?php echo $cimy_uef_name; ?></a></strong>
			</th>
			<td width="60%">v<?php echo $options['version'];
				if ($cimy_uef_version != $options['version']) {
					?> (<?php _e("installed is", $cimy_uef_domain); ?> v<?php echo $cimy_uef_version; ?>)<?php
				}
				
				if (!$db_options) {
					?><br /><h4><?php _e("OPTIONS DELETED!", $cimy_uef_domain); ?></h4>
					<input type="hidden" name="do_not_save_options" value="1" />

					<p class="submit" style="border-width: 0px;"><input class="button-primary" type="submit" name="force_activation" value="<?php _e("Fix the problem", $cimy_uef_domain); ?>" onclick="return confirm('<?php echo $update_db_label; ?>');" /></p><?php
				}
				else if ($cimy_uef_version != $options['version']) {
					?><br /><h4><?php _e("VERSIONS MISMATCH! This because you haven't de-activated and re-activated the plug-in after the update! This could give problems...", $cimy_uef_domain); ?></h4>

					<p class="submit" style="border-width: 0px;"><input class="button-primary" type="submit" name="force_activation" value="<?php _e("Fix the problem", $cimy_uef_domain); ?>" onclick="return confirm('<?php echo $update_db_label; ?>');" /></p><?php
				}
				?>
			</td>
		</tr>
		<tr>
			<th scope="row"><?php _e("Picture/Avatar upload", $cimy_uef_domain); ?></th>
			<td>
			<?php
				if (is_writable($cuef_upload_path))
					echo "<em>".$cuef_upload_path."</em><br />".__("is created and writable", $cimy_uef_domain);
				else
					echo "<em>".$cuef_upload_path."</em><br />".__("is NOT created or webserver does NOT have permission to write on it", $cimy_uef_domain);
			?>
			</td>
		</tr>
		<tr>
			<th scope="row">
				<input type="checkbox" name="mail_include_fields" value="1"<?php checked(true, $options['mail_include_fields'], true); ?> />
				<?php _e("Show all fields in the welcome email", $cimy_uef_domain); ?>
			</th>
			<td>
			<?php
				_e("the email sent to the admin and to the user upon registration will have all fields", $cimy_uef_domain);
			?>
			</td>
		</tr>
<?php if (!is_multisite()) { ?>
		<tr>
			<th scope="row">
				<input type="checkbox" name="confirm_email" value="1"<?php checked(true, $options['confirm_email'], true); ?> />
				<?php _e("Enable email confirmation", $cimy_uef_domain); ?>
			</th>
			<td>
			<?php
				_e("user that registers should confirm its email address via a link click", $cimy_uef_domain);
				echo "<br />";
				_e("<strong>note:</strong> this option turned on will automatically disable (only during the registration) all upload fields: file, picture, avatar", $cimy_uef_domain);
			?>
			</td>
		</tr>
		<tr>
			<th scope="row">
				<input type="checkbox" name="confirm_form" value="1"<?php checked(true, $options['confirm_form'], true); ?> />
				<?php _e("Enable form confirmation", $cimy_uef_domain); ?>
			</th>
			<td>
			<?php
				_e("a summary of the registration form will be presented to the user", $cimy_uef_domain);
			?>
			</td>
		</tr>
		<tr>
			<th scope="row">
				<?php _e("Customize welcome email sent to the new user", $cimy_uef_domain); ?>
			</th>
			<td>
				<textarea name="welcome_email" rows="6" cols="50"><?php echo esc_html($welcome_email); ?></textarea><br />
				<?php _e("if you change or remove the placeholders then the email won't have the correct information", $cimy_uef_domain); ?>
			</td>
		</tr>
		<tr>
			<th scope="row">
				<input type="checkbox" name="redirect_to" value="source"<?php checked("source", $options['redirect_to'], true); ?> />
				<?php _e("Redirect to the source", $cimy_uef_domain); ?>
			</th>
			<td>
			<?php
				_e("after the registration or confirmation the user will be redirected to the address where was exactly before clicking on the registration link", $cimy_uef_domain);
			?>
			</td>
		</tr>
<?php } ?>
		<tr>
			<th scope="row">
				<input type="radio" name="captcha" value="none"<?php checked("none", $options['captcha'], true); ?> />
				<?php _e('No captcha', $cimy_uef_domain); ?></a>
			</th>
			<td>
			</td>
		</tr>
		<tr>
			<th scope="row">
				<input type="radio" name="captcha" value="recaptcha"<?php checked("recaptcha", $options['captcha'], true); ?> />
				<?php _e('Enable <a href="http://www.google.com/recaptcha" target="_blank">reCAPTCHA</a>', $cimy_uef_domain); ?></a>
			</th>
			<td>
			<?php
				_e("Public KEY", $cimy_uef_domain);
			?>
				<input type="text" name="recaptcha_public_key" value="<?php echo esc_attr($recaptcha_public_key); ?>" size="40" /><br />
			<?php
				_e("Private KEY", $cimy_uef_domain);
			?>
				<input type="text" name="recaptcha_private_key" value="<?php echo esc_attr($recaptcha_private_key); ?>" size="40" />
			</td>
		</tr>
		<tr>
			<th scope="row">
				<input type="radio" name="captcha" value="securimage"<?php checked("securimage", $options['captcha'], true); ?> />
				<?php _e('Enable <a href="http://www.phpcaptcha.org/" target="_blank">Securimage Captcha</a>', $cimy_uef_domain); ?></a>
			</th>
			<td>
				<?php _e('This captcha is probably weaker, but is easier for users', $cimy_uef_domain); ?>
				<?php
				if (!is_file($cuef_plugin_dir.'/securimage/securimage.php')) {
					echo "<br />";
					printf(__('<strong>WARNING: to activate this captcha download <a href="http://www.phpcaptcha.org/latest.zip" target="_blank">this package</a> and unpack it under %s</strong>', $cimy_uef_domain), $cuef_plugin_dir.'/recaptcha/');
				}
			      ?>
			</td>
		</tr>
<?php if (!is_multisite()) { ?>
		<tr>
			<th scope="row"><?php _e("Change login/registration page logo", $cimy_uef_domain); ?></th>
			<td>
				<?php if (!empty($options["registration-logo"])) { ?><input type="hidden" name="registration_logo_oldfile" value="<?php echo basename($options["registration-logo"]); ?>" />
				<?php echo esc_html(basename($options["registration-logo"])).'<br />'; ?>
				<input type="checkbox" name="registration_logo_del" value="1" />
				<?php echo " ".__("Delete the picture", $cimy_uef_domain); ?><br /><br /><?php } ?>

				<input type="file" id="registration_logo" name="registration_logo" onchange="uploadFile('cimy_uef_options', 'registration_logo', '<?php echo $warning_msg; ?>', Array('gif', 'png', 'jpg', 'jpeg', 'tiff'))" />
				<?php _e("Maximum recommended logo width is 328px, but any height should work.", $cimy_uef_domain);?>
			</td>
		</tr>
<?php } ?>
	</table>
	<br />
	<h3><?php _e("Database", $cimy_uef_domain); ?></h3>
	<table class="form-table">
		<tr>
			<th scope="row" width="40%"><input type="checkbox" name="db_options_check" value="1" /> Cimy User Extra Fields <?php _e("Options"); ?></th>
			<td width="60%">
				<?php
				if ($db_options) {
					?>
					<select name="db_options">
						<option value="none">- <?php _e("select action", $cimy_uef_domain); ?> -</option>
						<option value="default"><?php _e("Default values", $cimy_uef_domain); ?></option>
						<option value="delete"><?php _e("Delete"); ?></option>
					</select><?php
				}
				else
					echo "<strong>".__("NOT PRESENT", $cimy_uef_domain)."</strong>";
				?>
			</td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="db_wp_fields_check" value="1" /> <?php _e("WordPress Fields table", $cimy_uef_domain); ?></th>
			<td>
				<?php
				if ($db_wp_fields >= 0) {
					?>
					<select name="db_wp_fields">
						<option value="none">- <?php _e("select action", $cimy_uef_domain); ?> -</option>
						<option value="empty"><?php _e("Empty", $cimy_uef_domain); ?></option>
						<option value="delete"><?php _e("Delete"); ?></option>
					</select><?php
				}
				else
					echo "<strong>".__("NOT PRESENT", $cimy_uef_domain)."</strong>";
				?>
			</td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="db_extra_fields_check" value="1" /> <?php _e("Extra Fields table", $cimy_uef_domain); ?></th>
			<td>
				<?php
				if ($db_extra_fields >= 0) {
					?>
					<select name="db_extra_fields">
						<option value="none">- <?php _e("select action", $cimy_uef_domain); ?> -</option>
						<option value="empty"><?php _e("Empty", $cimy_uef_domain); ?></option>
						<option value="delete"><?php _e("Delete"); ?></option>
					</select><?php
				}
				else
					echo "<strong>".__("NOT PRESENT", $cimy_uef_domain)."</strong>";
				?>
			</td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="db_data_check" value="1" /> <?php _e("Users Data table", $cimy_uef_domain); ?></th>
			<td>
				<?php
				if ($db_users_data) {
					?>
					<select name="db_data">
						<option value="none">- <?php _e("select action", $cimy_uef_domain); ?> -</option>
						<option value="empty"><?php _e("Empty", $cimy_uef_domain); ?></option>
						<option value="delete"><?php _e("Delete"); ?></option>
					</select> <?php
					_e("all data inserted by users in all and only extra fields", $cimy_uef_domain);
				}
				else
					echo "<strong>".__("NOT PRESENT", $cimy_uef_domain)."</strong>";
				?>
			</td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="force_activation" value="1" /> <?php _e("Force tables creation", $cimy_uef_domain); ?></th>
			<td>
			<?php
				_e("equivalent to de-activate and activate the plug-in; no other operation will be performed", $cimy_uef_domain);
			?>
			</td>
		</tr>
	</table>
	<br />
	<h3><?php _e("User Profile", $cimy_uef_domain); ?></h3>
	<table class="form-table">
		<tr>
			<th scope="row" width="40%"><?php _e("Extra Fields section title", $cimy_uef_domain); ?></th>
			<td width="60%"><input type="text" name="extra_fields_title" value="<?php echo esc_attr($options['extra_fields_title']); ?>" size="35" maxlength="<?php echo $max_length_extra_fields_title; ?>" /></td>
		</tr>
		<tr>
			<th scope="row"><?php _e("Fieldset's titles, separates with comma", $cimy_uef_domain); ?><br /><?php _e("example: title1,title2,title3", $cimy_uef_domain); ?></th>
			<td><input type="text" name="fieldset_title" value="<?php echo esc_attr($options['fieldset_title']); ?>" size="35" maxlength="<?php echo $max_length_fieldset_value; ?>" /> <?php _e("<strong>note:</strong> if you change order or remove fieldsets you may need to set all extra fields' fieldset assigment again", $cimy_uef_domain); ?></td>
		</tr>

	</table>
	<br />
	<h3><?php _e("Authors &amp; Users Extended", $cimy_uef_domain); ?></h3>
	<table class="form-table">
		<tr>
			<th scope="row" width="40%">
				<input type="checkbox" name="hide_username" value="1"<?php checked(true, in_array('username', $options['aue_hidden_fields']), true); ?> /> <?php _e("Hide username field", $cimy_uef_domain); ?>
			</th>
			<td width="60%"></td>
		</tr>
		<tr>
			<th>
				<input type="checkbox" name="hide_name" value="1"<?php checked(true, in_array('name', $options['aue_hidden_fields']), true); ?> /> <?php _e("Hide name field", $cimy_uef_domain); ?>
			</th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="hide_email" value="1"<?php checked(true, in_array('email', $options['aue_hidden_fields']), true); ?> /> <?php _e("Hide email field", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="hide_role" value="1"<?php checked(true, in_array('role', $options['aue_hidden_fields']), true); ?> /> <?php _e("Hide role field", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="hide_website" value="1"<?php checked(true, in_array('website', $options['aue_hidden_fields']), true); ?> /> <?php _e("Hide website field", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th><input type="checkbox" name="hide_posts" value="1"<?php checked(true, in_array('posts', $options['aue_hidden_fields']), true); ?> /> <?php _e("Hide n. posts field", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
	</table>
	<br />
	<h3><?php _e("WordPress hidden fields", $cimy_uef_domain); ?></h3>
	<table class="form-table">
<?php if (!is_multisite()) { ?>
		<tr>
			<th scope="row" width="40%"><input type="checkbox" name="show_wp_username" value="1"<?php checked(true, in_array('username', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show username", $cimy_uef_domain); ?></th>
			<td width="60%"><?php _e("when unchecked the email address will be used as username", $cimy_uef_domain); ?></td>
		</tr>
<?php } ?>
		<tr>
			<th scope="row" width="40%"><input type="checkbox" name="show_wp_password" value="1"<?php checked(true, in_array('password', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show password", $cimy_uef_domain); ?></th>
			<td width="60%"></td>
		</tr>
		<tr>
			<th>&nbsp;&nbsp;&nbsp;<input type="checkbox" name="show_wp_password2" value="1"<?php checked(true, in_array('password2', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show confirmation password", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th>&nbsp;&nbsp;&nbsp;<input type="checkbox" name="show_wp_password_meter" value="1"<?php checked(true, $options['password_meter'], true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show password strength meter", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th><input type="checkbox" name="show_wp_firstname" value="1"<?php checked(true, in_array('firstname', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show first name", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th><input type="checkbox" name="show_wp_lastname" value="1"<?php checked(true, in_array('lastname', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show last name", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th><input type="checkbox" name="show_wp_nickname" value="1"<?php checked(true, in_array('nickname', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show nickname", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="show_wp_website" value="1"<?php checked(true, in_array('website', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show website", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="show_wp_aim" value="1"<?php checked(true, in_array('aim', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show AIM", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="show_wp_yahoo" value="1"<?php checked(true, in_array('yahoo', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show Yahoo IM", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="show_wp_jgt" value="1"<?php checked(true, in_array('jgt', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show Jabber / Google Talk", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
		<tr>
			<th scope="row"><input type="checkbox" name="show_wp_bio-info" value="1"<?php checked(true, in_array('bio-info', $options['wp_hidden_fields']), true); disabled(true, $db_wp_fields < 0, true); ?> /> <?php _e("Show Biographical Info", $cimy_uef_domain); ?></th>
			<td></td>
		</tr>
	</table>
	<input type="hidden" name="cimy_options" value="1" />
	<p class="submit"><input class="button-primary" type="submit" name="Submit" value="<?php _e('Save Changes') ?>" /></p>
	</form>
	</div>
	<br />
	<?php
	
	return $ret;
}
