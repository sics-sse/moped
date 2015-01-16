<?php
// 	require_once('../wp-load.php');
	require_once("globalVariables.php");

	 
	function invoke_install_webservice($app_id, $jvm)
	{
		global $client;
		
// 		$webServiceAddress = getWebServiceAddress();
// 		echo "<br/><font color='green'>web service: $webServiceAddress</font><br />";
// 		//ini_set("soap.wsdl_cache_enabled", "1");  
		
		try  
		{  
			$vin = getVIN();
			$client->__soapCall("install", 
					array('vin' => $vin,
							'appID' => $app_id,
							'jvm' => $jvm));
			
			for ($i=0; $i<30; $i++) {
				$ret = $client->__soapCall("get_ack_status", 
						array('vin' => $vin,
								'appId' => $app_id));

				if ($ret) {
					echo "<font color='green'>Installation complete</font><br/>";
					break;
				}
				else {
					sleep(1);
				}
			}
			
			if (!$ret) {
				echo "<font color='red'>Installation failed</font>";
			}
			
			return $ret;
		} catch (SoapFault $exception) {  
			print $exception;
			return false;
		}
	}
	
	function addInstalledApp($appId) {
// 		global $wpdb;
		
 		echo "In addInstalledApp()";
		$vin = getVIN();
		echo "vin: $vin\r\n";
		
// 		$table_name = "Vehicle";
// 		$wpdb->show_errors();
// 		$vin = getVIN();

// 		$myvalue = $wpdb->get_var($wpdb->prepare("select INSTALLED_APPS from Vehicle where VIN = %s", $vin));
// 		if($myvalue == NULL || $myvalue == "") {
// 				$wpdb->query($wpdb->prepare(
// 			"
// 				UPDATE $table_name SET INSTALLED_APPS = $appId WHERE VIN = %s
// 			", $vin 
// 			));
// 		} else {
// 			$apps = $myvalue.",".$appId;
// 			$wpdb->query($wpdb->prepare(
// 		"
// 			UPDATE $table_name SET INSTALLED_APPS = $apps WHERE VIN = %s
// 		", $vin 
// 		));
// 		}
// 		return true;
	}
	
// 	if ( !is_user_logged_in() ) {
// 		echo 'login in at first!';
// 	}
// 	else {
// 		// userId
// 		$userId = get_current_user_id( );
// 		$ret = invoke_install_webservice();
// 		//echo("|".$ret->return."|");
// 		if($ret->return == "true") 
// 		{
// 			//$app_id = $_POST['id'];
// 			//$feedback = addInstalledApp($app_id);
// 			//if ( $feedback != NULL ) {
// 			echo "<br/><font color='green'>Download ...</font><br />";
// 			//} else {
// 			//	echo "<br/><font color='red'>Error. Fail to download</font><br />";
// 			//}
// 		} else {
// 			echo "<br/><font color='red'>Connection problem between Server and Vehicle</font><br />";
// 		}
// 	}
	
?>
