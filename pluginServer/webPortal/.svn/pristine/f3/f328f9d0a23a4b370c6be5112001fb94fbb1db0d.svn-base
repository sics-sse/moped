<?php
/**
 * class-groups-access-meta-boxes.php
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
 * Adds meta boxes to edit screens.
 * 
 * @link http://codex.wordpress.org/Function_Reference/add_meta_box
 */
class Groups_Access_Meta_Boxes {
	
	const NONCE = 'groups-meta-box-nonce';
	const SET_CAPABILITY = 'set-capability';
	const READ_ACCESS = 'read-access';
	const CAPABILITY = 'capability';
	
	/**
	 * Hooks for capabilities meta box and saving options.
	 */
	public static function init() {
		add_action( 'add_meta_boxes', array( __CLASS__, "add_meta_boxes" ), 10, 2 );
		add_action( 'save_post', array( __CLASS__, "save_post" ) );

		add_action( 'attachment_fields_to_edit', array( __CLASS__, 'attachment_fields_to_edit' ), 10, 2 );
		add_action( 'attachment_fields_to_save', array( __CLASS__, 'attachment_fields_to_save' ), 10, 2 );
	}

	/**
	 * Triggered by init() to add capability meta box.
	 */
	public static function add_meta_boxes( $post_type, $post = null ) {
		global $wp_version;
		$post_type_object = get_post_type_object( $post_type );
		if ( $post_type_object && $post_type != 'attachment' ) {
			$post_types_option = Groups_Options::get_option( Groups_Post_Access::POST_TYPES, array() );
			if ( !isset( $post_types_option[$post_type]['add_meta_box'] ) || $post_types_option[$post_type]['add_meta_box'] ) {
				if ( $wp_version < 3.3 ) {
					$post_types = get_post_types();
					foreach ( $post_types as $post_type ) {
						add_meta_box(
							"groups-access",
							__( "Access restrictions", GROUPS_PLUGIN_DOMAIN ),
							array( __CLASS__, "capability" ),
							$post_type,
							"side",
							"high"
						);
					}
				} else {
					add_meta_box(
						"groups-access",
						__( "Access restrictions", GROUPS_PLUGIN_DOMAIN ),
						array( __CLASS__, "capability" ),
						null,
						"side",
						"high"
					);
				}
			}
		}
	}
	
	/**
	 * Render meta box for capabilities.
	 * 
	 * @see do_meta_boxes()
	 * 
	 * @param Object $object
	 * @param Object $box
	 */
	public static function capability( $object = null, $box = null ) {

		$output = "";

		$post_id = isset( $object->ID ) ? $object->ID : null;
		$post_type = isset( $object->post_type ) ? $object->post_type : null;
		$post_singular_name = __( "Post", GROUPS_PLUGIN_DOMAIN );
		if ( $post_type !== null ) {
			$post_type_object = get_post_type_object( $post_type );
			$labels = isset( $post_type_object->labels ) ? $post_type_object->labels : null;
			if ( $labels !== null ) {
				if ( isset( $labels->singular_name ) )  {
					$post_singular_name = __( $labels->singular_name );
				}
			}
		}

		if ( self::user_can_restrict() ) {
			$user = new Groups_User( get_current_user_id() );
			$output .= __( "Enforce read access", GROUPS_PLUGIN_DOMAIN );
			$read_caps = get_post_meta( $post_id, Groups_Post_Access::POSTMETA_PREFIX . Groups_Post_Access::READ_POST_CAPABILITY );
			$valid_read_caps = Groups_Options::get_option( Groups_Post_Access::READ_POST_CAPABILITIES, array( Groups_Post_Access::READ_POST_CAPABILITY ) );
			$output .= '<div style="padding:0 1em;margin:1em 0;border:1px solid #ccc;border-radius:4px;">';
			$output .= '<ul>';
			foreach( $valid_read_caps as $valid_read_cap ) {
				if ( $capability = Groups_Capability::read_by_capability( $valid_read_cap ) ) {
					if ( $user->can( $capability->capability ) ) {
						$checked = in_array( $capability->capability, $read_caps ) ? ' checked="checked" ' : '';
						$output .= '<li>';
						$output .= '<label>';
						$output .= '<input name="' . self::CAPABILITY . '[]" ' . $checked . ' type="checkbox" value="' . esc_attr( $capability->capability_id ) . '" />';
						$output .= wp_filter_nohtml_kses( $capability->capability );
						$output .= '</label>';
						$output .= '</li>';
					}
				}
			}
			$output .= '</ul>';
			$output .= '</div>';
	
			$output .= '<p class="description">';
			$output .= sprintf( __( "Only groups or users that have one of the selected capabilities are allowed to read this %s.", GROUPS_PLUGIN_DOMAIN ), $post_singular_name );
			$output .= '</p>';
			$output .= wp_nonce_field( self::SET_CAPABILITY, self::NONCE, true, false );
		} else {
			$output .= '<p class="description">';
			$output .= sprintf( __( 'You cannot set any access restrictions.', GROUPS_PLUGIN_DOMAIN ), $post_singular_name );
			$style = 'cursor:help;vertical-align:middle;';
			if ( current_user_can( GROUPS_ADMINISTER_OPTIONS ) ) {
				$style = 'cursor:pointer;vertical-align:middle;';
				$output .= sprintf( '<a href="%s">', esc_url( admin_url( 'admin.php?page=groups-admin-options' ) ) );
			}
			$output .= sprintf( '<img style="%s" alt="?" title="%s" src="%s" />', $style, esc_attr( __( 'You must be in a group that has at least one capability enabled to enforce read access.', GROUPS_PLUGIN_DOMAIN ) ), esc_attr( GROUPS_PLUGIN_URL . 'images/help.png' ) );
			if ( current_user_can( GROUPS_ADMINISTER_OPTIONS ) ) {
				$output .= '</a>';
			}
			$output .= '</p>';
		}

		echo $output;
	}

