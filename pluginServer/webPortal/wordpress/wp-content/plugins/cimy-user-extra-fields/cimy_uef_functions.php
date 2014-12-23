<?php

function get_cimyFields($wp_fields=false, $order_by_section=false) {
	global $wpdb_fields_table, $wpdb_wp_fields_table, $wpdb;

	if ($wp_fields)
		$table = $wpdb_wp_fields_table;
	else
		$table = $wpdb_fields_table;

	// only extra fields can be order by fieldset
	if (($order_by_section) && (!$wp_fields))
		$order = " ORDER BY FIELDSET, F_ORDER";
	else
		$order = " ORDER BY F_ORDER";

	// if tables exist then read all fields else array empty, will be read after the creation
	if ($wpdb->get_var("SHOW TABLES LIKE '".$table."'") == $table) {
		$sql = "SELECT * FROM ".$table.$order;
		$extra_fields = $wpdb->get_results($sql, ARRAY_A);
	
		if (!isset($extra_fields))
			$extra_fields = array();
		else {
			for ($i = 0; $i < count($extra_fields); $i++) {
				$extra_fields[$i]['RULES'] = unserialize($extra_fields[$i]['RULES']);
			}
			
			$extra_fields = $extra_fields;
		}
	}
	else
		$extra_fields = array();

	return $extra_fields;
}

function set_cimyFieldValue($user_id, $field_name, $field_value) {
	global $wpdb, $wpdb_data_table, $wpdb_fields_table;

	$users = array();
	$results = array();
	$radio_ids = array();

	if (empty($field_name))
		return $results;

	$field_name = $wpdb->escape(strtoupper($field_name));

	$sql = "SELECT ID, TYPE, LABEL FROM $wpdb_fields_table WHERE NAME='$field_name'";
	$efields = $wpdb->get_results($sql, ARRAY_A);

	if ($efields == NULL)
		return $results;

	$type = $efields[0]['TYPE'];

	if ($type == "radio") {
		foreach ($efields as $ef) {
			if ($ef['LABEL'] == $field_value) {
				$field_value = "selected";
				$field_id = $ef['ID'];
			}
			else
				$radio_ids[] = $ef['ID'];
		}

		// if there are no radio fields with that label abort
		if ($field_value != "selected")
			return $results;
	}
	else if ($type == "checkbox") {
		if (($field_value) || ($field_value == "YES"))
			$field_value = "YES";
		else
			$field_value = "NO";

		$field_id = $efields[0]['ID'];
	}
	else
		$field_id = $efields[0]['ID'];

	if ($user_id) {
		$user_id = intval($user_id);
		$user_info = get_userdata($user_id);
		if (!$user_info)
			return $results;
	}
	else {
		$sql = "SELECT ID FROM $wpdb->users";
		$users = $wpdb->get_results($sql, ARRAY_A);
	}

	if (empty($users))
		$users[]["ID"] = $user_id;

	$field_value = $wpdb->escape($field_value);

	foreach ($users as $user) {
		if (!current_user_can('edit_user', $user["ID"]))
			continue;

		$sql = "SELECT ID FROM $wpdb_data_table WHERE FIELD_ID=$field_id AND USER_ID=".$user["ID"];
		$exist = $wpdb->get_var($sql);

		if ($exist == NULL)
			$sql = "INSERT INTO $wpdb_data_table SET USER_ID=".$user["ID"].", VALUE='$field_value', FIELD_ID=$field_id";
		else
			$sql = "UPDATE $wpdb_data_table SET VALUE='$field_value' WHERE FIELD_ID=$field_id AND USER_ID=".$user["ID"];


		$add_field_result = $wpdb->query($sql);

		if ($add_field_result > 0)
			$results[]["USER_ID"] = $user["ID"];

		if ($type == "radio") {
			if (!empty($radio_ids)) {
				foreach ($radio_ids as $r_id) {
					$sql2 = "UPDATE $wpdb_data_table SET VALUE='' WHERE FIELD_ID=$r_id AND USER_ID=".$user["ID"];
					$result_sql2 = $wpdb->query($sql2);
				}
			}
		}
	}

	return $results;
}

