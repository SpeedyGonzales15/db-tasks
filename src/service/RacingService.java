package service;

import db.RacingDatabaseInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RacingService {

    public void findCarsWithMinAvgPosition() {
        String sql = """
            WITH AvgPosition AS (
                SELECT
                    c.class,
                    c.name AS car,
                    AVG(r.position) AS avg_position,
                    COUNT(r.race) AS race_count
                FROM
                    Cars c
                JOIN
                    Results r ON c.name = r.car
                GROUP BY
                    c.class, c.name
            ),
            MinAvgPosition AS (
                SELECT
                    class,
                    MIN(avg_position) AS min_avg_position
                FROM
                    AvgPosition
                GROUP BY
                    class
            )
            SELECT
                a.class,
                a.car,
                a.avg_position,
                a.race_count
            FROM
                AvgPosition a
            JOIN
                MinAvgPosition m ON a.class = m.class AND a.avg_position = m.min_avg_position
            ORDER BY
                a.avg_position;
        """;

        try (Connection connection = RacingDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Автомобили с наименьшей средней позицией в гонках:");
            System.out.println("Класс\t\tАвтомобиль\tСредняя позиция\tКоличество гонок");

            while (resultSet.next()) {
                String carClass = resultSet.getString("class");
                String car = resultSet.getString("car");
                double avgPosition = resultSet.getDouble("avg_position");
                int raceCount = resultSet.getInt("race_count");

                System.out.printf("%s\t%s\t%.1f\t\t%d%n", carClass, car, avgPosition, raceCount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void findCarWithMinAvgPosition() {
        String sql = """
            WITH AvgPosition AS (
                SELECT
                    c.name AS car,
                    c.class,
                    AVG(r.position) AS avg_position,
                    COUNT(r.race) AS race_count
                FROM
                    Cars c
                JOIN
                    Results r ON c.name = r.car
                GROUP BY
                    c.name, c.class
            ),
            MinAvgPosition AS (
                SELECT
                    car,
                    class,
                    avg_position,
                    race_count,
                    ROW_NUMBER() OVER (ORDER BY avg_position, car) AS row_num
                FROM
                    AvgPosition
            )
            SELECT
                m.car,
                m.class,
                m.avg_position,
                m.race_count,
                cl.country
            FROM
                MinAvgPosition m
            JOIN
                Classes cl ON m.class = cl.class
            WHERE
                m.row_num = 1;
        """;

        try (Connection connection = RacingDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Автомобиль с наименьшей средней позицией в гонках:");
            System.out.println("Автомобиль\tКласс\tСредняя позиция\tКоличество гонок\tСтрана");

            while (resultSet.next()) {
                String car = resultSet.getString("car");
                String carClass = resultSet.getString("class");
                double avgPosition = resultSet.getDouble("avg_position");
                int raceCount = resultSet.getInt("race_count");
                String country = resultSet.getString("country");

                System.out.printf("%s\t%s\t%.1f\t\t%d\t\t%s%n", car, carClass, avgPosition, raceCount, country);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void findCarsInClassesWithMinAvgPosition() {
        String sql = """
            WITH ClassAvgPosition AS (
                SELECT
                    c.class,
                    AVG(r.position) AS avg_position,
                    COUNT(r.race) AS total_races
                FROM
                    Cars c
                JOIN
                    Results r ON c.name = r.car
                GROUP BY
                    c.class
            ),
            MinClassAvgPosition AS (
                SELECT
                    class,
                    avg_position,
                    total_races
                FROM
                    ClassAvgPosition
                WHERE
                    avg_position = (SELECT MIN(avg_position) FROM ClassAvgPosition)
            )
            SELECT
                c.name AS car,
                m.class,
                AVG(r.position) AS avg_position,
                COUNT(r.race) AS race_count,
                cl.country,
                m.total_races AS total_class_races
            FROM
                Cars c
            JOIN
                Results r ON c.name = r.car
            JOIN
                Classes cl ON c.class = cl.class
            JOIN
                MinClassAvgPosition m ON c.class = m.class
            GROUP BY
                c.name, m.class, cl.country, m.total_races
            ORDER BY
                m.class, c.name;
        """;

        try (Connection connection = RacingDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Автомобили из классов с наименьшей средней позицией в гонках:");
            System.out.println("Автомобиль\tКласс\tСредняя позиция\tКоличество гонок\tСтрана\tОбщее количество гонок");

            while (resultSet.next()) {
                String car = resultSet.getString("car");
                String carClass = resultSet.getString("class");
                double avgPosition = resultSet.getDouble("avg_position");
                int raceCount = resultSet.getInt("race_count");
                String country = resultSet.getString("country");
                int totalClassRaces = resultSet.getInt("total_class_races");

                System.out.printf("%s\t%s\t%.1f\t\t%d\t\t%s\t\t%d%n", car, carClass, avgPosition, raceCount, country, totalClassRaces);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void findCarsBetterThanClassAvg() {
        String sql = """
            WITH CarAvgPosition AS (
                SELECT
                    c.name AS car,
                    c.class,
                    AVG(r.position) AS avg_position,
                    COUNT(r.race) AS race_count
                FROM
                    Cars c
                JOIN
                    Results r ON c.name = r.car
                GROUP BY
                    c.name, c.class
            ),
            ClassAvgPosition AS (
                SELECT
                    class,
                    AVG(position) AS class_avg_position,
                    COUNT(DISTINCT c.name) AS car_count
                FROM
                    Cars c
                JOIN
                    Results r ON c.name = r.car
                GROUP BY
                    class
            )
            SELECT
                cp.car,
                cp.class,
                cp.avg_position,
                cp.race_count,
                cl.country
            FROM
                CarAvgPosition cp
            JOIN
                Classes cl ON cp.class = cl.class
            JOIN
                ClassAvgPosition cap ON cp.class = cap.class
            WHERE
                cp.avg_position < cap.class_avg_position
                AND cap.car_count > 1
            ORDER BY
                cp.class, cp.avg_position;
        """;

        try (Connection connection = RacingDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Автомобили с позицией лучше средней в их классе:");
            System.out.println("Автомобиль\tКласс\tСредняя позиция\tКоличество гонок\tСтрана");

            while (resultSet.next()) {
                String car = resultSet.getString("car");
                String carClass = resultSet.getString("class");
                double avgPosition = resultSet.getDouble("avg_position");
                int raceCount = resultSet.getInt("race_count");
                String country = resultSet.getString("country");

                System.out.printf("%s\t%s\t%.1f\t\t%d\t\t%s%n", car, carClass, avgPosition, raceCount, country);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void findClassesWithMostLowPositionCars() {
        String sql = """
            WITH CarAvgPosition AS (
                SELECT
                    c.name AS car,
                    c.class,
                    AVG(r.position) AS avg_position,
                    COUNT(r.race) AS race_count
                FROM
                    Cars c
                JOIN
                    Results r ON c.name = r.car
                GROUP BY
                    c.name, c.class
            ),
            LowPositionCars AS (
                SELECT
                    car,
                    class,
                    avg_position,
                    race_count
                FROM
                    CarAvgPosition
                WHERE
                    avg_position > 3.0
            ),
            ClassLowPositionCount AS (
                SELECT
                    class,
                    COUNT(car) AS low_position_car_count
                FROM
                    LowPositionCars
                GROUP BY
                    class
            ),
            MaxLowPositionClasses AS (
                SELECT
                    class
                FROM
                    ClassLowPositionCount
                WHERE
                    low_position_car_count = (SELECT MAX(low_position_car_count) FROM ClassLowPositionCount)
            )
            SELECT
                lpc.car,
                lpc.class,
                lpc.avg_position,
                lpc.race_count,
                cl.country,
                COUNT(r.race) AS total_class_races
            FROM
                LowPositionCars lpc
            JOIN
                Classes cl ON lpc.class = cl.class
            JOIN
                Results r ON lpc.car = r.car
            JOIN
                MaxLowPositionClasses mlpc ON lpc.class = mlpc.class
            GROUP BY
                lpc.car, lpc.class, lpc.avg_position, lpc.race_count, cl.country
            ORDER BY
                (SELECT low_position_car_count FROM ClassLowPositionCount WHERE class = lpc.class) DESC;
        """;

        try (Connection connection = RacingDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Классы с наибольшим количеством автомобилей с низкой средней позицией:");
            System.out.println("Автомобиль\tКласс\tСредняя позиция\tКоличество гонок\tСтрана\tОбщее количество гонок");

            while (resultSet.next()) {
                String car = resultSet.getString("car");
                String carClass = resultSet.getString("class");
                double avgPosition = resultSet.getDouble("avg_position");
                int raceCount = resultSet.getInt("race_count");
                String country = resultSet.getString("country");
                int totalClassRaces = resultSet.getInt("total_class_races");

                System.out.printf("%s\t%s\t%.1f\t\t%d\t\t%s\t\t%d%n", car, carClass, avgPosition, raceCount, country, totalClassRaces);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
