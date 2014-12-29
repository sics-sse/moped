jQuery(document).ready(function($){ 
	$('#add_configurations').click( function() {
		var $lab="<div id='lab'>"+$('#vehicle').find(":selected").text()+"<a id="+$('#vehicle').find(":selected").val()+" href='javascript:void(0)'  title='Delete'>X</a></div>";
		$('#configurations').append($lab);
		var $insert_hidden_value = "<input type='hidden' name='configs[]' value="+$('#vehicle').find(":selected").val()+" />";
		$('#vehicle_config').append($insert_hidden_value);
		
	});
	
	$(document).delegate('#lab a', 'click', function() {
		$(this).parent().remove();
		a_id_value= $(this).attr('id');
		$("input[type='hidden'][name='configs[]'][value='"+a_id_value+"']").remove();
		
	});
})(jQuery);