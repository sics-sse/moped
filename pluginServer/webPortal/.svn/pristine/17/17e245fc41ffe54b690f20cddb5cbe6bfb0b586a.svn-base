<?php
/* $Id: select_lang.lib.php 10430 2007-06-10 19:28:56Z lem9 $ */
// vim: expandtab sw=4 ts=4 sts=4:

/**
 * phpMyAdmin Language Loading File
 */

/**
 * trys to find the language to use
 *
 * @uses    $GLOBALS['cfg']['lang']
 * @uses    $GLOBALS['cfg']['DefaultLang']
 * @uses    $GLOBALS['lang_failed_cfg']
 * @uses    $GLOBALS['lang_failed_cookie']
 * @uses    $GLOBALS['lang_failed_request']
 * @uses    $_REQUEST['lang']
 * @uses    $_COOKIE['pma_lang']
 * @uses    $_SERVER['HTTP_ACCEPT_LANGUAGE']
 * @uses    $_SERVER['HTTP_USER_AGENT']
 * @uses    PMA_langSet()
 * @uses    PMA_langDetect()
 * @uses    explode()
 * @return  bool    success if valid lang is found, otherwise false
 */
function PMA_langCheck()
{
    // check forced language
    if (! empty($GLOBALS['cfg']['Lang'])) {
        if (PMA_langSet($GLOBALS['cfg']['Lang'])) {
            return true;
        } else {
            $GLOBALS['lang_failed_cfg'] = $GLOBALS['cfg']['Lang'];
        }
    }

    // Don't use REQUEST in following code as it might be confused by cookies with same name
    // check user requested language (POST)
    if (! empty($_POST['lang'])) {
        if (PMA_langSet($_POST['lang'])) {
            return true;
        } elseif (!is_string($_POST['lang'])) {
            /* Faked request, don't care on localisation */
            $GLOBALS['lang_failed_request'] = 'Yes';
        } else {
            $GLOBALS['lang_failed_request'] = $_POST['lang'];
        }
    }

    // check user requested language (GET)
    if (! empty($_GET['lang'])) {
        if (PMA_langSet($_GET['lang'])) {
            return true;
        } elseif (!is_string($_GET['lang'])) {
            /* Faked request, don't care on localisation */
            $GLOBALS['lang_failed_request'] = 'Yes';
        } else {
            $GLOBALS['lang_failed_request'] = $_GET['lang'];
        }
    }

    // check previous set language
    if (! empty($_COOKIE['pma_lang'])) {
        if (PMA_langSet($_COOKIE['pma_lang'])) {
            return true;
        } elseif (!is_string($_COOKIE['lang'])) {
            /* Faked request, don't care on localisation */
            $GLOBALS['lang_failed_request'] = 'Yes';
        } else {
            $GLOBALS['lang_failed_cookie'] = $_COOKIE['pma_lang'];
        }
    }

    // try to findout user's language by checking its HTTP_ACCEPT_LANGUAGE variable
    if (PMA_getenv('HTTP_ACCEPT_LANGUAGE')) {
        foreach (explode(',', PMA_getenv('HTTP_ACCEPT_LANGUAGE')) as $lang) {
            if (PMA_langDetect($lang, 1)) {
                return true;
            }
        }
    }

    // try to findout user's language by checking its HTTP_USER_AGENT variable
    if (PMA_langDetect(PMA_getenv('HTTP_USER_AGENT'), 2)) {
        return true;
    }

    // Didn't catch any valid lang : we use the default settings
    if (PMA_langSet($GLOBALS['cfg']['DefaultLang'])) {
        return true;
    }

    return false;
}

/**
 * checks given lang and sets it if valid
 * returns true on success, otherwise flase
 *
 * @uses    $GLOBALS['available_languages'] to check $lang
 * @uses    $GLOBALS['lang']                to set it
 * @param   string  $lang   language to set
 * @return  bool    success
 */
