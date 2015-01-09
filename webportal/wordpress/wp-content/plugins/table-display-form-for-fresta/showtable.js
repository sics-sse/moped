jQuery(document).ready(function($){ 
	$('#example').dataTable( {
		"bProcessing": true,
		"bServerSide": true,
		"sAjaxSource": "wordpress/custom/fetch_data_from_server.php",
		"aoColumns": [
		null,
		null,
		null,
		{ "bVisible": false, "aTargets": [3] },
		{ "mData":null,"sDefaultContent": "<input id='Squawk_install' type='image' src='wordpress/custom/images/install.png' alt='Install' /> " },
		{ "mData":null,"sDefaultContent": "<input id='Jdk_install' type='image' src='wordpress/custom/images/install.png' alt='Install' /> " }],
		"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$('td:eq(3)',nRow).children('input:first').val(aData[3]);
			$('td:eq(4)',nRow).children('input:first').val(aData[3]);
		}
	} ); 
	
	$('#Jdk_install').live('click', function() {
            	var id = $(this).val();
                $.ajax({type:'POST', url: 'wordpress/custom/jdk_install_handle.php', data:{id:id}, success: function(response) {
    $('#feedback').html(response);
        }});
                return false;
        });	

	$('#Squawk_install').live('click', function() {
		var id = $(this).val();
		$.ajax({type:'POST', url: 'wordpress/custom/install_handle.php', data:{id:id}, success: function(response) {
    $('#feedback').html(response);
	}});
		return false;

	});

	/*$('#example tbody tr td input[type="image"]').live('click', function() {
                var id = $(this).val();
                //var username = "zeni";
                $.ajax({type:'POST', url: 'wordpress/custom/install_handle.php', data:{id:id}, success: function(response) {
    $('#feedback').html(response);
        }});
                return false;
        });*/
})(jQuery);
