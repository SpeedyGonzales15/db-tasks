package service;

import db.HotelDatabaseInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HotelService {

    public void findCustomersWithMultipleBookings() {
        String sql = """
            WITH CustomerBookings AS (
                SELECT
                    c.ID_customer,
                    c.name,
                    c.email,
                    c.phone,
                    COUNT(DISTINCT h.ID_hotel) AS hotel_count,
                    COUNT(b.ID_booking) AS booking_count,
                    STRING_AGG(h.name, ', ') AS hotels,
                    AVG(b.check_out_date - b.check_in_date) AS avg_stay_duration
                FROM
                    Customer c
                JOIN
                    Booking b ON c.ID_customer = b.ID_customer
                JOIN
                    Room r ON b.ID_room = r.ID_room
                JOIN
                    Hotel h ON r.ID_hotel = h.ID_hotel
                GROUP BY
                    c.ID_customer, c.name, c.email, c.phone
                HAVING
                    COUNT(DISTINCT h.ID_hotel) > 2
            )
            SELECT
                name,
                email,
                phone,
                booking_count,
                hotels,
                ROUND(avg_stay_duration, 1) AS avg_stay_duration
            FROM
                CustomerBookings
            ORDER BY
                booking_count DESC;
        """;

        try (Connection connection = HotelDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Клиенты, сделавшие более двух бронирований в разных отелях:");
            System.out.println("Имя\tЭлектронная почта\tТелефон\tКоличество бронирований\tОтели\tСредняя длительность пребывания");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                int bookingCount = resultSet.getInt("booking_count");
                String hotels = resultSet.getString("hotels");
                double avgStayDuration = resultSet.getDouble("avg_stay_duration");

                System.out.printf("%s\t%s\t%s\t%d\t%s\t%.1f%n", name, email, phone, bookingCount, hotels, avgStayDuration);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void findHighSpendingCustomers() {
        String sql = """
            WITH CustomerBookings AS (
                SELECT
                    c.ID_customer,
                    c.name,
                    COUNT(b.ID_booking) AS total_bookings,
                    COUNT(DISTINCT h.ID_hotel) AS unique_hotels,
                    SUM(r.price * (b.check_out_date - b.check_in_date)) AS total_spent
                FROM
                    Customer c
                JOIN
                    Booking b ON c.ID_customer = b.ID_customer
                JOIN
                    Room r ON b.ID_room = r.ID_room
                JOIN
                    Hotel h ON r.ID_hotel = h.ID_hotel
                GROUP BY
                    c.ID_customer, c.name
            ),
            HighSpenders AS (
                SELECT
                    ID_customer,
                    name,
                    total_spent,
                    total_bookings
                FROM
                    CustomerBookings
                WHERE
                    total_spent > 500
            ),
            MultiHotelBookers AS (
                SELECT
                    ID_customer,
                    name,
                    total_bookings,
                    unique_hotels,
                    total_spent
                FROM
                    CustomerBookings
                WHERE
                    total_bookings > 2 AND unique_hotels > 1
            )
            SELECT
                m.ID_customer,
                m.name,
                m.total_bookings,
                m.total_spent,
                m.unique_hotels
            FROM
                MultiHotelBookers m
            JOIN
                HighSpenders h ON m.ID_customer = h.ID_customer
            ORDER BY
                m.total_spent ASC;
        """;

        try (Connection connection = HotelDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Клиенты, сделавшие более двух бронирований в разных отелях и потратившие более 500 долларов:");
            System.out.println("ID\tИмя\tКоличество бронирований\tОбщая сумма\tУникальные отели");

            while (resultSet.next()) {
                int id = resultSet.getInt("ID_customer");
                String name = resultSet.getString("name");
                int totalBookings = resultSet.getInt("total_bookings");
                double totalSpent = resultSet.getDouble("total_spent");
                int uniqueHotels = resultSet.getInt("unique_hotels");

                System.out.printf("%d\t%s\t%d\t%.2f\t%d\n", id, name, totalBookings, totalSpent, uniqueHotels);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при выполнении запроса:");
            e.printStackTrace();
        }
    }
    
    public void analyzeCustomerPreferences() {
        String sql = """
            WITH HotelCategories AS (
                SELECT
                    h.ID_hotel,
                    h.name AS hotel_name,
                    CASE
                        WHEN AVG(r.price) < 175 THEN 'Дешевый'
                        WHEN AVG(r.price) BETWEEN 175 AND 300 THEN 'Средний'
                        ELSE 'Дорогой'
                    END AS hotel_category
                FROM
                    Hotel h
                JOIN
                    Room r ON h.ID_hotel = r.ID_hotel
                GROUP BY
                    h.ID_hotel, h.name
            ),
            CustomerPreferences AS (
                SELECT
                    c.ID_customer,
                    c.name AS customer_name,
                    CASE
                        WHEN BOOL_OR(hc.hotel_category = 'Дорогой') THEN 'Дорогой'
                        WHEN BOOL_OR(hc.hotel_category = 'Средний') THEN 'Средний'
                        ELSE 'Дешевый'
                    END AS preferred_hotel_type,
                    STRING_AGG(DISTINCT hc.hotel_name, ', ') AS visited_hotels
                FROM
                    Customer c
                JOIN
                    Booking b ON c.ID_customer = b.ID_customer
                JOIN
                    Room r ON b.ID_room = r.ID_room
                JOIN
                    Hotel h ON r.ID_hotel = h.ID_hotel
                JOIN
                    HotelCategories hc ON h.ID_hotel = hc.ID_hotel
                GROUP BY
                    c.ID_customer, c.name
            )
            SELECT
                ID_customer,
                customer_name,
                preferred_hotel_type,
                visited_hotels
            FROM
                CustomerPreferences
            ORDER BY
                CASE
                    WHEN preferred_hotel_type = 'Дешевый' THEN 1
                    WHEN preferred_hotel_type = 'Средний' THEN 2
                    ELSE 3
                END;
        """;

        try (Connection connection = HotelDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Анализ предпочтений клиентов по типу отелей:");
            System.out.println("ID\tИмя\tПредпочитаемый тип\tПосещенные отели");

            while (resultSet.next()) {
                int id = resultSet.getInt("ID_customer");
                String name = resultSet.getString("customer_name");
                String preferredType = resultSet.getString("preferred_hotel_type");
                String visitedHotels = resultSet.getString("visited_hotels");

                System.out.printf("%d\t%s\t%s\t%s\n", id, name, preferredType, visitedHotels);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при выполнении запроса:");
            e.printStackTrace();
        }
    }
}
