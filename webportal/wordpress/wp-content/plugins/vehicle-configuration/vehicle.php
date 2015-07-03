<?php
/**
 * @package vehicle-configuration-for-fresta
 * @version 1.0
*/

/*
Plugin Name: vehicle configuration for fresta
Plugin URI: 
Description: This is used for engineers to configure vehicles.
Version: 1.0
Author: Ze Ni
Author URI: https://www.sics.se/people/ze-ni
License: GPL
*/

/*  Copyright 2013  vehicle configuration for fresta  (email : zeni@sics.se)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License, version 2, as 
    published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

//require_once('../wp-load.php');

   ini_set("soap.wsdl_cache_enabled", "0");
 	$webServiceAddress = "http://localhost:9990/moped/pws?wsdl";

try {
	  $client = new SoapClient
	    ($webServiceAddress,
	     array('cache_wsdl' => WSDL_CACHE_NONE,
		   'features' => SOAP_SINGLE_ELEMENT_ARRAYS));

} catch (SoapFault $fault) {
  print "<font color='red'>ERROR: the trusted server can not be contacted</font>";
  }


// create tables with plugins
function vehicle_install() {
	require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
	
	add_option( "vehicle_configuration_version", "1.0" );
}

register_activation_hook( __FILE__, 'vehicle_install' );

// add css
function vehicle_configuration_style()  
{  
    // Register the style like this for a plugin:  
   wp_register_style( 'vehicle_configuration_style', plugins_url( '/css/vehicle_admin.css', __FILE__ ), array(), '20130103', 'all' );  
    // or  
    // Register the style like this for a theme:  
    //wp_register_style( 'ajaxupload-style', get_template_directory_uri() . '/css/baseTheme/style.css', array(), '20120208', 'all' );  
  
    // For either a plugin or a theme, you can then enqueue the style:  
    wp_enqueue_style( 'vehicle_configuration_style' );  
}  
add_action( 'wp_enqueue_scripts', 'vehicle_configuration_style' );

// add scripts
function vehicle_operation_with_jquery()  
{  
    wp_register_script( 'vehicle_operation_with_jquery', plugins_url( '/js/vehicle_operation.js', __FILE__ ), array( 'jquery' ) );  
    wp_enqueue_script( 'vehicle_operation_with_jquery' );  
}  
add_action( 'wp_enqueue_scripts', 'vehicle_operation_with_jquery' );




// Arndt: These two function are probably completely obsolete
// internal APIs
function add_new_configuration($ecu_id, $name, $description, $function, $manufactory, $vehicle_id) {
	global $wpdb;
	$wpdb->show_errors();
	$table_name = "Ecu";
	$wpdb->query($wpdb->prepare(
	"
		INSERT INTO $table_name
		(description, ecuID, function, manufactory, name, vehicle_id) 
		VALUES(%s, %d, %s, %s, %s, %d)
	",$description, intval($ecu_id), $function, $manufactory, $name, intval($vehicle_id)));
}

function fetch_configurations() {
	global $wpdb;
	$vehicleId = 0;
	$wpdb->show_errors();
	$table_name = "User_vehicle_association";
	$user = wp_get_current_user();
	$user_id = $user->ID;
	
	$result = $wpdb->get_row(
		$wpdb->prepare(
			"
			SELECT * FROM $table_name WHERE userID = %d and defaultVehicle = 1", intval($user_id)), ARRAY_A);
	if($result == NULL) {
		echo "Select a default vehicle at first.";
		return NULL;
	} else {
		$vehicleId = $result['vehicleID'];
		$table_name = "Ecu";
		$result = $wpdb->get_results(
		$wpdb->prepare(
			"
			SELECT * FROM $table_name WHERE vehicle_id = %d", $vehicleId));
		return $result;
	}
}





function reset_default_vehicle($vin) {
  global $client;

  $userId = wp_get_current_user()->ID;

  return $client->setUserVehicleAssociationActive($userId, $vin, true);
}

function reset_not_default_vehicle($vin) {
  global $client;

  $userId = wp_get_current_user()->ID;

  return $client->setUserVehicleAssociationActive($userId, $vin, false);
}

// Arndt: obsolete
function remove_configuration($id) {
	global $wpdb;
	$table_name = "Ecu";
	$wpdb->query(
		$wpdb->prepare(
			"
			DELETE FROM $table_name
			WHERE id = %d", intval($id)));
}

function restore_configuration($id)  {
	// invoke webservice of restore ecu
	// Invoke Web Services
  global $client;
	try  
	{  
		$vin = getVIN();
		$param = array('arg0' => $vin , 'arg1' => $id);  
		$ret = $client->restoreEcu($param);  
		if($ret->return == true) 
		{
			return "<br/><font color='red'>Success to restore ECU $id</font><br />";
		} else {
			return "<br/><font color='red'>Error. Fail to restore ECU $id</font><br />";
		}
		//print_r($ret);  
	} catch (SoapFault $exception) {  
		print $exception;  
	}  
}

function add_vehicle($vehicle_name, $vehicle_vin, $vehicle_type) {
  global $client;

  return $client->addVehicle($vehicle_name, $vehicle_vin, $vehicle_type);
}

function fetch_vehicles() {
  global $client;

  $myrows = $client->listVehicles();
  return json_decode($myrows)->result;
}

function fetch_my_vehicles() {
  global $client;

  $userId = wp_get_current_user()->ID;
  $myrows = $client->listUserVehicles($userId);
  return json_decode($myrows);
}

function fetch_vehicleTypes() {
  global $client;

  $myrows = $client->listVehicleConfigs();
  return json_decode($myrows);
}

function remove_vehicle($vin) {
  global $client;

  return $client->deleteVehicle($vin);
}

function add_user_vehicle_association($userID, $vin) {
  global $client;

  $r = $client->addUserVehicleAssociation($userID, $vin, false);
  return $r;
}

function fetch_associations() {
  global $client;

  $userId = wp_get_current_user()->ID;
  $myrows = $client->listUserVehicleAssociations($userId);
  return json_decode($myrows)->result;
}

function remove_association($vin) {
  global $client;

  $user = wp_get_current_user();
  $user_id = $user->ID;
  $r = $client->deleteUserVehicleAssociation($user_id, $vin);
  return $r;
}

// append setting menu
add_action('admin_menu', 'user_vehicle_menu');
function user_vehicle_menu() {
	add_users_page('User Vehicle Association', 'User Vehicle Association', 'read', 'user-vehicle-association', 'operate_user_vehicle_association');
}

function operate_user_vehicle_association() {
?>
	<h2>User Vehicle Association</h2>
	<h3>Bind a new vehicle</h3>
	<form method="post" >
	<p>
		<label for="vehicle" required">Select a Vehicle</label><br/>
		<select name="selected_vehicle">
			<?php
				$rows = fetch_vehicles();
				foreach ($rows as $row) {
                                        $vin = $row->vin;
					$output = "<option value=$vin>$vin</option>";
					echo $output;
				}
			?>
		</select>
	</p>
	<input type="submit" name="add_vehicle_to_association" value="Add"/>
</form>
<h3>Browse User Vehicle Association</h3>
<form method="post">
<table>
	<thead>
		<tr>
			<th width="50%">Vehicle Name</th>
			<th width="30%">Select default vehicle</th>
			<th width="20%">Remove</th>
			<th width="0%"></th>
		</tr>
	</thead>
	<tbody>
		<?php
			$myrows = fetch_associations();
			$oldDefaultVehicle = 0;
			foreach ($myrows as $myrow) {
				$vin = $myrow->vin;
				$isDefault = $myrow->active;
				//$output = "<tr><td align='center'>$vehcile_name</td>";
				$output = "<tr><td align='center'>$vin</td>";
				/*$link_arr_paras = array('action' => 'edit', 'configid' => $myrow->id);
				$link = add_query_arg($link_arr_paras);
				$edit = "<td align='center'><a href=$link alt='Edit Vehicle Configuration'><img src='wp-content/plugins/vehicle-configuration/images/edit.png' /></a></td>";
				$output .= $edit;*/
				if($isDefault == true) {
					$output .= "<td align='center'><input type='radio' name='defaultVehicle' value='$myrow->vin' checked /></td>";
					$oldDefaultVehicle = $myrow->vin;
				} else {
					$output .= "<td align='center'><input type=radio name=defaultVehicle value=$myrow->vin /></td>";
				}
				$link_arr_paras = array('actionForAssociation' => 'remove', 'vin' => $myrow->vin);
				$link = add_query_arg($link_arr_paras);
				$remove = "<td align='center'><a href=$link alt='Remove Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/remove.png' /></a></td>";
				$output .= $remove;
				$output .= "</tr>";		
				echo $output;
			}
			echo "<input type=hidden name=oldDefaultVehicle value=$oldDefaultVehicle />";
			
		?>
	</tbody>
	<tfoot>
		<tr>
			<th>Vehicle Name</th>
			<th>Select default vehicle</th>
			<th>Remove</th>
		</tr>
	</tfoot>
