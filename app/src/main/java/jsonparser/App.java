package jsonparser;

import jsonparser.entity.MetaInfo;
import jsonparser.entity.Product;
import jsonparser.parser.JsonParser;
import jsonparser.parser.JsonParserImpl;
import lombok.SneakyThrows;

public class App {
    static JsonParser jsonParser = new JsonParserImpl();
    public static void main(String[] args) {
        start();
    }

    @SneakyThrows
    static void start(){

       Product product1 = new Product(1,"name", 1.1, 1, new MetaInfo(1, true));

       String generateJsonDefault1 = jsonParser.generateJsonDefault(product1);

       System.out.println("generateJsonDefault1 = " + generateJsonDefault1);

        Object generateObjectFromJson = jsonParser.generateObjectFromJson(Product.class, generateJsonDefault1);

        System.out.println("generateObjectFromJson = " + generateObjectFromJson);
    }


}