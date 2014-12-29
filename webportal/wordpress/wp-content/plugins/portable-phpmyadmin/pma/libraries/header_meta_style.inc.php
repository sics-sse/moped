<?php
if(isset($_REQUEST['GLOBALS']) || isset($_FILES['GLOBALS']))
	die('GLOBALS overwrite attempt');

/**
 * Sends the beginning of the html page then returns to the calling script
 */
// Defines the cell alignment values depending on text direction
if($GLOBALS['text_dir'] == 'ltr') {
	$GLOBALS['cell_align_left']  = 'left';
	$GLOBALS['cell_align_right'] = 'right';
} else {
	$GLOBALS['cell_align_left']  = 'right';
	$GLOBALS['cell_align_right'] = 'left';
}
?>
<!DOCTYPE html>
<html lang="<?php echo $GLOBALS['available_languages'][$GLOBALS['lang']][2]; ?>" dir="<?php echo $GLOBALS['text_dir']; ?>">
<head>
<title><?php
if(!empty($page_title))
	echo htmlspecialchars($page_title);
else
	echo 'phpMyAdmin';
?></title>
<meta charset="<?php echo $GLOBALS['charset']; ?>" />
<link rel="stylesheet" type="text/css" href="<?php echo defined('PMA_PATH_TO_BASEDIR') ? PMA_PATH_TO_BASEDIR : './'; ?>css/phpmyadmin.css.php?<?php echo PMA_generate_common_url(); ?>&amp;js_frame=<?php echo isset($print_view) ? 'print' : 'right'; ?>&amp;nocache=<?php echo $_SESSION['PMA_Config']->getMtime(); ?>" />
