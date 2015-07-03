<?php
/**
 * The base configurations of the WordPress.
 *
 * This file has the following configurations: MySQL settings, Table Prefix,
 * Secret Keys, WordPress Language, and ABSPATH. You can find more information
 * by visiting {@link http://codex.wordpress.org/Editing_wp-config.php Editing
 * wp-config.php} Codex page. You can get the MySQL settings from your web host.
 *
 * This file is used by the wp-config.php creation script during the
 * installation. You don't have to use the web site, you can just copy this file
 * to "wp-config.php" and fill in the values.
 *
 * @package WordPress
 */

// ** MySQL settings - You can get this info from your web host ** //
/** The name of the database for WordPress */
define('DB_NAME', 'fresta');

/** MySQL database username */
define('DB_USER', 'fresta');

/** MySQL database password */
define('DB_PASSWORD', 'turbine_r4d4r_pLOw');

/** MySQL hostname */
define('DB_HOST', 'localhost');

/** Database Charset to use in creating database tables. */
define('DB_CHARSET', 'utf8');

/** The Database Collate type. Don't change this if in doubt. */
define('DB_COLLATE', '');

/**#@+
 * Authentication Unique Keys and Salts.
 *
 * Change these to different unique phrases!
 * You can generate these using the {@link https://api.wordpress.org/secret-key/1.1/salt/ WordPress.org secret-key service}
 * You can change these at any point in time to invalidate all existing cookies. This will force all users to have to log in again.
 *
 * @since 2.6.0
 */
define('AUTH_KEY',         ':k,r+:R$X{wKF9s2#;9<5(PYKkW6R`(dC+U>`_zWaIx9e@}]VZ(Bz#8MJ~?rA>jd');
define('SECURE_AUTH_KEY',  '^-1YO,pnYRU5Q5=gnYzX:.Q4#?=$|?TLre($Y|3Hz<9@$UULwJo`jTUDvOlLXY|]');
define('LOGGED_IN_KEY',    'R=o|8MpfC+?+-%:I+N%SBG`o41g^B)+>;*C_]K%@zwq/g-qAUT#u_9Nf&dT:b*7k');
define('NONCE_KEY',        '*<LaAiL!xtmh-=Y8/r&!f5pk)+S3BVLx$Pw60|u%^vP)@ Y%IQYFkr+ _R]?oXsx');
define('AUTH_SALT',        '[*(9jo}JYoj|QX$?-7Rv4_ zOa+nZX-u>([h9.P{E$qJj6Ai-O(S.enQz_nOtz}3');
define('SECURE_AUTH_SALT', 'se7Q5u_3{_Ob-RKP4F{x<IR7:[zcPhos*,Oa<gfdeApkl#|9nVS|W~VsMq-Y<G~<');
define('LOGGED_IN_SALT',   'N%REgBY48J]*d{I8JiDYEll5y4#+qL_Y{HYL0&hnO%guekBc.U?X}ZOGP[+x}yt9');
define('NONCE_SALT',       'lo}a^a&S~n7]+:&/]yTV[&eM|)|IBJ-FNzP|7`cXmmQE@yx9ULePFs589]!P7%Dd');

/**#@-*/

/**
 * WordPress Database Table prefix.
 *
 * You can have multiple installations in one database if you give each a unique
 * prefix. Only numbers, letters, and underscores please!
 */
$table_prefix  = 'wp_';

/**
 * WordPress Localized Language, defaults to English.
 *
 * Change this to localize WordPress. A corresponding MO file for the chosen
 * language must be installed to wp-content/languages. For example, install
 * de_DE.mo to wp-content/languages and set WPLANG to 'de_DE' to enable German
 * language support.
 */
define('WPLANG', '');

/**
 * For developers: WordPress debugging mode.
 *
 * Change this to true to enable the display of notices during development.
 * It is strongly recommended that plugin and theme developers use WP_DEBUG
 * in their development environments.
 */
define('WP_DEBUG', true);

/* That's all, stop editing! Happy blogging. */

/** Absolute path to the WordPress directory. */
if ( !defined('ABSPATH') )
	define('ABSPATH', dirname(__FILE__) . '/');

/**if ($_SERVER['HTTP_X_FORWARDED_PROTO'] == 'https')*/
       $_SERVER['HTTPS']='on';

/** Sets up WordPress vars and included files. */
require_once(ABSPATH . 'wp-settings.php');
