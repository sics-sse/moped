<?php
/**
 * class-groups-admin-user-profile.php
 *
 * Copyright (c) 2013 "kento" Karim Rahimpur www.itthinx.com
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
 * @since groups 1.3.11
 */

/**
 * Show group info on user profile pages and let admins edit group membership.
 */
class Groups_Admin_User_Profile {

	/**
	 * Adds user profile actions.
	 */
	public static function init() {
		add_action( 'show_user_profile', array( __CLASS__, 'show_user_profile' ) );
		add_action( 'edit_user_profile', array( __CLASS__, 'edit_user_profile' ) );
		add_action( 'personal_options_update', array( __CLASS__, 'personal_options_update' ) );
		add_action( 'edit_user_profile_update', array( __CLASS__, 'edit_user_profile_update' ) );
	}

	/**
	 * Own profile.
	 * @param WP_User $user
	 */
	public static function show_user_profile( $user ) {
		if ( current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
			self::edit_user_profile( $user );
		} else {
			$output = '<h3>' . __( 'Groups', GROUPS_PLUGIN_DOMAIN ) . '</h3>';
			$user = new Groups_User( $user->ID );
			$groups = $user->groups;
			if ( is_array( $groups ) ) {
				if ( count( $groups ) > 0 ) {
					usort( $groups, array( __CLASS__, 'by_group_name' ) );
					$output .= '<ul>';
					foreach( $groups as $group ) {
						$output .= '<li>' . wp_filter_nohtml_kses( $group->name ) . '</li>';
					}
					$output .= '<ul>';
				}
			}
			echo $output;
		}
	}

	/**
	 * Editing a user profile.
	 * @param WP_User $user
	 */
	public static function edit_user_profile( $user ) {
		global $wpdb;
		if ( current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
			$output = '<h3>' . __( 'Groups', GROUPS_PLUGIN_DOMAIN ) . '</h3>';
			$user = new Groups_User( $user->ID );
			$user_groups = $user->groups;
			$groups_table = _groups_get_tablename( 'group' );
			if ( $groups = $wpdb->get_results( "SELECT * FROM $groups_table ORDER BY name" ) ) {
				$output .= '<ul>';
				foreach( $groups as $group ) {
					$is_member = Groups_User_Group::read( $user->ID, $group->group_id ) ? true : false;
					$output .= '<li>';
					$output .= '<label>';
					$output .= sprintf( '<input type="checkbox" name="group_ids[]" value="%d" %s />', Groups_Utility::id( $group->group_id ), $is_member ? ' checked="checked" ' : '' );
					$output .= ' ';
					$output .= wp_filter_nohtml_kses( $group->name );
					$output .= '</label>';
					$output .= '</li>';
				}
				$output .= '</ul>';
			}
		}
		echo $output;
	}
	
	/**
	 * Updates the group membership when a user's own profile is saved - but
	 * for group admins on their own profile page only.
	 * 
	 * @param int $user_id
	 * @see Groups_Admin_User_Profile::edit_user_profile_update()
	 */
	public static function personal_options_update( $user_id ) {
		// We're using the same method as for editing another user's profile,
		// but let's check for group admin here as well. 
		if ( current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
			self::edit_user_profile_update( $user_id );
		}
	}
	
	/**
	 * Updates the group membership.
	 * @param int $user_id
	 */
	public static function edit_user_profile_update( $user_id ) {
		global $wpdb;
		if ( current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
			$groups_table = _groups_get_tablename( 'group' );
			if ( $groups = $wpdb->get_results( "SELECT * FROM $groups_table" ) ) {
				$user_group_ids = isset( $_POST['group_ids'] ) && is_array( $_POST['group_ids'] ) ? $_POST['group_ids'] : array();
				foreach( $groups as $group ) {
					if ( in_array( $group->group_id, $user_group_ids) ) {
						if ( !Groups_User_Group::read( $user_id, $group->group_id ) ) {
							Groups_User_Group::create( array( 'user_id' => $user_id, 'group_id' => $group->group_id ) );
						}
					} else {
						if ( Groups_User_Group::read( $user_id, $group->group_id ) ) {
							Groups_User_Group::delete( $user_id, $group->group_id );
						}
					}
				}
			}
		}
	}

	/**
	 * usort helper
	 * @param Groups_Group $o1
	 * @param Groups_Group $o2
	 * @return int strcmp result for group names
	 */
	public static function by_group_name( $o1, $o2 ) {
		return strcmp( $o1->name, $o2->name );
	}

}
Groups_Admin_User_Profile::init();
