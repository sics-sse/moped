<?php
/**
 * wp-init.php
 * 
 * Copyright (c) "kento" Karim Rahimpur www.itthinx.com
 * 
 * This code is released under the GNU General Public License.
 * See COPYRIGHT.txt and LICENSE.txt.
 * 
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * This header and all notices must be kept intact.
 * 
 * @author Karim Rahimpur
 * @package groups
 * @since groups 1.0.0
 */

// startup
global $groups_version, $groups_admin_messages;

if ( !isset( $groups_admin_messages ) ) {
	$groups_admin_messages = array();
}

if ( !isset( $groups_version ) ) {
	$groups_version = GROUPS_CORE_VERSION;
}

// <= 3.2.1
if ( !function_exists( 'is_user_member_of_blog' ) ) {
	function is_user_member_of_blog( $user_id, $blog_id = 0 ) {
		return false !== get_user_by( "id", $user_id );
	}
}

/**
 * Load core :
 */

require_once( GROUPS_CORE_LIB . '/class-groups-utility.php' );

// options
require_once( GROUPS_CORE_LIB . '/class-groups-options.php' );

// plugin control: activation, deactivation, ...
require_once( GROUPS_CORE_LIB . '/class-groups-controller.php' );

// admin
if ( is_admin() ) {
	require_once( GROUPS_ADMIN_LIB . '/class-groups-admin.php' );
	if ( Groups_Options::get_option( GROUPS_SHOW_IN_USER_PROFILE, GROUPS_SHOW_IN_USER_PROFILE_DEFAULT ) ) {
		require_once( GROUPS_ADMIN_LIB . '/class-groups-admin-user-profile.php' );
	}
	require_once( GROUPS_ADMIN_LIB . '/class-groups-admin-users.php' );
}

// help
if ( is_admin() ) {
	require_once( GROUPS_CORE_LIB . '/class-groups-help.php' );
}

require_once( GROUPS_CORE_LIB . '/class-groups-capability.php' );
require_once( GROUPS_CORE_LIB . '/class-groups-group.php' );
require_once( GROUPS_CORE_LIB . '/class-groups-group-capability.php' );
require_once( GROUPS_CORE_LIB . '/class-groups-user.php' );
require_once( GROUPS_CORE_LIB . '/class-groups-user-capability.php' );
require_once( GROUPS_CORE_LIB . '/class-groups-user-group.php' );

/**
 * Load auto :
 */

require_once( GROUPS_AUTO_LIB . '/class-groups-registered.php' );

/**
 * Load access :
 */

require_once( GROUPS_ACCESS_LIB . '/class-groups-post-access.php' );

if ( is_admin() ) {
	require_once( GROUPS_ACCESS_LIB . '/class-groups-access-meta-boxes.php' );
}
require_once( GROUPS_ACCESS_LIB . '/class-groups-access-shortcodes.php' );
require_once( GROUPS_VIEWS_LIB . '/class-groups-shortcodes.php' );

/**
 * Load wp :
 */
require_once( GROUPS_WP_LIB . '/class-groups-wordpress.php' );

// widgets
// include_once( GROUPS_CORE_LIB . '/class-groups-widgets.php' );
// add_action( 'widgets_init', 'groups_widgets_init' );

/**
 * Register widgets
 */
// function groups_widgets_init() {
//	 register_widget( 'Groups_Widget' );
// }




/**
 * Returns the prefixed DB table name.
 * @param string $name the name of the DB table
 * @return string prefixed DB table name
 */
function _groups_get_tablename( $name ) {
	global $wpdb;
	return $wpdb->prefix . GROUPS_TP . $name;
}
