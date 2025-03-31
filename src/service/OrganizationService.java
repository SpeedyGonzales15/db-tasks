package service;

import db.OrganizationDatabaseInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrganizationService {

    public void findSubordinatesOfIvanIvanov() {
        String sql = """
            WITH RECURSIVE Subordinates AS (
                SELECT
                    EmployeeID,
                    Name,
                    ManagerID,
                    DepartmentID,
                    RoleID
                FROM
                    Employees
                WHERE
                    EmployeeID = 1

                UNION ALL

                SELECT
                    e.EmployeeID,
                    e.Name,
                    e.ManagerID,
                    e.DepartmentID,
                    e.RoleID
                FROM
                    Employees e
                INNER JOIN
                    Subordinates s ON e.ManagerID = s.EmployeeID
            ),
            EmployeeDetails AS (
                SELECT
                    s.EmployeeID,
                    s.Name AS EmployeeName,
                    s.ManagerID,
                    d.DepartmentName,
                    r.RoleName,
                    STRING_AGG(DISTINCT p.ProjectName, ', ') AS Projects,
                    STRING_AGG(DISTINCT t.TaskName, ', ') AS Tasks
                FROM
                    Subordinates s
                LEFT JOIN
                    Departments d ON s.DepartmentID = d.DepartmentID
                LEFT JOIN
                    Roles r ON s.RoleID = r.RoleID
                LEFT JOIN
                    Projects p ON d.DepartmentID = p.DepartmentID
                LEFT JOIN
                    Tasks t ON s.EmployeeID = t.AssignedTo
                GROUP BY
                    s.EmployeeID, s.Name, s.ManagerID, d.DepartmentName, r.RoleName
            )
            SELECT
                EmployeeID,
                EmployeeName,
                ManagerID,
                DepartmentName,
                RoleName,
                Projects,
                Tasks
            FROM
                EmployeeDetails
            ORDER BY
                EmployeeName;
        """;

        try (Connection connection = OrganizationDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Сотрудники, подчиняющиеся Ивану Иванову:");
            System.out.println("ID\tИмя\tМенеджер\tОтдел\tРоль\tПроекты\tЗадачи");

            while (resultSet.next()) {
                int id = resultSet.getInt("EmployeeID");
                String name = resultSet.getString("EmployeeName");
                int managerId = resultSet.getInt("ManagerID");
                String department = resultSet.getString("DepartmentName");
                String role = resultSet.getString("RoleName");
                String projects = resultSet.getString("Projects");
                String tasks = resultSet.getString("Tasks");

                System.out.printf("%d\t%s\t%d\t%s\t%s\t%s\t%s\n", id, name, managerId, department, role, projects, tasks);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при выполнении запроса:");
            e.printStackTrace();
        }
    }
    
    public void findSubordinatesOfIvanIvanovWithDetails() {
        String sql = """
            WITH RECURSIVE Subordinates AS (
                SELECT
                    EmployeeID,
                    Name,
                    ManagerID,
                    DepartmentID,
                    RoleID
                FROM
                    Employees
                WHERE
                    EmployeeID = 1

                UNION ALL

                SELECT
                    e.EmployeeID,
                    e.Name,
                    e.ManagerID,
                    e.DepartmentID,
                    e.RoleID
                FROM
                    Employees e
                INNER JOIN
                    Subordinates s ON e.ManagerID = s.EmployeeID
            ),
            EmployeeDetails AS (
                SELECT
                    s.EmployeeID,
                    s.Name AS EmployeeName,
                    s.ManagerID,
                    d.DepartmentName,
                    r.RoleName,
                    STRING_AGG(DISTINCT p.ProjectName, ', ') AS Projects,
                    STRING_AGG(DISTINCT t.TaskName, ', ') AS Tasks,
                    COUNT(DISTINCT t.TaskID) AS TotalTasks,
                    COUNT(DISTINCT e.EmployeeID) AS TotalSubordinates
                FROM
                    Subordinates s
                LEFT JOIN
                    Departments d ON s.DepartmentID = d.DepartmentID
                LEFT JOIN
                    Roles r ON s.RoleID = r.RoleID
                LEFT JOIN
                    Projects p ON d.DepartmentID = p.DepartmentID
                LEFT JOIN
                    Tasks t ON s.EmployeeID = t.AssignedTo
                LEFT JOIN
                    Employees e ON s.EmployeeID = e.ManagerID
                GROUP BY
                    s.EmployeeID, s.Name, s.ManagerID, d.DepartmentName, r.RoleName
            )
            SELECT
                EmployeeID,
                EmployeeName,
                ManagerID,
                DepartmentName,
                RoleName,
                Projects,
                Tasks,
                TotalTasks,
                TotalSubordinates
            FROM
                EmployeeDetails
            ORDER BY
                EmployeeName;
        """;

        try (Connection connection = OrganizationDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Сотрудники, подчиняющиеся Ивану Иванову:");
            System.out.println("ID\tИмя\tМенеджер\tОтдел\tРоль\tПроекты\tЗадачи\tКоличество задач\tКоличество подчиненных");

            while (resultSet.next()) {
                int id = resultSet.getInt("EmployeeID");
                String name = resultSet.getString("EmployeeName");
                int managerId = resultSet.getInt("ManagerID");
                String department = resultSet.getString("DepartmentName");
                String role = resultSet.getString("RoleName");
                String projects = resultSet.getString("Projects");
                String tasks = resultSet.getString("Tasks");
                int totalTasks = resultSet.getInt("TotalTasks");
                int totalSubordinates = resultSet.getInt("TotalSubordinates");

                System.out.printf("%d\t%s\t%d\t%s\t%s\t%s\t%s\t%d\t%d\n", id, name, managerId, department, role, projects, tasks, totalTasks, totalSubordinates);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при выполнении запроса:");
            e.printStackTrace();
        }
    }
    
    public void findManagersWithSubordinates() {
        String sql = """
            WITH RECURSIVE Managers AS (
                SELECT
                    EmployeeID,
                    Name,
                    ManagerID,
                    DepartmentID,
                    RoleID
                FROM
                    Employees
                WHERE
                    RoleID = 1
            ),
            Subordinates AS (
                SELECT
                    m.EmployeeID AS ManagerID,
                    m.Name AS ManagerName,
                    e.EmployeeID,
                    e.Name,
                    e.ManagerID,
                    e.DepartmentID,
                    e.RoleID
                FROM
                    Managers m
                INNER JOIN
                    Employees e ON m.EmployeeID = e.ManagerID

                UNION ALL

                SELECT
                    s.ManagerID,
                    s.ManagerName,
                    e.EmployeeID,
                    e.Name,
                    e.ManagerID,
                    e.DepartmentID,
                    e.RoleID
                FROM
                    Employees e
                INNER JOIN
                    Subordinates s ON e.ManagerID = s.EmployeeID
            ),
            ManagerDetails AS (
                SELECT
                    m.EmployeeID,
                    m.Name AS EmployeeName,
                    m.ManagerID,
                    d.DepartmentName,
                    r.RoleName,
                    STRING_AGG(DISTINCT p.ProjectName, ', ') AS Projects,
                    STRING_AGG(DISTINCT t.TaskName, ', ') AS Tasks,
                    COUNT(DISTINCT s.EmployeeID) AS TotalSubordinates
                FROM
                    Managers m
                LEFT JOIN
                    Departments d ON m.DepartmentID = d.DepartmentID
                LEFT JOIN
                    Roles r ON m.RoleID = r.RoleID
                LEFT JOIN
                    Projects p ON d.DepartmentID = p.DepartmentID
                LEFT JOIN
                    Tasks t ON m.EmployeeID = t.AssignedTo
                LEFT JOIN
                    Subordinates s ON m.EmployeeID = s.ManagerID
                GROUP BY
                    m.EmployeeID, m.Name, m.ManagerID, d.DepartmentName, r.RoleName
            )
            SELECT
                EmployeeID,
                EmployeeName,
                ManagerID,
                DepartmentName,
                RoleName,
                Projects,
                Tasks,
                TotalSubordinates
            FROM
                ManagerDetails
            WHERE
                TotalSubordinates > 0
            ORDER BY
                EmployeeName;
        """;

        try (Connection connection = OrganizationDatabaseInitializer.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Менеджеры с подчиненными:");
            System.out.println("ID\tИмя\tМенеджер\tОтдел\tРоль\tПроекты\tЗадачи\tКоличество подчиненных");

            while (resultSet.next()) {
                int id = resultSet.getInt("EmployeeID");
                String name = resultSet.getString("EmployeeName");
                int managerId = resultSet.getInt("ManagerID");
                String department = resultSet.getString("DepartmentName");
                String role = resultSet.getString("RoleName");
                String projects = resultSet.getString("Projects");
                String tasks = resultSet.getString("Tasks");
                int totalSubordinates = resultSet.getInt("TotalSubordinates");

                System.out.printf("%d\t%s\t%d\t%s\t%s\t%s\t%s\t%d\n", id, name, managerId, department, role, projects, tasks, totalSubordinates);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при выполнении запроса:");
            e.printStackTrace();
        }
    }
}