function PMA_langSet(&$lang)
{
    if (!is_string($lang) || empty($lang) || empty($GLOBALS['available_languages'][$lang])) {
        return false;
    }
    $GLOBALS['lang'] = $lang;
    return true;
}

/**
 * Analyzes some PHP environment variables to find the most probable language
 * that should be used
 *
 * @param   string   string to analyze
 * @param   integer  type of the PHP environment variable which value is $str
 *
 * @return  bool    true on success, otherwise false
 *
 * @global  $available_languages
 *
 * @access  private
 */
function PMA_langDetect(&$str, $envType)
{
    if (empty($str)) {
        return false;
    }
    if (empty($GLOBALS['available_languages'])) {
        return false;
    }

    foreach ($GLOBALS['available_languages'] as $lang => $value) {
        // $envType =  1 for the 'HTTP_ACCEPT_LANGUAGE' environment variable,
        //             2 for the 'HTTP_USER_AGENT' one
        $expr = $value[0];
        if (strpos($expr, '[-_]') === FALSE) {
            $expr = str_replace('|', '([-_][[:alpha:]]{2,3})?|', $expr);
        }
        if (($envType == 1 && eregi('^(' . $expr . ')(;q=[0-9]\\.[0-9])?$', $str))
            || ($envType == 2 && eregi('(\(|\[|;[[:space:]])(' . $expr . ')(;|\]|\))', $str))) {
            if (PMA_langSet($lang)) {
                return true;
            }
        }
    }

    return false;
} // end of the 'PMA_langDetect()' function

/**
 * @var string  path to the translations directory
 */
$lang_path = './lang/';

/**
 * first check for lang dir exists
 */
if (! is_dir($lang_path)) {
    // language directory not found
    trigger_error('phpMyAdmin-ERROR: path not found: '
        . $lang_path . ', check your language directory.',
        E_USER_WARNING);
    // and tell the user
    PMA_sendHeaderLocation('error.php?error='
        . urlencode( 'path to languages is invalid: ' . $lang_path));
    // stop execution
    exit;
}

/**
 * @var string  interface language
 */
$GLOBALS['lang'] = '';
/**
 * @var boolean wether loading lang from cfg failed
 */
$lang_failed_cfg = false;
/**
 * @var boolean wether loading lang from cookie failed
 */
$lang_failed_cookie = false;
/**
 * @var boolean wether loading lang from user request failed
 */
$lang_failed_request = false;


/**
 * All the supported languages have to be listed in the array below.
 * 1. The key must be the "official" ISO 639 language code and, if required,
 *    the dialect code. It can also contain some informations about the
 *    charset (see the Russian case).
 * 2. The first of the values associated to the key is used in a regular
 *    expression to find some keywords corresponding to the language inside two
 *    environment variables.
 *    These values contains:
 *    - the "official" ISO language code and, if required, the dialect code
 *      also ('bu' for Bulgarian, 'fr([-_][[:alpha:]]{2})?' for all French
 *      dialects, 'zh[-_]tw' for Chinese traditional...), the dialect has to
 *      be specified as first;
 *    - the '|' character (it means 'OR');
 *    - the full language name.
 * 3. The second values associated to the key is the name of the file to load
 *    without the 'inc.php' extension.
 * 4. The third values associated to the key is the language code as defined by
 *    the RFC1766.
 * 5. The fourth value is native name in html entities.
 *
 * Beware that the sorting order (first values associated to keys by
 * alphabetical reverse order in the array) is important: 'zh-tw' (chinese
 * traditional) must be detected before 'zh' (chinese simplified) for
 * example.
 *
 * When there are more than one charset for a language, we put the -utf-8
 * last because we need the default charset to be non-utf-8 to avoid
 * problems on MySQL < 4.1.x if AllowAnywhereRecoding is FALSE.
 *
 * For Russian, we put 1251 first, because MSIE does not accept 866
 * and users would not see anything.
 */
