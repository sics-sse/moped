<?php
/**
 * class-groups-post-access.php
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
 * Post access restrictions.
 * 
 * @todo when wp_count_posts() provides reasonable filters, use them so that
 * the post counts displayed on top are in line with the actual posts that
 * are displayed in the table; same for wp_count_attachments()
 * @see http://core.trac.wordpress.org/ticket/16603
 * 
 */
class Groups_Post_Access {
	
	const POSTMETA_PREFIX = 'groups-';
	
	const READ_POST_CAPABILITY = "groups_read_post";
	const READ_POST_CAPABILITY_NAME = "Read Post";
	const READ_POST_CAPABILITIES = 'read_post_capabilities';
	const POST_TYPES = 'post_types';
	
	/**
	 * Create needed capabilities on plugin activation.
	 * Must be called explicitly or hooked into activation.
	 */
	public static function activate() {
		if ( !Groups_Capability::read_by_capability( self::READ_POST_CAPABILITY ) ) {
			Groups_Capability::create( array( "capability" => self::READ_POST_CAPABILITY ) );
			// default read caps
			Groups_Options::update_option( Groups_Post_Access::READ_POST_CAPABILITIES, array( Groups_Post_Access::READ_POST_CAPABILITY ) );
			// for translation
			// @see self::READ_POST_CAPABILITY_NAME
			__( "Read Post", GROUPS_PLUGIN_DOMAIN );
		}
	}

	/**
	 * Sets up filters to restrict access.
	 */
	public static function init() {
		// post access
		add_filter( 'posts_where', array( __CLASS__, 'posts_where' ), 10, 2 );
		add_filter( 'get_pages', array( __CLASS__, "get_pages" ), 1 );
		add_filter( 'the_posts', array( __CLASS__, "the_posts" ), 1, 2 );
		add_filter( 'wp_get_nav_menu_items', array( __CLASS__, "wp_get_nav_menu_items" ), 1, 3 );
		// content access
		add_filter( "get_the_excerpt", array( __CLASS__, "get_the_excerpt" ), 1 );
		add_filter( "the_content", array( __CLASS__, "the_content" ), 1 );
		// edit & delete post
		add_filter( 'map_meta_cap', array( __CLASS__, 'map_meta_cap' ), 10, 4 );
		// @todo these could be interesting to add later ...
		// add_filter( "plugin_row_meta", array( __CLASS__, "plugin_row_meta" ), 1 );
		// add_filter( "posts_join_paged", array( __CLASS__, "posts_join_paged" ), 1 );
		// add_filter( "posts_where_paged", array( __CLASS__, "posts_where_paged" ), 1 );
	}

	/**
	 * Restrict access to edit or delete posts based on the post's access restrictions.
	 * @param array $caps
	 * @param string $cap
	 * @param int $user_id
	 * @param array $args
	 * @return array
	 */
	public static function map_meta_cap( $caps, $cap, $user_id, $args ) {
		if ( isset( $args[0] ) ) {
			if ( strpos( $cap, 'edit_' ) === 0 || strpos( $cap, 'delete_' ) === 0 ) {
				if ( $post_type = get_post_type( $args[0] ) ) {
					if ( $cap === "edit_$post_type" || $cap === "delete_$post_type" ) {
						$post_id = $args[0];
						if ( !self::user_can_read_post( $post_id, $user_id ) ) {
							$caps[] = 'do_not_allow';
						}
					}
				}
			}
		}
		return $caps;
	}

