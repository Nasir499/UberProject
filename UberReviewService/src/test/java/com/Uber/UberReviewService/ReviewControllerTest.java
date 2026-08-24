package com.Uber.UberReviewService;

import com.uberreviewservice.adapters.CreateReviewDtoToReviewAdapter;
import com.uberreviewservice.controllers.ReviewController;
import com.example.uberentityservice.models.Review;

import com.uberreviewservice.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ReviewControllerTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CreateReviewDtoToReviewAdapter createReviewDtoToReviewAdapter;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindReviewById_Success(){
        long reviewId = 1L;
        Review mockReview = Review.builder().build();
        mockReview.setId(reviewId);

        when(reviewService.findReviewById(reviewId)).thenReturn(Optional.of(mockReview));

        ResponseEntity<?> response=reviewController.findReviewById(reviewId);

       assertEquals(HttpStatus.OK, response.getStatusCode());
       Optional<Review> review = (Optional<Review>) response.getBody();
       assertEquals(reviewId,review.get().getId());
    }
}
