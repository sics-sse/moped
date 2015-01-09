<?php
/**
 * PHP Real Ajax Uploader
 * Copyright @Alban Xhaferllari
 * albanx@gmail.com
 * www.albanx.com
 */
error_reporting(E_ALL ^ E_NOTICE);//remove notice for json invalidation

$uploadPath	= $_REQUEST['ax-file-path'];
$fileName	= $_REQUEST['ax-file-name'];
$currByte	= $_REQUEST['ax-start-byte'];
$maxFileSize= $_REQUEST['ax-maxFileSize'];
$html5fsize	= $_REQUEST['ax-fileSize'];
$isLast		= $_REQUEST['isLast'];

//if set generates thumbs only on images type files
$thumbHeight	= $_REQUEST['ax-thumbHeight'];
$thumbWidth		= $_REQUEST['ax-thumbWidth'];
$thumbPostfix	= $_REQUEST['ax-thumbPostfix'];
$thumbPath		= $_REQUEST['ax-thumbPath'];
$thumbFormat	= $_REQUEST['ax-thumbFormat'];

$allowExt	= (empty($_REQUEST['ax-allow-ext']))?array():explode('|', $_REQUEST['ax-allow-ext']);
$uploadPath	.= (!in_array(substr($uploadPath, -1), array('\\','/') ) )?DIRECTORY_SEPARATOR:'';//normalize path

if(!file_exists($uploadPath) && !empty($uploadPath))
{
	mkdir($uploadPath, 0777, true);
}

if(!file_exists($thumbPath) && !empty($thumbPath))
{
	mkdir($thumbPath, 0777, true);
}


//with gd library

function createThumbGD($filepath, $thumbPath, $postfix, $maxwidth, $maxheight, $format='jpg', $quality=75)
{	
	if($maxwidth<=0 && $maxheight<=0)
	{
		return 'No valid width and height given';
	}
	
	$gd_formats	= array('jpg','jpeg','png','gif');//web formats
	$file_name	= pathinfo($filepath);
	if(empty($format)) $format = $file_name['extension'];
	
	if(!in_array(strtolower($file_name['extension']), $gd_formats))
	{
		return false;
	}
	
	$thumb_name	= $file_name['filename'].$postfix.'.'.$format;
	
	if(empty($thumbPath))
	{
		$thumbPath=$file_name['dirname'];	
	}
	$thumbPath.= (!in_array(substr($thumbPath, -1), array('\\','/') ) )?DIRECTORY_SEPARATOR:'';//normalize path
	
	// Get new dimensions
	list($width_orig, $height_orig) = getimagesize($filepath);
	if($width_orig>0 && $height_orig>0)
	{
		$ratioX	= $maxwidth/$width_orig;
		$ratioY	= $maxheight/$height_orig;
		$ratio 	= min($ratioX, $ratioY);
		$ratio	= ($ratio==0)?max($ratioX, $ratioY):$ratio;
		$newW	= $width_orig*$ratio;
		$newH	= $height_orig*$ratio;
			
		// Resample
		$thumb = imagecreatetruecolor($newW, $newH);
		$image = imagecreatefromstring(file_get_contents($filepath));
			
		imagecopyresampled($thumb, $image, 0, 0, 0, 0, $newW, $newH, $width_orig, $height_orig);
		
		// Output
		switch (strtolower($format)) {
			case 'png':
				imagepng($thumb, $thumbPath.$thumb_name, 9);
			break;
			
			case 'gif':
				imagegif($thumb, $thumbPath.$thumb_name);
			break;
			
			default:
				imagejpeg($thumb, $thumbPath.$thumb_name, $quality);;
			break;
		}
		imagedestroy($image);
		imagedestroy($thumb);
	}
	else 
	{
		return false;
	}
}


//for image magick
function createThumbIM($filepath, $thumbPath, $postfix, $maxwidth, $maxheight, $format)
{
	$file_name	= pathinfo($filepath);
	$thumb_name	= $file_name['filename'].$postfix.'.'.$format;
	
	if(empty($thumbPath))
	{
		$thumbPath=$file_name['dirname'];	
	}
	$thumbPath.= (!in_array(substr($thumbPath, -1), array('\\','/') ) )?DIRECTORY_SEPARATOR:'';//normalize path
	
	$image = new Imagick($filepath);
	$image->thumbnailImage($maxwidth, $maxheight);
	$images->writeImages($thumbPath.$thumb_name);
}


