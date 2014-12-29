<?php
/**
 * class-groups-user-capability.php
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
 * User Capability OPM.
 */
class Groups_User_Capability {
	
	/**
	 * Hook into appropriate actions.
	 *
	 * @see wp_delete_user()
	 * @see remove_user_from_blog()
	 */
	public static function init() {

		// when a user is deleted, user-capabilities must be removed
		// triggered by wp_delete_user()
		add_action( "deleted_user", array( __CLASS__, "deleted_user" ) );
		// when a capability is deleted the relationship must also be resolved
		add_action( 'groups_deleted_capability', array( __CLASS__, 'groups_deleted_capability' ) );
	}
	
	/**
	 * Persist a user-capability relation.
	 * 
	 * @param array $map attributes - must provide user_id and capability_id
	 * @return true on success, otherwise false
	 */
	public static function create( $map ) {
		
		global $wpdb;
		extract( $map );
		$result = false;

		// avoid nonsense requests
//		if ( !empty( $user_id ) && !empty( $capability_id) ) {
		if ( !empty( $capability_id) ) {
			// make sure user and capability exist
			if ( ( false !== Groups_Utility::id( $user_id ) ) && get_user_by( "id", $user_id ) && Groups_Capability::read( $capability_id ) ) {
				$user_capability_table = _groups_get_tablename( 'user_capability' );
				// don't try to create duplicate entries
				// also it would raise an error for duplicate PK
				if ( 0 === intval( $wpdb->get_var( $wpdb->prepare(
					"SELECT COUNT(*) FROM $user_capability_table WHERE user_id = %d AND capability_id = %d",
					Groups_Utility::id( $user_id ),
					Groups_Utility::id( $capability_id )
				) ) ) ) {
					$data = array(
						'user_id' => Groups_Utility::id( $user_id ),
						'capability_id' => Groups_Utility::id( $capability_id )
					);
					$formats = array( '%d', '%d' );
					if ( $wpdb->insert( $user_capability_table, $data, $formats ) ) {
						$result = true;
						do_action( "groups_created_user_capability", $user_id, $capability_id );
					}
				}
			}

		}
		return $result;
	}
	
	/**
	 * Retrieve a user-capability relation.
	 * 
	 * @param int $user_id user's id
	 * @param int $capability_id capability's id
	 * @return object upon success, otherwise false
	 */
	public static function read( $user_id, $capability_id ) {
		global $wpdb;
		$result = false;
		
		$user_capability_table = _groups_get_tablename( 'user_capability' );
		$user_capability = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $user_capability_table WHERE user_id = %d AND capability_id = %d",
			Groups_Utility::id( $user_id ),
			Groups_Utility::id( $capability_id )
		) );
		if ( $user_capability !== null ) {
			$result = $user_capability;
		}
		return $result;
	}
	
	/**
	 * Update user-capability relation.
	 * 
	 * This changes nothing so as of now it's pointless to even call this.
	 * 
	 * @param array $map
	 * @return true if successful, false otherwise
	 */
	public static function update( $map ) {
		$result = false;
//		if ( !empty( $user_id ) && !empty( $capability_id) ) {
		if ( !empty( $capability_id) ) {
			// make sure user and capability exist
			if ( ( false !== Groups_Utility::id( $user_id ) ) && get_user_by( "id", $user_id ) && Groups_Capability::read( $capability_id ) ) {
				$result = true;
				do_action( "groups_updated_user_capability", $user_id, $capability_id );
			}
		}
		return $result;
	}
	
	/**
	 * Remove user-capability relation.
	 * 
	 * @param int $user_id
	 * @param int $capability_id
	 * @return true if successful, false otherwise
	 */
	public static function delete( $user_id, $capability_id ) {

		global $wpdb;
		$result = false;
		
		// avoid nonsense requests
//		if ( !empty( $user_id ) && !empty( $capability_id) ) {
		if ( !empty( $capability_id) ) {
			// to allow deletion of an entry after a user has been deleted,
			// we don't check if the user exists
			$user_capability_table = _groups_get_tablename( 'user_capability' );
			// get rid of it
			$rows = $wpdb->query( $wpdb->prepare(
				"DELETE FROM $user_capability_table WHERE user_id = %d AND capability_id = %d",
				Groups_Utility::id( $user_id ),
				Groups_Utility::id( $capability_id )
			) );
			// must have affected a row, otherwise no great success
			$result = ( $rows !== false ) && ( $rows > 0 );
			if ( $result ) {
				do_action( "groups_deleted_user_capability", $user_id, $capability_id );
			}
		}
		return $result;
	}
	
	/**
	 * Hooks into the deleted_user action to remove the deleted user from
	 * all capabilities it is related to.
	 *
	 * @param int $user_id
	 */
	public static function deleted_user( $user_id ) {
		global $wpdb;
	
		$user_capability_table = _groups_get_tablename( "user_capability" );
		$rows = $wpdb->get_results( $wpdb->prepare(
			"SELECT * FROM $user_capability_table WHERE user_id = %d",
			Groups_Utility::id( $user_id )
		) );
		if ( $rows ) {
			foreach( $rows as $row ) {
				// don't optimize that in preference of a standard deletion
				// process (trigger actions ...)
				self::delete( $row->user_id, $row->capability_id );
			}
		}
	}
	
	/**
	 * Hooks into groups_deleted_capability to resolve all existing relations
	 * between users and the deleted capability.
	 * @param int $capability_id
	 */
	public static function groups_deleted_capability( $capability_id ) {
		global $wpdb;

		$user_capability_table = _groups_get_tablename( "user_capability" );
		$rows = $wpdb->get_results( $wpdb->prepare(
			"SELECT * FROM $user_capability_table WHERE capability_id = %d",
			Groups_Utility::id( $capability_id )
		) );
		if ( $rows ) {
			foreach( $rows as $row ) {
				// do NOT 'optimize' (must trigger actions ... same as above)
				self::delete( $row->user_id, $row->capability_id );
			}
		}
	}
}
Groups_User_Capability::init();
