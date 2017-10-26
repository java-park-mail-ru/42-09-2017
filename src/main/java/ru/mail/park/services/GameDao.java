package ru.mail.park.services;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.controllers.domain.MapMeta;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static ru.mail.park.info.constants.Constants.MAPS_ON_PAGE;

@Service
@Transactional
public class GameDao {
    private EntityManager em;

    public GameDao(EntityManager em) {
        this.em = em;
    }

    public List<MapMeta> getMaps(@Nullable String sort, @Nullable Integer page) {
        StringBuilder sql = new StringBuilder("select m from MapMeta m");
        if (sort != null) {
            sql.append(" order by ").append(sort);
        }
        if (page != null) {
            return getMapsPaged(sql, page);
        }
        try {
            return em.createQuery(sql.toString(), MapMeta.class)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    private List<MapMeta> getMapsPaged(StringBuilder sql, Integer page) {
        sql.append(" limit :limit offset :offset");
        try {
            return em.createQuery(sql.toString(), MapMeta.class)
                    .setParameter("limit", MAPS_ON_PAGE)
                    .setParameter("offset", MAPS_ON_PAGE * (page - 1))
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }
}
