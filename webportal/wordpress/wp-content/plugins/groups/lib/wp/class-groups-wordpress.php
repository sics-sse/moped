<?php
/**
 * class-groups-wordpress.php
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

/**
 * WordPress capabilities integration.
 */
class Groups_WordPress {
	
	/**
	 * Hook into actions to extend user capabilities.
	 * 
	 * @todo We might want to keep up with new capabilities when added, so
	 * that others don't have to add these explicitly to Groups when they
	 * add them to WordPress. Currently there's no hook for when a capability
	 * is added and checking this in any other way is too costly.
	 */
	public static function init() {
		// args: string $result, Groups_User $groups_user, string $capability
		add_filter( 'groups_user_can', array( __CLASS__, 'groups_user_can' ), 10, 3 );
		add_filter( 'user_has_cap', array( __CLASS__, 'user_has_cap' ), 10, 3 );
	}
	
	/**
	 * Extends Groups user capability with its WP_User capability.
	 * 
	 * @param string $result
	 * @param Groups_User $groups_user
	 * @param string $capability
	 */
	public static function groups_user_can( $result, $groups_user, $capability ) {
		if ( !$result ) {
			// Check if the capability exists, otherwise this will
			// produce a deprecation warning "Usage of user levels by plugins
			// and themes is deprecated", not because we actually use a
			// deprecated user level, but because it doesn't exist.
			if ( Groups_Capability::read_by_capability( $capability ) ) {
				$result = user_can( $groups_user->user->ID, $capability );
			}
		}
		return $result;
	}
	
	/**
	 * Extend user capabilities with Groups user capabilities.
	 * 
	 * @param array $allcaps the capabilities the user has
	 * @param unknown_type $caps
	 * @param unknown_type $args
	 */
	public static function user_has_cap( $allcaps, $caps, $args ) {
		$user_id = isset( $args[1] ) ? $args[1] : null;
		$groups_user = new Groups_User( $user_id );
		if ( is_array( $caps ) ) {
			// we need to deactivate this because invoking $groups_user->can()
			// would trigger this same function and we would end up
			// in an infinite loop
			remove_filter( 'user_has_cap', array( __CLASS__, 'user_has_cap' ), 10, 3 );
			foreach( $caps as $cap ) {
				if ( $groups_user->can( $cap ) ) {
					$allcaps[$cap] = true;
				}
			}
			add_filter( 'user_has_cap', array( __CLASS__, 'user_has_cap' ), 10, 3 );
		}
		return $allcaps;
	}
	
	/**
	 * Adds WordPress capabilities to Groups capabilities.
	 * Must be called explicitly.
	 * @see Groups_Controller::activate()
	 */
	public static function activate() {
		self::refresh_capabilities();
	}
	
	/**
	 * Refreshes Groups capabilities based on WordPress capabilities.
	 * @return int number of capabilities added
	 */
	public static function refresh_capabilities() {
		global $wp_roles;
		$capabilities = array();
		$count = 0;
		if ( !isset( $wp_roles ) ) {
			// just trigger initialization
			get_role( 'administrator' );
		}
		$roles = $wp_roles->roles;
		if ( is_array( $roles ) ) {
			foreach ( $roles as $rolename => $atts ) {
				if ( isset( $atts['capabilities'] ) && is_array( $atts['capabilities'] ) ) {
					foreach ( $atts['capabilities'] as $capability => $value ) {
						if ( !in_array( $capability, $capabilities ) ) {
							$capabilities[] = $capability;
						}
					}
				}
			}
		}
		foreach ( $capabilities as $capability ) {
			if ( !Groups_Capability::read_by_capability( $capability ) ) {
				Groups_Capability::create( array( 'capability' => $capability ) );
				$count++;
			}
		}
		return $count;
	}
}
Groups_WordPress::init();
