function preview(img, selection) {
	if (!selection.width || !selection.height)
		return;

	var scaleX = 100 / selection.width;
	var scaleY = 100 / selection.height;

	jQuery('#preview img').css({
		width: Math.round(scaleX * 300),
		height: Math.round(scaleY * 300),
		marginLeft: -Math.round(scaleX * selection.x1),
		marginTop: -Math.round(scaleY * selection.y1)
	});

	jQuery('#'+img.id+'_x1').val(selection.x1);
	jQuery('#'+img.id+'_y1').val(selection.y1);
	jQuery('#'+img.id+'_x2').val(selection.x2);
	jQuery('#'+img.id+'_y2').val(selection.y2);
	jQuery('#'+img.id+'_w').val(selection.width);
	jQuery('#'+img.id+'_h').val(selection.height);
}
