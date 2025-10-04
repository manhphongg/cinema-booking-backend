package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.cineshow.dto.response.*;
import vn.cineshow.model.Movie;
import vn.cineshow.model.MovieGenre;
import vn.cineshow.repository.MovieGenresRepository;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.service.MovieService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Movie_Service")
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieGenresRepository movieGenresRepository;

    @Override
    public PageResponse<?> getAllMovieWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();

        //if sortBy != null
        if (StringUtils.hasLength(sortBy)) {
            //firstName:asc|desc
            Pattern pattern = Pattern.compile("(\\w+)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else if (matcher.group(3).equalsIgnoreCase("desc")) {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                } else {
                    throw new IllegalArgumentException("Invalid sort parameter");
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));
        Page<Movie> movies = movieRepository.findAll(pageable);
        log.info("Movies found: totalElements={}, totalPages={}, size={}",
                movies.getTotalElements(), movies.getTotalPages(), movies.getSize());

        return getPageResponse(pageNo, pageSize, movies);
    }

    private PageResponse<?> getPageResponse(int pageNo, int pageSize, Page<Movie> movies) {

        List<MovieDetailResponse> responses = movies.stream().map(movie -> MovieDetailResponse.builder()
                .id(movie.getId())
                .actor(movie.getActor())
                .name(movie.getName())
                .genre(getMovieGenresByMovie(movie))
                .country(CountryResponse.builder()
                        .id(movie.getCountry().getId())
                        .name(movie.getCountry().getName())
                        .build())
                .description(movie.getDescription())
                .releaseDate(movie.getReleaseDate())
                .trailerUrl(movie.getTrailerUrl())
                .language(LanguageResponse.builder()
                        .id(movie.getLanguage().getId())
                        .name(movie.getLanguage().getName())
                        .build())
                .posterUrl(movie.getPosterUrl())
                .director(movie.getDirector())
                .build()).toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(movies.getTotalPages())
                .items(responses)
                .build();

    }

    private List<MovieGenreResponse> getMovieGenresByMovie(Movie movie) {
        List<MovieGenre> movieGenres = movie.getMovieGenres().stream().toList();
        return movieGenres.stream().map(movieGenre -> MovieGenreResponse.builder()
                        .id(movieGenre.getId())
                        .name(movieGenre.getName())
                        .build())
                .toList();
    }

}
