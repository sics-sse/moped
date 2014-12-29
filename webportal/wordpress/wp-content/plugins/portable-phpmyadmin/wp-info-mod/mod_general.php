<?php
// General functions
// General MySQL server information
function get_portable_serverinfo() {
	echo '
	<br class="clear" />
	<table class="widefat">
		<thead>
			<tr>
				<th>Variable Name</th>
				<th>Value</th>
				<th>Variable Name</th>
				<th>Value</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>OS</td>
				<td>'.PHP_OS.'</td>
				<td>Database Data Disk Usage</td>
				<td>'.format_filesize(get_mysql_data_usage()).'</td>
			</tr>
			<tr class="alternate">
				<td>Server</td>
				<td>'.$_SERVER['SERVER_SOFTWARE'].'</td>
				<td>Database Index Disk Usage</td>
				<td>'.format_filesize(get_mysql_index_usage()).'</td>
			</tr>
			<tr>
				<td>PHP</td>
				<td>v'.PHP_VERSION.'</td>
				<td>MYSQL Maximum Packet Size</td>
				<td>'.format_filesize(get_mysql_max_allowed_packet()).'</td>
			</tr>
			<tr class="alternate">
				<td>MYSQL</td>
				<td>v'.get_mysql_version().'</td>
				<td>MYSQL Maximum Allowed Connections</td>
				<td>'.number_format_i18n(get_mysql_max_allowed_connections()).'</td>
			</tr>
		</tbody>
	</table>
	<br class="clear" />';
}
// Function: Format Bytes Into TiB/GiB/MiB/KiB/Bytes
if(!function_exists('format_filesize')) {
	function format_filesize($rawSize) {
		if($rawSize / 1099511627776 > 1)
			return number_format_i18n($rawSize/1099511627776, 1).' TiB';
		elseif($rawSize / 1073741824 > 1)
			return number_format_i18n($rawSize/1073741824, 1).' GiB';
		elseif($rawSize / 1048576 > 1)
			return number_format_i18n($rawSize/1048576, 1).' MiB';
		elseif($rawSize / 1024 > 1)
			return number_format_i18n($rawSize/1024, 1).' KiB';
		elseif($rawSize > 1)
			return number_format_i18n($rawSize, 0).' bytes';
		else
			return 'unknown';
	}
}
// Function: Get MYSQL Version
if(!function_exists('get_mysql_version')) {
	function get_mysql_version() {
		global $wpdb;		
		return $wpdb->get_var("SELECT VERSION() AS version");
	}
}
// Function: Get MYSQL Data Usage
if(!function_exists('get_mysql_data_usage')) {
	function get_mysql_data_usage() {
		global $wpdb;
		$data_usage = 0;
		$tablesstatus = $wpdb->get_results("SHOW TABLE STATUS");
		foreach($tablesstatus as $tablestatus) {
			$data_usage += $tablestatus->Data_length;
		}
		if(!$data_usage)
			$data_usage = 'N/A';
		return $data_usage;
	}
}
// Function: Get MYSQL Index Usage
if(!function_exists('get_mysql_index_usage')) {
	function get_mysql_index_usage() {
		global $wpdb;		
		$index_usage = 0;
		$tablesstatus = $wpdb->get_results("SHOW TABLE STATUS");
		foreach($tablesstatus as $tablestatus) {
			$index_usage += $tablestatus->Index_length;
		}
		if(!$index_usage)
			$index_usage = 'N/A';
		return $index_usage;
	}
}
// Function: Get MYSQL Max Allowed Packet
if(!function_exists('get_mysql_max_allowed_packet')) {
	function get_mysql_max_allowed_packet() {
		global $wpdb;		
		$packet_max_query = $wpdb->get_row("SHOW VARIABLES LIKE 'max_allowed_packet'");
		$packet_max = $packet_max_query->Value;
		if(!$packet_max)
			$packet_max = 'N/A';
		return $packet_max;
	}
}
// Function:Get MYSQL Max Allowed Connections
if(!function_exists('get_mysql_max_allowed_connections')) {
	function get_mysql_max_allowed_connections() {
		global $wpdb;
		$connection_max_query = $wpdb->get_row("SHOW VARIABLES LIKE 'max_connections'");
		$connection_max = $connection_max_query->Value;
		if(!$connection_max)
			$connection_max = 'N/A';
		return $connection_max;
	}
}

// Size Categories
if(!function_exists('file_size_info')) {
	function file_size_info($filesize) {
		$bytes = array('KB', 'KB', 'MB', 'GB', 'TB');
		if ($filesize < 1024) $filesize = 1;

		for($i = 0; $filesize > 1024; $i++) $filesize /= 1024;
		$file_size_info['size'] = round($filesize,3);
		$file_size_info['type'] = $bytes[$i];
		return $file_size_info;
	} 
}
// Calculate DB size by adding table size + index size
if(!function_exists('db_size')) {
	function db_size() {
		$rows = mysql_query('SHOW table STATUS');
		$dbsize = 0;
		while($row = mysql_fetch_array($rows)) {
			$dbsize += $row['Data_length'] + $row['Index_length'];
		}

		if($dbsize > ALERTLEVEL * 1024 * 1024)
			$color = '#FF0000';
		else
			$color = '#0000FF';

		$dbsize = file_size_info($dbsize);
		echo '<span style="color: ' . $color . '">{' . $dbsize['size'] . '} {' . $dbsize['type'] . '}</span>'; 
	}
}
?>
