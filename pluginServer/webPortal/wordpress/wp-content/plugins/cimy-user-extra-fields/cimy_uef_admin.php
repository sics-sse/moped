<?php

function cimy_admin_define_extra_fields() {
	global $wpdb, $wpdb_fields_table, $wpdb_wp_fields_table, $rule_canbeempty, $rule_email, $rule_maxlen, $rule_maxlen_needed, $available_types, $max_length_name, $max_length_label, $max_length_desc, $max_length_value, $max_size_file, $rule_equalto, $rule_equalto_case_sensitive, $cimy_uef_domain, $cuef_plugin_path, $cimy_uef_file_types, $rule_equalto_regex;
	
	if (!cimy_check_admin('manage_options'))
		return;
// if (!empty($_POST))
// 	print_r($_POST);

	$role = & get_role('administrator');
	$role->add_cap('view_cimy_extra_fields');

	$errors = Array();
	$results = Array();

	$add_caption = __("Add field", $cimy_uef_domain);
	$edit_caption = __("Update field", $cimy_uef_domain);
	$del_caption = __("Delete field", $cimy_uef_domain);
	$delSel_caption = __("Delete selected fields", $cimy_uef_domain);
	$order_caption = __("Change order", $cimy_uef_domain);
	
	$submit_msgs = array();
	$submit_msgs['add_caption'] = $add_caption;
	$submit_msgs['edit_caption'] = $edit_caption;
	$submit_msgs['del_caption'] = $del_caption;
	$submit_msgs['delSel_caption'] = $delSel_caption;
	$submit_msgs['order_caption'] = $order_caption;
	
	$min_length_caption = __("Min length", $cimy_uef_domain);
	$exact_length_caption = __("Exact length", $cimy_uef_domain);
	$max_length_caption = __("Max length", $cimy_uef_domain);
	$exact_or_max_length_capton = __("Exact or Max length", $cimy_uef_domain);
	
	$minLen = 1;
	$maxLen = $max_length_value;

	$submit = "";
	$fieldset = 0;
	$action = "";
	// defaults to add
	$field_order = "0";
	
	// options form engine
	if (isset($_POST['cimy_options'])) {
		$action = "options";
		$res = cimy_save_options();
	}
	else if ((isset($_POST["submit_add"])) && (!empty($_POST["submit_add"]))) {
		if (!check_admin_referer('cimy_uef_addfield', 'cimy_uef_addfieldnonce'))
			return;
		$action = "add";
		$submit = $_POST["submit_add"];
	}
	else if ((isset($_POST["submit_edit"])) && (!empty($_POST["submit_edit"]))) {
		if (!check_admin_referer('cimy_uef_editfield', 'cimy_uef_editfieldnonce'))
			return;
		$action = "edit";
		$submit = $_POST["submit_edit"];
	}
	else if ((isset($_POST["submit_del"])) && (!empty($_POST["submit_del"]))) {
		if (!check_admin_referer('cimy_uef_editfield', 'cimy_uef_editfieldnonce'))
			return;
		$action = "del";
		$submit = $_POST["submit_del"];
	}
	else if ((isset($_POST["submit_del_sel"])) && (!empty($_POST["submit_del_sel"]))) {
		if (!check_admin_referer('cimy_uef_editfield', 'cimy_uef_editfieldnonce'))
			return;
		$action = "delSel";
		$submit = $_POST["submit_del_sel"];
	}
	else if ((isset($_POST["submit_order"])) && (!empty($_POST["submit_order"]))) {
		if (!check_admin_referer('cimy_uef_editfield', 'cimy_uef_editfieldnonce'))
			return;
		$action = "order";
		$submit = $_POST["submit_order"];
	}

	if (!isset($res))
		$res = array();
	
	// call options draw function
	$opt_ret = cimy_show_options($res, true);

	// needed fields count here, after options and before $action manager! do not move!
	$num_fields = $opt_ret['db_extra_fields'];
	$num_wp_fields = $opt_ret['db_wp_fields'];

	if (isset($_POST['wp_fields'])) {
		$wp_fields_post = true;
		$fields_table = $wpdb_wp_fields_table;
		$tot_fields = $num_wp_fields;
	}
	else {
		$wp_fields_post = false;
		$fields_table = $wpdb_fields_table;
		$tot_fields = $num_fields;
	}

	// if pushed change order button
	if ($action == "order") {
		$sql = "UPDATE ".$fields_table." SET F_ORDER=CASE F_ORDER";
		$k = (-1);
		$msg = "";
		$msg_new = "";
		$arr1 = Array();
		$arr2 = Array();

		// check only selected fields
		foreach ($_POST['check'] as $i) {
			if ($k > (-1)) {
				$msg.= ", ";
				$msg_new.= ", ";
			}
			else
				$k = $i;

			$sql.= " WHEN ".$i." THEN ".$_POST['order'][$i];
			$msg.= $i;
			$msg_new.= $_POST['order'][$i];

			array_push($arr1, $i);
			array_push($arr2, $_POST['order'][$i]);
		}
		if ($k > (-1)) {
			if (count(array_diff($arr1, $arr2)) == 0) {
				$sql.= " ELSE F_ORDER END WHERE F_ORDER IN (".$msg.")";

				// $sql WILL BE: UPDATE <table> SET F_ORDER=CASE F_ORDER WHEN <oldvalue1> THEN <newvalue1> [WHEN ... THEN ...] ELSE F_ORDER END WHERE F_ORDER IN(<oldvalue1> [, <oldvalue2>... ])
				$wpdb->query($sql);

				$results['order'] = __("Fields", $cimy_uef_domain)." #".$msg." ".__("changed to", $cimy_uef_domain)." #".$msg_new;
			}
			else
				$errors['order'] = __("You cannot give an order that misses some numbers", $cimy_uef_domain);
		}
		else
			$errors['order'] = __("Nothing selected", $cimy_uef_domain);
	}

	// if pushed delete or update single button
	if (($action == "del") || ($action == "edit")) {
		$field_order = key($submit);

		// if pushed the single delete button then check the relative checkbox and let delSel code to delete it
		if ($action == "del") {
			$_POST['check'][$field_order] = $field_order;
			$action = "delSel";
		}
	}
	
	if ($action == "delSel") {
		$sql = "DELETE FROM ".$fields_table." WHERE ";
		$sql_data_del = "";

		$k = (-1);
		$j = (-1);
		$msg = "";
		$not_del_old = "";
		$not_del_sql = "";

		// check which fields are selected for deletions
		for ($i = 1; $i <= $tot_fields; $i++)
			if (!empty($_POST['check'][$i])) {
				if ($k > (-1)) {
					$sql.= " OR ";
					$sql_data_del.= " OR ";
					$msg.= ", ";
				}
				else {
					$k = $i;
					$j = $i;
				}

				$sql_data_del.= "FIELD_ID=".$i;
				$sql.= "F_ORDER=".$i;
				$msg.= $i;
				// wpml stuff, unregister label and description for deleted fields
				$field_to_del_name = substr(stripslashes($_POST['name'][$i]), 0, $max_length_name);
				cimy_wpml_unregister_string($field_to_del_name."_label");
				cimy_wpml_unregister_string($field_to_del_name."_desc");
			}
			else // field to NOT be deleted, but order probably have to change, if j==(-1) then order is ok because deletions is after it!
				if ($j > (-1)) {
					if ($not_del_old != "") {
						
						$not_del_old.= ", ";
					}

					$not_del_sql.= " WHEN ".$i." THEN ".$j." ";
					$not_del_old.= $i;
					$j++;
				}

		// if at least one field was selected
		if ($k > (-1)) {
			// $sql WILL BE: DELETE FROM <table> WHERE F_ORDER=<value1> [OR F_ORDER=<value2> ...]
			$wpdb->query($sql);
			
			// delete also all data inserted by users in this/these field/s
			cimy_delete_users_info($sql_data_del);

			if ($not_del_sql != "") {
				$not_del_sql = "UPDATE ".$fields_table." SET F_ORDER=CASE F_ORDER".$not_del_sql."ELSE F_ORDER END WHERE F_ORDER IN(".$not_del_old.")";

				// $not_del_sql WILL BE: UPDATE <table> SET F_ORDER=CASE F_ORDER WHEN <oldvalue1> THEN <newvalue1> [WHEN ... THEN ...] ELSE F_ORDER END WHERE F_ORDER IN(<oldvalue1> [, <oldvalue2>...])
				$wpdb->query($not_del_sql);
			}

			$results['delete'] = __("Field(s)", $cimy_uef_domain)." #".$msg." ".__("deleted correctly", $cimy_uef_domain);
		}
		else
			$errors['delete'] = __("Nothing selected", $cimy_uef_domain);
	}

	// TODO add more defaults here and get rid of selected_input
	$store_rule = array();
	$store_rule['email'] = false;
	$store_rule['email_admin'] = false;

	if (($action == "add") || ($action == "edit")) {
		// RETRIEVE DATA FROM THE FORM
		$name = substr(stripslashes($_POST['name'][$field_order]), 0, $max_length_name);
		$value = substr(stripslashes($_POST['value'][$field_order]), 0, $max_length_value);
		$desc = substr(stripslashes($_POST['description'][$field_order]), 0, $max_length_desc);
		$label = substr(stripslashes($_POST['label'][$field_order]), 0, $max_length_label);

		$name = strtoupper($name);
		$oldname = isset($_POST['oldname'][$field_order]) ? strtoupper(stripslashes($_POST['oldname'][$field_order])) : '';
		$type = $_POST['type'][$field_order];
		$fieldset = isset($_POST['fieldset'][$field_order]) ? $_POST['fieldset'][$field_order] : '';
		$minlen = isset($_POST['minlen'][$field_order]) ? $_POST['minlen'][$field_order] : '';
		$exactlen = isset($_POST['exactlen'][$field_order]) ? $_POST['exactlen'][$field_order] : '';
		$maxlen = isset($_POST['maxlen'][$field_order]) ? $_POST['maxlen'][$field_order] : '';

		// min length available
		$minLen = 1;

		// max length or size for picture available
		if (in_array($type, $cimy_uef_file_types)) {
			$maxLen = $max_size_file;
			
			/* overwrite previous values */
			$min_length_caption = __("Min size", $cimy_uef_domain)." (KB)";
			$exact_length_caption = __("Exact size", $cimy_uef_domain)." (KB)";
			$max_length_caption = __("Max size", $cimy_uef_domain)." (KB)";
			
			$exact_or_max_length_capton = __("Exact or Max size", $cimy_uef_domain)." (KB)";
		}
		else
			$maxLen = $max_length_value;
		/* end overwrite previous values */
		
		if (!empty($minlen))
			$store_rule['min_length'] = intval($_POST['minlength'][$field_order]);
		
		if (!empty($exactlen))
			$store_rule['exact_length'] = intval($_POST['exactlength'][$field_order]);

		if (!empty($maxlen))
			$store_rule['max_length'] = intval($_POST['maxlength'][$field_order]);

		$store_rule['can_be_empty'] = empty($_POST['empty'][$field_order]) ? false : true;
		$store_rule['edit'] = $_POST['edit'][$field_order];
		$store_rule['email'] = empty($_POST['email'][$field_order]) ? false : true;

		$equal = empty($_POST['equal'][$field_order]) ? '' : $_POST['equal'][$field_order];
		if (!empty($equal)) {
			$store_rule['equal_to'] = empty($_POST['equalto'][$field_order]) ? '' : stripslashes($_POST['equalto'][$field_order]);
			$equalto_casesens = empty($_POST['equalto_casesens'][$field_order]) ? '' : $_POST['equalto_casesens'][$field_order];
			$equalto_regex = empty($_POST['equalto_regex'][$field_order]) ? '' : $_POST['equalto_regex'][$field_order];
		}

		$store_rule["advanced_options"] = stripslashes($_POST['advanced_options'][$field_order]);
		$store_rule['show_in_reg'] = empty($_POST['show_in_reg'][$field_order]) ? false : true;
		$store_rule['show_in_profile'] = empty($_POST['show_in_profile'][$field_order]) ? false : true;
		$store_rule['show_in_aeu'] = empty($_POST['show_in_aeu'][$field_order]) ? false : true;
		$store_rule['show_in_search'] = empty($_POST['show_in_search'][$field_order]) ? false : true;
		$store_rule['show_in_blog'] = empty($_POST['show_in_blog'][$field_order]) ? false : true;

		$show_level = $_POST['show_level'][$field_order];
		$store_rule['show_level'] = $show_level;
		$store_rule['email_admin'] = empty($_POST['email_admin'][$field_order]) ? false : true;

		// START CHECKING FOR ERRORS
		if (empty($name))
			$errors['name'] = __("Name not specified", $cimy_uef_domain);
		else if (!stristr($name, " ") === false)
			$errors['name'] = __("Name cannot contains spaces", $cimy_uef_domain);

		if (empty($label))
			$errors['label'] = __("Label not specified", $cimy_uef_domain);

		// max or exact length rule is needed for this type
		if (in_array($type, $rule_maxlen_needed)) {
			if (empty($maxlen) && empty($exactlen))
				$errors['maxlength1'] = $exact_or_max_length_capton." ".__("not selected (with this type is necessary)", $cimy_uef_domain);
		}
		
		// max or exact length rule is not needed but it's available for this type
		if (in_array($type, $rule_maxlen)) {
			if ((!empty($maxlen) || !empty($minlen)) && !empty($exactlen))
				$errors['exactlength1'] = __("If you select", $cimy_uef_domain)." ".$exact_length_caption." ".__("you cannot select Min or Max", $cimy_uef_domain);

			// MIN LEN
			if (!empty($minlen))
				if (($store_rule['min_length'] < $minLen) || ($store_rule['min_length'] > $maxLen))
					$errors['minlength3'] = $min_length_caption." ".__("should be in the range of", $cimy_uef_domain)." ".$minLen. "-".$maxLen;
			
			// EXACT LEN
			if (!empty($exactlen))
				if (($store_rule['exact_length'] < $minLen) || ($store_rule['exact_length'] > $maxLen))
					$errors['exactlength3'] = $exact_length_caption." ".__("should be in the range of", $cimy_uef_domain)." ".$minLen. "-".$maxLen;

			// MAX LEN
			if (!empty($maxlen))
				if (($store_rule['max_length'] < $minLen) || ($store_rule['max_length'] > $maxLen))
					$errors['maxlength3'] = $max_length_caption." ".__("should be in the range of", $cimy_uef_domain)." ".$minLen. "-".$maxLen;
		}
		else {
			$minlen = "";
			$exactlen = "";
			$maxlen = "";
		}

		if (!empty($equal)) {
			if (empty($store_rule['equal_to']))
				$errors['equalTo'] = __("Equal TO not specified", $cimy_uef_domain);
			else if ((strtoupper($store_rule['equal_to']) != "YES") && (strtoupper($store_rule['equal_to']) != "NO")) {
				if ($type == "checkbox")
					$errors['equalTo2'] = __("With checkbox type Equal TO can only be", $cimy_uef_domain).": [Yes, No]";

				if ($type == "radio")
					$errors['equalTo2'] = __("With radio type Equal TO can only be", $cimy_uef_domain).": [Yes, No]";
			}
			
			if ((!empty($equalto_casesens)) && (in_array($type, $rule_equalto_case_sensitive)))
				$store_rule['equal_to_case_sensitive'] = true;
			else
				$store_rule['equal_to_case_sensitive'] = false;

			if ((!empty($equalto_regex)) && (in_array($type, $rule_equalto_regex)))
				$store_rule['equal_to_regex'] = true;
			else
				$store_rule['equal_to_regex'] = false;
		}

		if ((!empty($value)) && (strtoupper($value) != "YES") && (strtoupper($value) != "NO")) {
			if ($type == "checkbox")
				$errors['value'] = __("With checkbox type Value can only be", $cimy_uef_domain).": [Yes, No]";

			if ($type == "radio")
				$errors['value'] = __("With radio type Value can only be", $cimy_uef_domain).": [Yes, No]";
		}

		if (is_multisite()) {
			// uploading files not supported with WordPress MU
			if (in_array($type, $cimy_uef_file_types)) {
				$store_rule["show_in_reg"] = false;
			}
		}

		// IF THERE ARE NO ERRORS THEN GO ON
		if (count($errors) == 0) {
			$exist = array();

			if ($type != "radio") {
				$sql1 = "SELECT id FROM ".$fields_table." WHERE name='".$wpdb->escape($name)."' LIMIT 1";
				$exist = $wpdb->get_row($sql1);
			}

			// SEARCH THE NAME IN THE DATABASE, GO ON ONLY IF DURING EDIT IT WAS THE SAME FIELD
			if ((count($exist) == 0) || (($action == "edit") && ($oldname == $name))) {
				// MIN LEN
				if (!in_array($type, $rule_maxlen))
					unset($store_rule['min_length']);

				// EXACT LEN
				if (!in_array($type, $rule_maxlen))
					unset($store_rule['exact_length']);

				// MAX LEN
				if (!in_array($type, $rule_maxlen))
					unset($store_rule['max_length']);
				
				if (!in_array($type, $rule_email))
					$store_rule['email'] = false;
				
				if (!in_array($type, $rule_canbeempty))
					$store_rule['can_be_empty'] = true;

				if (($type == "checkbox") || ($type == "radio"))
					$value = strtoupper($value);
				
				$data = array();
				$data['name'] = $name;
				$data['value'] = $value;
				$data['desc'] = $desc;
				$data['label'] = $label;
				$data['type'] = $type;
				$data['store_rule'] = $store_rule;
				$data['field_order'] = $field_order;
				$data['num_fields'] = $num_fields;
				$data['fieldset'] = $fieldset;
				
				cimy_save_field($action, $fields_table, $data);

				if ($action == "add")
					$results['inserted'] = __("Field inserted correctly", $cimy_uef_domain);
				else if ($action == "edit") {
					$results['edit'] = __("Field #", $cimy_uef_domain).$field_order." ".__("updated correctly", $cimy_uef_domain);
					// wpml stuff, unregister the string if name changed
					if ($name != $oldname && !empty($oldname)) {
						cimy_wpml_unregister_string($oldname."_label");
						cimy_wpml_unregister_string($oldname."_desc");
					}
				}
				// wpml stuff, always register or update
				cimy_wpml_register_string($name."_label", $label);
				cimy_wpml_register_string($name."_desc", $desc);
			}
			else {
				$errors['namedup'] = __("Name inserted is just in the database, change to another one", $cimy_uef_domain);
			}
		}
	}
	
	// if extra fields table is not present
	if ($num_fields == -1)
		exit;
	
	// do NOT move this line, it's here because should shows also fields just added to the database
	$allFields = get_cimyFields();
	
	?>

	<div class="wrap" id="addfield">
	<h2><?php _e("Add a new Field", $cimy_uef_domain); ?></h2>

	<?php

	// print errors if there are some
	cimy_uef_print_messages($errors, $results);
	
	if (isset($store_rule['min_length']) && $store_rule['min_length'] == 0)
		unset($store_rule['min_length']);
	
	if (isset($store_rule['exact_length']) && $store_rule['exact_length'] == 0)
		unset($store_rule['exact_length']);

	if (isset($store_rule['max_length']) && $store_rule['max_length'] == 0)
		unset($store_rule['max_length']);

	if (!isset($store_rule['show_level']))
		$store_rule['show_level'] = "-1";

	if (!isset($store_rule['edit']))
		$store_rule['edit'] = "ok_edit";

	if (empty($type))
		$type = "text";

	$selected_input["name"] = '';
	$selected_input["label"] = '';
	$selected_input["value"] = '';
	$selected_input["desc"] = '';
	$selected_input["min_length"] = '';
	$selected_input["exact_length"] = '';
	$selected_input["max_length"] = '';
	$selected_input["equal_to"] = '';
	$selected_input["minlen"] = '';
	$selected_input["exactlen"] = '';
	$selected_input["maxlen"] = '';
	$selected_input["advanced_options"] = '';

	if ($action == "add") {
		// NAME
		if (!empty($name))
			$selected_input["name"] = $name;

		// VALUE
		(empty($value)) ? $selected_input["value"] = '' : $selected_input["value"] = $value;

		// LABEL
		(empty($label)) ? $selected_input["label"] = '' : $selected_input["label"] = $label;

		// DESCRIPTION
		if (!empty($desc))
			$selected_input["desc"] = $desc;

		// MIN LEN
		if (!empty($minlen))
			$selected_input["minlen"] = ' checked="checked"';

		if (isset($store_rule['min_length']))
			$selected_input["min_length"] = $store_rule['min_length'];

		// EXACT LEN
		if (!empty($exactlen))
			$selected_input["exactlen"] = ' checked="checked"';

		if (isset($store_rule['exact_length']))
			$selected_input["exact_length"] = $store_rule['exact_length'];

		// MAX LEN
		if (!empty($maxlen))
			$selected_input["maxlen"] = ' checked="checked"';

		if (isset($store_rule['max_length']))
			$selected_input["max_length"] = $store_rule['max_length'];

		if (isset($store_rule['equal_to']))
			$selected_input["equal_to"] = $store_rule['equal_to'];

		// ADVANCED OPTIONS
		if (isset($store_rule['advanced_options']))
			$selected_input["advanced_options"] = $store_rule['advanced_options'];
	}

	// CAN BE EMPTY
	if ((!isset($store_rule['can_be_empty'])) || ($store_rule['can_be_empty'] == true) || ($action != "add"))
		$selected_input["empty"] = ' checked="checked"';
	else
		$selected_input["empty"] = '';

	// SHOW IN REGISTRATION
	if ((!isset($store_rule['show_in_reg'])) || ($store_rule['show_in_reg'] == true) || ($action != "add"))
		$selected_input["show_in_reg"] = ' checked="checked"';
	else
		$selected_input["show_in_reg"] = '';
	
	// SHOW IN PROFILE
	if ((!isset($store_rule['show_in_profile'])) || ($store_rule['show_in_profile'] == true) || ($action != "add"))
		$selected_input["show_in_profile"] = ' checked="checked"';
	else
		$selected_input["show_in_profile"] = '';

	// SHOW IN AUTHORS AND USERS EXTENDED
	if ((!isset($store_rule['show_in_aeu'])) || ($store_rule['show_in_aeu'] == true) || ($action != "add"))
		$selected_input["show_in_aeu"] = ' checked="checked"';
	else
		$selected_input["show_in_aeu"] = '';

	$selected_input["name"] = esc_attr($selected_input["name"]);
	$selected_input["value"] = esc_html($selected_input["value"]);
	$selected_input["label"] = esc_html($selected_input["label"]);
	$selected_input["desc"] = esc_html($selected_input["desc"]);
	$selected_input["equal_to"] = esc_attr($selected_input["equal_to"]);
	?>
	
	<form method="post" action="#addfield">
		<?php wp_nonce_field('cimy_uef_addfield', 'cimy_uef_addfieldnonce', false); ?>
		<p><?php _e("To add a new field you have to choose a name, type and label; optional are value and description. Rules are applied during user registration.", $cimy_uef_domain); ?></p>
		<ul>
			<li><?php _e("With <strong>radio</strong> and <strong>checkbox</strong>: <em>Value</em> and <em>equal TO</em> can only be 'Yes' or 'No' that means 'selected' or 'not selected'", $cimy_uef_domain); ?></li>
			<li><?php _e("With <strong>drop-down</strong>: you have to add all options into label for example: label/item1,item2,item3", $cimy_uef_domain); ?></li>
			<li><?php _e("With <strong>picture</strong>: you can preload a default image putting url in <em>Value</em>; 'min,exact,max size' are in KB; <em>equal TO</em> means max pixel size (width or height) for thumbnail", $cimy_uef_domain); ?></li>
			<li><?php _e("With <strong>picture-url</strong>: you can preload a default image putting url in <em>Value</em>; <em>equal TO</em> means max width pixel size (height will be proportional)", $cimy_uef_domain); ?></li>
			<li><?php _e("With <strong>registration-date</strong>: <em>equal TO</em> means date and time format", $cimy_uef_domain); ?></li>
			<li><?php _e("With <strong>avatar</strong>: you can preload a default image putting url in <em>Value</em>; 'min,exact,max size' are in KB; <em>equal TO</em> is automatically set to 512 pixels", $cimy_uef_domain); ?></li>
			<li><?php _e("With <strong>file</strong>: you can preload a default file putting url in <em>Value</em>; 'min,exact,max size' are in KB; under <em>equal TO</em> can be specified allowed extensions separated by comma, example: zip,pdf,doc", $cimy_uef_domain); ?></li>
		</ul>
		<br />

		<table  class="widefat" cellpadding="10">
		<thead align="center">
		<tr>
			<th><h3 style="text-align: center;"><?php _e("Name"); ?> - <?php _e("Value"); ?></h3></th>
			<th><h3 style="text-align: center;"><?php _e("Type", $cimy_uef_domain); ?></h3></th>
			<th><h3 style="text-align: center;"><?php _e("Label", $cimy_uef_domain); ?> - <?php _e("Description"); ?></h3></th>
			<th><h3 style="text-align: center;"><?php _e("Rules", $cimy_uef_domain); ?></h3></th>
			<th><h3 style="text-align: center;"><?php _e("Actions"); ?></h3></th>
		</tr>
		</thead>
		<tbody id="plugins" class="plugins">
		<tr class="active">
		<td style="vertical-align: middle;">
			<label><strong><?php _e("Name"); ?></strong><br /><input name="name[0]" type="text" value="<?php echo $selected_input["name"]; ?>" maxlength="<?php echo $max_length_name; ?>" /></label><br /><br />
			<label><strong><?php _e("Value"); ?></strong><br /><textarea name="value[0]" rows="2" cols="17"><?php echo $selected_input["value"]; ?></textarea></label>
		</td>
		<td style="vertical-align: middle;">
			<label><strong><?php _e("Type", $cimy_uef_domain); ?></strong><br />
			<select name="type[0]">
			<?php
				foreach($available_types as $this_type) {
					echo '<option value="'.$this_type.'"'.selected($this_type, $type, false).'>'.$this_type.'</option>';
					echo "\n";
				}
			?>
			</select>
			</label>
			<br /><br />
			<label><strong><?php _e("Fieldset", $cimy_uef_domain); ?></strong><br />
			<?php echo cimy_fieldsetOptions($fieldset, "0"); ?>
			</label>
		</td>
		<td style="vertical-align: middle;">
			<label><strong><?php _e("Label", $cimy_uef_domain); ?></strong><br /><textarea name="label[0]" rows="2" cols="18"><?php echo $selected_input["label"]; ?></textarea></label><br /><br />
			<label><strong><?php _e("Description"); ?></strong><br /><textarea name="description[0]" rows="4" cols="18"><?php echo $selected_input["desc"]; ?></textarea></label>
		</td>
		<td style="vertical-align: middle;">
			<!-- MIN LENGTH -->
			<input type="checkbox" name="minlen[0]" value="1"<?php echo $selected_input["minlen"]; ?> /> <?php echo $min_length_caption; ?> [1-<?php echo $maxLen; ?>]: &nbsp;&nbsp;&nbsp;<input type="text" name="minlength[0]" value="<?php echo $selected_input["min_length"]; ?>" maxlength="5" size="5" /><br />
			
			<!-- EXACT LENGTH -->
			<input type="checkbox" name="exactlen[0]" value="1"<?php echo $selected_input["exactlen"]; ?> /> <?php echo $exact_length_caption; ?> [1-<?php echo $maxLen; ?>]: <input type="text" name="exactlength[0]" value="<?php echo $selected_input["exact_length"]; ?>" maxlength="5" size="5" /><br />

			<!-- MAX LENGTH -->
			<input type="checkbox" name="maxlen[0]" value="1"<?php echo $selected_input["maxlen"]; ?> /> <?php echo $max_length_caption; ?> [1-<?php echo $maxLen; ?>]: &nbsp;&nbsp;<input type="text" name="maxlength[0]" value="<?php echo $selected_input["max_length"]; ?>" maxlength="5" size="5" /><br />
			
			<input type="checkbox" name="empty[0]" value="1"<?php echo $selected_input["empty"]; ?> /> <?php _e("Can be empty", $cimy_uef_domain); ?><br />
			<input type="checkbox" name="email[0]" value="1"<?php checked(true, $store_rule['email'], true); ?> /> <?php _e("Check for E-mail syntax", $cimy_uef_domain); ?><br />
			
			<select name="edit[0]">
				<option value="ok_edit"<?php selected('ok_edit', $store_rule['edit'], true); ?>><?php _e("Can be modified", $cimy_uef_domain); ?></option>
				<option value="edit_only_if_empty"<?php selected('edit_only_if_empty', $store_rule['edit'], true); ?>><?php _e("Can be modified only if empty", $cimy_uef_domain); ?></option>
				<option value="edit_only_by_admin"<?php selected('edit_only_by_admin', $store_rule['edit'], true); ?>><?php _e("Can be modified only by admin", $cimy_uef_domain); ?></option>
				<option value="edit_only_by_admin_or_if_empty"<?php selected('edit_only_by_admin_or_if_empty', $store_rule['edit'], true); ?>><?php _e("Can be modified only by admin or if empty", $cimy_uef_domain); ?></option>
				<option value="no_edit"<?php selected('no_edit', $store_rule['edit'], true); ?>><?php _e("Cannot be modified", $cimy_uef_domain); ?></option>
			</select>
			<br />
			<!-- EQUAL TO -->
			<input type="checkbox" name="equal[0]" value="1"<?php checked(false, empty($equal), true); ?> /> <?php _e("Should be equal TO", $cimy_uef_domain); ?>: <input type="text" name="equalto[0]" maxlength="500" value="<?php echo $selected_input["equal_to"]; ?>"/><br />
			<!-- CASE SENSITIVE -->
			&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="equalto_casesens[0]" value="1"<?php checked(false, empty($equalto_casesens), true); ?> /> <?php _e("Case sensitive", $cimy_uef_domain); ?><br />

			<!-- REGEX -->
			&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="equalto_regex[0]" value="1"<?php checked(false, empty($equalto_regex), true); ?> /> <?php _e("Regular Expression", $cimy_uef_domain); ?><br />
			
			<!-- SHOW IN REGISTRATION -->
			<input type="checkbox" name="show_in_reg[0]" value="1"<?php echo $selected_input["show_in_reg"]; ?> /> <?php _e("Show the field in the registration", $cimy_uef_domain); ?><br />
			
			<!-- SHOW IN PROFILE -->
			<input type="checkbox" name="show_in_profile[0]" value="1"<?php echo $selected_input["show_in_profile"]; ?> /> <?php _e("Show the field in User's profile", $cimy_uef_domain); ?><br />
			
			<!-- SHOW IN A&U EXTENDED -->
			<input type="checkbox" name="show_in_aeu[0]" value="1"<?php echo $selected_input["show_in_aeu"]; ?> /> <?php _e("Show the field in Users Extended section", $cimy_uef_domain); ?><br />

			<!-- SHOW IN THE SEARCH ENGINE -->
			<input type="checkbox" name="show_in_search[0]" value="1"<?php checked(empty($store_rule['show_in_search']), false, true); ?> /> <?php _e("Show the field in the search engine", $cimy_uef_domain); ?><br />

			<!-- SHOW IN THE BLOG -->
			<input type="checkbox" name="show_in_blog[0]" value="1"<?php checked(empty($store_rule['show_in_blog']), false, true); ?> /> <?php _e("Show the field in the blog", $cimy_uef_domain); ?><br />

			<!-- SHOW SECURITY LEVEL -->
			<?php _e("Show the field if the role is at least:", $cimy_uef_domain)." "; ?>
			<select name="show_level[0]">
			<option value="-1"<?php selected("-1", $store_rule['show_level'], true); ?>><?php _e("Anonymous"); ?></option>
			<option value="0"<?php selected("0", $store_rule['show_level'], true); ?>><?php echo translate_user_role("Subscriber"); ?></option>
			<option value="1"<?php selected("1", $store_rule['show_level'], true); ?>><?php echo translate_user_role("Contributor"); ?></option>
			<option value="2"<?php selected("2", $store_rule['show_level'], true); ?>><?php echo translate_user_role("Author"); ?></option>
			<option value="5"<?php selected("5", $store_rule['show_level'], true); ?>><?php echo translate_user_role("Editor"); ?></option>
			<option value="8"<?php selected("8", $store_rule['show_level'], true); ?>><?php echo translate_user_role("Administrator"); ?></option>
			<option value="view_cimy_extra_fields"<?php selected("view_cimy_extra_fields", $store_rule['show_level'], true); ?>><?php _e("User has 'view_cimy_extra_fields' capability", $cimy_uef_domain); ?></option>
			</select>
			<br />

			<!-- EMAIL ADMIN -->
			<input type="checkbox" name="email_admin[0]" value="1"<?php checked(true, $store_rule['email_admin'], true); ?> /> <?php _e("Send an email to the admin if the user changes its value", $cimy_uef_domain); ?><br />
			<!-- ADVANCED OPTIONS -->
			<?php _e("Advanced options", $cimy_uef_domain); ?>: <input type="text" name="advanced_options[0]" maxlength="500" value="<?php echo $selected_input["advanced_options"]; ?>"/><br />

		</td>
		<td align="center" style="vertical-align: middle;">
			<p class="submit" style="border-width: 0px;">
			<input class="button button-secondary" name="reset" type="reset" value="<?php _e("Clear", $cimy_uef_domain); ?>" /><br /><br />
			<input class="button button-primary" name="submit_add[0]" type="submit" value="<?php echo $add_caption ?>" />
			</p>
		</td>
		</tr>
		</tbody>
		</table>
		<br /><br />
	</form>

	</div>

<script type="text/javascript">
<!--//
function changeFormAction(form_id, tr_id) {
    var element = document.getElementById(form_id);
    element.action = '#'+tr_id;
}
//-->
</script>

<?php
	$wp_fields = get_cimyFields(true);

	cimy_admin_show_extra_fields($wp_fields, $submit_msgs, true, $errors, $results, $wp_fields_post, $field_order);
	cimy_admin_show_extra_fields($allFields, $submit_msgs, false, $errors, $results, $wp_fields_post, $field_order);
}

