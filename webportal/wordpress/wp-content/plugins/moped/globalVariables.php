<?php	
	$webServiceAddress = "http://appz-ext.sics.se:9990/moped/pws?wsdl";
// 	$webServiceAddress = "http://localhost:9990/moped/pws?wsdl";
	
	try {
		$client = new SoapClient($webServiceAddress,
					array('cache_wsdl' => WSDL_CACHE_NONE));
	} catch (Exception $ex) {}

	function getWebServiceAddress()
	{
		global $webServiceAddress;
		return $webServiceAddress;
	}
	
	/**
	 * Get VIN from the database
	 * 
	 * This should be moved into a WebService call that retrieves info from the MOPED-db
	 */
	function getVIN() {
		global $wpdb;
	
		$user_id = wp_get_current_user()->ID;
		$sql = "select v.VIN from User_vehicle_association a JOIN Vehicle v ON a.vehicleID = v.id
		where a.userID = $user_id and a.defaultVehicle = 1";
		$vin = $wpdb->get_var($sql);
	
		return $vin;
	}
?>
