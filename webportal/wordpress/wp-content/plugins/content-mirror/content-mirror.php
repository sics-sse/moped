<?php
/*
Plugin Name: Content Mirror
Plugin URI: http://klasehnemark.com
Description: You can display content from one page or post on another by just selecting the original post in the editor. When the original post is updated, so is the mirrored one. This also works with post from other sites in a multisite configuration. 
Author: Klas Ehnemark
Version: 1.2
Author URI: http://klasehnemark.com

Copyright (C) 2011 Klas Ehnemark (http://klasehnemark.com)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License, version 2, as 
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

More information can be found at http://klasehnemark.com/wordpress-plugins
*/


add_action('activated_plugin','content_mirror_save_error');
function content_mirror_save_error(){
    update_option('content_mirror_save_error_plugin_error',  ob_get_contents());
}
update_option('content_mirror_save_error_plugin_error',  '');

if (!class_exists("content_mirror")) { 

	class content_mirror {
		
		private $project_tasks_db_version = '0.13';


		////////////////////////////////////////////////////////////////////////////////
		//
		// INITIALIZE OBJECT
		//
		////////////////////////////////////////////////////////////////////////////////
	
		public function __construct() {
		
			$this->initialize_errors();

			// Initialization stuff
			add_action('init', array(&$this, 'wordpress_init'));
			add_action('admin_init', array(&$this, 'wordpress_admin_init'));

			// add ajax functions
			add_action('wp_ajax_render_content_mirror_admin_form', array ( $this, 'render_content_mirror_admin_form_ajax' ));
			add_action('wp_ajax_render_content_mirror_admin_form_options', array ( $this, 'render_content_mirror_admin_form_options_ajax' ));
			add_action('wp_ajax_create_shortcode_from_form', array ( $this, 'create_shortcode_from_form_ajax' ));			
			
			// add shortcode
			add_shortcode('contentmirror', array( $this, 'handle_shortcode_content_mirror' ));
			add_shortcode('contentmirrorvar', array( $this, 'handle_shortcode_content_mirror_var' ));
			add_shortcode('contentmirrorif', array( $this, 'handle_shortcode_content_mirror_if' ));
				
		}

		
		////////////////////////////////////////////////////////////////////////////////
		//
		// MAIN INIT FUNCTION
		// Runs upon WordPress initialization
		//
		////////////////////////////////////////////////////////////////////////////////
		
		function wordpress_admin_init() {

			// add stylesheet to editor
			add_filter( 'mce_css', array( $this, 'tinyplugin_css' ));
			
			// add editor plugin and editor button
			add_filter( 'mce_external_plugins', array( $this, 'tinyplugin_register' ));
			add_filter( 'mce_buttons_2', array( $this, 'tinyplugin_add_button') , 0);

		}
		
		public function wordpress_init () {
		
		}

		/* Initializes all the error messages */
		function initialize_errors() {
			//$this->errors = new WP_Error();
			//$this->errors->add('new_comment_posted', __('You cannot edit a comment after other comments have been posted.', $this->localizationName));
			
		} 



		////////////////////////////////////////////////////////////////////////////////
		//
		// ADD TINY MCE PLUGIN AND BUTTON
		//
		////////////////////////////////////////////////////////////////////////////////
		
		public function tinyplugin_register ( $plugin_array ) {
			//$plugin_array["content_mirror"] = WP_PLUGIN_URL . "/" . dirname( plugin_basename(__FILE__) ) . "/content-mirror-editor.js";
			$plugin_array["content_mirror"] = WP_PLUGIN_URL . "/content-mirror/content-mirror-editor.js";
			return $plugin_array;		
		}
		
		public function tinyplugin_add_button ( $buttons ) {
   			array_push($buttons, 'separator', 'content_mirror');
			return $buttons;		
		}
		
		public function tinyplugin_css( $wp ) {
			//$wp .= ',' . WP_PLUGIN_URL . "/" . dirname( plugin_basename(__FILE__) ) . "/content-mirror.css" ;
			$wp .= ',' . WP_PLUGIN_URL . "/content-mirror/content-mirror.css" ;
			return $wp;
		}
		
		public function tiny_mce_version ( $version ) {
			return ++$version;	
		}
		
		
		
		////////////////////////////////////////////////////////////////////////////////
		//
		// HANDLE SHORTCODE CONTENT MIRROR
		//
		////////////////////////////////////////////////////////////////////////////////
				
		function handle_shortcode_content_mirror ( $attr, $content = null ) {
	
			global $wpdb; 
			
			$defaults = array (
				'site' 				=> '',
				'post_type'			=> 'post',
				'item'				=> '0'
			);
			
			$r = wp_parse_args( $attr, $defaults );
			
			global $content_is_mirror;
			global $content_mirror_original_blogid;
			global $content_mirror_params;
			
			$content_is_mirror = true;
			$content_mirror_params = $r;
			$switched_blog = false;
			
			if ( $r['site'] != $wpdb->blogid ) {
				
				$content_mirror_original_blogid = $wpdb->blogid;
				switch_to_blog ( $r['site'] );
				$switched_blog = true;
			}

			$post_item = get_post( $r['item'] );
			$post_content = '';

			if ( !empty ( $post_item )) {
				$post_content = $post_item->post_content;
				$post_content = apply_filters('the_content', $post_content);
				$post_content = '<div class="content_mirror content_mirror_site_' . $r['site'] . ' content_mirror_post_type_' . $r['post_type'] . ' content_mirror_item_' . $r['item'] . '">' . str_replace(']]>', ']]&gt;', $post_content) . '</div>';
			}

			if ( $switched_blog !== false ) restore_current_blog();
			
			$post_content = apply_filters ('content_mirror_output', $post_content, $r['site'], $r['post_type'], $r['item'], $post_item );
			
			unset( $content_is_mirror );
			unset( $content_mirror_original_blogid );
			unset( $content_mirror_params );
			
			return $post_content;			
		}



		////////////////////////////////////////////////////////////////////////////////
		//
		// HANDLE SHORTCODE CONTENT MIRROR VAR
		//
		////////////////////////////////////////////////////////////////////////////////
				
		function handle_shortcode_content_mirror_var ( $attr, $content = null ) {
	
			global $wpdb; 
			global $content_mirror_params;
			
			$defaults = array (
				'name' 				=> ''
			);
			
			$r = wp_parse_args( $attr, $defaults );
			
			if ( $r['name'] != '' && isset( $content_mirror_params[$r['name']] )) return $content_mirror_params[$r['name']];
			
			return '';
			
		}
		
		
		
		////////////////////////////////////////////////////////////////////////////////
		//
		// HANDLE SHORTCODE CONTENT MIRROR VAR
		//
		////////////////////////////////////////////////////////////////////////////////
				
		function handle_shortcode_content_mirror_if ( $attr, $content = null ) {
	
			global $wpdb; 
			global $content_mirror_params;
			
			$defaults = array (
				'name' 				=> '',
				'compare'				=> 'equal',
				'value'				=> ''
			);
			
			$r = wp_parse_args( $attr, $defaults );
			
			if ( $r['name'] != '' && isset( $content_mirror_params[$r['name']] ) ) {
			
				switch ( $r['compare'] ) {
				
					case 'equal':
						if ( $content_mirror_params[$r['name']] == $r['value'] ) return $content;
						break;
						
					case 'not':
						if ( $content_mirror_params[$r['name']] != $r['value'] ) return $content;
						break;
						
				}
			}
			
			return '';

		}


		////////////////////////////////////////////////////////////////////////////////
		//
		// RENDER CONTENT MIRROR ADMIN FORM
		// Called from editor by javascript ajax
		// Viewed inside a thickbox
		//
		////////////////////////////////////////////////////////////////////////////////
	
		function render_content_mirror_admin_form () {
			global $wpdb; 
			wp_enqueue_script ( 'jquery' );
			wp_enqueue_script ( 'tiny-mce-popup', site_url() . '/' . WPINC . '/js/tinymce/tiny_mce_popup.js' );
			//wp_enqueue_script ( 'content-mirror', WP_PLUGIN_URL . "/" . dirname( plugin_basename(__FILE__) ) . "/content-mirror-options.js" );
			wp_enqueue_script ( 'content-mirror', WP_PLUGIN_URL . "/content-mirror/content-mirror-options.js" );?>
		
			<html><head>
				<link rel="stylesheet" type="text/css" href="<?php echo '/wp-content/plugins/' . str_replace( '.php', '.css', plugin_basename(__FILE__)) ?>" />
				<link rel="stylesheet" type="text/css" href="<?php echo WP_PLUGIN_URL . "/content-mirror/content-mirror.css" ?>" /><?php
				do_action('wp_head');?>
			</head>
			<body id="content_mirror_edit">
				<h3 class="mirror-title">Select what content you want to mirror</h3>
				
				<div class="selecters">
					<?php
					
					if ( is_multisite() == true ) { ?>
						<div class="row">
							<label for="site">Site:</label>
							<select name="site" id="site"><?php
							
								$blogs = $wpdb->get_results("SELECT blog_id FROM $wpdb->blogs", ARRAY_A);
								foreach ( $blogs as $blog ) {
									$current_blog_details = get_blog_details( array( 'blog_id' => $blog['blog_id'] ) );
									if ( $current_blog_details && $current_blog_details->blogname ) {
										echo '<option value="' . $blog['blog_id'] . '">' . $current_blog_details->blogname . '</option>';
									}
								}
							?>
								
							</select>
							<div class="clear"></div>
						</div><?php
					} ?>
					<div class="row">
						<label for="post-type">Post type:</label>
							<select name="post-type" id="post-type"><?php
						
							global $wp_post_types;
							$post_types=get_post_types( array( 'public' => true ), 'names'); 
  							foreach ($post_types  as $post_type ) {
  								if ( post_type_supports ( $post_type, 'editor' ) ) {
  									echo '<option value="' . $post_type . '">' . ucwords( $post_type ) . '</option>';
  								}
  							}?>
						</select>
						<div class="clear"></div>
					</div>				
					<div class="row">
						<label for="site">Item:</label>
						<select name="item" id="item">
							<option></option>
						</select>
						<div class="clear"></div>
					</div>
				</div>
				<div class="row">
					<label class="simple">Preview Original:</label>
				</div>
				<div id="preview_area" class="row preview">
				</div>
				<div id="form_buttons">
					<button class="button" id="select_button" type="button">Update</button>
					<button class="button" id="cancel_button" type="button">Cancel</button>
				</div>
				
				<script language="javascript">
					ajaxurl = '<?php echo admin_url('admin-ajax.php'); ?>';
					post_id = '<?php echo ( isset($_GET['post_id']) && $_GET['post_id'] != '' ) ? $_GET['post_id'] : '0'?>';
					jQuery(document).ready(function() { tinyMCEPopup.onInit.add(content_mirror_options.init, content_mirror_options); });
				</script>
			</body></html><?php
		}
		

		////////////////////////////////////////////////////////////////////////////////
		//
		// GET CONTENT USED IN THE MIRROR ADMIN FORM
		// Options and so on
		//
		////////////////////////////////////////////////////////////////////////////////
			
		function render_content_mirror_admin_form_options_ajax () {
		
			global $wpdb; 
			
			$content = ( isset($_POST['content']) && $_POST['content'] != '' ) ? $_POST['content'] : 'itemlist';
			$site = ( isset($_POST['site']) && $_POST['site'] != '' ) ? $_POST['site'] : '1';
			$posttype = ( isset($_POST['posttype']) && $_POST['posttype'] != '' ) ? $_POST['posttype'] : '1';
			$item = ( isset($_POST['item']) && $_POST['item'] != '' ) ? $_POST['item'] : '1';
			$post_id = ( isset($_POST['post_id']) && $_POST['post_id'] != '' ) ? $_POST['post_id'] : '0';
			
			switch ( $content ) {
				
				case 'itemlist':
					$current_blog_id = $wpdb->blogid;
					$wpdb->set_blog_id( $site );
					$post_items = get_posts ( array ( 'post_type' => $posttype, 'posts_per_page' => -1, 'orderby' => 'title', 'order' => 'ASC' ));
					foreach ( $post_items as $post_item ) {
						if ( $post_item->ID == $post_id && $current_blog_id == $site ) {						
							$item_list_option = array ( 'value' => '0', 'title' => $post_item->post_title, 'html_class' => 'same_item' );
						} else {
							$item_list_option = array ( 'value' => $post_item->ID, 'title' => $post_item->post_title, 'html_class' => '' );
						}
						$item_list_option = apply_filters ('content_mirror_list_item', $item_list_option, $post_item );
						if ( $item_list_option ) {
							echo '<option value="' . $item_list_option['value'] . '" class="' . $item_list_option['html_class'] . '">' . $item_list_option['title'] . '</option>';
						}
					}
					$wpdb->set_blog_id( $current_blog_id );
					break;
				case 'preview':
					if ( $item == '0' ) {
						echo '<h1 class="preview_error">This is the same page that you\'re editing</h1>';
					} else {
						$current_blog_id = $wpdb->blogid;
						$wpdb->set_blog_id( $site );
						$post_item = get_post( $item );
						if ( $post_item ) {
							echo $post_item->post_content == '' ? '<h1 class="preview_error">Post is empty</h1>' : wpautop ( $post_item->post_content );
						} else {
							echo '<h1 class="preview_error">Cannot find post</h1>';
						} 
						$wpdb->set_blog_id( $current_blog_id );
					}
					break;
			}
			die();
		}
		
		////////////////////////////////////////////////////////////////////////////////
		//
		// CREATE SHORTCODE FROM FORM
		// Called from admin editor by javascript ajax 
		// Creating the shortcode based on the input
		//
		////////////////////////////////////////////////////////////////////////////////

		function create_shortcode_from_form () {
		
			global $wpdb;
			
			
		}
		

		////////////////////////////////////////////////////////////////////////////////
		//
		// INTERNAL FUNCTIONS
		// And small intermediate ones
		//
		////////////////////////////////////////////////////////////////////////////////
		
		// Ajax intermediate functions
		function render_content_mirror_admin_form_ajax () { $this->render_content_mirror_admin_form (); die();}
		function create_shortcode_from_form_ajax () { $this->create_shortcode_from_form(); die(); }

	} 
	
} //End Class


if (class_exists("content_mirror")) { $content_mirror = new content_mirror(); }
echo get_option('content_mirror_save_error_plugin_error');
?>