import db.HotelDatabaseInitializer;
import db.OrganizationDatabaseInitializer;
import db.RacingDatabaseInitializer;
import db.VehicleDatabaseInitializer;
import service.HotelService;
import service.OrganizationService;
import service.RacingService;
import service.VehicleService;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Инициализация базы данных для транспортных средств
        try (Connection vehicleConnection = VehicleDatabaseInitializer.getConnection()) {
            VehicleService vehicleService = new VehicleService();

            // Выполнение задачи 1 (спортивные мотоциклы)
            System.out.println("Результаты задачи 1 (Спортивные мотоциклы):");
            vehicleService.findSportMotorcycles();

            // Выполнение задачи 2 (транспортные средства по критериям)
            System.out.println("\nРезультаты задачи 2 (Транспортные средства по критериям):");
            vehicleService.findVehiclesByCriteria();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Инициализация базы данных для гонок
        try (Connection racingConnection = RacingDatabaseInitializer.getConnection()) {
            RacingService racingService = new RacingService();

            // Выполнение задачи 1 (автомобили с наименьшей средней позицией в гонках)
            System.out.println("\nРезультаты новой задачи (Автомобили с наименьшей средней позицией в гонках):");
            racingService.findCarsWithMinAvgPosition();
            
            // Выполнение задачи 2 (автомобиль с наименьшей средней позицией)
            System.out.println("\nРезультаты задачи 2 (Автомобиль с наименьшей средней позицией):");
            racingService.findCarWithMinAvgPosition();
            
         // Выполнение задачи 3 (автомобили из классов с наименьшей средней позицией)
            System.out.println("\nРезультаты задачи 3 (Автомобили из классов с наименьшей средней позицией):");
            racingService.findCarsInClassesWithMinAvgPosition();
            
         // Выполнение задачи 4 (автомобили с позицией лучше средней в их классе)
            System.out.println("\nРезультаты задачи 4 (Автомобили с позицией лучше средней в их классе):");
            racingService.findCarsBetterThanClassAvg();
            
         // Выполнение задачи 5 (классы с наибольшим количеством автомобилей с низкой средней позицией)
            System.out.println("\nРезультаты задачи 5 (Классы с наибольшим количеством автомобилей с низкой средней позицией):");
            racingService.findClassesWithMostLowPositionCars();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (Connection hotelConnection = HotelDatabaseInitializer.getConnection()) {
            HotelService hotelService = new HotelService();

            // Выполнение задачи 1 (клиенты с более чем двумя бронированиями в разных отелях)
            System.out.println("Результаты задачи 1 (Клиенты с более чем двумя бронированиями в разных отелях):");
            hotelService.findCustomersWithMultipleBookings();
            
            // Выполнение задачи 2 (клиенты, сделавшие более двух бронирований и потратившие более 500 долларов)
            System.out.println("\nРезультаты задачи 2 (Клиенты, сделавшие более двух бронирований и потратившие более 500 долларов):");
            hotelService.findHighSpendingCustomers();
            
         // Выполнение задачи 3 (анализ предпочтений клиентов по типу отелей)
            System.out.println("\nРезультаты задачи 3 (Анализ предпочтений клиентов по типу отелей):");
            hotelService.analyzeCustomerPreferences();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (Connection connection = OrganizationDatabaseInitializer.getConnection()) {
        	OrganizationService organizationService = new OrganizationService();

            // Выполнение задачи 1 (сотрудники, подчиняющиеся Ивану Иванову)
            System.out.println("\nРезультаты задачи 1 (Сотрудники, подчиняющиеся Ивану Иванову):");
            organizationService.findSubordinatesOfIvanIvanov();
            
            // Выполнение задачи 2 (сотрудники, подчиняющиеся Ивану Иванову, с деталями)
            System.out.println("\nРезультаты задачи 2 (Сотрудники, подчиняющиеся Ивану Иванову, с деталями):");
            organizationService.findSubordinatesOfIvanIvanovWithDetails();
            
            // Выполнение задачи 3 (менеджеры с подчиненными)
            System.out.println("\nРезультаты задачи 3 (Менеджеры с подчиненными):");
            organizationService.findManagersWithSubordinates();

        } catch (SQLException e) {
            System.err.println("Ошибка при работе с базой данных:");
            e.printStackTrace();
        }
    
    }
}
