import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceManager {
    private static final String DATA_FILE = "expenses.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        createFileIfNotExists();

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addExpense(scanner);
                    case "2" -> viewExpenses();
                    case "3" -> analyzeExpenses();
                    case "4" -> showMonthlyReport(scanner);
                    case "5" -> {
                        System.out.println("Программа завершена.");
                        return;
                    }
                    default -> System.out.println("Неверный выбор. Попробуйте еще раз.");
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\nМенеджер личных финансов");
        System.out.println("------------------------");
        System.out.println("1. Добавить расход");
        System.out.println("2. Посмотреть все расходы");
        System.out.println("3. Анализ расходов");
        System.out.println("4. Месячный отчет");
        System.out.println("5. Выход");
        System.out.print("\nВыберите действие: ");
    }

    private static void createFileIfNotExists() {
        try {
            Files.createFile(Paths.get(DATA_FILE));
        } catch (FileAlreadyExistsException e) {
            // Файл уже существует, ничего делать не нужно
        } catch (IOException e) {
            System.out.println("Ошибка при создании файла: " + e.getMessage());
        }
    }

    private static void addExpense(Scanner scanner) {
        LocalDate date = readDate(scanner);
        if (date == null) return;

        System.out.print("Введите категорию расхода: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty() || category.contains(DELIMITER)) {
            System.out.println("Категория не может быть пустой или содержать запятые");
            return;
        }

        Double amount = readAmount(scanner);
        if (amount == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE, true))) {
            writer.write(String.format("%s%s%s%s%.2f%n", 
                date.format(DATE_FORMATTER), DELIMITER, category, DELIMITER, amount));
            System.out.println("✓ Расход успешно добавлен");
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static LocalDate readDate(Scanner scanner) {
        while (true) {
            System.out.print("Введите дату расхода (yyyy-MM-dd): ");
            String dateStr = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Дата не может быть в будущем");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Используйте формат yyyy-MM-dd");
                return null;
            }
        }
    }

    private static Double readAmount(Scanner scanner) {
        while (true) {
            System.out.print("Введите сумму расхода: ");
            try {
                double amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("Сумма должна быть положительным числом");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат суммы. Используйте числовое значение");
                return null;
            }
        }
    }

    private static void viewExpenses() {
        List<Expense> expenses = readExpenses();
        if (expenses.isEmpty()) {
            System.out.println("Расходы не найдены");
            return;
        }

        System.out.println("\nВсе расходы:");
        System.out.println("------------------------");
        expenses.forEach(expense -> 
            System.out.printf("%s | %-15s | %10.2f руб.%n", 
                expense.date.format(DATE_FORMATTER), 
                expense.category, 
                expense.amount)
        );
    }

    private static void analyzeExpenses() {
        List<Expense> expenses = readExpenses();
        if (expenses.isEmpty()) {
            System.out.println("Расходы не найдены");
            return;
        }

        // Общая статистика
        double totalAmount = expenses.stream().mapToDouble(e -> e.amount).sum();
        
        // Группировка по категориям
        Map<String, List<Expense>> expensesByCategory = expenses.stream()
            .collect(Collectors.groupingBy(e -> e.category));
        
        // Группировка по месяцам
        Map<YearMonth, Double> monthlyTotals = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> YearMonth.from(e.date),
                Collectors.summingDouble(e -> e.amount)
            ));

        // Поиск максимальных и средних значений
        Expense maxExpense = expenses.stream()
            .max(Comparator.comparingDouble(e -> e.amount))
            .orElse(null);
            
        double avgExpense = totalAmount / expenses.size();
        
        // Статистика по месяцам
        OptionalDouble maxMonthlyExpense = monthlyTotals.values().stream()
            .mapToDouble(v -> v)
            .max();
        OptionalDouble minMonthlyExpense = monthlyTotals.values().stream()
            .mapToDouble(v -> v)
            .min();
        double avgMonthlyExpense = monthlyTotals.values().stream()
            .mapToDouble(v -> v)
            .average()
            .orElse(0.0);

        // Вывод результатов
        System.out.println("\nПодробный анализ расходов");
        System.out.println("=========================");
        
        // 1. Общая статистика
        System.out.println("\n1. Общая статистика:");
        System.out.println("------------------------");
        System.out.printf("Общая сумма всех расходов: %.2f руб.%n", totalAmount);
        System.out.printf("Средний размер расхода: %.2f руб.%n", avgExpense);
        System.out.printf("Количество записей о расходах: %d%n", expenses.size());
        
        // 2. Максимальные расходы
        System.out.println("\n2. Максимальные расходы:");
        System.out.println("------------------------");
        if (maxExpense != null) {
            System.out.printf("Самый крупный расход: %.2f руб. (%s, %s)%n",
                maxExpense.amount, maxExpense.category,
                maxExpense.date.format(DATE_FORMATTER));
        }
        
        // 3. Анализ по категориям
        System.out.println("\n3. Расходы по категориям:");
        System.out.println("------------------------");
        expensesByCategory.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(
                e2.getValue().stream().mapToDouble(exp -> exp.amount).sum(),
                e1.getValue().stream().mapToDouble(exp -> exp.amount).sum()))
            .forEach(entry -> {
                double categoryTotal = entry.getValue().stream()
                    .mapToDouble(exp -> exp.amount)
                    .sum();
                double categoryAvg = categoryTotal / entry.getValue().size();
                double percentage = (categoryTotal / totalAmount) * 100;
                
                System.out.printf("Категория: %s%n", entry.getKey());
                System.out.printf("  Общая сумма: %.2f руб. (%5.1f%%)%n",
                    categoryTotal, percentage);
                System.out.printf("  Средний расход: %.2f руб.%n", categoryAvg);
                System.out.printf("  Количество расходов: %d%n",
                    entry.getValue().size());
            });
        
        // 4. Анализ по месяцам
        System.out.println("\n4. Анализ по месяцам:");
        System.out.println("------------------------");
        System.out.printf("Средний расход за месяц: %.2f руб.%n",
            avgMonthlyExpense);
        maxMonthlyExpense.ifPresent(max ->
            System.out.printf("Максимальный расход за месяц: %.2f руб.%n", max));
        minMonthlyExpense.ifPresent(min ->
            System.out.printf("Минимальный расход за месяц: %.2f руб.%n", min));
        
        // 5. Тренды расходов по месяцам
        System.out.println("\n5. Тренд расходов по месяцам:");
        System.out.println("------------------------");
        monthlyTotals.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
                System.out.printf("%s: %.2f руб.%n",
                    entry.getKey(), entry.getValue()));
    }

    private static void showMonthlyReport(Scanner scanner) {
        System.out.print("Введите месяц и год (yyyy-MM): ");
        String monthStr = scanner.nextLine().trim();
        
        try {
            YearMonth yearMonth = YearMonth.parse(monthStr);
            List<Expense> expenses = readExpenses().stream()
                .filter(e -> YearMonth.from(e.date).equals(yearMonth))
                .collect(Collectors.toList());

            if (expenses.isEmpty()) {
                System.out.printf("Расходы за %s не найдены%n", yearMonth);
                return;
            }

            double totalAmount = expenses.stream().mapToDouble(e -> e.amount).sum();
            
            System.out.printf("%nОтчет за %s%n", yearMonth);
            System.out.println("------------------------");
            System.out.printf("Общая сумма расходов: %.2f руб.%n%n", totalAmount);
            
            // Группировка по категориям
            Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                    e -> e.category,
                    Collectors.summingDouble(e -> e.amount)
                ));

            categoryTotals.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> 
                    System.out.printf("%-15s: %10.2f руб.%n", 
                        entry.getKey(), entry.getValue())
                );

        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты. Используйте формат yyyy-MM");
        }
    }

    private static List<Expense> readExpenses() {
        List<Expense> expenses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(DELIMITER);
                    if (parts.length == 3) {
                        LocalDate date = LocalDate.parse(parts[0], DATE_FORMATTER);
                        String category = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        expenses.add(new Expense(date, category, amount));
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка при чтении строки: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return expenses;
    }

    private static class Expense {
        final LocalDate date;
        final String category;
        final double amount;

        Expense(LocalDate date, String category, double amount) {
            this.date = date;
            this.category = category;
            this.amount = amount;
        }
    }
}