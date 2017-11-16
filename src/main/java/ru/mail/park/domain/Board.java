package ru.mail.park.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.mail.park.domain.pgtypes.JsonBStringType;

import javax.persistence.*;

@Entity
@TypeDef(name = "JsonBStringType", typeClass = JsonBStringType.class)
public class Board {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(type = "JsonBStringType")
    private String data;

    @OneToOne(mappedBy = "board")
    private BoardMeta meta;

    @JsonCreator
    public Board() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("objects")
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @JsonIgnore
    public BoardMeta getMeta() {
        return meta;
    }

    public void setMeta(BoardMeta meta) {
        this.meta = meta;
    }
}
