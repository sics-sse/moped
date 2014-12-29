/*
	function by Marco Cimmino - e-mail: cimmino.marco@gmail.com
	licensed under GPLv3
	v1.0.0

	This function allows uploading files over forms that do not allow it and checks
		for allowed extensions displaying a popup in case of error and clearing
		input field to avoid upload

	form_name: name of the form
	field_name: name of the input file field presents in the form
	msg: message to display in case extension check fails
	extensions: array containing extensions allowed, empty means everything is allowed
*/
function uploadFile(form_name, field_name, msg, extensions) {
	var browser = navigator.appName;
	var formblock = document.getElementById(form_name);
	var field = document.getElementById(field_name);

	// as usual not respecting standards, bugger!
	if (browser == "Microsoft Internet Explorer")
		formblock.encoding = "multipart/form-data";
	else
		formblock.enctype = "multipart/form-data";
	
	var upload = field.value;
	upload = upload.toLowerCase();
	
	if (upload != '') {
		var ext1 = upload.substring((upload.length-3),(upload.length));
		var ext2 = upload.substring((upload.length-4),(upload.length));
	}

	// if array is empty then means all extensions are allowed
	if (extensions.length > 0) {
		var found = false;
		for (var i=0; i<extensions.length; i++) {
			var ext = extensions[i].toLowerCase();

			if ((ext1 == ext) || (ext2 == ext)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			field.value = '';
			alert(msg+": "+extensions.join(' '));
		}
	}
}
