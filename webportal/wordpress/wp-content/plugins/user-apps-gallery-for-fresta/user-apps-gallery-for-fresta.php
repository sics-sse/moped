<?php
/**
 * @package user-apps-gallery-for-fresta
 * @version 1.0
*/

/*
Plugin Name: user apps gallery for fresta
Plugin URI: 
Description: This is used for users to illustrate their installed apps.
Version: 1.0
Author: Ze Ni
Author URI: https://www.sics.se/people/ze-ni
License: GPL
*/

/*  Copyright 2013  user apps gallery for fresta  (email : zeni@sics.se)

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

require_once(ABSPATH . "wp-includes/pluggable.php");
require_once(ABSPATH . "custom/globalVariables.php");

function get_app_ids() {
	//$user = wp_get_current_user();
	//$field_name = "INSTALLED_APPS";
	$apps = getInstalledApps(getVIN());
	if (strpos($apps, ',') !== false) {
		$apps_array = explode(',', $apps);
	} else {
		$apps_array = array($apps);
	}
	return $apps_array;
}

function getInstalledApps($vin) {
	global $wpdb;
	$myvalue = $wpdb->get_var($wpdb->prepare("select INSTALLED_APPS from Vehicle where VIN = %s", $vin));
	return $myvalue;
}

function removeInstalledApp($apps) {
	global $wpdb;
	$table_name = "Vehicle";
	$wpdb->show_errors();
	$vin = getVIN();
	$wpdb->query($wpdb->prepare(
		"
			UPDATE $table_name SET INSTALLED_APPS = %s WHERE VIN = %s
		", $apps, $vin 
	));
}

function get_info_by_app_id($app_id) {
	global $wpdb;
	$myrow = $wpdb->get_row($wpdb->prepare("select * from Application where applicationID = %d", $app_id), ARRAY_A);
	return $myrow;
}

function get_upgrade_flag_by_app_id($app_id) {
	global $wpdb;
	$myvalue = $wpdb->get_var($wpdb->prepare("select hasNewVersion from Application where applicationID = %d", $app_id));
	return $myvalue;
}

function get_newest_app_id($app_name) {
	global $wpdb;
	$wpdb->show_errors();
	$myvalue = $wpdb->get_var($wpdb->prepare("select MAX(applicationId) from Application where applicationName = %s", $app_name));
	return $myvalue;
}

function upgrade_app_by_app_id($app_id, $app_name) {
	// Invoke Web Services
	$webServiceAddress = getWebServiceAddress();
	//ini_set("soap.wsdl_cache_enabled", "0");   
	$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));  
	try  
	{  
		$vin = getVIN();
		$param = array('arg0' => $vin , 'arg1' => $app_id);  
		$ret = $client->upgrade($param);  
		//print_r($ret);  
	} catch (SoapFault $exception) {  
		print $exception;  
	}  
	// Synchronize DB
	//$user = wp_get_current_user();
	//$field_name = "INSTALLED_APPS";
	//$apps = get_cimyFieldValue(getVIN(), $field_name);
	/*$apps = getInstalledApps(getVIN());
	if (strpos($apps, ',') !== false) {
		$apps_array = explode(',', $apps);
	} else {
		$apps_array = array($apps);
	}
	foreach($apps_array as $key => $value) {
		if ($value == $app_id) {
			unset($apps_array[$key]);
			break;
		}
	}
	$new_app_id = get_newest_app_id($app_name);
	$apps_array[] = $new_app_id; 
	$size = count($apps_array);
	if( $size == 1) {
		$result = $new_app_id;
	} else {
		$result = implode(',', $apps_array);
	}
	removeInstalledApp($result);
	*/
}

function unintall_app_by_app_id($app_id) {
	// Invoke Web Services
	$webServiceAddress = getWebServiceAddress();
	ini_set("soap.wsdl_cache_enabled", "0");   
	$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));  
	try  
	{  
		$vin = getVIN();
		$param = array('arg0' => $vin , 'arg1' => $app_id);  
		$ret = $client->uninstall($param);  
		//print_r($ret);
		if($ret->return) {
			echo "<font color='green'>Application is uninstalling...</font>";
		} else {
			echo "<font color='red'>Application failed uninstallation</font>";
		}
	} catch (SoapFault $exception) {  
		print $exception;  
	}  
	// Synchronize DB
	//$field_name = "INSTALLED_APPS";
	//$apps = get_cimyFieldValue(getVIN(), $field_name);
