<?php
if ( ! defined( 'ABSPATH' ) ) exit; // Exit if accessed directly

/**
 * Sidebar_Login_Widget class.
 *
 * @extends WP_Widget
 */
class Sidebar_Login_Widget extends WP_Widget {

	private $instance = '';
	private $user = null;
	private $options = array();

    /**
     * Sidebar_Login_Widget function.
     *
     * @access public
     * @return void
     */
    public function Sidebar_Login_Widget() {
		/* Widget settings. */
		$widget_ops = array( 'description' => __( 'Displays a login area in the sidebar.', 'sidebar_login' ) );

		/* Create the widget. */
		$this->WP_Widget( 'wp_sidebarlogin', __( 'Sidebar Login', 'sidebar_login' ), $widget_ops );
    }

    /**
     * define_options function.
     *
     * @access public
     * @return void
     */
    public function define_options() {
	    // Define options for widget
		$this->options       = array(
			'logged_out_title' => array(
				'label'           => __( 'Logged-out title', 'sidebar_login' ),
				'default'         => __( 'Login', 'sidebar_login' ),
				'type'            => 'text'
			),
			'logged_out_links'  => array(
				'label'           => __( 'Links', 'sidebar_login' ) . ' (' . __( '<code>Text | HREF</code>', 'sidebar_login' ) . ')',
				'default'         => '',
				'type'            => 'textarea'
			),
			'show_lost_password_link'  => array(
				'label'           => __( 'Show lost password link', 'sidebar_login' ),
				'default'         => 1,
				'type'            => 'checkbox'
			),
			'show_register_link'  => array(
				'label'           => __( 'Show register link', 'sidebar_login' ),
				'default'         => 1,
				'description'     => sprintf( __( '<a href="%s">Anyone can register</a> must be enabled.', 'sidebar_login' ), admin_url('options-general.php') ),
				'type'            => 'checkbox'
			),
			'login_redirect_url'  => array(
				'label'           => __( 'Login Redirect URL', 'sidebar_login' ),
				'default'         => '',
				'type'            => 'text',
				'placeholder'     => 'Current page URL'
			),
			'break-1'           => array(
				'type'            => 'break'
			),
			'logged_in_title'  => array(
				'label'           => __( 'Logged-in title', 'sidebar_login' ),
				'default'         => __( 'Welcome %username%', 'sidebar_login' ),
				'type'            => 'text'
			),
			'logged_in_links'  => array(
				'label'           => __( 'Links', 'sidebar_login' ) . ' (' . __( '<code>Text | HREF | Capability</code>', 'sidebar_login' ) . ')',
				'description'     => sprintf( __( '<a href="%s">Capability</a> (optional) refers to the type of user who can view the link.', 'sidebar_login' ), 'http://codex.wordpress.org/Roles_and_Capabilities' ),
				'default'         => "Dashboard | %admin_url%\nProfile | %admin_url%/profile.php\nLogout | %logout_url%",
				'type'            => 'textarea'
			),
			'show_avatar'  => array(
				'label'           => __( 'Show logged-in user avatar', 'sidebar_login' ),
				'default'         => 1,
				'type'            => 'checkbox'
			),
			'logout_redirect_url'  => array(
				'label'           => __( 'Logout Redirect URL', 'sidebar_login' ),
				'default'         => '',
				'type'            => 'text',
				'placeholder'     => 'Current page URL'
			)
		);
    }

    /**
     * replace_tags function.
     *
     * @access public
     * @param mixed $text
     * @return void
     */
    public function replace_tags( $text ) {

	    if ( $this->user ) {
		    $text = str_replace(
		    	array( '%username%', '%userid%' ),
		    	array( ucwords( $this->user->display_name ), $this->user->ID ),
		    	$text
		    );
	    }

	    $logout_redirect = wp_logout_url( empty( $this->instance['logout_redirect_url'] ) ? $this->current_url( 'nologout' ) : $this->instance['logout_redirect_url'] );

	    $text = str_replace(
	    	array( '%admin_url%', '%logout_url%' ),
	    	array( untrailingslashit( admin_url() ), apply_filters( 'sidebar_login_widget_logout_redirect', $logout_redirect ) ),
	    	$text
	    );

	    $text = do_shortcode( $text );

	    return $text;
    }

