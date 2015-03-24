<?php
require_once("globalVariables.php");

	 
function invoke_install_webservice($app_id) {
  global $client;
		
  try  
    {  
      $vin = getVIN();
      $ret = $client->installApp($vin, $app_id);
      $ret = json_decode($ret);
      if ($ret->error) {
	$e = $ret->message;
	echo "<font color='red'>Installation failed: $e</font>";
	return;
      }

      for ($i=0; $i<30; $i++) {
	$ret = $client->get_ack_status($vin, $app_id);

	if ($ret) {
	  echo "<font color='green'>Installation complete</font><br/>";
	  break;
	}
	else {
	  sleep(1);
	}
      }
			
      if (!$ret) {
	echo "<font color='red'>Installation failed 2</font>";
      }
			
      return $ret;
    } catch (SoapFault $exception) {  
    print $exception;
    return false;
  }
}
	
	
?>