</table>

<input type="submit" name="bind_default_vehicle" value="Bind Default Vehicle"/>
</form>
<?php
}

add_action('admin_menu', 'vehicle_configuration_menu');
function vehicle_configuration_menu() {
	//add_options_page('Vehicle Configuration', 'Vehicle Configuration', 'manage_options', 'vehicle-configuration-identifer', 'change_vehicle_configuration');
	add_users_page('Vehicle Configuration', 'Vehicle Configuration', 'read', 'vehicle-configuration-identifer', 'change_vehicle_configuration');
}

function change_vehicle_configuration() {
	/*if (!current_user_can('manage_options')) {
		wp_die(__('You do not have sufficient permissions to access this page.'));
	}
	*/
?>
<h2>Vehicle Configuration</h2>
<h3>Browse Vehicle Configuration</h3>
<table>
	<thead>
		<tr>
			<th width="8%">Name</th>
			<th width="8%">ECU Reference</th>
			<th width="20%">Description</th>
			<th width="15%">Function</th>
			<th width="8%">Manufactory</th>
			<th width="3%">Remove</th>
			<th width="3%">Restore</th>
			<th width="35%"></th>
		</tr>
	</thead>
	<tbody>
		<?php
			$myrows = fetch_configurations();
			if($myrows != NULL)
			{
				foreach ($myrows as $myrow) {
					$output = "<tr><td align='center'>$myrow->name</td><td align='center'>$myrow->ecuId</td><td align='center'>$myrow->description</td><td align='center'>$myrow->function</td><td align='center'>$myrow->manufactory</td>";
					/*$link_arr_paras = array('action' => 'edit', 'configid' => $myrow->id);
					$link = add_query_arg($link_arr_paras);
					$edit = "<td align='center'><a href=$link alt='Edit Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/edit.png' /></a></td>";
					$output .= $edit;*/
					$link_arr_paras = array('action' => 'remove', 'configid' => $myrow->id);
					$link = add_query_arg($link_arr_paras);
					$remove = "<td align='center'><a href=$link alt='Remove Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/remove.png' /></a></td>";
					$output .= $remove;
					$link_arr_paras = array('action' => 'restore', 'configid' => $myrow->ecuId);
					$link = add_query_arg($link_arr_paras);
					$restore = "<td align='center'><a href=$link alt='Restore Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/restore.png' /></a></td>";
					$output .= $restore;
					$output .= "</tr>";
					echo $output;
				}	
			}
			
		?>
	</tbody>
	<tfoot>
		<tr>
			<th>Name</th>
			<th>ECU Reference</th>
			<th>Description</th>
			<th>Function</th>
			<th>Manufactory</th>
			<th>Remove</th>
			<th>Restore</th>
		</tr>
	</tfoot>
</table>
<?php
}

