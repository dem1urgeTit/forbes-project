package com.forbes;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== АНАЛИЗ ДАННЫХ FORBES ===\n");

        try {
            // 1. Парсинг CSV файла
            System.out.println("1. Чтение данных из CSV файла...");
            CSVParser parser = new CSVParser();
            List<Billionaire> billionaires = parser.parseCSV("Forbes.csv");

            if (billionaires.isEmpty()) {
                System.out.println("ОШИБКА: Не удалось загрузить данные.");
                System.out.println("Проверьте наличие файла Forbes.csv в корне проекта.");
                return;
            }

            // 2. Создание базы данных
            System.out.println("\n2. Подключение к базе данных...");
            DatabaseManager dbManager = new DatabaseManager();

            // 3. Создание таблиц
            System.out.println("\n3. Создание таблиц в базе данных...");
            dbManager.createTables();

            // 4. Загрузка данных в БД
            System.out.println("\n4. Загрузка данных в базу данных...");
            dbManager.insertData(billionaires);

            // 5. Выполнение SQL запросов
            System.out.println("\n5. Выполнение аналитических запросов...");
            dbManager.executeQueries();

            // 6. Визуализация данных (передаем соединение из dbManager)
            System.out.println("\n6. Создание визуализаций...");
            System.out.println("Открываются окна с графиками.");

            // Создаем визуализатор с тем же соединением
            DataVisualizer visualizer = new DataVisualizer(dbManager.getConnection());
            visualizer.showAllCharts();

            // Ждем немного чтобы графики успели открыться
            Thread.sleep(1000);

            // 7. Закрытие соединений (после того как графики откроются)
            System.out.println("\n7. Завершение работы...");
            System.out.println("Закройте окна графиков для окончания программы.");

            // НЕ закрываем соединение здесь, оно закроется автоматически при выходе
            // Базу данных закрываем только после завершения работы с графиками

            // Ждем 10 секунд перед закрытием, чтобы пользователь успел посмотреть графики
            Thread.sleep(10000);

            // Закрываем соединение
            dbManager.close();

            System.out.println("\n=== ПРОГРАММА ЗАВЕРШЕНА ===");

        } catch (Exception e) {
            System.out.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}