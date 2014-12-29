<?php
/**
 * groups-admin-capability-edit.php
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
 * Show edit capability form.
 * @param int $capability_id capability id
 */
function groups_admin_capabilities_edit( $capability_id ) {
	
	global $wpdb;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$capability = Groups_Capability::read( intval( $capability_id ) );
	
	if ( empty( $capability ) ) {
		wp_die( __( 'No such capability.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$current_url = ( is_ssl() ? 'https://' : 'http://' ) . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];
	$current_url = remove_query_arg( 'action', $current_url );
	$current_url = remove_query_arg( 'capability_id', $current_url );
	
	$capability_capability  = isset( $_POST['capability-field'] ) ? $_POST['capability-field'] : $capability->capability;
	$description = isset( $_POST['description-field'] ) ? $_POST['description-field'] : $capability->description;
	
	$capability_readonly = ( $capability->capability !== Groups_Post_Access::READ_POST_CAPABILITY ) ? "" : ' readonly="readonly" ';
	
	$output =
		'<div class="manage-capabilities">' .
		'<div>' .
			'<h2>' .
				__( 'Edit a capability', GROUPS_PLUGIN_DOMAIN ) .
			'</h2>' .
		'</div>' .
	
		'<form id="edit-capability" action="' . $current_url . '" method="post">' .
		'<div class="capability edit">' .
		'<input id="capability-id-field" name="capability-id-field" type="hidden" value="' . esc_attr( intval( $capability_id ) ) . '"/>' .
		
		'<div class="field">' .
		'<label for="capability-field" class="field-label first required">' .__( 'Capability', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		'<input ' . $capability_readonly . ' id="capability-field" name="capability-field" class="capability-field" type="text" value="' . esc_attr( $capability_capability ) . '"/>' .
		'</div>' .
			
		'<div class="field">' .
		'<label for="description-field" class="field-label description-field">' .__( 'Description', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		'<textarea id="description-field" name="description-field" rows="5" cols="45">' . wp_filter_nohtml_kses( $description ) . '</textarea>' .
		'</div>' .
	
		'<div class="field">' .
		wp_nonce_field( 'capabilities-edit', GROUPS_ADMIN_GROUPS_NONCE, true, false ) .
		'<input class="button" type="submit" value="' . __( 'Save', GROUPS_PLUGIN_DOMAIN ) . '"/>' .
		'<input type="hidden" value="edit" name="action"/>' .
		'<a class="cancel" href="' . $current_url . '">' . __( 'Cancel', GROUPS_PLUGIN_DOMAIN ) . '</a>' .
		'</div>' .
		'</div>' . // .capability.edit
		'</form>' .
		'</div>'; // .manage-capabilities
	
		echo $output;
	
	Groups_Help::footer();
} // function groups_admin_capabilities_edit

/**
 * Handle edit form submission.
 */
function groups_admin_capabilities_edit_submit() {
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_GROUPS_NONCE],  'capabilities-edit' ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$capability_id = isset( $_POST['capability-id-field'] ) ? $_POST['capability-id-field'] : null;
	$capability = Groups_Capability::read( $capability_id );
	if ( $capability ) {
		$capability_id	= $capability->capability_id;
		if ( $capability->capability !== Groups_Post_Access::READ_POST_CAPABILITY ) {
			$capability = isset( $_POST['capability-field'] ) ? $_POST['capability-field'] : null;
		} else {
			$capability = Groups_Post_Access::READ_POST_CAPABILITY;
		}
		$description = isset( $_POST['description-field'] ) ? $_POST['description-field'] : '';
		return Groups_Capability::update( compact( "capability_id", "capability", "description" ) );
	} else {
		return false;
	}
	
} // function groups_admin_capabilities_edit_submit
