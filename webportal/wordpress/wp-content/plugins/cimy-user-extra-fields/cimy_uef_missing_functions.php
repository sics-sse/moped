<?php

/**
 * Load an image from a string, if PHP supports it.
 *
 * @since 2.1.0
 *
 * @param string $file Filename of the image to load.
 * @return resource The resulting image resource on success, Error string on failure.
 */
function wp_load_image( $file ) {
	if ( is_numeric( $file ) )
		$file = get_attached_file( $file );

	if ( ! file_exists( $file ) )
		return sprintf(__("File '%s' doesn't exist?"), $file);

	if ( ! function_exists('imagecreatefromstring') )
		return __('The GD image library is not installed.');

	// Set artificially high because GD uses uncompressed images in memory
	@ini_set('memory_limit', '256M');
	$image = imagecreatefromstring( file_get_contents( $file ) );

	if ( !is_resource( $image ) )
		return sprintf(__("File '%s' is not an image."), $file);

	return $image;
}