function get_cimyFieldValue($user_id, $field_name, $field_value=false) {
	global $wpdb, $wpdb_data_table, $wpdb_fields_table;
	
	$sql_field_value = "";

	if ((!isset($user_id)) || (!isset($field_name)))
		return NULL;
	
	if ($field_name) {
		$field_name = strtoupper($field_name);
		$field_name = $wpdb->escape($field_name);
	}
	
	if ($field_value) {
		if (is_array($field_value)) {
			if (isset($field_value['value'])) {
				$sql_field_value = $wpdb->escape($field_value['value']);
				
				if ($field_value['like'])
					$sql_field_value = " AND data.VALUE LIKE '%".$sql_field_value."%'";
				else
					$sql_field_value = " AND data.VALUE='".$sql_field_value."'";
			}
		} else {
		
			$field_value = $wpdb->escape($field_value);
			$sql_field_value = " AND data.VALUE='".$field_value."'";
		}
	}

	if ($user_id) {
		$user_id = intval($user_id);
		
		if (!$user_id)
			return NULL;
	}
	
	// FIELD_NAME and USER_ID provided
	if (($field_name) && ($user_id)) {
		/*
			$sql will be:
		
			SELECT	efields.LABEL,
				efields.TYPE,
				data.VALUE
		
			FROM 	<uef data table> as data
		
			JOIN	<uef fields table> as efields
		
			ON	efields.id=data.field_id
		
			WHERE	efields.name=<field_name>
				AND data.USER_ID=<user_id>
				AND efields.TYPE!='password'
				AND (efields.TYPE!='radio' OR data.VALUE!='')
				[AND data.VALUE=<field_value>]
		*/
		$sql = "SELECT efields.LABEL, efields.TYPE, data.VALUE FROM ".$wpdb_data_table." as data JOIN ".$wpdb_fields_table." as efields ON efields.id=data.field_id WHERE efields.name='".$field_name."' AND data.USER_ID=".$user_id." AND efields.TYPE!='password' AND (efields.TYPE!='radio' OR data.VALUE!='')".$sql_field_value;
	}
	
	// only USER_ID provided
	if ((!$field_name) && ($user_id)) {
		/*
			$sql will be:
		
			SELECT	efields.LABEL,
				efields.TYPE,
				efields.NAME,
				data.VALUE
		
			FROM 	<uef data table> as data
		
			JOIN	<uef fields table> as efields
		
			ON	efields.id=data.field_id
		
			WHERE	AND data.USER_ID=<user_id>
				AND efields.TYPE!='password'
				AND (efields.TYPE!='radio' OR data.VALUE!='')
				[AND data.VALUE=<field_value>]
		
			ORDER BY efields.F_ORDER
		*/
		$sql = "SELECT efields.LABEL, efields.TYPE, efields.NAME, data.VALUE FROM ".$wpdb_data_table." as data JOIN ".$wpdb_fields_table." as efields ON efields.id=data.field_id WHERE data.USER_ID=".$user_id." AND efields.TYPE!='password' AND (efields.TYPE!='radio' OR data.VALUE!='')".$sql_field_value." ORDER BY efields.F_ORDER";
	}
	
	// only FIELD_NAME provided
	if (($field_name) && (!$user_id)) {
		/*
			$sql will be:
		
			SELECT	efields.LABEL,
				efields.TYPE,
				users.ID as user_id,
				users.user_login,
				data.VALUE
		
			FROM 	<wp users table> as users,
				<uef data table> as data
		
			JOIN	<uef fields table> as efields
		
			ON	efields.id=data.field_id
		
			WHERE	efields.name=<field_name>
				AND data.USER_ID=users.ID
				AND efields.TYPE!='password'
				AND (efields.TYPE!='radio' OR data.VALUE!='')
				[AND data.VALUE=<field_value>]
		
			ORDER BY users.user_login
		*/
		$sql = "SELECT efields.LABEL, efields.TYPE, users.ID as user_id, users.user_login, data.VALUE FROM ".$wpdb->users." as users, ".$wpdb_data_table." as data JOIN ".$wpdb_fields_table." as efields ON efields.id=data.field_id WHERE efields.name='".$field_name."' AND users.ID=data.USER_ID AND efields.TYPE!='password' AND (efields.TYPE!='radio' OR data.VALUE!='')".$sql_field_value." ORDER BY users.user_login";
	}
	
	// nothing provided
	if ((!$field_name) && (!$user_id)) {
		/*
			$sql will be:
		
			SELECT	users.ID as user_id,
				users.user_login,
				efields.NAME,
				efields.LABEL,
				efields.TYPE,
				data.VALUE
		
			FROM 	<wp users table> as users,
				<uef data table> as data
		
			JOIN	<uef fields table> as efields
		
			ON	efields.id=data.field_id
		
			WHERE	data.USER_ID=users.ID
				AND efields.TYPE!='password'
				AND (efields.TYPE!='radio' OR data.VALUE!='')
				[AND data.VALUE=<field_value>]
		
			ORDER BY users.user_login,
				efields.F_ORDER
		*/
		$sql = "SELECT users.ID as user_id, users.user_login, efields.NAME, efields.LABEL, efields.TYPE, data.VALUE FROM ".$wpdb->users." as users, ".$wpdb_data_table." as data JOIN ".$wpdb_fields_table." as efields ON efields.id=data.field_id WHERE users.ID=data.USER_ID AND efields.TYPE!='password' AND (efields.TYPE!='radio' OR data.VALUE!='')".$sql_field_value." ORDER BY users.user_login, efields.F_ORDER";
	}

	$field_data = $wpdb->get_results($sql, ARRAY_A);
	
	if (isset($field_data)) {
		if ($field_data != NULL)
			$field_data = $field_data;
	}
	else
		return NULL;

	$field_data = cimy_change_radio_labels($field_data, $user_id);

	if (($field_name) && ($user_id)) {
		if (isset($field_data[0]['VALUE']))
			$field_data = $field_data[0]['VALUE'];
		else
			$field_data = "";
	}

	return $field_data;
}