	/**
	 * Save capability options.
	 * 
	 * @param int $post_id
	 * @param mixed $post post data
	 */
	public static function save_post( $post_id = null, $post = null ) {
		if ( ( defined( "DOING_AUTOSAVE" ) && DOING_AUTOSAVE ) ) {
		} else {
			$post_type = get_post_type( $post_id );
			$post_type_object = get_post_type_object( $post_type );
			if ( $post_type_object && $post_type != 'attachment' ) {
				$post_types_option = Groups_Options::get_option( Groups_Post_Access::POST_TYPES, array() );
				if ( !isset( $post_types_option[$post_type]['add_meta_box'] ) || $post_types_option[$post_type]['add_meta_box'] ) {
					if ( isset( $_POST[self::NONCE] ) && wp_verify_nonce( $_POST[self::NONCE], self::SET_CAPABILITY ) ) {
						$post_type = isset( $_POST["post_type"] ) ? $_POST["post_type"] : null;
						if ( $post_type !== null ) {
							// See http://codex.wordpress.org/Function_Reference/current_user_can 20130119 WP 3.5
							// "... Some capability checks (like 'edit_post' or 'delete_page') require this [the post ID] be provided."
							// If the post ID is not provided, it will throw:
							// PHP Notice:  Undefined offset: 0 in /var/www/groups-forums/wp-includes/capabilities.php on line 1067 
							if ( current_user_can( 'edit_'.$post_type, $post_id ) ) {
								if ( self::user_can_restrict() ) {
									$valid_read_caps = self::get_valid_read_caps_for_user();
									foreach( $valid_read_caps as $valid_read_cap ) {
										if ( $capability = Groups_Capability::read_by_capability( $valid_read_cap ) ) {
											if ( !empty( $_POST[self::CAPABILITY] ) && is_array( $_POST[self::CAPABILITY] ) && in_array( $capability->capability_id, $_POST[self::CAPABILITY] ) ) {
												Groups_Post_Access::create( array(
													'post_id' => $post_id,
													'capability' => $capability->capability
												) );
											} else {
												Groups_Post_Access::delete( $post_id, $capability->capability );
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Render capabilities box for attachment post type (Media).
	 * @param array $form_fields
	 * @param object $post
	 * @return array
	 */
	public static function attachment_fields_to_edit( $form_fields, $post ) {
		$post_types_option = Groups_Options::get_option( Groups_Post_Access::POST_TYPES, array() );
		if ( !isset( $post_types_option['attachment']['add_meta_box'] ) || $post_types_option['attachment']['add_meta_box'] ) {
			if ( self::user_can_restrict() ) {
				$user = new Groups_User( get_current_user_id() );
				$output = "";
				$post_singular_name = __( 'Media', GROUPS_PLUGIN_DOMAIN );

				$output .= __( "Enforce read access", GROUPS_PLUGIN_DOMAIN );
				$read_caps = get_post_meta( $post->ID, Groups_Post_Access::POSTMETA_PREFIX . Groups_Post_Access::READ_POST_CAPABILITY );
				$valid_read_caps = self::get_valid_read_caps_for_user();
				$output .= '<div style="padding:0 1em;margin:1em 0;border:1px solid #ccc;border-radius:4px;">';
				$output .= '<ul>';
				foreach( $valid_read_caps as $valid_read_cap ) {
					if ( $capability = Groups_Capability::read_by_capability( $valid_read_cap ) ) {
						$checked = in_array( $capability->capability, $read_caps ) ? ' checked="checked" ' : '';
						$output .= '<li>';
						$output .= '<label>';
						$output .= '<input name="attachments[' . $post->ID . '][' . self::CAPABILITY . '][]" ' . $checked . ' type="checkbox" value="' . esc_attr( $capability->capability_id ) . '" />';
						$output .= wp_filter_nohtml_kses( $capability->capability );
						$output .= '</label>';
						$output .= '</li>';
					}
				}
				$output .= '</ul>';
				$output .= '</div>';

				$output .= '<p class="description">';
				$output .= sprintf( __( "Only groups or users that have one of the selected capabilities are allowed to read this %s.", GROUPS_PLUGIN_DOMAIN ), $post_singular_name );
				$output .= '</p>';

				$form_fields['groups_access'] = array(
					'label' => __( 'Access restrictions', GROUPS_PLUGIN_DOMAIN ),
					'input' => 'html',
					'html' => $output
				);
			}
		}
		return $form_fields;
	}

	/**
	 * Save capabilities for attachment post type (Media).
	 * When multiple attachments are saved, this is called once for each.
	 * @param array $post post data
	 * @param array $attachment attachment field data
	 * @return array
	 */
	public static function attachment_fields_to_save( $post, $attachment ) {
		$post_types_option = Groups_Options::get_option( Groups_Post_Access::POST_TYPES, array() );
		if ( !isset( $post_types_option['attachment']['add_meta_box'] ) || $post_types_option['attachment']['add_meta_box'] ) {
			// if we're here, we assume the user is allowed to edit attachments,
			// but we still need to check if the user can restrict access
			if ( self::user_can_restrict() ) {
				$post_id = null;
				if ( isset( $post['ID'] ) ) {
					$post_id = $post['ID'];
				} else if ( isset( $post['post_ID'] ) ) {
					$post_id = $post['post_ID'];
				}
				if ( $post_id !== null ) {
					$valid_read_caps = self::get_valid_read_caps_for_user();
					foreach( $valid_read_caps as $valid_read_cap ) {
						if ( $capability = Groups_Capability::read_by_capability( $valid_read_cap ) ) {
							if ( !empty( $attachment[self::CAPABILITY] ) && is_array( $attachment[self::CAPABILITY] ) && in_array( $capability->capability_id, $attachment[self::CAPABILITY] ) ) {
								Groups_Post_Access::create( array(
									'post_id' => $post_id,
									'capability' => $capability->capability
								) );
							} else {
								Groups_Post_Access::delete( $post_id, $capability->capability );
							}
						}
					}
				}
			}
		}
		return $post;
	}

	/**
	 * Returns true if the current user has at least one of the capabilities
	 * that can be used to restrict access to posts.
	 * @return boolean
	 */
	private static function user_can_restrict() {
		$has_read_cap = false;
		$user = new Groups_User( get_current_user_id() );
		$valid_read_caps = Groups_Options::get_option( Groups_Post_Access::READ_POST_CAPABILITIES, array( Groups_Post_Access::READ_POST_CAPABILITY ) );
		foreach( $valid_read_caps as $valid_read_cap ) {
			if ( $capability = Groups_Capability::read_by_capability( $valid_read_cap ) ) {
				if ( $user->can( $capability->capability_id ) ) {
					$has_read_cap = true;
					break;
				}
			}
		}
		return $has_read_cap;
	}

	/**
	 * @return array of valid read capabilities for the current or given user
	 */
	private static function get_valid_read_caps_for_user( $user_id = null ) {
		$result = array();
		$user = new Groups_User( $user_id === null ? get_current_user_id() : $user_id );
		$valid_read_caps = Groups_Options::get_option( Groups_Post_Access::READ_POST_CAPABILITIES, array( Groups_Post_Access::READ_POST_CAPABILITY ) );
		foreach( $valid_read_caps as $valid_read_cap ) {
			if ( $capability = Groups_Capability::read_by_capability( $valid_read_cap ) ) {
				if ( $user->can( $capability->capability ) ) {
					$result[] = $valid_read_cap;
				}
			}
		}
		return $result;
	}
} 
Groups_Access_Meta_Boxes::init();
