package ru.mail.park.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.mail.park.domain.pgtypes.JsonBStringType;

import javax.persistence.*;

@Entity
@Table(name = "map_data")
@TypeDef(name = "JsonBStringType", typeClass = JsonBStringType.class)
public class MapData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Type(type = "JsonBStringType")
    private String data;

    @JsonCreator
    public MapData() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("objects")
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
