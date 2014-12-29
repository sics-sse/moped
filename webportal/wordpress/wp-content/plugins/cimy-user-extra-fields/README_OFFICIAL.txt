Cimy User Extra Fields

WordPress is becoming more than ever a tool to open blog/websites and CMSs in an easier way. Users are increasing day by day; one of the limits however is the restricted and predefined fields that are available in the registered users profile: it is difficult for example to ask for the acceptance of "Terms and Conditions of Use" or "Permission to use personal data".
It's also possible to set a password during registration combined to equalTO rule, only people who knows the password can register.

We have developed a WordPress plug-in to do this.

There are some plug-ins that do something similar, but this one wants to focus on giving the administrator the possibility to add all fields needed, on the rules that can be defined for each field, and in giving the possibility to both administration and the user to change the data inserted.

As for now the plug-in supports:
 * text
 * textarea
 * textarea-rich
 * password
 * checkbox
 * radio
 * dropdown
 * dropdown-multi
 * picture
 * picture-url
 * registration-date
 * avatar
 * file

future versions can have more.

Following WordPress hidden fields can be enabled during registration:
 * password
 * first name
 * last name
 * nickname
 * website
 * Aim
 * Yahoo IM
 * Jabber/Google Talk
 * biographical info

Other features:
 * reCAPTCHA
 * image upload with crop/resize functions
 * custom welcome email (non MS installations)
 * custom registration logo (non MS installations)
 * email confirmation (non MS installations)
 * form confirmation (non MS installations)
 * username equal to the email address (non MS installations)
 * much more!

The plug-in adds two new menu voices in the admin for the administrator and two for users.

Two new menus are:

WordPress and WordPress MultiSite per-blog registration:
    1. "Users -> Users Extended" - lets you show users lists with the new fields that are created
    2. "Settings -> Cimy User Extra Fields" - lets administrators add as many new fields as are needed to the users' profile, giving the possibility to set some interesting rules.

Wordpress MultiSite unique registration:
    1. "Network Admin -> Users Extended" - lets you show users lists with the new fields that are created
    2. "Network Admin -> Cimy User Extra Fields" - lets administrators add as many new fields as are needed to the users' profile, giving the possibility to set some interesting rules.

Rules are:

    * min/exact/max length admitted
	[only for text, textarea, textarea-rich, password, picture, picture-url, avatar, file]

    * field can be empty
	[only for text, textarea, textarea-rich, password, picture, picture-url, dropdown, dropdown-multi, avatar, file]

    * check for e-mail address syntax
	[only for text, textarea, textarea-rich, password]

    * field can be modified after the registration
	[only for text, textarea, textarea-rich, password, picture, picture-url, checkbox, radio, dropdown, dropdown-multi, avatar, file]
	[for radio and checkbox 'edit_only_if_empty' has no effects and 'edit_only_by_admin_or_if_empty' has the same effect as edit_only_by_admin]

    * field equal to some value (for example accept terms and conditions)
	[all except avatar by default set to 512]

      * equal to can be or not case sensitive
	[only for text, textarea, textarea-rich, password, dropdown, dropdown-multi]

Visualization rules
    * field can be hidden during registration
	[all except the email address]

    * field can be hidden in user's profile
	[all except the WordPress fields]

    * field can be hidden in Users Extended page
	[all]

    * field can be hidden in Search Engine (only if you installed the template)
	[all]

    * field can be hidden in Blog's public page (only if you installed the template)
	[all]

    * all visualization rules can be overridden if an user has certain rights (default=no override)
	[all]

New fields will be visible everywhere by default, a part some WordPress fields.


BEFORE writing to me read carefully ALL the documentation AND the FAQ. Missing this step means you are wasting my time!
Bugs or suggestions can be mailed at: cimmino.marco@gmail.com

REQUIREMENTS:
 * PHP >= 5.0.0
 * WORDPRESS >= 3.1.x
 * WORDPRESS MultiSite >= 3.1.x
 * MYSQL >= 4.1.2

INSTALLATION:
- WordPress: just copy whole cimy-user-extra-fields subdir into your plug-in directory and activate it

- WordPress MultiSite:
There are two supported ways of using this plug-in under WordPress MultiSite:

1) Unique registration
If you want that ALL Blogs on your MultiSite installation follow the same registration with the same fields this is the case for you.
Every blog will have shared registration page and only the Super Admin can change it.
 - unpack the package under 'wp-content/mu-plugins' directory (if this directory does not exist, create it);
 - be sure that cimy_user_extra_fields.php is outside Cimy folder (move it if necessary);
 - go to "Network Admin -> Cimy User Extra Fields", press "Fix the problem" button and confirm.

2) Per-Blog registration
If you want that every single Blog can define its own Extra Fields then you should choose this installation.
Every registration will have Extra Fields defined by single blogs, every user will have anyway WordPress fields shared with ALL Blogs, this how it works WordPress MultiSite.
 - unpack the package under 'wp-content/plugins' directory;
 - be sure that cimy_uef_mu_activation.php is installed under 'wp-content/mu-plugins' directory (if this directory does not exist, create it);
 - then every single blog will have it under "Plugins" section.


UPDATE FROM A PREVIOUS VERSION:
- visit Cimy User Extra Fields admin options, DB upgrade will be performed automatically


HOW TO ASSIGN A DEFAULT VALUE TO THE EXTRA FIELDS:
1. You can assign a default value using the VALUE textarea in the admin panel.
2. You can use URL (only for registration page), example:
http://www.exampleofmywebsite.it/wordpress29/wp-login.php?action=register&FIELD_01=test1&FIELD_02=test2

FIELD_01 and FIELD_02 are two existing fields that will get default assignment with string "test1" and "test2".
Note 1: Field name should be upper case otherwise won't be recognized.
Note 2: These two methods can be used together, but remember that URL has higher priority.


FUNCTIONS USEFUL FOR YOUR THEMES OR TEMPLATES:

[Function get_cimyFieldValue]
NOTE: password fields values will not be returned for security reasons

USAGE:
$value = get_cimyFieldValue($user_id, $field_name, [$field_value]);

In ALL cases if an error is occured or there are no matching results from the call then NULL is returned.


This function is all you need to retrieve extra fields values, but in order to retrieve all power from it you have to understand all different ways that can be used.


CASE 1:
get an extra field value from a specific user

PARAMETERS: pass user_id as first parameter and field_name as second
RETURNED VALUE: the function will return a string containing the value

GENERIC:
	$value = get_cimyFieldValue(<user_id>, <field_name>);
EXAMPLE:
	$value = get_cimyFieldValue(1, 'MY_FIELD');
	echo cimy_uef_sanitize_content($value);


CASE 2:
get all extra fields values from a specific user

PARAMETERS: pass user_id as first parameter and a boolean set to false as second
RETURNED VALUE: the function will return an associative array containing all extra fields values from that user, this array is ordered by field order

GENERIC:
	$values = get_cimyFieldValue(<user_id>, false);
EXAMPLE:
	$values = get_cimyFieldValue(1, false);

	foreach ($values as $value) {
		echo $value['NAME'];
		echo $value['LABEL'];
		echo cimy_uef_sanitize_content($value['VALUE']);
	}


CASE 3:
get value from a specific extra field and from all users

PARAMETERS: pass a boolean set to false as first parameter and field_name as second
RETURNED VALUE: the function will return an associative array containing the specific extra field value from all users, this array is ordered by user login

GENERIC:
	$values = get_cimyFieldValue(false, <field_name>);
EXAMPLE:
	$values = get_cimyFieldValue(false, 'MY_FIELD');

	foreach ($values as $value) {
		$user_id = $value['user_id'];
		echo $value['user_login'];
		echo cimy_uef_sanitize_content($value['VALUE']);
	}


