jQuery(document).ready(function($) {
	editExtraField = function(user_id, field_name) {
		var e = $('#edit-'+user_id+'-'+field_name), revert_e = e.html(), new_html;
		var old_value = $('#value-'+user_id+'-'+field_name).html();
		var all_inside_ef_td = $('#ef-new-value-'+field_name).clone();
		var extra_field = all_inside_ef_td.find(':first-child');
		var tag_name = extra_field.get(0).tagName.toLowerCase();
		var extra_field_type;
		if (tag_name == 'select') {
			if (extra_field.attr('multiple') != undefined)
				extra_field_type = 'select-multiple';
			else
				extra_field_type = tag_name;
		}
		else
			extra_field_type = extra_field.attr('type');

		extra_field.attr('id', 'ef-new-value-'+user_id+'-'+field_name);
		extra_field.attr('name', '');

		new_html = all_inside_ef_td.html();
		new_html += '<br /><br /><a href="#" class="save button">'+postL10n.ok+'</a> <a class="cancel" href="#">'+postL10n.cancel+'</a><br /><br />';
		e.html(new_html);

		$('#ef-new-value-'+user_id+'-'+field_name).keypress(function(f){
			var key = f.keyCode || 0;
			// on enter, just save the new slug, don't save the post
			if ( 13 == key ) {
				e.children('.save').click();
				return false;
			}
			if ( 27 == key ) {
				e.children('.cancel').click();
				return false;
			}
		});

		// focus first, then feed the value, so cursor will be at the end
		$('#ef-new-value-'+user_id+'-'+field_name).focus();
		if (extra_field_type == 'select-multiple')
			$('#ef-new-value-'+user_id+'-'+field_name).val(old_value.split(','));
		else
			$('#ef-new-value-'+user_id+'-'+field_name).val(old_value);

		if (extra_field_type == 'checkbox' && old_value == "YES")
			$('#ef-new-value-'+user_id+'-'+field_name).attr("checked", true);

		$('.cancel').click(function() {
			e.html(revert_e);
		});

		$('.save').click(function() {
			var new_value, cFlag = false;
			if (extra_field_type == 'checkbox') {
				cFlag = $('#ef-new-value-'+user_id+'-'+field_name).is(':checked');
				cFlag ? new_value = '1' : new_value = '';
			}
			else
				new_value = $('#ef-new-value-'+user_id+'-'+field_name).val();

			if (extra_field_type == 'select-multiple') {
				if (new_value == null)
					new_value = Array();

				if ($.inArray(postL10n.dropdown_first_item, new_value) > -1)
					new_value.splice(0, 1);

				new_value = new_value.join(',');
			}

			if (extra_field_type == 'select-one' && new_value == postL10n.dropdown_first_item) {
				e.html(revert_e);
			} else {
				$.post(ajaxurl, {
					action: 'save-extra-field-new-value',
					user_id: user_id,
					field_name: field_name,
					new_value: new_value,
					extrafieldnewvaluenonce: $('#extrafieldnewvaluenonce').val()
				}, function(response) {
					var new_value_c;
					if (response != null) {
						new_value_c = new_value;
						if (extra_field_type == 'checkbox')
							cFlag ? new_value_c = "YES" : new_value_c = "NO";
					}
					else
						new_value_c = old_value

					e.html(revert_e);
					$('#value-'+user_id+'-'+field_name).html(new_value_c);
				});
			}
		});
	}
});
