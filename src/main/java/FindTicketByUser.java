import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
            default:
                System.out.println("Неверный выбор");
        }

    }

//    private static void showSpecialities() throws Exception {
//        JSONArray specialties = jsonSpecialties();
//        System.out.println("\n=== список специальностей ===");
//
//        for (int i = 0; i < Math.min(50, specialties.size()); i++) {
//            JSONObject item = (JSONObject) specialties.get(i);
//            String name = (String) item.get("name");
//            String id = (String) item.get("id");
//            Long tickets = (Long) item.get("countFreeTicket");
//
//            System.out.printf("%d. %s (ID: %s) - %d талонов%n", i+1, name, id, tickets);
//        }
//
//    }


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
        Files.writeString(TEMP_FILE, responseBody, StandardCharsets.UTF_8);
    }

    private static JSONArray readFromFile() throws Exception {
        if (!Files.exists(TEMP_FILE)){
            System.out.println("❌ Сначала получите данные (пункт 1)");
            return null;
        }
        String jsonString = Files.readString(TEMP_FILE, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(jsonString);
        return (JSONArray) obj.get("results");
    }
}
