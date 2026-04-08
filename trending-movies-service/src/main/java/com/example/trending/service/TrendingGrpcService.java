package com.example.trending.service;

import com.example.trending.models.UserRating;
import com.example.grpc.tmsrvc.TrendingServiceGrpc;
import com.example.grpc.tmsrvc.TrendingMoviesRequest;
import com.example.grpc.tmsrvc.TrendingMoviesResponse;
import com.example.grpc.tmsrvc.TopRatedMovie;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@GrpcService
public class TrendingGrpcService extends TrendingServiceGrpc.TrendingServiceImplBase {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void getTopRatedMovies(TrendingMoviesRequest request,
                                  StreamObserver<TrendingMoviesResponse> responseObserver) {
        int topN = request.getTopN() > 0 ? request.getTopN() : 10;

        try {
            UserRating userRating = restTemplate.getForObject(
                    "http://localhost:8083/ratings/1",
                    UserRating.class);

            TrendingMoviesResponse.Builder responseBuilder = TrendingMoviesResponse.newBuilder();

            if (userRating != null && userRating.getRatings() != null) {
                userRating.getRatings().stream()
                        .sorted((a, b) -> Integer.compare(b.getRating(), a.getRating()))
                        .limit(topN)
                        .forEach(r -> responseBuilder.addMovies(
                                TopRatedMovie.newBuilder()
                                        .setMovieId(r.getMovieId())
                                        .setRating(r.getRating())
                                        .build()
                        ));
            }

            responseObserver.onNext(responseBuilder.build());
        } catch (Exception e) {
            System.err.println("Error calling Ratings Service: " + e.getMessage());
        } finally {
            responseObserver.onCompleted();
        }
    }
}