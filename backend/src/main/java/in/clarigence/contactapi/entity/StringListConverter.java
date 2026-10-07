package in.clarigence.contactapi.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

/** Small ordered text lists stay in one column: no collection joins or N+1 queries. */
@Converter
public class StringListConverter implements AttributeConverter<List<String>,String> {
    private static final ObjectMapper JSON = new ObjectMapper();
    public String convertToDatabaseColumn(List<String> value) {
        try {return JSON.writeValueAsString(value==null?List.of():value);}
        catch(Exception error){throw new IllegalArgumentException("Invalid content list",error);}
    }
    public List<String> convertToEntityAttribute(String value) {
        if(value==null || value.isBlank())return List.of();
        try {return JSON.readValue(value,new TypeReference<List<String>>(){});}
        catch(Exception error){throw new IllegalArgumentException("Invalid stored content list",error);}
    }
}
