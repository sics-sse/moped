<?php

function cimy_plugin_install () {
	// for WP >= 2.5 when adding a global here need to be added also to main global
	global $wpdb, $old_wpdb_data_table, $wpdb_data_table, $old_wpdb_fields_table, $wpdb_fields_table, $wpdb_wp_fields_table, $cimy_uef_options, $cimy_uef_version, $cuef_upload_path, $cimy_uef_domain;

	if (!cimy_check_admin('activate_plugins'))
		return;
	
	$force_update = false;
	
	if (!($options = cimy_get_options())) {
		cimy_manage_db('new_options');
		$options = cimy_get_options();
	}
	else
		$force_update = true;

	$charset_collate = "";
	// try to get proper charset and collate
	if ($wpdb->has_cap('collation')) {
		if ( ! empty($wpdb->charset) )
			$charset_collate = " DEFAULT CHARACTER SET $wpdb->charset";
		if ( ! empty($wpdb->collate) )
			$charset_collate .= " COLLATE $wpdb->collate";
	}

	if ($force_update) {
		if (version_compare($options['version'], "0.9.1", "<=") === true) {
			unset($options['show_buggy_ie_warning']);
		}

		if (version_compare($options['version'], "1.0.0-beta1", "<=") === true) {
			$sql = "RENAME TABLE ".$old_wpdb_fields_table." TO ".$wpdb_fields_table;
			$wpdb->query($sql);
			
			$sql = "RENAME TABLE ".$old_wpdb_data_table." TO ".$wpdb_data_table;
			$wpdb->query($sql);

			$options['wp_hidden_fields'] = array();
			
			// convert all html entity to normal chars
			$sql = "SELECT * FROM ".$wpdb_fields_table;
			$fields = $wpdb->get_results($sql, ARRAY_A);
			
			foreach ($fields as $field) {
				$id = $field['ID'];
				$name = $wpdb->escape(html_entity_decode($field['NAME'], ENT_QUOTES, "UTF-8"));
				$label = $wpdb->escape(html_entity_decode($field['LABEL'], ENT_QUOTES, "UTF-8"));
				$desc = $wpdb->escape(html_entity_decode($field['DESCRIPTION'], ENT_QUOTES, "UTF-8"));
				$value = $wpdb->escape(html_entity_decode($field['VALUE'], ENT_QUOTES, "UTF-8"));
				
				$rules = unserialize($field['RULES']);
				$rules['equal_to'] = html_entity_decode($rules['equal_to'], ENT_QUOTES, "UTF-8");
				$rules = $wpdb->escape(serialize($rules));
				
				$sql = "UPDATE ".$wpdb_fields_table." SET name='".$name."', value='".$value."', description='".$desc."', label='".$label."', rules='".$rules."' WHERE ID=".$id;
				
				$wpdb->query($sql);
			}
		}

		if (version_compare($options['version'], "1.1.0-rc1", "<=") === true) {
			$sql = "SELECT ID FROM ".$wpdb_fields_table." WHERE TYPE='picture'";
			$f_pictures = $wpdb->get_results($sql, ARRAY_A);
			
			if (isset($f_pictures)) {
				if ($f_pictures != NULL) {
					foreach ($f_pictures as $f_picture) {
						$sql = "SELECT VALUE FROM ".$wpdb_data_table." WHERE FIELD_ID=".$f_picture['ID'];
						$p_filenames = $wpdb->get_results($sql, ARRAY_A);

						if (isset($p_filenames)) {
							if ($p_filenames != NULL) {
								foreach ($p_filenames as $p_filename) {
									$path_pieces = explode("/", $p_filename['VALUE']);
									$p_filename = basename($p_filename['VALUE']);
									$user_login = array_slice($path_pieces, -2, 1);
									
									$p_oldfilename_t = $cuef_upload_path.$user_login[0]."/".cimy_get_thumb_path($p_filename, true);
									$p_newfilename_t = $cuef_upload_path.$user_login[0]."/".cimy_get_thumb_path($p_filename, false);
									
									if (is_file($p_oldfilename_t))
										rename($p_oldfilename_t, $p_newfilename_t);
								}
							}
						}
					}
				}
			}
		}

		if (version_compare($options['version'], "1.1.0", "<=") === true) {
			if ($charset_collate != "") {
				$sql = "ALTER TABLE ".$wpdb_fields_table.$charset_collate;
				$wpdb->query($sql);
				
				$sql = "ALTER TABLE ".$wpdb_wp_fields_table.$charset_collate;
				$wpdb->query($sql);
				
				$sql = "ALTER TABLE ".$wpdb_data_table.$charset_collate;
				$wpdb->query($sql);
			}
		}

		if (version_compare($options['version'], "1.3.0-beta1", "<=") === true) {
			$options["users_per_page"] = 50;
		}

		if (version_compare($options['version'], "1.3.0-beta2", "<=") === true) {
			unset($options["disable_cimy_fieldvalue"]);
		}

		if (version_compare($options['version'], "1.3.1", "<=") === true) {
			$options["extra_fields_title"] = __("Extra Fields", $cimy_uef_domain);

			// Added again since after cleanup DB migration code in v1.3.0-beta2 was buggy!
			if (isset($options["disable_cimy_fieldvalue"]))
				unset($options["disable_cimy_fieldvalue"]);

			if (!isset($options["users_per_page"]))
				$options["users_per_page"] = 50;
		}

		if (version_compare($options['version'], "1.4.0-beta2", "<=") === true) {
			unset($options['items_per_fieldset']);

			$sql = "ALTER TABLE ".$wpdb_fields_table." ADD COLUMN FIELDSET bigint(20) NOT NULL DEFAULT 0 AFTER F_ORDER";
			$wpdb->query($sql);
		}

		if (version_compare($options['version'], "1.4.0", "<=") === true) {
			$sql = "ALTER TABLE ".$wpdb_data_table." MODIFY COLUMN VALUE LONGTEXT";
			$wpdb->query($sql);
		}


		// add $rules[show_in_blog]=true and $rules[show_level]=-1
		if (version_compare($options['version'], "1.5.0-beta1", "<=") === true) {
			for ($i = 0; $i <= 1; $i++) {
				if ($i == 0)
					$the_table = $wpdb_wp_fields_table;
				else
					$the_table = $wpdb_fields_table;

				$sql = "SELECT ID, RULES FROM ".$the_table;
				$all_rules = $wpdb->get_results($sql, ARRAY_A);

				if (isset($all_rules)) {
					foreach ($all_rules as $rule) {
						$rule_to_be_updated = unserialize($rule["RULES"]);
						$rule_id = $rule["ID"];
	
						// do not add show_level to $wpdb_wp_fields_table
						if ((!isset($rule_to_be_updated["show_level"]))  && ($i == 1))
							$rule_to_be_updated["show_level"] = -1;
		
						if (!isset($rule_to_be_updated["show_in_blog"]))
							$rule_to_be_updated["show_in_blog"] = true;
	
						if (!isset($rule_to_be_updated["show_in_search"]))
							$rule_to_be_updated["show_in_search"] = true;
		
						$sql = "UPDATE ".$the_table." SET RULES='".$wpdb->escape(serialize($rule_to_be_updated))."' WHERE ID=".$rule_id;
						$wpdb->query($sql);
					}
				}
			}
		}

		if (version_compare($options['version'], "2.0.0-beta1", "<=") === true) {
			if ($options["recaptcha"])
				$options["captcha"] = "recaptcha";
			else
				$options["captcha"] = "none";
			unset($options["recaptcha"]);

			for ($i = 0; $i <= 1; $i++) {
				if ($i == 0)
					$the_table = $wpdb_wp_fields_table;
				else
					$the_table = $wpdb_fields_table;

				$sql = "SELECT ID, RULES FROM ".$the_table;
				$all_rules = $wpdb->get_results($sql, ARRAY_A);

				if (isset($all_rules)) {
					foreach ($all_rules as $rule) {
						$rule_to_be_updated = unserialize($rule["RULES"]);
						$rule_id = $rule["ID"];
	
						// stupid bug introduced in v2.0.0-beta1
						if (empty($rule_to_be_updated["edit"]))
							$rule_to_be_updated["edit"] = "ok_edit";
		
						$sql = "UPDATE ".$the_table." SET RULES='".$wpdb->escape(serialize($rule_to_be_updated))."' WHERE ID=".$rule_id;
						$wpdb->query($sql);
					}
				}
			}
		}

		if (version_compare($options['version'], "2.0.0-beta2", "<=") === true) {
			$sql = "SELECT DESCRIPTION FROM $wpdb_wp_fields_table WHERE NAME='PASSWORD'";
			$desc = $wpdb->get_var($sql);

			if ($desc == __('<strong>Note:</strong> this website let you personalize your password; after the registration you will receive an e-mail with another password, do not care about that!', $cimy_uef_domain)) {
				$sql = "UPDATE $wpdb_wp_fields_table SET DESCRIPTION='' WHERE NAME='PASSWORD'";
				$wpdb->query($sql);
			}
		}

		if (version_compare($options['version'], "2.1.0-beta1", "<=") === true) {
			$welcome_email = sprintf(__('Username: %s'), "USERNAME") . "\r\n";
			$welcome_email .= sprintf(__('Password: %s'), "PASSWORD") . "\r\n";
			$welcome_email .= "LOGINLINK" . "\r\n";
			$options["welcome_email"] = $welcome_email;
		}

		if (version_compare($options['version'], "2.2.0", "<=") === true) {
			$javascripts_dep = array(
			'file_fields' => array(
				'show_in_reg' => 0,
				'show_in_profile' => 0,
				'show_in_aeu' => 0,
				'show_in_blog' => 0,
				'show_in_search' => 0
			),
			'image_fields' => array(
				'show_in_reg' => 0,
				'show_in_profile' => 0,
				'show_in_aeu' => 0,
				'show_in_blog' => 0,
				'show_in_search' => 0
			),
			'tinymce_fields' => array(
				'show_in_reg' => 0,
				'show_in_profile' => 0,
				'show_in_aeu' => 0,
				'show_in_blog' => 0,
				'show_in_search' => 0
			));
			$sql = "SELECT TYPE, RULES FROM ".$wpdb_fields_table;
			$all_rules = $wpdb->get_results($sql, ARRAY_A);

			if (isset($all_rules)) {
				foreach ($all_rules as $rule) {
					$rules = unserialize($rule["RULES"]);
					$type = $rule["TYPE"];
					$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_reg", $rules["show_in_reg"]);
					$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_profile", $rules["show_in_profile"]);
					$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_aeu", $rules["show_in_aeu"]);
					$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_blog", $rules["show_in_blog"]);
					$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_search", $rules["show_in_search"]);
				}
			}
			$options['file_fields'] = $javascripts_dep['file_fields'];
			$options['image_fields'] = $javascripts_dep['image_fields'];
			$options['tinymce_fields'] = $javascripts_dep['tinymce_fields'];
		}

		if (version_compare($options['version'], "2.3.11", "<=") === true) {
			for ($i = 0; $i <= 1; $i++) {
				if ($i == 0)
					$the_table = $wpdb_wp_fields_table;
				else
					$the_table = $wpdb_fields_table;

				$sql = "SELECT ID, RULES FROM ".$the_table;
				$all_fields = $wpdb->get_results($sql, ARRAY_A);

				if (!empty($all_fields)) {
					foreach ($all_fields as $field) {
						cimy_wpml_register_string($field['NAME']."_label", $field['LABEL']);
						cimy_wpml_register_string($field['NAME']."_desc", $field['DESCRIPTION']);
					}
				}
			}
		}

		if (version_compare($options['version'], "2.4.0", "<=") === true) {
			cimy_wpml_register_string("a_opt_welcome_email", $options['welcome_email']);
			cimy_wpml_register_string("a_opt_extra_fields_title", $options['extra_fields_title']);
			$fieldset_titles = explode(",", $options['fieldset_title']);
			if (!empty($fieldset_titles)) {
				foreach ($fieldset_titles as $fset_key => $fieldset) {
					cimy_wpml_register_string("a_opt_fieldset_title_".$fset_key, $fieldset);
				}
			}
		}

		if (version_compare($options['version'], "2.4.2", "<=") === true) {
			$options["wp_hidden_fields"][] = "username";
		}

		$options['version'] = $cimy_uef_version;

		cimy_set_options($options);
	}
	
	if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_wp_fields_table'") != $wpdb_wp_fields_table) {
		$sql = "CREATE TABLE ".$wpdb_wp_fields_table." (ID bigint(20) NOT NULL AUTO_INCREMENT, F_ORDER bigint(20) NOT NULL, NAME varchar(20), LABEL TEXT, DESCRIPTION TEXT, TYPE varchar(20), RULES TEXT, VALUE TEXT, PRIMARY KEY (ID), INDEX F_ORDER (F_ORDER), INDEX NAME (NAME))".$charset_collate.";";

		require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
		dbDelta($sql);
	}

	if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_data_table'") != $wpdb_data_table) {
		$sql = "CREATE TABLE ".$wpdb_data_table." (ID bigint(20) NOT NULL AUTO_INCREMENT, USER_ID bigint(20) NOT NULL, FIELD_ID bigint(20) NOT NULL, VALUE TEXT NOT NULL, PRIMARY KEY (ID), INDEX USER_ID (USER_ID), INDEX FIELD_ID (FIELD_ID))".$charset_collate.";";

		require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
		dbDelta($sql);
	}

	if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_fields_table'") != $wpdb_fields_table) {
		$sql = "CREATE TABLE ".$wpdb_fields_table." (ID bigint(20) NOT NULL AUTO_INCREMENT, F_ORDER bigint(20) NOT NULL, FIELDSET bigint(20) NOT NULL DEFAULT 0, NAME varchar(20), LABEL TEXT, DESCRIPTION TEXT, TYPE varchar(20), RULES TEXT, VALUE TEXT, PRIMARY KEY (ID), INDEX F_ORDER (F_ORDER), INDEX NAME (NAME))".$charset_collate.";";

		require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
		dbDelta($sql);
	}
	return $options;
}

