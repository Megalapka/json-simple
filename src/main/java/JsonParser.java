import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
public class JsonParser {


    public static void main(String[] args) {

        JSONParser parser = new JSONParser();
        String special = "Офтальмология";

        try {
            FileReader reader = new FileReader("response.json");

            JSONObject json = (JSONObject) parser.parse(reader);
            reader.close();

            JSONArray result = (JSONArray) json.get("result");
            System.out.println("Всего записей: " + result.size() + " ищем врача: " + special);
            System.out.println("----------------------");


                for (int i = 0; i < result.size(); i++) {
                    JSONObject item = (JSONObject) result.get(i);
                    Long ticket = (Long) item.get("countFreeTicket");
                    if (item.get("name").toString().equals(special) && ticket > 0) {
                        System.out.println("Талонов: для "+ special + " " + item.get("countFreeTicket") + "шт");
                    } else System.out.println("Название: " + item.get("name"));
                    System.out.println("----------------------");
                }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
