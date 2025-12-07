package com.forbes;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataVisualizer {
    private Connection connection;


    public DataVisualizer(Connection connection) {
        this.connection = connection;
    }

    public void showCountryWealthChart() {
        try {

            if (connection == null || connection.isClosed()) {
                System.out.println("Соединение с БД не активно для визуализации");
                return;
            }


            String query =
                    "SELECT c.name AS country, SUM(b.net_worth) AS total_wealth " +
                            "FROM billionaires b " +
                            "JOIN countries c ON b.country_id = c.id " +
                            "GROUP BY c.name " +
                            "ORDER BY total_wealth DESC " +
                            "LIMIT 10";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<String> countries = new ArrayList<>();
            List<Double> wealth = new ArrayList<>();

            while (rs.next()) {
                countries.add(rs.getString("country"));
                wealth.add(rs.getDouble("total_wealth"));
            }

            rs.close();
            stmt.close();


            JFrame frame = new JFrame("Общий капитал миллиардеров по странам");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);


            JPanel chartPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = getWidth();
                    int height = getHeight();
                    int padding = 50;
                    int chartWidth = width - 2 * padding;
                    int chartHeight = height - 2 * padding;


                    g2d.setFont(new Font("Arial", Font.BOLD, 18));
                    g2d.drawString("Топ-10 стран по общему капиталу миллиардеров", width/2 - 200, 30);


                    double maxWealth = wealth.stream().max(Double::compare).orElse(1.0);


                    g2d.drawLine(padding, height - padding, width - padding, height - padding); // X ось
                    g2d.drawLine(padding, padding, padding, height - padding); // Y ось


                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString("Страны", width/2, height - 10);
                    g2d.rotate(-Math.PI/2);
                    g2d.drawString("Капитал (млрд $)", -height/2, 20);
                    g2d.rotate(Math.PI/2);


                    int barWidth = chartWidth / (countries.size() * 2);
                    for (int i = 0; i < countries.size(); i++) {
                        int barHeight = (int) ((wealth.get(i) / maxWealth) * chartHeight);
                        int x = padding + i * (barWidth * 2);
                        int y = height - padding - barHeight;


                        g2d.setColor(new Color(70, 130, 180));
                        g2d.fillRect(x, y, barWidth, barHeight);


                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(x, y, barWidth, barHeight);


                        String country = countries.get(i);
                        if (country.length() > 10) {
                            country = country.substring(0, 10) + "...";
                        }
                        g2d.drawString(country, x, height - padding + 20);


                        String value = String.format("%.1f", wealth.get(i));
                        g2d.drawString(value, x, y - 5);
                    }


                    g2d.setColor(new Color(70, 130, 180));
                    g2d.fillRect(width - 150, 50, 20, 20);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(width - 150, 50, 20, 20);
                    g2d.drawString("Капитал", width - 120, 65);
                }
            };

            chartPanel.setBackground(Color.WHITE);
            frame.add(chartPanel);
            frame.setVisible(true);

        } catch (SQLException e) {
            System.out.println("Ошибка создания графика стран: " + e.getMessage());
        }
    }

    public void showIndustryDistribution() {
        try {

            if (connection == null || connection.isClosed()) {
                System.out.println("Соединение с БД не активно для визуализации");
                return;
            }


            String query =
                    "SELECT i.name AS industry, COUNT(*) as count, AVG(b.net_worth) as avg_wealth " +
                            "FROM billionaires b " +
                            "JOIN countries c ON b.country_id = c.id " +
                            "JOIN industries i ON b.industry_id = i.id " +
                            "WHERE c.name = 'United States' " +
                            "GROUP BY i.name " +
                            "ORDER BY count DESC " +
                            "LIMIT 10";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<String> industries = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            List<Double> avgWealth = new ArrayList<>();

            while (rs.next()) {
                industries.add(rs.getString("industry"));
                counts.add(rs.getInt("count"));
                avgWealth.add(rs.getDouble("avg_wealth"));
            }

            rs.close();
            stmt.close();


            JFrame frame = new JFrame("Распределение миллиардеров США по отраслям");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);

            JPanel chartPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = getWidth();
                    int height = getHeight();


                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    g2d.drawString("Топ-10 отраслей миллиардеров США", width/2 - 150, 30);


                    int centerX = width / 3;
                    int centerY = height / 2;
                    int radius = 200;


                    Color[] colors = {
                            new Color(255, 99, 132),   // красный
                            new Color(54, 162, 235),   // синий
                            new Color(255, 206, 86),   // желтый
                            new Color(75, 192, 192),   // бирюзовый
                            new Color(153, 102, 255),  // фиолетовый
                            new Color(255, 159, 64),   // оранжевый
                            new Color(199, 199, 199),  // серый
                            new Color(83, 102, 255),   // синий2
                            new Color(40, 159, 64),    // зеленый
                            new Color(210, 105, 30)    // коричневый
                    };

                    int totalCount = counts.stream().mapToInt(Integer::intValue).sum();
                    int startAngle = 0;


                    for (int i = 0; i < industries.size(); i++) {
                        int arcAngle = (int) (360 * counts.get(i) / (double) totalCount);
                        g2d.setColor(colors[i % colors.length]);
                        g2d.fillArc(centerX - radius, centerY - radius,
                                radius * 2, radius * 2,
                                startAngle, arcAngle);
                        g2d.setColor(Color.BLACK);
                        g2d.drawArc(centerX - radius, centerY - radius,
                                radius * 2, radius * 2,
                                startAngle, arcAngle);
                        startAngle += arcAngle;
                    }


                    int legendX = width * 2 / 3;
                    int legendY = 100;

                    for (int i = 0; i < industries.size(); i++) {
                        g2d.setColor(colors[i % colors.length]);
                        g2d.fillRect(legendX, legendY + i * 30, 20, 20);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(legendX, legendY + i * 30, 20, 20);

                        String label = industries.get(i) + " (" + counts.get(i) + " чел.)";
                        if (label.length() > 25) {
                            label = label.substring(0, 25) + "...";
                        }
                        g2d.drawString(label, legendX + 30, legendY + i * 30 + 15);
                    }


                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString("Всего миллиардеров в США: " + totalCount,
                            legendX, legendY + industries.size() * 30 + 30);

                    double maxAvg = avgWealth.stream().max(Double::compare).orElse(0.0);
                    int maxIndex = avgWealth.indexOf(maxAvg);
                    g2d.drawString("Самая высокая средняя капитализация: ",
                            legendX, legendY + industries.size() * 30 + 50);
                    g2d.drawString(industries.get(maxIndex) + " - $" + String.format("%.1f", maxAvg) + "B",
                            legendX + 20, legendY + industries.size() * 30 + 70);
                }
            };

            chartPanel.setBackground(Color.WHITE);
            frame.add(chartPanel);
            frame.setVisible(true);

        } catch (SQLException e) {
            System.out.println("Ошибка создания круговой диаграммы: " + e.getMessage());
        }
    }

    public void showTop10Billionaires() {
        try {

            if (connection == null || connection.isClosed()) {
                System.out.println("Соединение с БД не активно для визуализации");
                return;
            }


            String query =
                    "SELECT b.name, b.net_worth, b.age, c.name as country " +
                            "FROM billionaires b " +
                            "JOIN countries c ON b.country_id = c.id " +
                            "ORDER BY b.net_worth DESC " +
                            "LIMIT 10";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<String> names = new ArrayList<>();
            List<Double> wealth = new ArrayList<>();
            List<Integer> ages = new ArrayList<>();
            List<String> countries = new ArrayList<>();

            while (rs.next()) {
                names.add(rs.getString("name"));
                wealth.add(rs.getDouble("net_worth"));
                ages.add(rs.getInt("age"));
                countries.add(rs.getString("country"));
            }

            rs.close();
            stmt.close();


            JFrame frame = new JFrame("Топ-10 самых богатых миллиардеров мира");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            JPanel chartPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = getWidth();
                    int height = getHeight();
                    int padding = 80;


                    g2d.setFont(new Font("Arial", Font.BOLD, 20));
                    g2d.drawString("Топ-10 самых богатых миллиардеров мира", width/2 - 200, 40);


                    double maxWealth = wealth.stream().max(Double::compare).orElse(1.0);


                    int barHeight = 40;
                    int spaceBetweenBars = 10;
                    int totalBarHeight = (barHeight + spaceBetweenBars) * names.size();
                    int startY = (height - totalBarHeight) / 2;

                    for (int i = 0; i < names.size(); i++) {
                        int barLength = (int) ((wealth.get(i) / maxWealth) * (width - 2 * padding) * 0.9);
                        int y = startY + i * (barHeight + spaceBetweenBars);


                        Color barColor = getCountryColor(countries.get(i));
                        g2d.setColor(barColor);
                        g2d.fillRect(padding, y, barLength, barHeight);


                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(padding, y, barLength, barHeight);


                        String displayName = names.get(i);
                        if (displayName.length() > 30) {
                            displayName = displayName.substring(0, 30) + "...";
                        }

                        g2d.drawString((i+1) + ". " + displayName, padding + 5, y + barHeight/2 + 5);


                        String details = String.format("$%.1fB | %d лет | %s",
                                wealth.get(i), ages.get(i), countries.get(i));
                        g2d.drawString(details, padding + barLength + 10, y + barHeight/2 + 5);


                        if (barLength > 100) {
                            g2d.setColor(Color.WHITE);
                            g2d.drawString("$" + String.format("%.1f", wealth.get(i)) + "B",
                                    padding + barLength - 80, y + barHeight/2 + 5);
                        }
                    }


                    Map<String, Color> countryColors = new HashMap<>();
                    for (int i = 0; i < Math.min(countries.size(), 5); i++) {
                        countryColors.put(countries.get(i), getCountryColor(countries.get(i)));
                    }

                    int legendX = width - 200;
                    int legendY = 100;
                    int legendIndex = 0;

                    for (Map.Entry<String, Color> entry : countryColors.entrySet()) {
                        g2d.setColor(entry.getValue());
                        g2d.fillRect(legendX, legendY + legendIndex * 30, 20, 20);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(legendX, legendY + legendIndex * 30, 20, 20);

                        String country = entry.getKey();
                        if (country.length() > 15) {
                            country = country.substring(0, 15) + "...";
                        }
                        g2d.drawString(country, legendX + 30, legendY + legendIndex * 30 + 15);
                        legendIndex++;
                    }


                    g2d.setFont(new Font("Arial", Font.BOLD, 14));
                    double totalWealth = wealth.stream().mapToDouble(Double::doubleValue).sum();
                    g2d.drawString("Общий капитал топ-10: $" + String.format("%.1f", totalWealth) + "B",
                            legendX, legendY + legendIndex * 30 + 30);

                    double averageAge = ages.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                    g2d.drawString("Средний возраст: " + String.format("%.1f", averageAge) + " лет",
                            legendX, legendY + legendIndex * 30 + 60);
                }

                private Color getCountryColor(String country) {
                    int hash = country.hashCode();
                    return new Color(
                            Math.abs(hash % 200) + 55,
                            Math.abs((hash / 100) % 200) + 55,
                            Math.abs((hash / 10000) % 200) + 55
                    );
                }
            };

            chartPanel.setBackground(Color.WHITE);
            frame.add(chartPanel);
            frame.setVisible(true);

        } catch (SQLException e) {
            System.out.println("Ошибка создания графика топ-10: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showAllCharts() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            showCountryWealthChart();
            try { Thread.sleep(300); } catch (InterruptedException e) {}

            showIndustryDistribution();
            try { Thread.sleep(300); } catch (InterruptedException e) {}

            showTop10Billionaires();
        });
    }
}