function cimy_admin_show_extra_fields($allFields, $submit_msgs, $wp_fields, $errors, $results, $wp_fields_post, $field_order) {
	global $wpdb, $cimy_uef_domain, $rule_maxlen, $rule_email, $rule_canbeempty, $rule_equalto, $rule_equalto_case_sensitive, $available_types, $max_length_name, $max_length_label, $max_length_desc, $max_length_value, $max_size_file, $cimy_uef_file_types, $rule_equalto_regex;
	
	if (!cimy_check_admin("manage_options"))
		return;
	
	if ((count($allFields) == 0) && ($wp_fields))
		return;

	if ($wp_fields) {
		$field_anchor = "field_wp_";
		$div_id = "wp_extrafields";
		$form_id = "form_wp_fields";
	}
	else {
		$field_anchor = "field_";
		$div_id = "extrafields";
		$form_id = "form_extra_fields";

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
	}
	
	$add_caption = $submit_msgs['add_caption'];
	$edit_caption = $submit_msgs['edit_caption'];
	$del_caption = $submit_msgs['del_caption'];
	$delSel_caption = $submit_msgs['delSel_caption'];
	$order_caption = $submit_msgs['order_caption'];

	$invert_selection_label = $wpdb->escape(__("Invert selection", $cimy_uef_domain));
	$delete_fields_label = $wpdb->escape(__("Are you sure you want to delete field(s) and all data inserted into by users?", $cimy_uef_domain));
	
?>
	<div class="wrap" id="<?php echo $div_id; ?>">
	<h2><?php
		if ($wp_fields)
			_e("WordPress Fields", $cimy_uef_domain);
		else
			_e("Extra Fields", $cimy_uef_domain); ?>
	</h2>

	<form method="post" action="#addfield" id="<?php echo $form_id; ?>">

	<?php
	wp_nonce_field('cimy_uef_editfield', 'cimy_uef_editfieldnonce', false);
	if ($wp_fields)
		echo '<input type="hidden" name="wp_fields" value="1" />';

	if (count($allFields) == 0)
		_e("None!", $cimy_uef_domain);
	else {
		?>
		<p class="submit" style="border-width: 0px; margin-top: 0px; margin-bottom: 0px; padding: 0px;">
		<input class="button" type="button" value="<?php echo $invert_selection_label; ?>" onclick="this.value=invert_sel('<?php echo $form_id; ?>', 'check', '<?php echo $invert_selection_label; ?>')" />
		<input class="button action" name="submit_order" type="submit" value="<?php echo $order_caption ?>" />
		
		<?php if (!$wp_fields) { ?>
			<input class="button" name="submit_del_sel" type="submit" value="<?php echo $delSel_caption ?>" onclick="return confirm('<?php echo $delete_fields_label; ?>');" />
		<?php } ?>
		</p>

		<table class="widefat" cellpadding="10">
		<?php
		$thead_tfoot = '<tr>
			<th style="text-align: center;"><h3>'.__("Order", $cimy_uef_domain).'</h3></th>
			<th style="text-align: center;"><h3>'.__("Name").' - '.__("Value").' - '.__("Type", $cimy_uef_domain).'</h3></th>
			<th style="text-align: center;"><h3>'.__("Label", $cimy_uef_domain).' - '.__("Description").'</h3></th>
			<th style="text-align: center;"><h3>'.__("Rules", $cimy_uef_domain).'</h3></th>
			<th style="text-align: center;"><h3>'.__("Actions").'</h3></th>
		</tr>';
		?>
		<thead align="center">
			<?php echo $thead_tfoot; ?>
		</thead>
		<tfoot align="center">
			<?php echo $thead_tfoot; ?>
		</tfoot>
		<tbody>
		<?php

		$style = "";
		foreach ($allFields as $field) {
			$id = $field['ID'];
			$order = $field['F_ORDER'];
			$name = esc_attr($field['NAME']);
			$value = esc_html($field['VALUE']);
			$desc = esc_html($field['DESCRIPTION']);
			$label = esc_html($field['LABEL']);
			$type = $field['TYPE'];
			$rules = $field['RULES'];
			if (!$wp_fields) {
				$fieldset = $field["FIELDSET"];
				$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_reg", $rules["show_in_reg"]);
				$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_profile", $rules["show_in_profile"]);
				$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_aeu", $rules["show_in_aeu"]);
				$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_blog", $rules["show_in_blog"]);
				$javascripts_dep = cimy_uef_set_javascript_dependencies($javascripts_dep, $type, "show_in_search", $rules["show_in_search"]);
			}

			// MIN LEN
			if (isset($rules['min_length']))
				$minLength = $rules['min_length'];
			else
				$minLength = "";

			// EXACT LEN
			if (isset($rules['exact_length']))
				$exactLength = $rules['exact_length'];
			else
				$exactLength = "";

			// MAX LEN
			if (isset($rules['max_length']))
				$maxLength = $rules['max_length'];
			else
				$maxLength = "";

			if (isset($rules['equal_to']))
				$equalTo = $rules['equal_to'];
			else
				$equalTo = "";

			$equalTo = esc_attr($equalTo);
			$advanced_options = $rules['advanced_options'];

			if (in_array($type, $cimy_uef_file_types)) {
				$min_length_caption = __("Min size", $cimy_uef_domain)." (KB)";
				$exact_length_caption = __("Exact size", $cimy_uef_domain)." (KB)";
				$max_length_caption = __("Max size", $cimy_uef_domain)." (KB)";
				
				// overwrite max length but in another variable otherwise (bug)
				$max_length_value_caption = $max_size_file;
			}
			else {
				$min_length_caption = __("Min length", $cimy_uef_domain);
				$exact_length_caption = __("Exact length", $cimy_uef_domain);
				$max_length_caption = __("Max length", $cimy_uef_domain);

				$max_length_value_caption = $max_length_value;
			}

			$style = ('class="alternate"' == $style) ? '' : 'class="alternate"';
			?>
			<?php if (($field_order == $order) && ($wp_fields_post == $wp_fields)) { ?><tr <?php echo "id=\"".$field_anchor.$order."\""; ?>><td colspan="5"><?php cimy_uef_print_messages($errors, $results); ?></td></tr><?php } ?>
			<tr <?php echo $style; ?>>
			<td align="center" style="vertical-align: middle;">
				<input name="check[<?php echo $order ?>]" type="checkbox" value="<?php echo $order ?>" /><br /><br />
				<label><strong><?php _e("Order", $cimy_uef_domain); ?></strong><br />
				<input name="order[<?php echo $order ?>]" type="text" value="<?php echo $order ?>" maxlength="4" size="3" /></label>
			</td>
			<td style="vertical-align: middle;">
			<?php
			if ($wp_fields) {
			?>
				<input name="name[<?php echo $order ?>]" type="hidden" value="<?php echo $name ?>" />
				<input name="type[<?php echo $order ?>]" type="hidden" value="<?php echo $type ?>" />
			<?php
			}
			?>
				<label><strong><?php _e("Name"); ?></strong><br />
				<input name="name[<?php echo $order ?>]" type="text" value="<?php echo $name ?>" maxlength="<?php echo $max_length_name ?>"<?php disabled(true, $wp_fields, true); ?> /></label><br /><br />
				<input name="oldname[<?php echo $order ?>]" type="hidden" value="<?php echo $name ?>" />
				<label><strong><?php _e("Value"); ?></strong><br />
				<textarea name="value[<?php echo $order ?>]" rows="2" cols="17"><?php echo $value; ?></textarea></label>
				<br /><br />
				<label><strong><?php _e("Type", $cimy_uef_domain); ?></strong><br />
				<select name="type[<?php echo $order ?>]"<?php disabled(true, $wp_fields, true); ?>>
				<?php 
					foreach($available_types as $this_type)
						echo "<option value=\"".$this_type."\"".selected($type, $this_type, false).">".$this_type."</option>\n";
				?>
				</select>
				</label>

				<?php if (!$wp_fields) { ?>
				<br /><br />
				<label><strong><?php _e("Fieldset", $cimy_uef_domain); ?></strong><br />
				<?php echo cimy_fieldsetOptions($fieldset, $order); ?>
				</label>
				<?php } ?>
			</td>
			<td style="vertical-align: middle;">
				<label><strong><?php _e("Label", $cimy_uef_domain); ?></strong><br />
				<textarea name="label[<?php echo $order ?>]" rows="2" cols="18"><?php echo $label; ?></textarea>
				</label><br /><br />
				<label><strong><?php _e("Description"); ?></strong><br />
				<textarea name="description[<?php echo $order ?>]" rows="4" cols="18"><?php echo $desc ?></textarea>
				</label>
			</td>
			<td style="vertical-align: middle;">
				<!-- MIN LENGTH -->
				<input type="checkbox" name="minlen[<?php echo $order ?>]" value="1"<?php checked(true, isset($rules['min_length']), true); disabled(false, in_array($type, $rule_maxlen), true); ?> /> <?php echo $min_length_caption; ?> [1-<?php echo $max_length_value_caption; ?>]: &nbsp;&nbsp;&nbsp;<input type="text" name="minlength[<?php echo $order ?>]" value="<?php echo $minLength ?>" maxlength="5" size="5"<?php disabled(false, in_array($type, $rule_maxlen), true); ?> /><br />

				<!-- EXACT LENGTH -->
				<input type="checkbox" name="exactlen[<?php echo $order ?>]" value="1"<?php checked(true, isset($rules['exact_length']), true); disabled(false, in_array($type, $rule_maxlen), true); ?> /> <?php echo $exact_length_caption; ?> [1-<?php echo $max_length_value_caption; ?>]: <input type="text" name="exactlength[<?php echo $order ?>]" value="<?php echo $exactLength ?>" maxlength="5" size="5"<?php disabled(false, in_array($type, $rule_maxlen), true); ?> /><br />
				
				<!-- MAX LENGTH -->
				<input type="checkbox" name="maxlen[<?php echo $order ?>]" value="1"<?php checked(true, isset($rules['max_length']), true); disabled(false, in_array($type, $rule_maxlen), true); ?> /> <?php echo $max_length_caption; ?> [1-<?php echo $max_length_value_caption; ?>]: &nbsp;&nbsp;<input type="text" name="maxlength[<?php echo $order ?>]" value="<?php echo $maxLength ?>" maxlength="5" size="5"<?php disabled(false, in_array($type, $rule_maxlen), true); ?> /><br />
				
				<input type="checkbox" name="empty[<?php echo $order ?>]" value="1"<?php checked(true, $rules['can_be_empty'], true); disabled(false, in_array($type, $rule_canbeempty), true); ?> /> <?php _e("Can be empty", $cimy_uef_domain); ?><br />
				<input type="checkbox" name="email[<?php echo $order ?>]" value="1"<?php checked(true, $rules['email'], true); disabled(false, in_array($type, $rule_email), true); ?> /> <?php _e("Check for E-mail syntax", $cimy_uef_domain); ?><br />

				<select name="edit[<?php echo $order ?>]">
				<option value="ok_edit"<?php selected('ok_edit', $rules['edit'], true); ?>><?php _e("Can be modified", $cimy_uef_domain); ?></option>
				<option value="edit_only_if_empty"<?php selected('edit_only_if_empty', $rules['edit'], true); ?>><?php _e("Can be modified only if empty", $cimy_uef_domain); ?></option>
				<option value="edit_only_by_admin"<?php selected('edit_only_by_admin', $rules['edit'], true); ?>><?php _e("Can be modified only by admin", $cimy_uef_domain); ?></option>
				<option value="edit_only_by_admin_or_if_empty"<?php selected('edit_only_by_admin_or_if_empty', $rules['edit'], true); ?>><?php _e("Can be modified only by admin or if empty", $cimy_uef_domain); ?></option>
				<option value="no_edit"<?php selected('no_edit', $rules['edit'], true); ?>><?php _e("Cannot be modified", $cimy_uef_domain); ?></option>
				</select>
				<br />
				
				<!-- EQUAL TO -->
				<input type="checkbox" name="equal[<?php echo $order ?>]" value="1"<?php checked(true, isset($rules['equal_to']), true); disabled(false, in_array($type, $rule_equalto), true); ?> /> <?php _e("Should be equal TO", $cimy_uef_domain); ?>: <input type="text" name="equalto[<?php echo $order ?>]" maxlength="500" value="<?php echo $equalTo ?>"<?php disabled(false, in_array($type, $rule_equalto), true); ?> /><br />
				<!-- CASE SENSITIVE -->
				&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="equalto_casesens[<?php echo $order ?>]" value="1"<?php checked(true, isset($rules['equal_to']) && $rules['equal_to_case_sensitive'], true); disabled(false, in_array($type, $rule_equalto_case_sensitive), true); ?> /> <?php _e("Case sensitive", $cimy_uef_domain); ?><br />
				<!-- REGEX -->
				&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="equalto_regex[<?php echo $order ?>]" value="1"<?php checked(true, isset($rules['equal_to']) && $rules['equal_to_regex'], true); disabled(false, in_array($type, $rule_equalto_regex), true); ?> /> <?php _e("Regular Expression", $cimy_uef_domain); ?><br />

				
				<!-- SHOW IN REGISTRATION -->
				<!-- uploading files not supported with WordPress MS during registration due to email confirmation -->
				<input type="checkbox" name="show_in_reg[<?php echo $order ?>]" value="1"<?php checked(true, $rules['show_in_reg'], true); disabled(true, is_multisite() && in_array($type, $cimy_uef_file_types), true); ?> /> <?php _e("Show the field in the registration", $cimy_uef_domain); ?><br />
				
				<!-- SHOW IN PROFILE -->
				<input type="checkbox" name="show_in_profile[<?php echo $order ?>]" value="1"<?php checked(true, $rules['show_in_profile'], true); disabled(true, $wp_fields, true); ?> /> <?php _e("Show the field in User's profile", $cimy_uef_domain); ?><br />
				<?php
				if ($wp_fields) {
				?>
					<input name="show_in_profile[<?php echo $order ?>]" type="hidden" value="1" />
				<?php
				}
				?>
				<!-- SHOW IN A&U EXTENDED -->
				<input type="checkbox" name="show_in_aeu[<?php echo $order ?>]" value="1"<?php checked(true, $rules['show_in_aeu'], true); ?> /> <?php _e("Show the field in Users Extended section", $cimy_uef_domain); ?><br />

				<!-- SHOW IN THE SEARCH -->
				<input type="checkbox" name="show_in_search[<?php echo $order ?>]" value="1"<?php checked(true, $rules['show_in_search'], true); ?> /> <?php _e("Show the field in the search engine", $cimy_uef_domain); ?><br />

				<!-- SHOW IN THE BLOG -->
				<input type="checkbox" name="show_in_blog[<?php echo $order ?>]" value="1"<?php checked(true, $rules['show_in_blog'], true); ?> /> <?php _e("Show the field in the blog", $cimy_uef_domain); ?><br />

				<!-- SHOW SECURITY LEVEL -->
				<?php _e("Show the field if the role is at least:", $cimy_uef_domain)." "; ?>
				<select name="show_level[<?php echo $order ?>]">
				<option value="-1"<?php selected("-1", $rules['show_level'], true); ?>><?php _e("Anonymous"); ?></option>
				<option value="0"<?php selected("0", $rules['show_level'], true); ?>><?php echo translate_user_role("Subscriber"); ?></option>
				<option value="1"<?php selected("1", $rules['show_level'], true); ?>><?php echo translate_user_role("Contributor"); ?></option>
				<option value="2"<?php selected("2", $rules['show_level'], true); ?>><?php echo translate_user_role("Author"); ?></option>
				<option value="5"<?php selected("5", $rules['show_level'], true); ?>><?php echo translate_user_role("Editor"); ?></option>
				<option value="8"<?php selected("8", $rules['show_level'], true); ?>><?php echo translate_user_role("Administrator"); ?></option>
				<option value="view_cimy_extra_fields"<?php selected("view_cimy_extra_fields", $rules['show_level'], true); ?>><?php _e("User has 'view_cimy_extra_fields' capability", $cimy_uef_domain); ?></option>
				</select>
				<br />

				<?php
				if (!$wp_fields) {
				?>
					<!-- EMAIL ADMIN -->
					<input type="checkbox" name="email_admin[<?php echo $order ?>]" value="1"<?php checked(true, $rules['email_admin'], true); ?> /> <?php _e("Send an email to the admin if the user changes its value", $cimy_uef_domain); ?><br />
				<?php
				}
				?>
				<!-- ADVANCED OPTIONS -->
				<?php _e("Advanced options", $cimy_uef_domain); ?>: <input type="text" name="advanced_options[<?php echo $order ?>]" maxlength="500" value="<?php echo $advanced_options; ?>"/><br />
			</td>
			<td align="center" style="vertical-align: middle;">
				<p class="submit" style="border-width: 0px;">
				<input class="button button-secondary" name="reset" type="reset" value="<?php _e("Reset", $cimy_uef_domain); ?>" /><br /><br />
				<input class="button button-primary" name="submit_edit[<?php echo $order ?>]" type="submit" value="<?php echo $edit_caption." #".$order ?>" onclick="changeFormAction('<?php echo $form_id; ?>', '<?php echo $field_anchor.$order; ?>')" /><br /><br />
				
				<?php if (!$wp_fields) { ?>
					<input class="button button-secondary" name="submit_del[<?php echo $order ?>]" type="submit" value="<?php echo $del_caption." #".$order ?>" onclick="return confirm('<?php echo $delete_fields_label; ?>');" />
				<?php } ?>
				</p>
			</td>
			</tr>
		<?php
		} // end of foreach ($allFields as $field)
		?>
		</tbody>
		</table>
		<p class="submit" style="border-width: 0px; margin-top: 0px; margin-bottom: 0px; padding: 0px;">
		<input class="button" type="button" value="<?php echo $invert_selection_label; ?>" onclick="this.value=invert_sel('<?php echo $form_id; ?>', 'check', '<?php echo $invert_selection_label; ?>')" />
		<input class="button action" name="submit_order" type="submit" value="<?php echo $order_caption ?>" />
		
		<?php if (!$wp_fields) { ?>
			<input class="button" name="submit_del_sel" type="submit" value="<?php echo $delSel_caption ?>" onclick="return confirm('<?php echo $delete_fields_label; ?>');" />
		<?php } ?>
		</p>
		<br />
		<?php
	} // end of count($allFields) > 0

	?>
	</form>

	</div>

	<?php
	// this will help me to track down the javascript dependencies without looping through all fields too many times
	if (!$wp_fields && $options = cimy_get_options()) {
		$options['file_fields'] = $javascripts_dep['file_fields'];
		$options['image_fields'] = $javascripts_dep['image_fields'];
		$options['tinymce_fields'] = $javascripts_dep['tinymce_fields'];
		cimy_set_options($options);
	}
}

