package nextstep.web.repository.database;

import lombok.RequiredArgsConstructor;
import nextstep.domain.Reservation;
import nextstep.domain.Theme;
import nextstep.web.exception.BusinessException;
import nextstep.web.exception.ReservationErrorCode;
import nextstep.web.repository.ReservationRepository;
import nextstep.web.repository.database.mappingstrategy.ReservationMappingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class ReservationJdbcRepository implements ReservationRepository {

    public static final String TABLE_NAME = "reservation";
    public static final String KEY_COLUMN_NAME = "id";
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Reservation> actorRowMapper = (rs, rowNum) ->
            new ReservationMappingStrategy().map(rs);

    @Override
    public Long save(Reservation reservation) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(KEY_COLUMN_NAME);

        Number key = simpleJdbcInsert.executeAndReturnKey(prepareParams(reservation));

        return key.longValue();
    }


    @Override
    public Reservation findById(Long id) {
        String sql = "SELECT * FROM reservation join theme on (reservation.theme_id = theme.id) WHERE reservation.id = ?;";
        List<Reservation> reservations = jdbcTemplate.query(sql, actorRowMapper, id);
        if (reservations.isEmpty()) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.get(0);
    }

    @Override
    public Optional<Reservation> findByThemeId(Long themeId) {
        String sql = "SELECT * FROM reservation join theme on (reservation.theme_id = theme.id) WHERE theme_id = ?;";
        List<Reservation> reservations = jdbcTemplate.query(sql, actorRowMapper, themeId);
        if (reservations.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(reservations.get(0));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE ID = ?;";
        if (jdbcTemplate.update(sql, id) == 0) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private Map<String, Object> prepareParams(Reservation reservation) {
        Theme theme = reservation.getTheme();
        Map<String, Object> params = new HashMap<>();

        params.put("date", reservation.getDate());
        params.put("time", reservation.getTime());
        params.put("name", reservation.getName());
        params.put("theme_id", theme.getId());
        return params;
    }
}
