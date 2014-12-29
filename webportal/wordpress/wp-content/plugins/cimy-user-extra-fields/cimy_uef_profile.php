<?php

function cimy_extract_ExtraFields() {
	global $wpdb, $user_ID, $wpdb_data_table, $start_cimy_uef_comment, $end_cimy_uef_comment, $rule_profile_value, $cimy_uef_options, $rule_maxlen_needed, $fields_name_prefix, $cuef_upload_path, $cimy_uef_domain, $cuef_plugin_dir, $cimy_uef_file_types, $cimy_uef_textarea_types, $user_level;

	// if editing a different user (only admin)
	if (isset($_GET['user_id'])) {
		$get_user_id = $_GET['user_id'];

		if (!current_user_can('edit_user', $get_user_id))
			return;
	}
	else if (isset($_POST['user_id'])) {
		$get_user_id = $_POST['user_id'];

		if (!current_user_can('edit_user', $get_user_id))
			return;
	}
	// editing own profile
	else {
		if (!isset($user_ID))
			return;

		$get_user_id = $user_ID;
	}

	$get_user_id = intval($get_user_id);
	$options = cimy_get_options();

	$extra_fields = get_cimyFields(false, true);
	if (cimy_uef_is_multisite_per_blog_installation())
		echo "<input type=\"hidden\" name=\"from_blog_id\" value=\"".strval(get_current_blog_id())."\" />\n";

	if (!empty($extra_fields)) {
		$upload_image_function = false;

		echo $start_cimy_uef_comment;

		if ($options['extra_fields_title'] != "") {
			echo "<br clear=\"all\" />\n";
			echo "<h2>".esc_html(cimy_wpml_translate_string("a_opt_extra_fields_title", $options['extra_fields_title']))."</h2>\n";
		}

		foreach ($extra_fields as $thisField) {

			$field_id = $thisField['ID'];

			cimy_insert_ExtraFields_if_not_exist($get_user_id, $field_id);
		}

// 		$ef_db = $wpdb->get_results("SELECT FIELD_ID, VALUE FROM ".$wpdb_data_table." WHERE USER_ID = ".$get_user_id, ARRAY_A);

		$radio_checked = array();
		$current_fieldset = -1;
		$tiny_mce_objects = "";

		if (!empty($options['fieldset_title']))
			$fieldset_titles = explode(',', $options['fieldset_title']);
		else
			$fieldset_titles = array();

		$close_table = false;

		echo '<table class="form-table">';
		echo "\n";

		foreach ($extra_fields as $thisField) {
			$value = "";
			$old_value = "";
			$field_id = $thisField['ID'];
			$name = $thisField['NAME'];
			$rules = $thisField['RULES'];
			$type = $thisField['TYPE'];
			$label = cimy_wpml_translate_string($name."_label", $thisField["LABEL"]);
			$description = cimy_uef_sanitize_content(cimy_wpml_translate_string($name."_desc", $thisField["DESCRIPTION"]));
			$fieldset = $thisField['FIELDSET'];
			$unique_id = $fields_name_prefix.$field_id;
			$input_name = $fields_name_prefix.esc_attr($name);
			$field_id_data = $input_name."_".$field_id."_data";
			$advanced_options = cimy_uef_parse_advanced_options($rules["advanced_options"]);

			// if the current user LOGGED IN has not enough permissions to see the field, skip it
			// apply only for EXTRA FIELDS
			if ($rules['show_level'] == 'view_cimy_extra_fields')
			{
				if (!current_user_can($rules['show_level']))
					continue;
			}
			else if ($user_level < $rules['show_level'])
				continue;

			// if show_level == anonymous then do NOT ovverride other show_xyz rules
			if ($rules['show_level'] == -1) {
				// if flag to show the field in the profile is NOT activated, skip it
				if (!$rules['show_in_profile'])
					continue;
			}

// 			foreach ($ef_db as $d_field) {
// 				if ($d_field['FIELD_ID'] == $field_id)
// 					$value = $d_field['VALUE'];
// 			}
			$value = $wpdb->get_var($wpdb->prepare("SELECT VALUE FROM ".$wpdb_data_table." WHERE USER_ID=%d AND FIELD_ID=%d", $get_user_id, $field_id));
			$old_value = $value;

			if (($type == "radio") && (empty($radio_checked[$name])))
				$radio_checked[$name] = $wpdb->get_var($wpdb->prepare("SELECT COUNT(*) FROM ".$wpdb_data_table." WHERE USER_ID=%d AND FIELD_ID=%d AND VALUE=\"selected\"", $get_user_id, $field_id));

			// if nothing is inserted and field admin default value then assign it
			if (in_array($type, $rule_profile_value)) {
				if (empty($value))
					$value = $thisField['VALUE'];
			}

			if (($fieldset > $current_fieldset) && (isset($fieldset_titles[$fieldset]))) {
				$current_fieldset = $fieldset;

				// do not close the table if it is the first iteration
				if ($close_table)
					echo "</table>\n";
				else
					$close_table = true;

				if (isset($fieldset_titles[$current_fieldset]))
					echo "\n\t<h3>".esc_html(cimy_wpml_translate_string("a_opt_fieldset_title_".$current_fieldset, $fieldset_titles[$current_fieldset]))."</h3>\n";

				echo '<table class="form-table">';
				echo "\n";
			}

			echo "\t";
			echo "<tr>";
			echo "\n\t";

			// if you use it you need to escape it!
			$non_escaped_value = $value;
			$value = esc_attr($value);
			$old_value = esc_attr($old_value);
			$obj_class = '';
			if ($rules['can_be_empty'])
				$required = '' ;
			else
				$required = ' <span class="description">'.__("(required)").'</span>' ;

			switch($type) {
				case "picture-url":
				case "password":
				case "text":
					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).$required.'</label>';
					$obj_name = ' name="'.$input_name.'"';

					if ($type == "picture-url")
						$obj_type = ' type="text"';
					else
						$obj_type = ' type="'.$type.'"';

					$obj_value = ' value="'.$value.'"';
					$obj_value2 = "";
					$obj_checked = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
					$obj_style = ' class="regular-text"';

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";

					break;

				case "textarea":
					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).$required.'</label>';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = "";
					$obj_value = "";
					$obj_value2 = $value;
					$obj_checked = "";
					$obj_tag = "textarea";
					$obj_closing_tag = true;
					$obj_style = "";
					$obj_class = ' class="cimy_uef_textarea"';

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";

					break;

				case "textarea-rich":
					if ($tiny_mce_objects == "")
						$tiny_mce_objects = $unique_id;
					else
						$tiny_mce_objects .= ",".$unique_id;

					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).$required.'</label>';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = "";
					$obj_value = "";
					$obj_value2 = $value;
					$obj_checked = "";
					$obj_tag = "textarea";
					$obj_closing_tag = true;
					$obj_style = "";
					$obj_class = ' class="cimy_uef_textarea"';

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";
					break;

				case "dropdown-multi":
				case "dropdown":
					// cimy_dropDownOptions uses cimy_uef_sanitize_content and esc_attr by itself
					$ret = cimy_dropDownOptions($label, $non_escaped_value);
					$label = $ret['label'];
					$html = $ret['html'];
					$obj_label = '<label for="'.$unique_id.'">'.$label.$required.'</label>';

					if ($type == "dropdown-multi") {
						$obj_name = ' name="'.$input_name.'[]" multiple="multiple" size="5"';
						$obj_style = ' style="height: 11em;"';
					}
					else {
						$obj_name = ' name="'.$input_name.'"';
						$obj_style = '';
					}

					$obj_type = '';
					$obj_value = '';
					$obj_value2 = $html;
					$obj_checked = "";
					$obj_tag = "select";
					$obj_closing_tag = true;

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";

					break;

				case "checkbox":
					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).'</label>';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="'.$type.'"';
					$obj_value = ' value="1"';
					$obj_value2 = "";
					$value == "YES" ? $obj_checked = ' checked="checked"' : $obj_checked = '';
					$obj_tag = "input";
					$obj_closing_tag = false;
					$obj_style = ' style="width:auto; border:0; background:white;"';

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";

					break;

				case "radio":
					$obj_label = '<label for="'.$unique_id.'"> '.cimy_uef_sanitize_content($label).'</label>';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="'.$type.'"';
					$obj_value = ' value="'.$field_id.'"';
					$obj_value2 = "";
					$obj_tag = "input";
					$obj_closing_tag = false;
					$obj_style = ' style="width:auto; border:0; background:white;"';

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";

					if (($value == "selected") || (($value == "YES") && ($radio_checked[$name] == 0))) {
						$radio_checked[$name] = 1;
						$obj_checked = ' checked="checked"';
					}
					else
						$obj_checked = '';

					break;

				case "avatar":
				case "picture":
				case "file":
					$allowed_exts = '';
					if (isset($rules['equal_to']))
						if (!empty($rules['equal_to']))
							$allowed_exts = "'".implode("', '", explode(",", $rules['equal_to']))."'";

					$obj_label = '<label for="'.$unique_id.'">'.cimy_uef_sanitize_content($label).$required.'</label>';
					$obj_class = '';
					$obj_name = ' name="'.$input_name.'"';
					$obj_type = ' type="file"';
					$obj_value = ' value=""';
					$obj_value2 = '';
					$obj_checked = "";
					$obj_tag = "input";
					$obj_closing_tag = false;

					if ($type == "file") {
						// if we do not escape then some translations can break
						$warning_msg = $wpdb->escape(__("Please upload a file with one of the following extensions", $cimy_uef_domain));

						$obj_style = ' onchange="uploadFile(\'your-profile\', \''.$unique_id.'\', \''.$warning_msg.'\', Array('.$allowed_exts.'));"';
					}
					else {
						// if we do not escape then some translations can break
						$warning_msg = $wpdb->escape(__("Please upload an image with one of the following extensions", $cimy_uef_domain));
						$allowed_exts = "'".implode("','", cimy_uef_get_allowed_image_extensions())."'";
						$obj_style = ' onchange="uploadFile(\'your-profile\', \''.$unique_id.'\', \''.$warning_msg.'\', Array('.$allowed_exts.'));"';
					}

					if (cimy_uef_is_field_disabled($type, $rules['edit'], $old_value))
						$obj_disabled = ' disabled="disabled"';
					else
						$obj_disabled = "";

					break;

				case "registration-date":
					$value = cimy_get_registration_date($get_user_id, $value);
					if (isset($rules['equal_to']))
						$obj_value = cimy_get_formatted_date($value, $rules['equal_to']);
					else
						$obj_value = cimy_get_formatted_date($value);

					$obj_label = '<label>'.cimy_uef_sanitize_content($label).'</label>';

					break;
			}


			$obj_id = ' id="'.$unique_id.'"';
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

			$form_object = '<'.$obj_tag.$obj_id.$obj_class.$obj_name.$obj_type.$obj_value.$obj_checked.$obj_maxlen.$obj_rowscols.$obj_style.$obj_disabled;

			if ($obj_closing_tag)
				$form_object.= ">".$obj_value2."</".$obj_tag.">";
			else
				$form_object.= " />";

			echo "<th>";
			echo $obj_label;
			echo "</th>\n";

			echo "\t\t<td>";

			if ((!empty($description)) && (($type == "picture") || ($type == "picture-url")))
				echo "<span class='description'>".$description."</span><br />";

			if (in_array($type, $cimy_uef_file_types)) {
				$profileuser = get_user_to_edit($get_user_id);
			}

			if ($type == "avatar") {
				$user_email = $profileuser->user_email;
				$img_avatar = get_avatar($user_email, $size = '128');
				$img_avatar = str_replace("<img", "<img id='$field_id_data'", $img_avatar);
				echo '<div id="profpic">'.$img_avatar."</div>\n\t\t";
			}

			if ((in_array($type, $cimy_uef_file_types)) && (!empty($value))) {
				$old_value = basename($old_value);
				$user_login = $profileuser->user_login;

				if ($type == "picture") {
					$value_thumb = cimy_get_thumb_path($value);
					$file_on_server = cimy_uef_get_dir_or_filename($user_login, $value, false);
					$file_thumb = cimy_uef_get_dir_or_filename($user_login, $value, true);
					if ((!empty($advanced_options["no-thumb"])) && (is_file($file_thumb)))
						rename($file_thumb, $file_on_server);

					echo "\n\t\t";
					if (is_file($file_thumb)) {
						echo '<a target="_blank" href="'.$value.'"><img id="'.$field_id_data.'" src="'.$value_thumb.'" alt="picture" /></a><br />';
						echo "\n\t\t";
					}
					else if (is_file($file_on_server)) {
						echo '<img id="'.$field_id_data.'" src="'.$value.'" alt="picture" /><br />';
						echo "\n\t\t";
					}
				}

				if ($type == "file") {
					echo '<a target="_blank" href="'.$value.'">';
					echo basename($value);
					echo '</a><br />';
					echo "\n\t\t";
				}

				// if there is no image or there is the default one then disable delete button
				if (empty($old_value)) {
					$dis_delete_img = ' disabled="disabled"';
				}
				// else if there is an image and it's not the default one
				else {
					// take the "can be modified" rule just set before
					$dis_delete_img = $obj_disabled;

// 					echo '<input type="hidden" name="'.$input_name.'_oldfile" value="'.basename($value).'" />';
// 					echo "\n\t\t";
				}

				if ((($type == "picture") || ($type == "avatar")) && ((empty($rules["equal_to"])) || (!empty($advanced_options["no-thumb"])))) {
					echo "<input type=\"hidden\" name=\"".$field_id_data."_x1\" id=\"".$field_id_data."_x1\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".$field_id_data."_y1\" id=\"".$field_id_data."_y1\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".$field_id_data."_x2\" id=\"".$field_id_data."_x2\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".$field_id_data."_y2\" id=\"".$field_id_data."_y2\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".$field_id_data."_w\" id=\"".$field_id_data."_w\" value=\"\" />";
					echo "<input type=\"hidden\" name=\"".$field_id_data."_h\" id=\"".$field_id_data."_h\" value=\"\" />";
// 					echo "<p class=\"submit\"><input type=\"submit\" name=\"".$field_id_data."_button\" class=\"button-primary\" value=\"".__("Edit Image")."\"  /></p>";
					echo "<input type=\"hidden\" name=\"".$field_id_data."_button\" id=\"".$field_id_data."_button\" value=\"1\" />";
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
				echo '<input type="checkbox" name="'.$input_name.'_del" value="1" style="width:auto; border:0; background:white;"'.$dis_delete_img.' />';

				if ($type == "file") {
					echo " ".__("Delete the file", $cimy_uef_domain)."<br /><br />";
					echo "\n\t\t".__("Update the file", $cimy_uef_domain)."<br />";
				}
				else {
					echo " ".__("Delete the picture", $cimy_uef_domain)."<br /><br />";
					echo "\n\t\t".__("Update the picture", $cimy_uef_domain)."<br />";
				}
				echo "\n\t\t";
			}

			if ($type == "picture-url") {
				if (!empty($value)) {
					if (intval($rules['equal_to'])) {
						echo '<a target="_blank" href="'.$value.'">';
						echo '<img src="'.$value.'" alt="picture"'.$size.' width="'.intval($rules['equal_to']).'" height="*" />';
						echo "</a>";
					}
					else {
						echo '<img src="'.$value.'" alt="picture" />';
					}

					echo "<br />";
					echo "\n\t\t";
				}

				echo "<br />".__("Picture URL:", $cimy_uef_domain)."<br />\n\t\t";
			}

			// write previous value
			echo "<input type=\"hidden\" name=\"".$input_name."_".$field_id."_prev_value\" value=\"".$old_value."\" />\n\t\t";

			// TinceMCE needed and we have WordPress >= 3.3 yummy!
			if ($type == "textarea-rich" && function_exists("wp_editor")) {
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
				wp_editor($non_escaped_value, $unique_id, $editor_settings);
			}
			// write to the html the form object built
			else if ($type != "registration-date")
				echo $form_object;
			else
				echo $obj_value;

			if ((!empty($description)) && ($type != "picture") && ($type != "picture-url")) {
				if (($type == "textarea") || ($type == "textarea-rich"))
					echo "<br />";
				else
					echo " ";

				echo "<span class='description'>".$description."</span>" ;
			}

			echo "</td>";
			echo "\n\t</tr>\n";
		}

		echo "</table>";

		// WP 3.2 or lower (N)
		if (!empty($tiny_mce_objects) && !function_exists("wp_editor")) {
			require_once($cuef_plugin_dir.'/cimy_uef_init_mce.php');
		}
		echo $end_cimy_uef_comment;
	}
}

