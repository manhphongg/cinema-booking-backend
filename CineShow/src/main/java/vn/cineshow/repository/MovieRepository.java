package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