function cimy_force_signup_table_creation() {
	global $wpdb;
	$charset_collate = "";
	
	// try to get proper charset and collate
	if ($wpdb->has_cap('collation')) {
		if ( ! empty($wpdb->charset) )
			$charset_collate = " DEFAULT CHARACTER SET $wpdb->charset";
		if ( ! empty($wpdb->collate) )
			$charset_collate .= " COLLATE $wpdb->collate";
	}

	if ($wpdb->get_var("SHOW TABLES LIKE '".$wpdb->prefix."signups'") != $wpdb->prefix."signups") {

		$sql = "CREATE TABLE ".$wpdb->prefix."signups (
			domain varchar(200) NOT NULL default '',
			path varchar(100) NOT NULL default '',
			title longtext NOT NULL,
			user_login varchar(60) NOT NULL default '',
			user_email varchar(100) NOT NULL default '',
			registered datetime NOT NULL default '0000-00-00 00:00:00',
			activated datetime NOT NULL default '0000-00-00 00:00:00',
			active tinyint(1) NOT NULL default '0',
			activation_key varchar(50) NOT NULL default '',
			meta longtext,
			KEY activation_key (activation_key),
			KEY domain (domain)
		)".$charset_collate.";";

		require_once(ABSPATH . 'wp-admin/includes/upgrade.php');
		dbDelta($sql);
	}
}

