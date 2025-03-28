import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳
        ArrayList<RequestTable> eleRequestTables = new ArrayList<>();
        RequestTable masterRequestTable = new RequestTable();
        for (int i = 1; i <= 6; i++) {
            RequestTable eleRequestTable = new RequestTable();
            eleRequestTables.add(eleRequestTable);
            Elevator elevator = new Elevator(i, eleRequestTable);
            Thread thread = new Thread(elevator);
            thread.start();
        }
        InputThread inputThread = new InputThread(masterRequestTable);
        Thread thread1 = new Thread(inputThread);
        thread1.start();
        Scheduler scheduler = new Scheduler(masterRequestTable, eleRequestTables);
        Thread thread2 = new Thread(scheduler);
        thread2.start();

        /*
              代码部分
          */
    }
}