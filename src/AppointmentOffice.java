import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryTrace;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryBookState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.oocourse.library3.LibraryIO.PRINTER;

public class AppointmentOffice {

    private HashMap<LibraryBookId, LocalDate> keptBooksByDate;
    private HashMap<String, LibraryBookId> keptBooksByStu;
    private HashMap<String, Student> students;
    private HashMap<LibraryBookId, List<LibraryTrace>> traceMap;

    public AppointmentOffice(HashMap<String, Student> students, HashMap<LibraryBookId,
            List<LibraryTrace>> traceMap) {
        this.students = students;
        keptBooksByDate = new HashMap<>();
        keptBooksByStu = new HashMap<>();
        this.traceMap = traceMap;
    }

    public ArrayList<LibraryBookId> arrangeOverdueBooks(LocalDate date) {
        List<LibraryMoveInfo> infos = new ArrayList<>();
        ArrayList<LibraryBookId> removeBooks = new ArrayList<>();
        for (LibraryBookId bookId : keptBooksByDate.keySet()) {
            LocalDate date1 = keptBooksByDate.get(bookId);
            if (date.toEpochDay() - date1.toEpochDay() > 5) {
                removeBooks.add(bookId);
            }
        }
        for (LibraryBookId bookId : removeBooks) {
            keptBooksByDate.remove(bookId);
        }
        ArrayList<String> removeStudents = new ArrayList<>();
        for (String studentId : keptBooksByStu.keySet()) {
            if (removeBooks.contains(keptBooksByStu.get(studentId))) {
                removeStudents.add(studentId);
            }
        }
        for (String studentId : removeStudents) {
            students.get(studentId).failOrder();
            students.get(studentId).changeCreditScore(-15);
            keptBooksByStu.remove(studentId);
        }

        return removeBooks;
    }

    public void receiveOrderedBooks(HashMap<LibraryBookId, String> books, LocalDate date) {
        for (LibraryBookId bookId : books.keySet()) {
            keptBooksByDate.put(bookId, date);
            keptBooksByStu.put(books.get(bookId), bookId);
        }
    }

    public void pickBook(LibraryReqCmd req) {
        String studentId = req.getStudentId();
        if (keptBooksByStu.containsKey(studentId)) {
            if (req.getBookIsbn().isTypeA()) {
                PRINTER.reject(req);
            } else if (req.getBookIsbn().isTypeB()) {
                if (students.get(studentId).hasB()) {
                    PRINTER.reject(req);
                } else {
                    exchangeBook(req, studentId);
                }
            } else {
                if (students.get(studentId).hasC(req.getBookIsbn())) {
                    PRINTER.reject(req);
                } else {
                    exchangeBook(req, studentId);
                }
            }
        } else {
            PRINTER.reject(req);
        }
    }

    private void exchangeBook(LibraryReqCmd req, String studentId) {
        LibraryBookId bookId = keptBooksByStu.get(studentId);
        keptBooksByStu.remove(studentId);
        keptBooksByDate.remove(bookId);
        students.get(studentId).pickBook(bookId,req.getDate());
        updateTrace(bookId, req.getDate(), 6);
        PRINTER.accept(req, bookId);
    }

    public void updateTrace(LibraryBookId bookId, LocalDate date, int type) {
        List<LibraryTrace> traces = traceMap.get(bookId);
        LibraryTrace newTrace = null;
        if (type == 1) {
            newTrace = new LibraryTrace(date, LibraryBookState.BOOKSHELF,
                    LibraryBookState.USER);
        } else if (type == 2) {
            newTrace = new LibraryTrace(date, LibraryBookState.BOOKSHELF,
                    LibraryBookState.APPOINTMENT_OFFICE);
        } else if (type == 3) {
            newTrace = new LibraryTrace(date, LibraryBookState.USER,
                    LibraryBookState.BORROW_RETURN_OFFICE);
        } else if (type == 4) {
            newTrace = new LibraryTrace(date, LibraryBookState.APPOINTMENT_OFFICE,
                    LibraryBookState.BOOKSHELF);
        } else if (type == 5) {
            newTrace = new LibraryTrace(date, LibraryBookState.BORROW_RETURN_OFFICE,
                    LibraryBookState.BOOKSHELF);
        } else if (type == 6) {
            newTrace = new LibraryTrace(date, LibraryBookState.APPOINTMENT_OFFICE,
                    LibraryBookState.USER);
        }
        traces.add(newTrace);
        traceMap.put(bookId, traces);
    }
}