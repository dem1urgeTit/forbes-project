package com.forbes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    public List<Billionaire> parseCSV(String filename) {
        List<Billionaire> billionaires = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                Billionaire billionaire = parseLine(line);
                if (billionaire != null) {
                    billionaires.add(billionaire);
                }
            }

            System.out.println("Загружено " + billionaires.size() + " записей из CSV");

        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
        }

        return billionaires;
    }

    private Billionaire parseLine(String line) {
        try {

            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (parts.length < 7) {
                System.out.println("Пропущена строка (недостаточно данных): " + line);
                return null;
            }


            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
                if (parts[i].startsWith("\"") && parts[i].endsWith("\"")) {
                    parts[i] = parts[i].substring(1, parts[i].length() - 1);
                }
            }


            int rank = Integer.parseInt(parts[0]);
            String name = parts[1];
            double netWorth = Double.parseDouble(parts[2]);
            int age = parts[3].isEmpty() ? 0 : Integer.parseInt(parts[3]);
            String country = parts[4];
            String source = parts[5];
            String industry = parts[6];

            return new Billionaire(rank, name, netWorth, age, country, source, industry);

        } catch (Exception e) {
            System.out.println("Ошибка парсинга строки: " + line);
            return null;
        }
    }
}