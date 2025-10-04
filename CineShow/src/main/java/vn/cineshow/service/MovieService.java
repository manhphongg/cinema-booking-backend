package vn.cineshow.service;

import vn.cineshow.dto.response.PageResponse;

public interface MovieService {
    PageResponse<?> getAllMovieWithSortBy(int pageNo, int pageSize, String sortBy);
}