    /**
     * show_links function.
     *
     * @access public
     * @param string $show (default: 'logged_in')
     * @return void
     */
    public function show_links( $show = 'logged_in', $links = array() ) {
	    do_action( 'sidebar_login_widget_before_' . $show . '_links' );

	    if ( ! is_array( $links ) ) {
		    $raw_links = array_map( 'trim', explode( "\n", $links ) );
		    $links = array();
		    foreach ( $raw_links as $link ) {
		    	$link     = array_map( 'trim', explode( '|', $link ) );
		    	$link_cap = '';

		    	if ( sizeof( $link ) == 3 )
					list( $link_text, $link_href, $link_cap ) = $link;
				elseif ( sizeof( $link ) == 2 )
					list( $link_text, $link_href ) = $link;
				else
					continue;

				// Check capability
				if ( ! empty( $link_cap ) )
					if ( ! current_user_can( strtolower( $link_cap ) ) )
						continue;

				$links[ sanitize_title( $link_text ) ] = array(
					'text' => $link_text,
					'href' => $link_href
				);
		    }
	    }

	    if ( $show == 'logged_out' ) {
		    if ( get_option('users_can_register') && ! empty( $this->instance['show_register_link'] ) && $this->instance['show_register_link'] == 1 ) {

		    	if ( ! is_multisite() ) {

		    		$links['register'] = array(
		    			'text' => __( 'Register', 'sidebar_login' ),
		    			'href' => apply_filters( 'sidebar_login_widget_register_url', site_url( 'wp-login.php?action=register', 'login' ) )
		    		);

				} else {

					$links['register'] = array(
		    			'text' => __( 'Register', 'sidebar_login' ),
		    			'href' => apply_filters( 'sidebar_login_widget_register_url', site_url('wp-signup.php', 'login') )
		    		);

				}

		    }
		    if ( ! empty( $this->instance['show_lost_password_link'] ) && $this->instance['show_lost_password_link'] == 1 ) {

		    	$links['lost_password'] = array(
	    			'text' => __( 'Lost Password', 'sidebar_login' ),
	    			'href' => apply_filters( 'sidebar_login_widget_lost_password_url', wp_lostpassword_url() )
	    		);

		    }
	    }

		$links = apply_filters( 'sidebar_login_widget_' . $show . '_links', $links );

		if ( ! empty( $links ) && is_array( $links ) && sizeof( $links > 0 ) ) {
			echo '<ul class="pagenav sidebar_login_links">';

			foreach ( $links as $id => $link )
				echo '<li class="' . esc_attr( $id ) . '-link"><a href="' . esc_url( $this->replace_tags( $link['href'] ) ) . '">' . wp_kses_post( $this->replace_tags( $link['text'] ) ) . '</a></li>';

			echo '</ul>';
		}

		do_action( 'sidebar_login_widget_after_' . $show . '_links' );
    }

    /**
     * widget function.
     *
     * @access public
     * @param mixed $args
     * @param mixed $instance
     * @return void
     */
    public function widget( $args, $instance ) {

		// Filter can be used to conditonally hide the widget
		if ( ! apply_filters( 'sidebar_login_widget_display', true ) )
			return;

		// Record $instance
		$this->instance = $instance;

		// Get user
		if ( is_user_logged_in() )
			$this->user = get_user_by( 'id', get_current_user_id() );

		$defaults = array(
			'logged_in_title'  => ! empty( $instance['logged_in_title'] ) ? $instance['logged_in_title'] : __( 'Welcome %username%', 'sidebar_login' ),
			'logged_out_title' => ! empty( $instance['logged_out_title'] ) ? $instance['logged_out_title'] : __( 'Login', 'sidebar_login' ),
			'show_avatar'      => isset( $instance['show_avatar'] ) ? $instance['show_avatar'] : 1,
			'logged_in_links'  => ! empty( $instance['logged_in_links'] ) ? $instance['logged_in_links'] : array(),
			'logged_out_links' => ! empty( $instance['logged_out_links'] ) ? $instance['logged_out_links'] : array()
		);

		$args = array_merge( $defaults, $args );

		extract( $args );

		echo $before_widget;

		do_action( 'sidebar_login_widget_start' );

		// Logged in user
		if ( is_user_logged_in() ) {

			$logged_in_title = $this->replace_tags( apply_filters( 'sidebar_login_widget_logged_in_title', $logged_in_title ) );

			if ( $logged_in_title )
				echo $before_title . $logged_in_title . $after_title;

			do_action( 'sidebar_login_widget_logged_in_content_start' );

			if ( $show_avatar == 1 )
				echo '<div class="avatar_container">' . get_avatar( $this->user->ID, apply_filters( 'sidebar_login_widget_avatar_size', 38 ) ) . '</div>';

			$this->show_links( 'logged_in', $logged_in_links );

			do_action( 'sidebar_login_widget_logged_in_content_end' );

		// Logged out user
		} else {

			$logged_out_title = $this->replace_tags( apply_filters( 'sidebar_login_widget_logged_out_title', $logged_out_title ) );

			if ( $logged_out_title )
				echo $before_title . $logged_out_title . $after_title;

			do_action( 'sidebar_login_widget_logged_out_content_start' );

			$redirect = empty( $instance['login_redirect_url'] ) ? $this->current_url( 'nologout' ) : $instance['login_redirect_url'];

			$login_form_args = apply_filters( 'sidebar_login_widget_form_args', array(
		        'echo' 				=> true,
		        'redirect' 			=> esc_url( apply_filters( 'sidebar_login_widget_login_redirect', $redirect ) ),
		        'label_username' 	=> __( 'Username', 'sidebar_login' ),
		        'label_password' 	=> __( 'Password', 'sidebar_login' ),
		        'label_remember' 	=> __( 'Remember Me', 'sidebar_login' ),
		        'label_log_in' 		=> __( 'Login &rarr;', 'sidebar_login' ),
		        'remember' 			=> true,
		        'value_remember' 	=> true
		    ) );

			wp_login_form( $login_form_args );

			$this->show_links( 'logged_out', $logged_out_links );

			do_action( 'sidebar_login_widget_logged_out_content_end' );
		}

		do_action( 'sidebar_login_widget_end' );

		echo $after_widget;
    }

