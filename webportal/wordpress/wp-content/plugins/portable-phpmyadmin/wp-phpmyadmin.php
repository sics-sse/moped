<?php
/*
Plugin Name: Portable phpMyAdmin
Plugin URI: http://getbutterfly.com/wordpress-plugins/portable-phpmyadmin/
Description: Portable phpMyAdmin allows a user to access the phpMyAdmin section straight from the Dashboard.
Version: 1.4.1
Author: Ciprian Popescu
Author URI: http://getbutterfly.com/
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

phpMyAdmin is licensed under the terms of the GNU General Public License
version 2, as published by the Free Software Foundation.
*/
define('PORTABLE_PHPMYADMIN_VERSION', '1.4.1');
define('ALERTLEVEL', 8); // Alert level. Database size is shown in red if greater than this value (in MB), else in green.

//
define('PMA_PLUGIN_URL', WP_PLUGIN_URL . '/' . dirname(plugin_basename(__FILE__)));
define('PMA_PLUGIN_PATH', WP_PLUGIN_DIR . '/' . dirname(plugin_basename(__FILE__)));
//

include(PMA_PLUGIN_PATH . '/wp-info-mod/mod_general.php');

add_action('admin_menu', 'add_option_page_portable_phpmyadmin');

// Activate and check for security key
function ppma_activate() {
	if(get_option('pma_key') == '') {
		$pma_key = uniqid('pma_', true) . '!' . md5(rand());
		add_option('pma_key', $pma_key);
	}
}
register_activation_hook(__FILE__, 'ppma_activate');
// End activation

function add_option_page_portable_phpmyadmin() {
	add_menu_page('Portable PMA', 'Portable PMA', 'manage_options', __FILE__, 'option_page_portable_phpmyadmin', PMA_PLUGIN_URL . '/images/icon-16.png');
	add_submenu_page(__FILE__, 'About/Help', 'About/Help', 'manage_options', 'pma_about', 'option_page_portable_pmaabout'); 
}

function option_page_portable_phpmyadmin() {
?>
<div class="wrap">
	<div id="icon-plugins" class="icon32"></div>
	<h2>Portable phpMyAdmin</h2>

	<div style="-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);">
		<iframe width="100%" height="800" name="pmaframe" src="<?php echo PMA_PLUGIN_URL; ?>/pma/index.php?call=<?php echo get_option('pma_key'); ?>" frameborder="0" seamless="seamless"></iframe>
	</div>

	<br class="clear" />
	<table class="widefat">
		<thead>
			<tr>
				<th>Variable Name</th>
				<th>Value</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>Database host</td>
				<td><code><?php echo DB_HOST;?></code></td>
			</tr>
			<tr>
				<td>Database size</td>
				<td><code><?php db_size();?></code></td>
			</tr>
			<tr>
				<td><strong>Portable phpMyAdmin</strong> plugin version</td>
				<td><code><?php echo PORTABLE_PHPMYADMIN_VERSION; ?></code></td>
			</tr>
			<tr>
				<td><strong>Portable phpMyAdmin</strong> plugin security key</td>
				<td>
					<?php
					if(get_option('pma_key') != '') {
						echo get_option('pma_key');
					}
					else {
						echo 'No security key available.';
					}
					?>
				</td>
		</tbody>
	</table>

	<?php get_portable_serverinfo(); ?>
</div>
<?php
}
function option_page_portable_pmaabout() { ?>
	<div class="wrap">
		<div id="icon-plugins" class="icon32"><br /></div>
		<h2>About Portable phpMyAdmin</h2>
		<div class="updated fade below-h2" style="background-color: rgb(255, 251, 204);">
			<p><strong>Important:</strong> You should have a backup of your database before modifying any data.</p>
		</div>
		<div class="updated fade below-h2" style="background-color: #FFBABA; border: 0.1em solid #CC0000;">
			<p><strong>Note:</strong> This plugin should only be used for development purposes or by experienced users. If more users have access to the administration section, you should consider using the plugin <em>only when necessary</em>.</p>
		</div>

		<p><strong>Portable phpMyAdmin</strong> allows a user to access the phpMyAdmin section straight from the Dashboard. If the user doesn't know the MySQL credentials, the plugin extracts them straight from wp-config.php. This plugin requires PHP 5+. Also, MySQL 5+ is recommended.</p>
		<p>Once activated, the plugin extracts MySQL information from the database and displays it on a separate page.</p>
		<p><strong>Remember:</strong> Always have a backup of your database before modifying any data! You should also make your blog inaccessible during database editing by activating the maintenance mode!</p>

		<h3>Donate</h3>
		<p>Fill out the form below and send us a few dollars for your favourite plugin.</p>
		<form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_blank">
			<div class="updated fade below-h2" style="background-color: rgb(255, 251, 204);">
				<p>
					<input type="hidden" name="cmd" value="_xclick">
					<input type="hidden" name="business" value="cip_sb@yahoo.com">
					I want to <select name="item_name" id="item_name">
						<option value="Donation">donate</option>
						<option value="Contribution">contribute</option>
					</select> 
					USD<input type="text" name="amount" name="item_name" size="3" value="10"> for future development of <b>Portable phpMyAdmin</b>. 

					<input type="hidden" name="no_shipping" value="0">
					<input type="hidden" name="no_note" value="1">
					<input type="hidden" name="currency_code" value="USD">
					<input type="hidden" name="lc" value="RO">
					<input type="hidden" name="bn" value="PP-BuyNowBF">
					<input type="hidden" name="return" value="http://getbutterfly.com/">
					<input type="submit" value="Pay with PayPal!" class="button button-secondary"> <img src="<?php echo PMA_PLUGIN_URL; ?>/images/icon-paypal.png" alt="" style="vertical-align: middle;">
				</p>
				<p><small>All donations and contributions go into a special hosting server fund, used for beta-testing and development. Thank you.</small></p>
			</div>
		</form>

		<p>Check the <a href="http://getbutterfly.com/wordpress-plugins/portable-phpmyadmin/" rel="external">official homepage</a> for feedback and support, or rate it on <a href="http://wordpress.org/extend/plugins/portable-phpmyadmin/" rel="external">WordPress plugin repository.</a></p>
		<p><small>Portable phpMyAdmin is based on phpMyAdmin 2.11.11.3 (2011-02-11) with several fixes and enhancements.</small></p>
	</div>
<?php }?>
