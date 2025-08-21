package util;

import config.AppConfig;

import java.sql.*;
import java.util.*;

/**
 * AdaptiveRegression - Provides salary prediction using regression analysis
 * Predicts employee salary based on worked hours and present days history
 */
public class AdaptiveRegression {

    public static double predictSalary(String empId) {
        try (Connection conn = DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USERNAME, AppConfig.DB_PASSWORD)) {
            String sql = "SELECT worked_hours, present_days, salary FROM salarysummary WHERE employee_id = ? ORDER BY year DESC, month DESC LIMIT 5";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, empId);
            ResultSet rs = ps.executeQuery();

            List<Double> x1 = new ArrayList<>();
            List<Double> x2 = new ArrayList<>();
            List<Double> y = new ArrayList<>();

            while (rs.next()) {
                x1.add(rs.getDouble("worked_hours"));
                x2.add(rs.getDouble("present_days"));
                y.add(rs.getDouble("salary"));
            }

            if (x1.size() >= 2) {
                double[] x1Arr = x1.stream().mapToDouble(Double::doubleValue).toArray();
                double[] x2Arr = x2.stream().mapToDouble(Double::doubleValue).toArray();
                double[] yArr = y.stream().mapToDouble(Double::doubleValue).toArray();

                RegressionModel model = new RegressionModel();
                model.fit(x1Arr, x2Arr, yArr);

                double avgHours = Arrays.stream(x1Arr).average().orElse(0);
                double avgDays = Arrays.stream(x2Arr).average().orElse(0);
                return model.predict(avgHours, avgDays);
            }
        } catch (Exception e) {
            System.err.println("Prediction error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * RegressionModel - Internal class for handling regression calculations
     * Supports both simple and multiple regression based on data variability
     */
    private static class RegressionModel {
        private double a = 0, b = 0, c = 0;
        private boolean useX1 = false, useX2 = false;

        public void fit(double[] x1, double[] x2, double[] y) {
            boolean x1Varies = !allEqual(x1);
            boolean x2Varies = !allEqual(x2);
            useX1 = x1Varies;
            useX2 = x2Varies;

            if (x1Varies && x2Varies) fitMultiple(x1, x2, y);
            else if (x1Varies) fitSimple(x1, y, true);
            else if (x2Varies) fitSimple(x2, y, false);
        }

        private void fitMultiple(double[] x1, double[] x2, double[] y) {
            int n = y.length;
            double sumX1 = 0, sumX2 = 0, sumY = 0;
            double sumX1X1 = 0, sumX2X2 = 0, sumX1X2 = 0;
            double sumX1Y = 0, sumX2Y = 0;

            for (int i = 0; i < n; i++) {
                sumX1 += x1[i];
                sumX2 += x2[i];
                sumY += y[i];
                sumX1X1 += x1[i] * x1[i];
                sumX2X2 += x2[i] * x2[i];
                sumX1X2 += x1[i] * x2[i];
                sumX1Y += x1[i] * y[i];
                sumX2Y += x2[i] * y[i];
            }

            double denom = sumX1X1 * sumX2X2 - sumX1X2 * sumX1X2;
            if (denom == 0) return;

            a = (sumX2X2 * sumX1Y - sumX1X2 * sumX2Y) / denom;
            b = (sumX1X1 * sumX2Y - sumX1X2 * sumX1Y) / denom;
            c = (sumY - a * sumX1 - b * sumX2) / n;
        }

        private void fitSimple(double[] x, double[] y, boolean isX1) {
            int n = x.length;
            double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

            for (int i = 0; i < n; i++) {
                sumX += x[i];
                sumY += y[i];
                sumXY += x[i] * y[i];
                sumXX += x[i] * x[i];
            }

            double xMean = sumX / n;
            double yMean = sumY / n;

            double slope = (sumXY - n * xMean * yMean) / (sumXX - n * xMean * xMean);
            double intercept = yMean - slope * xMean;

            if (isX1) { a = slope; b = 0; } 
            else { b = slope; a = 0; }
            c = intercept;
        }

        private boolean allEqual(double[] arr) {
            for (double v : arr) {
                if (v != arr[0]) return false;
            }
            return true;
        }

        public double predict(double x1, double x2) {
            return c + (useX1 ? a * x1 : 0) + (useX2 ? b * x2 : 0);
        }
    }
}