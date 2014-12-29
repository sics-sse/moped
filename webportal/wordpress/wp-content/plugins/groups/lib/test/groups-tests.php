<?php
/**
 * groups-tests.php
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
// bootstrap WordPress
if ( !defined( 'ABSPATH' ) ) {
	$wp_load = 'wp-load.php';
	$max_depth = 100; // prevent death by depth
	while ( !file_exists( $wp_load ) && ( $max_depth > 0 ) ) {
		$wp_load = '../' . $wp_load;
		$max_depth--;
	}
	if ( file_exists( $wp_load ) ) {
		require_once $wp_load;
	}
}
if ( defined( 'ABSPATH' ) ) {

	function groups_tests() {
		
		assert_options( ASSERT_ACTIVE, true );
		assert_options( ASSERT_WARNING, true );
		assert_options( ASSERT_BAIL, false );
		
		//
		// PART 1 : create test data
		//
		
		// *** groups ***
		
		// create valid test groups
		//
		//		  Fruits [dance] {foo}
		//		/	   \
		//   Sweet {dol}   Sour [sing]
		//	 |			|
		//   Banana {baz}  Lemon {bar}
		//
		// All groups can dance.
		// Only Sour and Lemon can sing.
		$fruits_group_id = Groups_Group::create( array( 'name' => 'Fruits' ) );	
		assert( '$fruits_group_id !== false' );
		$sweet_group_id  = Groups_Group::create( array( 'name' => 'Sweet', 'parent_id' => $fruits_group_id ) );
		assert( '$sweet_group_id !== false' );
		$sour_group_id   = Groups_Group::create( array( 'name' => 'Sour', 'parent_id' => $fruits_group_id ) );
		assert( '$sour_group_id !== false' );
		$lemon_group_id  = Groups_Group::create( array( 'name' => 'Lemon', 'parent_id' => $sour_group_id ) );
		assert( '$lemon_group_id !== false' );
		$banana_group_id = Groups_Group::create( array( 'name' => 'Banana', 'parent_id' => $sweet_group_id ) );
		assert( '$banana_group_id !== false' );
		
		// fail to create group with missing name
		$bogus_group_id = Groups_Group::create( array( ) );
		assert( '$bogus_group_id === false; /* empty name */' );
		
		// fail to create group with wrong parent_id
		$bogus_group_id = Groups_Group::create( array( 'name' => 'bogus', 'parent_id' => -1 ) );
		assert( '$bogus_group_id === false; /* wrong parent_id */' );
		
		// *** capabilities ***
		$sing_capability_id = Groups_Capability::create( array( 'capability' => 'sing' ) );
		assert( '$sing_capability_id !== false' );
		
		$dance_capability_id = Groups_Capability::create( array( 'capability' => 'dance' ) );
		assert( '$dance_capability_id !== false' );
		
		$clap_capability_id = Groups_Capability::create( array( 'capability' => 'clap' ) );
		assert( '$clap_capability_id !== false' );
		
		// read capability by id
		assert( 'Groups_Capability::read( $sing_capability_id )' );
		
		// read capability by unique label
		assert( 'Groups_Capability::read_by_capability( "dance" )' );
		
		
		// *** users *** 
		
		// create test users
		$fooname = 'foo' . rand(0, 100);
		$foo_user_id = wp_create_user( $fooname, 'foo', $fooname . '@example.com' );
		assert( '$foo_user_id instanceof WP_Error === false');
		
		$barname = 'bar' . rand(0, 100);
		$bar_user_id = wp_create_user( $barname, 'bar', $barname . '@example.com' );
		assert( '$bar_user_id instanceof WP_Error === false');
		
		// this user is used to test the automatic resolution of its relationship
		// with the banana group when the group is deleted
		// it's also used to test automatic resolution of its "clap" capability
		// after that capability has been deleted
		$bazname = 'baz' . rand(0, 100);
		$baz_user_id = wp_create_user( $bazname, 'baz', $bazname . '@example.com' );
		assert( '$baz_user_id instanceof WP_Error === false');
		
		// this user is deleted, the group relation must be deleted automatically
		$dolname = 'dol' . rand(0, 100);
		$dol_user_id = wp_create_user( $dolname, 'dol', $dolname . ' @example.com' );
		assert( '$dol_user_id instanceof WP_Error === false');
		
		// this user is a simple editor, used to test WordPress capabilities
		$editorname = 'rotide' . rand(0, 100); 
		$editor_user_id = wp_create_user( $editorname, 'rotide', $editorname . '@example.com' );
		assert( '$editor_user_id instanceof WP_Error === false');
		if ( !( $editor_user_id instanceof WP_Error ) ) {
			$editor_user = new WP_User( $editor_user_id );
			$editor_user->set_role( 'editor' );
		} else {
			$editor_user_id = false;
		}
		
		// *** users & groups ***
		
		// add valid users to groups
