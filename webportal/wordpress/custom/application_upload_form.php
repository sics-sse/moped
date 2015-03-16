<?php
/**
Plugin Name: application_upload_form.php 
Description: A form for uploading new applications to the server.
Version: 1.0
Author: Ze Ni
License: GPL

	Copyright 2013  multiple file upload form for fresta  (email : zeni@sics.se)

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

// require_once("upload_form_handle.php");
require_once("globalVariables.php");

function application_upload_form(){
?>		
<div id="respond">
		<form id="upload_application_form" method="post" enctype="multipart/form-data">
<!-- 		<p> -->
<!-- 			<label for="applicationName">Application Name</label> -->
<!--			<input type="text" name="applicationName" id="applicationName" value="" /> <!-- required /> --> 
<!-- 		</p> -->
<!-- 		<p> -->
<!-- 			<label for="version">Version</label> -->
<!--			<input type="text" name="version" id="version" value="" /> <!-- required /> --> 
<!-- 		</p> -->
<!-- 		<p> -->
<!-- 			<label for="publisher">Publisher </label> -->
<!-- 			<input type="text" name="publisher" id="publisher" value="" /> -->
<!-- 		</p> -->
<!-- 		<input type="hidden" name="numOfPlugin" id="numOfPlugin" value="0"> -->
		<hr>
		<p>
			<label for="app_files">Application files *</label>
			<input type="file" name="app_files[]" id="app_files" multiple="" required />
		</p>
		<hr>
		<p>
			<input type="submit" name="submit_app" value="Upload" />
<!-- 		</p> -->
<!-- 		<p> -->
	</form>
</div>

<?php
}
add_shortcode('application_upload_form', 'application_upload_form');
if (isset($_POST['submit_app'])) {
	application_upload_handler();
}

function application_upload_handler() {
  global $client;
	
  /* Store files in the application directory */ 
  for($i=0; $i<count($_FILES['app_files']['name']); $i++) {
    $res = true; 
		
    $fname = $_FILES['app_files']['name'][$i];
    $versionIndex = strrpos($fname, "-");

    /* Check that this is a *.jar-file */
    if (substr($fname, -4) != '.jar') {
      echo "<font color='red'>Only *.jar files are currently supported, skipping over $fname</font><br/>";
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
      echo "<font color='orange'>No version information was detected in $fname (e.g. name-ver.jar), setting default version (1.0)</font><br/>";
    }
		
    $path = $_FILES['app_files']['tmp_name'][$i];
    $myfile = fopen($path, "r");
    $data = fread($myfile, filesize($path));
    fclose($myfile);
    $res = $client->uploadApp($data, $shortName, $version);

    if ($res) {
      echo "<font color='green'>$fname was successfully submitted...</font><br/>";
      $res = $client->compileApp($shortName, $version);

      if ($res) {
	echo "<font color='green'>Suite generation for $fname was successful.</font><br/>";

      } else {
	echo "<font color='red'>Suite generation for $fname failed.</font><br/>";
      }
    } else {
      echo "<font color='red'>Submission of $fname failed.</font><br/>";
    }
  }
}

?>