	/**
	 * current_url function.
	 *
	 * @access public
	 * @param string $url (default: '')
	 * @return void
	 */
	private function current_url( $url = '' ) {
		$pageURL  = force_ssl_admin() ? 'https://' : 'http://';
		$pageURL .= esc_attr( $_SERVER['HTTP_HOST'] );
		$pageURL .= esc_attr( $_SERVER['REQUEST_URI'] );

		if ( $url != "nologout" ) {
			if ( ! strpos( $pageURL, '_login=' ) ) {
				$rand_string = md5( uniqid( rand(), true ) );
				$rand_string = substr( $rand_string, 0, 10 );
				$pageURL = add_query_arg( '_login', $rand_string, $pageURL );
			}
		}

		return esc_url_raw( $pageURL );
	}

	/**
	 * update function.
	 *
	 * @see WP_Widget->update
	 * @access public
	 * @param array $new_instance
	 * @param array $old_instance
	 * @return array
	 */
	function update( $new_instance, $old_instance ) {
		$this->define_options();

		foreach ( $this->options as $name => $option ) {
			if ( $option['type'] == 'break' )
				continue;

			$instance[ $name ] = strip_tags( stripslashes( $new_instance[ $name ] ) );
		}
		return $instance;
	}

	/**
	 * form function.
	 *
	 * @see WP_Widget->form
	 * @access public
	 * @param array $instance
	 * @return void
	 */
	function form( $instance ) {
		$this->define_options();

		foreach ( $this->options as $name => $option ) {

			if ( $option['type'] == 'break' ) {
				echo '<hr style="border: 1px solid #ddd; margin: 1em 0" />';
				continue;
			}

			if ( ! isset( $instance[ $name ] ) )
				$instance[ $name ] = $option['default'];

			if ( empty( $option['placeholder'] ) )
				$option['placeholder'] = '';

			echo '<p>';

			switch ( $option['type'] ) {
				case "text" :
					?>
					<label for="<?php echo esc_attr( $this->get_field_id( $name ) ); ?>"><?php echo wp_kses_post( $option['label'] ) ?>:</label>
					<input type="text" class="widefat" id="<?php echo esc_attr( $this->get_field_id( $name ) ); ?>" name="<?php echo esc_attr( $this->get_field_name( $name ) ); ?>" placeholder="<?php echo esc_attr( $option['placeholder'] ); ?>" value="<?php echo esc_attr( $instance[ $name ] ); ?>" />
					<?php
				break;
				case "checkbox" :
					?>
					<label for="<?php echo esc_attr( $this->get_field_id( $name ) ); ?>"><input type="checkbox" class="checkbox" id="<?php echo esc_attr( $this->get_field_id( $name ) ); ?>" name="<?php echo esc_attr( $this->get_field_name( $name ) ); ?>" <?php checked( $instance[ $name ], 1 ) ?> value="1" /> <?php echo wp_kses_post( $option['label'] ) ?></label>
					<?php
				break;
				case "textarea" :
					?>
					<label for="<?php echo esc_attr( $this->get_field_id( $name ) ); ?>"><?php echo wp_kses_post( $option['label'] ) ?>:</label>
					<textarea class="widefat" cols="20" rows="3" id="<?php echo esc_attr( $this->get_field_id( $name ) ); ?>" name="<?php echo esc_attr( $this->get_field_name( $name ) ); ?>" placeholder="<?php echo esc_attr( $option['placeholder'] ); ?>"><?php echo esc_textarea( $instance[ $name ] ); ?></textarea>
					<?php
				break;
			}

			if ( ! empty( $option['description'] ) )
				echo '<span class="description" style="display:block; padding-top:.25em">' . wp_kses_post( $option['description'] ) . '</span>';

			echo '</p>';
		}
	}
}

register_widget( 'Sidebar_Login_Widget' );