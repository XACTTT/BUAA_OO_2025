import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BorrowAndReturnOffice {
    private ArrayList<LibraryBookId> returnedBooks;
    private HashMap<LibraryBookId, List<LibraryTrace>> traceMap;

    public BorrowAndReturnOffice(HashMap<LibraryBookId, List<LibraryTrace>> traceMap) {
        this.returnedBooks = new ArrayList<>();
        this.traceMap = traceMap;
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
