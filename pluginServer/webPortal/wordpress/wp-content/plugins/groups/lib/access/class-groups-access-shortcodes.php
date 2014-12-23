<?php
/**
 * class-groups-access-shortcodes.php
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
 * Shortcode handlers.
 */
class Groups_Access_Shortcodes {
	
	/**
	 * Defines access shortcodes.
	 */
	public static function init() {
		
		// group restrictions
		add_shortcode( 'groups_member', array( __CLASS__, 'groups_member' ) );
		add_shortcode( 'groups_non_member', array( __CLASS__, 'groups_non_member' ) );
		
		// capabilities
		add_shortcode( 'groups_can', array( __CLASS__, 'groups_can' ) );
		add_shortcode( 'groups_can_not', array( __CLASS__, 'groups_can_not' ) );
	}
	
	/**
	 * Takes one attribute "group" which is a comma-separated list of group
	 * names or ids (can be mixed).
	 * The content is shown if the current user belongs to the group(s).
	 * 
	 * @param array $atts attributes
	 * @param string $content content to render
	 */
	public static function groups_member( $atts, $content = null ) {
		$output = "";
		$options = shortcode_atts( array( "group" => "" ), $atts );
		$show_content = false;
		if ( $content !== null ) {
			$groups_user = new Groups_User( get_current_user_id() );
			$groups = explode( ",", $options['group'] );
			foreach ( $groups as $group ) {
				$group = trim( $group );
				$current_group = Groups_Group::read( $group );
				if ( !$current_group ) {
					$current_group = Groups_Group::read_by_name( $group );
				}
				if ( $current_group ) {
					if ( Groups_User_Group::read( $groups_user->user->ID , $current_group->group_id ) ) {
						$show_content = true;
						break;
					}
				}
			}
			if ( $show_content ) {
				remove_shortcode( 'groups_member' );
				$content = do_shortcode( $content );
				add_shortcode( 'groups_member', array( __CLASS__, 'groups_member' ) );
				$output = $content;
			}
		}
		return $output;
	}
	
	/**
	 * Takes one attribute "group" which is a comma-separated list of group
	 * names or ids (can be mixed).
	 * The content is shown if the current user does NOT belong to the group(s).
	 *
	 * @param array $atts attributes
	 * @param string $content content to render
	 */
	public static function groups_non_member( $atts, $content = null ) {
		$output = "";
		$options = shortcode_atts( array( "group" => "" ), $atts );
		$show_content = true;
		if ( $content !== null ) {
			$groups_user = new Groups_User( get_current_user_id() );
			$groups = explode( ",", $options['group'] );
			foreach ( $groups as $group ) {
				$group = trim( $group );
				$current_group = Groups_Group::read( $group );
				if ( !$current_group ) {
					$current_group = Groups_Group::read_by_name( $group );
				}
				if ( $current_group ) {
					if ( Groups_User_Group::read( $groups_user->user->ID , $current_group->group_id ) ) {
						$show_content = false;
						break;
					}
				}
			}
			if ( $show_content ) {
				remove_shortcode( 'groups_non_member' );
				$content = do_shortcode( $content );
				add_shortcode( 'groups_non_member', array( __CLASS__, 'groups_non_member' ) );
				$output = $content;
			}
		}
		return $output;
	}
	
	/**
	 * Takes one attribute "capability" that must be a valid capability label.
	 * The content is shown if the current user has the capability.
	 * 
	 * @param array $atts attributes
	 * @param string $content content to render
	 */
	public static function groups_can( $atts, $content = null ) {
		$output = "";
		$options = shortcode_atts( array( "capability" => "" ), $atts );
		if ( $content !== null ) {
			$groups_user = new Groups_User( get_current_user_id() );
			$capability = $options['capability'];
			if ( $groups_user->can( $capability ) ) {
				remove_shortcode( 'groups_can' );
				$content = do_shortcode( $content );
				add_shortcode( 'groups_can', array( __CLASS__, 'groups_can' ) );
				$output = $content;
			}
		}
		return $output;
	}
	
	/**
	 * Takes one attribute "capability" that must be a valid capability label.
	 * The content is shown if the current user does NOT have the capability.
	 *
	 * @param array $atts attributes
	 * @param string $content content to render
	 */
	public static function groups_can_not( $atts, $content = null ) {
		$output = "";
		$options = shortcode_atts( array( "capability" => "" ), $atts );
		if ( $content !== null ) {
			$groups_user = new Groups_User( get_current_user_id() );
			$capability = $options['capability'];
			if ( !$groups_user->can( $capability ) ) {
				remove_shortcode( 'groups_can_not' );
				$content = do_shortcode( $content );
				add_shortcode( 'groups_can_not', array( __CLASS__, 'groups_can_not' ) );
				$output = $content;
			}
		}
		return $output;
	}
}
Groups_Access_Shortcodes::init();
