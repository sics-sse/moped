<?php
// 	require_once($_SERVER['DOCUMENT_ROOT'].'/wordpress/wp-load.php' );
// 	require_once(ABSPATH . "custom/globalVariables.php");
// 	require_once(ABSPATH . "custom/xml_parser.php");

// 	class Plugin {
// 		private $pluginName;
// 		private $package;
// 		private $className;
// 		private $reference;
		
// 		public function __construct($plugin_name, $package, $className, $reference) {
// 			$this->plugin_name = $plugin_name;
// 			$this->package = $package;
// 			$this->className = $className;
// 			$this->reference = $reference;
// 		}
		
// 		public function getPluginName()
// 		{
// 			return $pluginName;
// 		}
		
// 		public function getPackage()
// 		{
// 			return $package;
// 		}
		
// 		public function getClassName()
// 		{
// 			return $className;
// 		}
		
// 		public function getReference()
// 		{
// 			return $reference;
// 		}
// 	}
	
// 	function extractFullClassName($path) {
//                 //print("testing...");
//                 if(!file_exists($path))
//                 {
//                         printf("zip file not found");
//                         return false;
//                 } else {
//                         $zip = zip_open($path);
//                         while($zip_entry = zip_read($zip)) {
//                                 $filename = zip_entry_name($zip_entry);
//                                 if(($dotPos = strpos($filename, '.')) !== false) {
//                                         $j2mePos = strpos($filename, 'j2meclasses');
//                                         if($j2mePos !== false) {
//                                                 $j2mePos += 12;
//                                                 $res = substr($filename, $j2mePos, $dotPos - $j2mePos);
//                                                 return $res;
//                                         }
//                                 }
//                         }
//                         return false;
//                 }
//         }
	
	function insert_new_app($applicationName, $publisher, $version, $numOfPlugin)
	{
		global $wpdb;
		
		$reply = "";
		$wpdb->show_errors();
		$is_exist = $wpdb->get_var($wpdb->prepare("SELECT COUNT(applicationId) from Application where applicationName = %s AND version = %s", $applicationName, $version));
		if($is_exist == 0)
		{
			// Insert application info
			$wpdb->insert(
				'Application',
				array(
					'applicationName' => $applicationName,
					'publisher' => $publisher,
					'version' => $version
				)
			);
			
			// Fetch applicationID that is used to insert plugins later
			$application_id = $wpdb->get_var($wpdb->prepare("SELECT applicationID from Application where applicationName = %s AND version = %s", $applicationName, $version));
			if($application_id == "0")
			{
				return "Error! Fail to insert ".$applicationName." to Database.";
			} else
			{
				// Insert relevant plugins info
				$dir_location_for_parse = ABSPATH . "custom" . DIRECTORY_SEPARATOR ."uploaded" . DIRECTORY_SEPARATOR . $applicationName . DIRECTORY_SEPARATOR . $version . DIRECTORY_SEPARATOR;
				$dir_location = "/opt/glassfish/glassfish/domains/domain1/config" . DIRECTORY_SEPARATOR ."uploaded" . DIRECTORY_SEPARATOR . $applicationName . DIRECTORY_SEPARATOR . $version . DIRECTORY_SEPARATOR;

				for($i=1;$i<=$numOfPlugin;$i++)
				{
					$pluginName = $_POST["plug_in_name$i"];
					$location = $dir_location;
					$location4zip = $dir_location.$pluginName;
					if(eregi(".xml$", $pluginName)) {
						//pluginConfigParser($application_id, $location);
						pluginConfigParser($application_id, $dir_location_for_parse.$pluginName);
						//parsePluginConfiguration($application_id, $dir_location_for_parse.$pluginName);
					} elseif(eregi(".suite$", $pluginName)) {
						$allocationStrategy = $_POST["allocationStrategy$i"];
						$reference = $_POST["reference$i"];
						$classname = $_POST["className$i"];//read_full_classname($location);
						//$noPostfixPluginName = substr($pluginName, 0, strlen($pluginName)-4);
						$wpdb->query($wpdb->prepare(
							"INSERT INTO DatabasePlugin(location, fullClassName, name, strategy, application_applicationId) VALUES(%s, %s, %s, %s, %d)",
							array(
								$location,
								$classname,
								$pluginName,
								$allocationStrategy,
								$application_id
							)
						));
					} else if(eregi(".zip$", $pluginName)) {
						$allocationStrategy = $_POST["allocationStrategy$i"];
						$reference = $_POST["reference$i"];
						//$classname = $_POST["className$i"];
						$classname = extractFullClassName($location4zip);

						$dotIndex = strripos($pluginName, '.');
						$prefixPluginName = substr($pluginName, 0, $dotIndex);
						$location = $location.$prefixPluginName.DIRECTORY_SEPARATOR.$prefixPluginName.".suite";
						$reply = invoke_generateSuite_webservice($location4zip, $prefixPluginName); 
						// update DB
						$wpdb->query($wpdb->prepare(
							"INSERT INTO DatabasePlugin(location, zipLocation, fullClassName, name, zipName, strategy, application_applicationId) VALUES(%s, %s, %s, %s, %s, %s, %d)",
							array(
								$location,
								$location4zip,
								$classname,
								$prefixPluginName.".suite",
								$pluginName,
								$allocationStrategy,
								$application_id
							)
						));
					} else {
						$allocationStrategy = $_POST["allocationStrategy$i"];
						$reference = $_POST["reference$i"];
						
						$classname = read_full_classname($location);
						$noPostfixPluginName = substr($pluginName, 0, strlen($pluginName)-4);
						$wpdb->query($wpdb->prepare(
							"INSERT INTO DatabasePlugin(location, zipLocation, fullClassName, name, zipName, strategy, application_applicationId) VALUES(%s, %s, %s, %s, %s, %s, %d)",
							array(
								$location,
								$location4zip,
								$classname,
								$noPostfixPluginName,
								$pluginName,
								$allocationStrategy,
								$application_id
							)
						));
					
					}
					
				}
				
				// Check whether invoke upgrade web service. If the verison of uploaded application is newer than the max version of the rest of applications, invoke upgrade webservice
				$result = $wpdb->get_results($wpdb->prepare("SELECT * from Application where applicationName = %s", $applicationName, ARRAY_A));
				foreach ($result as $v) {
					if($v->version < $version)
						invoke_upgrade_webservice($v->applicationId);
				}
				return "<font color='green'>$reply<br />Success! Your application ($applicationName) submited</font>";
			}	
		} 
		else 
			return "<font color='red'>$reply<br />Error! The version ($version) of your application ($applicationName) existed.</font>";
	}
	