function cimy_change_radio_labels($field_data, $user_id) {
	$i = 0;

	while ($i < count($field_data)) {
		if ($field_data[$i]['TYPE'] == "radio") {
			$field_data[$i]['VALUE'] = $field_data[$i]['LABEL'];
		}
		else if (($field_data[$i]['TYPE'] == "dropdown") || ($field_data[$i]['TYPE'] == "dropdown-multi")) {
			$ret = cimy_dropDownOptions($field_data[$i]['LABEL'], false);
			
			$field_data[$i]['LABEL'] = $ret['label'];
		}
		else if ($field_data[$i]['TYPE'] == "registration-date") {
			if (isset($field_data[$i]['user_id']))
				$field_data[$i]['VALUE'] = cimy_get_registration_date($field_data[$i]['user_id'], $field_data[$i]['VALUE']);
			else
				$field_data[$i]['VALUE'] = cimy_get_registration_date($user_id, $field_data[$i]['VALUE']);
		}

		$i++;
	}
	
	return $field_data;
}

function cimy_get_formatted_date($value, $date_format="%d %B %Y @%H:%M") {
	$locale = get_locale();

	if (stristr($locale, ".") === false)
		$locale2 = $locale.".utf8";
	else
		$locale2 = "";

	setlocale(LC_TIME, $locale, $locale2);

	if (($value == "") || (!isset($value)))
		$registration_date = "";
	else
		$registration_date = strftime($date_format, intval($value));

	return $registration_date;
}

function cimy_dropDownOptions($values, $selected) {
	
	$label_pos = strpos($values, "/");
	
	if ($label_pos) {
		$label = substr($values, 0, $label_pos);
		$values = substr($values, $label_pos + 1);
	}
	else
		$label = "";
	
	$items = explode(",", $values);
	$sel_items = explode(",", $selected);
	$html_options = "";
	$sel_i = 0;

	foreach ($items as $item) {
		$item_clean = trim($item, "\t\n\r");

		$html_options.= "\n\t\t\t";
		$html_options.= '<option value="'.esc_attr($item_clean).'"';

		if (isset($sel_items[$sel_i])) {
			$is_selected = selected($item_clean, $sel_items[$sel_i], false);
			if (!empty($is_selected)) {
				$sel_i++;
				$html_options.= $is_selected;
			}
		}

		$html_options.= ">".esc_html($item_clean)."</option>";
	}

	$ret = array();
	$ret['html'] = $html_options;
	$ret['label'] = cimy_uef_sanitize_content($label);
	
	return $ret;
}

