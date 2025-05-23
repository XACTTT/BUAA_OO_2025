import com.oocourse.library1.LibraryBookId;

import java.util.ArrayList;

public class BorrowAndReturnOffice {
    private ArrayList<LibraryBookId> returnedBooks;

    public BorrowAndReturnOffice() {
        this.returnedBooks = new ArrayList<>();
    }

    public void receiveBook(LibraryBookId bookId) {
        returnedBooks.add(bookId);
    }

    public ArrayList<LibraryBookId> arrangeReturnedBooks() {
        ArrayList<LibraryBookId> returnedBooks = new ArrayList<>(this.returnedBooks);
        this.returnedBooks.clear();
        return returnedBooks;
    }
}
