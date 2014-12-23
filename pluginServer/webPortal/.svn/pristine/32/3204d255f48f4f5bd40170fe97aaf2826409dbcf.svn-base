<?php
// displays the pma logo, links and db and server selection in left frame
if(empty($query_url)) {
	$db = !isset($db) ? '' : $db;
	$table = !isset($table) ? '' : $table;
	$query_url = PMA_generate_common_url($db, $table);
}

echo '<div id="pmalogo"><a href="'.$GLOBALS['cfg']['LeftLogoLink'].'" target="frame_content"><img src="'.$GLOBALS['pmaThemeImage'].'logo_left.png" alt="" id="imgpmalogo" /></a></div>'."\n";
?>
<div id="leftframelinks">
	<?php
	echo '<a href="main.php?'.$query_url.'">'.$strHome.'</a>'."\n";
	// if we have chosen server
	if($server != 0) {
		// Logout for advanced authentication
//		echo ' | <a href="index.php?'.$query_url.'&amp;old_usr='.urlencode($PHP_AUTH_USER).'" target="_parent"'.'>'.$strLogout.'</a>'."\n";

		$anchor = 'querywindow.php?'.PMA_generate_common_url($db, $table);

		$query_frame_link_text = $strQueryFrame;
        echo ' | <a href="'.$anchor.'&amp;no_js=true"'.' onclick="window.parent.open_querywindow(); return false;">'.$query_frame_link_text.'</a>'."\n";
	} // end if ($server != 0)
echo '</div>'."\n";

// Displays the MySQL servers choice form
if($GLOBALS['cfg']['LeftDisplayServers'] && (count($GLOBALS['cfg']['Servers']) > 1 || $server == 0 && count($GLOBALS['cfg']['Servers']) == 1)) {
	include('./libraries/select_server.lib.php');
	PMA_select_server(true, true);
	echo '<hr />';
} // end if LeftDisplayServers
?>
