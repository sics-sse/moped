<?php
/**
 * class-groups-group-capability.php
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
 * Group Capability OPM
 */
class Groups_Group_Capability {
	
	/**
	 * Hook into appropriate actions when needed.
	 * For now, this does nothing.
	 * 
	 * @see Groups_Group::delete()
	 */
	public static function init() {
		// Note that group-capabilities are deleted when a group is deleted.
	}
	
	/**
	 * Persist a group-capability relation.
	 * 
	 * @param array $map attributes - must provide group_id and capability_id
	 * @return true on success, otherwise false
	 */
	public static function create( $map ) {
		
		global $wpdb;
		extract( $map );
		$result = false;

		// avoid nonsense requests
		if ( !empty( $group_id ) && !empty( $capability_id) ) {
			// make sure group and capability exist
			if ( Groups_Group::read( $group_id ) && Groups_Capability::read( $capability_id ) ) {
				$group_capability_table = _groups_get_tablename( 'group_capability' );
				// don't try to create duplicate entries
				// also it would raise an error for duplicate PK
				if ( 0 === intval( $wpdb->get_var( $wpdb->prepare(
					"SELECT COUNT(*) FROM $group_capability_table WHERE group_id = %d AND capability_id = %d",
					Groups_Utility::id( $group_id ),
					Groups_Utility::id( $capability_id )
				) ) ) ) {
					$data = array(
						'group_id' => Groups_Utility::id( $group_id ),
						'capability_id' => Groups_Utility::id( $capability_id )
					);
					$formats = array( '%d', '%d' );
					if ( $wpdb->insert( $group_capability_table, $data, $formats ) ) {
						$result = true;
						do_action( "groups_created_group_capability", $group_id, $capability_id );
					}
				}
			}
		}
		return $result;
	}
	
	/**
	 * Retrieve a group-capability relation.
	 * 
	 * @param int $group_id group's id
	 * @param int $capability_id capability's id
	 * @return object upon success, otherwise false
	 */
	public static function read( $group_id, $capability_id ) {
		global $wpdb;
		$result = false;
		
		$group_capability_table = _groups_get_tablename( 'group_capability' );
		$group_capability = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $group_capability_table WHERE group_id = %d AND capability_id = %d",
			Groups_Utility::id( $group_id ),
			Groups_Utility::id( $capability_id )
		) );
		if ( $group_capability !== null ) {
			$result = $group_capability;
		}
		return $result;
	}
	
	/**
	 * Update group-capability relation.
	 * 
	 * This changes nothing so as of now it's pointless to even call this.
	 * 
	 * @param array $map
	 * @return true if successful, false otherwise
	 */
	public static function update( $map ) {
		$result = false;
		if ( !empty( $group_id ) && !empty( $capability_id) ) {
			// make sure group and capability exist
			if ( Groups_Group::read( $group_id ) && Groups_Capability::read( $capability_id ) ) {
				$result = true;
				do_action( "groups_updated_group_capability", $group_id, $capability_id );
			}
		}
		return $result;
	}
	
	/**
	 * Remove group-capability relation.
	 * 
	 * @param int $group_id
	 * @param int $capability_id
	 * @return true if successful, false otherwise
	 */
	public static function delete( $group_id, $capability_id ) {

		global $wpdb;
		$result = false;
		
		// avoid nonsense requests
		if ( !empty( $group_id ) && !empty( $capability_id) ) {
			// we can omit checking if the group and capability exist, to
			// allow resolving the relationship after they have been deleted
			$group_capability_table = _groups_get_tablename( 'group_capability' );
			// get rid of it
			$rows = $wpdb->query( $wpdb->prepare(
				"DELETE FROM $group_capability_table WHERE group_id = %d AND capability_id = %d",
				Groups_Utility::id( $group_id ),
				Groups_Utility::id( $capability_id )
			) );
			// must have affected a row, otherwise no great success
			$result = ( $rows !== false ) && ( $rows > 0 );
			if ( $result ) {
				do_action( "groups_deleted_group_capability", $group_id, $capability_id );
			}
		}
		return $result;
	}
}
Groups_Group_Capability::init();
