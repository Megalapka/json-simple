import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Scanner;

public class FindTicketByUser {

    private static final String API_URL = "https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/22/specialties";
    private static final Path TEMP_FILE = Paths.get("temp_specialties.json");

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== мониторинг талонов горздрав ===");
            System.out.println("1. Получить и сохранить данные");
            System.out.println("2. Показать список специальностей (из файла)");
            System.out.println("3. Поиск по названию (из файла)");
            System.out.println("4. Отслеживать специальность");
            System.out.println("5. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    fetchAndSaveToFile();
                    System.out.println("✅ Данные сохранены в файл");
                    break;
                case "2":
                    showSpecialities();
                    break;
                case "3":
                    searchFromFile(scanner);
                    break;
                case "4":
                    trackSpeciality(scanner);
                    break;
                case "5":
                    System.out.println("До свидания!");
                    return;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }
    // добавить ТО на коннект
    private static void fetchAndSaveToFile () throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/json")
                .header("User-Agent", "Java-TicketChecker")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        Files.writeString(TEMP_FILE, responseBody);
        //добавить проверку наличия связи
    }

    private static JSONArray readFromFile() throws Exception {
        if (!Files.exists(TEMP_FILE)) {
            System.out.println("Сначала получите данные (пункт 1)");
            return null;
        }
        FileReader reader = new FileReader(TEMP_FILE.toString());
        JSONParser parser = new JSONParser();
        JSONObject root = (JSONObject) parser.parse(reader);
        return (JSONArray) root.get("result");
    }

    private static void showSpecialities() throws Exception {
        JSONArray specialties = readFromFile();
        System.out.println("\n=== список специальностей ===");
        for (int i = 0; i < Math.min(50, specialties.size()); i++) {
            JSONObject item = (JSONObject) specialties.get(i);
            String name = (String) item.get("name");
            String id = (String) item.get("id");
            Long tickets = (Long) item.get("countFreeTicket");

            System.out.printf("%d. %s (ID: %s) - %d талонов%n", i+1, name, id, tickets);
        }
    }

    private static void searchFromFile(Scanner scanner) throws Exception {
        JSONArray specialties = readFromFile();
        if (specialties == null) return;

        System.out.println("Введите название для поиска: ");
        String query = scanner.nextLine().toLowerCase();

        System.out.println("\n ++++++ Результаты поиска ++++++");
        boolean found = false;

        for (Object obj : specialties) {
            JSONObject item = (JSONObject) obj;
            String name = (String) item.get("name");

            if (name.toLowerCase().contains(query)) {
                found = true;
                String id = (String) item.get ("id");
                Long tickets = (Long) item.get("countFreeTicket");
                System.out.printf("%s (ID: %s) - %d талонов%n", name, id, tickets);
            }
        }

        if (!found) {
            System.out.println("Ничего не найдено");
        }

    }

    private static void trackSpeciality(Scanner scanner) throws Exception {
        JSONArray specialties = readFromFile();
        if (specialties == null) return;

        System.out.println("\n ------------выберите спеуиальность--------------");
        for (int i = 0; i < Math.min(50, specialties.size()); i++) {
            JSONObject item = (JSONObject) specialties.get(i);
            String name = (String) item.get("name");
            System.out.printf("%d. %s%n", i+1, name);
        }
        System.out.println("Введите номер специальности: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine())-1;
            JSONObject selected = (JSONObject) specialties.get(choice);
            String name = (String) selected.get("name");
            String id = (String) selected.get("id");

            System.out.println("\n выбрана: " + name);
            System.out.println("Начинаем отслеживание.. \n");

            monitoringSpeciality(id, name);

        } catch (Exception e) {
            System.out.println("Ошибка выбора");
        }
    }

    //мониторинг с периодическим обновлением файла
    private static void monitoringSpeciality (String targetId, String targetName) throws Exception {
        int lastTicket = -1;

        while (true) {
            fetchAndSaveToFile();

            JSONArray specialties = readFromFile();
            if (specialties != null) {
                for (Object obj : specialties) {
                    JSONObject item = (JSONObject) obj;
                    String id = (String) item.get("id");

                    if (targetId.equals(id)) {
                        Long tickets = (Long) item.get("countFreeTicket");
                        String date = (String) item.get("nearestDate");

                        String time = java.time.LocalTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

                        if (tickets > 0) {
                            System.out.printf("✅ [%s] %s: %d талонов! %s%n",
                                    time, targetName, tickets,
                                    date != null ? date.replace("T", " ") : "");
                            System.out.print("\007");
                        }
                        break;
                    }
                }
            }

            Thread.sleep(60000);
        }

    }
}
