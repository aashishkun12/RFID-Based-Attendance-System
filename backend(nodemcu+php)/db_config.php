<?php

// Database Configuration File
define('DB_SERVER', 'localhost');
define('DB_USERNAME', 'YOUR DB_PASSWORD');
define('DB_PASSWORD', 'YOUR DB_PASSWORD');
define('DB_NAME', 'ams');
define('TIMEZONE', 'Asia/Kathmandu');

/**
 * Establishes database connection
 * @return mysqli connection object
 */
function getDBConnection() {
    $conn = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_NAME);
    
    if ($conn->connect_error) {
        die("Database Connection failed: " . $conn->connect_error);
    }
    
    return $conn;
}
?>