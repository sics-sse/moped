<?php
/**
 * Plugin Name: Mobile Open Platform for Experimental Development (MOPED) 
 * Plugin URI: https://moped.sics.se/
 * Description: Web portal for app upload and installation in MOPED platforms. 
 * Version: 1.0 
 * Author: Avenir Kobetski
 */

global $plugin_dir;
$plugin_dir = plugin_dir_path(__FILE__);

require_once($plugin_dir.'/application_mgmt.php');
require_once($plugin_dir.'/vehicle_mgmt.php');
?>
