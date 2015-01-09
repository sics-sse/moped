<?php
class add_from_server {

	var $version = '3.2.0.1';
	var $basename = '';
	var $folder = '';
	
	var $meets_guidelines = array(); // Internal use only.
	
	function __construct($plugin) {
		$this->basename = $plugin;
		$this->folder = dirname($plugin);
		//Register general hooks.
		add_action('init', array(&$this, 'load_translations')); // must run before admin_menu
		add_action('admin_init', array(&$this, 'admin_init'));
		add_action('admin_menu', array(&$this, 'admin_menu'));
	}
	
	function requires_32() {
		echo '<div class="error"><p>' . __('<strong>Add From Server:</strong> Sorry, This plugin requires WordPress 3.2+. Please upgrade your WordPress installation or deactivate this plugin.', 'add-from-server') . '</p></div>';
	}
	
	function load_translations() {
		//Load any translation files needed:
		load_plugin_textdomain('add-from-server', '', $this->folder . '/langs/');
	}
	
	function admin_init() {

		//Register our JS & CSS
		wp_register_style ('add-from-server', plugins_url( '/add-from-server.css', __FILE__ ), array(), $this->version);

		if ( ! function_exists('submit_button') ) {
			add_action('admin_notices', array(&$this, 'requires_32') );
			return;
		}

		//Enqueue JS & CSS
		add_action('load-media_page_add-from-server', array(&$this, 'add_styles') );
		add_action('media_upload_server', array(&$this, 'add_styles') );

		add_filter('plugin_action_links_' . $this->basename, array(&$this, 'add_configure_link'));

		if ( $this->user_allowed() ) {
			//Add actions/filters
			add_filter('media_upload_tabs', array(&$this, 'tabs'));
			add_action('media_upload_server', array(&$this, 'tab_handler'));
		}
		
		//Register our settings:
		register_setting('add_from_server', 'frmsvr_root', array(&$this, 'sanitize_option_root') );
		//register_setting('add-from-server', 'frmsvr_last_folder');
		register_setting('add_from_server', 'frmsvr_uac');
		register_setting('add_from_server', 'frmsvr_uac_users');
		register_setting('add_from_server', 'frmsvr_uac_role');
		
	}
	
	function admin_menu() {
		if ( ! function_exists('submit_button') )
			return;
		if ( $this->user_allowed() )
			add_media_page( __('Add From Server', 'add-from-server'), __('Add From Server', 'add-from-server'), 'read', 'add-from-server', array(&$this, 'menu_page') );
		add_options_page( __('Add From Server Settings', 'add-from-server'), __('Add From Server', 'add-from-server'), 'manage_options', 'add-from-server-settings', array(&$this, 'options_page') );
	}

	function add_configure_link($_links) {
		$links = array();
		if ( $this->user_allowed() )
			$links[] = '<a href="' . admin_url('upload.php?page=add-from-server') . '">' . __('Import Files', 'add-from-server') . '</a>';
		if ( current_user_can('manage_options') )
			$links[] = '<a href="' . admin_url('options-general.php?page=add-from-server-settings') . '">' . __('Options', 'add-from-server') . '</a>';

		return array_merge($links, $_links);
	}

	//Add a tab to the media uploader:
	function tabs($tabs) {
		if ( $this->user_allowed() )
			$tabs['server'] = __('Add From Server', 'add-from-server');
		return $tabs;
	}
	
	function add_styles() {
		//Enqueue support files.
		if ( 'media_upload_server' == current_filter() )
			wp_enqueue_style('media');
		wp_enqueue_style('add-from-server');
	}

	//Handle the actual page:
	function tab_handler(){
		if ( ! $this->user_allowed() )
			return;

		//Set the body ID
		$GLOBALS['body_id'] = 'media-upload';

		//Do an IFrame header
		iframe_header( __('Add From Server', 'add-from-server') );

		//Add the Media buttons	
		media_upload_header();

		//Handle any imports:
		$this->handle_imports();

		//Do the content
		$this->main_content();

		//Do a footer
		iframe_footer();
	}
	