function cimy_update_ExtraFields() {
	global $wpdb, $wpdb_data_table, $user_ID, $max_length_value, $fields_name_prefix, $cimy_uef_file_types, $user_level, $cimy_uef_domain;

	// if updating meta-data from registration post then exit
	if (isset($_POST['cimy_post']))
		return;

	if (isset($_POST['user_id'])) {
		$get_user_id = $_POST['user_id'];

		if (!current_user_can('edit_user', $get_user_id))
			return;
	}
	else
		return;

	$get_user_id = intval($get_user_id);
	$profileuser = get_user_to_edit($get_user_id);
	$user_login = $profileuser->user_login;
	$user_displayname = $profileuser->display_name;
	$extra_fields = get_cimyFields(false, true);

	$query = "UPDATE ".$wpdb_data_table." SET VALUE=CASE FIELD_ID";
	$i = 0;

	$field_ids = "";
	$mail_changes = "";

	foreach ($extra_fields as $thisField) {
		$field_id = $thisField["ID"];
		$name = $thisField["NAME"];
		$type = $thisField["TYPE"];
		$label = $thisField["LABEL"];
		$rules = $thisField["RULES"];
		$unique_id = $fields_name_prefix.$field_id;
		$input_name = $fields_name_prefix.esc_attr($name);
		$field_id_data = $input_name."_".$field_id."_data";
		$advanced_options = cimy_uef_parse_advanced_options($rules["advanced_options"]);

		cimy_insert_ExtraFields_if_not_exist($get_user_id, $field_id);

		// if the current user LOGGED IN has not enough permissions to see the field, skip it
		// apply only for EXTRA FIELDS
		if ($rules['show_level'] == 'view_cimy_extra_fields')
		{
			if (!current_user_can($rules['show_level']))
				continue;
		}
		else if ($user_level < $rules['show_level'])
			continue;

		// if show_level == anonymous then do NOT ovverride other show_xyz rules
		if ($rules['show_level'] == -1) {
			// if flag to show the field in the profile is NOT activated, skip it
			if (!$rules['show_in_profile'])
				continue;
		}

		$prev_value = $wpdb->escape(stripslashes($_POST[$input_name."_".$field_id."_prev_value"]));
		if (cimy_uef_is_field_disabled($type, $rules['edit'], $prev_value))
			continue;

		if ((isset($_POST[$input_name])) && (!in_array($type, $cimy_uef_file_types))) {
			if ($type == "dropdown-multi")
				$field_value = stripslashes(implode(",", $_POST[$input_name]));
			else
				$field_value = stripslashes($_POST[$input_name]);

			if ($type == "picture-url")
				$field_value = str_replace('../', '', $field_value);

			if (isset($rules['max_length']))
				$field_value = substr($field_value, 0, $rules['max_length']);
			else
				$field_value = substr($field_value, 0, $max_length_value);

			$field_value = $wpdb->escape($field_value);

			if ($i > 0)
				$field_ids.= ", ";
			else
				$i = 1;

			$field_ids.= $field_id;

			$query.= " WHEN ".$field_id." THEN ";

			switch ($type) {
				case 'dropdown':
				case 'dropdown-multi':
					$ret = cimy_dropDownOptions($label, $field_value);
					$label = $ret['label'];
				case 'picture-url':
				case 'textarea':
				case 'textarea-rich':
				case 'password':
				case 'text':
					$value = "'".$field_value."'";
					$prev_value = "'".$prev_value."'";
					break;

				case 'checkbox':
					$value = $field_value == '1' ? "'YES'" : "'NO'";
					$prev_value = $prev_value == "YES" ? "'YES'" : "'NO'";
					break;

				case 'radio':
					$value = $field_value == $field_id ? "'selected'" : "''";
					$prev_value = "'".$prev_value."'";
					break;
			}

			$query.= $value;
		}
		// when a checkbox is not selected then it isn't present in $_POST at all
		// file input in html also is not present into $_POST at all so manage here
		else {
			$rules = $thisField['RULES'];

			if (in_array($type, $cimy_uef_file_types)) {
				if ($type == "avatar") {
					// since avatars are drawn max to 512px then we can save bandwith resizing, do it!
					$rules['equal_to'] = 512;
				}

				if (isset($_POST[$input_name.'_del']))
					$delete_file = true;
				else
					$delete_file = false;

				if (isset($_POST[$input_name."_".$field_id."_prev_value"]))
					$old_file = stripslashes($_POST[$input_name."_".$field_id."_prev_value"]);
				else
					$old_file = false;

				$field_value = cimy_manage_upload($input_name, $user_login, $rules, $old_file, $delete_file, $type, (!empty($advanced_options["filename"])) ? $advanced_options["filename"] : "");

				if ((!empty($field_value)) || ($delete_file)) {
					if ($i > 0)
						$field_ids.= ", ";
					else
						$i = 1;

					$field_ids.= $field_id;

					$value = "'".$field_value."'";
					$prev_value = "'".$prev_value."'";

					$query.= " WHEN ".$field_id." THEN ";
					$query.= $value;
				}
				else {
					$prev_value = $value;

					$file_on_server = cimy_uef_get_dir_or_filename($user_login, $old_file, false);
					if (($type == "picture") || ($type == "avatar"))
						cimy_uef_crop_image($file_on_server, $field_id_data);
				}
			}

			if ($type == 'checkbox') {
				// if can be editable then write NO
				// there is no way to understand if was YES or NO previously
				// without adding other hidden inputs so write always
				if ($i > 0)
					$field_ids.= ", ";
				else
					$i = 1;

				$field_ids.= $field_id;

				$field_value = "NO";
				$value = "'".$field_value."'";
				$prev_value = $prev_value == "YES" ? "'YES'" : "'NO'";

				$query.= " WHEN ".$field_id." THEN ";
				$query.= $value;
			}

			if ($type == 'dropdown-multi') {
				// if can be editable then write ''
				// there is no way to understand if was YES or NO previously
				// without adding other hidden inputs so write always
				if ($i > 0)
					$field_ids.= ", ";
				else
					$i = 1;

				$field_ids.= $field_id;

				$field_value = '';
				$value = "'".$field_value."'";
				$prev_value = "'".$prev_value."'";
				$ret = cimy_dropDownOptions($label, $field_value);
				$label = $ret['label'];
				$query.= " WHEN ".$field_id." THEN ";
				$query.= $value;
			}
		}
		if (($rules["email_admin"]) && ($value != $prev_value) && ($type != "registration-date")) {
			$mail_changes.= sprintf(__("%s previous value: %s new value: %s", $cimy_uef_domain), $label, stripslashes($prev_value), stripslashes($value));
			$mail_changes.= "\r\n";
		}
	}

	if ($i > 0) {
		$query.=" ELSE FIELD_ID END WHERE FIELD_ID IN(".$field_ids.") AND USER_ID = ".$get_user_id;

		// $query WILL BE: UPDATE <table> SET VALUE=CASE FIELD_ID WHEN <field_id1> THEN <value1> [WHEN ... THEN ...] ELSE FIELD_ID END WHERE FIELD_ID IN(<field_id1>, [<field_id2>...]) AND USER_ID=<user_id>
		$wpdb->query($query);
	}

	// mail only if set and if there is something to mail
	if (!empty($mail_changes)) {
		$admin_email = get_option('admin_email');
		$mail_subject = sprintf(__("%s (%s) has changed one or more fields", $cimy_uef_domain), $user_displayname, $user_login);
		wp_mail($admin_email, $mail_subject, $mail_changes);
	}
}