CASE 4a:
get all users that have a specific value in a specific extra field

PARAMETERS: pass a boolean set to false as first parameter, field_name as second and field_value as third
RETURNED VALUE: the function will return an associative array containing all users that has that value in that specific extra field, this array is ordered first by user login

GENERIC:
	$values = get_cimyFieldValue(false, <field_name>, <field_value>);
EXAMPLE:
	$values = get_cimyFieldValue(false, 'COLOR', 'red');

	foreach ($values as $value) {
		$user_id = $value['user_id'];
		echo $value['user_login'];
	}


CASE 4b:
get all users that contains (also partially) a specific value in a specific extra field

PARAMETERS: pass a boolean set to false as first parameter, field_name as second and a special array as third
RETURNED VALUE: the function will return an associative array containing all users that contains (also partially) that value in that specific extra field, this array is ordered by user login

GENERIC:
	$values = get_cimyFieldValue(false, <field_name>, <array>);
EXAMPLE:
	$field_value = array();
	$field_value['value'] = ".com";
	$field_value['like'] = true;

	$values = get_cimyFieldValue(false, 'WEBSITE', $field_value);

	foreach ($values as $value) {
		$user_id = $value['user_id'];
		echo $value['user_login'];
	}


CASE 5:
get all users with all values

PARAMETERS: pass two boolean set to false as first and second parameter
RETURNED VALUE: the function will return an associative array containing all extra fields values for every user, this array is ordered first by user login and second by field order

GENERIC:
	$values = get_cimyFieldValue(false, false);
EXAMPLE:
	$values = get_cimyFieldValue(false, false);
	$old_name = "";

	foreach ($values as $value) {
		$user_id = $value['user_id'];
		$new_name = $value['user_login'];

		if ($old_name != $new_name)
			echo "<br /><br />".$new_name."<br /><br />";

		echo $value['LABEL'].": ";
		echo cimy_uef_sanitize_content($value['VALUE'])."<br />";

		$old_name = $new_name;
	}


ADDITIONAL EXAMPLES:
This is an example how to retrieve and display one uploaded image of the current user logged in.

$user = wp_get_current_user();

// is there someone logged?
if ($user->ID) {
	$value = cimy_uef_sanitize_content(get_cimyFieldValue($user->ID, 'IMAGE'));
	echo '<img src="'.$value.'" alt="description_here" />';
}


This is an entire example that can be used into your theme for example to retrieve the value from SITE extra field that of course was created.
If you put the example just inside an existing loop you shouldn't add it again, just use get_cimyFieldValue call and echo call.

if (have_posts()) {
	while (have_posts()) {
		the_post();

		$value = get_cimyFieldValue(get_the_author_ID(), 'SITE');

		if ($value != NULL)
			echo cimy_uef_sanitize_content($value);
	}
}


If you experience duplicate printing this is due to the loop and where/how it is used; to avoid this you can use this code that has a little workaround.
REMEMBER: you cannot use get_the_author_ID() outside the loop, this because WordPress doesn't permit this.

if (have_posts()) {
	$flag = true;

	while (have_posts()) {
		the_post();

		if ($flag) {
			$value = get_cimyFieldValue(get_the_author_ID(), 'SITE');

			if ($value != NULL)
				echo cimy_uef_sanitize_content($value);

			$flag = false;
		}
	}
}


PICTURE AND get_cimyFieldValue FUNCTION:

If you want to display the image in an HTML page just use IMG object like this:
<img src="<?php echo $image_url; ?>" alt="description_here" />


If you want to get the thumbnail url and you have only the image url you can use this function:
$thumb_url = cimy_get_thumb_path($image_url);


REGISTRATION-DATE AND get_cimyFieldValue FUNCTION:

Remember that the function returns the timestamp so to have the correct date printed you can use this function:
echo cimy_get_formatted_date($value);

or

echo cimy_get_formatted_date($value, $format);

where $format is the date and time format, more tags details here:
http://www.php.net/manual/en/function.strftime.php


[Function set_cimyFieldValue]
NOTE 1: the user should have permission to write the value: this mean if the function is used when no user is logged in will always fail.
NOTE 2: for checkbox fields use field_value=true/false to check/uncheck
NOTE 3: for radio fields use field_value equal to the label you want to select
NOTE 4: for dropdown fields use field_value equal to the item in the label you want to select
NOTE 5: for dropdown-multi fields use field_value equal to the items in the label you want to select separated by a comma: ','

USAGE:
$result = set_cimyFieldValue($user_id, $field_name, $field_value);

RETURNED VALUE:
An array is returned with all user ids where the change has been successful; empty array in case of error or the value is already in the DB.


CASE 1:
set an extra field value for a specific user

PARAMETERS: pass user_id as first parameter, field_name as second and field_value as third
RETURNED VALUE: the function will return an array containing USER_ID=1 if 'NEW_VALUE' has been written into 'MY_FIELD'

GENERIC:
	$result = set_cimyFieldValue(<user_id>, <field_name>, <field_value>);
EXAMPLE:
	$result = set_cimyFieldValue(1, 'MY_FIELD', 'NEW_VALUE');


CASE 2:
set an extra field value for all users

PARAMETERS: pass false as first parameter, field_name as second and field_value as third
RETURNED VALUE: the function will return an array containing all USER_IDs where 'NEW_VALUE' has been written into 'MY_FIELD'

GENERIC:
	$result = set_cimyFieldValue(false, <field_name>, <field_value>);
EXAMPLE:
	$result = set_cimyFieldValue(false, 'MY_FIELD', 'NEW_VALUE');


[Function cimy_uef_sanitize_content]
This function protects your blog from users trying to add JavaScript or alter your blog doing HTML injection in extra fields. It is very important that you do not remove that function.
This function filters only some html tags and let other be used, the list of tags that are allowed is present under /wp-includes/kses.php search for $allowedtags array definition.

It can accepts two parameters:
$content: the content to be protected against HTML injections
$override_allowed_tags [array|null, default null]: if you want to override allowed tags you should pass a proper array where all your favourite tags are listed.

USAGE:
echo cimy_uef_sanitize_content($content, [$override_allowed_tags]);

EXAMPLE:
global $allowedtags;

// copy the array to not modify the original one
$my_tags = $allowedtags;

// add img tag to allowed tags list
$my_tags["img"] = array(
	"src" => array(),
	"alt => array(),
);

// $content is what I want to show, see previous examples for more details
echo cimy_uef_sanitize_content($content, $my_tags);


[Function get_cimyFields]
This function returns an array containing all extra fields defined by the admin ordered by the order defined in the admin page, if there are no fields an empty array is returned.

It can accepts two parameters:
$wp_fields [true|false, default false]: if true will return hidden WordPress fields enabled
$order_by_section [true|false, default false]: if true array returned will be ordered as first key by fieldset and as second key by order; this parameter can be applied only if the first one is set to false.

USAGE:
$allFields = get_cimyFields([$wp_fields], [$order_by_section]);

EXAMPLE:
$allFields = get_cimyFields();

if (count($allFields) > 0) {
	foreach ($allFields as $field) {
		echo "ID: ".$field['ID']." \n";
		echo "F_ORDER: ".$field['F_ORDER']." \n";
		echo "NAME: ".cimy_uef_sanitize_content($field['NAME'])." \n";
		echo "TYPE: ".cimy_uef_sanitize_content($field['TYPE'])." \n";
		echo "VALUE: ".cimy_uef_sanitize_content($field['VALUE'])." \n";
		echo "LABEL: ".cimy_uef_sanitize_content($field['LABEL'])." \n";
		echo "DESCRIPTION: ".cimy_uef_sanitize_content($field['DESCRIPTION'])." \n";

		echo "RULES: ";
		print_r($field['RULES']);
		echo "\n\n";
	}
}

