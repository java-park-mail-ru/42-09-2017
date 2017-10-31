package ru.mail.park.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.domain.MapData;
import ru.mail.park.domain.MapMeta;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.mail.park.info.constants.Constants.MAPS_ON_PAGE;

@Service
@Transactional
public class GameDao {
    private EntityManager em;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(GameDao.class);

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

    public String getMap(Integer id) {
        try {
            MapData mapData = em.find(MapData.class, id);
            return mapData.getData();
        } catch (NoResultException e) {
            return null;
        }
    }

    public MapMeta createMap(Map<String, Object> mapDataObj, MapMeta mapMeta) throws JsonProcessingException {
        LOGGER.warn(mapDataObj.toString());

        LOGGER.warn(MAPPER.writeValueAsString(mapDataObj));
        MapData mapData = new MapData();
        mapData.setData(MAPPER.writeValueAsString(mapDataObj));
        em.persist(mapData);
        mapMeta.setMap(mapData);
        em.persist(mapMeta);
        return mapMeta;
    }
}
