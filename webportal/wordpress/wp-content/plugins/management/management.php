<?php
/*
Plugin Name: Management
Plugin URI:
Description: Management allows the admin to delete all APPs in the server from the Dashboard.
Version: 1.0.0
Author: Ze Ni
Author URI:
License: GNU General Public License v3.0
License URI: http://www.gnu.org/licenses/gpl-3.0.html

Copyright 2009, 2010, 2011, 2012, 2013 Ciprian Popescu (email: getbutterfly@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

define('MANAGEMENT_VERSION', '1.0.0');
//define('ALERTLEVEL', 8); // Alert level. Database size is shown in red if greater than this value (in MB), else in green.

define('MANAGEMENT_PLUGIN_URL', WP_PLUGIN_URL . '/' . dirname(plugin_basename(__FILE__)));

//include(PMA_PLUGIN_PATH . '/wp-info-mod/mod_general.php');

add_action('admin_menu', 'add_option_page_management');

function add_option_page_management() {
	add_menu_page('Management', 'Management', 'manage_options', __FILE__, 'option_page_management', MANAGEMENT_PLUGIN_URL . '/images/manage-16.png');
}

function option_page_management() {
?>

<div class="wrap">
	<div id="icon-plugins" class="icon32"></div>
	<h2>Management</h2>

	<br class="clear" />
	<table class="widefat">
		<thead>
			<tr>
				<th>Description</th>
				<th>Operation</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>Delete all plugins in the server</td>
				<td><code><?php echo DB_HOST;?></code></td>
			</tr>
			<tr>
				<td>Database size</td>
				<td><code><?php db_size();?></code></td>
			</tr>
			<tr>
				<td><strong>Management</strong> plugin version</td>
				<td><code><?php echo MANAGEMENT_VERSION; ?></code></td>
			</tr>
		</tbody>
	</table>
</div>
<?php
}
?>
