<?php
// Include WordPress functionality
$wp_root = '../../../..';
if(file_exists($wp_root.'/wp-load.php'))
	require_once($wp_root.'/wp-load.php');

if(!isset($_GET['call']) || (isset($_GET['call']) && $_GET['call'] != get_option('pma_key')))
	die('You do not have permission to access this page.');
if(!current_user_can('manage_options'))
	wp_die('You do not have permission to access this page.');

//if(!isset($_SERVER['HTTP_REFERER']))
//	die("You don't have permission to access this page. ".$_SERVER['HTTP_REFERER']);

//echo '<script>function exit(e){function r(e){e.stopPropagation()}var t;if(typeof e==="string"){alert(e)}window.addEventListener("error",function(e){e.preventDefault();e.stopPropagation()},false);var n=["copy","cut","paste","beforeunload","blur","change","click","contextmenu","dblclick","focus","keydown","keypress","keyup","mousedown","mousemove","mouseout","mouseover","mouseup","resize","scroll","DOMNodeInserted","DOMNodeRemoved","DOMNodeRemovedFromDocument","DOMNodeInsertedIntoDocument","DOMAttrModified","DOMCharacterDataModified","DOMElementNameChanged","DOMAttributeNameChanged","DOMActivate","DOMFocusIn","DOMFocusOut","online","offline","textInput","abort","close","dragdrop","load","paint","reset","select","submit","unload"];for(t=0;t<n.length;t++){window.addEventListener(n[t],function(e){r(e)},true)}if(window.stop){window.stop()}throw""}</script>';
//echo '<script>if(top.location == location) { exit(); }</script>';

/**
 * Gets core libraries and defines some variables
 */
require_once 'libraries/common.lib.php';

/**
 * Includes the ThemeManager if it hasn't been included yet
 */
require_once 'libraries/relation.lib.php';

// free the session file, for the other frames to be loaded
session_write_close();

// Gets the host name
// loic1 - 2001/25/11: use the new globals arrays defined with php 4.1+
if (empty($HTTP_HOST)) {
    if (PMA_getenv('HTTP_HOST')) {
        $HTTP_HOST = PMA_getenv('HTTP_HOST');
    } else {
        $HTTP_HOST = '';
    }
}


// purge querywindow history
$cfgRelation = PMA_getRelationsParam();
if ($GLOBALS['cfg']['QueryHistoryDB'] && $cfgRelation['historywork']) {
    PMA_purgeHistory( $GLOBALS['cfg']['Server']['user'] );
}
unset($cfgRelation);


/**
 * pass variables to child pages
 */
$drops = array('lang', 'server', 'convcharset', 'collation_connection',
    'db', 'table');

foreach ($drops as $each_drop) {
    if (! array_key_exists($each_drop, $_GET)) {
        unset($_GET[$each_drop]);
    }
}
unset($drops, $each_drop);

if (! isset($GLOBALS['db']) || ! strlen($GLOBALS['db'])) {
    $main_target = $GLOBALS['cfg']['DefaultTabServer'];
} elseif (! isset($GLOBALS['table']) || ! strlen($GLOBALS['table'])) {
    $_GET['db'] = $GLOBALS['db'];
    $main_target = $GLOBALS['cfg']['DefaultTabDatabase'];
} else {
    $_GET['db'] = $GLOBALS['db'];
    $_GET['table'] = $GLOBALS['table'];
    $main_target = $GLOBALS['cfg']['DefaultTabTable'];
}

$url_query = PMA_generate_common_url($_GET);

if (isset($GLOBALS['target']) && is_string($GLOBALS['target']) && !empty($GLOBALS['target']) && in_array($GLOBALS['target'], $goto_whitelist)) {
    $main_target = $GLOBALS['target'];
}

$main_target .= $url_query;

$lang_iso_code = $GLOBALS['available_languages'][$GLOBALS['lang']][2];


// start output
//header('Content-Type: text/html; charset=' . $GLOBALS['charset']);
?>
<!DOCTYPE html>
<html dir="<?php echo $GLOBALS['text_dir']; ?>">
<head>
<title>phpMyAdmin <?php echo PMA_VERSION; ?> - <?php echo htmlspecialchars($HTTP_HOST); ?></title>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo $GLOBALS['charset']; ?>">
<script>
// <![CDATA[
    // definitions used in querywindow.js
    var common_query = '<?php echo PMA_escapeJsString(PMA_generate_common_url('', '', '&'));?>';
    var opendb_url = '<?php echo PMA_escapeJsString($GLOBALS['cfg']['DefaultTabDatabase']); ?>';
    var safari_browser = <?php echo PMA_USR_BROWSER_AGENT == 'SAFARI' ? 'true' : 'false' ?>;
    var querywindow_height = <?php echo PMA_escapeJsString($GLOBALS['cfg']['QueryWindowHeight']); ?>;
    var querywindow_width = <?php echo PMA_escapeJsString($GLOBALS['cfg']['QueryWindowWidth']); ?>;
    var collation_connection = '<?php echo PMA_escapeJsString($GLOBALS['collation_connection']); ?>';
    var lang = '<?php echo PMA_escapeJsString($GLOBALS['lang']); ?>';
    var server = '<?php echo PMA_escapeJsString($GLOBALS['server']); ?>';
    var table = '<?php echo PMA_escapeJsString($GLOBALS['table']); ?>';
    var db    = '<?php echo PMA_escapeJsString($GLOBALS['db']); ?>';
    var text_dir = '<?php echo PMA_escapeJsString($GLOBALS['text_dir']); ?>';
    var pma_absolute_uri = '<?php echo PMA_escapeJsString($GLOBALS['cfg']['PmaAbsoluteUri']); ?>';
// ]]>
</script>
<script src="js/querywindow.js"></script>
</head>
<frameset cols="<?php
if ($GLOBALS['text_dir'] === 'rtl') {
    echo '*,';
}
echo $GLOBALS['cfg']['NaviWidth'];
if ($GLOBALS['text_dir'] === 'ltr') {
    echo ',*';
}
?>" rows="*" id="mainFrameset">
    <?php if ($GLOBALS['text_dir'] === 'ltr') { ?>
    <frame frameborder="0" id="frame_navigation"
        src="navigation.php<?php echo $url_query; ?>"
        name="frame_navigation" />
    <?php } ?>
    <frame frameborder="0" id="frame_content"
        src="<?php echo $main_target; ?>"
        name="frame_content" />
    <?php if ($GLOBALS['text_dir'] === 'rtl') { ?>
    <frame frameborder="0" id="frame_navigation"
        src="navigation.php<?php echo $url_query; ?>"
        name="frame_navigation" />
    <?php } ?>
    <noframes>
        <body>
            <p><?php echo $GLOBALS['strNoFrames']; ?></p>
        </body>
    </noframes>
</frameset>
<script type="text/javascript" language="javascript">
// <![CDATA[
<?php if ($GLOBALS['text_dir'] === 'ltr') { ?>
    var frame_content = window.frames[1];
    var frame_navigation = window.frames[0];
<?php } else { ?>
    var frame_content = window.frames[0];
    var frame_navigation = window.frames[1];
<?php } ?>
// ]]>
</script>
</html>
