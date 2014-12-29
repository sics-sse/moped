<?php
/**
 * class-groups-group.php
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
require_once( GROUPS_CORE_LIB . "/interface-i-capable.php" );

/**
 * Group OPM.
 */
class Groups_Group implements I_Capable {
	
	/**
	 * @var Object Persisted group.
	 */
	var $group = null;
		
	/**
	 * Create by group id.
	 * Must have been persisted.
	 * @param int $group_id
	 */
	public function __construct( $group_id ) {
		$this->group = self::read( $group_id );
	}
	
	/**
	 * Retrieve a property by name.
	 *
	 * Possible properties:
	 * - group_id
	 * - parent_id
	 * - creator_id
	 * - datetime
	 * - name
	 * - description
	 * - capabilities, returns an array of Groups_Capability
	 * - users, returns an array of Groups_User
	 *
	 * @param string $name property's name
	 * @return property value, will return null if property does not exist
	 */
	public function __get( $name ) {
		global $wpdb;
		$result = null;
		if ( $this->group !== null ) {
			switch( $name ) {
				case "group_id" :
				case "parent_id" :
				case "creator_id" :
				case "datetime" :
				case "name" :
				case "description" :
					$result = $this->group->$name;
					break;
				case "capabilities" :
					$group_capability_table = _groups_get_tablename( "group_capability" );
					$rows = $wpdb->get_results( $wpdb->prepare(
						"SELECT capability_id FROM $group_capability_table WHERE group_id = %d",
						Groups_Utility::id( $this->group->group_id )
					) );
					if ( $rows ) {
						$result = array();
						foreach ( $rows as $row ) {
							$result[] = new Groups_Capability( $row->capability_id );
						}
					}
					break;
				case 'users' :
					$user_group_table = _groups_get_tablename( "user_group" );
					$users = $wpdb->get_results( $wpdb->prepare(
						"SELECT ID FROM $wpdb->users LEFT JOIN $user_group_table ON $wpdb->users.ID = $user_group_table.user_id WHERE $user_group_table.group_id = %d",
						Groups_Utility::id( $this->group->group_id )
					) );
					if ( $users ) {
						$result = array();
						foreach( $users as $user ) {
							$result[] = new Groups_User( $user->ID );
						}
					}
					break;
			}
		}
		return $result;
	}
	
	/**
	 * (non-PHPdoc)
	 * @see I_Capable::can()
	 */
	public function can( $capability ) {

		global $wpdb;
		$result = false;
		
		if ( $this->group !== null ) {
			
			$group_table = _groups_get_tablename( "group" );
			$capability_table = _groups_get_tablename( "capability" );
			$group_capability_table = _groups_get_tablename( "group_capability" );
			
			// determine capability id 
			$capability_id = null;
			if ( is_numeric( $capability ) ) {
				$capability_id = Groups_Utility::id( $capability );
			} else if ( is_string( $capability ) ) {
				$capability_id = $wpdb->get_var( $wpdb->prepare(
					"SELECT capability_id FROM $capability_table WHERE capability = %s",
					$capability
				) );
			}
			
			if ( $capability_id !== null ) {
				// check if the group itself can
				$result = ( Groups_Group_Capability::read( $this->group->group_id, $capability_id ) !== false );
				if ( !$result ) {
					// find all parent groups and include in the group's
					// upward hierarchy to see if any of these can
					$group_ids		   = array( $this->group->group_id );
					$iterations		  = 0;
					$old_group_ids_count = 0;
					$all_groups = $wpdb->get_var( "SELECT COUNT(*) FROM $group_table" );
					while( ( $iterations < $all_groups ) && ( count( $group_ids ) !== $old_group_ids_count ) ) {
						$iterations++;
						$old_group_ids_count = count( $group_ids );
						$id_list = implode( ",", $group_ids );
						$parent_group_ids = $wpdb->get_results(
							"SELECT parent_id FROM $group_table WHERE parent_id IS NOT NULL AND group_id IN ($id_list)"
						);
						if ( $parent_group_ids ) {
							foreach( $parent_group_ids as $parent_group_id ) {
								$parent_group_id = Groups_Utility::id( $parent_group_id->parent_id );
								if ( !in_array( $parent_group_id, $group_ids ) ) {
									$group_ids[] = $parent_group_id;
								}
							}
						}
					}
					if ( count( $group_ids ) > 0 ) {
						$id_list = implode( ",", $group_ids );
						$rows = $wpdb->get_results( $wpdb->prepare(
							"SELECT capability_id FROM $group_capability_table WHERE capability_id = %d AND group_id IN ($id_list)",
							Groups_Utility::id( $capability_id )
						) );
						
						if ( count( $rows ) > 0 ) {
							$result = true;
						}
					}
				}
			}
		}
		$result = apply_filters_ref_array( "groups_group_can", array( $result, &$this, $capability ) );
		return $result;
	}
	