HOW TO CHANGE REGISTRATION DATE FORMAT:
You can change the format of the registration date putting your own format in equal to rule using tags from strftime php function:
http://www.php.net/manual/en/function.strftime.php

default used if not specified:
%d %B %Y @%H:%M

Month and weekday names and other language dependent strings respect the current locale set in your WordPress installation.


HOW TO USE PICTURE SUPPORT:
You have two possibilities for user picture support: "picture-url" and "picture"

[PICTURE-URL]
User will provide only a link and the image will be linked from that site, it will NOT be copied into the server

[PICTURE]
User will upload the image that is stored into his/her computer and the image will be copied into the server

First of all you need a directory where all pictures will be stored, the directory MUST HAVE this name: 'Cimy_User_Extra_Fields' and MUST BE placed under: "wp-content" dir
example: /wp-content/Cimy_User_Extra_Fields directory and give it 777 permissions if you are under Linux (or 770 and group to "www-data" if you are under Ubuntu Linux).

HOW TO USE AVATAR SUPPORT:
Create the same directory needed for PICTURE support, avatars will be stored in a subdirectory and will not interfere with other pictures uploaded by the same plug-in


ADVANCED OPTIONS:
Advanced options have been introduced in v2.1.0 to accommodate some extra options per field.
Multiple options should be comma separated ','

[AVATAR, PICTURE]
no-thumb=1 - do not crate the thumbnail (if equalTO rule is set it will resize the original image)
crop_ratio=4:3 - fix cropping ratio
crop_x1=0,crop_y1=0,crop_x2=80,crop_y2=90 - pre-select cropping window

[AVATAR, PICTURE, FILE]
filename=default.pdf - rename the uploaded file to the given file name


HOW TO USE WPML SUPPORT:
Since v2.4.0 field's label and description can be translated using the WordPress Multilingual plug-in.
To use it in your code you can get the get_cimyFields example above and change only the following lines:
    echo "LABEL: ".cimy_uef_sanitize_content(cimy_wpml_translate_string($field['NAME']."_label", $field['LABEL']))." \n";
    echo "DESCRIPTION: ".cimy_uef_sanitize_content(cimy_wpml_translate_string($field['NAME']."_desc", $field['DESCRIPTION']))." \n";


KNOWN ISSUES:
- if you add too many fields in the "Users Extended" menu they will go out of frame
- some rules are never applied if you are using PHP 4.x please update to PHP 5.x as stated in the REQUIREMENTS
- registration date cannot be modified
- picture and avatar upload is disabled during registration under WordPress MultiSite, will be possible once user is activated
- if you change order or remove fieldsets you may need to set all extra fields' fieldset assigment again
- dropdown issues:
  - custom value is not supported
  - comma is not allowed as it is the delimiter
- registration confirmation does not work if you use Theme My Login plug-in


FAQ:
Q: When will be supported mandatory accept of terms and conditions field?

A: Has been always there: is as easy as adding a checkbox and setting "equal to" rule to YES.


Q: What the Database options do exactly?

A: Basically you can:
- empty or drop extra fields or extra fields data (inserted by users)
- reset or drop plug-in's options


Q: Yes but will this affect other data? It says "WordPress Fields table", this is scary.

A: No, it will affect _only_ the data produced by the plug-in. Really.
Also keep in mind that if you do NOT have to restore default values or uninstall the plug-in, then you do not need to touch them.


Q: Cimy User Extra Fields is not compatible with "Themed Login", how can I do?

A: The reality is this plug-in IS compatible with WordPress 2.1 or greater and "Themed Login" NOT, so it's NOT a Cimy User Extra Field's bug! However I have tried with a little success a workaround to make it works, but first please understand that this is totally untested and unsupported hack, if you want a better one ask the author of that plug-in to support new WordPress!
If you still want *my* personal and unsupported hack edit the plug-in "Themed Login" and do these 3 modifications:

1) at line 773, after "global $wpdb, $wp_query;" add this:
global $errors;

2) at line 811, before "if ( 0 == count($errors) ) {" add this:
do_action('register_post');

3) at line 871, before "A password will be emailed to you." add this:
<?php do_action('register_form'); ?>


Q1: I got "Fatal error: Allowed memory size of 8388608 bytes exhausted [..]", why?
Q2: I got blank pages after activating this plug-in, why?

A1: Because your memory limit is too low, to fix it edit your php.ini and search memory_limit key and put at least to 12M
A2: If you do not have access to your php.ini you can try this workaround (might not work)
http://codex.wordpress.org/Editing_wp-config.php#Increasing_memory_allocated_to_PHP


Q: Your plug-in is great, but when you will add support to add more than one choice in radio and dropdown fields?

A: This feature is here since ages, for radio field just use the same name, for dropdown field read instructions in the add field area (in the plug-in).


Q: Uploaded images are not resized, why?

A: You should add php-gd module (under Ubuntu install php5-gd package).


Q: Why big files are not being uploaded?

A: Please check this website, tells you how to change your PHP configurations to fix this issue: http://www.radinks.com/upload/config.php


Q: Why admin user cannot see all fields even if I set to do it?

A: Probably you installed the first time WordPress on PHP4 and you experienced this bug: http://core.trac.wordpress.org/ticket/8317
To fix the problem you need to create another administrator user and change admin user to another role and then back to administrator.


Q1: I'm using your plug-in on WordPress MultiSite per-blog installation and when I register one user all Extra Fields are ignored, why?
Q2: I get this error: 'Fatal error: Call to undefined function cimy_uef_mu_blog_exists()', why?

A: Because you missed to move cimy_uef_mu_activation.php file please check carefully the installation steps.


Q: I'm using your plug-in on WordPress MultiSite per-blog installation, I'm registering users on one blog but they appear on the main blog too, why?

A: Because WordPress MS is designed like that and I can't do anything about, however all extra fields and relative data are saved per-blog.
Since I had already a long discussion with an user that didn't believe this, don't bother me to insist on this topic until you prove I'm wrong.


Q: I’m trying to use a regular expression in the rules, but the check does not work as expected, why?

A: Usually means your regex is wrong, please study how to properly build it: http://php.net/manual/en/function.preg-match.php


Q1: I do not see Extra Fields under the page user-new.php can you add there too?
Q2: How can I import new users with Extra Fields data into?

A: Unluckily due to a WordPress limitation I can’t add Extra Fields into user-new.php but you can quickly add all the users you want using my plugin:
Cimy User Manager – http://www.marcocimmino.net/cimy-wordpress-plugins/cimy-user-manager/


Q: I cannot edit neither delete some Extra Fields, usually after the 10th one, why?

A: Your PHP server is probably limiting number of $_POST elements, Suhosin for example does it. Please allow at least 500 or more vars.
http://www.hardened-php.net/suhosin/configuration.html#suhosin.post.max_vars


Q: I am trying to change the fields' order, but whatever I try it doesn't work, why?

A: First of all you need to select at least 2 fields if you want to change the order.
Secondly if you change for example field n.1 to position n.3 be sure that field n.3 goes into another position and finally that a field goes into position n.1
In short: every field should have a new position or at least the same one, but no positions can be skipped or be present multiple times.


Q: When feature XYZ will be added?

A: I don't know, remember that this is a 100% free project so answer is "When I have time and/or when someone help me with a donation".


Q: Can I help with a donation?

A: Sure, visit the donation page or contact me via e-mail.


Q: Can I hack this plug-in and hope to see my code in the next release?

