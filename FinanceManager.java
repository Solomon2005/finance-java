import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FinanceManager {

    private static final String DATA_FILE = "expenses.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Добавить расход");
            System.out.println("2. Посмотреть расходы");
            System.out.println("3. Анализ расходов");
            System.out.println("4. Выход");

            System.out.print("Введите номер действия: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addExpense(scanner);
                    break;
                case "2":
                    viewExpenses();
                    break;
                case "3":
                    analyzeExpenses();
                    break;
                case "4":
                    System.out.println("Завершение работы.");
                    return;
                default:
                    System.out.println("Неверный выбор. Попробуйте еще раз.");
            }
        }
    }

    private static void addExpense(Scanner scanner) {
        System.out.print("Введите дату расхода (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine();
        LocalDate date;
        try{
             date = LocalDate.parse(dateStr, DATE_FORMATTER);
        }
        catch (Exception e){
            System.out.println("Неверный формат даты. Попробуйте еще раз.");
            return;
        }


        System.out.print("Введите категорию расхода: ");
        String category = scanner.nextLine();

        System.out.print("Введите сумму расхода: ");
        double amount;
        try{
            amount = Double.parseDouble(scanner.nextLine());
        }
        catch(Exception e){
            System.out.println("Неверный формат суммы. Попробуйте еще раз.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE, true))) {
            writer.write(String.format("%s,%s,%.2f%n", date.format(DATE_FORMATTER), category, amount));
            System.out.println("Расход добавлен.");
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

     private static void viewExpenses() {
        if (!Files.exists(Paths.get(DATA_FILE))) {
            System.out.println("Файл с расходами не найден. Добавьте сначала расходы.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            System.out.println("\n--- Расходы ---");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                   try{
                       LocalDate date = LocalDate.parse(parts[0], DATE_FORMATTER);
                        String category = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                       System.out.printf("%s | %s | %.2f%n", date.format(DATE_FORMATTER), category, amount);

                   }
                   catch (Exception e){
                       System.out.println("Неверный формат данных в файле.");
                       continue;
                   }

                }
                 else {
                  System.out.println("Неверный формат данных в файле.");
                 }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }


    private static void analyzeExpenses() {
        if (!Files.exists(Paths.get(DATA_FILE))) {
            System.out.println("Файл с расходами не найден. Добавьте сначала расходы.");
            return;
        }

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpenses = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                  if (parts.length == 3) {
                   try{
                       LocalDate date = LocalDate.parse(parts[0], DATE_FORMATTER);
                        String category = parts[1];
                        double amount = Double.parseDouble(parts[2]);

                      totalExpenses += amount;
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);

                   }
                   catch (Exception e){
                       System.out.println("Неверный формат данных в файле.");
                       continue;
                   }

                }
                 else {
                  System.out.println("Неверный формат данных в файле.");
                 }


            }

            System.out.println("\n--- Анализ расходов ---");
            System.out.printf("Общая сумма расходов: %.2f%n", totalExpenses);
            System.out.println("Расходы по категориям:");
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                System.out.printf("  %s: %.2f%n", entry.getKey(), entry.getValue());
            }

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

}