/*	$apps = getInstalledApps(getVIN());
	if (strpos($apps, ',') !== false) {
		$apps_array = explode(',', $apps);
	} else {
		$apps_array = array($apps);
	}
	$removed_array = array_diff($apps_array, array($app_id));
	$size = count($removed_array);
	if($size == 0) {
		$result = "";
	} else if ($size == 1) {
		$result = $removed_array[0];
	} else {
		$result = implode(',', $removed_array);
	}
	removeInstalledApp($result);
	*/
}

function publish_app_by_app_id($app_id) {
	// Invoke Web Services
	$webServiceAddress = getWebServiceAddress();
	ini_set("soap.wsdl_cache_enabled", "0");   
	$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));  
	try  
	{
		$param = array('arg0' => $app_id);  
		$ret = $client->publish($param);  
		//print_r($ret);  
		if($ret ==  "true") {
			print "publish it successfully.";
		} else {
			print "Fail to publish";
		}
	} catch (SoapFault $exception) {  
		print $exception;  
	}  
}

add_action('admin_menu', 'my_apps_menu');

function my_apps_menu() {
	add_users_page('My Apps', 'My Apps', 'read', 'my-apps-identifer', 'show_installed_apps');
}

function show_installed_apps() {
	if (!current_user_can('read')) {
		wp_die(__('You do not have sufficient permissions to access this page.'));
	}
	$apps = get_app_ids();
?>

<table>
<?php
	$count = count($apps) - 1;
	$nRows = floor($count / 5) + 1;
	$minNColumn = count($apps) % 5;
	for ($i=1;$i<=$nRows;$i++) {
		echo "<tr>";
			if ( $i != $nRows ) {
				for ($j=1;$j<=5;$j++) {
					echo "<td>";
					echo "<form method='post' action=''>";
					$index = $i * $j - 1;
					$app_id = $apps[$index];
					$result = get_info_by_app_id($app_id);
					echo "App Name: ".$result['applicationName']."<br />";
					echo "Publisher: ".$result['publisher']."<br />";
					echo "Version: ".$result['version']."<br />";
					//echo "<input type='image' src='../custom/images/publish.png' alt='Publish' name='publish' value='$app_id' />";
					echo "<input type='image' src='../custom/images/trash.png' alt='Uninstall' name='uninstall' value='$app_id' />";
					$new_app_id = get_upgrade_flag_by_app_id($app_id);
					if( $new_app_id != 0 ) {
						echo "<input type='image' src='../custom/images/upgrade.png' alt='Upgrade' name='upgrade' value='$app_id' />";
					}			
					echo "</form>";
					echo "</td>";
				}
			}
			else {
				for ($k=1;$k<=$minNColumn;$k++) {
					echo "<td>";
					echo "<form method='post' action=''>";
					$index = $i * $k - 1;
					$app_id = $apps[$index];
					$result = get_info_by_app_id($app_id);
					$app_name = $result['applicationName'];
					echo "App Name: ".$app_name."<br />";
					echo "Publisher: ".$result['publisher']."<br />";
					echo "Version: ".$result['version']."<br />";
					//echo "<input type='image' src='../custom/images/publish.png' alt='Publish' name='publish' value='$app_id' />";					
					echo "<input type='image' src='../custom/images/trash.png' alt='Uninstall' name='uninstall' value='$app_id' />";
					$new_app_id = get_upgrade_flag_by_app_id($app_id);
					if( $new_app_id != 0 ) {
						echo "<input type='hidden' name='app_name' value='$app_name' />";
						echo "<input type='image' src='../custom/images/upgrade.png' alt='Upgrade' name='upgrade' value='$app_id' />";
					}			
					echo "</form>";
					echo "</td>";
				}		
			}
		echo "</tr>";
	}
?>

</table>

<?php
}

if(isset($_POST['uninstall'])) {
	$app_id = $_POST['uninstall'];
	unintall_app_by_app_id($app_id);
} else if(isset($_POST['upgrade'])) {
	$app_id = $_POST['upgrade'];
	$app_name = $_POST['app_name'];
	upgrade_app_by_app_id($app_id, $app_name);
} /*else if(isset($_POST['publish'])) {
	$app_id = $_POST['publish'];
	
	publish_app_by_app_id($app_id);
}*/
?>