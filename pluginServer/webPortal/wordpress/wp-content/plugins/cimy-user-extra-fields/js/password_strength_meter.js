function check_pass_strength() {
	var pass1 = jQuery('[name=cimy_uef_wp_PASSWORD]').val(), pass2 = jQuery('[name=cimy_uef_wp_PASSWORD2]').val(), user, strength;
	
	if (jQuery('#user_login').length)
		user = jQuery('#user_login').val();
	else
		user = jQuery('#user_name').val();

	jQuery('#pass-strength-result').removeClass('short bad good strong');
	if ( ! pass1 ) {
		jQuery('#pass-strength-result').html( pwsL10n.empty );
		return;
	}

	strength = passwordStrength(pass1, user, pass2);

	switch ( strength ) {
		case 2:
			jQuery('#pass-strength-result').addClass('bad').html( pwsL10n['bad'] );
			break;
		case 3:
			jQuery('#pass-strength-result').addClass('good').html( pwsL10n['good'] );
			break;
		case 4:
			jQuery('#pass-strength-result').addClass('strong').html( pwsL10n['strong'] );
			break;
		case 5:
			jQuery('#pass-strength-result').addClass('short').html( pwsL10n['mismatch'] );
			break;
		default:
			jQuery('#pass-strength-result').addClass('short').html( pwsL10n['short'] );
	}
}

jQuery(document).ready( function() {
	// really we need this IF otherwise we break freaking form confirmation when password and password meter are both present
	if (jQuery('[name=cimy_uef_wp_PASSWORD]').attr('type') == 'password') {
		jQuery('[name=cimy_uef_wp_PASSWORD]').val('').keyup( check_pass_strength );
		jQuery('[name=cimy_uef_wp_PASSWORD2]').val('').keyup( check_pass_strength );
		check_pass_strength();
	}
});
