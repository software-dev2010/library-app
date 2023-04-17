package com.dgs.springbootlibrary.controller;

import com.dgs.springbootlibrary.requestmodels.ReviewRequest;
import com.dgs.springbootlibrary.service.ReviewService;
import com.dgs.springbootlibrary.utils.ExtractJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("https://localhost:3000")
@RestController()
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController {

    private ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/secure/user/book")
    public Boolean reviewBookByUser(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam Long bookId) throws Exception {
        log.info("Inside /api/reviews/secure/user/book controller");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        if (userEmail == null) {
            throw new Exception("User email is missing");
        }
        return reviewService.userReviewListed(userEmail, bookId);
    }

    @PostMapping("/secure")
    public void postReview(
            @RequestHeader(value = "Authorization") String token,
            @RequestBody ReviewRequest reviewRequest) throws Exception {
        log.info("Inside /api/reviews/secure controller");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        if (userEmail == null) {
            throw new Exception("User email is missing");
        }

        reviewService.postReview(userEmail, reviewRequest);
    }
}