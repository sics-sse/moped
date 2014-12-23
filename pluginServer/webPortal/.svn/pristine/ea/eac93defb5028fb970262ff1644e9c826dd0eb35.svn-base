<?php
/**
 * class-groups-user-group.php
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
 * User Group OPM.
 */
class Groups_User_Group {
	
	/**
	 * @var persisted object
	 */
	var $user_group = null;
	
	/**
	 * Hook into appropriate actions.
	 * @see wp_delete_user()
	 * @see remove_user_from_blog()
	 */
	public static function init() {
		// when a user is deleted, it must be removed from all groups it
		// belongs to - triggered by wp_delete_user() and wpmu_delete_user()
		add_action( "deleted_user", array( __CLASS__, "deleted_user" ) );
		
		// when a user is removed from a blog, the user must be removed
		// from all groups in that blog that it belongs to
		add_action( "remove_user_from_blog", array( __CLASS__, "remove_user_from_blog" ), 10, 2 );
	}
	
	/**
	 * Create by user and group id.
	 * Must have been persisted.
	 * @param int $user_id
	 * @param int $group_id
	 */
	public function __construct( $user_id, $group_id ) {
		$this->user_group = self::read( $user_id, $group_id );
	}
	
	/**
	 * Retrieve a property by name.
	 * 
	 * Possible properties:
	 * - user_id
	 * - group_id
	 * 
	 * @param string $name property's name
	 * @return property value, will return null if property does not exist
	 */
	public function __get( $name ) {
		$result = null;
		if ( $this->user_group !== null ) {
			switch( $name ) {
				case "user_id" :
				case "group_id" :
					$result = $this->user_group->$name;
					break;
			}
		}
		return $result;
	}
	
	/**
	 * Persist a user-group relation.
	 * 
	 * @param array $map attributes - must provide user_id and group_id
	 * @return true on success, otherwise false
	 */
	public static function create( $map ) {
		
		global $wpdb;
		extract( $map );
		$result = false;

		// avoid nonsense requests
//		if ( !empty( $user_id ) && !empty( $group_id ) ) {
		if ( !empty( $group_id ) ) {			
			// make sure user and group exist
			if ( ( false !== Groups_Utility::id( $user_id ) ) && ( $wpdb->get_var( $wpdb->prepare( "SELECT COUNT(ID) FROM $wpdb->users WHERE ID = %d", $user_id ) ) > 0 ) && ( $group = Groups_Group::read( $group_id ) ) ) {
				// only allow to add users to groups if they belong to the
				// group's blog or we have the anonymous user
				if ( is_user_member_of_blog( Groups_Utility::id( $user_id ) ) || ( Groups_Utility::id( $user_id ) === 0 ) ) {
					$user_group_table = _groups_get_tablename( 'user_group' );
					// don't try to create duplicate entries
					// also it would raise an error for duplicate PK
					if ( 0 === intval( $wpdb->get_var( $wpdb->prepare(
						"SELECT COUNT(*) FROM $user_group_table WHERE user_id = %d AND group_id = %d",
						Groups_Utility::id( $user_id ),
						Groups_Utility::id( $group_id ) ) ) )
					) {
						$data = array(
							'user_id' => Groups_Utility::id( $user_id ),
							'group_id' => Groups_Utility::id( $group_id )
						);
						$formats = array( '%d', '%d' );
						if ( $wpdb->insert( $user_group_table, $data, $formats ) ) {
							$result = true;
							do_action( "groups_created_user_group", $user_id, $group_id );
						}
					}
				}
			}
		}
		return $result;
	}
	
