=== Content Mirror ===
Contributors: stratoponjak
Donate link: http://klasehnemark.com/wordpress-plugins
Tags: content, mirror, copy, shortcode, page, post, editor, admin, multisite
Requires at least: 3.0
Tested up to: 3.9
Stable tag: 1.2

You can display content from one page or post on another by mirror content. This also works between sites in a multisite configuration.

== Description ==

This WordPress plugin makes it possible to show content from one page on another – it mirrors content. It’s a very easy to use, but yet a powerful plugin.

It takes content from pages, posts and custom post types and shows it on other pages, posts or custom post types. It also works between sites in a Multi-site configuration. For instance, if you have a web site with one site for each language, you only reuse the same information on all sites.

== Installation ==

1. Upload the folder "content-mirror" to the "/wp-content/plugins/" directory
1. Activate the plugin through the 'Plugins' menu in WordPress
1. Once you’ve activated the plugin in your WordPress server you invoke Content Mirror in the editor.

== Frequently Asked Questions ==

= What kind of shortcodes can I use? =

All shortcodes and usage are listed at [klasehnemark.com/content-mirror](http://klasehnemark.com/content-mirror "Full Documentation of Content Mirror") 

== Screenshots ==

1. This is what mirrored content looks like in the editor.
1. This is the dialog window when selected what content will be mirrored.
1. This is an example of how to use shortcodes instead.

== Changelog ==

= 1.2 =
* Bugfixes, for working in Wordpress v.3.9

= 1.1 =
* Removed php-tags in the .js-files that caused the plugin not to work properly in some web browsers
* Changed how plugin-directory is detected, previous method caused the plugin not to work properly in some Wordpress installations
* Corrected a bug in the content_mirror_output-filter that sended a false parameter
* Adding a content_mirror_list_item-filter so the mirror-list of items to choose from in admin can be manipulated

= 1.0.1 =
* Changed license to GNU General Public License, version 2

= 1.0 =
* Initial release
