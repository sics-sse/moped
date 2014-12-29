<?php
/**
 * groups-admin-capabilities-add.php
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
 * Show add capability form.
 */
function groups_admin_capabilities_add() {
	
	global $wpdb;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$current_url = ( is_ssl() ? 'https://' : 'http://' ) . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];
	$current_url = remove_query_arg( 'paged', $current_url );
	$current_url = remove_query_arg( 'action', $current_url );
	$current_url = remove_query_arg( 'capability_id', $current_url );
	
	$capability  = isset( $_POST['capability-field'] ) ? $_POST['capability-field'] : '';
	$description = isset( $_POST['description-field'] ) ? $_POST['description-field'] : '';
	
	$capability_table = _groups_get_tablename( 'capability' );
		
	$output =
		'<div class="manage-capabilities">' .
		'<div>' .
			'<h2>' .
				__( 'Add a new capability', GROUPS_PLUGIN_DOMAIN ) .
			'</h2>' .
		'</div>' .
	
		'<form id="add-capability" action="' . $current_url . '" method="post">' .
		'<div class="capability new">' .
		
		'<div class="field">' .
		'<label for="capability-field" class="field-label first required">' .__( 'Capability', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		'<input id="name-field" name="capability-field" class="capability-field" type="text" value="' . esc_attr( $capability ) . '"/>' .
		'</div>' .
		
		'<div class="field">' .
		'<label for="description-field" class="field-label description-field">' .__( 'Description', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
		'<textarea id="description-field" name="description-field" rows="5" cols="45">' . wp_filter_nohtml_kses( $description ) . '</textarea>' .
		'</div>' .
	
		'<div class="field">' .
		wp_nonce_field( 'capabilities-add', GROUPS_ADMIN_GROUPS_NONCE, true, false ) .
		'<input class="button" type="submit" value="' . __( 'Add', GROUPS_PLUGIN_DOMAIN ) . '"/>' .
		'<input type="hidden" value="add" name="action"/>' .
		'<a class="cancel" href="' . $current_url . '">' . __( 'Cancel', GROUPS_PLUGIN_DOMAIN ) . '</a>' .
		'</div>' .
		'</div>' . // .capability.new
		'</form>' .
		'</div>'; // .manage-capabilities
	
		echo $output;
		
	Groups_Help::footer();
} // function groups_admin_capabilities_add

/**
 * Handle add capability form submission.
 * @return int new capability's id or false if unsuccessful
 */
function groups_admin_capabilities_add_submit() {
	
	global $wpdb;
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_GROUPS_NONCE], 'capabilities-add' ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
	
	$capability  = isset( $_POST['capability-field'] ) ? $_POST['capability-field'] : null; 
	$description = isset( $_POST['description-field'] ) ? $_POST['description-field'] : '';
	
	return Groups_Capability::create( compact( "capability", "description" ) );
} // function groups_admin_capabilities_add_submit
