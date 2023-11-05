package jsonparser.parser;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Objects;

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
    
    @Override
    public Object generateObjectFromJson(Class<?> clas, String json){
        return null;
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
