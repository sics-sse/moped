<?php
/**
 * groups-admin-tree-view.php
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
 * Tree view : a simple tree view
 */
function groups_admin_tree_view() {
	
	global $wpdb;
	
	$output = '';
	$today = date( 'Y-m-d', time() );
	
	if ( !current_user_can( GROUPS_ACCESS_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}
		
	$output .=
		'<div class="groups-tree-view">' .
		'<div>' .
		'<h2>' .
		__( 'Tree of Groups', GROUPS_PLUGIN_DOMAIN ) .
		'</h2>' .
		'</div>';

	$tree = Groups_Utility::get_group_tree();
	$tree_output = '';
	Groups_Utility::render_group_tree( $tree, $tree_output );
	$output .= $tree_output;
	
	$output .= '</div>'; // .groups-overview
	$output .= '</div>'; // .manage-groups
	
	echo $output;
	Groups_Help::footer();
} // function groups_admin_tree_view()
?>