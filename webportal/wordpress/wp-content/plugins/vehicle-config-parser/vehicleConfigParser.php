<?php
/**
 * @package vehicle-configuration-for-fresta
 * @version 1.0
*/

/*
Plugin Name: vehicle configuration for fresta
Plugin URI: 
Description: This is used for engineers to configure vehicles.
Version: 1.0
Author: Ze Ni
Author URI: https://www.sics.se/people/ze-ni
License: GPL
*/

/*  Copyright 2013  vehicle configuration for fresta  (email : zeni@sics.se)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License, version 2, as 
    published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

//require_once('../wp-load.php');
//require_once('wordpress/wp-load.php');
require_once(ABSPATH . "custom/globalVariables.php");

function vehicleConfigParser($data) {
  global $client;
  try  
    {  
      $ret = $client->addVehicleConfig("MOPED", $data);
      $ret = json_decode($ret);
      if($ret->result == "true")
	echo "<br/><font color='green'>Vehicle Configuration updated successfuflly</font><br />";
      else
	echo "<br/><font color='red'>".$ret->result."</font><br />";
      return $ret;
    } catch (SoapFault $exception) {  
    print $exception;
    return false;
  }
}

function vehicle_configuration_upload_form() {
?>

<form method="post"
enctype="multipart/form-data">
<label for="file">Filename:</label>
<input type="file" name="file" id="file" /> 
<br />
<input type="submit" name="submit" value="Submit" />
</form>
<?php
}
add_shortcode('vehicle_configuration_upload_form', 'vehicle_configuration_upload_form');

if (isset($_POST['submit'])) {
	if ((($_FILES["file"]["type"] == "text/xml"))
&& ($_FILES["file"]["size"] < 2000000))
  {
  if ($_FILES["file"]["error"] > 0)
    {
    echo "Return Code: " . $_FILES["file"]["error"] . "<br />";
    }
  else
    {
	/*
    echo "Upload: " . $_FILES["file"]["name"] . "<br />";
    echo "Type: " . $_FILES["file"]["type"] . "<br />";
    echo "Size: " . ($_FILES["file"]["size"] / 1024) . " Kb<br />";
    echo "Temp file: " . $_FILES["file"]["tmp_name"] . "<br />";
*/
    if (file_exists(ABSPATH."/custom/uploaded/" . $_FILES["file"]["name"]))
      {
      echo "<br/><font color='red'>".$_FILES["file"]["name"] . " already exists. </font><br />";
      }
    else
      {
	$path = $_FILES['file']['tmp_name'];
	$myfile = fopen($path, "r");
	$data = fread($myfile, filesize($path));
	fclose($myfile);

	unlink($path);

	vehicleConfigParser($data);
      }
    }
  }
else
  {
  echo "Invalid file";
  }
}
?>