function cimy_get_thumb_path($file_path, $oldname=false) {
	$file_path_purename = substr($file_path, 0, strrpos($file_path, "."));
	$file_path_ext = strtolower(substr($file_path, strlen($file_path_purename)));
	
	if ($oldname)
		$file_thumb_path = $file_path_purename.".thumbnail".$file_path_ext;
	else
		$file_thumb_path = $file_path_purename."-thumbnail".$file_path_ext;
	
	return $file_thumb_path;
}

function cimy_uef_sanitize_content($content, $override_allowed_tags=null) {
	global $allowedtags;

	if (is_array($override_allowed_tags))
		$cimy_allowedtags = $override_allowed_tags;
	else {
		$cimy_allowedtags = $allowedtags;
		$cimy_allowedtags['a']['target'] = array();
	}

	$content = wp_kses($content, $cimy_allowedtags);
	$content = wptexturize($content);

	return $content;
}

function cimy_check_admin($permission) {
	if (cimy_uef_is_multisite_unique_installation())
		return is_super_admin();
	else
		return current_user_can($permission);
	
	return false;
}

function cimy_fieldsetOptions($selected=0, $order="", $select_all=false) {
	global $cimy_uef_domain;

	if (!cimy_check_admin('manage_options'))
		return;

	$options = cimy_get_options();

	$i = 0;
	$html = "<select name=\"fieldset[".$order."]\">\n";

	if (empty($options['fieldset_title']) && !$select_all) {
		$html.= "\t<option value=\"$i\" selected=\"selected\">".__("no fieldset", $cimy_uef_domain)."</option>\n";
	}
	else {
		if ($select_all)
			$html.= "\t<option value=\"-1\"".selected(-1, $selected, false).">".__("All")."</option>\n";

		if (!empty($options['fieldset_title'])) {
			$fieldset_titles = explode(',', $options['fieldset_title']);

			foreach ($fieldset_titles as $fieldset) {
				$html.= "\t<option value=\"$i\"".selected($i, $selected, false).">".esc_html($fieldset)."</option>\n";
				$i++;
			}
		}
	}

	$html.= "</select>";

	return $html;
}

function cimy_switch_to_blog($meta=array()) {
	global $cimy_uef_plugins_dir;

	if ((is_multisite()) && ($cimy_uef_plugins_dir == "plugins")) {
		if (isset($meta["blog_id"]))
			$mu_blog_id = intval($meta["blog_id"]);
		else if (isset($_GET["blog_id"]))
			$mu_blog_id = intval($_GET["blog_id"]);
		else if (isset($_POST["blog_id"]))
			$mu_blog_id = intval($_POST["blog_id"]);
		// needed because WordPress 3.5+ MS doesn't like to redirect to wp-signup.php using 'blog_id' parameter
		if (isset($meta["from_blog_id"]))
			$mu_blog_id = intval($meta["from_blog_id"]);
		else if (isset($_GET["from_blog_id"]))
			$mu_blog_id = intval($_GET["from_blog_id"]);
		else if (isset($_POST["from_blog_id"]))
			$mu_blog_id = intval($_POST["from_blog_id"]);
		else
			$mu_blog_id = 1;

		if (cimy_uef_mu_blog_exists($mu_blog_id)) {
			if (!switch_to_blog($mu_blog_id))
				$mu_blog_id = 1;
		}
		else
			$mu_blog_id = 1;
	}
}

function cimy_uef_blog_switched($new_blog_id, $prev_blog_id) {
	cimy_uef_set_tables();
}

function cimy_is_at_least_wordpress35() {
	return version_compare(get_bloginfo('version'), '3.5') >= 0;
}

function cimy_switch_current_blog($hidden_field=false) {
	global $switched, $blog_id;

	if (isset($switched)) {
		if ($hidden_field) {
			if (cimy_is_at_least_wordpress35()) {
				echo "\t<input type=\"hidden\" name=\"from_blog_id\" value=\"".$blog_id."\" />\n";
			}
			else {
				echo "\t<input type=\"hidden\" name=\"blog_id\" value=\"".$blog_id."\" />\n";
			}
			
		}

		if (cimy_is_at_least_wordpress35())
			restore_current_blog();
	}
}