	function menu_page() {
		if ( ! $this->user_allowed() )
			return;

		//Handle any imports:
		$this->handle_imports();

		echo '<div class="wrap">';
		screen_icon('upload');
		echo '<h2>' . __('Add From Server', 'add-from-server') . '</h2>';

		//Do the content
		$this->main_content();
		
		echo '</div>';
	}

	function options_page() {
		if ( ! current_user_can('manage_options') )
			return;

		include 'class.add-from-server-settings.php';
		$this->settings = new add_from_server_settings($this);
		$this->settings->render();
	}

	function get_root( $context = 'use' ) {
		static $static_root = null;
		if ( $static_root )
			return $static_root;

		$root = get_option('frmsvr_root', false);
		if ( strpos($root, '%') !== false && 'raw' != $context ) {
			$user = wp_get_current_user();

			$root = str_replace('%username%', $user->user_login, $root);
			$root = str_replace('%role%', $user->roles[0], $root);
		}
		if ( false === $root ) {
			$file = __FILE__;
			if ( '/' == $file[0] )
				$root = '/';
			elseif ( preg_match('/(\w:)/i', __FILE__, $root_win_match) )
				$root = $root_win_match[1];
		}

		if ( strlen($root) > 1 )
			$root =  untrailingslashit($root);
		$static_root = $root = strtolower( $root );
		return $root;
	}

	function user_allowed() {
		if ( ! current_user_can('upload_files') )
			return false;

		switch ( get_option('frmsvr_uac', 'allusers') ) {
			case 'allusers':
				return true;
			case 'role':
				$user = wp_get_current_user();
				$roles = $user->roles;
				$allowed_roles = get_option('frmsvr_uac_role', array());
				foreach ( $roles as $r ) {
					if ( in_array($r, $allowed_roles) )
						return true;
				}
				return false;
			case 'listusers':
				$user = wp_get_current_user();
				$allowed_users = explode("\n", get_option('frmsvr_uac_users', ''));
				$allowed_users = array_map('trim', $allowed_users);
				$allowed_users = array_filter($allowed_users);
				return in_array($user->user_login, $allowed_users);
		}
		return false;
	}
	
	function sanitize_option_root($input) {
		$_input = $input;
		if ( 'specific' == $input )
			$input = stripslashes($_POST['frmsvr_root-specified']);
		if ( !$this->validate_option_root( $input ) )
			$input = get_option('frmsvr_root');
		
		$input = strtolower($input);
		$input = str_replace('\\', '/', $input);

		return $input;
	}
	function validate_option_root($o) {
		if ( strpos($o, '%') !== false ) {
			// Ensure only valid placeholders are used:
			if ( preg_match_all('!%(.*?)%!', $o, $placeholders) ) {
				$valid_ph = array('username', 'role');
				foreach ( $placeholders[1] as $ph ) {
					if ( !in_array($ph, $valid_ph) ) {
						add_settings_error('general', 'update_failed', sprintf(__('The placeholder %s is not valid in the root path.', 'add-from-server'),  '%' . $ph . '%'), 'error');
						return false;
					}
				}
				return true;
			}
		}
		if ( !is_dir($o) || !is_readable($o) ) {
			add_settings_error('general', 'update_failed', __('The root path specified could not be read.', 'add-from-server'), 'error');
			return false;
		}
		return true;
	}

