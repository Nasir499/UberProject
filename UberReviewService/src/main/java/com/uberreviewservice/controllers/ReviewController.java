package com.uberreviewservice.controllers;

import com.example.uberentityservice.models.Review;
import com.uberreviewservice.adapters.CreateReviewDtoToReviewAdapter;
import com.uberreviewservice.dtos.CreateReviewDto;
import com.uberreviewservice.dtos.ReviewDto;
import com.uberreviewservice.services.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final CreateReviewDtoToReviewAdapter createReviewDtoToReviewAdapter;
    public ReviewController(ReviewService reviewService, CreateReviewDtoToReviewAdapter createReviewDtoToReviewAdapter){

        this.reviewService = reviewService;
        this.createReviewDtoToReviewAdapter = createReviewDtoToReviewAdapter;
    }

    @PostMapping
    public ResponseEntity<?> publishReview(@Validated @RequestBody CreateReviewDto request) {
        Review incomingReview  = this.createReviewDtoToReviewAdapter.convertDto(request);
        if(incomingReview == null) {
            return new ResponseEntity<>("Invalid arguments", HttpStatus.BAD_REQUEST);
        }
        Review review = this.reviewService.publishReview(incomingReview);
        ReviewDto response = ReviewDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .booking(review.getBooking().getId())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews(){
        List<Review> reviews = this.reviewService.findAllReviews();
        List<ReviewDto> response = reviews.stream().map(r -> ReviewDto.builder()
                .id(r.getId())
                .content(r.getContent())
                .booking(r.getBooking() != null ? r.getBooking().getId() : null)
                .rating(r.getRating())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build()).toList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> findReviewById(@PathVariable Long reviewId) {
        try {
            Optional<Review> reviewOpt = this.reviewService.findReviewById(reviewId);
            if (reviewOpt.isEmpty()) {
                return new ResponseEntity<>("Review not found with id: " + reviewId, HttpStatus.NOT_FOUND);
            }
            Review review = reviewOpt.get();
            ReviewDto response = ReviewDto.builder()
                    .id(review.getId())
                    .content(review.getContent())
                    .booking(review.getBooking() != null ? review.getBooking().getId() : null)
                    .rating(review.getRating())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReviewById(@PathVariable Long reviewId) {
        try {
            boolean isDeleted = this.reviewService.deleteReviewById(reviewId);
            if(!isDeleted) return new ResponseEntity<>("Unable to delete Review", HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>("Review deleted successfully", HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody Review request){
        try {
            Review review = this.reviewService.updateReview(reviewId, request);
            ReviewDto response = ReviewDto.builder()
                    .id(review.getId())
                    .content(review.getContent())
                    .booking(review.getBooking() != null ? review.getBooking().getId() : null)
                    .rating(review.getRating())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
