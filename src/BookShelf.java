import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookShelf {
    private Map<LibraryBookIsbn, ArrayList<String>> libraryBooks;
    private HashMap<LibraryBookId, List<LibraryTrace>> traceMap;

    public BookShelf(HashMap<LibraryBookId, List<LibraryTrace>> traceMap) {
        this.traceMap = traceMap;
        libraryBooks = new HashMap<>();
    }

    public void init(Map<LibraryBookIsbn, Integer> libraryBookIsbnMap) {
        for (LibraryBookIsbn isbn : libraryBookIsbnMap.keySet()) {
            int num = libraryBookIsbnMap.get(isbn);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 1; i <= num; i++) {
                String copyId;
                if (i < 10) {
                    copyId = "0" + i;
                } else {
                    copyId = "" + i;
                }
                list.add(copyId);
                LibraryBookId bookId = new LibraryBookId(isbn.getType(), isbn.getUid(), copyId);
                traceMap.put(bookId, new ArrayList<>());
            }

            libraryBooks.put(isbn, list);
        }
    }

    public Map<LibraryBookIsbn, ArrayList<String>> getBooks() {
        return libraryBooks;
    }

    public boolean containsBook(LibraryBookIsbn isbn) {
        if (libraryBooks.containsKey(isbn)) {
            int num = libraryBooks.get(isbn).size();
            return num > 0;
        }
        return false;
    }

    public void addBooks(List<LibraryBookId> books) {
        for (LibraryBookId bookId : books) {
            LibraryBookIsbn isbn = new LibraryBookIsbn(bookId.getType(), bookId.getUid());
            ArrayList<String> copyIds = libraryBooks.get(isbn);
            copyIds.add(bookId.getCopyId());
            libraryBooks.put(isbn, copyIds);
        }

    }

}
