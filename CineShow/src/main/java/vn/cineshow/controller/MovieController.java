package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.model.Movie;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Controller")
@Slf4j(topic = "MOVIE-CONTROLLER")
public class MovieController {

    private final MovieService movieService;

    private final MovieRepository movieRepository;

    @Operation(summary = "Get all movies with sort by",
            description = "Send a request via this API to get all movies with sort by")
    @GetMapping("/list-with-sortBy")
    public ResponseData<PageResponse<?>> getMoviesList(@Min(1) @RequestParam(defaultValue = "0", required = false) int pageNo,
                                                       @Min(10) @RequestParam(defaultValue = "0", required = false) int pageSize,
                                                       @RequestParam(required = false) String sortBy) {
        log.info("Request getMoviesList, pageNo: {}, pageSize: {}", pageNo, pageSize);
        PageResponse<?> movies = movieService.getAllMovieWithSortBy(pageNo, pageSize, sortBy);
        log.info("Response getMoviesList, pageNo: {}, pageSize: {}", pageNo, pageSize);
        return new ResponseData<>(HttpStatus.OK.value(), "Movies founded successfully", movies);
    }

    @GetMapping("/all")
    public List<Movie> findALl() {
        return movieRepository.findAll();
    }

}
