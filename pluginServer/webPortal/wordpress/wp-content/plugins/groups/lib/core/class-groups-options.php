<?php
/**
 * class-groups-options.php
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
 * Groups options handler
 */
class Groups_Options {
		
	/**
	 * Groups plugin option key.
	 * 
	 * @var string
	 */
	const option_key = 'groups_options'; 
	
	/**
	 * General option index.
	 * 
	 * @var string
	 */
	const general = 'general';

	/**
	 * No instances are needed.
	 */
	private function __construct() {
	}
	
	/**
	 * No cloning.
	 */
	private function __clone() {
	}
	
	/**
	 * Would be pointless.
	 */
	private function __wakeup() {
	}

	/**
	 * Registers Groups options (not autoloaded).
	 */
	public static function init() {
		$options = get_option( self::option_key );
		if ( $options === false ) {
			$options = array( self::general => array() );
			add_option( self::option_key, $options, null, 'no' );
		}
	}
	
	/**
	 * Returns the current Groups options and initializes them
	 * through init() if needed.
	 * @return array Groups options
	 */
	private static function get_options() {
		$options = get_option( self::option_key );
		if ( $options === false ) {
			self::init();
			$options = get_option( self::option_key );
		}
		return $options;
	}
	
	/**
	 * Returns the value of a general setting.
	 *
	 * @param string $option the option id
	 * @param mixed $default default value to retrieve if option is not set
	 * @return option value, $default if set or null
	 */
	public static function get_option( $option, $default = null ) {
		$options = self::get_options();
		$value = isset( $options[self::general][$option] ) ? $options[self::general][$option] : null;
		if ( $value === null ) {
			$value = $default;
		}
		return $value;
	}
	
	
	/**
	 * Returns the value of a user setting.
	 * 
	 * @param string $option the option id
	 * @param mixed $default default value to retrieve if option is not set
	 * @param int $user_id retrieve option for this user, defaults to null for current user
	 * @return option value, $default if set or null
	 */
	public static function get_user_option( $option, $default = null, $user_id = null ) {
		if ( $user_id === null ) {
			$current_user = wp_get_current_user();
			if ( !empty( $current_user ) ) {
				$user_id = $current_user->ID;
			}
		}
		$value = null;
		if ( $user_id !== null ) {
			$options = self::get_options();
			$value = isset( $options[$user_id][$option] ) ? $options[$user_id][$option] : null;
		}
		if ( $value === null ) {
			$value = $default;
		}
		return $value;
	}
		
	/**
	 * Updates a general setting.
	 *
	 * @param string $option the option's id
	 * @param mixed $new_value the new value
	 */
	public static function update_option( $option, $new_value ) {
		$options = self::get_options();
		$options[self::general][$option] = $new_value;
		update_option( self::option_key, $options );
	}
	
	/**
	 * Updates a user setting.
	 *
	 * @param string $option the option's id
	 * @param mixed $new_value the new value
	 * @param int $user_id update option for this user, defaults to null for current user
	 */
	public static function update_user_option( $option, $new_value, $user_id = null ) {
		
		if ( $user_id === null ) {
			$current_user = wp_get_current_user();
			if ( !empty( $current_user ) ) {
				$user_id = $current_user->ID;
			}
		}
		
		if ( $user_id !== null ) {
			$options = self::get_options();
			$options[$user_id][$option] = $new_value;
			update_option( self::option_key, $options );
		}
	}
	
	/**
	 * Deletes a general setting.
	 *
	 * @param string $option the option's id
	 */
	public static function delete_option( $option ) {
		$options = self::get_options();
		if ( isset( $options[self::general][$option] ) ) {
			unset( $options[self::general][$option] );
			update_option( self::option_key, $options );
		}
	}
	
	/**
	 * Deletes a user setting.
	 * 
	 * @param string $option the option's id
	 * @param int $user_id delete option for this user, defaults to null for current user
	 */
	public static function delete_user_option( $option, $user_id = null ) {
		
		if ( $user_id === null ) {
			$current_user = wp_get_current_user();
			if ( !empty( $current_user ) ) {
				$user_id = $current_user->ID;
			}
		}
		
		if ( $user_id !== null ) {
			$options = self::get_options();
			if ( isset( $options[$user_id][$option] ) ) {
				unset( $options[$user_id][$option] );
				update_option( self::option_key, $options );
			}
		}
	}
	
	/**
	 * Deletes all settings - this includes user and general options.
	 */
	public static function flush_options() {
		delete_option( self::option_key );
	}
}
