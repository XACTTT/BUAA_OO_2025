import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import java.util.ArrayList;

public class Student {
    private String studentId;
    private ArrayList<LibraryBookId> borrowBooks;
    private LibraryBookIsbn order;

    public Student(String id) {
        this.studentId = id;
        this.borrowBooks = new ArrayList<>();
        this.order = null;
    }

    public boolean hasBookToPick() {
        return (order != null);
    }

    public void orderBook(LibraryBookIsbn isbn) {
        this.order = isbn;
    }

    public void pickBook(LibraryBookId bookId) {
        this.order = null;
        this.borrowBook(bookId);
    }

    public boolean hasB() {
        for (LibraryBookId book : borrowBooks) {
            if (book.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasC(LibraryBookIsbn isbn) {
        for (LibraryBookId book : borrowBooks) {
            if (book.getBookIsbn().equals(isbn)) {
                return true;
            }
        }
        return false;
    }

    public void borrowBook(LibraryBookId bid) {
        borrowBooks.add(bid);
    }

    public void returnBook(LibraryBookId bid) {
        borrowBooks.remove(bid);
    }
}
