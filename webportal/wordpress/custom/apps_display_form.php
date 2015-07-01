<?php
/**
 * @package table-display-form-for-fresta
 * @version 1.0
*/

/*
Plugin Name: table display form for fresta
Plugin URI: 
Description: This is used to make a dedicated form for users to browse apps in the server.
Version: 1.0
Author: Ze Ni
Author URI: https://www.sics.se/people/ze-ni
License: GPL
*/

require_once("install_handle.php");

/*  Copyright 2013  table display form for fresta  (email : zeni@sics.se)

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
ini_set("soap.wsdl_cache_enabled", "0");
 	$webServiceAddress = "http://localhost:9990/moped/pws?wsdl";
	
	try {
	  $client = new SoapClient
	    ($webServiceAddress,
	     array('cache_wsdl' => WSDL_CACHE_NONE,
		   'features' => SOAP_SINGLE_ELEMENT_ARRAYS));
	} catch (Exception $ex) {}

function apps_display_form(){
	global $client;
	
	// we should look at the error value, too
	$apps = $client->listApplications();
	$apps = json_decode($apps);
	$apps = $apps->result;

	$username = wp_get_current_user()->user_login;

	$myvin = getVIN();
	if (!$username) {
	  print "<font color='red'>You are not logged in</font>";
	} else {
	  if (!$myvin) {
	    print "<font color='red'>No active vehicle</font>";
	  } else {
	    print "Active vehicle: $myvin";
	  }
	}

	$car = $client->infoVehicle($myvin);
	$car = json_decode($car);
	$car = $car->result;

	$iapps = $client->listInstalledApps();
	$iapps = json_decode($iapps);
	$iapps = $iapps->result;

	$ia = array();
	$istatea = array();
	$ecua = array();
	foreach ($iapps as $iapp) {
	  $nam = $iapp->name;
	  $vin = $iapp->vin;
	  if ($iapp->vin == $myvin) {
	    $ia[$nam] = 1;
	    $istatea[$nam] = $iapp->installationState;
	    $ecua[$nam] = "ecu".$iapp->ecu;
	  }
	}

	?>
		
	<table cellpadding="0" cellspacing="0" border="0" class="display" id="example">
		<thead>
			<tr>
				<th width="18%">Application</th>
				<th width="15%">Publisher</th>
				<th width="13%">Version</th>
				<th width="10%">Type</th>
				<th width="12%">Install</th>
				<th width="17%">Info</th>
				<th width="15%">Uninstall</th>
			</tr>
		</thead>
		<tbody>
			<?php
			$app_nr = 0;
			foreach ($apps as $app) {
				echo "<tr>\r\n";
				echo "<td>$app->name</td>\r\n";
				echo "<td>$app->publisher</td>\r\n";
				echo "<td>$app->version</td>\r\n";
				echo "<td>$app->vehicleConfig</td>\r\n";
				echo "<td><form method=\"post\"><input type='hidden' name='app_row' value='$app_nr'/><input type='hidden' name='app_id' value='$app->id'/><input name='Jdk_install' type='image' src='wordpress/custom/images/install.png' alt='Install'/></form></td>\r\n";
				$i = $ia[$app->name];
				if (!$i) {
				  if ($app->vehicleConfig == $car->type)
				    $i = 0;
				}
				$id = $app->id;
				$state = $app->state;
				$istate = $istatea[$app->name];
				$ecus = "";
				foreach ($app->ecuList as $ecu) {
				  $ecus = $ecus." ecu$ecu";
				}
				echo "<td>$i $id $state $istate $ecus</td>\r\n";
				echo "<td><form method=\"post\"><input type='hidden' name='app_row' value='$app_nr'/><input type='hidden' name='app_id' value='$app->id'/><input name='Jdk_uninstall' type='image' src='wordpress/custom/images/uninstall.png' alt='Uninstall'/></form></td>\r\n";
				echo "</tr>\r\n";
				$app_nr++;
			}
			
			?>
	<!-- 		<tr> -->
	<!-- 			<td colspan=\"6\" class=\"dataTables_empty\">Loading data from server...</td> -->
	<!-- 		</tr> -->
		</tbody>
		<tfoot>
			<tr>
				<th>Application</th>
				<th>Publisher</th>
				<th>Version</th>
				<th>Type</th>
				<th>Install</th>
				<th>Info</th>
				<th>Uninstall</th>
			</tr>
		</tfoot>
	</table>
	<div id="feedback"></div>
	
	<?php 
	if (isset($_POST['Jdk_uninstall_x'])) {
	  $myvin = getVIN();
	  if (!$myvin) {
	    print "<font color='red'>No active vehicle: add one to your list of cars, and mark it as active</font>";
	  } else {
	    $ret = invoke_uninstall_webservice($_POST['app_id']);
	  }
	}
	if (isset($_POST['Jdk_install_x'])) {
	  $myvin = getVIN();
	  if (!$myvin) {
	    print "<font color='red'>No active vehicle: add one to your list of cars, and mark it as active</font>";
	  } else {
	    $ret = invoke_install_webservice($_POST['app_id']);
	  }
	}
}
add_shortcode('apps_display_form', 'apps_display_form');
?>