function checkFilename($fileName, $size, $newName = '')
{
	global $allowExt, $uploadPath, $maxFileSize;
	
	//------------------max file size check from js
	$maxsize_regex = preg_match("/^(?'size'[\\d]+)(?'rang'[a-z]{0,1})$/i", $maxFileSize, $match);
	$maxSize=4*1024*1024;//default 4 M
	if($maxsize_regex && is_numeric($match['size']))
	{
		switch (strtoupper($match['rang']))//1024 or 1000??
		{
			case 'K': $maxSize = $match[1]*1024; break;
			case 'M': $maxSize = $match[1]*1024*1024; break;
			case 'G': $maxSize = $match[1]*1024*1024*1024; break;
			case 'T': $maxSize = $match[1]*1024*1024*1024*1024; break;
			default: $maxSize = $match[1];//default 4 M
		}
	}

	if(!empty($maxFileSize) && $size>$maxSize)
	{
		echo json_encode(array('name'=>$fileName, 'size'=>$size, 'status'=>'error', 'info'=>'File size not allowed.'));
		return false;
	}
	//-----------------End max file size check
	
	
	//comment if not using windows web server
	$windowsReserved	= array('CON', 'PRN', 'AUX', 'NUL','COM1', 'COM2', 'COM3', 'COM4', 'COM5', 'COM6', 'COM7', 'COM8', 'COM9',
            				'LPT1', 'LPT2', 'LPT3', 'LPT4', 'LPT5', 'LPT6', 'LPT7', 'LPT8', 'LPT9');    
	$badWinChars		= array_merge(array_map('chr', range(0,31)), array("<", ">", ":", '"', "/", "\\", "|", "?", "*"));

	$fileName	= str_replace($badWinChars, '', $fileName);
    $fileInfo	= pathinfo($fileName);
    $fileExt	= $fileInfo['extension'];
    $fileBase	= $fileInfo['filename'];
    
    //check if legal windows file name
	if(in_array($fileName, $windowsReserved))
	{
		echo json_encode(array('name'=>$fileName, 'size'=>0, 'status'=>'error', 'info'=>'File name not allowed. Windows reserverd.'));	
		return false;
	}
	
    //check if is allowed extension
	if(!in_array($fileExt, $allowExt) && count($allowExt))
	{
		echo json_encode(array('name'=>$fileName, 'size'=>0, 'status'=>'error', 'info'=>"Extension [$fileExt] not allowed."));	
		return false;
	}
    
	$fullPath = $uploadPath.$fileName;
    $c=0;
	while(file_exists($fullPath))
	{
		$c++;
		$fileName	= $fileBase."($c).".$fileExt;
		$fullPath 	= $uploadPath.$fileName;
	}
	return $fullPath;
}

if(isset($_FILES['ax-files'])) 
{
	//for eahc theorically runs only 1 time, since i upload i file per time
    foreach ($_FILES['ax-files']['error'] as $key => $error)
    {
		if ($error == UPLOAD_ERR_OK)
        {
        	$newName = !empty($fileName)? $fileName:$_FILES['ax-files']['name'][$key];
        	$fullPath = checkFilename($newName, $_FILES['ax-files']['size'][$key]);
        	
        	if($fullPath)
        	{
				move_uploaded_file($_FILES['ax-files']['tmp_name'][$key], $fullPath);
				if(!empty($thumbWidth) || !empty($thumbHeight))
					createThumbGD($fullPath, $thumbPath, $thumbPostfix, $thumbWidth, $thumbHeight, $thumbFormat);
					
				echo json_encode(array('name'=>basename($fullPath), 'size'=>filesize($fullPath), 'status'=>'uploaded', 'info'=>'File uploaded'));
        	}
        }
        else
        {
        	echo json_encode(array('name'=>basename($_FILES['ax-files']['name'][$key]), 'size'=>$_FILES['ax-files']['size'][$key], 'status'=>'error', 'info'=>$error));	
        }
    }
}
elseif(isset($_REQUEST['ax-file-name'])) 
{
	//check only the first peice
	$fullPath = ($currByte!=0) ? $uploadPath.$fileName:checkFilename($fileName, $html5fsize);
	
	if($fullPath)
	{
		$flag			= ($currByte==0) ? 0:FILE_APPEND;
		$receivedBytes	= file_get_contents('php://input');
		//strange bug on very fast connections like localhost, some times cant write on file
		//TODO future version save parts on different files and then make join of parts
	    while(@file_put_contents($fullPath, $receivedBytes, $flag) === false)
	    {
	    	usleep(50);
	    }
	    
	    if($isLast=='true')
	    {
	    	createThumbGD($fullPath, $thumbPath, $thumbPostfix, $thumbWidth, $thumbHeight, $thumbFormat);
	    }
	    echo json_encode(array('name'=>basename($fullPath), 'size'=>$currByte, 'status'=>'uploaded', 'info'=>'File/chunk uploaded'));
	}
}