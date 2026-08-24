package com.uberreviewservice.adapters;

import com.example.uberentityservice.models.Review;
import com.uberreviewservice.dtos.CreateReviewDto;




public interface CreateReviewDtoToReviewAdapter {

    Review convertDto(CreateReviewDto createReviewDto);
}
