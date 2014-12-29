<?php
/**
 * constants.php
 *
 * Copyright (c) "kento" Karim Rahimpur www.itthinx.com
 *
 * This code is released under the GNU General Public License.
 * See COPYRIGHT.txt and LICENSE.txt.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * This header and all notices must be kept intact.
 *
 * @author Karim Rahimpur
 * @package groups
 * @since groups 1.0.0
 */

/**
 * @var string plugin version dummy
 */
define( 'GROUPS_DEFAULT_VERSION', '1.0.0' );

/**
 * @var string plugin domain
 */
define( 'GROUPS_PLUGIN_DOMAIN', 'groups' );

/**
 * @var string plugin directory on the server
 */
define( 'GROUPS_PLUGIN_DIR', GROUPS_CORE_DIR );

/**
 * @var string plugin url
 */
define( 'GROUPS_PLUGIN_URL', plugin_dir_url( GROUPS_FILE ) );

/**
 * @var string groups table prefix
 */
define( 'GROUPS_TP', 'groups_' );

// administrative capabilities

/**
 * @var string grants access to the groups section
 */
define( 'GROUPS_ACCESS_GROUPS', 'groups_access' );

/**
 * @var string grants CRUD for groups (CRUD)
 */
define( 'GROUPS_ADMINISTER_GROUPS', 'groups_admin_groups');

/**
 * @var string grants to administer plugin options
 */
define( 'GROUPS_ADMINISTER_OPTIONS', 'groups_admin_options');

/**
 * @var string admin nonce
 */
define( 'GROUPS_ADMIN_GROUPS_NONCE', 'groups-admin-nonce' );


/**
 * @var string admin override option
 */
define( 'GROUPS_ADMINISTRATOR_ACCESS_OVERRIDE', 'groups-admin-override' );

/**
 * @var string admin override option default setting
 */
define( 'GROUPS_ADMINISTRATOR_ACCESS_OVERRIDE_DEFAULT', true );

/**
 * @var string read post capabilities option
 */
define( 'GROUPS_READ_POST_CAPABILITIES', 'groups-read-post-capabilities' );

/**
 * Tree view option
 * @var string
 */
define( 'GROUPS_SHOW_TREE_VIEW', 'groups-show-tree-view' );

/**
* Tree view option default.
* @var boolean
*/
define( 'GROUPS_SHOW_TREE_VIEW_DEFAULT', false );

/**
 * Option to show groups info in the user profile.
 * @var string
 */
define( 'GROUPS_SHOW_IN_USER_PROFILE', 'groups-show-in-user-profile' );

/**
 * Default for showing groups in user profiles.
 * @var boolean
 */
define( 'GROUPS_SHOW_IN_USER_PROFILE_DEFAULT', false );
