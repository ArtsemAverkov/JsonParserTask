package jsonparser.parser;

public interface JsonParser {
    String generateJsonDefault(Object obj) throws Exception;
    Object generateObjectFromJson(Class<?> clas, String json) throws Exception;
}
