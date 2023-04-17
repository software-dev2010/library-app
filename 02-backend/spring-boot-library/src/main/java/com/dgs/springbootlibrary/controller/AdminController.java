package com.dgs.springbootlibrary.controller;

import com.dgs.springbootlibrary.entity.Book;
import com.dgs.springbootlibrary.requestmodels.AddBookRequest;
import com.dgs.springbootlibrary.service.AdminService;
import com.dgs.springbootlibrary.utils.ExtractJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin("https://localhost:3000")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/secure/increase/book/quantity")
    public void increaseBookQuantity(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam Long bookId) throws Exception {

        String admin = ExtractJWT.payloadJwtExtraction(token, "\"userType\"");
        if (admin == null || !admin.equals("admin")) {
            throw new Exception("Administration page only");
        }

        adminService.increaseBookQuantity(bookId);
    }

    @PutMapping("/secure/decrease/book/quantity")
    public void decreaseBookQuantity(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam Long bookId) throws Exception {

        String admin = ExtractJWT.payloadJwtExtraction(token, "\"userType\"");
        if (admin == null || !admin.equals("admin")) {
            throw new Exception("Administration page only");
        }

        adminService.decreaseBookQuantity(bookId);
    }

    @PostMapping("/secure/add/book")
    public void postBook(
            @RequestHeader(value = "Authorization") String token,
            @RequestBody AddBookRequest addBookRequest) throws Exception {

        String admin = ExtractJWT.payloadJwtExtraction(token, "\"userType\"");
        if (admin == null || !admin.equals("admin")) {
            throw new Exception("Administration page only.");
        }

        adminService.postBook(addBookRequest);
    }

    @DeleteMapping("/secure/delete/book")
    public void deleteBook(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam Long bookId) throws Exception {

        String admin = ExtractJWT.payloadJwtExtraction(token, "\"userType\"");
        if (admin == null || !admin.equals("admin")) {
            throw new Exception("Administration page only.");
        }

        adminService.deleteBook(bookId);
    }
}