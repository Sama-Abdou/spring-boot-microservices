package com.example.trending.service;

import com.example.grpc.tmsrvc.TrendingServiceGrpc;
import com.example.grpc.tmsrvc.TrendingMoviesRequest;
import com.example.grpc.tmsrvc.TrendingMoviesResponse;
import com.example.grpc.tmsrvc.TopRatedMovie;

import com.example.trending.repository.RatingRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService // endpt for grpc 
public class TrendingGrpcService extends TrendingServiceGrpc.TrendingServiceImplBase {

    @Autowired
    private RatingRepository ratingRepository;

    @Override
    public void getTopRatedMovies(TrendingMoviesRequest request,
                                  StreamObserver<TrendingMoviesResponse> responseObserver) {

        int topN = request.getTopN();
        List<Object[]> results = ratingRepository.findTopRatedMovies(topN);

        TrendingMoviesResponse.Builder responseBuilder = TrendingMoviesResponse.newBuilder();

        for (Object[] row : results) {
            String movieId = (String) row[0];
            int avgRating = ((Number) row[1]).intValue();

            TopRatedMovie movie = TopRatedMovie.newBuilder()
                    .setMovieId(movieId)
                    .setMovieName("Movie-" + movieId) // placeholder; real name would need movie-info-service
                    .setRating(avgRating)
                    .build();

            responseBuilder.addMovies(movie);
        }

        // Send response back and complete the call
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}