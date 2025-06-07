import com.oocourse.library3.*;
import com.oocourse.library3.annotation.Trigger;

import static com.oocourse.library3.LibraryIO.PRINTER;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private BookShelf libBookShelf;
    private BookShelf hotBookShelf;
    private BorrowAndReturnOffice borrowOffice;
    private Readingroom readingroom;
    private AppointmentOffice appointmentOffice;
    private HashMap<LibraryBookId, List<LibraryTrace>> traceMap;
    private HashMap<String, Student> students;
    private HashMap<String, LibraryBookIsbn> orderBooks;
    private HashSet<LibraryBookIsbn> hotBooks;

    public Library() {
        this.traceMap = new HashMap<>();
        this.students = new HashMap<>();
        this.orderBooks = new HashMap<>();
        this.appointmentOffice = new AppointmentOffice(students, traceMap);
        this.borrowOffice = new BorrowAndReturnOffice();
        this.libBookShelf = new BookShelf(traceMap);
        this.hotBookShelf = new BookShelf(traceMap);
        this.readingroom = new Readingroom();
        this.hotBooks = new HashSet<>();
    }

    public void init(Map<LibraryBookIsbn, Integer> libraryBookIsbnMap) {
        libBookShelf.init(libraryBookIsbnMap);
        hotBookShelf.initHotBookShelf(libraryBookIsbnMap);
    }

    public void addBooks(List<LibraryBookId> books) {
        libBookShelf.addBooks(books);
    }

    public void arrangeOpen(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = arrangeOverdueBooks(date);
        infos.addAll(arrangeHotBooks(date));
        hotBooks.clear();
        PRINTER.move(date, infos);
        for (Student student : students.values()) {
            student.updateCs(date);
            if (student.isReading()) {
                student.changeCreditScore(-10);
            }
            student.restoreBook();
        }
    }

    public void arrangeClose(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = arrangeOverdueBooks(date);
        infos.addAll(arrangeReturnedBooks(date));
        infos.addAll(arrangeReadBooks(date));
        infos.addAll(arrangeOrderedBooks(date));
        PRINTER.move(date, infos);
    }

    @Trigger(from = "hbs", to = "bs")
    @Trigger(from = "bs", to = "hbs")
    public ArrayList<LibraryMoveInfo> arrangeHotBooks(LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (LibraryBookIsbn isbn : hotBookShelf.getBooks().keySet()) {
            if (hotBookShelf.containsBook(isbn) && !hotBooks.contains(isbn)) {
                ArrayList<String> copyIds = hotBookShelf.getBooks().get(isbn);
                ArrayList<LibraryBookId> bookIds = new ArrayList<>();
                for (String id : copyIds) {
                    LibraryBookId book = new LibraryBookId(isbn.getType(), isbn.getUid(), id);
                    LibraryMoveInfo info = new LibraryMoveInfo(book, LibraryBookState.HOT_BOOKSHELF,
                            LibraryBookState.BOOKSHELF);
                    updateTrace(book, date, 7);
                    bookIds.add(book);
                    infos.add(info);
                }
                libBookShelf.addBooks(bookIds);
                hotBookShelf.getBooks().put(isbn, new ArrayList<>());

            }
        }
        for (LibraryBookIsbn isbn : libBookShelf.getBooks().keySet()) {
            if (libBookShelf.containsBook(isbn) && hotBooks.contains(isbn)) {
                ArrayList<String> copyIds = libBookShelf.getBooks().get(isbn);
                ArrayList<LibraryBookId> bookIds = new ArrayList<>();
                for (String id : copyIds) {
                    LibraryBookId book = new LibraryBookId(isbn.getType(), isbn.getUid(), id);
                    LibraryMoveInfo info = new LibraryMoveInfo(book, LibraryBookState.BOOKSHELF,
                            LibraryBookState.HOT_BOOKSHELF);
                    updateTrace(book, date, 8);
                    bookIds.add(book);
                    infos.add(info);
                }
                hotBookShelf.addBooks(bookIds);
                libBookShelf.getBooks().put(isbn, new ArrayList<>());
            }
        }
        return infos;
    }

    @Trigger(from = "ao", to = "bs")
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

    @Trigger(from = "bro", to = "bs")
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

    @Trigger(from = "rr", to = "bs")
    public ArrayList<LibraryMoveInfo> arrangeReadBooks(LocalDate date) {
        ArrayList<LibraryBookId> readBooks = readingroom.arrangeBooks();
        addBooks(readBooks);
        for (LibraryBookId bookId : readBooks) {
            updateTrace(bookId, date, 14);
        }
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (LibraryBookId bookId : readBooks) {
            LibraryMoveInfo info = new LibraryMoveInfo(bookId,
                    LibraryBookState.READING_ROOM, LibraryBookState.BOOKSHELF);
            infos.add(info);
        }
        return infos;
    }

    @Trigger(from = "bs", to = "ao")
    @Trigger(from = "hbs", to = "ao")
    public ArrayList<LibraryMoveInfo> arrangeOrderedBooks(LocalDate date) {
        HashMap<LibraryBookId, String> reservedBooks = new HashMap<>();
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        for (String studentId : orderBooks.keySet()) {
            LibraryBookIsbn isbn = orderBooks.get(studentId);
            if (libBookShelf.containsBook(isbn)) {
                ArrayList<String> books = libBookShelf.getBooks().get(isbn);
                String copyId = books.remove(0);
                LibraryBookId book = new LibraryBookId(isbn.getType(),
                        isbn.getUid(), copyId);
                reservedBooks.put(book, studentId);
                updateTrace(book, date, 2);
                LibraryMoveInfo info = new LibraryMoveInfo(book,
                        LibraryBookState.BOOKSHELF, LibraryBookState.APPOINTMENT_OFFICE,
                        reservedBooks.get(book));
                infos.add(info);
            } else if (hotBookShelf.containsBook(isbn)) {
                ArrayList<String> books = hotBookShelf.getBooks().get(isbn);
                String copyId = books.remove(0);
                LibraryBookId book = new LibraryBookId(isbn.getType(),
                        isbn.getUid(), copyId);
                reservedBooks.put(book, studentId);
                updateTrace(book, date, 10);
                LibraryMoveInfo info = new LibraryMoveInfo(book,
                        LibraryBookState.HOT_BOOKSHELF, LibraryBookState.APPOINTMENT_OFFICE,
                        reservedBooks.get(book));
                infos.add(info);
            }
        }
        appointmentOffice.receiveOrderedBooks(reservedBooks, date);
        orderBooks.clear();
        return infos;
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
        } else if (type == 7) {
            newTrace = new LibraryTrace(date, LibraryBookState.HOT_BOOKSHELF,
                    LibraryBookState.BOOKSHELF);
        } else if (type == 8) {
            newTrace = new LibraryTrace(date, LibraryBookState.BOOKSHELF,
                    LibraryBookState.HOT_BOOKSHELF);
        } else if (type == 9) {
            newTrace = new LibraryTrace(date, LibraryBookState.HOT_BOOKSHELF,
                    LibraryBookState.USER);
        } else if (type == 10) {
            newTrace = new LibraryTrace(date, LibraryBookState.HOT_BOOKSHELF,
                    LibraryBookState.APPOINTMENT_OFFICE);
        } else if (type == 11) {
            newTrace = new LibraryTrace(date, LibraryBookState.HOT_BOOKSHELF,
                    LibraryBookState.READING_ROOM);
        } else if (type == 12) {
            newTrace = new LibraryTrace(date, LibraryBookState.BOOKSHELF,
                    LibraryBookState.READING_ROOM);
        } else if (type == 13) {
            newTrace = new LibraryTrace(date, LibraryBookState.READING_ROOM,
                    LibraryBookState.BORROW_RETURN_OFFICE);
        } else if (type == 14) {
            newTrace = new LibraryTrace(date, LibraryBookState.READING_ROOM,
                    LibraryBookState.BOOKSHELF);
        }
        traces.add(newTrace);
        traceMap.put(bookId, traces);
    }

    public void queryCs(LibraryQcsCmd req) {
        if (students.containsKey(req.getStudentId())) {
            int value = students.get(req.getStudentId()).getCreditScore();
            PRINTER.info(req, value);
        }else {
            students.put(req.getStudentId(), new Student(req.getStudentId()));
            int value = students.get(req.getStudentId()).getCreditScore();
            PRINTER.info(req, value);
        }

    }

    @Trigger(from = "bs", to = "user")
    @Trigger(from = "hbs", to = "user")
    public void borrowBook(LibraryReqCmd req) {
        if (req.getBookIsbn().isTypeA()) {
            PRINTER.reject(req);
        } else if (req.getBookIsbn().isTypeB()) {
            this.borrowB(req);
        } else if (req.getBookIsbn().isTypeC()) {
            this.borrowC(req);
        }
    }

    @Trigger(from = "user", to = "bro")
    public void returnBook(LibraryReqCmd req, LocalDate date) {
        int overdueSign = 1;
        if (students.get(req.getStudentId()).checkDate(req.getBookId(), date)) {
            overdueSign = 0;
        }
        students.get(req.getStudentId()).returnBook(req.getBookId());
        borrowOffice.receiveBook(req.getBookId());
        updateTrace(req.getBookId(), date, 3);
        if (overdueSign == 0) {
            PRINTER.accept(req, "not overdue");
            students.get(req.getStudentId()).changeCreditScore(10);
        } else {
            PRINTER.accept(req, "overdue");
        }

    }

    private void borrowB(LibraryReqCmd req) {
        if (!libBookShelf.containsBook(req.getBookIsbn()) &&
                !hotBookShelf.containsBook(req.getBookIsbn())) {
            PRINTER.reject(req);
        } else {
            String studentId = req.getStudentId();
            if (students.containsKey(studentId)) {
                if (students.get(studentId).hasB()) {
                    PRINTER.reject(req);
                } else {
                    if (!students.get(req.getStudentId()).checkBorrowPerm()) {
                        PRINTER.reject(req);
                    } else {
                        exchangeBook(req, studentId);
                        hotBooks.add(req.getBookIsbn());
                    }

                }

            } else {
                students.put(studentId, new Student(studentId));
                exchangeBook(req, studentId);
                hotBooks.add(req.getBookIsbn());
            }
        }
    }

    private void borrowC(LibraryReqCmd req) {
        if (!libBookShelf.containsBook(req.getBookIsbn()) &&
                !hotBookShelf.containsBook(req.getBookIsbn())) {
            PRINTER.reject(req);
        } else {
            String studentId = req.getStudentId();
            if (students.containsKey(studentId)) {
                if (students.get(studentId).hasC(req.getBookIsbn())) {
                    PRINTER.reject(req);
                } else {
                    if (!students.get(req.getStudentId()).checkBorrowPerm()) {
                        PRINTER.reject(req);
                    } else {
                        exchangeBook(req, studentId);
                        hotBooks.add(req.getBookIsbn());
                    }

                }

            } else {
                students.put(studentId, new Student(studentId));
                exchangeBook(req, studentId);
                hotBooks.add(req.getBookIsbn());
            }
        }
    }

    private void exchangeBook(LibraryReqCmd req, String studentId) {
        ArrayList<String> books;
        if (libBookShelf.containsBook(req.getBookIsbn())) {
            books = libBookShelf.getBooks().get(req.getBookIsbn());
            String copyId = books.remove(0);
            LibraryBookId book = new LibraryBookId(req.getBookIsbn().getType(),
                    req.getBookIsbn().getUid(), copyId);
            students.get(studentId).borrowBook(book, req.getDate());
            updateBook(req.getBookIsbn(), books);
            updateTrace(book, req.getDate(), 1);
            PRINTER.accept(req, book);
        } else {
            books = hotBookShelf.getBooks().get(req.getBookIsbn());
            String copyId = books.remove(0);
            LibraryBookId book = new LibraryBookId(req.getBookIsbn().getType(),
                    req.getBookIsbn().getUid(), copyId);
            students.get(studentId).borrowBook(book, req.getDate());
            updateHotBook(req.getBookIsbn(), books);
            updateTrace(book, req.getDate(), 9);
            PRINTER.accept(req, book);
        }

    }

    private void updateBook(LibraryBookIsbn isbn, ArrayList<String> books) {
        libBookShelf.getBooks().put(isbn, books);
    }

    private void updateHotBook(LibraryBookIsbn isbn, ArrayList<String> books) {
        hotBookShelf.getBooks().put(isbn, books);
    }

    public void orderBook(LibraryReqCmd req) {
        if (checkPermission(req)) {
            orderBooks.put(req.getStudentId(), req.getBookIsbn());
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
            if (student.checkOrderPerm()) {

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
            } else {
                PRINTER.reject(req);
                return false;
            }
        }
    }

    @Trigger(from = "ao", to = "user")
    public void pickBook(LibraryReqCmd req) {
        appointmentOffice.pickBook(req);
    }

    @Trigger(from = "bs", to = "rr")
    @Trigger(from = "hbs", to = "rr")
    public void readBook(LibraryReqCmd req) {
        if (!libBookShelf.containsBook(req.getBookIsbn()) &&
                !hotBookShelf.containsBook(req.getBookIsbn())) {
            PRINTER.reject(req);
        } else {
            String studentId = req.getStudentId();
            if (students.containsKey(studentId)) {
                if (students.get(studentId).checkReadPerm(req.getBookIsbn())) {
                    if (students.get(studentId).isReading()) {
                        PRINTER.reject(req);
                    } else {
                        readingBook(req, studentId);
                    }
                } else {
                    PRINTER.reject(req);
                }
            } else {
                students.put(studentId, new Student(studentId));
                readingBook(req, studentId);
            }
        }
    }

    private void readingBook(LibraryReqCmd req, String studentId) {
        ArrayList<String> books;
        if (libBookShelf.containsBook(req.getBookIsbn())) {
            books = libBookShelf.getBooks().get(req.getBookIsbn());
            String copyId = books.remove(0);
            LibraryBookId book = new LibraryBookId(req.getBookIsbn().getType(),
                    req.getBookIsbn().getUid(), copyId);
            students.get(studentId).readBook(book);
            readingroom.receiveBook(book);
            updateBook(req.getBookIsbn(), books);
            updateTrace(book, req.getDate(), 12);
            PRINTER.accept(req, book);
        } else {
            books = hotBookShelf.getBooks().get(req.getBookIsbn());
            String copyId = books.remove(0);
            LibraryBookId book = new LibraryBookId(req.getBookIsbn().getType(),
                    req.getBookIsbn().getUid(), copyId);
            students.get(studentId).readBook(book);
            readingroom.receiveBook(book);
            updateHotBook(req.getBookIsbn(), books);
            updateTrace(book, req.getDate(), 11);
            PRINTER.accept(req, book);
        }
        hotBooks.add(req.getBookIsbn());
    }

    @Trigger(from = "rr", to = "bro")
    public void restoreBook(LibraryReqCmd req, LocalDate date) {
        students.get(req.getStudentId()).restoreBook();
        students.get(req.getStudentId()).changeCreditScore(10);//
        readingroom.restoreBook(req.getBookId());
        borrowOffice.receiveBook(req.getBookId());
        updateTrace(req.getBookId(), date, 13);
        PRINTER.accept(req);
    }

}
