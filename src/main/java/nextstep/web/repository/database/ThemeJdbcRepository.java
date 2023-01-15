package nextstep.web.repository.database;

import lombok.RequiredArgsConstructor;
import nextstep.domain.Theme;
import nextstep.web.exception.BusinessException;
import nextstep.web.exception.CommonErrorCode;
import nextstep.web.repository.ThemeRepository;
import nextstep.web.repository.database.mappingstrategy.ThemeMappingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ThemeJdbcRepository implements ThemeRepository {

    public static final String TABLE_NAME = "theme";

    public static final String KEY_COLUMN_NAME = "id";

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Theme> actorRowMapper = (rs, rowNum) ->
            new ThemeMappingStrategy().map(rs);

    @Override
    public Long save(Theme theme) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(KEY_COLUMN_NAME);

        Number key = simpleJdbcInsert.executeAndReturnKey(prepareParams(theme));

        return key.longValue();
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(sql, actorRowMapper);
    }

    @Override
    public Theme findById(Long id) {
        String sql = "SELECT id, name, desc, price FROM theme WHERE id = ?";
        List<Theme> themes = jdbcTemplate.query(sql, actorRowMapper, id);
        if (themes.isEmpty()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        return themes.get(0);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM theme WHERE ID = ?;";
        if (jdbcTemplate.update(sql, id) == 0) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private Map<String, Object> prepareParams(Theme theme) {
        return Map.of(
                "name", theme.getName(),
                "desc", theme.getDesc(),
                "price", theme.getPrice()
        );
    }
}