function cimy_manage_db($command) {
	global $wpdb, $wpdb_data_table, $wpdb_wp_fields_table, $wpdb_fields_table, $cimy_uef_options, $cimy_uef_version, $cimy_uef_domain;
	
	if (!cimy_check_admin('activate_plugins'))
		return;

	$welcome_email = sprintf(__('Username: %s'), "USERNAME") . "\r\n";
	$welcome_email .= sprintf(__('Password: %s'), "PASSWORD") . "\r\n";
	$welcome_email .= "LOGINLINK" . "\r\n";

	$options = array(
		'extra_fields_title' => __("Extra Fields", $cimy_uef_domain),
		'users_per_page' => 50,
		'aue_hidden_fields' => array('website', 'posts', 'email'),
		'wp_hidden_fields' => array('username'),
		'fieldset_title' => '',
		'registration-logo' => '',
		'captcha' => 'none',
		'welcome_email' => $welcome_email,
		'confirm_form' => false,
		'confirm_email' => false,
		'password_meter' => false,
		'mail_include_fields' => false,
		'redirect_to' => '',
		'file_fields' => array(
			'show_in_reg' => 0,
			'show_in_profile' => 0,
			'show_in_aeu' => 0,
			'show_in_blog' => 0,
			'show_in_search' => 0
		),
		'image_fields' => array(
			'show_in_reg' => 0,
			'show_in_profile' => 0,
			'show_in_aeu' => 0,
			'show_in_blog' => 0,
			'show_in_search' => 0
		),
		'tinymce_fields' => array(
			'show_in_reg' => 0,
			'show_in_profile' => 0,
			'show_in_aeu' => 0,
			'show_in_blog' => 0,
			'show_in_search' => 0
		)
	);

	switch ($command) {
		case 'new_options':
			$options['version'] = $cimy_uef_version;
			
			cimy_set_options($options);
			break;

		case 'default_options':
			$old_options = cimy_get_options();
			
			if (isset($old_options['version']))
				$options['version'] = $old_options['version'];
			else
				$options['version'] = $cimy_uef_version;
			
			cimy_set_options($options);
			
			break;
			
		case 'drop_options':
			if (is_multisite())
				delete_site_option($cimy_uef_options);
			else
				delete_option($cimy_uef_options);
			
			break;
			
		case 'empty_wp_fields':
			if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_wp_fields_table'") == $wpdb_wp_fields_table) {
				$sql = "TRUNCATE TABLE ".$wpdb_wp_fields_table;
				$wpdb->query($sql);
			}
			break;

		case 'empty_extra_fields':
			if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_fields_table'") == $wpdb_fields_table) {
				$sql = "TRUNCATE TABLE ".$wpdb_fields_table;
				$wpdb->query($sql);
			}
			break;

		case 'empty_data':
			if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_data_table'") == $wpdb_data_table) {
				$sql = "TRUNCATE TABLE ".$wpdb_data_table;
				$wpdb->query($sql);
			}
			break;
			
		case 'drop_wp_fields':
			if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_wp_fields_table'") == $wpdb_wp_fields_table) {
				$sql = "DROP TABLE ".$wpdb_wp_fields_table;
				$wpdb->query($sql);
			}
			break;
			
		case 'drop_extra_fields':
			if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_fields_table'") == $wpdb_fields_table) {
				$sql = "DROP TABLE ".$wpdb_fields_table;
				$wpdb->query($sql);
			}
			break;

		case 'drop_data':
			if ($wpdb->get_var("SHOW TABLES LIKE '$wpdb_data_table'") == $wpdb_data_table) {
				$sql = "DROP TABLE ".$wpdb_data_table;
				$wpdb->query($sql);
			}
			break;
			
	}
}

