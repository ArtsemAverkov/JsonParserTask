package jsonparser;

import jsonparser.entity.MetaInfo;
import jsonparser.entity.Product;
import jsonparser.parser.JsonParserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonParserImplTest {

    JsonParserImpl jsonParser;

    @BeforeEach
    public void setUp() {
        jsonParser = new JsonParserImpl();
    }

    @ParameterizedTest
    @CsvSource({
            "1, 'Product1', 10.0, 5, 1, true"
    })
    public void testGenerateJsonDefault(long id, String name, double price, long amount, long metaInfoId, boolean isDiscount) {
        MetaInfo metaInfo = new MetaInfo(metaInfoId, isDiscount);
        Product product = new Product(id, name, price, amount, metaInfo);

        String expectedJson = "{\"id\":1,\"name\":\"Product1\",\"price\":10.0,\"amount\":5,\"metaInfo\":{\"id\":1,\"isDiscount\":true}}";
        String generatedJson = jsonParser.generateJsonDefault(product);

        assertEquals(expectedJson, generatedJson);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 'Product1', 10.0, 5, 1, true"
    })
    public void testGenerateObjectFromJson(long id, String name, double price, long amount, long metaInfoId, boolean isDiscount) {
        String json = "{\"id\":1,\"name\":\"Product1\",\"price\":10.0,\"amount\":5,\"metaInfo\":{\"id\":1,\"isDiscount\":true}}";

        MetaInfo metaInfo = new MetaInfo(metaInfoId, isDiscount);
        Product expectedObject = new Product(id, name, price, amount, metaInfo);

        Product parsedObject = (Product) jsonParser.generateObjectFromJson(Product.class, json);

        assertEquals(expectedObject, parsedObject);
    }
}