function cimy_get_registration_date($user_id, $value) {
	if (!empty($value))
		return $value;

	$author_meta = get_userdata(intval($user_id));
	$author_registered = $author_meta->user_registered;
	if (!empty($author_registered))
		if (($date = strtotime($author_registered)) !== FALSE);
			return $date;

	return "";
}

function cimy_uef_is_field_disabled($type, $edit_rule, $old_value) {
	global $rule_cannot_be_empty;

	switch($edit_rule)
	{
		case 'no_edit':
			return true;
			break;

		case 'edit_only_if_empty':
			if ((in_array($type, $rule_cannot_be_empty)) && (!empty($old_value)))
				return true;
			break;

		case 'edit_only_by_admin':
			if (!current_user_can('edit_users'))
				return true;
			break;

		case 'edit_only_by_admin_or_if_empty':
			if ((!current_user_can('edit_users')) && (!((in_array($type, $rule_cannot_be_empty)) && (empty($old_value)))))
				return true;
			break;
	}

	// field is enabled
	return false;
}

function cimy_uef_crop_image($file, $field_id_data) {
	if (!empty($_POST[$field_id_data."_button"]) && (!empty($_POST[$field_id_data.'_w'])) && (!empty($_POST[$field_id_data.'_h']))) {
		$targ_w = $_POST[$field_id_data.'_w'];
		$targ_h = $_POST[$field_id_data.'_h'];
		$jpeg_quality = 100;

		$src = $file;
		$dst = $file;
		$size = getimagesize($src);
		switch ($size["mime"]) {
			case "image/jpeg":
				$img_r = imagecreatefromjpeg($src); //jpeg file
				break;
			case "image/gif":
				$img_r = imagecreatefromgif($src); //gif file
				break;
			case "image/png":
				$img_r = imagecreatefrompng($src); //png file
				break;
			default:
				$img_r = false;
		}
		if (!empty($img_r)) {
			$dst_r = ImageCreateTrueColor($targ_w, $targ_h);
			imagecopyresampled($dst_r, $img_r, 0, 0, $_POST[$field_id_data.'_x1'],$_POST[$field_id_data.'_y1'], $targ_w, $targ_h, $targ_w, $targ_h);
			switch ($size["mime"]) {
				case "image/jpeg":
					imagejpeg($dst_r, $dst, $jpeg_quality); //jpeg file
					break;
				case "image/gif":
					imagegif($dst_r, $dst); //gif file
					break;
				case "image/png":
					imagepng($dst_r, $dst); //png file
					break;
			}
		}
	}
}

function cimy_uef_parse_advanced_options($options) {
	$advanced_options = array();
	$adv_array = explode(",", $options);
	foreach ($adv_array as $item) {
		$tmp_array = explode("=", $item);
		if (count($tmp_array) < 2)
			continue;
		if (strtolower($tmp_array[0]) == "filename")
			$advanced_options["filename"] = $tmp_array[1];
		else if (strtolower($tmp_array[0]) == "crop_ratio")
			$advanced_options["crop_ratio"] = $tmp_array[1];
		else if (strtolower($tmp_array[0]) == "crop_x1")
			$advanced_options["crop_x1"] = $tmp_array[1];
		else if (strtolower($tmp_array[0]) == "crop_y1")
			$advanced_options["crop_y1"] = $tmp_array[1];
		else if (strtolower($tmp_array[0]) == "crop_x2")
			$advanced_options["crop_x2"] = $tmp_array[1];
		else if (strtolower($tmp_array[0]) == "crop_y2")
			$advanced_options["crop_y2"] = $tmp_array[1];
		else if (strtolower($tmp_array[0]) == "no-thumb")
			$advanced_options["no-thumb"] = $tmp_array[1];
	}
	return $advanced_options;
}

function cimy_uef_get_dir_or_filename($user_login, $url="", $is_thumbnail=false) {
	global $cimy_uef_plugins_dir, $cuef_upload_path;

	$blog_path = $cuef_upload_path;
	if (($cimy_uef_plugins_dir == "plugins") && (is_multisite())) {
		global $blog_id;

		$blog_path .= $blog_id."/";
	}

	if (empty($url))
		return $blog_path.$user_login;
	else if ($is_thumbnail)
		return $blog_path.$user_login."/".cimy_get_thumb_path(basename($url));
	else
		return $blog_path.$user_login."/".basename($url);
}