// function to delete all files/subdirs in a path
// taken from PHP unlink's documentation comment by torch - torchsdomain dot com @ 22-Nov-2006 09:27
// modified by Marco Cimmino to delete correctly call recursion before so can also delete subdirs when empty
if (!function_exists("cimy_rfr")) {
	function cimy_rfr($path, $match) {
		static $deld = 0, $dsize = 0;

		// remember that glob returns FALSE in case of error
		$dirs = glob($path."*");
		$files = glob($path.$match);

		// call recursion before so we delete files in subdirs first!
		if (is_array($dirs)) {
			foreach ($dirs as $dir) {
				if (is_dir($dir)) {
					$dir = basename($dir) . "/";
					cimy_rfr($path.$dir, $match);
				}
			}
		}

		if (is_array($files)) {
			foreach ($files as $file) {
				if (is_file($file)) {
					$dsize += filesize($file);
					unlink($file);
					$deld++;
				}
				else if (is_dir($file)) {
					rmdir($file);
				}
			}
		}

		return "$deld files deleted with a total size of $dsize bytes";
	}
}

function cimy_delete_blog_info($blog_id, $drop) {
	global $cuef_upload_path;

	$file_path = $cuef_upload_path.$blog_id."/";
	
	// delete all uploaded files for that users
	cimy_rfr($file_path, "*");
	
	// delete also the subdir
	if (is_dir($file_path))
		rmdir($file_path);

	// in this case no need to delete anything, per blog tables are not created
	if (cimy_uef_is_multisite_unique_installation())
		$drop = false;

	if ($drop) {
		cimy_manage_db("drop_wp_fields");
		cimy_manage_db("drop_extra_fields");
		cimy_manage_db("drop_data");
	}
}

