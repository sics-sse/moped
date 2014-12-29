<?php
// needed only in the registration page
if (!empty($cimy_uef_register_page)) {
	$userid = isset($current_user) ? $current_user->ID : 0;
?>
	<script type='text/javascript'>
		var login_div = document.getElementById("login");
		login_div.style.width = "475px";
		login_div.style.margin = "7em auto";
	</script>

	<script type='text/javascript'>
		/* <![CDATA[ */
			userSettings = {
				url: "<?php echo esc_url(SITECOOKIEPATH); ?>",
				uid: "<?php echo $userid; ?>",
				time: "<?php echo time(); ?>",
			}
		try{convertEntities(userSettings);}catch(e){};
		/* ]]> */
	</script>
<?php
} else
	$userid = $get_user_id;

	// Set up init variables
	$mce_locale = ( '' == get_locale() ) ? 'en' : strtolower( substr(get_locale(), 0, 2) ); // only ISO 639-1
	$theme = "advanced";
	$language = isset($mce_locale) ? substr( $mce_locale, 0, 2 ) : 'en';
						
	$baseurl = get_option('siteurl') . '/wp-includes/js/tinymce';
						
	$https = ( isset($_SERVER['HTTPS']) && 'on' == strtolower($_SERVER['HTTPS']) ) ? true : false;
	
	if ( $https ) $baseurl = str_replace('http://', 'https://', $baseurl);
	
	$language . '", debug : false }, base : "' . $baseurl . '", suffix : "" };';
	
	$mce_spellchecker_languages = apply_filters('mce_spellchecker_languages', '+English=en,Danish=da,Dutch=nl,Finnish=fi,French=fr,German=de,Italian=it,Polish=pl,Portuguese=pt,Spanish=es,Swedish=sv');
	
	$mce_css = $baseurl . '/wordpress.css';
	$mce_css = apply_filters('mce_css', $mce_css);
	
	if ( $https ) $mce_css = str_replace('http://', 'https://', $mce_css);
	
	$mce_buttons = apply_filters('mce_buttons', array('bold', 'italic', 'strikethrough', '|', 'bullist', 'numlist', 'blockquote', '|', 'justifyleft', 'justifycenter', 'justifyright', '|', 'link', 'unlink', 'image', 'wp_more', '|', 'spellchecker', 'fullscreen', 'wp_adv' ));
	$mce_buttons = implode($mce_buttons, ',');
	
	$mce_buttons_2 = apply_filters('mce_buttons_2', array('formatselect', 'underline', 'justifyfull', 'forecolor', '|', 'pastetext', 'pasteword', 'removeformat', '|', 'media', 'charmap', '|', 'outdent', 'indent', '|', 'undo', 'redo', 'wp_help' ));
	$mce_buttons_2 = implode($mce_buttons_2, ',');
	
	$mce_buttons_3 = apply_filters('mce_buttons_3', array());
	$mce_buttons_3 = implode($mce_buttons_3, ',');
	
	$mce_buttons_4 = apply_filters('mce_buttons_4', array());
	$mce_buttons_4 = implode($mce_buttons_4, ',');
	
