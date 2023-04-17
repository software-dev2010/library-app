package com.dgs.springbootlibrary.controller;

import com.dgs.springbootlibrary.entity.Book;
import com.dgs.springbootlibrary.responsemodels.ShelfCurrentLoansResponse;
import com.dgs.springbootlibrary.service.BookService;
import com.dgs.springbootlibrary.utils.ExtractJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/secure/currentloans")
    public List<ShelfCurrentLoansResponse> currentLoans(
            @RequestHeader(value = "Authorization") String token)
            throws Exception {
        log.info("Inside /api/books/secure/currentloans controller");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        return bookService.currentLoans(userEmail);
    }

    @GetMapping("/secure/currentloans/count")
    public int currentLoansCount(
            @RequestHeader(value = "Authorization") String token) {
        log.info("Inside /api/books/secure/currentloans/count");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        return bookService.currentLoansCount(userEmail);
    }

    @GetMapping("/secure/ischeckedout/byuser")
    public Boolean checkoutBookByUser(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam Long bookId) {
        log.info("Inside /api/books/secure/ischeckedout/byuser");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        return bookService.checkoutBookByUser(userEmail, bookId);
    }

    @PutMapping("/secure/checkout")
    public Book checkoutBook(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam Long bookId) throws Exception {
        log.info("Inside /api/books/secure/checkout");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        return bookService.checkoutBook(userEmail, bookId);
    }

    @PutMapping("/secure/return")
    public void returnBook(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam long bookId) throws Exception {
        log.info("Inside /api/books/secure/return");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        bookService.returnBook(userEmail, bookId);
    }

    @PutMapping("/secure/renew/loan")
    public void renewLoan(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam long bookId) throws Exception {
        log.info("Inside /api/books/secure/renew/loan");
        String userEmail = ExtractJWT.payloadJwtExtraction(token, "\"sub\"");
        bookService.renewLoan(userEmail, bookId);
    }
}