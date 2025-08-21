-- ==========================================
-- Database: Attendance Management System (AMS)
-- ==========================================

CREATE DATABASE ams;
USE ams;

-- ==========================
-- Employee Table
-- Stores employee details
-- ==========================
CREATE TABLE employee (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    e_name VARCHAR(100) NOT NULL,
    e_address VARCHAR(255),
    e_phone VARCHAR(10),
    e_email VARCHAR(100) UNIQUE,
    e_dob DATE,
    e_gender ENUM('Male', 'Female'),
    e_hiredate DATE DEFAULT CURRENT_DATE,
    e_rfid VARCHAR(10)
);

-- ==========================
-- Attendance Table
-- Tracks daily attendance
-- ==========================
CREATE TABLE attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT,
    date DATE NOT NULL,
    timeIn TIME NULL,
    timeOut TIME NULL,
    workedHour DECIMAL(5,2) DEFAULT 0.00,
    status ENUM('Present', 'Absent') DEFAULT 'Absent',
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- ==========================
-- Salary Summary Table
-- Monthly salary calculation
-- ==========================
CREATE TABLE salarysummary (
    summary_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    present_days INT DEFAULT 0,
    worked_hours DECIMAL(7,2) DEFAULT 0.00,
    salary DECIMAL(10,2) GENERATED ALWAYS AS (worked_hours * 320) STORED,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    UNIQUE KEY unique_summary (employee_id, year, month)
);

-- ==========================================
-- EVENTS
-- ==========================================

DELIMITER //

-- Event 1: Mark absent employees daily at 11 AM (except Sundays)
CREATE EVENT mark_absent_employees
ON SCHEDULE
    EVERY 1 DAY
    STARTS CURRENT_DATE + INTERVAL 11 HOUR
DO
BEGIN
    IF DAYOFWEEK(CURDATE()) != 7 THEN
        INSERT INTO attendance (employee_id, date)
        SELECT e.employee_id, CURDATE()
        FROM employee e
        WHERE e.employee_id NOT IN (
            SELECT a.employee_id
            FROM attendance a
            WHERE a.date = CURDATE()
        );
    END IF;
END;
//

-- Event 2: Auto set timeout at 5 PM if employee forgets to log out
CREATE EVENT auto_set_timeout
ON SCHEDULE EVERY 1 MINUTE
STARTS TIMESTAMP(CURRENT_DATE, '17:00:00')
DO
BEGIN
    -- Update only between 17:00 and 17:05 if no timeout recorded
    IF CURRENT_TIME BETWEEN '17:00:00' AND '17:05:00' THEN
        UPDATE attendance
        SET 
            timeOut = '17:00:00',
            workedHour = ROUND(TIME_TO_SEC(TIMEDIFF('17:00:00', timeIn)) / 3600, 2)
        WHERE 
            date = CURDATE()
            AND timeIn IS NOT NULL
            AND timeOut IS NULL;
    END IF;
END;
//

-- Event 3: Update monthly salary summary
CREATE EVENT update_salary_summary
ON SCHEDULE
    EVERY 1 MONTH
    STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY - INTERVAL DAY(CURRENT_DATE) DAY)
DO
BEGIN
    INSERT INTO salarysummary (employee_id, year, month, present_days, worked_hours, salary)
    SELECT 
        employee_id,
        YEAR(date) AS year,
        MONTH(date) AS month,
        COUNT(DISTINCT date) AS present_days,
        SUM(workedHour) AS worked_hours,
        SUM(workedHour) * 320 AS salary
    FROM attendance
    WHERE status = 'Present'
      AND date >= DATE_FORMAT(CURRENT_DATE - INTERVAL 1 MONTH, '%Y-%m-01')
      AND date < DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
    GROUP BY employee_id, year, month
    ON DUPLICATE KEY UPDATE 
        present_days = VALUES(present_days),
        worked_hours = VALUES(worked_hours),
        salary = VALUES(salary);
END;
//

DELIMITER ;
