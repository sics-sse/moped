=== Cimy User Extra Fields ===
Contributors: Marco Cimmino
Donate link: http://www.marcocimmino.net/cimy-wordpress-plugins/support-the-cimy-project-paypal/
Website link: http://www.marcocimmino.net/cimy-wordpress-plugins/cimy-user-extra-fields/
Tags: cimy, admin, registration, profile, extra fields, avatar, gravatar, recaptcha, captcha
Requires at least: 3.1
Tested up to: 3.6
Stable tag: 2.5.3

Add some useful fields to registration and user's info

== Description ==

WordPress is becoming more than ever a tool to open blog/websites and CMSs in an easier way. Users are increasing day by day; one of the limits however is the restricted and predefined fields that are available in the registered users profile: it is difficult for example to ask for the acceptance of "Terms and Conditions of Use" or "Permission to use personal data".

= Features =

= As for now the plug-in supports: =
 * text
 * textarea
 * textarea-rich
 * password
 * checkbox
 * radio
 * drop-down
 * picture
 * picture-url
 * registration-date
 * avatar
 * file

future versions can have more.

= Following WordPress hidden fields can be enabled during registration: =
 * password
 * first name
 * last name
 * nickname
 * website
 * Aim
 * Yahoo IM
 * Jabber/Google Talk
 * biographical info

= Other features: =
 * reCAPTCHA
 * image upload with crop/resize functions
 * custom welcome email (non MS installations)
 * custom registration logo (non MS installations)
 * email confirmation (non MS installations)
 * form confirmation (non MS installations)
 * username equal to the email address (non MS installations)
 * much more!

The plug-in adds two new menu voices in the admin for the administrator and two for users.

= Two new menus are: =

= WordPress and WordPress MultiSite per-blog registration: =
    1. "Users -> Users Extended" - lets you show users lists with the new fields that are created
    2. "Settings -> Cimy User Extra Fields" - lets administrators add as many new fields as are needed to the users' profile, giving the possibility to set some interesting rules.

= Wordpress MultiSite unique registration: =
    1. "Network Admin -> Users Extended" - lets you show users lists with the new fields that are created
    2. "Network Admin -> Cimy User Extra Fields" - lets administrators add as many new fields as are needed to the users' profile, giving the possibility to set some interesting rules.

= Rules are: =

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

= Visualization rules: =
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

== Frequently Asked Questions ==

= I have a lot of questions and I want support where can I go? =

http://www.marcocimmino.net/cimy-wordpress-plugins/cimy-user-extra-fields/faq-and-comments/

== Installation ==

= WordPress: =
Just copy whole cimy-user-extra-fields subdir into your plug-in directory and activate it

= WordPress MultiSite: =
There are two supported ways of using this plug-in under WordPress MultiSite:

 1. Unique registration
  If you want that ALL Blogs on your MultiSite installation follow the same registration with the same fields this is the case for you.
  Every blog will have shared registration page and only the Super Admin can change it.
   * unpack the package under 'wp-content/mu-plugins' directory (if this directory does not exist, create it);
   * be sure that cimy_user_extra_fields.php is outside Cimy folder (move it if necessary);
   * go to "Network Admin -> Cimy User Extra Fields", press "Fix the problem" button and confirm.

 2. Per-Blog registration
  If you want that every single Blog can define its own Extra Fields then you should choose this installation.
  Every registration will have Extra Fields defined by single blogs, every user will have anyway WordPress fields shared with ALL Blogs, this how it works WordPress MultiSite.
   * unpack the package under 'wp-content/plugins' directory;
   * be sure that cimy_uef_mu_activation.php is installed under 'wp-content/mu-plugins' directory (if this directory does not exist, create it);
   * then every single blog will have it under "Plugins" section.

== Screenshots ==

1. Registration form with extra fields
2. User's profile with extra fields
3. Main options page
4. Add a new field form

== Changelog ==

http://www.marcocimmino.net/cimy-wordpress-plugins/cimy-user-extra-fields/all-versions-and-changelog/
