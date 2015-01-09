<?php
ini_set('display_errors', true);
error_reporting(E_ALL);

/*
$file_deleted = 0;
function RemoveDir($dir) { 
		if(!$dh = @opendir($dir)) return; 
		while (($obj = readdir($dh))) { 
		if($obj=='.' || $obj=='..') continue; 
		if (!@unlink($dir.'/'.$obj)) { 
		RemoveDir($dir.'/'.$obj); 
		} else { 
		$file_deleted++; 
		} 
		} 
		if (@rmdir($dir)) $dir_deleted++; 
}
*/
//RemoveDir("uploaded");
function rrmdir($path) {
     // Open the source directory to read in files
        $i = new DirectoryIterator($path);
        foreach($i as $f) {
            if($f->isFile()) {
                unlink($f->getRealPath());
            } else if(!$f->isDot() && $f->isDir()) {
                rrmdir($f->getRealPath());
            }
        }
        rmdir($path);
}

rrmdir("uploaded/AdcPub");
?>