	/**
	 * Filters out posts that the user should not be able to access.
	 * 
	 * @param string $where current where conditions
	 * @param WP_Query $query current query
	 * @return string modified $where
	 */
	public static function posts_where( $where, &$query ) {

		global $wpdb;

		$user_id = get_current_user_id();

		// this only applies to logged in users
		if ( $user_id ) {
			// if administrators can override access, don't filter
			if ( get_option( GROUPS_ADMINISTRATOR_ACCESS_OVERRIDE, GROUPS_ADMINISTRATOR_ACCESS_OVERRIDE_DEFAULT ) ) {
				if ( user_can( $user_id, 'administrator' ) ) {
					return $where;
				}
			}
		}

		// 1. Get all the capabilities that the user has, including those that are inherited:
		$caps = array();
		if ( $user = new Groups_User( $user_id ) ) {
			$capabilities = $user->capabilities_deep;
			if ( is_array( $capabilities ) ) {
				foreach ( $capabilities as $capability ) {
					$caps[] = "'". $capability . "'";
				}
			}
		}

		if ( count( $caps ) > 0 ) {
			$caps = implode( ',', $caps );
		} else {
			$caps = '\'\'';
		}

		// 2. Filter the posts that require a capability that the user doesn't
		// have, or in other words: exclude posts that the user must NOT access:
		
		// The following is not correct in that it requires the user to have ALL capabilities:
// 		$where .= sprintf(
// 			" AND {$wpdb->posts}.ID NOT IN (SELECT DISTINCT ID FROM $wpdb->posts LEFT JOIN $wpdb->postmeta on {$wpdb->posts}.ID = {$wpdb->postmeta}.post_id WHERE {$wpdb->postmeta}.meta_key = '%s' AND {$wpdb->postmeta}.meta_value NOT IN (%s) ) ",
// 			self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY,
// 			$caps
// 		);

		// This allows the user to access posts where the posts are not restricted or where
		// the user has ANY of the capabilities:
		$where .= sprintf(
			" AND {$wpdb->posts}.ID IN " .
			" ( " .
			"   SELECT ID FROM $wpdb->posts WHERE ID NOT IN ( SELECT post_id FROM $wpdb->postmeta WHERE {$wpdb->postmeta}.meta_key = '%s' ) " . // posts without access restriction
			"   UNION ALL " . // we don't care about duplicates here, just make it quick
			"   SELECT post_id AS ID FROM $wpdb->postmeta WHERE {$wpdb->postmeta}.meta_key = '%s' AND {$wpdb->postmeta}.meta_value IN (%s) " . // posts that require any capability the user has
			" ) ",
			self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY,
			self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY,
			$caps
		);

		return $where;
	}

	/**
	 * Filter pages by access capability.
	 * 
	 * @param array $pages
	 */
	public static function get_pages( $pages ) {
		$result = array();
		$user_id = get_current_user_id();
		foreach ( $pages as $page ) {
			if ( self::user_can_read_post( $page->ID, $user_id ) ) {
				$result[] = $page;
			}
		}
		return $result;
	}
	
	/**
	 * Filter posts by access capability.
	 * 
	 * @param array $posts list of posts
	 * @param WP_Query $query
	 */
	public static function the_posts( $posts, &$query ) {
		$result = array();
		$user_id = get_current_user_id();
		foreach ( $posts as $post ) {
			if ( self::user_can_read_post( $post->ID, $user_id ) ) {
				$result[] = $post;
			}
		}
		return $result;
	}
	
	/**
	 * Filter menu items by access capability.
	 * 
	 * @todo admin section: this won't inhibit the items being offered to be added, although when they're added they won't show up in the menu
	 * 
	 * @param array $items
	 * @param mixed $menu
	 * @param array $args
	 */
	public static function wp_get_nav_menu_items( $items = null, $menu = null, $args = null ) {
		$result = array();
		$user_id = get_current_user_id();
		foreach ( $items as $item ) {
			// @todo might want to check $item->object and $item->type first,
			// for example these are 'page' and 'post_type' for a page
			if ( self::user_can_read_post( $item->object_id, $user_id ) ) {
				$result[] = $item;
			}
		}
		return $result;
	}
	
	/**
	 * Filter excerpt by access capability.
	 * 
	 * @param string $output
	 * @return $output if access granted, otherwise ''
	 */
	public static function get_the_excerpt( $output ) {
		global $post;
		$result = '';
		if ( isset( $post->ID ) ) {
			if ( self::user_can_read_post( $post->ID ) ) {
				$result = $output;
			}
		}
		return $result;
	}
	
