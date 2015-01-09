<?php
	$file = $_FILES['file'];
	move_uploaded_file($_FILES["file"]["tmp_name"], 'upload_dir/' . $_FILES["file"]["name"]);
	echo 'OK';
?>