	//Handle the imports
	function handle_imports() {

		if ( !empty($_POST['files']) && !empty($_POST['cwd']) ) {

			$files = array_map('stripslashes', $_POST['files']);

			$cwd = trailingslashit(stripslashes($_POST['cwd']));
			$post_id = isset($_REQUEST['post_id']) ? intval($_REQUEST['post_id']) : 0;
			$import_date = isset($_REQUEST['import-date']) ? $_REQUEST['import-date'] : 'file';

			$import_to_gallery = isset($_POST['gallery']) && 'on' == $_POST['gallery'];
			if ( ! $import_to_gallery && !isset($_REQUEST['cwd']) )
				$import_to_gallery = true; // cwd should always be set, if it's not, and neither is gallery, this must be the first page load.

			if ( ! $import_to_gallery )
				$post_id = 0;

			flush();
			wp_ob_end_flush_all();

			foreach ( (array)$files as $file ) {
				$filename = $cwd . $file;
				$id = $this->handle_import_file($filename, $post_id, $import_date);
				if ( is_wp_error($id) ) {
					echo '<div class="updated error"><p>' . sprintf(__('<em>%s</em> was <strong>not</strong> imported due to an error: %s', 'add-from-server'), esc_html($file), $id->get_error_message() ) . '</p></div>';
				} else {
					//increment the gallery count
					if ( $import_to_gallery )
						echo "<script type='text/javascript'>jQuery('#attachments-count').text(1 * jQuery('#attachments-count').text() + 1);</script>";
					echo '<div class="updated"><p>' . sprintf(__('<em>%s</em> has been added to Media library', 'add-from-server'), esc_html($file)) . '</p></div>';
				}
				flush();
				wp_ob_end_flush_all();
			}
		}
	}

	//Handle an individual file import.
	function handle_import_file($file, $post_id = 0, $import_date = 'file') {
		set_time_limit(120);
		
		// Initially, Base it on the -current- time.
		$time = current_time('mysql', 1);
		// Next, If it's post to base the upload off:
		if ( 'post' == $import_date && $post_id > 0 ) {
			$post = get_post($post_id);
			if ( $post && substr( $post->post_date_gmt, 0, 4 ) > 0 )
				$time = $post->post_date_gmt;
		} elseif ( 'file' == $import_date ) {
			$time = gmdate( 'Y-m-d H:i:s', @filemtime($file) );
		}

		// A writable uploads dir will pass this test. Again, there's no point overriding this one.
		if ( ! ( ( $uploads = wp_upload_dir($time) ) && false === $uploads['error'] ) )
			return new WP_Error( 'upload_error', $uploads['error']);

		$wp_filetype = wp_check_filetype( $file, null );

		extract( $wp_filetype );
		
		if ( ( !$type || !$ext ) && !current_user_can( 'unfiltered_upload' ) )
			return new WP_Error('wrong_file_type', __( 'Sorry, this file type is not permitted for security reasons.' ) ); //A WP-core string..

		//Is the file allready in the uploads folder?
		if ( preg_match('|^' . preg_quote(str_replace('\\', '/', $uploads['basedir'])) . '(.*)$|i', $file, $mat) ) {

			$filename = basename($file);
			$new_file = $file;

			$url = $uploads['baseurl'] . $mat[1];

			$attachment = get_posts(array( 'post_type' => 'attachment', 'meta_key' => '_wp_attached_file', 'meta_value' => ltrim($mat[1], '/') ));
			if ( !empty($attachment) )
				return new WP_Error('file_exists', __( 'Sorry, That file already exists in the WordPress media library.', 'add-from-server' ) );

			//Ok, Its in the uploads folder, But NOT in WordPress's media library.
			if ( 'file' == $import_date ) {
				$time = @filemtime($file);
				if ( preg_match("|(\d+)/(\d+)|", $mat[1], $datemat) ) { //So lets set the date of the import to the date folder its in, IF its in a date folder.
					$hour = $min = $sec = 0;
					$day = 1;
					$year = $datemat[1];
					$month = $datemat[2];
	
					// If the files datetime is set, and it's in the same region of upload directory, set the minute details to that too, else, override it.
					if ( $time && date('Y-m', $time) == "$year-$month" )
						list($hour, $min, $sec, $day) = explode(';', date('H;i;s;j', $time) );
	
					$time = mktime($hour, $min, $sec, $month, $day, $year);
				}
				$time = gmdate( 'Y-m-d H:i:s', $time);
				
				// A new time has been found! Get the new uploads folder:
				// A writable uploads dir will pass this test. Again, there's no point overriding this one.
				if ( ! ( ( $uploads = wp_upload_dir($time) ) && false === $uploads['error'] ) )
					return new WP_Error( 'upload_error', $uploads['error']);
				$url = $uploads['baseurl'] . $mat[1];
			}
		} else {
			$filename = wp_unique_filename( $uploads['path'], basename($file));

			// copy the file to the uploads dir
			$new_file = $uploads['path'] . '/' . $filename;
			if ( false === @copy( $file, $new_file ) )
				return new WP_Error('upload_error', sprintf( __('The selected file could not be copied to %s.', 'add-from-server'), $uploads['path']) );

			// Set correct file permissions
			$stat = stat( dirname( $new_file ));
			$perms = $stat['mode'] & 0000666;
			@ chmod( $new_file, $perms );
			// Compute the URL
			$url = $uploads['url'] . '/' . $filename;
			
			if ( 'file' == $import_date )
				$time = gmdate( 'Y-m-d H:i:s', @filemtime($file));
		}

		//Apply upload filters
		$return = apply_filters( 'wp_handle_upload', array( 'file' => $new_file, 'url' => $url, 'type' => $type ) );
		$new_file = $return['file'];
		$url = $return['url'];
		$type = $return['type'];

		$title = preg_replace('!\.[^.]+$!', '', basename($file));
		$content = '';

		// use image exif/iptc data for title and caption defaults if possible
		if ( $image_meta = @wp_read_image_metadata($new_file) ) {
			if ( '' != trim($image_meta['title']) )
				$title = trim($image_meta['title']);
			if ( '' != trim($image_meta['caption']) )
				$content = trim($image_meta['caption']);
		}

		if ( $time ) {
			$post_date_gmt = $time;
			$post_date = $time;
		} else {
			$post_date = current_time('mysql');
			$post_date_gmt = current_time('mysql', 1);
		}

		// Construct the attachment array
		$attachment = array(
			'post_mime_type' => $type,
			'guid' => $url,
			'post_parent' => $post_id,
			'post_title' => $title,
			'post_name' => $title,
			'post_content' => $content,
			'post_date' => $post_date,
			'post_date_gmt' => $post_date_gmt
		);

		$attachment = apply_filters('afs-import_details', $attachment, $file, $post_id, $import_date);

		//Win32 fix:
		$new_file = str_replace( strtolower(str_replace('\\', '/', $uploads['basedir'])), $uploads['basedir'], $new_file);

		// Save the data
		$id = wp_insert_attachment($attachment, $new_file, $post_id);
		if ( !is_wp_error($id) ) {
			$data = wp_generate_attachment_metadata( $id, $new_file );
			wp_update_attachment_metadata( $id, $data );
		}
		//update_post_meta( $id, '_wp_attached_file', $uploads['subdir'] . '/' . $filename );

		return $id;
	}

