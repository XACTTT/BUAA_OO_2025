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
        this.id1=updateRequest.getElevatorAId();
        this.id2=updateRequest.getElevatorBId();
    }
    public synchronized void isReady() throws InterruptedException {
    //    通过count和readyCondition来协同开始改造
        readyCnt++;
        if(readyCnt == 2) {
            TimableOutput.println("UPDATE-BEGIN"+"-"+id1+"-"+id2);
            notifyAll();
        } else {
            while (readyCnt < 2) {
                wait();
            }
        }
    //    修改状态、等待、唤醒

    //    在此由其中一个特定电梯线程输出UPDATE-BEGIN
    //    Tips:注意负责输出的电梯先进来和后进来的区别（也可以用输出标志位磨平这个差异）
    }

    // 电梯已经可以结束改造
    public synchronized void updateEnd() throws InterruptedException {
        endCnt++;
        if(endCnt == 2) {
            TimableOutput.println("UPDATE-END"+"-"+id1+"-"+id2);
            notifyAll();
        } else {
            while (endCnt < 2) {
                wait();
            }
        }
    }
    public synchronized void enterShareFloor() throws InterruptedException {
        // 如果共享层已被占用，等待直到被唤醒
        while (isOccupied) {
        //    TimableOutput.println(String.format("电梯%d 等待进入共享层 %s", elevatorId, overlapFloor.name()));
            wait();
        }
        // 占用共享层
        isOccupied = true;

        //TimableOutput.println(String.format("电梯%d 进入共享层 %s", elevatorId, overlapFloor.name()));
    }

    // 离开共享层（同步方法）
    public synchronized void exitShareFloor() {
        // 释放共享层
        isOccupied = false;
        //TimableOutput.println(String.format("电梯%d 离开共享层 %s", elevatorId, overlapFloor.name()));
        notifyAll(); // 唤醒其他等待的电梯
    }

    // 检查某楼层是否为共享层
    public boolean isShareFloor(Floor floor) {
        return floor == shareFloor;
    }

}