A: For sure, this is just happened and can happen again if you write useful new features and good code. Try to see how I maintain the code and try to do the same (or even better of course), I have rules on how I write it, don't want "spaghetti code", I'm Italian and I want spaghetti only on my plate.
There is no guarantee that your patch will hit an official upcoming release of the plug-in, but feel free to do a fork of this project and distribute it, this is GPL!


Q1: I have found a bug what can I do?
Q2: Something does not work as expected, why?

A: The first thing is to download the latest version of the plug-in and see if you still have the same issue.
If yes please write me an email or write a comment but give as more details as you can, like:
- Plug-in version
- WordPress version
- MYSQL version
- PHP version
- exact error that is returned (if any)

after describe what you did, what you expected and what instead the plug-in did :)
Then the MOST important thing is: DO NOT DISAPPEAR!
A lot of times I cannot reproduce the problem and I need more details, so if you don't check my answer then 80% of the times bug (if any) will NOT BE FIXED!


CHANGELOG:
v2.5.3 - 24/06/2013
- Fixed updating extra fields from a different blog doesn't work (MS per-blog only) (thanks to GilesFarrow)
- Fixed Securimage captcha sometimes does not validate correctly codes (thanks to websitesareus)
- Fixed two PHP warnings produced in the plug-in's admin panel (thanks to Ov3rfly)
- Fixed tabindex is no longer needed for WordPress 3.5 and Theme My Login users with Securimage captcha too

v2.5.2 - 03/06/2013
- Added support for Theme My Login v6.3.x
- Fixed Users Extended page is blank when the WordPress installation hosts a lot (10.000+) of users (thanks to mightypixel, eArtboard and more)
- Fixed cimy_uef_register.css file inclusion does not happen (MS only) (introduced in v2.5.0)
- Fixed strlen doesn't count correctly special accented characters, changed to mb_strlen (thanks to Batischev Oleg for the patch)
- Fixed user activation email's subject doesn't get translated (non-MS only) (thanks to Torstein Knutsen for the patch)
- Updated Italian translation

v2.5.1 - 06/05/2013
- Updated Securimage Captcha to v3.5.0
- Fixed captcha check was performed on /wp-admin/user-new.php page even without a captcha showed (MS only) (thanks to KZeni)
- Fixed strength password hint description is showed inline with the password strength when reCAPTCHA is also showed (thanks to coopersita)

v2.5.0 - 18/03/2013
- Added support for hiding the username under standard WP registration, email will substitute it (non-MS only) (thanks to Matt Hoffman)
- Added support for WordPress hidden fields rules under profile update

v2.4.2 - 18/02/2013
- Fixed missing 'cimy_update_ExtraFields' PHP warning (introduced in v2.4.1) (thanks to Ashton Clark and vtxyzzy)
- Fixed WordPress MS per-blog installation was not working for non main sites since WordPress MS 3.5 (thanks to Denis Lam)
- Fixed WordPress MS per-blog installation was not deleting cimy tables when blog is deleted

v2.4.1 - 11/02/2013
- Added support for welcome email, extra fields title and fieldset titles under the WordPress Multilingual plug-in (thanks to Piet for testing it)
- Added Arabic translation (Mamoun Elkheir)
- Fixed tabindex is no longer needed for WordPress 3.5 and Theme My Login users
- Fixed PHP files inclusion, do not include the admin's files if not in the admin area
- Fixed cursor doesn't appear at the end of the text edit under 'Users Extended'

v2.4.0 - 27/12/2012
- Added support for the WordPress Multilingual plug-in.
- Fixed plain text password was staying in the DataBase for registered users (WordPress MS and WordPress + confirmation email only)
- Fixed (better) background logo was stretched under Safari (thanks to DarioDN)
- Fixed PHP warnings wpdb::supports_collation usage on WordPress 3.5
- Fixed PHP warnings wpdb::prepare usage on WordPress 3.5
- Fixed PHP warnings on 'Users Extended' page on WordPress 3.5

v2.3.11 - 29/10/2012
- Fixed reCAPTCHA was not working on secure webservers (thanks to invisnet for the patch)
- Fixed extra lines outputted by php files (thanks to Ov3rfly)
- Fixed one PHP warning on options save (thanks to thomask)
- Fixed background logo was stretched under Safari (thanks to DarioDN)
- Updated Securimage Captcha to v3.0.1

v2.3.10 - 24/09/2012
- Fixed email is not sent to the user once confirmed its email address (non-MS only) (introduced in v2.3.9) (thanks to nerik73 and all people that reported this)
- Fixed 'A password will be e-mailed to you.' will be hidden when password field is not hidden (non-MS only) (thanks to lcool for the idea)
- Fixed labels for dropdown and dropdown-multi were not correctly showed in the welcome email

v2.3.9 - 06/08/2012
- Fixed another possible security issue where webservers with poor configuration might end up executing arbitrary PHP code when a malicious [file|avatar|picture] is uploaded (thanks to Artyom Skrobov from Secunia)
- Fixed dropdown-multi were not saved correctly under 'Users Extended' (thanks to David Vranish)
- Fixed dropdown-multi were not saved correctly under WordPress MS registration (thanks to David Vranish)

v2.3.8 - 30/07/2012
- Fixed security issue where any site with [file|avatar|picture] extra fields is vulnerable by a possible remote code execution vulnerability present in all versions of the plug-in probably since v0.9.5
  see: secunia.com/advisories/49975/ ('thanks' to the kid 'Crim3R' that in the need of popularity thought that exposing thousands of users was a better idea rather than responsibly email me first)
- Fixed image extensions are now restricted to what WordPress allows
- Fixed plug-in PHP error for people that have 'plugins' directory with a different name/location (thanks to anmari)

v2.3.7 - 05/03/2012
- Fixed image/file/avatar upload on profile edit when Theme My Login - Themed profile is in use (introduced with v2.3.0) (thanks to Giovanni Gonzalez)
- Fixed fields were showed anyways in the form confirmation even if they were not showed in the registration
- Fixed textarea-rich didn't work sometimes in the registration form
- Fixed registration was not possible if both password field and password meter are present and form confirmation is turned on (introduced with v2.3.3) (thanks to Jörg Thanheiser)

v2.3.6 - 03/02/2012
- Fixed rules are not applied if form confirmation is turned on (introduced with v2.3.1) (thanks to Pietro Gabba)

v2.3.5 - 30/01/2012
- Added (required) for extra fields with unchecked 'Can be empty' rule (thanks to Paul 'Sparrow Hawk' Biron for the patch)
- Changed fields' description is now consistent with WordPress look (thanks to Paul 'Sparrow Hawk' Biron for the patch)
- Allow 'target' attribute for links added in the extra fields' description
- Fixed use of a deprecated function on plug-in activation, fixes relative warning (thanks to David Anderson)
- Added Ukrainian translation (Oleg Bondarenko)

v2.3.4 - 02/01/2012
- Fixed profiles cannot be updated anymore when there are some rules set (introduced with v2.3.3) (thanks to Csaba)
- Fixed PHP warnings
- Code cleanup

v2.3.3 - 29/12/2011
- Fixed image/file/avatar upload when Theme My Login is in use (introduced with v2.3.0) (thanks to Brandon Krakowsky)
- Fixed password meter and textarea-rich were not working after submitting a registration with some errors when form confirmation is turned on (introduced with v2.3.0)
- Fixed a lot of PHP warnings
- Code cleanup
- Added Belarusian translation (Alexander Ovsov)

v2.3.2 - 14/12/2011
- Fixed image upload was no more possible in some cases under WordPress MS (introduced with v2.3.0) (thanks to Alexander Temper)
- Fixed a rare case of site crash under WordPress MS registration (thanks to Dan)

