package service;

import db.VehicleDatabaseInitializer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleService {

    public void findSportMotorcycles() {
        String sql = "SELECT v.maker, v.model " +
                     "FROM Vehicle v " +
                     "JOIN Motorcycle m ON v.model = m.model " +
                     "WHERE m.horsepower > 150 AND m.price < 20000 AND m.type = 'Sport' " +
                     "ORDER BY m.horsepower DESC";

        try (Connection connection = VehicleDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String maker = resultSet.getString("maker");
                String model = resultSet.getString("model");
                System.out.println("Производитель: " + maker + ", Модель: " + model);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void findVehiclesByCriteria() {
        String sql = "SELECT v.maker, c.model, c.horsepower, c.engine_capacity, 'Car' AS type " +
                     "FROM Vehicle v JOIN Car c ON v.model = c.model " +
                     "WHERE c.horsepower > 150 AND c.engine_capacity < 3.0 AND c.price < 35000 " +
                     "UNION ALL " +
                     "SELECT v.maker, m.model, m.horsepower, m.engine_capacity, 'Motorcycle' AS type " +
                     "FROM Vehicle v JOIN Motorcycle m ON v.model = m.model " +
                     "WHERE m.horsepower > 150 AND m.engine_capacity < 1.5 AND m.price < 20000 " +
                     "UNION ALL " +
                     "SELECT v.maker, b.model, NULL AS horsepower, NULL AS engine_capacity, 'Bicycle' AS type " +
                     "FROM Vehicle v JOIN Bicycle b ON v.model = b.model " +
                     "WHERE b.gear_count > 18 AND b.price < 4000 " +
                     "ORDER BY horsepower DESC NULLS LAST";

        try (Connection connection = VehicleDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String maker = resultSet.getString("maker");
                String model = resultSet.getString("model");
                Integer horsepower = resultSet.getInt("horsepower");
                Double engineCapacity = resultSet.getDouble("engine_capacity");
                String type = resultSet.getString("type");

                System.out.printf(
                    "Производитель: %s, Модель: %s, Мощность: %s, Объем двигателя: %s, Тип: %s%n",
                    maker, model,
                    (horsepower != null) ? horsepower : "N/A",
                    (engineCapacity != null) ? engineCapacity : "N/A",
                    type
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
