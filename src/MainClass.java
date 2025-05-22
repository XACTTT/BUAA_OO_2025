import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryCloseCmd;

import static com.oocourse.library1.LibraryIO.SCANNER;

import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryBookIsbn;

import java.time.LocalDate;
import java.util.Map;


public class MainClass {
    private static Library library = new Library();

    public static void main(String[] args) {

        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();// 获取图书馆内所有书籍ISBN号及相应副本数
        library.init(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                // 在开馆时做点什么
                library.arrangeOpen(today);
            } else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                library.arrangeClose(today);
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;

                // 对指令进行处理
                handle(req,today);
            }
        }


    }

    private static void handle(LibraryReqCmd req,LocalDate date) {
        LibraryReqCmd.Type type = req.getType(); // 指令对应的类型（查询/阅读/借阅/预约/还书/取书/归还）
        switch (type) {
            case QUERIED:
                library.queryTrace(req,date);
                break;
            case BORROWED:
                library.borrowBook(req, date);
                break;
            case ORDERED:
                library.orderBook(req, date);
                break;
            case RETURNED:
                library.returnBook(req, date);
                break;
            case PICKED:
                library.pickBook(req, date);
                break;

        }
    }

}
