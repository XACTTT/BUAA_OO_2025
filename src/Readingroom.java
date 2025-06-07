import com.oocourse.library3.LibraryBookId;

import java.util.ArrayList;

public class Readingroom {
    private ArrayList<LibraryBookId> books;

    public Readingroom() {
        this.books = new ArrayList<>();
    }

    public void receiveBook(LibraryBookId bookId) {
        books.add(bookId);
    }

    public ArrayList<LibraryBookId> arrangeBooks() {
        ArrayList<LibraryBookId> returnedBooks = new ArrayList<>(this.books);
        this.books.clear();
        return returnedBooks;
    }

    public void restoreBook(LibraryBookId bookId) {
        this.books.remove(bookId);
    }
}