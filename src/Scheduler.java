import java.util.ArrayList;

public class Scheduler implements Runnable {
    private RequestTable masterRequestTable;
    private ArrayList<RequestTable> eleRequestTables;

    public Scheduler(RequestTable masterRequestTable, ArrayList<RequestTable> eleRequestTables) {
        this.masterRequestTable = masterRequestTable;
        this.eleRequestTables = eleRequestTables;
    }

    public void run() {
        while (true) {
            if (masterRequestTable.isEmpty() && masterRequestTable.isEnd()) {
                for (RequestTable eleRequestTable : eleRequestTables) {
                    eleRequestTable.setEnd();
                }
                break;
            }
            Person person = masterRequestTable.getRequestFromMasterTable();
                if (person == null) {
                    continue;
                }
             eleRequestTables.get(person.getElevatorId() - 1).addRequest(person);


        }
    }
}
