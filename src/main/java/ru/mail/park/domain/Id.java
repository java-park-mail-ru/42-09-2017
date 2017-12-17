package ru.mail.park.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import static ru.mail.park.info.constants.Constants.HASHCODE_CONSTANT;

@SuppressWarnings("ALL")
public class Id<T> {
    private final long id;

    public Id(long id) {
        this.id = id;
    }

    @JsonValue
    public long getId() {
        return id;
    }

    @SuppressWarnings("StaticMethodNamingConvention")
    public static <T> Id<T> of(long id) {
        return new Id<>(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Id<?> id1 = (Id<?>) obj;
        return id == id1.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> HASHCODE_CONSTANT));
    }

    @Override
    public String toString() {
        return "Id{"
                + "id=" + id
                + '}';
    }
}