function cimy_uef_print_messages($errors, $results) {
	global $cimy_uef_domain;

	if (count($errors) > 0) {
	?>
		<div class="error inline">
		<h3><?php _e("ERROR", $cimy_uef_domain); ?></h3>
		<ul>
			<?php 
			foreach ($errors as $error)
				echo "<li>".$error."</li>";
			?>
		</ul>
		</div>
	<?php
	}
	?>

	<?php

	// print successes if there are some
	if (count($results) > 0) {
	?>
		<div class="updated inline">
		<h3><?php _e("SUCCESSFUL", $cimy_uef_domain); ?></h3>
		<ul>
			<?php 
			foreach ($results as $result)
				echo "<li>".$result."</li>";
			?>
		</ul>
		</div>
	<?php
	}
}

function cimy_admin_users_list_page() {
	global $wpdb, $wp_roles, $wpdb_data_table, $cuef_upload_path, $cimy_uef_domain, $cimy_uef_file_types;

	if (!cimy_check_admin('list_users'))
		return;

	$options = cimy_get_options();
	$dropdown_first_item = '--- '.__("select", $cimy_uef_domain).' ---';

	$extra_fields = get_cimyFields();

	if ((isset($_POST["submit_new_values"])) && (!empty($_POST["users"])) && (!empty($_POST["ef_write_type"]))) {
		if (!check_admin_referer('extrafieldnewvalue', 'extrafieldnewvaluenonce'))
			return;
		foreach ($_POST["users"] as $user_id) {
			foreach ($_POST["ef_write_type"] as $ef_name=>$ef_type) {
				if (!isset($_POST["ef_write_sel"][$ef_name]))
					continue;

				if (isset($_POST["ef_write"][$ef_name])) {
					$ef_value = $_POST["ef_write"][$ef_name];

					// if it is selected the "--- select ---" item then skip this check
					if ((($ef_type == "dropdown") || ($ef_type == "dropdown-multi")) && ($ef_value == $dropdown_first_item))
						continue;

					set_cimyFieldValue($user_id, $ef_name, $ef_value);
				}
				else {
					if ($ef_type == "checkbox") {
						$ef_value = "NO";
						set_cimyFieldValue($user_id, $ef_name, "");
					}
				}
			}
		}
	}

	if (isset($_POST["fieldset"][0]))
		$fieldset_selection = $_POST["fieldset"][0];
	else
		$fieldset_selection = -1;

	$usersearch = empty($_REQUEST['s']) ? "" : $_REQUEST['s'];
	$role = empty($_REQUEST['role']) ? "" : $_REQUEST['role'];
	$paged = intval(empty($_GET['userspage']) ? "1" : $_GET['userspage']);

	if (is_network_admin()) {
		require_once(ABSPATH . 'wp-admin/includes/class-wp-ms-users-list-table.php');
		class WP_Cimy_Users_List_Table extends WP_MS_Users_List_Table {
			function prepare_items() {
				global $role, $usersearch, $wpdb;

				$usersearch = isset($_REQUEST['s']) ? $_REQUEST['s'] : '';
				$role = isset($_REQUEST['role']) ? $_REQUEST['role'] : '';
				if (isset($_POST["cimy_uef_users_per_page"])) {
					$users_per_page = intval($_POST["cimy_uef_users_per_page"]);
					if ($user = wp_get_current_user())
						update_user_meta($user->ID, 'users_network_per_page', $users_per_page);
				}
				$users_per_page = $this->get_items_per_page( 'users_network_per_page' );

				$args = array(
					'number' => $users_per_page,
					'role' => $role,
					'search' => $usersearch,
					'fields' => 'all_with_meta',
					'blog_id' => 0,
				);

				if ($role == 'super') {
					$logins = implode("', '", get_super_admins());
					$args['include'] = $wpdb->get_col("SELECT ID FROM $wpdb->users WHERE user_login IN ('$logins')");
				}

				// If the network is large and a search is not being performed, show only the latest users with no paging in order
				// to avoid expensive count queries.
				if (!$usersearch && (get_blog_count() >= 10000)) {
					if (!isset($_REQUEST['orderby']))
						$_GET['orderby'] = $_REQUEST['orderby'] = 'id';
					if (!isset($_REQUEST['order']))
						$_GET['order'] = $_REQUEST['order'] = 'DESC';
					$args['count_total'] = false;
				}

				$args['search'] = ltrim($args['search'], '*');

				if (isset($_REQUEST['orderby']))
					$args['orderby'] = $_REQUEST['orderby'];

				if (isset($_REQUEST['order']))
					$args['order'] = $_REQUEST['order'];

				// Query the user IDs for this page
				$wp_user_search = new WP_User_Query($args);

				$this->items = $wp_user_search->get_results();
				$this->old_args = $args;
			}

			function prepare_items2($include, $exclude) {
				global $wpdb;
				if (isset($_POST["cimy_uef_users_per_page"])) {
					$users_per_page = intval($_POST["cimy_uef_users_per_page"]);
					if ($user = wp_get_current_user())
						update_user_meta($user->ID, 'users_network_per_page', $users_per_page);
				}
				$users_per_page = $this->get_items_per_page('users_network_per_page');
				$paged = $this->get_pagenum();
				$role = isset( $_REQUEST['role'] ) ? $_REQUEST['role'] : '';

				if ($role == 'super') {
					$logins = implode("', '", get_super_admins());
					$include = array_diff($wpdb->get_col("SELECT ID FROM $wpdb->users WHERE user_login IN ('$logins')"), $exclude);
				}

				$args = array_merge($this->old_args, array(
					'number' => $users_per_page,
					'offset' => ($paged-1) * $users_per_page,
					'include' => $include,
					'exclude' => $exclude,
				));
				// Query the user IDs for this page
				$wp_user_search = new WP_User_Query($args);

				$this->items = $wp_user_search->get_results();

				$this->set_pagination_args(array(
					'total_items' => $wp_user_search->get_total(),
					'per_page' => $users_per_page,
				));
			}
			function bulk_actions() {}
			function extra_tablenav($which) {
				if ('top' != $which)
					return;

				global $cimy_uef_domain;
				if (isset($_POST["fieldset"][0]))
					$fieldset_selection = $_POST["fieldset"][0];
				else
					$fieldset_selection = -1;
?>
				<label><?php _e("Fieldset", $cimy_uef_domain); ?>
				<?php echo cimy_fieldsetOptions($fieldset_selection, 0, true); ?>
				</label>

				<?php _e("Users per page", $cimy_uef_domain); ?> 
				<input type="text" name="cimy_uef_users_per_page" value="<?php echo $this->get_users_per_page(); ?>" size="4" />
				<input class="button" type="submit" name="submit" value="<?php _e("Apply"); ?>" />
<?php
			}
			function get_total() { return $this->_pagination_args['total_items']; }
			function get_users_per_page() { return $this->_pagination_args['per_page']; }
		}
	}
	else {
		require_once(ABSPATH . 'wp-admin/includes/class-wp-users-list-table.php');
		class WP_Cimy_Users_List_Table extends WP_Users_List_Table {
			function prepare_items() {
				global $role, $usersearch;

				$usersearch = isset($_REQUEST['s']) ? $_REQUEST['s'] : '';
				$role = isset($_REQUEST['role']) ? $_REQUEST['role'] : '';
				$per_page = ( $this->is_site_users ) ? 'site_users_network_per_page' : 'users_per_page';
				if (isset($_POST["cimy_uef_users_per_page"])) {
					$users_per_page = intval($_POST["cimy_uef_users_per_page"]);
					if ($user = wp_get_current_user())
						update_user_meta($user->ID, $per_page, $users_per_page);
				}
				$users_per_page = $this->get_items_per_page( $per_page );

				$args = array(
					'number' => $users_per_page,
					'role' => $role,
					'search' => $usersearch,
					'fields' => 'all_with_meta'
				);

				if ('' !== $args['search'])
					$args['search'] = '*' . $args['search'] . '*';

				if ($this->is_site_users)
					$args['blog_id'] = $this->site_id;

				if (isset($_REQUEST['orderby']))
					$args['orderby'] = $_REQUEST['orderby'];

				if (isset($_REQUEST['order']))
					$args['order'] = $_REQUEST['order'];

				// Query the user IDs for this page
				$wp_user_search = new WP_User_Query($args);

				$this->items = $wp_user_search->get_results();
				$this->old_args = $args;
			}

			function prepare_items2($include, $exclude) {
				$per_page = ($this->is_site_users) ? 'site_users_network_per_page' : 'users_per_page';
				if (isset($_POST["cimy_uef_users_per_page"])) {
					$users_per_page = intval($_POST["cimy_uef_users_per_page"]);
					if ($user = wp_get_current_user())
						update_user_meta($user->ID, $per_page, $users_per_page);
				}
				$users_per_page = $this->get_items_per_page($per_page);
				$paged = $this->get_pagenum();
				$args = array_merge($this->old_args, array(
					'number' => $users_per_page,
					'offset' => ($paged-1) * $users_per_page,
					'include' => $include,
					'exclude' => $exclude,
				));
				// Query the user IDs for this page
				$wp_user_search = new WP_User_Query($args);

				$this->items = $wp_user_search->get_results();

				$this->set_pagination_args(array(
					'total_items' => $wp_user_search->get_total(),
					'per_page' => $users_per_page,
				));
			}
			function bulk_actions() {}
			function extra_tablenav($which) {
				if ('top' != $which)
					return;

				global $cimy_uef_domain;
				if (isset($_POST["fieldset"][0]))
					$fieldset_selection = $_POST["fieldset"][0];
				else
					$fieldset_selection = -1;
?>
				<label><?php _e("Fieldset", $cimy_uef_domain); ?>
				<?php echo cimy_fieldsetOptions($fieldset_selection, 0, true); ?>
				</label>

				<?php _e("Users per page", $cimy_uef_domain); ?> 
				<input type="text" name="cimy_uef_users_per_page" value="<?php echo $this->get_users_per_page(); ?>" size="4" />
				<input class="button" type="submit" name="submit" value="<?php _e("Apply"); ?>" />
<?php
			}
			function get_total() { return $this->_pagination_args['total_items']; }
			function get_users_per_page() { return $this->_pagination_args['per_page']; }
		}
	}
	$cimy_users_table = new WP_Cimy_Users_List_Table();
	$cimy_users_table->prepare_items();
	$search_result = $cimy_users_table->items;

	$excluded_users = array();
	// search into extra field engine
	foreach ($search_result as $key=>$user_object) {
		foreach ($extra_fields as $ef) {
			$ef_id = $ef["ID"];
			$ef_type = $ef["TYPE"];
			$ef_name = $ef["NAME"];

			$ef_search = "";
			if (isset($_POST["ef_search"][$ef_name])) {
				$ef_search = $_POST["ef_search"][$ef_name];
			}

			if (!empty($ef_search)) {
				$remove = false;
				$ef_value = get_cimyFieldValue($user_object->ID, $ef_name);

				if (($ef_type == "text") || ($ef_type == "textarea") || ($ef_type == "textarea-rich") || ($ef_type == "picture") || ($ef_type == "picture-url") || ($ef_type == "file")) {
					if (stristr($ef_value, $ef_search) === FALSE) {
						$remove = true;
					}
				} else if ($ef_type == "checkbox") {
					if (($ef_search == "1") && ($ef_value != "YES")) {
						$remove = true;
					}
				} else if ($ef_type == "radio") {
					if (($ef_search == $ef_id) && ($ef_value != "selected")) {
						$remove = true;
					}
				} else if ($ef_type == "dropdown") {
					// if it is selected the "--- select ---" item then skip this check
					if ($ef_search == $dropdown_first_item)
						continue;

					if ($ef_search != $ef_value) {
						$remove = true;
					}
				} else if ($ef_type == "dropdown-multi") {
					// if it is selected the "--- select ---" item then remove it
					if ($ef_search[0] == $dropdown_first_item)
						unset($ef_search[0]);

					if (count(array_diff($ef_search, explode(",", $ef_value))) != 0)
						$remove = true;
				}
				
				if ($remove) {
					$excluded_users[] = $user_object->ID;
					break;
				}
			}
		}
	}
	$cimy_users_table->prepare_items2(array(), $excluded_users);
	$users_found = $cimy_users_table->get_total();
	?>
	<div class="wrap">
	
	<?php
		if (function_exists("screen_icon"))
			screen_icon("users");
	?>
	<h2><?php
	_e("Users Extended", $cimy_uef_domain);

	if (current_user_can('create_users')) { ?>
		<a href="user-new.php" class="add-new-h2"><?php echo esc_html_x('Add New', 'user'); ?></a>
	<?php } elseif (is_multisite() && current_user_can('promote_users')) { ?>
		<a href="user-new.php" class="add-new-h2"><?php echo esc_html_x('Add Existing', 'user'); ?></a>
	<?php }
	if (!empty($usersearch))
		printf('<span class="subtitle">'.__('Search results for &#8220;%s&#8221;')." (%s)</span>", esc_html($usersearch), $users_found);
	?></h2>
	<form id="posts-filter" action="" method="post"><?php
	wp_nonce_field('extrafieldnewvalue', 'extrafieldnewvaluenonce', false);
	$role_links = array();
	if (is_network_admin()) {
		$super_admins = get_super_admins();
		$total_admins = count($super_admins);
		$total_users = get_user_count();
	}
	else {
		$super_admins = array();
		$users_of_blog = count_users();
		$total_users = $users_of_blog['total_users'];
		$avail_roles =& $users_of_blog['avail_roles'];
		unset($users_of_blog);
	}

	$current_role = false;
	$class = empty($role) ? ' class="current"' : '';
	$role_links['all'] = "<li><a href='users.php?page=users_extended'$class>" . sprintf(_nx('All <span class="count">(%s)</span>', 'All <span class="count">(%s)</span>', $total_users, 'users'), number_format_i18n($total_users)) . '</a>';
	if (is_network_admin()) {
		$class = $role == 'super' ? ' class="current"' : '';
		$role_links['super'] = "<li><a href='" . network_admin_url('users.php?page=users_extended&role=super') . "'$class>" . sprintf(_n('Super Admin <span class="count">(%s)</span>', 'Super Admins <span class="count">(%s)</span>', $total_admins), number_format_i18n($total_admins)) . '</a>';
	}

	foreach ($wp_roles->get_names() as $this_role => $name) {
		if (!isset($avail_roles[$this_role]))
			continue;

		$class = '';

		if (!empty($_GET['role']) && $this_role == $_GET['role']) {
			$current_role = $_GET['role'];
			$class = ' class="current"';
		}

		$name = translate_user_role($name);
		$name = sprintf(__('%1$s <span class="count">(%2$s)</span>'), $name, $avail_roles[$this_role]);
		$tmp_link = esc_url(add_query_arg('role', $this_role));
		$role_links[] = "<li><a href=\"$tmp_link\"$class>" . $name . '</a>';
	}
	
	echo '<ul class="subsubsub">'.implode(' | </li>', $role_links) . '</li></ul>';
	unset($role_links);
?>
	<?php
		$cimy_users_table->search_box(__('Search Users'), 'user');
		$cimy_users_table->display_tablenav("top");
	?>
	<?php if (isset($errors) && is_wp_error($errors)) : ?>
		<div class="error">
			<ul>
			<?php
				foreach ($errors->get_error_messages() as $err)
					echo "<li>$err</li>\n";
			?>
			</ul>
		</div>
	<?php endif; ?>
	<?php if ($cimy_users_table->items) : ?>
		<table class="widefat" cellpadding="3" cellspacing="3" width="100%">
		<?php
		$thead_str = '<tr class="thead">';
		$thead_str.= '<th id="cb" scope="col" class="manage-column column-cb check-column" style=""><input type="checkbox" /> </th>';

		$tfoot_str = '<tr class="thead">';
		$tfoot_str.= '<th scope="col" class="manage-column column-cb check-column" style=""><input type="checkbox" /> </th>';

		if (!in_array("username", $options['aue_hidden_fields'])) {
			$thead_str.= '<th id="username" scope="col" class="manage-column column-username" style="">'.__("Username").'</th>';
			$tfoot_str.= '<th scope="col" class="manage-column column-username" style="">'.__("Username").'</th>';
		}
	
		if (!in_array("name", $options['aue_hidden_fields'])) {
			$thead_str.= '<th id="name" scope="col" class="manage-column column-name" style="">'.__("Name").'</th>';
			$tfoot_str.= '<th scope="col" class="manage-column column-name" style="">'.__("Name").'</th>';
		}
	
		if (!in_array("email", $options['aue_hidden_fields'])) {
			$thead_str.= '<th id="email" scope="col" class="manage-column column-email" style="">'.__("E-mail").'</th>';
			$tfoot_str.= '<th scope="col" class="manage-column column-email" style="">'.__("E-mail").'</th>';
		}
		
		if (!in_array("role", $options['aue_hidden_fields'])) {
			$thead_str.= '<th id="role" scope="col" class="manage-column column-role" style="">'.__("Role").'</th>';
			$tfoot_str.= '<th scope="col" class="manage-column column-role" style="">'.__("Role").'</th>';
		}
	
		if (!in_array("website", $options['aue_hidden_fields'])) {
			$thead_str.= '<th scope="col" class="manage-column" style="">'.__("Website").'</th>';
			$tfoot_str.= '<th scope="col" class="manage-column" style="">'.__("Website").'</th>';
		}
	
		if (!in_array("posts", $options['aue_hidden_fields'])) {
			$thead_str.= '<th id="posts" scope="col" class="manage-column column-posts num" style="">'.__("Posts").'</th>';
			$tfoot_str.= '<th scope="col" class="manage-column column-posts num" style="">'.__("Posts").'</th>';
		}
			
		$i = 0;
		$write_inputs = array();
		$write_input_checkbox = array();

		if (count($extra_fields) > 0)
			foreach ($extra_fields as $thisField) {
				$rules = $thisField['RULES'];
				if ($rules['show_in_aeu']) {
					$i++;
					$id = $thisField['ID'];
					$name = $thisField['NAME'];
					$name_esc_attr = esc_attr($thisField['NAME']);
					$label = cimy_uef_sanitize_content(cimy_wpml_translate_string($name."_label", $thisField["LABEL"]));
					$type = $thisField['TYPE'];
					$fieldset = $thisField["FIELDSET"];

					if ($type == "avatar")
						continue;

					if (($fieldset_selection > -1) && ($fieldset_selection != $fieldset))
						continue;

					$search_input = "";
					$search_value = "";

					if (!empty($_POST["ef_search"][$name])) {
						if ($type == "dropdown-multi")
							$search_value = esc_attr(stripslashes(implode(",", $_POST["ef_search"][$name])));
						else
							$search_value = esc_attr(stripslashes($_POST["ef_search"][$name]));
					}

					$thead_str.= "<th scope=\"col\" class=\"manage-column\" style=\"\">";
					$tfoot_str.= "<th scope=\"col\" class=\"manage-column\" style=\"\">";
					
					switch ($type) {
						case "dropdown":
							$ret = cimy_dropDownOptions($label, $search_value);
							$ret2 = str_ireplace(' selected="selected"', '', $ret['html']);
							$label = $ret['label'];
							
							$search_input = '<select name="ef_search['.$name_esc_attr.']"><option>'.$dropdown_first_item.'</option>'.$ret['html'].'</select>';
							$write_input[$i] = '<td>'.$label.'</td><td id="ef-new-value-'.$name_esc_attr.'"><select name="ef_write['.$name_esc_attr.']"><option>'.$dropdown_first_item.'</option>'.$ret2.'</select>';
							break;
						case "dropdown-multi":
							$ret = cimy_dropDownOptions($label, $search_value);
							$ret2 = str_ireplace(' selected="selected"', '', $ret['html']);
							$label = $ret['label'];
							
							$search_input = '<select name="ef_search['.$name_esc_attr.'][]" multiple="multiple" style="height: 6em;"><option>'.$dropdown_first_item.'</option>'.$ret['html'].'</select>';
							$write_input[$i] = '<td>'.$label.'</td><td id="ef-new-value-'.$name_esc_attr.'"><select name="ef_write['.$name_esc_attr.'][]" multiple="multiple" style="height: 6em;"><option>'.$dropdown_first_item.'</option>'.$ret2.'</select>';
							break;
						case "text":
						case "textarea":
						case "textarea-rich":
						case "picture-url":
							$search_input = '<input type="text" name="ef_search['.$name_esc_attr.']" value="'.$search_value.'" size="6" />';
							$write_input[$i] = '<td>'.$label.'</td><td id="ef-new-value-'.$name_esc_attr.'"><input type="text" name="ef_write['.$name_esc_attr.']" value="" size="40" />';
							break;
						case "picture":
						case "file":
							$search_input = '<input type="text" name="ef_search['.$name_esc_attr.']" value="'.$search_value.'" size="6" />';
							break;
						case "checkbox":
							$search_input = '<input type="checkbox" name="ef_search['.$name_esc_attr.']" value="1"'.checked(false, empty($search_value), false).' />';
							$write_input[$i] = '<td>'.$label.'</td><td id="ef-new-value-'.$name_esc_attr.'"><input type="checkbox" name="ef_write['.$name_esc_attr.']" value="1" />';
							break;
							
						case "radio":
							$search_input = '<input type="radio" name="ef_search['.$name_esc_attr.']" value="'.$id.'"'.checked($search_value, $id, false).' />';
							$write_input[$i] = '<td>'.$label.'</td><td><input type="radio" name="ef_write['.$name_esc_attr.']" value="'.$label.'" />';
							break;
					}

					if (isset($write_input[$i])) {
						if (empty($write_input_checkbox[$name])) {
							$write_input[$i] = '<td><input type="checkbox" name="ef_write_sel['.$name_esc_attr.']" value="1" /></td>'.$write_input[$i];
							$write_input_checkbox[$name] = true;
						}
						else
							$write_input[$i] = '<td>&nbsp;</td>'.$write_input[$i];

						$write_input[$i].= '<input type="hidden" name="ef_write_type['.$name_esc_attr.']" value="'.$type.'" /></td>';
					}

					$thead_str.= "$label<br />$search_input</th>";
					$tfoot_str.= "$label</th>";
				}
			}

		$thead_str.= '</tr>';
		$tfoot_str.= '</tr>';

		?>
		<thead>
			<?php echo $thead_str; ?>
		</thead>
		<tfoot>
			<?php echo $tfoot_str; ?>
		</tfoot>
		<?php
		$style = '';
		foreach ($cimy_users_table->items as $user_object) {
			$roles = $user_object->roles;
			$role = array_shift($roles);
			$email = $user_object->user_email;
			$url = $user_object->user_url;
			$short_url = str_replace('http://', '', $url);
			$short_url = str_replace('www.', '', $short_url);
				
			if ('/' == substr($short_url, -1))
				$short_url = substr($short_url, 0, -1);
				
			if (strlen($short_url) > 35)
				$short_url =  substr($short_url, 0, 32).'...';
				
			$style = ('class="alternate"' == $style) ? '' : 'class="alternate"';
			$numposts = count_user_posts($user_object->ID);
				
			if (0 < $numposts) $numposts = "<a href='edit.php?author=".$user_object->ID."' title='" . __('View posts by this author') . "'>$numposts</a>";
			echo "
			<tr $style>
			
			<th scope='row' class='check-column'>";
				if (current_user_can('edit_user', $user_object->ID))
					echo "<input type='checkbox' name='users[]' id='user_{$user_object->ID}' class='$role' value='{$user_object->ID}' />";
			echo "</th>";
			
			if (!in_array("username", $options['aue_hidden_fields'])) {
				// produce username clickable
				if (current_user_can('edit_user', $user_object->ID)) {
					$current_user = wp_get_current_user();
					
					if ($current_user->ID == $user_object->ID) {
						$edit = 'profile.php';
					} else {
						$edit = esc_url(add_query_arg('wp_http_referer', urlencode(esc_url(stripslashes($_SERVER['REQUEST_URI']))), "user-edit.php?user_id=$user_object->ID"));
					}
					$edit = "<a href=\"$edit\">$user_object->user_login</a>";
				} else {
					$edit = $user_object->user_login;
				}

				if (in_array($user_object->user_login, $super_admins))
					$edit.= ' - ' . __('Super Admin');

				$avatar = get_avatar($user_object->user_email, 32);
				echo "<td class=\"username column-username\"><strong>$avatar $edit</strong></td>";
			}
	
			if (!in_array("name", $options['aue_hidden_fields'])) {
				echo "<td class=\"name column-name\"><label for='user_{$user_object->ID}'>$user_object->first_name $user_object->last_name</label></td>";
			}
	
			if (!in_array("email", $options['aue_hidden_fields'])) {
				echo "<td class=\"email column-email\"><a href='mailto:$email' title='" . sprintf(__('e-mail: %s'), $email) . "'>$email</a></td>";
			}
			
			if (!in_array("role", $options['aue_hidden_fields'])) {
				$role_name = "";
				if (!empty($wp_roles->role_names[$role]))
					$role_name = translate_user_role($wp_roles->role_names[$role]);
				
				echo "<td class=\"role column-role\">";
				echo $role_name;
				echo '</td>';
			}
	
			if (!in_array("website", $options['aue_hidden_fields'])) {
				echo "<td ><a href='$url' title='website: $url'>$short_url</a></td>";
			}
	
			if (!in_array("posts", $options['aue_hidden_fields'])) {
				echo "<td class=\"posts column-posts num\">$numposts</td>";
			}

			// print all the content of extra fields if there are some
			if (count($extra_fields) > 0) {
				foreach ($extra_fields as $thisField) {
					$field_id = $thisField['ID'];

					// if user has not yet fields in the data table then create them
					cimy_insert_ExtraFields_if_not_exist($user_object->ID, $field_id);
				}

				foreach ($extra_fields as $thisField) {
					$name = $thisField['NAME'];
					$name_esc_attr = esc_attr($thisField['NAME']);
					$rules = $thisField['RULES'];
					$type = $thisField['TYPE'];
					$value = $thisField['VALUE'];
					$fieldset = $thisField["FIELDSET"];

					if ($type == "avatar")
						continue;

					if (($fieldset_selection > -1) && ($fieldset_selection != $fieldset))
						continue;

					if ($rules['show_in_aeu']) {
						$field_id = $thisField['ID'];
						$field = get_cimyFieldValue($user_object->ID, $name);

						echo "<td>";
						echo "<div id=\"edit-".$user_object->ID."-".$name_esc_attr."\">";
						echo "<div id=\"value-".$user_object->ID."-".$name_esc_attr."\">";

						if ($type == "picture-url") {
							if (empty($field))
								$field = $value;
								
							if (!empty($field)) {
								if (intval($rules['equal_to'])) {
									echo '<a target="_blank" href="'.esc_attr($field).'">';
									echo '<img src="'.esc_attr($field).'" alt="picture"'.$size.' width="'.intval($rules['equal_to']).'" height="*" />';
									echo "</a>";
								}
								else {
									echo '<img src="'.esc_attr($field).'" alt="picture" />';
								}
							
								echo "<br />";
								echo "\n\t\t";
							}
						}
						else if ($type == "picture") {
							if (empty($field))
								$field = $value;
							
							if (!empty($field)) {
								$user_login = $user_object->user_login;

								$value_thumb = cimy_get_thumb_path($field);
								$file_thumb = $cuef_upload_path.$user_login."/".cimy_get_thumb_path(basename($field));
								$file_on_server = $cuef_upload_path.$user_login."/".basename($field);

								echo "\n\t\t";
								if (is_file($file_thumb)) {
									echo '<a target="_blank" href="'.esc_attr($field).'"><img src="'.esc_attr($value_thumb).'" alt="picture" /></a><br />';
									echo "\n\t\t";
								}
								else if (is_file($file_on_server)) {
									echo '<img src="'.esc_attr($field).'" alt="picture" /><br />';
									echo "\n\t\t";
								}
							}
						}
						else if ($type == "file") {
							echo '<a target="_blank" href="'.esc_attr($field).'">';
							echo esc_html(basename($field));
							echo '</a>';
						}
						else if ($type == "registration-date") {
							$field = cimy_get_registration_date($user_object->ID, $field);
							if (isset($rules['equal_to']))
								$registration_date = cimy_get_formatted_date($field, $rules['equal_to']);
							else
								$registration_date = cimy_get_formatted_date($field);
								
							echo esc_html($registration_date);
						}
						else
							echo cimy_uef_sanitize_content($field);

						echo "</div>";
						if ((!in_array($type, $cimy_uef_file_types)) && ($type != "radio") && ($type != "registration-date") && (current_user_can('edit_user', $user_object->ID)))
							echo "[<a href=\"#\" onclick=\"editExtraField(".$user_object->ID.", '".$name_esc_attr."'); return false;\">".__("Change")."</a>]";

						echo "&nbsp;</div></td>";
					}
				}
			}

			echo '</tr>';
		}
	
		?>
		</table>
		<?php $cimy_users_table->display_tablenav("bottom"); ?>
	<?php endif; ?>

	<?php if (!empty($write_input)) : ?>
	<h2><?php _e("Update selected users", $cimy_uef_domain); ?></h2>
	<table class="widefat" cellpadding="3" cellspacing="3">
	<thead>
		<tr class="thead">
			<th class="manage-column column-name" style="" width="10px">&nbsp;</th><th class="manage-column column-name" style="" width="200px"><?php _e("Extra Fields", $cimy_uef_domain); ?></th><th class="manage-column column-name" style=""><?php _e("Value"); ?></th>
		</tr>
	</thead>
	<tfoot>
		<tr class="thead">
			<th class="manage-column column-name" style="" width="10px">&nbsp;</th><th class="manage-column column-name" style="" width="200px"><?php _e("Extra Fields", $cimy_uef_domain); ?></th><th class="manage-column column-name" style=""><?php _e("Value"); ?></th>
		</tr>
	</tfoot>
	<tbody>
	<?php
		foreach ($write_input as $input) {
			echo '<tr>'.$input.'</tr>';
		}
	?>
	</tbody>
	</table>
	<br />
	<input class="button" type="submit" name="submit_new_values" value="<?php _e("Update"); ?>" />
	<?php endif; ?>
	</form>
	</div>
	<?php
}

