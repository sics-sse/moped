<?php
/**
* class-groups-capability.php
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
 * Capability OPM
 */
class Groups_Capability {
	
	/**
	 * @var persisted capability object
	 */
	var $capability = null;
	
	/**
	 * Create by capability id.
	 * Must have been persisted.
	 * @param int $capability_id
	 */
	public function __construct( $capability_id ) {
		$this->capability = self::read( $capability_id );
	}
	
	/**
	 * Retrieve a property by name.
	 * 
	 * Possible properties:
	 * - capability_id
	 * - capability
	 * - class
	 * - object
	 * - name
	 * - description
	 * 
	 * @param string $name property's name
	 * @return property value, will return null if property does not exist
	 */
	public function __get( $name ) {
		$result = null;
		if ( $this->capability !== null ) {
			switch( $name ) {
				case "capability_id" :
				case "capability" :
				case "class" :
				case "object" :
				case "name" :
				case "description" :
					$result = $this->capability->$name;
					break;
			}
		}
		return $result;
	}
	
	/**
	 * Persist a capability.
	 * 
	 * Possible keys in $map:
	 * 
	 * - "capability" (required) - unique capability label, max 20 characters
	 * - "class" (optional) - class the capability applies to, max 100 chars
	 * - "object" (optional) - identifies object of that class, max 100 chars
	 * - "name" (optional) - name it if you have to
	 * - "description" (optional) - dito
	 * 
	 * @param array $map attributes, requires at least: "capability"
	 * @return capability_id on success, otherwise false
	 */
	public static function create( $map ) {
		
		global $wpdb;
		extract( $map );
		$result = false;
		
		if ( !empty( $capability ) ) {
			
			if ( self::read_by_capability( $capability ) === false ) {
			
				$data = array(
					'capability' => $capability
				);
				$formats = array( '%s' );
				
				if ( !empty( $class ) ) {
					$data['class'] = $class;
					$formats[] = '%s';
				}
				if ( !empty( $object ) ) {
					$data['object'] = $object;
					$formats[] = '%s';
				}
				if ( !empty( $name ) ) {
					$data['name'] = $name;
					$formats[] = '%s';
				}
				if ( !empty( $description ) ) {
					$data['description'] = $description;
					$formats[] = '%s';
				}
				$capability_table = _groups_get_tablename( 'capability' );
				if ( $wpdb->insert( $capability_table, $data, $formats ) ) {
					if ( $result = $wpdb->get_var( "SELECT LAST_INSERT_ID()" ) ) {
						do_action( "groups_created_capability", $result );
					}
				}
			}
		}
		return $result;
	}
	
	/**
	 * Retrieve a capability.
	 * 
	 * Use Groups_Capability::read_capability() if you are trying to retrieve a capability by its unique label.
	 * 
	 * @see Groups_Capability::read_by_capability()
	 * @param int $capability_id capability's id
	 * @return object upon success, otherwise false
	 */
	public static function read( $capability_id ) {
		global $wpdb;
		$result = false;
		$capability_table = _groups_get_tablename( 'capability' );
		$capability = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $capability_table WHERE capability_id = %d",
			Groups_Utility::id( $capability_id )
		) );
		if ( isset( $capability->capability_id ) ) {
			$result = $capability;
		}
		return $result;
	}

	/**
	 * Retrieve a capability by its unique label.
	 * 
	 * @param string $capability capability's unique label
	 * @return object upon success, otherwise false
	 */
	public static function read_by_capability( $capability ) {
		global $wpdb;
		$result = false;
		
		$capability_table = _groups_get_tablename( 'capability' );
		$capability = $wpdb->get_row( $wpdb->prepare(
			"SELECT * FROM $capability_table WHERE capability = %s",
			$capability
		) );
		if ( isset( $capability->capability_id ) ) {
			$result = $capability;
		}
		return $result;
	}
	
	
	/**
	 * Update capability.
	 * 
	 * @param array $map capability attribute, must contain capability_id
	 * @return capability_id on success, otherwise false
	 */
	public static function update( $map ) {
		
		global $wpdb;
		extract( $map );
		$result = false;
		
		if ( isset( $capability_id ) && !empty( $capability ) ) {
			$capability_table = _groups_get_tablename( 'capability' );
			$old_capability = Groups_Capability::read( $capability_id );
			if ( $old_capability ) {
				if ( isset( $capability ) ) {
					$old_capability->capability = $capability;
				}
				if ( isset( $class ) ) {
					$old_capability->class = $class;
				}
				if ( isset( $object ) ) {
					$old_capability->object = $object;
				}
				if ( isset( $name ) ) {
					$old_capability->name = $name;
				}
				if ( isset( $description ) ) {
					$old_capability->description = $description;
				}
				$rows = $wpdb->query( $wpdb->prepare(
					"UPDATE $capability_table SET capability = %s, class = %s, object = %s, name = %s, description = %s WHERE capability_id = %d",
					$old_capability->capability,
					$old_capability->class,
					$old_capability->object,
					$old_capability->name,
					$old_capability->description,
					Groups_Utility::id( $capability_id )
				) );
				if ( ( $rows !== false ) && ( $rows > 0 ) ) {
					$result = $capability_id;
					do_action( "groups_updated_capability", $result );
				}
			}
		}
		return $result;
	}
	
	/**
	 * Remove capability and its relations.
	 * 
	 * @param int $capability_id
	 * @return capability_id if successful, false otherwise
	 */
	public static function delete( $capability_id ) {

		global $wpdb;
		$result = false;
		
		// avoid nonsense requests
		if ( $capability = Groups_Capability::read( $capability_id ) ) {
			$capability_table = _groups_get_tablename( 'capability' );
			// get rid of it
			if ( $rows = $wpdb->query( $wpdb->prepare(
				"DELETE FROM $capability_table WHERE capability_id = %d",
				Groups_Utility::id( $capability_id )
			) ) ) {
				$result = $capability_id;
				do_action( "groups_deleted_capability", $result );
			}
		}
		return $result;
	}
}