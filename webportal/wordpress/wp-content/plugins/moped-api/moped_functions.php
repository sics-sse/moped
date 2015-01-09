<?php

/********************************************************************************/
/*								Moped functions									*/
/********************************************************************************/
try {
	$client = new SoapClient("http://localhost:9990/moped/pws?wsdl",
				array('cache_wsdl' => WSDL_CACHE_NONE));
} catch (Exception $ex) {}

function install_plugin() {
	global $client; 
	
	echo "<br/><font color='green'>Installation nr 5...\r\n</font><br />";
	$client->__soapCall("install", array('vin' => '20UYA31581L000000', 
										 'id' => 24)); 
	echo "Installed";
}

function register_vehicle($name, $vin, $type) {
	global $client;
	
	$client->__soapCall("register_vehicle", 
						array('name' => $name,
							  'vin' => $vin,
							  'type' => $type));
}

function register_vehicle_configuration() {
	global $client;
	
	if ($_FILES['configFile']['name']) {
		echo "file found; ";
	}
	else {
		echo "file not found; ";
	}
	
	move_uploaded_file($_FILES['configFile']['tmp_name'], '/tmp/vConf.xml'); // Needed for some strange reason (at least right now)
	$client->__soapCall("parseVehicleConfiguration", array('path' => '/tmp/vConf.xml'));
}
?>