function vehicle_build_form(){
?>
	
	<h3>Add a new Vehicle</h3>
	<form method="post" action="" id="vehicle_config_form">
		<p>
			<label for="vehicle_name" required>Vehicle Name</label><br/>
			<input type="text" name="vehicle_name" value=""  />
		</p>
		<p>
			<label for="vehicle_vin" required>VIN</label><br/>
			<input type="text" name="vehicle_vin" value=""  />
		</p>
		<p>
			<label for="vehicle_type" required>Vehicle Type</label><br/>
			<select name="vehicle_type">
				<option value="-1" selected>Select Vehicle Type: </option>
				<?php
					$vehicleTypes =  fetch_vehicleTypes();
					foreach ($vehicleTypes->result as $vehicleType) {
						//$id = $vehicleType->id;
						$name = $vehicleType->name;
						echo "<option value='".$name."'>$name</option>";
					}
				?>
			</select>
		</p>
		<input type="submit" name="add_vehicle" value="Submit">
	</form>
	
	<h3>Browse Vehicles</h3>
	<table>
		<thead>
			<tr>
				<th width="8%">Name</th>
				<th width="8%">VIN</th>
				<th width="3%">Remove</th>
				<th width="81%"></th>
			</tr>
		</thead>
		<tbody>
			<?php
				$myrows = fetch_my_vehicles();
				foreach ($myrows->result as $myrow) {
					$output = "<tr><td align='center'>$myrow->name</td><td align='center'>$myrow->vin</td>";
					/*$link_arr_paras = array('action' => 'edit', 'configid' => $myrow->id);
					$link = add_query_arg($link_arr_paras);
					$edit = "<td align='center'><a href=$link alt='Edit Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/edit.png' /></a></td>";
					$output .= $edit;*/
					$link_arr_paras = array('actionForVehicle' => 'remove', 'vehicleid' => $myrow->vin);
					$link = add_query_arg($link_arr_paras);
					$remove = "<td align='center'><a href=$link alt='Remove Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/remove.png' /></a></td>";
					$output .= $remove;
					$output .= "</tr>";
					echo $output;
				}	
			?>
		</tbody>
		<tfoot>
			<tr>
				<th>Name</th>
				<th>VIN</th>
				<th>Remove</th>
			</tr>
		</tfoot>
	</table>
<?php	
}
add_shortcode('vehicle_build_form', 'vehicle_build_form');