function cimy_uef_admin_ajax_edit() {
	global $cimy_uef_domain;

	$dropdown_first_item = '--- '.__("select", $cimy_uef_domain).' ---';
?>
<script type='text/javascript'>
/* <![CDATA[ */
	var postL10n = {
		ok: "<?php echo esc_js(__('OK')); ?>",
		cancel: "<?php echo esc_js(__('Cancel')); ?>",
		dropdown_first_item: "<?php echo esc_js($dropdown_first_item); ?>"
	};
	try{convertEntities(postL10n);}catch(e){};
/* ]]> */
</script>
<?php
	wp_enqueue_script("cimy_uef_ajax_new_value");
}

function cimy_uef_admin_ajax_save_ef_new_value() {
	check_ajax_referer('extrafieldnewvalue', 'extrafieldnewvaluenonce');
	$user_id = $_POST["user_id"];
	$field_name = $_POST["field_name"];
	$new_value = $_POST["new_value"];

	$res = set_cimyFieldValue($user_id, $field_name, $new_value);

	if (!empty($res[0]["USER_ID"]))
		echo esc_attr($new_value);
	else
		echo null;

	die;
}

function cimy_save_field($action, $table, $data) {
	global $wpdb, $wpdb_wp_fields_table;
	
	if (!cimy_check_admin("manage_options"))
		return;
	
	if ($table == $wpdb_wp_fields_table) {
		$wp_fields = true;
		$fieldset_sql = "";
	}
	else {
		$wp_fields = false;
		$fieldset = intval($data['fieldset']);
		$fieldset_sql = ", fieldset=".$fieldset;
	}
	
	$name = $wpdb->escape($data['name']);
	$value = $wpdb->escape($data['value']);
	$desc = $wpdb->escape($data['desc']);

	if ($wp_fields)
		$label = $wpdb->escape(__($data['label']));
	else
		$label = $wpdb->escape($data['label']);
	
	$type = $wpdb->escape($data['type']);
	$store_rule = $wpdb->escape(serialize($data['store_rule']));
	$num_fields = $wpdb->escape($data['num_fields']);

	if ($action == "add")
		$sql = "INSERT INTO ".$table." ";
	else if ($action == "edit")
		$sql = "UPDATE ".$table." ";

	$sql.= "SET name='".$name."', value='".$value."', description='".$desc."', label='".$label."', type='".$type."', rules='".$store_rule."'".$fieldset_sql;

	if ($action == "add")
		$sql.= ", F_ORDER=".($num_fields + 1);
	else if ($action == "edit") {
		$field_order = $wpdb->escape($data['field_order']);
		$sql.= " WHERE F_ORDER=".$field_order;
	}

	$wpdb->query($sql);
}