v2.3.1 - 06/12/2011
- Fixed profiles cannot be updated anymore when captcha is selected (introduced with v2.3.0) (thanks to Miguel Morera and Takanudo)
- Fixed captcha error messages are not displayed under WordPress MS

v2.3.0 - 28/11/2011
- Fixed security issue where reCAPTCHA and Securimage Captcha could be by-passed (thanks to corij)
- Fixed tinyMCE was not working anymore since WP 3.3
- Fixed all JavaScripts inclusion to be as requested by WordPress APIs
- Fixed file, picture and avatar extra fields were not styled on registration page
- Code cleanup

v2.2.0 - 04/09/2011
- Added textarea and textarea-rich class in the profile area (thanks to Evaluator)
- Added case sensitive check for regex equalTo rules (thanks to Juliette for the patch)
- Fixed WordPress MS unique installation was broken since WP 3.1 (thanks to RaSo0l)
- Fixed CSS file inclusion, should fix RTL admin layout mess (thanks to Moti Nisim)
- Fixed extra slash present into image uris (thanks to zyrq)
- Fixed dropdown selection when illegal characters are present (introduced with v2.0.5) (thanks to Jared)
- Fixed some URLs still were not caring about https when used
- Fixed captcha shouldn't show on form confirmation only
- Fixed textarea-rich pictures had relative instead of absolute urls (thanks to David Alexander)
- Fixed dropdown-multi were not saved when form confirmation is turned on
- Fixed images shouldn't be shows if not present on the disk (form confirmation only)
- Fixed plug-in description
- Code cleanup

v2.1.1 - 11/07/2011
- Fixed compatibility with Theme My Login plug-in (introduced with v2.1.0) (thanks to Michele and Mark)
- Fixed compatibility with WP-reCAPTCHA plug-in (thanks to des for the patch)

v2.1.0 - 28/06/2011
- Added confirmation registration (non MS only) (thanks to Marcello Foglia for sponsoring it)
- Added custom welcome email (non MS only) (thanks to Marcello Foglia for sponsoring it)
- Added crop functionalities for picture and avatar fields (thanks to Marcello Foglia for sponsoring it)
- Added advanced options (thanks to Marcello Foglia for sponsoring it)
- Code cleanup

v2.0.5 - 11/05/2011
- Added 'view_cimy_extra_fields' capability and rule to Show field if user has this cap (thanks to Matt)
- A&U Extended is now renamed to Users Extended
- Updated Users Extended page to use newer functions and to show # of search results
- Updated equalTo field can now accommodate up to 500 characters string
- Fixed several html bugs in Users Extended page
- Fixed several (but minor) security issues

v2.0.4 - 12/04/2011
- Fixed password strength meter was not translated (thanks to Jonas for the patch)
- Fixed tinyMCE was not working anymore since WP 3.1 (thanks to Jeremiah Tolbert)
- Fixed tinyMCE had huge buttons if a different registration logo was set
- Fixed "Can be modified only by admin or if empty" rule regressed in v2.0.3
- Updated Brazilian Portuguese translation (Diana)

v2.0.3 - 15/03/2011
- Fixed A&U Extended page permissions to "list_users" instead of "edit_users" (thanks to Matt)
- Fixed pre value for checkbox, radio, dropdown and dropdown-multi fields (thanks to Matt)
- Fixed fields can be modified in some ways even if set to not to (thanks to Matt)
- Fixed rules were applied anyway when adding new user from user-new.php (introduced with a change in WP 3.1) (thanks to Matt)
- Fixed Fatal error: Cannot redeclare wp_load_image() on MS installations (thanks to jcraig)

v2.0.2 - 05/02/2011
- Fixed for some WordPress MS unique installations DB table definitions were wrongly set (thanks to Yuri)
- Fixed registration date now works for all registered users, regardless when the field has been added
- Fixed dropdown-multi can't deselect all values
- Fixed registration emails are now sent with Blog's name as sender
- Fixed Extra Fields were shown to "anonymous" users even when setting minimum level to "subscriber" (thanks to Bruno PS)
- Fixed Extra Fields were not included in the welcome email when email confirmation was enabled (non MS only) (thanks to Michael T. Lee)
- Code cleanup
- Updated French translation (Bruno PS)

v2.0.1 - 19/11/2010
- Added possibility to change individual values "on the fly" into the Extra Fields from A&U Extended page (thanks to Cuántica Webs for sponsoring it)
- Added Extra Fields group filtering in the A&U Extended page
- Fixed various security issues (thanks to Mark Jaquith)
- Fixed all URLs were not caring about https when used (thanks to Álvaro Degives-Más)
- Fixed Extra Fields were not included in the admin email even if specified (MS only) (thanks to Kris LaGreca)

v2.0.0 - 13/10/2010
- Added possibility to mass-write new data into the Extra Fields from A&U Extended page (thanks to Cuántica Webs for sponsoring it)
- Fixed a debug information leftover introduced in 2.0.0-beta2 when saving a profile (thanks to Erik)
- Fixed some obsolete/wrong strings (thanks Mary & Patrick)
- Fixed some strings from the email confirmation were not translated correctly (non MS only)

