<?php
/**
 * class-groups-admin.php
 *
 * Copyright (c) 2011 "kento" Karim Rahimpur www.itthinx.com
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

/**
 * Groups admin sections initialization.
 */
class Groups_Admin {
	public static function init() {
		add_action( 'admin_init', array( __CLASS__, 'admin_init' ) );
		add_action( 'admin_notices', array( __CLASS__, 'admin_notices' ) );
		add_action( 'admin_menu', array( __CLASS__, 'admin_menu' ) );
		add_action( 'network_admin_menu', array( __CLASS__, 'network_admin_menu' ) );
	}
	
	/**
	* Hooks into admin_init.
	* @see Groups_Admin::admin_menu()
	* @see Groups_Admin::admin_print_styles()
	* @link http://codex.wordpress.org/Function_Reference/wp_enqueue_script#Load_scripts_only_on_plugin_pages
	*/
	public static function admin_init() {
		global $groups_version;
		wp_register_style( 'groups_admin', GROUPS_PLUGIN_URL . 'css/groups_admin.css', array(), $groups_version );
	}
	
	/**
	 * Loads styles for the Groups admin section.
	 * 
	 * @see Groups_Admin::admin_menu()
	 */
	public static function admin_print_styles() {
		wp_enqueue_style( 'groups_admin' );
	}
	
	/**
	 * Loads scripts.
	 */
	public static function admin_print_scripts() {
		global $groups_version;
		wp_enqueue_script( 'groups', GROUPS_PLUGIN_URL . 'js/groups_admin.js', array( ), $groups_version );
	}
	
	/**
	 * Prints admin notices.
	 */
	public static function admin_notices() {
		global $groups_admin_messages;
		if ( !empty( $groups_admin_messages ) ) {
			foreach ( $groups_admin_messages as $msg ) {
				echo $msg;
			}
		}
	}
	
	/**
	 * Admin menu.
	 */
	public static function admin_menu() {
		
		include_once( GROUPS_ADMIN_LIB . '/groups-admin-groups.php');
		include_once( GROUPS_ADMIN_LIB . '/groups-admin-capabilities.php');
		include_once( GROUPS_ADMIN_LIB . '/groups-admin-options.php');
		
		$pages = array();
	
		// main
		$page = add_menu_page(
			__( 'Groups', GROUPS_PLUGIN_DOMAIN ),
			__( 'Groups', GROUPS_PLUGIN_DOMAIN ),
			GROUPS_ADMINISTER_GROUPS,
			'groups-admin',
			apply_filters( 'groups_add_menu_page_function', 'groups_admin_groups' ),
			GROUPS_PLUGIN_URL . '/images/groups.png'
		);
		$pages[] = $page;
		add_action( 'admin_print_styles-' . $page, array( __CLASS__, 'admin_print_styles' ) );
		add_action( 'admin_print_scripts-' . $page, array( __CLASS__, 'admin_print_scripts' ) );
		
		$show_tree_view = Groups_Options::get_option( GROUPS_SHOW_TREE_VIEW, GROUPS_SHOW_TREE_VIEW_DEFAULT );
		if ( $show_tree_view ) {
			include_once( GROUPS_ADMIN_LIB . '/groups-admin-tree-view.php');
			$page = add_submenu_page(
				'groups-admin',
				__( 'Tree', GROUPS_PLUGIN_DOMAIN ),
				__( 'Tree', GROUPS_PLUGIN_DOMAIN ),
				GROUPS_ACCESS_GROUPS,
				'groups-admin-tree-view',
				apply_filters( 'groups_add_submenu_page_function', 'groups_admin_tree_view' )
			);
			$pages[] = $page;
			add_action( 'admin_print_styles-' . $page, array( __CLASS__, 'admin_print_styles' ) );
			add_action( 'admin_print_scripts-' . $page, array( __CLASS__, 'admin_print_scripts' ) );
		}
		
		// capabilities
		$page = add_submenu_page(
			'groups-admin',
			__( 'Groups Capabilities', GROUPS_PLUGIN_DOMAIN ),
			__( 'Capabilities', GROUPS_PLUGIN_DOMAIN ),
			GROUPS_ADMINISTER_GROUPS,
			'groups-admin-capabilities',
			apply_filters( 'groups_add_submenu_page_function', 'groups_admin_capabilities' )
		);
		$pages[] = $page;
		add_action( 'admin_print_styles-' . $page, array( __CLASS__, 'admin_print_styles' ) );
		add_action( 'admin_print_scripts-' . $page, array( __CLASS__, 'admin_print_scripts' ) );
		
		// options
		$page = add_submenu_page(
			'groups-admin',
			__( 'Groups options', GROUPS_PLUGIN_DOMAIN ),
			__( 'Options', GROUPS_PLUGIN_DOMAIN ),
			GROUPS_ADMINISTER_OPTIONS,
			'groups-admin-options',
			apply_filters( 'groups_add_submenu_page_function', 'groups_admin_options' )
		);
		$pages[] = $page;
		add_action( 'admin_print_styles-' . $page, array( __CLASS__, 'admin_print_styles' ) );
		add_action( 'admin_print_scripts-' . $page, array( __CLASS__, 'admin_print_scripts' ) );
	
		do_action( 'groups_admin_menu', $pages );
	}
	
	/**
	 * Network admin menu.
	 */
	public static function network_admin_menu() {
	
		include_once( GROUPS_ADMIN_LIB . '/groups-admin-options.php');
	
		$pages = array();
	
		// main
		$page = add_menu_page(
			__( 'Groups', GROUPS_PLUGIN_DOMAIN ),
			__( 'Groups', GROUPS_PLUGIN_DOMAIN ),
			GROUPS_ADMINISTER_GROUPS,
			'groups-network-admin',
			apply_filters( 'groups_add_menu_page_function', 'groups_network_admin_options' ),
			GROUPS_PLUGIN_URL . '/images/groups.png'
		);
		$pages[] = $page;
		add_action( 'admin_print_styles-' . $page, array( __CLASS__, 'admin_print_styles' ) );
		add_action( 'admin_print_scripts-' . $page, array( __CLASS__, 'admin_print_scripts' ) );
		
		do_action( 'groups_network_admin_menu', $pages );
	}
}
Groups_Admin::init();
