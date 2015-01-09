<?php

require_once('../wp-load.php');

$file = 'plugin.xml';

function saveAppConfig($appId, $brand, $vehicleName) {
	global $wpdb;
	$table_name = "AppConfig";
	$wpdb->show_errors();
	$result = $wpdb->query($wpdb->prepare(
	"
		INSERT INTO $table_name
		(appId, brand, vehicleName) 
		VALUES(%d, %s, %s)
	", $appId, $brand, $vehicleName
	));
	return $result;
}

function savePluginConfig($ecuId, $name, $appConfig_id) {
	global $wpdb;
	$table_name = "PluginConfig";
	$wpdb->show_errors();
	$vin = getVIN();
	$result = $wpdb->query($wpdb->prepare(
	"
		INSERT INTO $table_name
		(ecuId, name, appConfig_id) 
		VALUES(%d, %s, %d)
	", $ecuId, $name, $appConfig_id
	));
	return $result;
}

function savePluginPortConfig($portName, $pluginConfig_id) {
	global $wpdb;
	$table_name = "PluginPortConfig";
	$wpdb->show_errors();
	$result = $wpdb->query($wpdb->prepare(
	"
		INSERT INTO $table_name
		(name, pluginConfig_id) 
		VALUES(%s, %d)
	", $portName, $pluginConfig_id
	));
	return $result;
}

function savePluginLinkConfig($fromPortStr, $toPortStr, $remotePortStr, $pluginConfig_id) {
	global $wpdb;
	$table_name = "PluginLinkConfig";
	$wpdb->show_errors();
	$result = $wpdb->query($wpdb->prepare(
	"
		INSERT INTO $table_name
		(fromStr, toStr, remote, pluginConfig_id) 
		VALUES(%s, %s, %s, %d)
	", $fromPortStr, $toPortStr, $remotePortStr, $pluginConfig_id
	));
	return $result;
}

function getAppConfigId($appId, $brand, $vehicleName) {
	global $wpdb;
	$table_name = "AppConfig";
	$wpdb->show_errors();
	$result = $wpdb->get_var($wpdb->prepare("SELECT id FROM $table_name WHERE appId = %d and brand = %s and vehicleName = %s", $appId, $brand, $vehicleName));
	return $result;
}

function getPluginConfigId($ecuId, $pluginName, $appConfig_id) {
	global $wpdb;
	$table_name = "PluginConfig";
	$wpdb->show_errors();
	$result = $wpdb->get_var($wpdb->prepare("SELECT id FROM $table_name WHERE ecuId = %d and name = %s and appConfig_id = %d", $ecuId, $pluginName, $appConfig_id));
	return $result;
}

function getVehicleLinks($vehicleStr, $brandStr, $fromEcuId, $toEcuId) {
	global $wpdb;
	$table_name = "VehicleConfig";
	$wpdb->show_errors();
	$vehicleConfigId = $wpdb->get_var($wpdb->prepare("SELECT id FROM $table_name WHERE name = %s and brand = %s", $ecuId, $vehicleStr, $brandStr));
	
	$table_name = "Link";
	$result= $wpdb->get_results($wpdb->prepare("SELECT fromPortId, toPortId FROM $table_name WHERE fromEcuId = %d and toEcuId = %d and vehicleConfig_id= %d", $fromEcuId, $toEcuId, $vehicleConfigId));
	return $result;

}

function parsePluginConfiguration($appId, $file) {
	if (file_exists($file)) {
		$portNameHashMap = array();
		$portName2EcuIdHashMap = array();
		$portName2PluginConfigIdHashMap = array();

		$xml = simplexml_load_file($file);
		
		if(isset($xml->vehicleName)) {
			$vehicleName = $xml->vehicleName;
		}
		
		if(isset($xml->brand)) {
			$brand = $xml->brand;
		}
		
		// Save APP config
		saveAppConfig($appId, $brand, $vehicleName);
		
		$appConfigId = getAppConfigId($appId, $brand, $vehicleName);

		if(isset($xml->plugins)) {
			$plugins = $xml->plugins;
			$pluginElement = $plugins->children();
			$pluginElementSize = count($pluginElement);

			if($pluginElementSize > 0 ) {
				foreach ($pluginElement as $entry) {
					$pluginName = $entry->name;
					
					$ecuId = $entry->ecu;
					
					// Save PlugIn config
					savePluginConfig($ecuId, $pluginName, $appConfigId);
					
					$ports = $entry->ports;
					if(isset($ports)) {
						$pluginConfigId = getPluginConfigId($ecuId, $pluginName, $appConfigId); 
						
						$portsElement = $ports->children();
						$portsElementSize = count($portsElement);
						if($portsElementSize > 0) {
							foreach($portsElement as $portElement) {
								$portName = (string) $portElement->name;

								// Save temperarily port name that will be used during link setup
								$portNameHashMap[] = $portName;
								$portName2EcuIdHashMap[$portName] = $ecuId;
								$portName2PluginConfigIdHashMap[$portName] = $pluginConfigId;
 
								// Save PlugIn Port config
								savePluginPortConfig($portName, $pluginConfigId);
							}
						}
						
						
					}
					
				}
			}
			
		}
		
		if(isset($xml->links)) {
			$links = $xml->links;
			$linksElement = $links->children();
			$linksElementSize = count($linksElement);
			if($linksElementSize > 0) {
				foreach($linksElement as $link) {
					$from = (string) $link->from;
					$to = (string) $link->to;

					// Check fromPort type
					$fromType = in_array($from, $portNameHashMap);				
					// Check toPort type
					$toType = in_array($to, $portNameHashMap);

					if ($fromType && $toType) {
						$fromEcuId = $portName2EcuIdHashMap[$from];
						$toEcuId = $portName2EcuIdHashMap[$to];
						if(strcmp($fromEcuId, $toEcuId) == 0) {
							// same ECU
							// GlobalVariables.PPORT2PPORT
							$pluginConfigIdStr = $portName2PluginConfigIdHashMap[$from];
							$remotePortStr = "-1";
							savePluginLinkConfig($from, $to, $remotePortStr, intval($pluginConfigIdStr)); 					
						} else {
							// different ECUs
							$rows = getVehicleLinks($vehicleName , $brand, intval($fromEcuId), intval($toEcuId));
							foreach ($rows as $row) {
								$fromVPortId = $row->fromPortId;
								$toVPortId = $row->toPortId;
								// Save Link config
								$pluginConfigIdStr = $portName2PluginConfigIdHashMap[$from];
								savePluginLinkConfig($from, strval($fromVPortId), "-2", intval($pluginConfigIdStr)); 
								savePluginLinkConfig($from, strval($toVPortId), "-3", intval($pluginConfigIdStr)); 
							}
						}
					} else if ($fromType && !$toType) {
						// GlobalVariables.PPORT2VPORT
						$pluginConfigIdStr = $portName2PluginConfigIdHashMap[$from];
						$remotePortStr = "-2";
						savePluginLinkConfig($from, $to, $remotePortStr, intval($pluginConfigIdStr)); 
					} else if (!$fromType && $toType) {
						// GlobalVariables.VPORT2PORT
						$pluginConfigIdStr = $portName2PluginConfigIdHashMap[$to];
						$remotePortStr = "-3";
						savePluginLinkConfig($from, $to, $remotePortStr, intval($pluginConfigIdStr)); 
					} else {
						echo "Error: wrong port type in plugin configuration file.";
						return;
					}
				}
			}

			

		}
		
	} else {
		exit('Failed to open '.$file);
	}
}
	


?>