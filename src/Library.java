import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryTrace;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryBookIsbn;

import static com.oocourse.library1.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    private BookShelf libBookShelf;
    private BorrowAndReturnOffice borrowOffice;
    private AppointmentOffice appointmentOffice;
    private HashMap<LibraryBookId, List<LibraryTrace>> traceMap;
    private HashMap<String, Student> students;
    private HashMap<String, LibraryBookIsbn> orderBooks;

    public Library() {
        this.traceMap = new HashMap<>();
        this.students = new HashMap<>();
        this.orderBooks = new HashMap<>();
        this.appointmentOffice = new AppointmentOffice(students, traceMap);
        this.borrowOffice = new BorrowAndReturnOffice();
        this.libBookShelf = new BookShelf(traceMap);
    }

    public void init(Map<LibraryBookIsbn, Integer> libraryBookIsbnMap) {
        libBookShelf.init(libraryBookIsbnMap);
    }

    public void addBooks(List<LibraryBookId> books) {
        libBookShelf.addBooks(books);
    }

    public void arrangeOpen(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = arrangeOverdueBooks(date);
        PRINTER.move(date, infos);
    }

    public void arrangeClose(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = arrangeOverdueBooks(date);
        infos.addAll(arrangeReturnedBooks(date));
        infos.addAll(arrangeOrderedBooks(date));
        PRINTER.move(date, infos);
    }

    public ArrayList<LibraryMoveInfo> arrangeOverdueBooks(LocalDate date) {
        ArrayList<LibraryBookId> removeBooks = appointmentOffice.arrangeOverdueBooks(date);
        addBooks(removeBooks);
        for (LibraryBookId bookId : removeBooks) {
            updateTrace(bookId, date, 4);
        }
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (LibraryBookId bookId : removeBooks) {
            LibraryMoveInfo info = new LibraryMoveInfo(bookId,
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF);
            infos.add(info);
        }
        return infos;
    }

    public ArrayList<LibraryMoveInfo> arrangeReturnedBooks(LocalDate date) {
        ArrayList<LibraryBookId> returnedBooks = borrowOffice.arrangeReturnedBooks();
        addBooks(returnedBooks);
        for (LibraryBookId bookId : returnedBooks) {
            updateTrace(bookId, date, 5);
        }
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (LibraryBookId bookId : returnedBooks) {
            LibraryMoveInfo info = new LibraryMoveInfo(bookId,
                    LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF);
            infos.add(info);
        }
        return infos;
    }

    public ArrayList<LibraryMoveInfo> arrangeOrderedBooks(LocalDate date) {
        HashMap<LibraryBookId, String> reservedBooks = getOrderedBooksId();
        for (LibraryBookId bookId : reservedBooks.keySet()) {
            updateTrace(bookId, date, 2);
        }
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (LibraryBookId bookId : reservedBooks.keySet()) {
            LibraryMoveInfo info = new LibraryMoveInfo(bookId,
                    LibraryBookState.BOOKSHELF, LibraryBookState.APPOINTMENT_OFFICE,
                    reservedBooks.get(bookId));
            infos.add(info);
        }
        appointmentOffice.receiveOrderedBooks(reservedBooks, date);
        orderBooks.clear();
        return infos;
    }

    private HashMap<LibraryBookId, String> getOrderedBooksId() {
        HashMap<LibraryBookId, String> reservedBooks = new HashMap<>();
        for (String studentId : orderBooks.keySet()) {
            LibraryBookIsbn isbn = orderBooks.get(studentId);
            if (libBookShelf.containsBook(isbn)) {
                ArrayList<String> books = libBookShelf.getBooks().get(isbn);
                String copyId = books.remove(0);
                LibraryBookId book = new LibraryBookId(isbn.getType(),
                        isbn.getUid(), copyId);
                reservedBooks.put(book, studentId);
            }
        }
        return reservedBooks;
    }

    public void queryTrace(LibraryReqCmd req) {
        PRINTER.info(req.getDate(), req.getBookId(), traceMap.get(req.getBookId()));
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

    public void borrowBook(LibraryReqCmd req) {
        if (req.getBookIsbn().isTypeA()) {
            PRINTER.reject(req);
        } else if (req.getBookIsbn().isTypeB()) {
            this.borrowB(req);
        } else if (req.getBookIsbn().isTypeC()) {
            this.borrowC(req);
        }
    }

    public void returnBook(LibraryReqCmd req, LocalDate date) {
        students.get(req.getStudentId()).returnBook(req.getBookId());
        borrowOffice.receiveBook(req.getBookId());
        updateTrace(req.getBookId(), date, 3);
        PRINTER.accept(req);
    }

    private void borrowB(LibraryReqCmd req) {
        if (!libBookShelf.containsBook(req.getBookIsbn())) {
            PRINTER.reject(req);
        } else {
            String studentId = req.getStudentId();
            if (students.containsKey(studentId)) {
                if (students.get(studentId).hasB()) {
                    PRINTER.reject(req);
                } else {
                    exchangeBook(req, studentId);
                }

            } else {
                students.put(studentId, new Student(studentId));
                exchangeBook(req, studentId);
            }
        }
    }

    private void borrowC(LibraryReqCmd req) {
        if (!libBookShelf.containsBook(req.getBookIsbn())) {
            PRINTER.reject(req);
        } else {
            String studentId = req.getStudentId();
            if (students.containsKey(studentId)) {
                if (students.get(studentId).hasC(req.getBookIsbn())) {
                    PRINTER.reject(req);
                } else {
                    exchangeBook(req, studentId);
                }

            } else {
                students.put(studentId, new Student(studentId));
                exchangeBook(req, studentId);
            }
        }
    }

    private void exchangeBook(LibraryReqCmd req, String studentId) {
        ArrayList<String> books = libBookShelf.getBooks().get(req.getBookIsbn());
        String copyId = books.remove(0);
        LibraryBookId book = new LibraryBookId(req.getBookIsbn().getType(),
                req.getBookIsbn().getUid(), copyId);
        students.get(studentId).borrowBook(book);
        updateBook(req.getBookIsbn(), books);
        updateTrace(book, req.getDate(), 1);
        PRINTER.accept(req, book);
    }

    private void updateBook(LibraryBookIsbn isbn, ArrayList<String> books) {
        libBookShelf.getBooks().put(isbn, books);
    }

    public void orderBook(LibraryReqCmd req) {
        if (checkPermission(req)) {
            orderBooks.put(req.getStudentId(),req.getBookIsbn());
            students.get(req.getStudentId()).orderBook(req.getBookIsbn());
            PRINTER.accept(req);
        }
    }

    private boolean checkPermission(LibraryReqCmd req) {
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student;
        if (students.containsKey(req.getStudentId())) {
            student = students.get(req.getStudentId());
        } else {
            student = new Student(req.getStudentId());
            students.put(req.getStudentId(), student);
        }
        if (student.hasBookToPick()) {
            PRINTER.reject(req);
            return false;
        } else {
            if (isbn.isTypeA()) {
                PRINTER.reject(req);
                return false;
            } else if (isbn.isTypeB()) {
                if (student.hasB()) {
                    PRINTER.reject(req);
                    return false;
                } else {
                    return true;
                }
            } else {
                if (student.hasC(isbn)) {
                    PRINTER.reject(req);
                    return false;
                } else {
                    return true;
                }
            }

        }
    }

    public void pickBook(LibraryReqCmd req) {
        appointmentOffice.pickBook(req);
    }

}