function vehicle_configuration_form() {
?>
<h2>Vehicle Configuration</h2>
<h3>Add a new Vehicle Configuration</h3>
<form method="post" >
	<p>
		<label for="vehicle" required">Vehicle</label><br/>
		<select id="vehicle" name="vehicle">
			<?php
				$myrows = fetch_vehicles();
				foreach ($myrows as $myrow) {
					$output = "<option value=$myrow->id>$myrow->VIN</option>";
					echo $output;
				}
			?>
		</select>
	</p>
	<p>
		<label for="name-field" required>Name</label><br/>
		<input id="name-field" name="name-field" type="text" value=""/>
	</p>
	<p>
		<label for="ecuID" required>ECU Reference</label><br/>
		<input id="ecuID" name="ecuID" type="text" value=""/>
	</p>
	<p>
		<label for="description">Description</label><br/>
		<input id="description" name="description" type="text" value=""/>
	</p>
	<p>
		<label for="function">Function</label><br/>
		<input id="function" name="function" type="text" value=""/>
	</p>
	<p>
		<label for="manufactory">Manufactory</label><br/>
		<input id="manufactory" name="manufactory" type="text" value=""/>
	</p>
	<input type="submit" name="Add_configuration" value="Add (ignored)"/>
</form>
<h3>Browse Vehicle Configuration</h3>
<table>
	<thead>
		<tr>
			<th width="10%">Name</th>
			<th width="10%">ECU</th>
			<th width="35%">Description</th>
			<th width="20%">Function</th>
			<th width="20%">Manufactory</th>
			<th width="5%">Remove</th>

		</tr>
	</thead>
	<tbody>
		<?php
			$myrows = fetch_configurations();
			if($myrows != NULL)
			{
				foreach ($myrows as $myrow) {
					$output = "<tr><td align='center'>$myrow->name</td><td align='center'>$myrow->ecuId</td><td align='center'>$myrow->description</td><td align='center'>$myrow->function</td><td align='center'>$myrow->manufactory</td>";
					/*$link_arr_paras = array('action' => 'edit', 'configid' => $myrow->id);
					$link = add_query_arg($link_arr_paras);
					$edit = "<td align='center'><a href=$link alt='Edit Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/edit.png' /></a></td>";
					$output .= $edit;*/
					$link_arr_paras = array('action' => 'remove', 'configid' => $myrow->id);
					$link = add_query_arg($link_arr_paras);
					$remove = "<td align='center'><a href=$link alt='Remove Vehicle Configuration'><img src='/wp/wp-content/plugins/vehicle-configuration/images/remove.png' /></a></td>";
					$output .= $remove;
					$output .= "</tr>";
					echo $output;
				}	
			}
			
		?>
	</tbody>
	<tfoot>
		<tr>
			<th>Name</th>
			<th>ECU</th>
			<th>Description</th>
			<th>Function</th>
			<th>Manufactory</th>
			<th>Remove</th>
		</tr>
	</tfoot>
</table>
<?php
}
add_shortcode('operate_user_vehicle_association', 'operate_user_vehicle_association');

