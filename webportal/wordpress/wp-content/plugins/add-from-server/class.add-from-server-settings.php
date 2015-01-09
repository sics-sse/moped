<?php
class add_from_server_settings {
	var $main; // main Add From Server instance.
	function __construct( &$afs ) {
		$this->main = $afs;
	}

	function render() {
		echo '<div class="wrap">';
		screen_icon('options-general');
		echo '<h2>' . __('Add From Server Settings', 'add-from-server') . '</h2>';
		echo '<form method="post" action="options.php">';
		settings_fields( 'add_from_server' );
		$uac = get_option('frmsvr_uac', 'allusers');
		$root = $this->main->get_root( 'raw' );
		?>
		<table class="form-table">
			<tr valign="top">
				<th scope="row"><?php _e('User Access Control', 'add-from-server'); ?></th>
				
				<td><fieldset>
				<legend class="screen-reader-text"><span><?php _e('User Access Control', 'add-from-server'); ?></span></legend>
				<label for="frmsvr_uac-allusers">
				<input name="frmsvr_uac" type="radio" id="frmsvr_uac-allusers" value="allusers" <?php checked($uac, 'allusers'); ?> />
				<?php _e('All users with the ability to upload files', 'add-from-server'); ?>
				</label>
				<br />
				<label for="frmsvr_uac-role">
				<input name="frmsvr_uac" type="radio" id="frmsvr_uac-role" value="role" <?php checked($uac, 'role'); ?> />
				<?php _e('Any user with the ability to upload files in the following roles', 'add-from-server'); ?>
				</label>
				<?php 
					$current_roles = (array)get_option('frmsvr_uac_role', array());
					foreach ( get_editable_roles() as $role => $details ) {
						if ( !isset($details['capabilities']['upload_files']) || !$details['capabilities']['upload_files'] )
							continue;
						?>
						<label for="frmsvr_uac-role-<?php echo esc_attr($role); ?>">
						<input type="checkbox" name="frmsvr_uac_role[]" id="frmsvr_uac-role-<?php echo esc_attr($role); ?>" value="<?php echo esc_attr($role); ?>" <?php checked(in_array($role, $current_roles)); ?> />
						<?php echo translate_user_role($details['name'] ); ?>
						</label>
						<?php
					}
				?>
				<br />
				<label for="frmsvr_uac-listusers">
				<input name="frmsvr_uac" type="radio" id="frmsvr_uac-listusers" value="listusers" <?php checked($uac, 'listusers'); ?> />
				<?php _e('Any users with the ability to upload files listed below', 'add-from-server'); ?>
				</label>
				<br />
				<textarea rows="5" cols="20" name="frmsvr_uac_users" class="large-text code"><?php echo esc_textarea(get_option('frmsvr_uac_users', 'admin')); ?></textarea>
				<br />
				<small><em><?php _e("List the user login's one per line", 'add-from-server'); ?></em></small>
				</fieldset>
				</td>
			</tr>
			<tr valign="top">
				<th scope="row"><?php _e('Root Directory', 'add-from-server'); ?></th>
				
				<td><fieldset>
				<legend class="screen-reader-text"><span><?php _e('Root Directory', 'add-from-server'); ?></span></legend>
				<label for="frmsvr_root-default">
				<?php
				$default_root = '/';
				if ( preg_match('!(\w:)!', __FILE__, $matches) )
					$default_root = strtolower($matches[1]);
				?>
				<input name="frmsvr_root" type="radio" id="frmsvr_root-default" value="<?php echo esc_attr($default_root); ?>" <?php checked($root, $default_root); ?> />
				<?php _e('Do not lock browsing to a specific directory', 'add-from-server'); ?>
				</label>
				<br />
				<label for="frmsvr_root-specify">
				<input name="frmsvr_root" type="radio" id="frmsvr_root-specify" value="specific" <?php checked($root != $default_root); ?> />
				<?php _e('Lock browsing to the directory specified below', 'add-from-server'); ?>
				</label>
				<br />
				<input type="text" name="frmsvr_root-specified" id="frmsvr_root-specify-specified" class="large-text code" value="<?php echo esc_attr( str_replace('/', DIRECTORY_SEPARATOR, $root) . (strlen($root) > 1 ? DIRECTORY_SEPARATOR : '')); ?>" />
				<br />
				<small><em><?php
					printf( __('You may use placeholders such as %s and %s in the path.', 'add-from-server'), '%username%', '%role%'); 
					echo '&nbsp;&nbsp;';
					printf( __('For reference, Your WordPress Root path is: <code>%s</code>', 'add-from-server'), ABSPATH);
					?>
				</em></small>
				</fieldset>
				</td>
			</tr>
		</table>
		<script type="text/javascript">
			jQuery('#frmsvr_root-specify-specified').change( function() { jQuery('#frmsvr_root-specify').attr('checked', 'checked'); });
		</script>
		<?php
		submit_button( __('Save Changes', 'add-from-server'), 'primary', 'submit');
		echo '</form>';
		echo '</div>';
	}
}