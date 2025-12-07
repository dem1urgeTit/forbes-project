package com.forbes;

import java.sql.*;
import java.util.List;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:forbes.db");
            System.out.println("База данных подключена успешно!");
        } catch (Exception e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    // Новый метод для получения соединения
    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        try {
            Statement stmt = connection.createStatement();

            // Удаляем старые таблицы, если они существуют
            stmt.execute("DROP TABLE IF EXISTS billionaires");
            stmt.execute("DROP TABLE IF EXISTS countries");
            stmt.execute("DROP TABLE IF EXISTS industries");

            // Создаем таблицу стран
            String createCountries =
                    "CREATE TABLE countries (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    name TEXT UNIQUE NOT NULL" +
                            ")";
            stmt.execute(createCountries);

            // Создаем таблицу отраслей
            String createIndustries =
                    "CREATE TABLE industries (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    name TEXT UNIQUE NOT NULL" +
                            ")";
            stmt.execute(createIndustries);

            // Создаем таблицу миллиардеров
            String createBillionaires =
                    "CREATE TABLE billionaires (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    rank INTEGER NOT NULL," +
                            "    name TEXT NOT NULL," +
                            "    net_worth REAL NOT NULL," +
                            "    age INTEGER," +
                            "    country_id INTEGER," +
                            "    source TEXT," +
                            "    industry_id INTEGER," +
                            "    FOREIGN KEY (country_id) REFERENCES countries(id)," +
                            "    FOREIGN KEY (industry_id) REFERENCES industries(id)" +
                            ")";
            stmt.execute(createBillionaires);

            stmt.close();
            System.out.println("Таблицы созданы успешно!");

        } catch (SQLException e) {
            System.out.println("Ошибка создания таблиц: " + e.getMessage());
        }
    }

    public void insertData(List<Billionaire> billionaires) {
        if (billionaires.isEmpty()) {
            System.out.println("Нет данных для вставки");
            return;
        }

        try {
            // Подготовка запросов
            String insertCountry = "INSERT OR IGNORE INTO countries (name) VALUES (?)";
            String insertIndustry = "INSERT OR IGNORE INTO industries (name) VALUES (?)";
            String insertBillionaire =
                    "INSERT INTO billionaires (rank, name, net_worth, age, country_id, source, industry_id) " +
                            "VALUES (?, ?, ?, ?, " +
                            "(SELECT id FROM countries WHERE name = ?), " +
                            "?, " +
                            "(SELECT id FROM industries WHERE name = ?))";

            PreparedStatement countryStmt = connection.prepareStatement(insertCountry);
            PreparedStatement industryStmt = connection.prepareStatement(insertIndustry);
            PreparedStatement billionaireStmt = connection.prepareStatement(insertBillionaire);

            // Вставляем уникальные страны и отрасли
            for (Billionaire b : billionaires) {
                countryStmt.setString(1, b.getCountry());
                countryStmt.addBatch();

                industryStmt.setString(1, b.getIndustry());
                industryStmt.addBatch();
            }

            countryStmt.executeBatch();
            industryStmt.executeBatch();

            // Вставляем миллиардеров
            for (Billionaire b : billionaires) {
                billionaireStmt.setInt(1, b.getRank());
                billionaireStmt.setString(2, b.getName());
                billionaireStmt.setDouble(3, b.getNetWorth());
                billionaireStmt.setInt(4, b.getAge());
                billionaireStmt.setString(5, b.getCountry());
                billionaireStmt.setString(6, b.getSource());
                billionaireStmt.setString(7, b.getIndustry());
                billionaireStmt.addBatch();
            }

            billionaireStmt.executeBatch();

            // Закрываем statements
            countryStmt.close();
            industryStmt.close();
            billionaireStmt.close();

            System.out.println("Данные успешно загружены в базу данных!");

        } catch (SQLException e) {
            System.out.println("Ошибка вставки данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void executeQueries() {
        System.out.println("\n=== РЕЗУЛЬТАТЫ ЗАПРОСОВ ===");

        // Запрос 1: Общий капитал по странам
        System.out.println("\n1. Общий капитал по странам (топ-10):");
        String query1 =
                "SELECT c.name AS Страна, " +
                        "ROUND(SUM(b.net_worth), 1) AS 'Общий капитал (млрд $)' " +
                        "FROM billionaires b " +
                        "JOIN countries c ON b.country_id = c.id " +
                        "GROUP BY c.name " +
                        "ORDER BY SUM(b.net_worth) DESC " +
                        "LIMIT 10";
        printQueryResult(query1);

        // Запрос 2: Самый молодой миллиардер из Франции
        System.out.println("\n2. Самый молодой миллиардер из Франции с капиталом > 10 млрд:");
        String query2 =
                "SELECT b.name AS Имя, " +
                        "b.net_worth AS 'Капитал (млрд $)', " +
                        "b.age AS Возраст, " +
                        "b.source AS Источник " +
                        "FROM billionaires b " +
                        "JOIN countries c ON b.country_id = c.id " +
                        "WHERE c.name = 'France' AND b.net_worth > 10 " +
                        "ORDER BY b.age ASC " +
                        "LIMIT 1";
        printQueryResult(query2);

        // Запрос 3: Самый богатый американец в сфере Energy
        System.out.println("\n3. Бизнесмен из США с самым большим капиталом в сфере Energy:");
        String query3 =
                "SELECT b.name AS Имя, " +
                        "b.net_worth AS 'Капитал (млрд $)', " +
                        "b.source AS Источник " +
                        "FROM billionaires b " +
                        "JOIN countries c ON b.country_id = c.id " +
                        "JOIN industries i ON b.industry_id = i.id " +
                        "WHERE c.name = 'United States' AND i.name = 'Energy' " +
                        "ORDER BY b.net_worth DESC " +
                        "LIMIT 1";
        printQueryResult(query3);
    }

    private void printQueryResult(String query) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Выводим заголовки
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-30s", metaData.getColumnName(i));
            }
            System.out.println();

            // Выводим разделитель
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-30s", "------------------------------");
            }
            System.out.println();

            // Выводим данные
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.printf("%-30s", rs.getString(i));
                }
                System.out.println();
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.out.println("Ошибка выполнения запроса: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с базой данных закрыто.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}