v2.0.0 beta2 - 22/09/2010
- Added Securimage Captcha support (thanks to Patrick McCain for sponsoring it)
- Added redirection support (non MS only) (thanks to Patrick McCain for sponsoring it)
- Fixed adding/updating a field is now more user-friendly
- Fixed uploading of any file should be forbidden during the registration if the confirmation email is turned on
- Fixed an user can't register again after admin deletion within 2 days if the email confirmation is turned on (non MS only)
- Fixed show permissions were not saved properly (causing checkboxes can't be unchecked anymore since v2.0.0-beta1) (thanks to Bak)

v2.0.0 beta1 - 02/09/2010
- Added WordPress 3.0.x support
  - Fixed MultiSite recognization
- Added reCAPTCHA support (thanks to Patrick McCain for sponsoring it)
- Added password strength meter support (thanks to Patrick McCain for sponsoring it)
- Added password confirmation support (thanks to Patrick McCain for sponsoring it)
- Added email confirmation support (non MS only) (thanks to Patrick McCain for sponsoring it)
- Added extra fields to welcome new user email support (thanks to JeffPBlues Design for sponsoring it)
- Added custom login/registration logo support (non MS only) (thanks to Patrick McCain for sponsoring it)
- Fixed some errors were not translated under registration form
- Fixed rules shouldn't be applied if the user can't edit the field due to permissions (thanks to Paolo Sivori for pointing it)
- Fixed localization was not working for MultiSite unique registration
- Fixed extra fields search in the A&U Extended Page using Internet Explorer (thanks to Tom Matteson)
- Fixed personalized password was not sent in the new user email (non MS only)
- Fixed default password warning should not appear in case custom password has been entered (non MS only)
- Fixed directory/file permissions (thanks to Jim)
- Updated Italian translation
- Code cleanup
- Readme file updated

v1.5.3 - 09/04/2010
- Added set_cimyFieldValue function (thanks to Knark Planta for sponsoring)
- Fixed some PHP warnings
- Readme file updated

v1.5.2 - 03/03/2010
- Fixed profile page was not updating when setting 'Could not be empty' rule for picture, avatar or file fields (thanks to Erum Munir)
- Fixed WordPress MU per-blog installation and wp-content in a custom location was giving PHP error after installing cimy_uef_mu_activation.php
- Fixed WordPress MU per-blog installation can't edit posts anymore after installing cimy_uef_mu_activation.php (thanks to Ekaterina Kurasheva)

v1.5.1 - 10/02/2010
- Fixed PHP error when using dirty user_id in the profile's URL
- Worked around a bug introduced by WordPress 2.9.x when admin is editing another's user profile sometimes get wrong data into Extra Fields (thanks to Serge Meier)
- Added Polish translation (PiK)

v1.5.0 - 30/01/2010
- Added registration fields pre-filed within URL support (thanks to Charlie Markwick for sponsoring)
- Fixed WordPress MU unique registration mode was completely broken (introduced with v1.5.0 beta2) (thanks to Nicolene Heunis)
- Fixed WordPress MU per-blog registration was not working if main site had the plug-in disabled
- Fixed WordPress MU per-blog A&U Extended was not working
- Readme file updated

v1.5.0 beta2 - 22/11/2009
- Added roles support to Extra Fields (thanx to Jakob Wallsten for sponsoring)
- Added public search support (thanx to Jakob Wallsten and Nacho Arribas for sponsoring)
- Added public profile support (thanx to Jakob Wallsten and Nacho Arribas for sponsoring)
- Added WordPress MU per-blog support (thanx to Uwe Moosheimer for sponsoring)
- Fixed picture fields visualization in profile (introduced with v1.5.0 beta1)
- Fixed text fields visualization in profile
- Fixed admin page was completely screwed for some translations like Swedish (thanks to Erik Billerby for pointing it)
- Fixed some fields were ignored when adding new user from user-new.php in some circumstances (thanks to Erik Billerby for pointing it)
- Fixed rules were applied anyway when adding new user from user-new.php (introduced with v1.5.0 beta1)
- Fixed WordPress fields' rules were applied anyway when updating profile (introduced with v1.5.0 beta1)
- Fixed rules were applied in some circumstances when updating profile even if the field was hidden (introduced with v1.5.0 beta1)
- Fixed get_cimyFieldValue function was too slow when retrieving data using user_id and field_id (thanks to Erik Billerby for pointing it)
- Fixed A&U Extended search was not properly working when dropdown or dropdown-multi fields were present
- Fixed get_cimyFieldValue was returning an uncleaned label for dropdown-multi fields
- Fixed Biographical Info sometimes were disappearing for no apparent reason (thanks to Edward)
- Updated Swedish translation (Erik)
- Updated Italian translation

v1.5.0 beta1 - 16/08/2009
- Added WordPress 2.8.x support
- Added dropdown-multi support (thanks to Natural Building Network)
- Added file upload support (thanks to Karl Sandoval)
- Added rules check also during profile update (WP >= 2.8.x only)
- Added possibility to see up to 5000 users per page on A&U Extended page
- Added Spanish translation (David Gil)
- Changed max length for: label, description and value up to 50000 chars
- Fixed avatar and picture upload were not working for some languages like French (thanks to Miss K)
- Fixed Extra Fields were displayed after other plugins in the registration page (thanks to Nicolene Heunis)
- Fixed to not display 'picture' if the file does not exists in A&U Extended page
- Fixed an untranslatable word in A&U Extended page
- Readme file updated
- Updated Italian translation

v1.4.0 - 18/03/2009
- Added user_id in the array returned by get_cimyFieldValue function
- Added regular expression to equalTo rule for text, textarea, textarea-rich, password, dropdown (thanks to Shane Hartman for the patch)
- Fixed (again) textarea-rich under user registration page, hopefully now it works for everyone (thanx to Romain Bordessoul)
- Fixed some error messages weren't displayed under WordPress MU registration page (thanx to Nicolene Heunis for the patch)
- Readme file updated

v1.4.0 release candidate 1 - 24/02/2009
- Added picture/avatar directory check under options
- Added second parameter to cimy_uef_sanitize_content to let override allowed tags
- Fixed translation under WordPress MU (workaround)
- Fixed wrong password shown on WordPress MU activation page when using password personalization (thanx to Leo Kimble and Andrew Billits)
- Fixed dropdown not working if new lines were added to the list
- Renamed plug-in directory due to WordPress Plugin Directory rules
- Readme file updated

v1.4.0 beta3 - 08/02/2009
- Added possibility to set a custom fieldset per each extra fields
- Added fieldset to registration page
- Fixed data not saved into extra fields for some MYSQL configurations (thanx to Daniel Quinn)
- Fixed PHP error on comments when using avatar extra field (thanx to Serge Hardmeier)
- Fixed picture/avatar path were written even if upload failed
- Fixed password always overwritten in the WordPress MU welcome email
- Moved and renamed A&U Extended page under WordPress MU
- Disabled picture and avatar fields under WordPress MU registration page
- Removed deprecated "items per fieldset" option
- Readme file updated
- Updated Italian translation
- Updated German translation (Franz Josef)
- Code cleanup

v1.4.0 beta2 - 21/01/2009
- WordPress MU fixes:
  - Fixed Extra Fields not saved under registration
  - Fixed WordPress hidden fields not saved under registration
  - Fixed PHP error when at least one WordPress hidden field is present under registration
  - Fixed endless registration if there is at least one error due to rules in Extra Fields
  - Fixed notification email is filtered in case password is chosen by the user

v1.4.0 beta1 - 19/01/2009
- Added WordPress MU 2.5.x & 2.6.x support
- Added a button to fix missing/pending update of tables/options
- Fixed PHP error when deleting an user on certain installations (thanx to jarred)
- Fixed PHP error when options are not present
- Fixed Extra Fields filter (introduced with v1.3.0 beta1)
- Readme file updated

v1.3.2 - 11/01/2009
- Added possibility to change/remove Extra Fields section title under user profile
- Fixed bug where options were not correctly migrated for certain versions (introduced with v1.3.0 beta2)
- Fixed bug where A&U Extended page where showing 0 users due to previous bug (thanx to Alessandra)
- Fixed avatar support for installations with different table prefix (thanx to Sergey for pointing it)
- Fixed Table Creation option to not perform anything else when selected
- Fixed unitialized variable under options code
- Fixed last use of "level_10" role check (missed in the previous fix)
- Updated Italian translation
- Readme file updated and renamed to README_OFFICIAL.txt due to WordPress Plugin Directory rules

v1.3.1 - 02/01/2009
- Added WordPress Biographical Info field support
- Fixed Wordpress hidden fields' labels not translated
- Fixed WordPress password field' description not translated
- Fixed subdir for uploading pictures/avatars since there is no need to have it changing
- General cosmetic fixes
- Readme file updated

v1.3.0 "Happy New Year" - 01/01/2009
- XHTML 1.0 Transitional compliant bug fixes:
  - Fixed JS inclusion for picture and avatar uploads under profile page (introduced with v1.1.0 beta2)
  - Fixed missing 'cols' and 'rows' attributes for textarea-rich (introduced with v1.2.0 beta1)
  - Fixed CSS inclusion under registration page with textarea-rich (introduced with v1.2.0 beta1)
  - Fixed some elements' IDs defined twice under A&U Extended page (introduced with v1.3.0 beta1)
  - Fixed maxlength attribute wrongly added to avatars (introduced with v1.3.0 beta2)
  - Fixed & character in paging links under A&U Extended page (introduced with v1.3.0 beta2)
  - Fixed Apply button in wrong place under A&U Extended page (introduced with v1.3.0 beta2)
- Fixed PHP error when deleting users that have picture/avatar uploaded (introduced with v0.9.5!)
- Fixed (a bit better) textarea-rich under user registration page (introduced with v1.3.0 beta1)
- Fixed PHP error with avatars and WordPress 2.5 and 2.6 (introduced with v1.3.0 beta2)
- Fixed PHP error when uploading an avatar before a picture (introduced with v1.3.0 beta2)
- Fixed picture upload wrongly reaches avatar/ subdir (introduced with v1.3.0 beta2)
- Fixed paging option not created for new installations
- Fixed picture extensions check not always performed
- Fixed max length rule didn't recognize avatars as files to be uploaded
- Fixed max length rule wrongly shown to 20000 in certain cases
- Changed JS/CSS inclusions: now WP standard APIs are used
- Relaxed a bit security fix introduced in v1.2.0 now some html tags are allowed, script execution is anyway forbidden
- Moved uploadPic() JS function to a stand-alone file
- Again DB migration code cleanup, yes less is better :)
- Dropped use of "level_10" role check since is deprecated
- Dropped useless get_cimyFieldValue's option to turn it off, there are no security threats with it
- Readme file updated