	/**
	 * Persist a group.
	 * 
	 * Parameters:
	 * - name (required) - the group's name
	 * - creator_id (optional) - defaults to the current user's id
	 * - datetime (optional) - defaults to now
	 * - description (optional)
	 * - parent_id (optional)
	 * 
	 * @param array $map attributes
	 * @return group_id on success, otherwise false
	 */
	public static function create( $map ) {
		global $wpdb;
		extract( $map );
		$result = false;
		$error = false;		
		
		if ( !empty( $name ) ) {
			
			$group_table = _groups_get_tablename( "group" );
			
			$data = array( 'name' => $name );
			$formats = array( '%s' );
			if ( !isset( $creator_id ) ) {
				$creator_id = get_current_user_id();
			}
			if ( isset( $creator_id ) ) {
				$data['creator_id'] = Groups_Utility::id( $creator_id );
				$formats[] = '%d';
			}
			if ( !isset( $datetime ) ) {
				$datetime = date( 'Y-m-d H:i:s', time() );
			}
			if ( isset( $datetime ) ) {
				$data['datetime'] = $datetime;
				$formats[] = '%s';
			}
			if ( !empty( $description ) ) {
				$data['description'] = $description;
				$formats[] = '%s';
			}
			if ( !empty( $parent_id ) ) {
				// only allow to set an existing parent group (that is from the same blog)
				$parent_group_id = $wpdb->get_var( $wpdb->prepare(
					"SELECT group_id FROM $group_table WHERE group_id = %d",
					Groups_Utility::id( $parent_id )
				) );
				if ( $parent_group_id === $parent_id ) {
					$data['parent_id'] = Groups_Utility::id( $parent_id );
					$formats[] = '%d';
				} else {
					$error = true;
				}
			}
			// no duplicate names
			$duplicate = Groups_Group::read_by_name( $name );
			if ( $duplicate ) {
				$error = true;
			}
			if ( !$error ) {
				if ( $wpdb->insert( $group_table, $data, $formats ) ) {
					if ( $result = $wpdb->get_var( "SELECT LAST_INSERT_ID()" ) ) {
						do_action( "groups_created_group", $result );
					}
				}
			}
		}
		return $result;
	}
	