function cimy_uef_set_javascript_dependencies($javascripts_dep, $type, $rule_name, $rule) {
	switch ($type) {
		case "avatar":
		case "picture":
			if ($rule)
				$javascripts_dep['image_fields'][$rule_name] += 1;
			// no break we want to count as a file too!
		case "file":
			if ($rule)
				$javascripts_dep['file_fields'][$rule_name] += 1;
			break;
		case "textarea-rich":
			if ($rule)
				$javascripts_dep['tinymce_fields'][$rule_name] += 1;
			break;
		default:
			break;
	}
	return $javascripts_dep;
}

function cimy_uef_avatar_filter($avatar, $id_or_email, $size, $default, $alt="") {
	global $wpdb, $wpdb_data_table, $wpdb_fields_table, $cuef_upload_path;

	$sql = "SELECT ID,VALUE FROM $wpdb_fields_table WHERE TYPE='avatar' LIMIT 1";
	$res = $wpdb->get_results($sql);

	if (empty($res))
		return $avatar;

	$field_id = $res[0]->ID;
	$overwrite_default = $res[0]->VALUE;

	// if there is no avatar field all the rest is totally cpu time wasted, returning...
	if (!isset($field_id))
		return $avatar;

	if (!empty($overwrite_default))
		$overwrite_default = "<img alt='{$safe_alt}' src='{$overwrite_default}' class='avatar avatar-{$size} photo avatar-default' height='{$size}' width='{$size}' />";

	$email = '';
	$user_login = '';

	// $id_or_email could be id, email or an object... fancy way to implement things!
	// we may have the id
	if ( is_numeric($id_or_email) ) {
		$id = (int) $id_or_email;
		$user = get_userdata($id);
		if ( $user ) {
			$email = $user->user_email;
			$user_login = $user->user_login;
		}
	} elseif ( is_object($id_or_email) ) {
		// we may have the object...
		if ( isset($id_or_email->comment_type) && '' != $id_or_email->comment_type && 'comment' != $id_or_email->comment_type )
			return false; // No avatar for pingbacks or trackbacks, maybe useless as same check is performed before this code is fired...

		if ( !empty($id_or_email->user_id) ) {
			$id = (int) $id_or_email->user_id;
			$user = get_userdata($id);
			if ( $user) {
				$email = $user->user_email;
				$user_login = $user->user_login;
			}
		} else {
			// no user_id no custom avatar, nothing else to do
			return $avatar;
		}
	} else {
		// ...or we may have the email
		$email = $id_or_email;

		$sql = sprintf("SELECT ID, user_login FROM %s WHERE user_email='%s' LIMIT 1", $wpdb->users, $wpdb->escape($email));
		$res = $wpdb->get_results($sql);

		// something went wrong, aborting and returning normal avatar
		if (!isset($res))
			return $avatar;

		$id = $res[0]->ID;
		$user_login = $res[0]->user_login;
	}

	if (isset($id)) {
		$sql = "SELECT data.VALUE FROM $wpdb_data_table as data JOIN $wpdb_fields_table as efields ON efields.id=data.field_id WHERE (efields.TYPE='avatar' AND data.USER_ID=$id) LIMIT 1";

		$value = $wpdb->get_var($sql);

		if ( false === $alt)
			$safe_alt = '';
		else
			$safe_alt = esc_attr($alt);

		// max $size allowed is 512
		if (isset($value)) {
			if ($value == "") {
				// apply default only here or below, as we are sure to have an user that did not set anything
				if ($overwrite_default != "")
					return $overwrite_default;
				else
					return $avatar;
			}

			$thumb_value = cimy_get_thumb_path($value);
			$file_thumb = $cuef_upload_path.$user_login."/avatar/".cimy_get_thumb_path(basename($value));

			if (is_file($file_thumb))
				$value = $thumb_value;

			$avatar = "<img alt='{$safe_alt}' src='{$value}' class='avatar avatar-{$size} photo' height='{$size}' width='{$size}' />";
		}
		// apply default only here, as we are sure to have an user that did not set anything
		else if ($overwrite_default != "")
			return $overwrite_default;
	}

	return $avatar;
}

