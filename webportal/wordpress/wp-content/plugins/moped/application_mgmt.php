<?php

require_once("globalVariables.php");

/************************************************************/
/* 						Forms 								*/
/************************************************************/

/**
 * Form (GUI) for application upload
 */
function application_upload_form(){
?>		
<div id="respond">
		<form id="upload_application_form" method="post" enctype="multipart/form-data">
		<hr>
		<p>
			<label for="app_files">Application files *</label>
			<input type="file" name="app_files[]" id="app_files" multiple="" required />
		</p>
		<hr>
		<p>
			<input type="submit" name="upload" value="Upload" />
<!-- 		</p> -->
<!-- 		<p> -->
		<input type="submit" name="test_comp" value="Upload and compile"/></p>
	</form>
</div>

<?php
}
add_shortcode('application_upload_form', 'application_upload_form');
if (isset($_POST['upload'])) {
	ws_uploadApp();
}

/**
 * Form for displaying all uploaded apps in a table.
 *
 * Also contains buttons for installing apps to vehicle / simulator.
 */
function apps_display_form(){
	global $wpdb;

	//TODO: This will have to be moved into a WebService call (to MOPED-db instead of wpdb)
	$apps = $wpdb->get_results("SELECT * FROM Application ORDER BY applicationName, version");
	?>
		
	<table cellpadding="0" cellspacing="0" border="0" class="display" id="example">
		<thead>
			<tr>
				<th width="30%">Application</th>
				<th width="20%">Publisher</th>
				<th width="20%">Version</th>
				<th width="15%">Squawk</th>
				<th width="15%">JDK</th>
			</tr>
		</thead>
		<tbody>
			<?php
			$app_nr = 0;
			foreach ($apps as $app) {
				echo "<tr>\r\n";
				echo "<td>$app->applicationName</td>\r\n";
				echo "<td>$app->publisher</td>\r\n";
				echo "<td>$app->version</td>\r\n";
				echo "<td><form method=\"post\"><input type='hidden' name='app_row' value='$app_nr'/><input type='hidden' name='app_id' value='$app->applicationId'/><input name='Squawk_install' type='image' src='wordpress/custom/images/install.png' alt='Install'/></form></td>\r\n";
				echo "<td><form method=\"post\"><input type='hidden' name='app_row' value='$app_nr'/><input type='hidden' name='app_id' value='$app->applicationId'/><input name='Jdk_install' type='image' src='wordpress/custom/images/install.png' alt='Install'/></form></td>\r\n";
				echo "</tr>\r\n";
				$app_nr++;
			}
			
			mysql_close($conn);
			?>
		</tbody>
		<tfoot>
			<tr>
				<th>Application</th>
				<th>Publisher</th>
				<th>Version</th>
				<th>Squawk</th>
				<th>JDK</th>
			</tr>
		</tfoot>
	</table>
	<div id="feedback"></div>
	
	<?php 
	if (isset($_POST['Jdk_install_x'])) {
		$ret = ws_install($_POST['app_id'], 'jdk');
	}
	else if (isset($_POST['Squawk_install_x'])) {
		$ret = ws_install($_POST['app_id'], 'Squawk');
	}
}
add_shortcode('apps_display_form', 'apps_display_form');


/************************************************************/
/* 					Calls to Web Services  					*/
/************************************************************/

/**
 * Upload an application and compile it for Squawk 
 */
function ws_uploadApp() {
	echo "<font color='green'>Testing compilation...</font><br/>";
	
	/* Store files in the application directory */
	for($i=0; $i<count($_FILES['app_files']['name']); $i++) {
		$res = true;
	
		$fname = $_FILES['app_files']['name'][$i];
		$versionIndex = strrpos($fname, "-");
		
		/* Check that this is a *.jar-file */
		if (substr($fname, -4) != '.jar') {
			echo "<font color='red'>Only *.jar files are currently supported, skipping $fname</font><br/>";
			continue;
		}

		/* Extract the application name (file name without its class type)
		* and its version (use 1.0 as default) */
		if ($versionIndex !== false) {
			$version = substr($fname, $versionIndex+1, -4);
			$shortName = substr($fname, 0, $versionIndex);
		}
		else {
			$version = "1.0";
			$shortName = substr($fname, 0, -4);
		}
	
		/* Create a directory for the new application */
		$targetDir = __DIR__.'/../../moped_plugins/'.$shortName.'/'.$version.'/';
		if (!file_exists($targetDir)) {
			$res &= mkdir($targetDir, 0777, true);
		}
		
		$res &= move_uploaded_file($_FILES['app_files']['tmp_name'][$i],
				$targetDir.$shortName.'.jar'); //.zip???
		$res &= copy($targetDir.$shortName.'.jar', $targetDir.$shortName.'.zip'); //TEMP
		
		$res &= ws_generateSuite($targetDir.$shortName.'.zip', $shortName);
		
		/* Report the result */
		if ($res) {
			echo "<font color='green'>$fname was compiled...</font><br/>";
		}
		else {
			echo "<font color='red'>Compilation of $fname failed...</font><br/>";
		}
	}
}

/**
 * Generate Squawk suite files (i.e. compile for Squawk) 
 * @param $zipFile
 * @param $fullClassName
 */
function ws_generateSuite($zipFile, $fullClassName) {
	global $client;
	$reply = "";

	try
	{
		$reply = $client->__soapCall("generateSuite",
				array('arg0' => $zipFile,
						'name' => $fullClassName));
		echo "<font color='blue'>Compilation result: $reply</font><br/>";
	} catch (SoapFault $exception) {
		print $exception;
	}

	if ($reply == "")
		return false;
	else 
		return true;
}

/**
 * Invoke the app installation web service
 *
 * @param $app_id 		- app id, as stored in the MOPED database
 * @param $jvm			- type of JVM (e.g. JDK or Squawk)
 */
function ws_install($app_id, $jvm)
{
	global $client;

	try
	{
		$vin = getVIN();
		$client->__soapCall("install",
				array('vin' => $vin,
						'appID' => $app_id,
						'jvm' => $jvm));

		//TODO: This could probably be done in another way...
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
		//TODO: ... up to here

		if (!$ret) {
			echo "<font color='red'>Installation failed</font>";
		}

		return $ret;
	} catch (SoapFault $exception) {
		print $exception;
		return false;
	}
}
?>