jQuery(document).ready(function($){
		$("#UploadButton").ajaxUpload({
		url : "upload.php",
		name: "file",
		onSubmit: function() {
			$('#InfoBox').html('Uploading ... ');
		},
		onComplete: function(result) {
			$('#InfoBox').html('File uploaded with result' + result);
		}
		});
})(jQuery);