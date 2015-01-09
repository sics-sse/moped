<?php
	require_once('../wp-load.php');
	require_once(ABSPATH . "custom/globalVariables.php");

	 
	function invoke_jdk_install_webservice()
	{
		$webServiceAddress = getWebServiceAddress();
		echo "<br/><font color='green'>web service: $webServiceAddress</font><br />";
		//ini_set("soap.wsdl_cache_enabled", "1");  
		$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));
		try  
		{  
			$app_id = $_POST['id'];
			$vin = getVIN();
			print("vin: $vin; app_id: $app_id\r\n");
			
			$param = array('arg0' => $vin , 'arg1' => $app_id); 
			echo "<br/><font color='green'>1</font><br />";
			$ret = $client->install4Jdk($param);
			echo "<br/><font color='green'>2</font><br />";
			return $ret;
		} catch (SoapFault $exception) {  
			print $exception;
			return false;
		}
	}
	
	function addInstalledApp($appId) {
		global $wpdb;
		$table_name = "Vehicle";
		$wpdb->show_errors();
		$vin = getVIN();
		$myvalue = $wpdb->get_var($wpdb->prepare("select INSTALLED_APPS from Vehicle where VIN = %s", $vin));
		if($myvalue == NULL || $myvalue == "") {
				$wpdb->query($wpdb->prepare(
			"
				UPDATE $table_name SET INSTALLED_APPS = $appId WHERE VIN = %s
			", $vin 
			));
		} else {
			$apps = $myvalue.",".$appId;
			$wpdb->query($wpdb->prepare(
		"
			UPDATE $table_name SET INSTALLED_APPS = $apps WHERE VIN = %s
		", $vin 
		));
		}
		return true;
	}
	
	if ( !is_user_logged_in() ) {
		echo 'login in at first!';
	}
	else {
		// userId
		$userId = get_current_user_id( );
		$ret = invoke_jdk_install_webservice();
		//echo("|".$ret->return."|");
		if($ret->return == "true") 
		{
			//$app_id = $_POST['id'];
			//$feedback = addInstalledApp($app_id);
			//if ( $feedback != NULL ) {
			echo "<br/><font color='green'>Download ...</font><br />";
			//} else {
			//	echo "<br/><font color='red'>Error. Fail to download</font><br />";
			//}
		} else {
			echo "<br/><font color='red'>Connection problem between Server and Vehicle</font><br />";
		}
	}
	
?>