	/**
	 * Retrieve a user-group relation.
	 * 
	 * @param int $user_id user's id
	 * @param int $group_id group's id
	 * @return object upon success, otherwise false
	 */
	public static function read( $user_id, $group_id ) {
		global $wpdb;
		$result = false;
		
		$user_group_table = _groups_get_tablename( 'user_group' );
		$user_group = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $user_group_table WHERE user_id = %d AND group_id = %d",
			Groups_Utility::id( $user_id ),
			Groups_Utility::id( $group_id )
		) );
		if ( $user_group !== null ) {
			$result = $user_group;
		}
		return $result;
	}
	
	/**
	 * Update user-group relation.
	 * 
	 * This is a relation and as the relation is, this does nothing and
	 * it SHOULD do nothing.
	 * 
	 * @param array $map
	 * @return true on success, otherwise false
	 */
	public static function update( $map ) {
		$result = false;
//		if ( !empty( $user_id ) && !empty( $group_id) ) {
		if ( !empty( $group_id) ) {
			// make sure user and group exist
			if ( ( false !== Groups_Utility::id( $user_id ) ) && get_user_by( "id", $user_id ) && Groups_Group::read( $group_id ) ) {
				$result = true;
				do_action( "groups_updated_user_group", $user_id, $group_id );
			}
		}
		return $result;
	}
	
	/**
	 * Remove user-group relation.
	 * 
	 * @param int $user_id
	 * @param int $group_id
	 * @return true if successful, false otherwise
	 */
	public static function delete( $user_id, $group_id ) {

		global $wpdb;
		$result = false;
		
		// avoid nonsense requests
//		if ( !empty( $user_id ) && !empty( $group_id ) ) {
		if ( !empty( $group_id ) ) {
			// to allow deletion of an entry after a user has been deleted,
			// we don't check if the user exists
			$user_group_table = _groups_get_tablename( 'user_group' );
			// get rid of it
			$rows = $wpdb->query( $wpdb->prepare(
				"DELETE FROM $user_group_table WHERE user_id = %d AND group_id = %d",
				Groups_Utility::id( $user_id ),
				Groups_Utility::id( $group_id )
			) );
			// must have affected a row, otherwise no great success
			$result = ( $rows !== false ) && ( $rows > 0 );
			if ( $result ) {
				do_action( "groups_deleted_user_group", $user_id, $group_id );
			}
		}
		return $result;
	}
	
	/**
	 * Hooks into the deleted_user action to remove the deleted user from
	 * all groups it belongs to.
	 * 
	 * @param int $user_id
	 */
	public static function deleted_user( $user_id ) {
		global $wpdb;
		
		$user_group_table = _groups_get_tablename( "user_group" );
		$rows = $wpdb->get_results( $wpdb->prepare(
			"SELECT * FROM $user_group_table WHERE user_id = %d",
			Groups_Utility::id( $user_id )
		) );
		if ( $rows ) {
			foreach( $rows as $row ) {
				// don't optimize that in preference of a standard deletion
				// process (trigger actions ...) 
				self::delete( $row->user_id, $row->group_id );
			}
		}
	}
	
	/**
	 * Hooks into the remove_user_from_blog action to remove the user
	 * from groups that belong to that blog.
	 * 
	 *  Note that this is preemptive as there is no
	 *  removed_user_from_blog action.
	 * 
	 * @param int $user_id
	 * @param int $blog_id
	 */
	public static function remove_user_from_blog( $user_id, $blog_id ) {
		
		if ( is_multisite() ) {
			Groups_Controller::switch_to_blog( $blog_id );
		}
		
		global $wpdb;
		
		$group_table = _groups_get_tablename( "group" );
		$user_group_table = _groups_get_tablename( "user_group" );
		// We can end up here while a blog is being deleted, in that case, 
		// the tables have already been deleted.
		if ( ( $wpdb->get_var( "SHOW TABLES LIKE '" . $group_table . "'" ) == $group_table ) &&
			( $wpdb->get_var( "SHOW TABLES LIKE '" . $user_group_table . "'" ) == $user_group_table )
		) {
		
			$rows = $wpdb->get_results( $wpdb->prepare(
				"SELECT * FROM $user_group_table
				LEFT JOIN $group_table ON $user_group_table.group_id = $group_table.group_id
				WHERE $user_group_table.user_id = %d
				",
				Groups_Utility::id( $user_id )
			) );
			if ( $rows ) {
				foreach( $rows as $row ) {
					// don't optimize that, favour standard deletion
					self::delete( $row->user_id, $row->group_id );
				}
			}
		
		}
		
		if ( is_multisite() ) {
			Groups_Controller::restore_current_blog();
		}
	}
}
Groups_User_Group::init();