add_shortcode('vehicle_configuration_form', 'vehicle_configuration_form');

if (isset($_POST['Add_configuration'])) {
  echo "<font color='red'>This button has no function</font>";
} else if( isset ( $_GET['action'] ) ) {
		// handle action request - show form
		switch( $_GET['action'] ) {
			case 'remove' :
				if ( isset( $_GET['configid'] ) ) {
					remove_configuration( $_GET['configid'] );
				}
				break;
			case 'restore' :
				if ( isset( $_GET['configid'] ) ) {
					echo restore_configuration( $_GET['configid'] );
				}
				break;
		}
} else if ( isset( $_POST['add_vehicle'] )) {
	$vehicle_name = $_POST['vehicle_name'];
	$vehicle_vin = $_POST['vehicle_vin'];
	$vehicle_type= $_POST['vehicle_type'];
	if($vehicle_type == "-1")
		echo "<font color='red'>Please select one vehicle type</font>";
	else {
	  if (!add_vehicle($vehicle_name, $vehicle_vin, $vehicle_type)) {
	    echo "<font color='red'>Creating failed</font>";
	  }
	}
} else if( isset ( $_GET['actionForVehicle'] ) ) {
		// handle action request - show form
		switch( $_GET['actionForVehicle'] ) {
			case 'remove' :
				if ( isset( $_GET['vehicleid'] ) ) {
				  if (!remove_vehicle( $_GET['vehicleid'] )) {
				    echo "<font color='red'>Removal failed - maybe the vehicle is in a user association</font>";
				  }
				}
				break;
		}
} else if ( isset( $_POST['add_vehicle_to_association'])) {
	$user = wp_get_current_user();
	$vehicle_vin = $_POST['selected_vehicle'];
	add_user_vehicle_association($user->ID, $vehicle_vin);
} else if ( isset( $_POST['bind_default_vehicle'])) {
	$oldDefaultVehicleRowId = $_POST['oldDefaultVehicle'];
	reset_not_default_vehicle($oldDefaultVehicleRowId);
	$newDefaultVehileRowId = $_POST['defaultVehicle'];
	reset_default_vehicle($newDefaultVehileRowId);
} else if ( isset( $_GET['actionForAssociation']) ) {
	switch( $_GET['actionForAssociation'] ) {
			case 'remove' :
				if ( isset( $_GET['vin'] ) ) {
					remove_association( $_GET['vin'] );
				}
				break;
		}
}
?>
