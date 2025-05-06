import static org.junit.Assert.*;

import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.main.PersonInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private static class TestData {
        final int[][] relations; // 要添加的关系数组// 预期三元环数
        final int expected;
        TestData(int[][] relations,int expectedAns) {
            this.relations = relations;
            this.expected = expectedAns;
        }
    }

    // 参数化数据生成
    @Parameterized.Parameters
    public static Collection<TestData> data() {
        return Arrays.asList(
                // 空图
                new TestData(
                        new int[][]{}, // 无关系
                        0 // 预期无couple
                ),

                // 单个双向最佳好友对（A-B）
                new TestData(
                        new int[][]{
                                {0, 1, 100}  // 0和1互为最佳
                        },
                        1 // 预期couple数为1
                ),

                // 多个独立双向最佳对（A-B, C-D）
                new TestData(
                        new int[][]{
                                {0, 1, 200},
                                {2, 3, 300}
                        },
                        2 // 预期couple数为2
                ),

                // 单向最佳好友（A→B，但B→C）
                new TestData(
                        new int[][]{
                                {0, 1, 100}, {1, 2, 200}
                        },
                        1 // 只有B和C互为最佳，预期couple数为1
                ),

                // 复杂网络（含平局和多重关系）
                new TestData(
                        new int[][]{
                                {0, 1, 100}, // A-B互为最佳
                                {0, 2, 100},                // A与C亲密度相同，但选较小ID（B）// C的最佳是A，但A的最佳是B → 不计入
                                {3, 4, 50},    // D-E互为最佳
                                {5, 6, 200}   // F-G互为最佳
                        },
                        3 // 预期A-B, D-E, F-G 共3对
                ),

                // 平局场景（某人多个好友亲密度相同）
                new TestData(
                        new int[][]{
                                {0, 1, 100}, {0, 2, 100},
                        },
                        1 // 只有0和1互为最佳
                )
        );

    }

    private final TestData testData;
    private Network network;
    private Network oldNetwork;

    public NetworkTest(TestData testData) {
        this.testData = testData;
    }


    private void initNetworks() throws Exception {
        network = new Network();
        oldNetwork = new Network();

        // 自动推断需要添加的节点（根据关系中的最大ID）
        int maxId = Arrays.stream(testData.relations)
                .flatMapToInt(arr -> Arrays.stream(arr).limit(2))
                .max().orElse(10);

        for (int i = 0; i <= maxId; i++) {
            network.addPerson(new Person(i, "P"+i, 20));
            oldNetwork.addPerson(new Person(i, "P"+i, 20));
        }
    }

    // 核心测试方法（保持原有逻辑结构）
    @Test
    public void queryCoupleSum() throws Exception {
        initNetworks();

        // 按顺序添加所有关系
        for (int[] rel : testData.relations) {
            addRelation(rel[0], rel[1], rel[2]);
        }

        testForCP();
    }


    private void addRelation(int i, int j, int value) throws Exception {
        network.addRelation(i, j, value);
        oldNetwork.addRelation(i, j, value);
    }


    private void testForCP() {
        int actual = network.queryCoupleSum();
        assertEquals(testData.expected, actual);
        verifyStateConsistency();
    }




    // 保持原有状态验证
    private void verifyStateConsistency() {
        PersonInterface[] newPersons = network.getPersons();
        assertNotNull(newPersons);
        assertEquals(oldNetwork.getPersons().length, newPersons.length);

        for (int i = 0; i < newPersons.length; i++) {
            PersonInterface actual = newPersons[i];
            Person expected = (Person) oldNetwork.getPersons()[i];
            assertTrue(expected.strictEquals(actual));
        }
    }
}