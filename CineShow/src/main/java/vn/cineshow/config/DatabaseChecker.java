package vn.cineshow.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class DatabaseChecker {

    private final DataSource dataSource;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    public DatabaseChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void checkDbConnection() throws SQLException {
        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            log.info("✅ Connected to DB: {}", conn.getCatalog()); // tên database/schema
        }
    }

    @PostConstruct
    public void checkMoviesCount() {
        long count = movieRepository.count();
        log.info("🎬 Movies table currently has {} records", count);
    }

    @PostConstruct
    public void checkAccountCount() {
        long count = userRepository.count();
        log.info("🎬 userRepository table currently has {} records", count);
    }

}
