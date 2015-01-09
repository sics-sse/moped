=== Add From Server ===
Contributors: dd32
Tags: admin, media, uploads, post, import, files
Requires at least: 3.2
Stable tag: 3.2.0.3

"Add From Server" is a quick plugin which allows you to import media & files into the WordPress uploads manager from the Webservers filesystem

== Description ==

The heart of a CMS is the ability to upload and insert content, WordPress does a fantastic job at this, unfortunately, some web hosts have limited servers, or users simply do not have the ability to upload large files through their web browser.
Add From Server is designed to help ease this pain, You can upload a bunch of files via FTP (Or your favourite transmission method) and simply import those files from the webserver directly into WordPress.

== Changelog ==

= 3.2.0.3 =
 * PHP 5.4 compatibility
 * Special characters in URL fixes
 * Japanese Translations from Naoko Takano ( http://en.naoko.cc/ )

= 3.2.0.2 =
 * Fix: Translations not loaded early enough for menu items.

= 3.2.0.1 =
 * Fix: Incorrect GUID on imported items in subdir of uploads folder
 * Fix: Not all items would correctly trigger the already-imported error

= 3.2.0 =
 * For Pre-3.2 changelog, Please see http://plugins.svn.wordpress.org/add-from-server/tags/2.3/readme.txt
 * Cleanup, Now requires WordPress 3.2+
 * Now has an options panel to control who is allowed to import files
 * Now has the option to specify what the base directory is for file imports (ie. allows you to prevent users access to non-public directories)
 * Versioning changes - This plugin will from now use the earliest version of WordPress it supports. 3.2 requires 3.2, 3.2.0.1 is a point release which requires 3.2. 3.3.4 will require WP 3.3.4 etc. May seem a little weird, but you'll get used to it, bigger numbers are still better :)

== Upgrade Notice ==

= 3.2.0.3 =
Japanese Translations, PHP 5.4 compatibility, and a fix to special characters in urls.

== FAQ ==
 Q: What placeholders can I use in the Root path option?
 You can use %role% and %username% only. In the case of Role, The first role which the user has is used, This can mean that in complex installs, that using %role% is unreliable.

 Q: Why does the file I want to import have a red background?
 WordPress only allows the importing/uploading of certain file types to improve your security. If you wish to add extra file types, you can use a plugin such as: http://wordpress.org/extend/plugins/pjw-mime-config/ You can also enable "Unfiltered uploads" globally for WordPress if you'd like to override this security function. Please see the WordPress support forum for details.

 Q: Where are the files saved?
 If you import a file which is outside your standard upload directory (usually wp-content/uploads/) then it will be copied to your current upload directory setting as normal. If you however import a file which -is already within the uploads directory- (for example, wp-content/uploads/2011/02/superplugin.zip) then the file will not be copied, and will be used as-is.

 Q: I have a a bug report
 Then please email me! wordpress at dd32.id.au is best.

== Screenshots ==

1. The import manager, This allows you to select which files to import. Note that files which cannot be imported are Red.
2. The Options panel, This allows you to specify what users can access Add From Server, and which folders users can import files from.
