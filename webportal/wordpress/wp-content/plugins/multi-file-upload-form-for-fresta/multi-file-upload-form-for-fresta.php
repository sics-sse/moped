<?php
/**
 * @package multi-file-upload-form-for-fresta
 * @version 1.0
*/

/*
Plugin Name: multiple file upload form for fresta
Plugin URI: 
Description: This is used to make a dedicated form through which developers can register their new applications and upload relevant Plug-Ins to the server.
Version: 1.0
Author: Ze Ni
Author URI: https://www.sics.se/people/ze-ni
License: GPL
*/

/*  Copyright 2013  multiple file upload form for fresta  (email : zeni@sics.se)

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

/*
function upload_install() {
	global $wpdb;
	
	$table_name = "application";
	$sql = "CREATE TABLE IF NOT EXISTS $table_name (
		id INT NOT NULL AUTO_INCREMENT,
		applicationName varchar(255) NOT NULL,
		plugins tinyblob,
		publisher varchar(255),
		version varchar(255) NOT NULL,
		PRIMARY KEY  (id)
	);";
	
	require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
	dbDelta( $sql );
	
	add_option( "upload_version", "1.0" );
}

register_activation_hook( __FILE__, 'upload_install' );
*/

require_once(__DIR__."/../../custom/upload_form_handle.php");

function wptuts_styles_with_the_lot()  
{  
    // Register the style like this for a plugin:  
   wp_register_style( 'ajaxupload-style', plugins_url( '/css/baseTheme/style.css', __FILE__ ), array(), '20120208', 'all' );  
    // or  
    // Register the style like this for a theme:  
    //wp_register_style( 'ajaxupload-style', get_template_directory_uri() . '/css/baseTheme/style.css', array(), '20120208', 'all' );  
  
    // For either a plugin or a theme, you can then enqueue the style:  
    wp_enqueue_style( 'ajaxupload-style' );  
}  
add_action( 'wp_enqueue_scripts', 'wptuts_styles_with_the_lot' );

function ui_widget_scripts_with_jquery()  
{  
	wp_enqueue_script(
		'ajaxupload.js',
		plugins_url( '/js/ajaxupload-min.js',  __FILE__ ),
		array( 'jquery' )
	);
    // Register the script like this for a plugin:  
    //wp_register_script( 'ajaxupload', plugins_url( '/examples/jslibs/ajaxupload.js', __FILE__ ), array( 'jquery' ) );  
    // or  
    // Register the script like this for a theme:  
    //wp_register_script( 'ajaxupload-min', get_template_directory_uri() . '/js/ajaxupload.js', array( 'jquery' ) );  
  
    // For either a plugin or a theme, you can then enqueue the script:  
    //wp_enqueue_script( 'ajaxupload' );  
}  
add_action( 'wp_enqueue_scripts', 'ui_widget_scripts_with_jquery' );   

function upload_scripts_with_jquery()  
{  
    // Register the script like this for a plugin:  
    wp_register_script( 'upload', plugins_url( '/js/upload.js', __FILE__ ), array( 'jquery' ) );  
    // or  
    // Register the script like this for a theme:  
    //wp_register_script( 'ajaxupload-min', get_template_directory_uri() . '/js/ajaxupload.js', array( 'jquery' ) );  
  
    // For either a plugin or a theme, you can then enqueue the script:  
    wp_enqueue_script( 'upload' );  
}  
add_action( 'wp_enqueue_scripts', 'upload_scripts_with_jquery' );   

function new_app_registration_form(){
?>		
<div id="respond">
<!-- 	<form id="upload_application_form" method="post" action="wordpress/custom/upload_form_handle.php" enctype="multipart/form-data"> -->
		<form id="upload_application_form" method="post" enctype="multipart/form-data">
		<p>
			<label for="applicationName">Application Name <span class="required">*</span></label>
			<input type="text" name="applicationName" id="applicationName" value="" required />
		</p>
		<p>
			<label for="publisher">Publisher </label>
			<input type="text" name="publisher" id="publisher" value="" />
		</p>
		<p>
			<label for="version">Version <span class="required">*</span></label>
			<input type="text" name="version" id="version" value="" required />
		</p>
		<input type="hidden" name="numOfPlugin" id="numOfPlugin" value="0">
		<hr>
		<p id="plugin_info" />
		<p class="demo" />
		<hr>
		<p>
			<input type="submit" id="submit_upload_app" name="submit_upload_app" value="submit" />
		</p>
		<div class="form_result"> </div>
	</form>
</div>

<?php
}
add_shortcode('new_app_registration_form', 'new_app_registration_form');

if (isset($_POST['submit_upload_app'])) {
	insert_new_app($_POST['applicationName'], $_POST['publisher'], $_POST['version'], $_POST['numOfPlugin']);
}
?>