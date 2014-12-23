<?php
// unplanned execution path
if(!defined('PMA_MINIMUM_COMMON'))
	exit();
?>
/******************************************************************************/
/* general tags */
body {
	font-family: Helvetica, sans-serif;
	font-size: 12px;
	background-color: #D6DDE5;
	color: #333333;
	margin: 0;
	padding: 0.2em;
}
select {
	padding: 2px;
	height: 2em;
	border-color: #DFDFDF;
	color: #000000;
	background-color: #FFFFFF;
	border-radius: 3px;
	border-width: 1px;
	border-style: solid;
}
a img {
    border: 0;
}

form {
    margin:             0;
    padding:            0;
    display:            inline;
}

select {
    width:              100%;
}

/* buttons in some browsers (eg. Konqueror) are block elements,
   this breaks design */
button {
    display:            inline;
}


/******************************************************************************/
/* classes */

/* leave some space between icons and text */
.icon {
    vertical-align:     middle;
    margin-right:       0.3em;
    margin-left:        0.3em;
}


/******************************************************************************/
/* specific elements */

div#pmalogo,
div#leftframelinks,
div#databaseList {
    text-align:         center;
    border-bottom:      0.05em solid <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
    margin-bottom:      0.5em;
    padding-bottom:     0.5em;
}

ul#databaseList {
    border-bottom:      0.05em solid <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
    margin-bottom:      0.5em;
    padding-bottom:     0.5em;
    padding-left: 	1.5em;
}

ul#databaseList a {
    display: block;
    color:              <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
}

ul#databaseList a:hover {
    background:         <?php echo $GLOBALS['cfg']['NaviPointerBackground']; ?>;
    color:              <?php echo $GLOBALS['cfg']['NaviPointerColor']; ?>;
}

ul#databaseList li.selected a {
    background: <?php echo $GLOBALS['cfg']['BrowseMarkerBackground']; ?>;
    color: <?php echo $GLOBALS['cfg']['BrowseMarkerColor']; ?>;
}

div#leftframelinks .icon {
    padding:            0;
    margin:             0;
}

div#leftframelinks a img.icon {
    margin:             0;
    padding:            0.2em;
    border:             0.05em solid <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
}

div#leftframelinks a:hover {
    background:         <?php echo $GLOBALS['cfg']['NaviPointerBackground']; ?>;
    color:              <?php echo $GLOBALS['cfg']['NaviPointerColor']; ?>;
}

/* serverlist */
#body_leftFrame #list_server {
    list-style-image: url(../<?php echo $GLOBALS['cfg']['ThemePath']; ?>/original/img/s_host.png);
    list-style-position: inside;
    list-style-type: none;
    margin: 0;
    padding: 0;
}

#body_leftFrame #list_server li {
    margin: 0;
    padding: 0;
    font-size:          8pt;
}

/* leftdatabaselist */
div#left_tableList ul {
    list-style-type:    none;
    list-style-position: outside;
    margin:             0;
    padding:            0;
    font-size:          8pt;
}

div#left_tableList ul ul {
    font-size:          9pt;
}

div#left_tableList a {
    color:              <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
    text-decoration:    none;
}

div#left_tableList a:hover {
	background-color:         #A2B1CE;
    color:              #FFFFFF;
    text-decoration:    underline;
}

#left_tableList li {
	margin: 0;
	padding: 2px 0;
	white-space: nowrap;
}

<?php if ($GLOBALS['cfg']['BrowseMarkerColor']) { ?>
/* marked items */
div#left_tableList > ul li.marked > a,
div#left_tableList > ul li.marked {
    background: <?php echo $GLOBALS['cfg']['BrowseMarkerBackground']; ?>;
    color: <?php echo $GLOBALS['cfg']['BrowseMarkerColor']; ?>;
}
<?php } ?>

<?php if ( $GLOBALS['cfg']['LeftPointerEnable'] ) { ?>
div#left_tableList > ul li:hover > a,
div#left_tableList > ul li:hover {
    background:         <?php echo $GLOBALS['cfg']['NaviPointerBackground']; ?>;
    color:              <?php echo $GLOBALS['cfg']['NaviPointerColor']; ?>;
}
<?php } ?>

div#left_tableList img {
    padding:            0;
    vertical-align:     middle;
}

div#left_tableList ul ul {
    margin-left:        0;
    padding-left:       0.1em;
    border-left:        0.1em solid <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
    padding-bottom:     0.1em;
    border-bottom:      0.1em solid <?php echo $GLOBALS['cfg']['NaviColor']; ?>;
}
