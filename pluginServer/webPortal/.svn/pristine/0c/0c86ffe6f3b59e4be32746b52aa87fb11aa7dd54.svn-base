var formblock;
var forminputs;

function invert_sel(formname, name, label) {
	formblock = document.getElementById(formname);
	forminputs = formblock.getElementsByTagName('input');

	for (i = 0; i < forminputs.length; i++) {
		// regex here to check name attribute
		var regex = new RegExp(name, "i");

		if (regex.test(forminputs[i].getAttribute('name'))) {
			
			if (forminputs[i].checked == false) {
				forminputs[i].checked = true;
			} else {
				forminputs[i].checked = false;
			}
		}
	}

	return label;
}