	/**
	 * Retrieve a group.
	 * 
	 * @param int $group_id group's id
	 * @return object upon success, otherwise false
	 */
	public static function read( $group_id ) {
		global $wpdb;
		$result = false;
		
		$group_table = _groups_get_tablename( 'group' );
		$group = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $group_table WHERE group_id = %d",
			Groups_Utility::id( $group_id )
		) );
		if ( isset( $group->group_id ) ) {
			$result = $group;
		}
		return $result;
	}
	
	/**
	 * Retrieve a group by name.
	 *
	 * @param string $name the group's name
	 * @return object upon success, otherwise false
	 */
	public static function read_by_name( $name ) {
		global $wpdb;
		$result = false;
		$group_table = _groups_get_tablename( 'group' );
		$group = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $group_table WHERE name = %s",
			$name
		) );
		if ( isset( $group->group_id ) ) {
			$result = $group;
		}
		return $result;
	}
	
	/**
	 * Update group.
	 * 
	 * @param array $map group attribute, must contain group_id
	 * @return group_id on success, otherwise false
	 */
	public static function update( $map ) {
		
		global $wpdb;
		extract( $map );
		$result = false;
		
		if ( isset( $group_id ) && !empty( $name ) ) {
			$group_table = _groups_get_tablename( 'group' );
			if ( !isset( $description ) || ( $description === null ) ) {
				$description = '';
			}
			$wpdb->query( $wpdb->prepare(
				"UPDATE $group_table SET name = %s, description = %s WHERE group_id = %d",
				$name,
				$description,
				Groups_Utility::id( $group_id )
			) );
			if ( empty( $parent_id ) ) {
				$wpdb->query( $wpdb->prepare(
					"UPDATE $group_table SET parent_id = NULL WHERE group_id = %d",
					Groups_Utility::id( $group_id )
				) );
			} else {
				 
				// Prohibit circular dependencies:
				// This group cannot have a parent that is its successor
				// at any level in its successor hierarchy.
				// S(g)  : successor of group g
				// S*(g) : successors of group g, any level deep
				// P(g)  : parent of g
				// --- 
				// It must hold: !( P(g) in S*(g) )
				
				// Find all successors of this group
				$groups = $wpdb->get_var( "SELECT COUNT(*) FROM $group_table" );
				if ( $groups !== null ) {
					$group_ids		   = array();
					$group_ids[]		 = Groups_Utility::id( $group_id );
					$iterations		  = 0;
					$old_group_ids_count = 0;
					while( ( $iterations < $groups ) && ( count( $group_ids ) > 0 ) && ( count( $group_ids ) !== $old_group_ids_count ) ) {
						
						$iterations++;
						$old_group_ids_count = count( $group_ids );
						
						$id_list	 = implode( ",", $group_ids );
						// We can trust ourselves here, no need to use prepare()
						// but careful if this query is modified!
						$successor_group_ids = $wpdb->get_results(
							"SELECT group_id FROM $group_table WHERE parent_id IS NOT NULL AND parent_id IN ($id_list)"
						);
						if ( $successor_group_ids ) {
							foreach( $successor_group_ids as $successor_group_id ) {
								$successor_group_id = Groups_Utility::id( $successor_group_id->group_id );
								if ( !in_array( $successor_group_id, $group_ids ) ) {
									$group_ids[] = $successor_group_id;
								}
							}
						}
					}
					// only add if condition holds
					if ( !in_array( Groups_Utility::id( $parent_id ), $group_ids ) ) {
						$wpdb->query( $wpdb->prepare(
							"UPDATE $group_table SET parent_id = %d WHERE group_id = %d",
							Groups_Utility::id( $parent_id),
							Groups_Utility::id( $group_id )
						) );
					}
				}
			}
			$result = $group_id;
			do_action( "groups_updated_group", $result );
		}
		return $result;
	}
	
	/**
	 * Remove group and its relations.
	 * 
	 * @param int $group_id
	 * @return group_id if successful, false otherwise
	 */
	public static function delete( $group_id ) {

		global $wpdb;
		$result = false;
		
		if ( $group = self::read( $group_id ) ) {
			
			// delete group-capabilities
			$group_capability_table = _groups_get_tablename( 'group_capability' );
			$wpdb->query( $wpdb->prepare(
				"DELETE FROM $group_capability_table WHERE group_id = %d",
				Groups_Utility::id( $group->group_id )
			) );
			
			// delete group-users
			$user_group_table = _groups_get_tablename( 'user_group' );
			$wpdb->query( $wpdb->prepare(
				"DELETE FROM $user_group_table WHERE group_id = %d",
				$group->group_id
			) );
			
			// set parent_id to null where this group is parent
			$group_table = _groups_get_tablename( 'group' );
			$wpdb->query( $wpdb->prepare(
				"UPDATE $group_table SET parent_id = NULL WHERE parent_id = %d",
				$group->group_id
			) );
			
			// delete group
			if ( $wpdb->query( $wpdb->prepare(
				"DELETE FROM $group_table WHERE group_id = %d",
				$group->group_id
			) ) ) {
				$result = $group->group_id;
				do_action( "groups_deleted_group", $result );
			}
		}
		return $result;
	}
}