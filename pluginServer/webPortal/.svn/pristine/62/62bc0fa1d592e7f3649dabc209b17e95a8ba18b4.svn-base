<?php
/**
 * groups-admin-groups-add.php
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
 * @since groups 1.1.0
 */

/**
 * Show add group form.
 */
function groups_admin_groups_add() {
	
	global $wpdb;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$current_url = ( is_ssl() ? 'https://' : 'http://' ) . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];
	$current_url = remove_query_arg( 'paged', $current_url );
	$current_url = remove_query_arg( 'action', $current_url );
	$current_url = remove_query_arg( 'group_id', $current_url );
	
	$parent_id   = isset( $_POST['parent-id-field'] ) ? $_POST['parent-id-field'] : '';
	$name		= isset( $_POST['name-field'] ) ? $_POST['name-field'] : '';
	$description = isset( $_POST['description-field'] ) ? $_POST['description-field'] : '';
	
	$group_table = _groups_get_tablename( 'group' );
	$parent_select = '<select name="parent-id-field">';
	$parent_select .= '<option value="">--</option>';
	$groups = $wpdb->get_results( "SELECT * FROM $group_table" );
	foreach ( $groups as $group ) {
		$parent_select .= '<option value="' . esc_attr( $group->group_id ) . '">' . wp_filter_nohtml_kses( $group->name ) . '</option>';
	}
	$parent_select .= '</select>';
		
	$output =
		'<div class="manage-groups">' .
		'<div>' .
			'<h2>' .
				__( 'Add a new group', GROUPS_PLUGIN_DOMAIN ) .
			'</h2>' .
		'</div>' .
	
		'<form id="add-group" action="' . $current_url . '" method="post">' .
		'<div class="group new">' .
		
		'<div class="field">' .
		'<label for="name-field" class="field-label first required">' .__( 'Name', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		'<input id="name-field" name="name-field" class="namefield" type="text" value="' . esc_attr( $name ) . '"/>' .
		'</div>' .
		
		'<div class="field">' .
		'<label for="parent-id-field" class="field-label">' .__( 'Parent', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		$parent_select .
		'</div>' .		
		
		'<div class="field">' .
		'<label for="description-field" class="field-label description-field">' .__( 'Description', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		'<textarea id="description-field" name="description-field" rows="5" cols="45">' . wp_filter_nohtml_kses( $description ) . '</textarea>' .
		'</div>' .
	
		'<div class="field">' .
		wp_nonce_field( 'groups-add', GROUPS_ADMIN_GROUPS_NONCE, true, false ) .
		'<input class="button" type="submit" value="' . __( 'Add', GROUPS_PLUGIN_DOMAIN ) . '"/>' .
		'<input type="hidden" value="add" name="action"/>' .
		'<a class="cancel" href="' . $current_url . '">' . __( 'Cancel', GROUPS_PLUGIN_DOMAIN ) . '</a>' .
		'</div>' .
		'</div>' . // .group.new
		'</form>' .
		'</div>'; // .manage-groups
	
		echo $output;
		
	Groups_Help::footer();
} // function groups_admin_groups_add

/**
 * Handle add group form submission.
 * @return int new group's id or false if unsuccessful
 */
function groups_admin_groups_add_submit() {
	
	global $wpdb;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_GROUPS_NONCE], 'groups-add' ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$creator_id  = get_current_user_id();
	$datetime	= date( 'Y-m-d H:i:s', time() );
	$parent_id   = isset( $_POST['parent-id-field'] ) ? $_POST['parent-id-field'] : null;
	$description = isset( $_POST['description-field'] ) ? $_POST['description-field'] : '';
	$name		= isset( $_POST['name-field'] ) ? $_POST['name-field'] : null;
	return Groups_Group::create( compact( "creator_id", "datetime", "parent_id", "description", "name" ) );
} // function groups_admin_groups_add_submit
