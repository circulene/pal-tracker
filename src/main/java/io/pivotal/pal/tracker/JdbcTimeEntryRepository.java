package io.pivotal.pal.tracker;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class JdbcTimeEntryRepository  implements TimeEntryRepository{
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public JdbcTimeEntryRepository(DataSource dataSource){
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry inTimeEntry) {

        KeyHolder gkh = new GeneratedKeyHolder();

        PreparedStatementCreator psc = new PreparedStatementCreator() {
            String sql = "INSERT INTO time_entries(project_id, user_id, date, hours) " +
                    "VALUES(?, ?, ?, ?)";
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, inTimeEntry.getProjectId());
                ps.setLong(2, inTimeEntry.getUserId());
                ps.setDate(3, Date.valueOf(inTimeEntry.getDate()));
                ps.setInt(4, inTimeEntry.getHours());
                return ps;
            }
        };
        jdbcTemplate.update(psc, gkh);

        return find(gkh.getKey().longValue());
    }

    @Override
    public TimeEntry find(long id) {
        String querySql = "SELECT id, project_id, user_id, date, hours FROM time_entries " +
                "WHERE id = ?";
        List<TimeEntry> result = jdbcTemplate.query(querySql, new Object[]{ id },
                (rs, i) -> {
                    return new TimeEntry(
                            rs.getLong(1),
                            rs.getLong(2),
                            rs.getLong(3),
                            rs.getDate(4).toLocalDate(),
                            rs.getInt(5));
                });
        return result.size() == 1 ? result.get(0) : null;
    }

    @Override
    public List<TimeEntry> list() {
        String querySql = "SELECT id, project_id, user_id, date, hours FROM time_entries";
        return jdbcTemplate.query(querySql, (rs, i) -> {
            return new TimeEntry(
                    rs.getLong(1),
                    rs.getLong(2),
                    rs.getLong(3),
                    rs.getDate(4).toLocalDate(),
                    rs.getInt(5));
        });
    }

    @Override
    public TimeEntry update(long id, TimeEntry newTimeEntry) {
        String sql = "UPDATE time_entries SET project_id = ?, user_id = ?, date = ?, hours = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                newTimeEntry.getProjectId(),
                newTimeEntry.getUserId(),
                Date.valueOf(newTimeEntry.getDate()),
                newTimeEntry.getHours(),
                id);
        return find(id);
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM time_entries WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