	/**
	 * Filter content by access capability.
	 *
	 * @param string $output
	 * @return $output if access granted, otherwise ''
	 */
	public static function the_content( $output ) {
		global $post;
		$result = '';
		if ( isset( $post->ID ) ) {
			if ( self::user_can_read_post( $post->ID ) ) {
				$result = $output;
			}
		}
		return $result;
	}
	
	/**
	 * Adds an access capability requirement.
	 * 
	 * $map must contain 'post_id'
	 * 
	 * For now this only should be used to add the READ_POST_CAPABILITY which
	 * it does automatically. Nothing else is checked for granting access.
	 * 
	 * @param array $map
	 * @return true if the capability could be added to the post, otherwis false
	 */
	public static function create( $map ) {
		extract( $map );
		$result = false;

		if ( !isset( $capability ) ) {
			$capability = self::READ_POST_CAPABILITY;
		}

		if ( !empty( $post_id ) && !empty( $capability) ) {
			if ( Groups_Capability::read_by_capability( $capability ) ) {
				if ( !in_array( $capability, get_post_meta( $post_id, self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY ) ) ) {
					$result = add_post_meta( $post_id, self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY, $capability );
				}
			}
		}
		return $result;
	}
	
	/**
	 * Returns true if the post requires the given capability to grant access.
	 * 
	 * Currently only READ_POST_CAPABILITY should be used, this is also taken
	 * as the default.
	 * 
	 * @param int $post_id
	 * @param string $capability capability label
	 * @return true if the capability is required, otherwise false
	 */
	public static function read( $post_id, $capability = self::READ_POST_CAPABILITY ) {
		$result = false;
		$caps = get_post_meta( $post_id, self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY );
		if ( $caps ) {
			$result = in_array( $capability, $caps );
		}
		return $result;
	}
	
	/**
	 * Currently does nothing, always returns false.
	 * 
	 * @param array $map
	 * @return false
	 */
	public static function update( $map ) {
		return false;
	}
	
	/**
	 * Removes a capability requirement from a post.
	 * 
	 * @param int $post_id
	 * @param string $capability defaults to groups_read_post, removes all if null is given
	 * @return true on success, otherwise false
	 */
	public static function delete( $post_id, $capability = self::READ_POST_CAPABILITY ) {
		$result = false;
		if ( !empty( $post_id ) ) {
			if ( !empty( $capability ) ) {
				$result = delete_post_meta( $post_id, self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY, $capability );
			} else {
				$result = delete_post_meta( $post_id, self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY );
			}
		}
		return $result;
	}
	
	/**
	 * Returns a list of capabilities that grant access to the post.
	 * 
	 * @param int $post_id
	 * @return array of string, capabilities
	 */
	public static function get_read_post_capabilities( $post_id ) {
		return get_post_meta( $post_id, self::POSTMETA_PREFIX . self::READ_POST_CAPABILITY );
	}
	
	/**
	 * Returns true if the user has any of the capabilities that grant access to the post.
	 * 
	 * @param int $post_id post id
	 * @param int $user_id user id or null for current user 
	 * @return boolean true if user can read the post
	 */
	public static function user_can_read_post( $post_id, $user_id = null ) {
		$result = false;
		if ( !empty( $post_id ) ) {
			if ( $user_id === null ) {
				$user_id = get_current_user_id();
			}
			$groups_user = new Groups_User( $user_id );
			$read_caps = self::get_read_post_capabilities( $post_id );
			if ( !empty( $read_caps ) ) {
				foreach( $read_caps as $read_cap ) {
					if ( $groups_user->can( $read_cap ) ) {
						$result = true;
						break;
					}
				}
			} else {
				$result = true;
			}
		}
		return $result;
	}

}
Groups_Post_Access::init();
