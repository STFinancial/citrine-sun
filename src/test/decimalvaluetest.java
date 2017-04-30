package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.converter.BigDecimalStringConverter;

import java.io.IOException;

/**
 * Created by Timothy on 2/22/17.
 */
public class decimalvaluetest {

    public static void main(String[] args) {
        BigDecimalStringConverter converter = new BigDecimalStringConverter();
        ObjectMapper m = new ObjectMapper();
        JsonNode j = null;
        try {
             j = m.readTree("{ \"field\": \"1.234567\" }");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(j.get("field").decimalValue());
        System.out.println(converter.fromString(j.get("field").asText()));
        System.out.println(j.get("field").asDouble());
    }

}