function cimy_manage_upload($input_name, $user_login, $rules, $old_file=false, $delete_file=false, $type="", $new_filename="") {
	global $cuef_upload_path, $cuef_upload_webpath, $cuef_plugin_dir, $cimy_uef_plugins_dir;

	$type_path = "";
	if (($type == "file") || ($type == "avatar"))
		$type_path.= $type."/";

	$blog_path = $cuef_upload_path;

	if (($cimy_uef_plugins_dir == "plugins") && (is_multisite())) {
		global $blog_id;

		$blog_path .= $blog_id."/";

		// create blog subdir
		if (!is_dir($blog_path)) {
			if (defined("FS_CHMOD_DIR")) {
				mkdir($blog_path, FS_CHMOD_DIR);
				chmod($blog_path, FS_CHMOD_DIR);
			}
			else {
				mkdir($blog_path, 0777);
				chmod($blog_path, 0777);
			}
		}
	}

	if (!empty($user_login)) {
		$user_path = $blog_path.$user_login."/";
		$file_path = $blog_path.$user_login."/".$type_path;
	}
	else {
		$user_path = $blog_path;
		$file_path = $blog_path.$type_path;
	}

	// delete old file if requested
	if ($delete_file) {
		if (is_file($file_path.$old_file))
			unlink($file_path.$old_file);
	
		$old_thumb_file = cimy_get_thumb_path($old_file);
		
		if (is_file($file_path.$old_thumb_file))
			unlink($file_path.$old_thumb_file);
	}

	// if $user_login is not present
	//	or there is no file to upload
	//	or dest dir is not writable
	// then everything else is useless
	if ((($user_login == "") && ($type != "registration-logo")) || (empty($_FILES[$input_name]['name'])) || (!is_writable($cuef_upload_path)))
		return "";

	// create user subdir
	if (!is_dir($user_path)) {
		if (defined("FS_CHMOD_DIR")) {
			mkdir($user_path, FS_CHMOD_DIR);
			chmod($user_path, FS_CHMOD_DIR);
		}
		else {
			mkdir($user_path, 0777);
			chmod($user_path, 0777);
		}
	}

	// create avatar subdir if needed
	if (($type != "registration-logo") && ($type != "picture") && (!is_dir($file_path))) {
		if (defined("FS_CHMOD_DIR")) {
			mkdir($file_path, FS_CHMOD_DIR);
			chmod($file_path, FS_CHMOD_DIR);
		}
		else {
			mkdir($file_path, 0777);
			chmod($file_path, 0777);
		}
	}

	if (!empty($new_filename))
		$file_name = $new_filename;
	else
		$file_name = $_FILES[$input_name]['name'];

	// filesize in Byte transformed in KiloByte
	$file_size = $_FILES[$input_name]['size'] / 1024;
	$file_type = $_FILES[$input_name]['type'];
	$file_tmp_name = $_FILES[$input_name]['tmp_name'];
	$file_error = $_FILES[$input_name]['error'];

	$allowed_mime_types = get_allowed_mime_types();
	// let's see if the image extension is correct, bad boy
	$validate = wp_check_filetype_and_ext($file_tmp_name, $file_name, $allowed_mime_types);
	if ($validate['proper_filename'] !== false)
		$file_name = $validate['proper_filename'];

	// sanitize the file name
	$file_name = wp_unique_filename($file_path, $file_name);
	// file path
	$file_full_path = $file_path.$file_name;

	// picture url to write in the DB
	$data = $cuef_upload_webpath;

	if (($cimy_uef_plugins_dir == "plugins") && (is_multisite()))
		$data.= $blog_id."/";

	if (empty($user_login))
		$data .= $type_path.$file_name;
	else
		$data .= $user_login."/".$type_path.$file_name;

	// CHECK IF IT IS A REAL PICTURE
	if (($type != "file") && (stristr($file_type, "image/") === false))
		$file_error = 1;
	
	// MIN LENGTH
	if (isset($rules['min_length']))
		if ($file_size < (intval($rules['min_length'])))
			$file_error = 1;
	
	// EXACT LENGTH
	if (isset($rules['exact_length']))
		if ($file_size != (intval($rules['exact_length'])))
			$file_error = 1;

	// MAX LENGTH
	if (isset($rules['max_length']))
		if ($file_size > (intval($rules['max_length'])))
			$file_error = 1;

	// if there are no errors and filename is NOT empty
	if (($file_error == 0) && (!empty($file_name))) {
		if (move_uploaded_file($file_tmp_name, $file_full_path)) {
			// change file permissions for broken servers
			if (defined("FS_CHMOD_FILE"))
				@chmod($file_full_path, FS_CHMOD_FILE);
			else
				@chmod($file_full_path, 0644);
			
			// if there is an old file to delete
			if ($old_file) {
				// delete old file if the name is different, if equal NOPE because new file is already uploaded
				if ($file_name != $old_file)
					if (is_file($file_path.$old_file))
						unlink($file_path.$old_file);
				
				$old_thumb_file = cimy_get_thumb_path($old_file);
				
				if (is_file($file_path.$old_thumb_file))
					unlink($file_path.$old_thumb_file);
			}
			
			// should be stay AFTER DELETIONS
			if ((isset($rules['equal_to'])) && ($type != "file")) {
				if ($maxside = intval($rules['equal_to'])) {
					if (!function_exists("image_resize"))
						require_once(ABSPATH . 'wp-includes/media.php');

					if (!function_exists("wp_load_image"))
						require_once($cuef_plugin_dir.'/cimy_uef_missing_functions.php');

					image_resize($file_full_path, $maxside, $maxside, false, "thumbnail");
				}
			}
		}
		else
			$data = "";
	}
	else
		$data = "";

	return $data;
}