// 	function invoke_upgrade_webservice($old_app_id) {
// 		$webServiceAddress = getWebServiceAddress();
// 		ini_set("soap.wsdl_cache_enabled", "0");  
// 		$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));  
// 		try  
// 		{  
// 			$param = array('arg0' => $old_app_id);  
// 			$client->setUpgradeFlag($param);  
// 		} catch (SoapFault $exception) {  
// 			print $exception;
// 		}
// 	}
	
	function invoke_generateSuite_webservice($zipFile, $fullClassName) {
		global $client;
		$reply = "";
		
// 		$webServiceAddress = getWebServiceAddress();
// 		ini_set("soap.wsdl_cache_enabled", "0");  
// 		$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));  
		
		try  
		{  
// 			$param = array('arg0' => $zipFile, 'arg1' => $fullClassName);  
// 			$reply = $client->generateSuite($param);
			
			$reply = $client->__soapCall("generateSuite", 
					array('arg0' => $zipFile, 
							'name' => $fullClassName));
		} catch (SoapFault $exception) {  
			print $exception;
		}
		return $reply->return;
	}
	
// 	function read_full_classname($file_path) {
// 		if (eregi(".zip$",$file_path)){ 
// 			$zip = zip_open($file_path);
// 			if($zip) {
// 				while($zip_entry = zip_read($zip)) {
// 					$file = zip_entry_name($zip_entry);
// 					if(strpos($file, '$') == false) {
// 						$file = substr($file, 0, strlen($file)-6);
// 						$file = str_replace("/", ".", $file);
// 						return $file;
// 					}
					
// 				}
// 			}
// 		 }else{ 
// 			return "";
// 		 }
// 	}
	
// 	function creat_path_for_new_app($applicationName, $version)
// 	{
// 		$basedir = dirname(__FILE__) . DIRECTORY_SEPARATOR . "uploaded" . DIRECTORY_SEPARATOR . $applicationName . DIRECTORY_SEPARATOR . $version . DIRECTORY_SEPARATOR;
// 		mkdirs($basedir);
// 	}
	
// 	function mkdirs($dir)
// 	{
// 		if(!is_dir($dir))
// 		{
// 			if(!mkdirs(dirname($dir))){
// 				return false;
// 			}
// 			if(!mkdir($dir,0777)){
// 				return false;
// 			}
// 		}
// 		return true;
// 	}

// 	function pluginConfigParser($appId, $path) {
// 		$webServiceAddress = getWebServiceAddress();
// 		//ini_set("soap.wsdl_cache_enabled", "1");  
// 		$client = new SoapClient($webServiceAddress, array('encoding'=>'UTF-8'));
// 		try  
// 		{  
// 			$param = array('arg0' => $appId,'arg1' => $path);  
// 			$ret = $client->parsePluginConfiguration($param);
// 			return $ret;
// 		} catch (SoapFault $exception) {  
// 			print $exception;
// 			return false;
// 		}
// 	}
	
	
// 	if($_POST['applicationName'] !="" && $_POST['version'] != "" ) {
// 		$applicationName = $_POST['applicationName'];
// 		$publisher = $_POST['publisher'];
// 		$version = $_POST['version'];
		
// 		$plugins = array();
// 		$numOfPlugin = intval($_POST['numOfPlugin']);
	
	
	
// 		$reply = insert_new_app($applicationName, $publisher, $version, $numOfPlugin);
?>

<?php
// }
	
//creat_path_for_new_app($applicationName, $version);
//     die();
?>
