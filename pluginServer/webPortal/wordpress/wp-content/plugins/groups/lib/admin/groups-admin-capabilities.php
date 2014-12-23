<?php
/**
 * groups-admin-capabilities.php
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
define( 'GROUPS_CAPABILITIES_PER_PAGE', 10 );
define( 'GROUPS_ADMIN_CAPABILITIES_NONCE_1', 'groups-cap-nonce-1');
define( 'GROUPS_ADMIN_CAPABILITIES_NONCE_2', 'groups-cap-nonce-2');
define( 'GROUPS_ADMIN_CAPABILITIES_ACTION_NONCE', 'groups-cap-action-nonce');
define( 'GROUPS_ADMIN_CAPABILITIES_FILTER_NONCE', 'groups-cap-filter-nonce' );

require_once( GROUPS_CORE_LIB . '/class-groups-pagination.php' );
require_once( GROUPS_ADMIN_LIB . '/groups-admin-capabilities-add.php');
require_once( GROUPS_ADMIN_LIB . '/groups-admin-capabilities-edit.php');
require_once( GROUPS_ADMIN_LIB . '/groups-admin-capabilities-remove.php');

/**
 * Manage capabilities: table of capabilities and add, edit, remove actions.
 */
function groups_admin_capabilities() {
	
	global $wpdb;
	
	$output = '';
	$today = date( 'Y-m-d', time() );
	
	if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
		wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
	}

	//
	// handle actions
	//
	if ( isset( $_POST['action'] ) ) {
		//  handle action submit - do it
		switch( $_POST['action'] ) {
			case 'add' :
				if ( !groups_admin_capabilities_add_submit() ) {
					return groups_admin_capabilities_add();
				}
				break;
			case 'edit' :
				if ( !groups_admin_capabilities_edit_submit() ) {
					return groups_admin_capabilities_edit( $_POST['capability-id-field'] );
				}
				break;
			case 'remove' :
				groups_admin_capabilities_remove_submit();
				break;
			// bulk actions on groups: capabilities
			case 'groups-action' :
//				 if ( wp_verify_nonce( $_POST[GROUPS_ADMIN_CAPABILITIES_ACTION_NONCE], 'admin' ) ) {
//					 $group_ids = isset( $_POST['group_ids'] ) ? $_POST['group_ids'] : null;
//					 $subaction = isset( $_POST['add'] ) ? $_POST['add'] : ( isset( $_POST['remove'] ) ? $_POST['remove'] : null );
//					 $capability_id = isset( $_POST['capability_id'] ) ? $_POST['capability_id'] : null;					
//					 if ( is_array( $group_ids ) && ( $subaction !== null ) && ( $capability_id !== null ) ) {
//						 foreach ( $group_ids as $group_id ) {
//							 switch ( $subaction ) {
//								 case 'Add' :
//									 Groups_Group_Capability::create( array( 'group_id' => $group_id, 'capability_id' => $capability_id ) );
//									 break;
//								 case 'Remove' :
//									 Groups_Group_Capability::delete( $group_id, $capability_id );
//									 break;
//							 }
//						 }
//					 }
//				 }
				break;
		}
	} else if ( isset ( $_GET['action'] ) ) {
		// handle action request - show form
		switch( $_GET['action'] ) {
			case 'add' :
				return groups_admin_capabilities_add();
				break;
			case 'edit' :
				if ( isset( $_GET['capability_id'] ) ) {
					return groups_admin_capabilities_edit( $_GET['capability_id'] );
				}
				break;
			case 'remove' :
				if ( isset( $_GET['capability_id'] ) ) {
					return groups_admin_capabilities_remove( $_GET['capability_id'] );
				}
				break;
			case 'refresh' :
				if ( check_admin_referer( 'refresh' ) ) {
					$n = Groups_WordPress::refresh_capabilities();
					if ( $n > 0 ) {
						$output .= '<div class="info">' . sprintf( _n( 'One capability has been added.', '%d capabilities have been added.', $n, GROUPS_PLUGIN_DOMAIN ), $n ) . '</div>';
					} else {
						$output .= '<div class="info">' . __( 'No new capabilities have been found.', GROUPS_PLUGIN_DOMAIN ) .  '</div>';
					}
				} else {
					wp_die( __( 'A Duck!', GROUPS_PLUGIN_DOMAIN ) );
				}
				break;
		}
	}
	
	//
	// capabilities table
	//
	if (
		isset( $_POST['clear_filters'] ) ||
		isset( $_POST['capability_id'] ) ||
		isset( $_POST['capability'] )
	) {
		if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_CAPABILITIES_FILTER_NONCE], 'admin' ) ) {
			wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
		}
	}
	
	// filters
	$capability_id = Groups_Options::get_user_option( 'capabilities_capability_id', null );
	$capability	= Groups_Options::get_user_option( 'capabilities_capability', null );
	
	if ( isset( $_POST['clear_filters'] ) ) {
		Groups_Options::delete_user_option( 'capabilities_capability_id' );
		Groups_Options::delete_user_option( 'capabilities_capability' );
		$capability_id = null;
		$capability = null;
	} else if ( isset( $_POST['submitted'] ) ) {
		// filter by name
		if ( !empty( $_POST['capability'] ) ) {
			$capability = $_POST['capability'];
			Groups_Options::update_user_option( 'capabilities_capability', $capability );
		}
		// filter by capability id
		if ( !empty( $_POST['capability_id'] ) ) {
			$capability_id = intval( $_POST['capability_id'] );
			Groups_Options::update_user_option( 'capabilities_capability_id', $capability_id );
		} else if ( isset( $_POST['capability_id'] ) ) { // empty && isset => '' => all
			$capability_id = null;
			Groups_Options::delete_user_option( 'capabilities_capability_id' );	
		}
	}
	
	if ( isset( $_POST['row_count'] ) ) {
		if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_CAPABILITIES_NONCE_1], 'admin' ) ) {
			wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
		}
	}
	
	if ( isset( $_POST['paged'] ) ) {
		if ( !wp_verify_nonce( $_POST[GROUPS_ADMIN_CAPABILITIES_NONCE_2], 'admin' ) ) {
			wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
		}
	}
	
	$current_url = ( is_ssl() ? 'https://' : 'http://' ) . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];
	$current_url = remove_query_arg( 'paged', $current_url );
	$current_url = remove_query_arg( 'action', $current_url );
	$current_url = remove_query_arg( 'capability_id', $current_url );
	
	$capability_table = _groups_get_tablename( 'capability' );
	
	$output .=
		'<div class="manage-capabilities">' .
		'<div>' .
		'<h2>' .
		__( 'Capabilities', GROUPS_PLUGIN_DOMAIN ) .
		'</h2>' .
		'</div>';
	
	$output .=
		'<div class="manage">' .
		"<a title='" . __( 'Click to add a new capability', GROUPS_PLUGIN_DOMAIN ) . "' class='add button' href='" . esc_url( $current_url ) . "&action=add'><img class='icon' alt='" . __( 'Add', GROUPS_PLUGIN_DOMAIN) . "' src='". GROUPS_PLUGIN_URL . "images/add.png'/><span class='label'>" . __( 'New Capability', GROUPS_PLUGIN_DOMAIN) . "</span></a>" .
		"<a title='" . __( 'Click to refresh capabilities', GROUPS_PLUGIN_DOMAIN ) . "' class='refresh button' href='" . esc_url( wp_nonce_url( $current_url, 'refresh' ) ) . "&action=refresh'><img class='icon' alt='" . __( 'Refresh', GROUPS_PLUGIN_DOMAIN) . "' src='". GROUPS_PLUGIN_URL . "images/refresh.png'/><span class='label'>" . __( '', GROUPS_PLUGIN_DOMAIN) . "</span></a>" .
		'</div>';

	$row_count = isset( $_POST['row_count'] ) ? intval( $_POST['row_count'] ) : 0;
	
	if ($row_count <= 0) {
		$row_count = Groups_Options::get_user_option( 'capabilities_per_page', GROUPS_CAPABILITIES_PER_PAGE );
	} else {
		Groups_Options::update_user_option('capabilities_per_page', $row_count );
	}
	$offset = isset( $_GET['offset'] ) ? intval( $_GET['offset'] ) : 0;
	if ( $offset < 0 ) {
		$offset = 0;
	}
	$paged = isset( $_GET['paged'] ) ? intval( $_GET['paged'] ) : 0;
	if ( $paged < 0 ) {
		$paged = 0;
	} 
	
	$orderby = isset( $_GET['orderby'] ) ? $_GET['orderby'] : null;
	switch ( $orderby ) {
		case 'capability_id' :
		case 'capability' :
			break;
		default:
			$orderby = 'name';
	}
	
	$order = isset( $_GET['order'] ) ? $_GET['order'] : null;
	switch ( $order ) {
		case 'asc' :
		case 'ASC' :
			$switch_order = 'DESC';
			break;
		case 'desc' :
		case 'DESC' :
			$switch_order = 'ASC';
			break;
		default:
			$order = 'ASC';
			$switch_order = 'DESC';
	}
	
	$filters = array();
	$filter_params = array();
	if ( $capability_id ) {
		$filters[] = " $capability_table.capability_id = %d ";
		$filter_params[] = $capability_id;
	}
	if ( $capability ) {
		$filters[] = " $capability_table.capability LIKE '%%%s%%' ";
		$filter_params[] = $capability;
	}
		
	if ( !empty( $filters ) ) {
		$filters = " WHERE " . implode( " AND ", $filters );
	} else {
		$filters = '';
	}
	
	$count_query = $wpdb->prepare( "SELECT COUNT(*) FROM $capability_table $filters", $filter_params );
	$count  = $wpdb->get_var( $count_query );
	if ( $count > $row_count ) {
		$paginate = true;
	} else {
		$paginate = false;
	}
	$pages = ceil ( $count / $row_count );
	if ( $paged > $pages ) {
		$paged = $pages;
	}
	if ( $paged != 0 ) {
		$offset = ( $paged - 1 ) * $row_count;
	}
	
	$query = $wpdb->prepare(
		"SELECT * FROM $capability_table
		$filters
		ORDER BY $orderby $order
		LIMIT $row_count OFFSET $offset",
		$filter_params
	);
	
	$results = $wpdb->get_results( $query, OBJECT );

	$column_display_names = array(
		'capability_id' => __( 'Id', GROUPS_PLUGIN_DOMAIN ),
		'capability'	=> __( 'Capability', GROUPS_PLUGIN_DOMAIN ),
		'description'   => __( 'Description', GROUPS_PLUGIN_DOMAIN ),		
		'edit'		  => __( 'Edit', GROUPS_PLUGIN_DOMAIN ),
		'remove'		=> __( 'Remove', GROUPS_PLUGIN_DOMAIN )
	);
	
	$output .= '<div class="capabilities-overview">';
	
	$output .=
		'<div class="filters">' .
			'<label class="description" for="setfilters">' . __( 'Filters', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
			'<form id="setfilters" action="" method="post">' .
				'<p>' .
				'<label class="capability-id-filter" for="capability_id">' . __( 'Capability Id', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
				'<input class="capability-id-filter" name="capability_id" type="text" value="' . esc_attr( $capability_id ) . '"/>' .
				'<label class="capability-filter" for="capability">' . __( 'Capability', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
				'<input class="capability-filter" name="capability" type="text" value="' . $capability . '"/>' .
				'</p>' .
				'<p>' .
				wp_nonce_field( 'admin', GROUPS_ADMIN_CAPABILITIES_FILTER_NONCE, true, false ) .
				'<input class="button" type="submit" value="' . __( 'Apply', GROUPS_PLUGIN_DOMAIN ) . '"/>' .
				'<input class="button" type="submit" name="clear_filters" value="' . __( 'Clear', GROUPS_PLUGIN_DOMAIN ) . '"/>' .
				'<input type="hidden" value="submitted" name="submitted"/>' .
				'</p>' .
			'</form>' .
		'</div>';
	
	$output .= '
		<div class="page-options">
			<form id="setrowcount" action="" method="post">
				<div>
					<label for="row_count">' . __( 'Results per page', GROUPS_PLUGIN_DOMAIN ) . '</label>' .
					'<input name="row_count" type="text" size="2" value="' . esc_attr( $row_count ) .'" />
					' . wp_nonce_field( 'admin', GROUPS_ADMIN_CAPABILITIES_NONCE_1, true, false ) . '
					<input class="button" type="submit" value="' . __( 'Apply', GROUPS_PLUGIN_DOMAIN ) . '"/>
				</div>
			</form>
		</div>
		';
		
	if ( $paginate ) {
	  require_once( GROUPS_CORE_LIB . '/class-groups-pagination.php' );
		$pagination = new Groups_Pagination( $count, null, $row_count );
		$output .= '<form id="posts-filter" method="post" action="">';
		$output .= '<div>';
		$output .= wp_nonce_field( 'admin', GROUPS_ADMIN_CAPABILITIES_NONCE_2, true, false );
		$output .= '</div>';
		$output .= '<div class="tablenav top">';
		$output .= $pagination->pagination( 'top' );
		$output .= '</div>';
		$output .= '</form>';
	}
	
	
//	 $capability_table = _groups_get_tablename( "capability" );
//	 $group_capability_table = _groups_get_tablename( "group_capability" );
	
//	 // capabilities select
//	 $capabilities_select = '<select name="capability_id">';
//	 $capabilities = $wpdb->get_results( $wpdb->prepare( "SELECT * FROM $capability_table ORDER BY capability" ) );
//	 foreach( $capabilities as $capability ) {
//		 $capabilities_select .= '<option value="' . esc_attr( $capability->capability_id ) . '">' . wp_filter_nohtml_kses( $capability->capability ) . '</option>';
//	 }
//	 $capabilities_select .= '</select>';
	
	
//	 $output .= '<form id="groups-action" method="post" action="">';
	
//	 $output .= '<div class="tablenav top">';
//	 $output .= '<div class="alignleft">';
//	 $output .= __( "Apply capability to selected groups:", GROUPS_PLUGIN_DOMAIN );
//	 $output .= $capabilities_select;
//	 $output .= '<input class="button" type="submit" name="add" value="' . __( "Add", GROUPS_PLUGIN_DOMAIN ) . '"/>';
//	 $output .= '<input class="button" type="submit" name="remove" value="' . __( "Remove", GROUPS_PLUGIN_DOMAIN ) . '"/>';
//	 $output .= wp_nonce_field( 'admin', GROUPS_ADMIN_CAPABILITIES_ACTION_NONCE, true, false );
//	 $output .= '<input type="hidden" name="action" value="groups-action"/>';
//	 $output .= '</div>'; // .alignleft
//	 $output .= '</div>'; // .tablenav.top
	
	$output .= '
		<table id="" class="wp-list-table widefat fixed" cellspacing="0">
		<thead>
			<tr>
			';
	
	$output .= '<th id="cb" class="manage-column column-cb check-column" scope="col"><input type="checkbox"></th>';
	
	foreach ( $column_display_names as $key => $column_display_name ) {
		$options = array(
			'orderby' => $key,
			'order' => $switch_order
		);
		$class = $key;
		if ( !in_array($key, array( 'capabilities', 'edit', 'remove' ) ) ) {
			if ( strcmp( $key, $orderby ) == 0 ) {
				$lorder = strtolower( $order );
				$class = "$key manage-column sorted $lorder";
			} else {
				$class = "$key manage-column sortable";
			}
			$column_display_name = '<a href="' . esc_url( add_query_arg( $options, $current_url ) ) . '"><span>' . $column_display_name . '</span><span class="sorting-indicator"></span></a>';
		}
		$output .= "<th scope='col' class='$class'>$column_display_name</th>";
	}
	
	$output .= '</tr>
		</thead>
		<tbody>
		';
		
	if ( count( $results ) > 0 ) {
		for ( $i = 0; $i < count( $results ); $i++ ) {
			
			$result = $results[$i];
			
			$output .= '<tr class="' . ( $i % 2 == 0 ? 'even' : 'odd' ) . '">';
			
			$output .= '<th class="check-column">';
			$output .= '<input type="checkbox" value="' . esc_attr( $result->capability_id ) . '" name="capability_ids[]"/>';
			$output .= '</th>';
			
			$output .= "<td class='capability-id'>";
			$output .= $result->capability_id;
			$output .= "</td>";
			$output .= "<td class='capability'>" . stripslashes( wp_filter_nohtml_kses( $result->capability ) ) . "</td>";
			$output .= "<td class='description'>" . stripslashes( wp_filter_nohtml_kses( $result->description ) ) . "</td>";
						
			$output .= "<td class='edit'>";
			$output .= "<a href='" . esc_url( add_query_arg( 'paged', $paged, $current_url ) ) . "&action=edit&capability_id=" . $result->capability_id . "' alt='" . __( 'Edit', GROUPS_PLUGIN_DOMAIN) . "'><img src='". GROUPS_PLUGIN_URL ."images/edit.png'/></a>";
			$output .= "</td>";
			
			$output .= "<td class='remove'>";
			if ( $result->capability !== Groups_Post_Access::READ_POST_CAPABILITY ) {
				$output .= "<a href='" . esc_url( $current_url ) . "&action=remove&capability_id=" . $result->capability_id . "' alt='" . __( 'Remove', GROUPS_PLUGIN_DOMAIN) . "'><img src='". GROUPS_PLUGIN_URL ."images/remove.png'/></a>";
			}
			$output .= "</td>";
			
			$output .= '</tr>';
		}
	} else {
		$output .= '<tr><td colspan="10">' . __( 'There are no results.', GROUPS_PLUGIN_DOMAIN ) . '</td></tr>';
	}
		
	$output .= '</tbody>';
	$output .= '</table>';
	
//	 $output .= '</form>'; // #groups-action
					
	if ( $paginate ) {
	  require_once( GROUPS_CORE_LIB . '/class-groups-pagination.php' );
		$pagination = new Groups_Pagination($count, null, $row_count);
		$output .= '<div class="tablenav bottom">';
		$output .= $pagination->pagination( 'bottom' );
		$output .= '</div>';			
	}

	$output .= '</div>'; // .capabilities-overview
	$output .= '</div>'; // .manage-capabilities
	
	echo $output;
	Groups_Help::footer();
} // function groups_admin_capabilities()