v1.3.0 beta2 - 22/12/2008
- Added avatar support
- Added WordPress registration password support
- Added paging for A&U Extended page (now up to 500 users per page)
- Fixed picture upload under user profile with WordPress 2.7 (thanx to Shark)
- Fixed PHP error when uploading an image during registration and equal_to rule is set
- Fixed some untranslatable strings for picture and picture-url fields
- General cosmetic fixes
- Finished to cleanup DB migration code
- Updated Italian translation
- Readme file updated

v1.3.0 beta1 - 16/12/2008
- Changed plug-in link, to celebrate 2 years birthday we have a new home!
- Added WordPress 2.7.x support
  - Fixed PHP error under A&U Extended page
  - Fixed textarea-rich under user registration page
  - Fixed textarea-rich buttons labels
  - Fixed all input fields to match new registration page style
  - General cosmetic fixes
- Added Bulgarian translation (Boyan)
- Added avatars to A&U Extended page
- Fixed "Invert selection" button feature when 'wp-content' is not in the default location
- Removed some old DB migration code (migration supported now only starting from v0.9.1)

v1.2.0 - 27/09/2008
- Fixed a security bug that affects A&U Extended page: text, textarea, password and picture-url fields were not properly sanitized causing arbitrary JavaScript code execution (if enabled) inserted by any registered user when administrator access that page
  The bug is confirmed from v1.0.x to v1.2.0-beta1 older versions may be affected as well (thanx to Joseph Engo for pointing it)
  * UPDATE IS STRONGLY RECOMMENDED *
- Fixed checkbox fields visualization in the registration
- Fixed picture fields visualization in the registration

v1.2.0 beta1 - 11/09/2008
- Added WordPress 2.6.x support
- Added textarea-rich support with tinyMCE! (thanx to Shekhar K. for sponsoring)
- Added extra fields search in A&U Extended page
- Fixed a bad bug where in some circumstances current user extra fields data can be overwritten
- Fixed radio fields visualization in profile
- Code cleanup

v1.1.1 - 15/05/2008
- Added Swedish translation (Peter)
- Fixed problems with special characters (may need resave the content)
- Fixed two untranslated strings in the options (thanx to Peter)

v1.1.0 - 07/05/2008
- Fixed thumbnails were broken with WordPress 2.5.x (thanx to Rita)
- Fixed thumbnails were broken when an image have an upper-case extension (due to a WordPress issue)
- Updated German translation (Rita)

v1.1.0 release candidate 1 - 28/04/2008
- Fixed a regression with WordPress 2.5.x user's without admin privileges cannot edit extra fields at all
- Fixed pages in A&U Extended page pointed to non Extended page
- Fixed some hidden text in "Add field" area for certain configurations (thanx to Rik)

v1.1.0 beta2 - 05/04/2008
- Changed theme for: user's Profile, A&U Extended page, Options and Fields management
- Code cleanup
- Updated Italian translation
- Readme file updated

v1.1.0 beta1 - 31/03/2008
- Added initial support to WordPress 2.5
- Added custom css for registration fields

v1.0.2 - 24/03/2008
- Added Russian translation (mikolka)
- Added Danish translation (Rune)
- Fixed a bad bug that in some cases checkbox fields were saved wrongly as checked (thanx to Dana Rockel for the patch)
- Fixed picture file attributes for broken server (thanx to Chris Adams for the partial patch)
- Fixed picture url when WordPress URL and Blog URL are different (thanx to Neil Stead for the patch)
- Fixed picture upload with Internet Explorer, due to a bug in it probably :( (thanx to Nicola aka ala_747 for the partial patch)
- Fixed picture upload with some localized WordPress (like French) (thanx to buzz)
- Removed an obsolete part in the Readme file (thanx to Mark)

v1.0.1 - 22/11/2007
- Added better directory creation handling for images uploader
- Added French translation (Sev)
- Updated Brazilian Portuguese translation (Sher)
- Moved invert selection javascript to a stand-alone file so admin page is XHTML 1.0 Transitional compliant again
- Fixed a rare image upload failure during registration, can happen if at least one WordPress hidden field is present
- Fixed warning pop-up for image extension, shown wrongly in certain cases

v1.0.0 - 16/10/2007
- Added hidden WordPress fields support (First name, Last name, Nickname, Website, AIM, Yahoo IM and Jabber / Google Talk)
- Added initial WordPress MU compatibility! (Thanx to Martin Cleaver and Beau Lebens for explaining me how MU works)
  - I need more hours of work to finish MU support, if someone want to sponsor it then will be faster, email me :)
- Added force tables creation option
- Added Can be modified by admin or if empty rule
- Added Can be empty (or not) rule also to dropdown
- Added Cimy Plug-in Series support
- Added capabilities to upload pictures without modifying WordPress files anymore (Thanx to j5)
- Added invert selection button useful when there are a lot of fields
- Added a warning pop-up before deleting fields
- Added a warning pop-up if a file that haven't a valid image extension is chosen
- Added German translation (Rita)
- Added Brazilian Portuguese translation (Sher)
- Updated Italian translation
- Changed the way how data are escaped, this has the effect that now html is allowed in label and description for example
- Fixed data inserted in the extra field by users were never deleted if the extra field was deleted
- Fixed maxlength attribute wrongly added to input file element during registration
- Fixed various problems with special characters present in some languages
- Removed break after label in the registration form (not for checkbox fields)
- Dropped magic_quotes_gpc_off function
- Lot of code cleanup (all the plug-in is now divided into different files)

v0.9.9 - 04/09/2007
- Added possibility to translate the plug-in
- Added Italian translation
- Fixed user's number of posts in A&U Extended page

v0.9.8 - 03/09/2007
- Added registration-date support
- Added rule to let modify extra fields content only by administrator
- Added database options: [empty, delete] extra fields and users data tables; [set to default, delete] options
- Added field's LABEL and TYPE to the array returned by get_cimyFieldValue (apart the case when providing both user_id and field_name)

v0.9.7 - 23/07/2007
- Added to get_cimyFieldValue partial results in search over user's extra fields values, see CASE 4b
- Fixed a bug introduced in v0.9.4 in get_cimyFieldValue that affected all MYSQL 4.x users
- Changed array order returned by get_cimyFieldValue function, see updated examples for details
- Home page url updated
- Readme file updated

v0.9.6 - 15/07/2007
- Added support for user picture-url, user can simply put an url of an existing image, no need to hack WordPress like picture support that anyway is a cool different feature
- Added ability in get_cimyFieldValue to retrieve all extra fields values from all users
- Pictures are now all resized according to equalTO rule also in A&U Extended page
- Fixed missing check for spaces presence on extra fields names
- Fixed cimy_rfr() redeclaration error
- Fixed wrong message error for picture field refers to size in MegaByte instead of KiloByte
- Fixed equalTo rule wrongly applied to picture fields during registration
- Fixed wp_create_thumbnail() missing function during registration for picture fields with equalTo rule specified
- Fixed default checked item with radio fields was broken in some cases
- Readme file updated
- Code cleanup

