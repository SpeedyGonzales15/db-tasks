package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import config.DatabaseConfig;

public class HotelDatabaseInitializer {

    private static boolean isInitialized = false;

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
                DatabaseConfig.getDbUrl(),
                DatabaseConfig.getDbUser(),
                DatabaseConfig.getDbPassword()
        );

        if (!isInitialized) {
            initializeDatabase(connection);
            isInitialized = true;
        }

        return connection;
    }

    private static void initializeDatabase(Connection connection) {
        String createTablesScript = """
            -- Создание таблицы Hotel
            CREATE TABLE IF NOT EXISTS Hotel (
                ID_hotel SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                location VARCHAR(255) NOT NULL
            );

            -- Создание таблицы Room
            CREATE TABLE IF NOT EXISTS Room (
                ID_room SERIAL PRIMARY KEY,
                ID_hotel INT,
                room_type VARCHAR(20) NOT NULL CHECK (room_type IN ('Single', 'Double', 'Suite')),
                price DECIMAL(10, 2) NOT NULL,
                capacity INT NOT NULL,
                FOREIGN KEY (ID_hotel) REFERENCES Hotel(ID_hotel)
            );

            -- Создание таблицы Customer
            CREATE TABLE IF NOT EXISTS Customer (
                ID_customer SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                phone VARCHAR(20) NOT NULL
            );

            -- Создание таблицы Booking
            CREATE TABLE IF NOT EXISTS Booking (
                ID_booking SERIAL PRIMARY KEY,
                ID_room INT,
                ID_customer INT,
                check_in_date DATE NOT NULL,
                check_out_date DATE NOT NULL,
                FOREIGN KEY (ID_room) REFERENCES Room(ID_room),
                FOREIGN KEY (ID_customer) REFERENCES Customer(ID_customer)
            );
        """;

        String populateTablesScript = """
            -- Вставка данных в таблицу Hotel
            INSERT INTO Hotel (ID_hotel, name, location) VALUES
            (1, 'Grand Hotel', 'Paris, France'),
            (2, 'Ocean View Resort', 'Miami, USA'),
            (3, 'Mountain Retreat', 'Aspen, USA'),
            (4, 'City Center Inn', 'New York, USA'),
            (5, 'Desert Oasis', 'Las Vegas, USA'),
            (6, 'Lakeside Lodge', 'Lake Tahoe, USA'),
            (7, 'Historic Castle', 'Edinburgh, Scotland'),
            (8, 'Tropical Paradise', 'Bali, Indonesia'),
            (9, 'Business Suites', 'Tokyo, Japan'),
            (10, 'Eco-Friendly Hotel', 'Copenhagen, Denmark');

            -- Вставка данных в таблицу Room
            INSERT INTO Room (ID_room, ID_hotel, room_type, price, capacity) VALUES
            (1, 1, 'Single', 150.00, 1),
            (2, 1, 'Double', 200.00, 2),
            (3, 1, 'Suite', 350.00, 4),
            (4, 2, 'Single', 120.00, 1),
            (5, 2, 'Double', 180.00, 2),
            (6, 2, 'Suite', 300.00, 4),
            (7, 3, 'Double', 250.00, 2),
            (8, 3, 'Suite', 400.00, 4),
            (9, 4, 'Single', 100.00, 1),
            (10, 4, 'Double', 150.00, 2),
            (11, 5, 'Single', 90.00, 1),
            (12, 5, 'Double', 140.00, 2),
            (13, 6, 'Suite', 280.00, 4),
            (14, 7, 'Double', 220.00, 2),
            (15, 8, 'Single', 130.00, 1),
            (16, 8, 'Double', 190.00, 2),
            (17, 9, 'Suite', 360.00, 4),
            (18, 10, 'Single', 110.00, 1),
            (19, 10, 'Double', 160.00, 2);

            -- Вставка данных в таблицу Customer
            INSERT INTO Customer (ID_customer, name, email, phone) VALUES
            (1, 'John Doe', 'john.doe@example.com', '+1234567890'),
            (2, 'Jane Smith', 'jane.smith@example.com', '+0987654321'),
            (3, 'Alice Johnson', 'alice.johnson@example.com', '+1122334455'),
            (4, 'Bob Brown', 'bob.brown@example.com', '+2233445566'),
            (5, 'Charlie White', 'charlie.white@example.com', '+3344556677'),
            (6, 'Diana Prince', 'diana.prince@example.com', '+4455667788'),
            (7, 'Ethan Hunt', 'ethan.hunt@example.com', '+5566778899'),
            (8, 'Fiona Apple', 'fiona.apple@example.com', '+6677889900'),
            (9, 'George Washington', 'george.washington@example.com', '+7788990011'),
            (10, 'Hannah Montana', 'hannah.montana@example.com', '+8899001122');

            -- Вставка данных в таблицу Booking
            INSERT INTO Booking (ID_booking, ID_room, ID_customer, check_in_date, check_out_date) VALUES
            (1, 1, 1, '2025-05-01', '2025-05-05'),
            (2, 2, 2, '2025-05-02', '2025-05-06'),
            (3, 3, 3, '2025-05-03', '2025-05-07'),
            (4, 4, 4, '2025-05-04', '2025-05-08'),
            (5, 5, 5, '2025-05-05', '2025-05-09'),
            (6, 6, 6, '2025-05-06', '2025-05-10'),
            (7, 7, 7, '2025-05-07', '2025-05-11'),
            (8, 8, 8, '2025-05-08', '2025-05-12'),
            (9, 9, 9, '2025-05-09', '2025-05-13'),
            (10, 10, 10, '2025-05-10', '2025-05-14');
        """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTablesScript);

            statement.executeUpdate(populateTablesScript);

            System.out.println("База данных для отелей успешно инициализирована.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
