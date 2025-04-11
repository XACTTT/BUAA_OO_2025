import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;

public class InputThread implements Runnable {
    private RequestTable masterRequestTable;

    public InputThread(RequestTable masterRequestTable) {
        this.masterRequestTable = masterRequestTable;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                masterRequestTable.setEnd();
                break;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    Person person = new Person(personRequest.getPersonId(),
                            personRequest.getPriority(),
                            Floor.valueOf(personRequest.getFromFloor())
                            , Floor.valueOf(personRequest.getToFloor()));
                    masterRequestTable.addPersonRequest(person);
                } else if (request instanceof ScheRequest) {
                    ScheRequest scheRequest = (ScheRequest) request;
                    masterRequestTable.addScheRequest(scheRequest);

                }

            }
        }

    }
}
