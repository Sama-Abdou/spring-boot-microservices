package com.moviecatalogservice.resources;

import com.example.grpc.tmsrvc.TrendingServiceGrpc;
import com.example.grpc.tmsrvc.TrendingMoviesRequest;
import com.example.grpc.tmsrvc.TrendingMoviesResponse;
import com.example.grpc.tmsrvc.TopRatedMovie;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class TrendingResource {

    @GrpcClient("trending-service")
    private TrendingServiceGrpc.TrendingServiceBlockingStub trendingStub;

    @GetMapping("/trending")
    public List<Map<String, Object>> getTopTrendingMovies() {
        TrendingMoviesRequest request = TrendingMoviesRequest.newBuilder()
                .setTopN(10)
                .build();

        TrendingMoviesResponse response = trendingStub.getTopRatedMovies(request);

        return response.getMoviesList().stream()
                .map(movie -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("movieId", movie.getMovieId());
                    m.put("movieName", movie.getMovieName());
                    m.put("rating", movie.getRating());
                    return m;
                })
                .collect(Collectors.toList());
    }
}