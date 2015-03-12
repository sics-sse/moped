<?php	
//	$webServiceAddress = "http://appz-ext.sics.se:9990/moped/pws?wsdl";
 	$webServiceAddress = "http://localhost:9990/moped/pws?wsdl";
	
        ini_set("soap.wsdl_cache_enabled", "0");

	try {
	  $client = new SoapClient
	    ($webServiceAddress,
	     array('cache_wsdl' => WSDL_CACHE_NONE,
		   'features' => SOAP_SINGLE_ELEMENT_ARRAYS));
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
		global $client;
	
		$user_id = wp_get_current_user()->ID;

		$ass = $client->listUserVehicleAssociations($user_id);
		$ass = json_decode($ass);
		// can there be error?

		foreach ($ass->result as $a) {
		  if ($a->active)
		    return $a->vin;
		}
		return "";
	}
?>