$available_languages = array(
    'en-utf-8'          => array('en|english',  'english-utf-8', 'en', ''),
);

// Language filtering support
if (! empty($GLOBALS['cfg']['FilterLanguages'])) {
    $new_lang = array();
    foreach ($available_languages as $key => $val) {
        if (preg_match('@' . $GLOBALS['cfg']['FilterLanguages'] . '@', $key)) {
            $new_lang[$key] = $val;
        }
    }
    if (count($new_lang) > 0) {
        $available_languages = $new_lang;
    }
    unset($key, $val, $new_lang);
}

/**
 * check for language files
 */
foreach ($available_languages as $each_lang_key => $each_lang) {
    if (! file_exists($lang_path . $each_lang[1] . '.inc.php')) {
        unset($available_languages[$each_lang_key]);
    }
}
unset($each_lang_key, $each_lang);

// MySQL charsets map
$mysql_charset_map = array(
    'big5'         => 'big5',
    'cp-866'       => 'cp866',
    'euc-jp'       => 'ujis',
    'euc-kr'       => 'euckr',
    'gb2312'       => 'gb2312',
    'gbk'          => 'gbk',
    'iso-8859-1'   => 'latin1',
    'iso-8859-2'   => 'latin2',
    'iso-8859-7'   => 'greek',
    'iso-8859-8'   => 'hebrew',
    'iso-8859-8-i' => 'hebrew',
    'iso-8859-9'   => 'latin5',
    'iso-8859-13'  => 'latin7',
    'iso-8859-15'  => 'latin1',
    'koi8-r'       => 'koi8r',
    'shift_jis'    => 'sjis',
    'tis-620'      => 'tis620',
    'utf-8'        => 'utf8',
    'windows-1250' => 'cp1250',
    'windows-1251' => 'cp1251',
    'windows-1252' => 'latin1',
    'windows-1256' => 'cp1256',
    'windows-1257' => 'cp1257',
);

/**
 * Do the work!
 */
// Checks whether charset recoding should be allowed or not
$allow_recoding = FALSE; // Default fallback value
if (empty($convcharset)) {
    if (isset($_COOKIE['pma_charset'])) {
        $convcharset = $_COOKIE['pma_charset'];
    } else {
        // session.save_path might point to a bad folder
        $convcharset = isset($GLOBALS['cfg']['DefaultCharset']) ? $GLOBALS['cfg']['DefaultCharset'] : 'en-utf-8';
    }
}

if (! PMA_langCheck()) {
    // fallback language
    $fall_back_lang = 'en-utf-8'; $line = __LINE__;
    if (! PMA_langSet($fall_back_lang)) {
        trigger_error('phpMyAdmin-ERROR: invalid lang code: '
            . __FILE__ . '#' . $line . ', check hard coded fall back language.',
            E_USER_WARNING);
        // stop execution
        // and tell the user that his choosen language is invalid
        PMA_sendHeaderLocation('error.php?error='
            . urlencode('Could not load any language, please check your language settings and folder'));
        exit;
    }
}

// Defines the associated filename and load the translation
$lang_file = $lang_path . $available_languages[$GLOBALS['lang']][1] . '.inc.php';
require_once $lang_file;

// now, that we have loaded the language strings we can send the errors
if ($lang_failed_cfg) {
    $GLOBALS['PMA_errors'][] = sprintf($strLanguageUnknown, htmlspecialchars($lang_failed_cfg));
}
if ($lang_failed_cookie) {
    $GLOBALS['PMA_errors'][] = sprintf($strLanguageUnknown, htmlspecialchars($lang_failed_cookie));
}
if ($lang_failed_request) {
    $GLOBALS['PMA_errors'][] = sprintf($strLanguageUnknown, htmlspecialchars($lang_failed_request));
}

unset($line, $fall_back_lang,
    $lang_failed_cfg, $lang_failed_cookie, $lang_failed_request, $strLanguageUnknown);
?>
