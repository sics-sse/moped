<?php	
	function getWebServiceAddress()
	{
		$webServiceAddress = "http://localhost:8080/server/pluginWebServices?wsdl";
		//$webServiceAddress = "http://appz.i.sics.se:8080/server/pluginWebServices?wsdl";
// 		$webServiceAddress = "http://appz-ext.sics.se:8080/server/pluginWebServices?wsdl";
		return $webServiceAddress;
	}
	
	function getVIN()
	{
		global $wpdb;
		$wpdb->show_errors();
		$current_user = wp_get_current_user();
		$user_id = $current_user->ID;
		$sql = "select v.VIN from User_vehicle_association a JOIN Vehicle v ON a.vehicleID = v.id where a.userID = $user_id and a.defaultVehicle = 1";
		$vin = $wpdb->get_var($sql);
		return $vin;
	}
?>
