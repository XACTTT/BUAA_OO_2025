import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;

public class ShareData {
    private Floor shareFloor;
    private int readyCnt;
    private int endCnt;
    private boolean isOccupied;
    private int id1;
    private int id2;

    public ShareData(UpdateRequest updateRequest) {
        this.shareFloor = Floor.valueOf(updateRequest.getTransferFloor());
        this.readyCnt = 0;
        this.endCnt = 0;
        this.isOccupied = false;
        this.id1 = updateRequest.getElevatorAId();
        this.id2 = updateRequest.getElevatorBId();
    }

    public synchronized void isReady() throws InterruptedException {
        readyCnt++;
        if (readyCnt == 2) {
            TimableOutput.println("UPDATE-BEGIN" + "-" + id1 + "-" + id2);
            notifyAll();
        } else {
            while (readyCnt < 2) {
                wait();
            }
        }

    }

    public synchronized void updateEnd() throws InterruptedException {
        endCnt++;
        if (endCnt == 2) {
            TimableOutput.println("UPDATE-END" + "-" + id1 + "-" + id2);
            notifyAll();
        } else {
            while (endCnt < 2) {
                wait();
            }
        }
    }

    public synchronized void enterShareFloor() throws InterruptedException {

        while (isOccupied) {

            wait();
        }

        isOccupied = true;

    }

    public synchronized void exitShareFloor() {

        isOccupied = false;
        notifyAll();
    }

    public boolean isShareFloor(Floor floor) {
        return floor == shareFloor;
    }

}
