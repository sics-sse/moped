<?php
/**
 * groups-admin-capabilities-remove.php
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
 * Shows form to confirm capability deletion.
 * @param int $capability_id capability id
 */
function groups_admin_capabilities_remove( $capability_id ) {
	
	global $wpdb;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$capability = Groups_Capability::read( intval( $capability_id ) );
	
	if ( empty( $capability ) ) {
		wp_die( __( 'No such capability.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$capability_table = _groups_get_tablename( 'capability' );
		
	$current_url = ( is_ssl() ? 'https://' : 'http://' ) . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];
	$current_url = remove_query_arg( 'action', $current_url );
	$current_url = remove_query_arg( 'capability_id', $current_url );
	
	$output =
		'<div class="manage-capabilities">' .
		'<div>' .
			'<h2>' .
				__( 'Remove a capability', GROUPS_PLUGIN_DOMAIN ) .
			'</h2>' .
		'</div>' .
		'<form id="remove-capability" action="' . $current_url . '" method="post">' .
		'<div class="capability remove">' .
		'<input id="capability-id-field" name="capability-id-field" type="hidden" value="' . esc_attr( intval( $capability->capability_id ) ) . '"/>' .
		'<ul>' .
		'<li>' . sprintf( __( 'Capability : %s', GROUPS_PLUGIN_DOMAIN ), wp_filter_nohtml_kses( $capability->capability ) ) . '</li>' .
		'</ul> ' .
		wp_nonce_field( 'capabilities-remove', GROUPS_ADMIN_GROUPS_NONCE, true, false ) .
		'<input class="button" type="submit" value="' . __( 'Remove', GROUPS_PLUGIN_DOMAIN ) . '"/>' .
		'<input type="hidden" value="remove" name="action"/>' .
		'<a class="cancel" href="' . $current_url . '">' . __( 'Cancel', GROUPS_PLUGIN_DOMAIN ) . '</a>' .
		'</div>' .
		'</div>' . // .capability.remove
		'</form>' .
		'</div>'; // .manage-capabilities
	
	echo $output;
	
	Groups_Help::footer();
} // function groups_admin_capabilities_remove

/**
 * Handle remove form submission.
 */
function groups_admin_capabilities_remove_submit() {
	
	global $wpdb;
	
	$result = false;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_GROUPS_NONCE], 'capabilities-remove' ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$capability_id = isset( $_POST['capability-id-field'] ) ? $_POST['capability-id-field'] : null;
	$capability = Groups_Capability::read( $capability_id );
	if ( $capability ) {
		if ( $capability->capability !== Groups_Post_Access::READ_POST_CAPABILITY ) {
			$result = Groups_Capability::delete( $capability_id );
		}
	}
	return $result;
} // function groups_admin_capabilities_remove_submit
?>