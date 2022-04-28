package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    /* Index Action - GET ALL reviews */
    @ApiOperation(value = "List all menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allReviews() {
        Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
        return reviews;
    }

    // @ApiOperation(value = "Get a single commons")
    // @PreAuthorize("hasRole('ROLE_USER')")
    // @GetMapping("")
    // public UCSBDiningCommons getById(
    //         @ApiParam("code") @RequestParam String code) {
    //     UCSBDiningCommons commons = ucsbDiningCommonsRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommons.class, code));

    //     return commons;
    // }

    /* Create Action - POST a new entry */
    @ApiOperation(value = "Create a new review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postReview(
        @ApiParam("itemId") @RequestParam long itemId,
        @ApiParam("reviewerEmail") @RequestParam String reviewerEmail,
        @ApiParam("stars") @RequestParam int stars,
        @ApiParam("dateReviewed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,  
        @ApiParam("comments") @RequestParam String comments
        )
        {

        MenuItemReview review = new MenuItemReview();
        review.setItemId(itemId);
        review.setReviewerEmail(reviewerEmail);
        review.setStars(stars);
        review.setDateReviewed(dateReviewed);
        review.setComments(comments);

        MenuItemReview savedReview = menuItemReviewRepository.save(review);

        return savedReview;
    }

    // @ApiOperation(value = "Delete a UCSBDiningCommons")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @DeleteMapping("")
    // public Object deleteCommons(
    //         @ApiParam("code") @RequestParam String code) {
    //     UCSBDiningCommons commons = ucsbDiningCommonsRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommons.class, code));

    //     ucsbDiningCommonsRepository.delete(commons);
    //     return genericMessage("UCSBDiningCommons with id %s deleted".formatted(code));
    // }

    // @ApiOperation(value = "Update a single commons")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @PutMapping("")
    // public UCSBDiningCommons updateCommons(
    //         @ApiParam("code") @RequestParam String code,
    //         @RequestBody @Valid UCSBDiningCommons incoming) {

    //     UCSBDiningCommons commons = ucsbDiningCommonsRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommons.class, code));


    //     commons.setName(incoming.getName());  
    //     commons.setHasSackMeal(incoming.getHasSackMeal());
    //     commons.setHasTakeOutMeal(incoming.getHasTakeOutMeal());
    //     commons.setHasDiningCam(incoming.getHasDiningCam());
    //     commons.setLatitude(incoming.getLatitude());
    //     commons.setLongitude(incoming.getLongitude());

    //     ucsbDiningCommonsRepository.save(commons);

    //     return commons;
    // }
}
