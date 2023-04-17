package com.dgs.springbootlibrary.service;

import com.dgs.springbootlibrary.dao.BookRepository;
import com.dgs.springbootlibrary.dao.CheckoutRepository;
import com.dgs.springbootlibrary.dao.HistoryRepository;
import com.dgs.springbootlibrary.dao.PaymentRepository;
import com.dgs.springbootlibrary.entity.Book;
import com.dgs.springbootlibrary.entity.Checkout;
import com.dgs.springbootlibrary.entity.History;
import com.dgs.springbootlibrary.entity.Payment;
import com.dgs.springbootlibrary.responsemodels.ShelfCurrentLoansResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final CheckoutRepository checkoutRepository;

    private final HistoryRepository historyRepository;

    private final PaymentRepository paymentRepository;

    @Autowired
    public BookService(
            BookRepository bookRepository,
            CheckoutRepository checkoutRepository,
            HistoryRepository historyRepository,
            PaymentRepository paymentRepository) {
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
    }

    public Book checkoutBook(String userEmail, Long bookId) throws Exception {
        Optional<Book> book = bookRepository.findById(bookId);

        // Why are we checking for a specific book when a user is trying to check out the book?
        // Well, we only want a user to be able to check out a single book on time.
        // We don't want to allow a user to check out the same book more than once.
        // So, we're just checking to make sure validate checkout is equal to null
        // because if it's not equal to null, that means we found a book in the database
        // where the user email and bookId matches the parameters that we're passing in.
        Checkout validateCheckout =
                checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (!book.isPresent() ||
                validateCheckout != null ||
                book.get().getCopiesAvailable() <= 0) {
            throw new Exception("Book doesn't exist or already checkout by user!");
        }

        List<Checkout> currentBookCheckedOut =
                checkoutRepository.findBooksByUserEmail(userEmail);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean bookNeedsReturned = false;

        for (Checkout checkout : currentBookCheckedOut) {
            Date d1 = sdf.parse(checkout.getReturnDate());
            Date d2 = sdf.parse(LocalDate.now().toString());
            TimeUnit time = TimeUnit.DAYS;
            double differenceInTime = time.convert(
                    d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);

            log.debug("differenceInTime = {}", differenceInTime);

            if (differenceInTime < 0) {
                bookNeedsReturned = true;
                break;
            }
        }

        Payment userPayment = paymentRepository.findByUserEmail(userEmail);

        if ((userPayment != null && userPayment.getAmount() > 0) ||
                (userPayment != null && bookNeedsReturned)) {
            throw new Exception("Outstanding fees");
        }

        if (userPayment == null) {
            Payment payment = new Payment();
            payment.setAmount(00.00);
            payment.setUserEmail(userEmail);
            paymentRepository.save(payment);
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() - 1);
        bookRepository.save(book.get());

        Checkout checkout = new Checkout(
                userEmail,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(7).toString(),
                book.get().getId());

        checkoutRepository.save(checkout);

        return book.get();
    }

    public Boolean checkoutBookByUser(String userEmail, Long bookId) {
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if (validateCheckout != null) {
            return true;
        } else {
            return false;
        }
    }

    public int currentLoansCount(String userEmail) {
        return checkoutRepository.findBooksByUserEmail(userEmail).size();
    }

    public List<ShelfCurrentLoansResponse> currentLoans(String userEmail) throws Exception {
        List<ShelfCurrentLoansResponse> shelfCurrentLoansResponses = new ArrayList<>();
        List<Checkout> checkoutList = checkoutRepository
                .findBooksByUserEmail(userEmail);

        List<Long> bookIdList = new ArrayList<>();
        for (Checkout checkout : checkoutList) {
            bookIdList.add(checkout.getBookId());
        }
        List<Book> books = bookRepository.findBooksByBookIds(bookIdList);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Book book : books) {
            Optional<Checkout> checkout = checkoutList.stream()
                    .filter(c -> c.getBookId() == book.getId()).findFirst();

            if (checkout.isPresent()) {
                Date d1 = sdf.parse(checkout.get().getReturnDate());
                Date d2 = sdf.parse(LocalDate.now().toString());
                TimeUnit time = TimeUnit.DAYS;
                long differenceInTime = time.convert(
                        d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);
                shelfCurrentLoansResponses.add(
                        new ShelfCurrentLoansResponse(book, (int) differenceInTime));
            }
        }
        return shelfCurrentLoansResponses;
    }

    public void returnBook(String userEmail, long bookId) throws Exception {
        Optional<Book> book = bookRepository.findById(bookId);
        log.debug("book: {} retrieved from db for book id: {}", book, bookId);
        Checkout validateCheckout = checkoutRepository
                .findByUserEmailAndBookId(userEmail, bookId);

        log.debug("validateCheckout: {} retrieved from db for email address: {} and book id: {}",
                validateCheckout, userEmail, bookId);

        if (!book.isPresent() || validateCheckout == null) {
            throw new Exception("Book does not exist or not checkout out by user");
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() + 1);
        bookRepository.save(book.get());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = sdf.parse(validateCheckout.getReturnDate());
        Date d2 = sdf.parse(LocalDate.now().toString());
        TimeUnit time = TimeUnit.DAYS;
        double differenceInTime = time.convert(
                d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);

        if (differenceInTime < 0) {
            Payment payment = paymentRepository.findByUserEmail(userEmail);
            payment.setAmount(payment.getAmount() + (differenceInTime * -1));
            paymentRepository.save(payment);
        }

        checkoutRepository.deleteById(validateCheckout.getId());

        History history = new History(
                userEmail,
                validateCheckout.getCheckoutDate(),
                LocalDate.now().toString(),
                book.get().getTitle(),
                book.get().getAuthor(),
                book.get().getDescription(),
                book.get().getImg()
        );

        historyRepository.save(history);
    }

    public void renewLoan(String userEmail, long bookId) throws Exception {
        Checkout validateCheckout = checkoutRepository
                .findByUserEmailAndBookId(userEmail, bookId);

        if (validateCheckout == null) {
            throw new Exception("Book does not exist or not checked out by user");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = sdf.parse(validateCheckout.getReturnDate());
        Date d2 = sdf.parse(LocalDate.now().toString());

        if (d1.compareTo(d2) > 0 || d1.compareTo(d2) == 0) {
            validateCheckout.setReturnDate(LocalDate.now().plusDays(7).toString());
            checkoutRepository.save(validateCheckout);
        }
    }
}