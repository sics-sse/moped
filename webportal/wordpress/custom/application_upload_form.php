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

function application_upload_form(){
?>		
<div id="respond">
		<form id="upload_application_form" method="post" enctype="multipart/form-data">
		<p>
			<label for="applicationName">Application Name</label>
			<input type="text" name="applicationName" id="applicationName" value="" /> <!-- required /> --> 
		</p>
		<p>
			<label for="version">Version</label>
			<input type="text" name="version" id="version" value="" /> <!-- required /> --> 
		</p>
		<p>
			<label for="publisher">Publisher </label>
			<input type="text" name="publisher" id="publisher" value="" />
		</p>
		<input type="hidden" name="numOfPlugin" id="numOfPlugin" value="0">
		<hr>
		<p>
			<label for="app_files">Application files *</label>
			<input type="file" name="app_files[]" id="app_files" multiple="" required />
		</p>
		<hr>
		<p>
			<input type="submit" name="submit_app" value="submit" />
		</p>
	</form>
</div>

<?php
}
add_shortcode('application_upload_form', 'application_upload_form');
if (isset($_POST['submit_app'])) {
	application_upload_handler();
}

function application_upload_handler() {
	$res = true;
	$targetDir = __DIR__.'/../../moped_plugins/'.$_POST['applicationName'].'/'.$_POST['version'].'/';
	$localDir = $_POST['applicationName'].'/j2meclasses/';

	/* Create a directory for the new application */
	if (!file_exists($targetDir)) {
		$res &= mkdir($targetDir, 0777, true);
	}
	
	/* Create a zip archive */
	$zip = new ZipArchive();
	$res &= $zip->open($targetDir.$_POST['applicationName'].'.zip', ZipArchive::OVERWRITE);
	
	$res &= $zip->addEmptyDir($localDir);
	
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
			echo "<font color='orange'>No version information was detected in $fname (e.g. name-ver.jar), setting default version (1.0)</font><br/>";
		}
		
		/* Create a directory for the new application */
		$targetDir = __DIR__.'/../../moped_plugins/'.$shortName.'/'.$version.'/';
		if (!file_exists($targetDir)) {
			$res &= mkdir($targetDir, 0777, true);
		}
		
		$res &= move_uploaded_file($_FILES['app_files']['tmp_name'][$i],
							   	   $targetDir.$shortName.'.zip');
	
		/* Create a zip archive */
// 		$zip = new ZipArchive();
// 		$res &= $zip->open($targetDir.$shortName.'.zip', ZipArchive::OVERWRITE);
// 		$res &= $zip->addEmptyDir($shortName.'/j2meclasses/');
// 		$zip->addFile($_FILES['app_files']['tmp_name'][$i], 
// 					  $shortName.'/j2meclasses/'.$shortName);
		
// 		$res &= move_uploaded_file($_FILES['app_files']['tmp_name'][$i], 
// 					   	   		   $targetDir.$_FILES['app_files']['name'][$i]);

		/* Report the result */
		if ($res) {
			echo "<font color='green'>$fname was successfully submitted...</font><br/>";
		}
		else {
			echo "<font color='red'>Submission of $fname failed...</font><br/>";
		}
	}
	
	$zip->close();
	
	
		
	// 	insert_new_app($_POST['applicationName'], $_POST['publisher'], $_POST['version'], $_POST['numOfPlugin']);
}
?>