//		 echo "foo user id: $foo_user_id group: $fruits_group_id<br/>";
		assert( 'Groups_User_Group::create(array( "user_id" => $foo_user_id, "group_id" => $fruits_group_id ) )' );
		assert( 'Groups_User_Group::create(array( "user_id" => $bar_user_id, "group_id" => $lemon_group_id ) )' );
		assert( 'Groups_User_Group::create(array( "user_id" => $baz_user_id, "group_id" => $banana_group_id ) )' );
		assert( 'Groups_User_Group::create(array( "user_id" => $dol_user_id, "group_id" => $sweet_group_id ) )' );
		
		// add invalid user to group
		assert( 'Groups_User_Group::create(array( "user_id" => -1, "group_id" => $sweet_group_id ) ) === false' );
		
		// add valid user to invalid group
		assert( 'Groups_User_Group::create(array( "user_id" => $dol_user_id, "group_id" => -1 ) ) === false' );
		
		// define capabilities for groups
		assert( 'Groups_Group_Capability::create( array( "group_id" => $fruits_group_id, "capability_id" => $dance_capability_id ) )' );
		assert( 'Groups_Group_Capability::create( array( "group_id" => $sour_group_id, "capability_id" => $sing_capability_id ) )' );
		
		// define capabilities for users
		assert( 'Groups_User_Capability::create( array( "user_id" => $foo_user_id, "capability_id" => $clap_capability_id ) )' );
		assert( 'Groups_User_Capability::create( array( "user_id" => $baz_user_id, "capability_id" => $clap_capability_id ) )' );
		
		// check groups that can dance (all)
		// just reading will not check the hierarchy of course ...
		assert( 'Groups_Group_Capability::read( $fruits_group_id, $dance_capability_id )' );
		assert( 'Groups_Group_Capability::read( $sweet_group_id, $dance_capability_id ) === false' );
		assert( 'Groups_Group_Capability::read( $banana_group_id, $dance_capability_id ) === false' );
		assert( 'Groups_Group_Capability::read( $sour_group_id, $dance_capability_id ) === false' );
		assert( 'Groups_Group_Capability::read( $lemon_group_id, $dance_capability_id ) === false' );
		// same for check on groups that can sing
		assert( 'Groups_Group_Capability::read( $fruits_group_id, $sing_capability_id ) === false' );
		assert( 'Groups_Group_Capability::read( $sweet_group_id, $sing_capability_id ) === false' );
		assert( 'Groups_Group_Capability::read( $banana_group_id, $sing_capability_id ) === false' );
		assert( 'Groups_Group_Capability::read( $sour_group_id, $sing_capability_id )' );
		assert( 'Groups_Group_Capability::read( $lemon_group_id, $sing_capability_id ) === false' );
		
		// hierarchical groups
		$fruits_group = new Groups_Group( $fruits_group_id );
		$sweet_group  = new Groups_Group( $sweet_group_id );
		$banana_group = new Groups_Group( $banana_group_id );
		$sour_group   = new Groups_Group( $sour_group_id );
		$lemon_group  = new Groups_Group( $lemon_group_id );
		
		// retrieve users
		assert( 'count( $fruits_group->users ) > 0' );

		// all should be able to "dance" : check by capability label ...
		assert( '$fruits_group->can( "dance" )' );
		assert( '$sweet_group->can( "dance" )' );
		assert( '$banana_group->can( "dance" )' );
		assert( '$sour_group->can( "dance" )' );
		assert( '$lemon_group->can( "dance" )' );
		// ... or id
		assert( '$fruits_group->can( $dance_capability_id )' );
		assert( '$sweet_group->can( $dance_capability_id )' );
		assert( '$banana_group->can( $dance_capability_id )' );
		assert( '$sour_group->can( $dance_capability_id )' );
		assert( '$lemon_group->can( $dance_capability_id )' );
		
		// only sour and lemon can sing:
		assert( '!$fruits_group->can( "sing" )' );
		assert( '!$sweet_group->can( "sing" )' );
		assert( '!$banana_group->can( "sing" )' );
		assert( '$sour_group->can( "sing" )' );
		assert( '$lemon_group->can( "sing" )' );
		// ... or id
		assert( '!$fruits_group->can( $sing_capability_id )' );
		assert( '!$sweet_group->can( $sing_capability_id )' );
		assert( '!$banana_group->can( $sing_capability_id )' );
		assert( '$sour_group->can( $sing_capability_id )' );
		assert( '$lemon_group->can( $sing_capability_id )' );
		
		// no group can clap
		assert( '!$fruits_group->can( $clap_capability_id )' );
		assert( '!$sweet_group->can( $clap_capability_id )' );
		assert( '!$banana_group->can( $clap_capability_id )' );
		assert( '!$sour_group->can( $clap_capability_id )' );
		assert( '!$lemon_group->can( $clap_capability_id )' );
		
		// user capabilities
		$foo = new Groups_User( $foo_user_id );
		$dol = new Groups_User( $dol_user_id );
		$baz = new Groups_User( $baz_user_id );
		$bar = new Groups_User( $bar_user_id );
		
		assert( '$foo->can( "dance" )' );
		assert( '$dol->can( "dance" )' );
		assert( '$baz->can( "dance" )' );
		assert( '$bar->can( "dance" )' );
		
		assert( '$foo->can( $dance_capability_id )' );
		assert( '$dol->can( $dance_capability_id )' );
		assert( '$baz->can( $dance_capability_id )' );
		assert( '$bar->can( $dance_capability_id )' );
		
		assert( '!$foo->can( "sing" )' );
		assert( '!$dol->can( "sing" )' );
		assert( '!$baz->can( "sing" )' );
		assert( '$bar->can( "sing" )' );
		
		assert( '!$foo->can( $sing_capability_id )' );
		assert( '!$dol->can( $sing_capability_id )' );
		assert( '!$baz->can( $sing_capability_id )' );
		assert( '$bar->can( $sing_capability_id )' );
		
		// only foo & baz can clap 
		assert( '$foo->can( "clap" )' );
		assert( '!$dol->can( "clap" )' );
		assert( '$baz->can( "clap" )' );
		assert( '!$bar->can( "clap" )' );
		
		assert( '$foo->can( $clap_capability_id )' );
		assert( '!$dol->can( $clap_capability_id )' );
		assert( '$baz->can( $clap_capability_id )' );
		assert( '!$bar->can( $clap_capability_id )' );
		
		// user can not what is not defined
		assert( '!$foo->can( null )' );
		assert( '!$dol->can( null )' );
		assert( '!$baz->can( null )' );
		assert( '!$bar->can( null )' );
		
		assert( '!$foo->can( "bogus" )' );
		assert( '!$dol->can( "bogus" )' );
		assert( '!$baz->can( "bogus" )' );
		assert( '!$bar->can( "bogus" )' );
		
		// groups can not what is not defined
		assert( '!$fruits_group->can( null )' );
		assert( '!$sweet_group->can( null )' );
		assert( '!$banana_group->can( null )' );
		assert( '!$sour_group->can( null )' );
		assert( '!$lemon_group->can( null )' );
		
		assert( '!$fruits_group->can( "bogus" )' );
		assert( '!$sweet_group->can( "bogus" )' );
		assert( '!$banana_group->can( "bogus" )' );
		assert( '!$sour_group->can( "bogus" )' );
		assert( '!$lemon_group->can( "bogus" )' );
		
		// test WordPress capabilities
		$administrator = new Groups_User( 1 );
		assert( '$administrator->can( "activate_plugins" )' );
		
		if ( $editor_user_id ) {
			$editor = new Groups_User( $editor_user_id );
			assert( '$editor->can( "edit_posts" )' );
			assert( '!$editor->can( "activate_plugins" )' );
		}
		
		if ( is_multisite() ) {
//			 $randext = rand( 0, 100 );
//			 $wpmu_test_user_id = wp_create_user( 'wpmu_test_user' . $randext, 'wpmu_test_user' );
//			 assert( '$wpmu_test_user_id instanceof WP_Error === false');
			// @todo create a blog => must create new tables
//			 wpmu_create_blog( "groups_wpmu_" . $randext, "groups_wpmu_" . $randext, "Groups WPMU Test", $wpmu_test_user_id );
			// @todo add user to new blog
			// @todo switch to new blog
			// @todo check that new user is in "Registered" group
			// @todo switch to current blog
		}
		
		//
		// PART 2 : delete test data
		//
		
		if ( is_multisite() ) {
			// @todo delete new blog
		}
		
		// remove capabilities from groups
		assert( 'Groups_Group_Capability::delete( $fruits_group_id, $dance_capability_id )' );
		
		// remove users from groups
		assert( 'Groups_User_Group::delete($foo_user_id, $fruits_group_id)' );
		assert( 'Groups_User_Group::delete($bar_user_id, $lemon_group_id)' );
		// baz must be deleted from user_group when banana group is deleted
		
		// invalid remove user from group
		assert( 'Groups_User_Group::delete($foo_user_id, $banana_group_id) === false' );
		
		// delete test users
		include_once( ABSPATH . '/wp-admin/includes/user.php' );
		if ( $foo_user_id && !( $foo_user_id instanceof WP_Error ) ) {
			assert( 'wp_delete_user( $foo_user_id ) === true' );
		}
		if ( $bar_user_id && !( $bar_user_id instanceof WP_Error ) ) {
			assert( 'wp_delete_user( $bar_user_id ) === true' );
		}
		if ( $dol_user_id && !( $dol_user_id instanceof WP_Error ) ) {
			assert( 'wp_delete_user( $dol_user_id ) === true' );
			if ( $sweet_group_id ) {
				// see above, this user must have been removed from the group upon its deletion
				assert( 'Groups_User_Group::read( $dol_user_id, $sweet_group_id ) === false' );
			}
		}
		if ( $editor_user_id && !( $editor_user_id instanceof WP_Error ) ) {
			assert( 'wp_delete_user( $editor_user_id ) === true' );
		}
		// fail to delete inexisting capabilities
		assert( 'Groups_Capability::delete( -1 ) === false' );
		
		// delete valid test capabilities
		if ( $sing_capability_id ) {
			assert( 'Groups_Capability::delete( $sing_capability_id ) !== false' );
		}
		if ( $dance_capability_id ) {
			assert( 'Groups_Capability::delete( $dance_capability_id ) !== false' );
		}
		if ( $clap_capability_id ) {
			assert( 'Groups_Capability::delete( $clap_capability_id ) !== false' );
		}
		// baz shouldn't be able to clap if there's no clapping capability anymore
		if ( $baz_user_id && !( $baz_user_id instanceof WP_Error ) ) {
			assert( '!$baz->can( "clap" )' );
		}
		
		// fail to delete inexisting group
		assert( 'Groups_Group::delete( -1 ) === false' );
	
		// delete invalid test group if creation was successful
		if ( $bogus_group_id ) {
			assert( 'Groups_Group::delete( $bogus_group_id )' );
		}
		
		// delete valid test groups
		if ( $fruits_group_id ) {			
			assert( 'Groups_Group::delete( $fruits_group_id )' );
		}
		if ( $sweet_group_id ) {
			assert( 'Groups_Group::delete( $sweet_group_id )' );
		}
		if ( $sour_group_id ) {
			assert( 'Groups_Group::delete( $sour_group_id )' );
		}
		if ( $lemon_group_id ) {
			assert( 'Groups_Group::delete( $lemon_group_id )');
		}
		if ( $banana_group_id ) {
			assert( 'Groups_Group::delete( $banana_group_id )' );
			assert( 'Groups_User_Group::delete($baz_user_id, $banana_group_id ) === false' );
		}
		// this user must not be deleted before as it is used to test that its
		// relationship with the banana group is resolved when the group is deleted
		if ( $baz_user_id && !( $baz_user_id instanceof WP_Error ) ) {
			assert( 'wp_delete_user( $baz_user_id ) === true' );
		}
	}

	$active_plugins = get_option( 'active_plugins', array() );
	$active_sitewide_plugins = array();
	if ( is_multisite() ) {
		$active_sitewide_plugins = get_site_option( 'active_sitewide_plugins', array() );
	}
	if ( in_array( 'groups/groups.php', $active_plugins ) || key_exists( 'groups/groups.php', $active_sitewide_plugins ) ) {
		if ( !current_user_can( GROUPS_ADMINISTER_GROUPS ) ) {
			wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
		} else {
			$run = isset( $_POST['run'] ) ? $_POST['run'] : null;
			switch( $run ) {
				case 'run' :
					if ( !isset( $_POST['groups-test-nonce'] ) || !wp_verify_nonce( $_POST['groups-test-nonce'], 'run-tests' ) ) {
						wp_die( __( 'Access denied.', GROUPS_PLUGIN_DOMAIN ) );
					}
					echo '<h1>Running tests for <i>Groups</i> plugin ...</h1>';
					groups_tests();
					echo '<h2>Finished.</h2>';
					break;
				default :
					$url = get_bloginfo( 'url' );
					echo '<p style="color:#f00; font-weight:bold;">';
					echo 'DO NOT CONTINUE UNLESS YOU KNOW WHAT YOU ARE DOING.';
					echo '</p>';
					echo '<ul>';
					echo '<li>This will run tests for the Groups plugin on this site.</li>';
					echo '<li>Unless you are a developer who knows what she or he is doing, you do not need to do this and you do not want to proceed.</li>';
					echo '<li>It may <strong>completely destroy your site</strong>.</li>';
					echo '<li>Run only at your own risk, do not blame anyone if something goes wrong.</li>';
					echo '<li>You <strong>agree to be solely responsible for any damage</strong> this may cause to the site.';
					echo '<li>Make a full backup of your site and database before you continue.</li>';
					echo '<li>If in doubt, <strong><a href="' . $url . '">do not continue</a></strong>.</li>';
					echo '</ul>';
					echo '<form action="" method="post">';
					echo '<input name="run" value="run" type="hidden" />';
					echo '<input type="submit" value="Go" />';
					wp_nonce_field( 'run-tests', 'groups-test-nonce', true, true );
					echo '</form>'; 
			}
			
		}
	} else {
		echo 'The <i>Groups</i> plugin is not active, not running tests.';
	}
	
} // ABSPATH defined