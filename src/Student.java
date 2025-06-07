import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Student {
    private String studentId;
    private HashMap<LibraryBookId, LocalDate> borrowBooks;
    private HashMap<LibraryBookId, LocalDate> bookDate;
    private LibraryBookIsbn order;
    private LibraryBookId readingBook;
    private int creditScore;

    public Student(String id) {
        this.studentId = id;
        this.borrowBooks = new HashMap<>();
        this.order = null;
        this.readingBook = null;
        this.creditScore = 100;
        this.bookDate = new HashMap<>();
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void changeCreditScore(int value) {
        this.creditScore += value;
        if (this.creditScore >= 180) {
            this.creditScore = 180;
        } else if (this.creditScore <= 0) {
            this.creditScore = 0;
        }
    }

    public boolean hasBookToPick() {
        return (order != null);
    }

    public void orderBook(LibraryBookIsbn isbn) {
        this.order = isbn;
    }

    public void failOrder() {
        this.order = null;
    }

    public void pickBook(LibraryBookId bookId, LocalDate date) {
        this.order = null;
        this.borrowBook(bookId, date);
    }

    public boolean hasB() {
        for (LibraryBookId book : borrowBooks.keySet()) {
            if (book.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasC(LibraryBookIsbn isbn) {
        for (LibraryBookId book : borrowBooks.keySet()) {
            if (book.getBookIsbn().equals(isbn)) {
                return true;
            }
        }
        return false;
    }

    public void borrowBook(LibraryBookId bid, LocalDate date) {
        borrowBooks.put(bid, date);
        if (bid.isTypeB()) {
            bookDate.put(bid, date.plusDays(30));
        } else {
            bookDate.put(bid, date.plusDays(60));
        }
    }

    public void returnBook(LibraryBookId bid) {
        borrowBooks.remove(bid);
        bookDate.remove(bid);
    }

    public void readBook(LibraryBookId bid) {
        readingBook = bid;
    }

    public void restoreBook() {
        readingBook = null;
    }

    public boolean isReading() {
        return (readingBook != null);
    }

    public boolean checkReadPerm(LibraryBookIsbn isbn) {
        if (isbn.isTypeA()) {
            return creditScore >= 40;
        } else {
            return creditScore > 0;
        }
    }

    public boolean checkBorrowPerm() {
        return creditScore >= 60;
    }

    public boolean checkOrderPerm() {
        return creditScore >= 100;
    }

    public boolean checkDate(LibraryBookId bookId, LocalDate date) {
        LocalDate borrowDate = borrowBooks.get(bookId);
        LocalDate deadline;
        if (bookId.isTypeB()) {
            deadline = borrowDate.plusDays(30);
        } else {
            deadline = borrowDate.plusDays(60);
        }
        return !date.isAfter(deadline);
    }

    public void updateCs(LocalDate date) {
        for (LibraryBookId bookId : borrowBooks.keySet()) {
            LocalDate date1 = bookDate.get(bookId);
            if (date.isAfter(date1)) {
                changeCreditScore((int) (date1.toEpochDay() - date.toEpochDay()) * 5);
                bookDate.remove(bookId);
                bookDate.put(bookId, date);
            }

        }
    }
}
