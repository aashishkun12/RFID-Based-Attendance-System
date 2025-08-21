<?php

require_once 'db_config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $rfid = $_POST['rfid'];

    $conn = getDBConnection();

    $query = "SELECT employee_id, e_name FROM employee WHERE e_rfid = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $rfid);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $employeeId = $row['employee_id'];
        $employeeName = $row['e_name'];

        date_default_timezone_set(TIMEZONE);
        $currentDate = date('Y-m-d');
        $currentTime = date('H:i:s');
        $currentHourMin = date('H:i');

        $checkQuery = "SELECT * FROM attendance WHERE employee_id = ? AND date = ?";
        $checkStmt = $conn->prepare($checkQuery);
        $checkStmt->bind_param("is", $employeeId, $currentDate);
        $checkStmt->execute();
        $attendanceResult = $checkStmt->get_result();

        if ($attendanceResult->num_rows === 0) {
            // Clock-in only allowed between 10:00 and 11:00
            if ($currentHourMin >= '10:00' && $currentHourMin <= '11:00') {
                $insertQuery = "INSERT INTO attendance (employee_id, date, timeIn, status)
                                VALUES (?, ?, ?, 'Present')";
                $insertStmt = $conn->prepare($insertQuery);
                $insertStmt->bind_param("iss", $employeeId, $currentDate, $currentTime);
                $insertStmt->execute();

                echo "Welcome $employeeName! Time In recorded at $currentTime";
            } else {
                echo "Sorry $employeeName, you can only clock in between 10:00 AM and 11:00 AM.";
            }
        } else {
            // Handle possible clock-out
            $attendanceRow = $attendanceResult->fetch_assoc();

            if ($attendanceRow['status'] == 'Present' && $attendanceRow['timeOut'] == null) {
                $timeIn = strtotime($attendanceRow['timeIn']);
                $now = strtotime($currentTime);
                $minWait = 10 * 60;

                if ($now < $timeIn + $minWait) {
                    echo "⏳ Please wait at least 10 minutes after Time In before clocking out.";
                } elseif ($currentHourMin > '17:00') {
                    echo "You are already clock out at 5:00 PM.";
                } else {
                    $workedHours = round(($now - $timeIn) / 3600, 2);

                    $updateQuery = "UPDATE attendance SET timeOut = ?, workedHour = ? 
                                    WHERE employee_id = ? AND date = ?";
                    $updateStmt = $conn->prepare($updateQuery);
                    $updateStmt->bind_param("sdis", $currentTime, $workedHours, $employeeId, $currentDate);
                    $updateStmt->execute();

                    echo "Goodbye $employeeName! Time Out recorded at $currentTime. Worked $workedHours hours.";
                }
            } else {
                echo "Hi $employeeName! You've already clocked in and out today.";
            }
        }
    } else {
        echo "RFID: $rfid not found in employee database.";
    }

    $conn->close();
} else {
    echo "Only POST requests allowed.";
}
?>