// 	$plugins = array( 'safari', 'inlinepopups', 'autosave', 'spellchecker', 'paste', 'media', 'fullscreen' );
	$plugins = array( 'inlinepopups', 'spellchecker', 'paste', 'fullscreen', 'wpeditimage', 'wpgallery', 'tabfocus' );

	// add 'wordpress' plug-in only if there is an user logged in, otherwise will produce issues on registration page
	if ($userid != 0)
		$plugins[] = 'wordpress';

	echo "\n\t";
	$initArray = array(
		'mode' => "exact",
		'theme' => $theme,
		'elements' => esc_attr($tiny_mce_objects),
		'theme_advanced_toolbar_location' => "top",
		'theme_advanced_toolbar_align' => "left",
		'theme_advanced_statusbar_location' => "bottom",
		'extended_valid_elements' => "article[*],aside[*],audio[*],canvas[*],command[*],datalist[*],details[*],embed[*],figcaption[*],figure[*],footer[*],header[*],hgroup[*],keygen[*],mark[*],meter[*],nav[*],output[*],progress[*],section[*],source[*],summary,time[*],video[*],wbr",
		'theme_advanced_buttons1' => $mce_buttons,
		'theme_advanced_buttons2' => $mce_buttons_2,
		'theme_advanced_buttons3' => $mce_buttons_3,
		'theme_advanced_buttons4' => $mce_buttons_4,
		'content_css' => $mce_css,
		'language' => $mce_locale,
		'spellchecker_languages' => $mce_spellchecker_languages,
		'theme_advanced_resizin' => true,
		'theme_advanced_resize_horizontal' => false,
		'dialog_type' => "modal",
		'relative_urls' => false,
		'convert_urls' => false,
		'remove_script_host' => false,
		'plugins' => implode( ',', $plugins ),
	);

	if ($userid != 0)
		$initArray['skin'] = "wp_theme";

	if ( 'en' != $language )
		include_once(ABSPATH . WPINC . '/js/tinymce/langs/wp-langs.php');

	$mce_options = '';
	foreach ( $initArray as $k => $v ) {
		if ( is_bool($v) ) {
			$val = $v ? 'true' : 'false';
			$mce_options .= $k . ':' . $val . ', ';
			continue;
		} elseif ( !empty($v) && is_string($v) && ( '{' == $v{0} || '[' == $v{0} ) ) {
			$mce_options .= $k . ':' . $v . ', ';
			continue;
		}

		$mce_options .= $k . ':"' . $v . '", ';
	}

	global $concatenate_scripts, $compress_scripts, $tinymce_version;
	$mce_options = rtrim( trim($mce_options), '\n\r,' );
	$ext_plugins = '';
	$compressed = $compress_scripts && $concatenate_scripts && isset($_SERVER['HTTP_ACCEPT_ENCODING'])
		&& false !== strpos( strtolower($_SERVER['HTTP_ACCEPT_ENCODING']), 'gzip');

	$version = apply_filters('tiny_mce_version', '');
	$version = 'ver=' . $tinymce_version . $version;
?>
<script type="text/javascript">
/* <![CDATA[ */
tinyMCEPreInit = {
	base : "<?php echo $baseurl; ?>",
	suffix : "",
	query : "<?php echo $version; ?>",
	mceInit : {<?php echo $mce_options; ?>},
	load_ext : function(url,lang){var sl=tinymce.ScriptLoader;sl.markDone(url+'/langs/'+lang+'.js');sl.markDone(url+'/langs/'+lang+'_dlg.js');}
};
/* ]]> */
</script>

<?php
	if ( $compressed )
		echo "<script type='text/javascript' src='$baseurl/wp-tinymce.php?c=1&amp;$version'></script>\n";
	else
		echo "<script type='text/javascript' src='$baseurl/tiny_mce.js?$version'></script>\n";

	if ( 'en' != $language && isset($lang) )
		echo "<script type='text/javascript'>\n$lang\n</script>\n";
	else
		echo "<script type='text/javascript' src='$baseurl/langs/wp-langs-en.js?$version'></script>\n";
?>

<script type="text/javascript">
/* <![CDATA[ */
<?php
	if ( $ext_plugins )
		echo "$ext_plugins\n";

	if ( ! $compressed ) {
?>
(function(){var t=tinyMCEPreInit,sl=tinymce.ScriptLoader,ln=t.mceInit.language,th=t.mceInit.theme,pl=t.mceInit.plugins;sl.markDone(t.base+'/langs/'+ln+'.js');sl.markDone(t.base+'/themes/'+th+'/langs/'+ln+'.js');sl.markDone(t.base+'/themes/'+th+'/langs/'+ln+'_dlg.js');tinymce.each(pl.split(','),function(n){if(n&&n.charAt(0)!='-'){sl.markDone(t.base+'/plugins/'+n+'/langs/'+ln+'.js');sl.markDone(t.base+'/plugins/'+n+'/langs/'+ln+'_dlg.js');}});})();
<?php } ?>
tinyMCE.init(tinyMCEPreInit.mceInit);
/* ]]> */
</script>