function cimy_uef_get_allowed_image_extensions() {
	$all_ext = get_allowed_mime_types();
	$image_ext = array();
	if (empty($all_ext))
		return $image_ext;
	foreach ($all_ext as $key=>$value)
		if (stristr($value, "image/") !== false)
			$image_ext = array_merge($image_ext, explode('|', $key));
	return $image_ext;
}

// http://wpml.org/documentation/support/translation-for-texts-by-other-plugins-and-themes/
function cimy_wpml_register_string($name, $value) {
	global $cimy_uef_name;
	if (function_exists('icl_register_string'))
		icl_register_string($cimy_uef_name, $name, $value);
}

function cimy_wpml_translate_string($name, $value) {
	global $cimy_uef_name;
	if (function_exists('icl_t'))
		return icl_t($cimy_uef_name, $name, $value);
	return $value;
}

function cimy_wpml_unregister_string($name) {
	global $cimy_uef_name;
	if (function_exists('icl_unregister_string'))
		icl_unregister_string($cimy_uef_name, $name);
}

/**
 * @since 2.5.2
 * @return true on WordPress registration page
 */
function cimy_uef_is_register_page() {
	if (cimy_uef_is_theme_my_login_register_page())
		return true;
	$script_file = end(explode('/', $_SERVER['SCRIPT_NAME']));
	if (!is_multisite() && stripos($script_file, "wp-login.php") !== false && !empty($_GET['action']) && $_GET['action'] == 'register')
		return true;
	else if (is_multisite() && stripos($script_file, "wp-signup.php") !== false)
		return true;
	return false;
}

/**
 * @since 2.5.2
 * @return true on Themed My Login - Themed Registration page
 */
function cimy_uef_is_theme_my_login_register_page() {
	// Theme My Login <= v6.2.x
	if (!empty($GLOBALS['theme_my_login']) && $GLOBALS['theme_my_login']->is_login_page())
		return true;
	// Theme My Login >= v6.3.0
	if (function_exists('Theme_My_Login') && Theme_My_Login::is_tml_page('register'))
		return true;
	return false;
}

/**
 * @since 2.5.2
 * @return true on Themed My Login - Themed Profiles pages
 */
function cimy_uef_is_theme_my_login_profile_page() {
	if (!empty($GLOBALS['theme_my_login']) || function_exists('Theme_My_Login'))
		return defined('IS_PROFILE_PAGE') && constant('IS_PROFILE_PAGE');
	return false;
}

function cimy_uef_is_multisite_unique_installation() {
	global $cimy_uef_plugins_dir;
	return is_multisite() && $cimy_uef_plugins_dir == "mu-plugins";
}

function cimy_uef_is_multisite_per_blog_installation() {
	global $cimy_uef_plugins_dir;
	return is_multisite() && $cimy_uef_plugins_dir != "mu-plugins";
}