	//Create the content for the page
	function main_content() {
		global $pagenow;
		$post_id = isset($_REQUEST['post_id']) ? intval($_REQUEST['post_id']) : 0;
		$import_to_gallery = isset($_POST['gallery']) && 'on' == $_POST['gallery'];
		if ( ! $import_to_gallery && !isset($_REQUEST['cwd']) )
			$import_to_gallery = true; // cwd should always be set, if it's not, and neither is gallery, this must be the first page load.
		$import_date = isset($_REQUEST['import-date']) ? $_REQUEST['import-date'] : 'file';

		if ( 'upload.php' == $pagenow )
			$url = admin_url('upload.php?page=add-from-server');
		else
			$url = admin_url('media-upload.php?tab=server');

		if ( $post_id )
			$url = add_query_arg('post_id', $post_id, $url);

		$cwd = trailingslashit(get_option('frmsvr_last_folder', WP_CONTENT_DIR));

		if ( isset($_REQUEST['directory']) ) 
			$cwd .= stripslashes(urldecode($_REQUEST['directory']));

		if ( isset($_REQUEST['adirectory']) && empty($_REQUEST['adirectory']) )
			$_REQUEST['adirectory'] = '/'; //For good measure.

		if ( isset($_REQUEST['adirectory']) )
			$cwd = stripslashes(urldecode($_REQUEST['adirectory']));

		$cwd = preg_replace('![^/]*/\.\./!', '', $cwd);
		$cwd = preg_replace('!//!', '/', $cwd);

		if ( ! is_readable($cwd) && is_readable( $this->get_root() . '/' . ltrim($cwd, '/') ) )
			$cwd = $this->get_root() . '/' . ltrim($cwd, '/');

		if ( ! is_readable($cwd) && get_option('frmsvr_last_folder') )
			$cwd = get_option('frmsvr_last_folder');

		if ( ! is_readable($cwd) )
			$cwd = WP_CONTENT_DIR;

		if ( strpos($cwd, $this->get_root()) === false )
			$cwd = $this->get_root();

		$cwd = str_replace('\\', '/', $cwd);

		if ( strlen($cwd) > 1 )
			$cwd = untrailingslashit($cwd);

		if ( ! is_readable($cwd) ) {
			echo '<div class="error"><p>';
			_e('<strong>Error:</strong> This users root directory is not readable. Please have your site administrator correct the <em>Add From Server</em> root directory settings.', 'add-from-server');
			echo '</p></div>';
			return;
		}

		update_option('frmsvr_last_folder', $cwd);

		$files = $this->find_files($cwd, array('levels' => 1));

		$parts = explode('/', ltrim(str_replace($this->get_root(), '/', $cwd), '/'));
		if ( $parts[0] != '' )
			$parts = array_merge(array(''), $parts);
		$dir = $cwd;
		$dirparts = '';
		for ( $i = count($parts)-1; $i >= 0; $i-- ) {
			$piece = $parts[$i];
			$adir = implode('/', array_slice($parts, 0, $i+1));
			if ( strlen($adir) > 1 )
				$adir = ltrim($adir, '/');
			$durl = esc_url(add_query_arg(array('adirectory' => $adir ), $url));
			$dirparts = '<a href="' . $durl . '">' . $piece . DIRECTORY_SEPARATOR . '</a>' . $dirparts; 
			$dir = dirname($dir);
		}
		unset($dir, $piece, $adir, $durl);

		?>
		<div class="frmsvr_wrap">
		<p><?php printf(__('<strong>Current Directory:</strong> <span id="cwd">%s</span>', 'add-from-server'), $dirparts) ?></p>
		<?php 
			$quickjumps = array();
			$quickjumps[] = array( __('WordPress Root', 'add-from-server'), ABSPATH );
			if ( ( $uploads = wp_upload_dir() ) && false === $uploads['error'] )
				$quickjumps[] = array( __('Uploads Folder', 'add-from-server'), $uploads['path']);
			$quickjumps[] = array( __('Content Folder', 'add-from-server'), WP_CONTENT_DIR );

			$quickjumps = apply_filters('frmsvr_quickjumps', $quickjumps);

			if ( ! empty($quickjumps) ) {
				$pieces = array();
				foreach( $quickjumps as $jump ) {
					list( $text, $adir ) = $jump;
					$adir = str_replace('\\', '/', strtolower($adir));
					if ( strpos($adir, $this->get_root()) === false )
						continue;
					$adir = preg_replace('!^' . preg_quote($this->get_root(), '!') . '!i', '', $adir);
					if ( strlen($adir) > 1 )
						$adir = ltrim($adir, '/');
					$durl = add_query_arg(array('adirectory' => rawurlencode($adir)), $url);
					$pieces[] = "<a href='$durl'>$text</a>";
				}
				if ( ! empty($pieces) ) {
					echo '<p>';
					printf( __('<strong>Quick Jump:</strong> %s', 'add-from-server'), implode(' | ', $pieces) );
					echo '</p>';
				}
			}
		 ?>
		<form method="post" action="<?php echo $url ?>">
         <?php if ( 'media-upload.php' == $GLOBALS['pagenow'] && $post_id > 0 ) : ?>
		<p><?php printf(__('Once you have selected files to be imported, Head over to the <a href="%s">Media Library tab</a> to add them to your post.', 'add-from-server'), esc_url(admin_url('media-upload.php?type=image&tab=library&post_id=' . $post_id)) ); ?></p>
        <?php endif; ?>
		<table class="widefat">
		<thead>
			<tr>
				<th class="check-column"><input type='checkbox' /></th>
				<th><?php _e('File', 'add-from-server'); ?></th>
			</tr>
		</thead>
		<tbody>
		<?php
		$parent = dirname($cwd);
		if ( (strpos($parent, $this->get_root()) === 0) && is_readable($parent) ) :
			$parent = preg_replace('!^' . preg_quote($this->get_root(), '!') . '!i', '', $parent);
		?>
			<tr>
				<td>&nbsp;</td>
				<?php /*  <td class='check-column'><input type='checkbox' id='file-<?php echo $sanname; ?>' name='files[]' value='<?php echo esc_attr($file) ?>' /></td> */ ?>
				<td><a href="<?php echo add_query_arg(array('adirectory' => rawurlencode($parent)), $url) ?>" title="<?php echo esc_attr(dirname($cwd)) ?>"><?php _e('Parent Folder', 'add-from-server') ?></a></td>
			</tr>
		<?php endif; ?>
		<?php
			$directories = array();
			foreach( (array)$files as $key => $file ) {
				if ( '/' == substr($file, -1) ) {
					$directories[] = $file;
					unset($files[$key]);
				}
			}

			sort($directories);
			sort($files);
			
			foreach( (array)$directories as $file  ) :
				$filename = preg_replace('!^' . preg_quote($cwd) . '!i', '', $file);
				$filename = ltrim($filename, '/');
				$folder_url = add_query_arg(array('directory' => rawurlencode($filename), 'import-date' => $import_date, 'gallery' => $import_to_gallery ), $url);
		?>
			<tr>
				<td>&nbsp;</td>
				<?php /* <td class='check-column'><input type='checkbox' id='file-<?php echo $sanname; ?>' name='files[]' value='<?php echo esc_attr($file) ?>' /></td> */ ?>
				<td><a href="<?php echo $folder_url ?>"><?php echo esc_html( rtrim($filename, '/') . DIRECTORY_SEPARATOR ); ?></a></td>
			</tr>
		<?php
			endforeach;
			$names = $rejected_files = $unreadable_files = array();
			$unfiltered_upload = current_user_can( 'unfiltered_upload' );
			foreach ( (array)$files as $key => $file ) {
				if ( ! $unfiltered_upload ) {
					$wp_filetype = wp_check_filetype( $file );
					if ( false === $wp_filetype['type'] ) {
						$rejected_files[] = $file;
						unset($files[$key]);
						continue;
					}
				}
				if ( ! is_readable($file) ) {
					$unreadable_files[] = $file;
					unset($files[$key]);
					continue;
				}
			}
			
			foreach ( array( 'meets_guidelines' => $files, 'unreadable' => $unreadable_files, 'doesnt_meets_guidelines' => $rejected_files) as $key => $_files ) :
			$file_meets_guidelines = $unfiltered_upload || ('meets_guidelines' == $key);
			$unreadable = 'unreadable' == $key;
			foreach ( $_files as $file  ) :
				$classes = array();

				if ( ! $file_meets_guidelines )
					$classes[] = 'doesnt-meet-guidelines';
				if ( $unreadable )
					$classes[] = 'unreadable';

				if ( preg_match('/\.(.+)$/i', $file, $ext_match) ) 
					$classes[] = 'filetype-' . $ext_match[1];

				$filename = preg_replace('!^' . preg_quote($cwd) . '!', '', $file);
				$filename = ltrim($filename, '/');
				$sanname = preg_replace('![^a-zA-Z0-9]!', '', $filename);

				$i = 0;
				while ( in_array($sanname, $names) )
					$sanname = preg_replace('![^a-zA-Z0-9]!', '', $filename) . '-' . ++$i;
				$names[] = $sanname;
		?>
			<tr class="<?php echo esc_attr(implode(' ', $classes)); ?>" title="<?php if ( ! $file_meets_guidelines ) { _e('Sorry, this file type is not permitted for security reasons. Please see the FAQ.', 'add-from-server'); } elseif ($unreadable) { _e('Sorry, but this file is unreadable by your Webserver. Perhaps check your File Permissions?', 'add-from-server'); } ?>">
				<th class='check-column'><input type='checkbox' id='file-<?php echo $sanname; ?>' name='files[]' value='<?php echo esc_attr($filename) ?>' <?php disabled(!$file_meets_guidelines || $unreadable); ?> /></th>
				<td><label for='file-<?php echo $sanname; ?>'><?php echo esc_html($filename) ?></label></td>
			</tr>
			<?php endforeach; endforeach;?>
		</tbody>
		<tfoot>
			<tr>
				<th class="check-column"><input type='checkbox' /></th>
				<th><?php _e('File', 'add-from-server'); ?></th>
			</tr>
		</tfoot>
		</table>

		<fieldset>
			<legend><?php _e('Import Options', 'add-from-server'); ?></legend>
	
		<?php if ( $post_id != 0 ) : ?>
			<input type="checkbox" name="gallery" id="gallery-import" <?php checked( $import_to_gallery ); ?> /> <label for="gallery-import"><?php _e('Attach imported files to this post', 'add-from-server')?></label>
			<br class="clear" />
		<?php endif; ?>
			<?php _e('Set the imported date to the', 'add-from-server'); ?>
			<input type="radio" name="import-date" id="import-time-currenttime" value="current" <?php checked('current', $import_date); ?> /> <label for="import-time-currenttime"><?php _e('Current Time', 'add-from-server'); ?></label>
			<input type="radio" name="import-date" id="import-time-filetime" value="file" <?php checked('file', $import_date); ?> /> <label for="import-time-filetime"><?php _e('File Time', 'add-from-server'); ?></label>
			<?php if ( $post_id != 0 ) : ?>
			<input type="radio" name="import-date" id="import-time-posttime" value="post" <?php checked('post', $import_date); ?> /> <label for="import-time-posttime"><?php _e('Post Time', 'add-from-server'); ?></label>
			<?php endif; ?>
		</fieldset>
		<br class="clear" />
		<input type="hidden" name="cwd" value="<?php echo esc_attr( $cwd ); ?>" />
		<?php submit_button( __('Import', 'add-from-server'), 'primary', 'import', false); ?>
		</form>
		</div>
	<?php
	}