function cimy_delete_users_info($fields_id) {
	global $wpdb, $wpdb_data_table;
	
	if (!cimy_check_admin('edit_users'))
		return;
	
	$sql = "DELETE FROM ".$wpdb_data_table." WHERE ".$fields_id;
	$wpdb->query($sql);
}

function cimy_delete_user_info($user_id) {
	global $wpdb, $wpdb_data_table, $cuef_upload_path;
	
	if (!current_user_can('edit_user', $user_id))
		return;
	
	$sql = "DELETE FROM ".$wpdb_data_table." WHERE USER_ID=".$user_id;
	$wpdb->query($sql);
	
	$profileuser = get_user_to_edit($user_id);
	$user_login = $profileuser->user_login;
	
	$file_path = $cuef_upload_path.$user_login."/";
	
	// delete all uploaded files for that users
	cimy_rfr($file_path, "*");
	
	// delete also the subdir
	if (is_dir($file_path))
		rmdir($file_path);
}

function cimy_insert_ExtraFields_if_not_exist($user_id, $field_id) {
	global $wpdb, $wpdb_data_table;

	$sql = "SELECT ID FROM ".$wpdb_data_table." WHERE FIELD_ID=".$field_id." AND USER_ID=".$user_id;
	$exist = $wpdb->get_var($sql);

	if ($exist == NULL) {
		$sql = "INSERT INTO ".$wpdb_data_table." SET FIELD_ID=".$field_id.", USER_ID=".$user_id.", VALUE=''";
		$wpdb->query($sql);
	}
}

function cimy_get_options() {
	global $cimy_uef_options;

	if (cimy_uef_is_multisite_unique_installation())
		$options = get_site_option($cimy_uef_options);
	else
		$options = get_option($cimy_uef_options);

	return $options;
}

function cimy_set_options($options) {
	global $cimy_uef_options, $cimy_uef_options_descr;

	if (cimy_uef_is_multisite_unique_installation())
		update_site_option($cimy_uef_options, $options);
	else
		update_option($cimy_uef_options, $options, $cimy_uef_options_descr, "no");
}

function cimy_uef_get_meta_from_user_login($user_login) {
	global $wpdb;

	return $wpdb->get_row($wpdb->prepare("SELECT user_login, user_email, meta FROM ".$wpdb->prefix."signups WHERE user_login = %s AND active = %d", $user_login, 0), ARRAY_A);
}

function cimy_uef_get_meta_from_url($domain, $path) {
	global $wpdb;

	return $wpdb->get_row($wpdb->prepare("SELECT user_login, user_email, meta FROM ".$wpdb->prefix."signups WHERE domain = %s AND path = %s AND active = %d", $domain, $path, 0), ARRAY_A);
}
