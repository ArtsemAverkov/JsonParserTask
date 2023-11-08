package jsonparser.parser;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParserImpl  implements JsonParser{


    /**
     * Метод, который генерирует JSON-строку из объекта с учетом дефолтных значений полей.
     * @param obj объект, для которого генерируется JSON-строка
     * @return JSON-представление объекта в виде строки
     * @throws IllegalAccessException выбрасывается в случае ошибки доступа к полям объекта
     */
    @SneakyThrows
    @Override
    public String generateJsonDefault(Object obj) {
        // Создаем объект StringBuilder для построения JSON-строки
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Получаем все поля объекта
        Field[] fields = obj.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true); // Делаем поля доступными для чтения

            Object fieldValue = fields[i].get(obj); // Получаем значение поля
            String fieldName = fields[i].getName(); // Получаем имя поля

            sb.append("\"").append(fieldName).append("\":"); // Добавляем имя поля в JSON

            if (fieldValue == null && !fields[i].getType().isPrimitive() && fields[i].getType() != String.class) {
                // Если значение поля равно null и тип поля не примитивный и не является строкой,
                // генерируем JSON-представление для нового экземпляра объекта
                sb.append(generateJsonDefaultForNullObject(fields[i].getType()));
            } else {
                // Иначе, определяем и преобразуем значение поля в JSON
                sb.append(determineAndConvertToJson(fieldValue));
            }
            if (i < fields.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Генерирует объект Java из JSON-строки.
     *
     * @param clas Класс объекта, который нужно создать.
     * @param json JSON-строка.
     * @return Объект, созданный на основе JSON.
     * @throws IllegalAccessException Если есть ошибка доступа.
     * @throws InstantiationException Если есть ошибка создания экземпляра класса.
     */
    @SneakyThrows
    @Override
    public Object generateObjectFromJson(Class<?> clas, String json) {
        // Парсим JSON-строку в Map
        Map<String, Object> stringObjectMap = parseJson(json);
        // Создаем объект класса 'clas' и заполняем его данными из Map
        Object object = createObject(clas, stringObjectMap);
        return object;
    }

    /**
     * Создает объект заданного класса и заполняет его данными из Map.
     *
     * @param clas            Класс объекта для создания.
     * @param stringObjectMap Map с данными для заполнения объекта.
     * @return Созданный объект.
     * @throws IllegalAccessException Если есть ошибка доступа.
     * @throws InstantiationException Если есть ошибка создания экземпляра класса.
     */
    private Object createObject(Class<?> clas, Map<String, Object> stringObjectMap) throws InstantiationException, IllegalAccessException {
        // Создаем новый экземпляр класса 'clas'
        Object newInstance = createNewInstance(clas);
        // Перебираем поля класса 'clas'
        for (Field field : clas.getDeclaredFields()) {
            field.setAccessible(true);
            // Получаем значение поля из Map
            Object value = stringObjectMap.get(field.getName());
            if (Objects.nonNull(value)) {
                Class<?> fieldType = field.getType();
                if (!fieldType.isPrimitive() && fieldType != String.class) {
                    // Если поле - объект, создаем его и заполняем данными
                    String fieldTypeName = fieldType.getSimpleName();
                    String string = fieldTypeName.substring(0, 1).toLowerCase() + fieldTypeName.substring(1);
                    Map<String, Object> nestedMap = (Map<String, Object>) stringObjectMap.get(string);
                    if (nestedMap != null) {
                        Object nestedObject = createObject(fieldType, nestedMap);
                        field.set(newInstance, nestedObject);
                    }
                } else {
                    // Если поле - примитив или строка, устанавливаем значение
                    field.set(newInstance, value);
                }
            }
        }
        return newInstance;
    }

    /**
     * Парсит JSON-строку и возвращает данные в виде Map.
     *
     * @param json JSON-строка для парсинга.
     * @return Map с данными из JSON-строки.
     * @throws Exception Если есть ошибка в процессе парсинга.
     */
    public Map<String, Object> parseJson(String json) throws Exception {
        // Создаем Map для хранения данных JSON
        Map<String, Object> jsonMap = new HashMap<>();
        // Определяем начальный и конечный индексы JSON-строки
        int startIndex = 1;
        int endIndex = json.length() - 1;
        // Разбиваем JSON-строку на части
        String[] parts = json.substring(startIndex, endIndex).split(",");
        // Перебираем части JSON
        for (String part : parts) {
            String[] pair = part.split(":");
            String key = pair[0].replaceAll("\"", "").trim();
            String value = pair[1].trim();
            if (value.startsWith("{")) {
                // Если значение начинается с '{', это вложенный JSON, парсим его рекурсивно
                Pattern p = Pattern.compile("^.+" + key + "\":(.+)}");
                Matcher m = p.matcher(json);
                String value2 = m.matches() ? m.group(1) : null;
                assert value2 != null;
                Map<String, Object> nestedMap = parseJson(value2);
                jsonMap.put(key, nestedMap);
                break;
            } else if (value.startsWith("\"") && value.endsWith("\"")) {
                // Если значение - строка в двойных кавычках, добавляем в Map
                jsonMap.put(key, value.toString());
            } else if (value.equals("null")) {
                // Если значение - null, добавляем в Map
                jsonMap.put(key, null);
            } else if (value.equals("true") || value.equals("false")) {
                // Если значение - true или false, добавляем в Map как булево значение
                jsonMap.put(key, Boolean.valueOf(value));
            } else if (value.contains(".")) {
                // Если значение содержит точку, добавляем в Map как число с плавающей точкой
                jsonMap.put(key, Double.valueOf(value));
            } else {
                // В остальных случаях добавляем в Map как целое число
                jsonMap.put(key, Long.parseLong(value));
            }
        }
        return jsonMap;
    }

    /**
     * Метод для определения и преобразования значения объекта в JSON-представление.
     * Если объект null, возвращает "null". Если объект является строкой, оборачивает его в кавычки.
     * Если объект является числом или булевым значением, возвращает его строковое представление.
     * Если объект не попадает ни в одну из вышеперечисленных категорий, вызывает метод генерации JSON-представления для объекта.
     * @param object объект для преобразования
     * @return JSON-представление объекта в виде строки
     */
    private String determineAndConvertToJson(Object object) {
        if (Objects.isNull(object)) {
            return "null";
        } else if (object instanceof String) {
            return "\"" + object + "\"";
        } else if (object instanceof Number || object instanceof Boolean) {
            return object.toString();
        } else {
            return generateJsonDefault(object);
        }
    }

    /**
     * Метод для генерации JSON-представления нового экземпляра объекта заданного класса.
     * @param type класс объекта, для которого генерируется JSON
     * @return JSON-представление нового объекта с дефолтными значениями
     */
    private String generateJsonDefaultForNullObject(Class<?> type) {
        Object newInstance = createNewInstance(type);
        return generateJsonDefault(newInstance);
    }

    /**
     * Метод для создания нового экземпляра объекта заданного класса.
     * @param type класс объекта, который нужно создать
     * @return новый экземпляр объекта
     * @throws RuntimeException выбрасывается в случае ошибки при создании объекта
     */
    private Object createNewInstance(Class<?> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create" + type, e);
        }
    }
}