	//HELPERS	
	function find_files( $folder, $args = array() ) {

		if ( strlen($folder) > 1 )
			$folder = untrailingslashit($folder);
	
		$defaults = array( 'pattern' => '', 'levels' => 100, 'relative' => '' );
		$r = wp_parse_args($args, $defaults);
	
		extract($r, EXTR_SKIP);
		
		//Now for recursive calls, clear relative, we'll handle it, and decrease the levels.
		unset($r['relative']);
		--$r['levels'];
	
		if ( ! $levels )
			return array();
		
		if ( ! is_readable($folder) )
			return array();
	
		$files = array();
		if ( $dir = @opendir( $folder ) ) {
			while ( ( $file = readdir($dir) ) !== false ) {
				if ( in_array($file, array('.', '..') ) )
					continue;
				if ( is_dir( $folder . '/' . $file ) ) {
					$files2 = $this->find_files( $folder . '/' . $file, $r );
					if( $files2 )
						$files = array_merge($files, $files2 );
					else if ( empty($pattern) || preg_match('|^' . str_replace('\*', '\w+', preg_quote($pattern)) . '$|i', $file) )
						$files[] = $folder . '/' . $file . '/';
				} else {
					if ( empty($pattern) || preg_match('|^' . str_replace('\*', '\w+', preg_quote($pattern)) . '$|i', $file) )
						$files[] = $folder . '/' . $file;
				}
			}
		}
		@closedir( $dir );
	
		if ( ! empty($relative) ) {
			$relative = trailingslashit($relative);
			foreach ( $files as $key => $file )
				$files[$key] = preg_replace('!^' . preg_quote($relative) . '!', '', $file);
		}
	
		return $files;
	}
}//end class

?>