v0.9.5 - 09/07/2007
- Added support for user picture!
- Added id attribute to paragraph elements in the registration and user's profile
- Changed get_cimyFieldValue return a warning string when it's used but not enabled
- Fixed a bad bug when deselecting "Show in the registration" or "Show in user's profile" produced unexpected value inserted in that field
- Fixed warning message when updated the plug-in disappears just saving options and not de-activating and re-activating the plug-in
- Fixed some error messages not displayed when adding wrong numbers in [min,exact,max] length with textarea fields
- Fixed new row in "A&U Extended" wrong added when you have 9 or more fields
- Code cleanup

v0.9.4 - 25/06/2007
- Added a checkbox to manage equalTO rule and make it case sensitive or not
- Rewritten get_cimyFieldValue function:
  - now can accept also only one parameter: FIELD_NAME or USER_ID
  - added a third optional parameter called FIELD_VALUE
  - never return values from password fields for security reasons
  - updated README file to reflect changes to this function
  - note that this new version is backward compatible with previous calls
- Fixed equalTO rule that worked only if text was written in upper case
- Fixed equalTO error message for drop-down fields included also all choices and not only real label
- Fixed all HTML code, now it's XHTML 1.0 Transitional compliant
- Code cleanup

v0.9.3 - 10/06/2007
- Added min length and exact length rules
- Code cleanup

v0.9.2 - 06/06/2007
- Fixed radio and checkbox fields were too large under Internet Explorer 6.0 or lower, I know I have said that I will never fix this, but I lied!
- Removed warning and relative option for MSIE 6.0 (or lower) users introduced in 0.9.1
- Removed in user's profile border and grey background in radio and checkbox fields that were visible in some browsers like: Opera and Internet Explorer
- Fixed fields order, was totally broken for newer MYSQL versions (at least in mine 5.0.38, now should be ok for every one)
- Added a check that shows a warning in the options page when an user has updated the plug-in but forgot to de-activate and re-activate it

v0.9.1 - 05/06/2007
- Added Options page:
  - enable/disable get_cimyFieldValue() function to avoid unwanted use of this function by third parties
  - show/not a warning in the user's profile for who uses Microsoft Internet Explorer 6.0 or lower
  - add titles to fieldset
  - choose how many extra fields to show per one fieldset
  - hide/show some columns in "A&U Extended" page
- User's profile is now reorganized: checkbox and radio fields are back into fieldset; due to a WordPress CSS I made a workaround to avoid bigger inputs, however this workaround doesn't work under Microsoft Internet Explorer 6.0 or lower, this will never be fixed so don't ask about it!
- Do not include php_compat_mqgpc_unescape() function if already included by some other plug-in
- Code cleanup

v0.9.0 final - 15/05/2007
- Added some checks to make plug-in more secure and avoid admin functions to be used by non-admin (more security patches with next releases)
- Fixed own profile saved twice
- Re-added subdir in the package, removed by a mistake in 0.9.0-rc2
- Some Readme changes

v0.9.0 release candidate 2 - 15/04/2007
- Added extra fields data deletions when a user is deleted
- Added a lot of checks for string length inserted by user
- Added max length rule also to textarea field
- Changed max length up to 5000 characters for: value, label and description
- Changed "value" and "label" fields to textarea in the admin menu

v0.9.0 release candidate 1 - 01/04/2007
- Added drop-down support thanx to Raymond Elferink that hacked my plug-in, I have made only some small enhancements to his code
- Added a rule to set/unset field visibility in User's profile page
- Added a rule to set/unset field visibility in "A&U Extended" page
- Changed the way rules are saved, with php serialize seems a better way for code maintainability
- Label can now have length up to 1024 characters
- Updated get_cimyFields example in README file
- Fixed a bug that prevents changing options to a field with special characters in the name
- Some cosmetic changes to A&U Extended page
- Emulate magic_quotes_gpc=off if they are turned on
- Database changes: added indexes in both tables, if you have a lot of data probably this will increase speed
- Database changes: changed field 'LABEL' to TEXT in wp_cimy_fields table
- Code cleanup

v0.9.0 beta8 - 23/03/2007
- Finally fixed the very boring bug that for some people plug-in never creates tables when activated! Thanx to ysjack for helping me reproduce the problem
- Dropped using $table_prefix variable since WordPress 2.1 deprecated it
- Updated get_cimyFieldValue example in README file, now it is a complete example
- Added a subdir in the package

v0.9.0 beta7 - 17/03/2007
- Added password support, now you can set a password to register to your site!
- Added stripslashes also to get_cimyFieldValue function so no more backslashes are returned with certain characters
- Fixed get_cimyFieldValue function never returns NULL when some parameter was wrong, now it does!

v0.9.0 beta6 - 16/03/2007
- Fixed value field wasn't applied in user's profile when a text or textarea was empty
- Fixed a bug (introduced in beta5) that fill some data in the new field form when you just update an existing field
- Fixed editable rule that was broken probably in beta3 or 4 during some code cleanup :(
- Changed plug-in link, now it points to the specific blog page

v0.9.0 beta5 - 15/03/2007
- Added textarea support (up to 1024 characters)
- Added class attribute in the registration form so now all extra fields have the same look like built-in ones
- Added tabindex attribute in the registration form so now when you press tab you have a normal sequence
- Now in the option page when you fill the form for a new field all data are kept in memory every time you press "Add" button
- Fixed equalTO rule for text fields that any value entered was to be all in upper case format to make error message go away
- Code cleanup

v0.9.0 beta4 - 13/03/2007 later
- Forgot to change also tables creation with new modification made in beta3, if you had problem please update to this version and activate the plug-in again, all will back to normal
- Performance improvement: now extra fields are read from database only when they are needed
- Added get_cimyFields() function to retrieve all extra fields information useful for templates/themes
- Fixed equalTO rule in radio fields, was completely broken
- Code cleanup

v0.9.0 beta3 - 13/03/2007
- Added radio input support
- Added value field, you can now pre-enter characters in your text fields or pre-select checkbox and radio fields
- Added a check that disable all rules unrelated to a certain field type
- Added some login-bkg-tile.gif with bigger height to workaround fields out of frame during registration
- Fixed checkbox fields in edit profile were very large, to fix this all radio and checkbox fields are now out of fieldset
- Some cosmetic changes to option and profile pages
- All html transformable characters are now transformed in UTF-8 html format
- Added a stripslashes before showing data so these characters are now allowed: ', ", <, >, (, ), [, ], #
- Database changes: new field 'VALUE', changed fields 'RULES' and 'DESCRIPTION' to TEXT in wp_cimy_fields table
- Database changes: changed field 'VALUE' to TEXT in wp_cimy_data table
- Code cleanup

v0.9.0 beta2 - 27/02/2007
- Fixed a bug that returns MYSQL error when deleting extra fields in certain circumstances
- Fixed a bug that returns MYSQL errors in Users->Your Profile and when adding a new user from administration, these errors were shown only when there were no extra fields defined
- Added a control to prevent from creating/deleting tables to users without enough privileges

v0.9.0 beta1 - 12/02/2007
- Plug-in now supports only WordPress 2.1!
- Removed wp-register.php, with WP 2.1 is not needed anymore, form registration extra fields are now built-in
- Added get_cimyFieldValue function that can be used in your themes
- Fixed a bug that prevents to save unchecked checkbox in user's profile
- Fixed a bug that returns MYSQL error during user's profile update and all fields were set to "Cannot be edited"
- Moved all these infos to a readme file

v0.8.7 - 28/12/2006
- Fixed a bug with PHP<5.0 that in the "equalTo" field saves some strange characters

v0.8.6
- First public release