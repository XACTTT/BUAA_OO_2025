import static org.junit.Assert.*;

import com.oocourse.spec1.main.PersonInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private static class TestData {
        final int[][] relations; // 要添加的关系数组
        final int expectedTriples; // 预期三元环数

        TestData(int[][] relations, int expected) {
            this.relations = relations;
            this.expectedTriples = expected;
        }
    }

    // 参数化数据生成
    @Parameterized.Parameters
    public static Collection<TestData> data() {
        return Arrays.asList(
                // 空图
                new TestData(new int[][]{}, 0),

                // 单个三元环
                new TestData(new int[][]{
                        {0,1,1}, {1,2,1}, {2,0,1}
                }, 1),

                // 两个独立三元环
                new TestData(new int[][]{
                        {0,1,1}, {1,2,1}, {2,0,1},
                        {3,4,1}, {4,5,1}, {5,3,1}
                }, 2),

                // 共享边的两个三元环
                new TestData(new int[][]{
                        {0,1,1}, {1,2,1}, {2,0,1},
                        {1,3,1}, {3,2,1}
                }, 2),

                 new TestData(new int[][]{
                         {0,1,1},
                         {1,2,1},
                         {2,0,1},
                         {3,1,1}, {3,2,1},
                         {0,4,1}, {1,4,1}, {2,4,1}, {4,3,1},
                         {5,6,1},{6,7,1},{7,5,1}
                    }, 8),

                new TestData(new int[][]{
                        {0,1,1},
                        {1,2,1},
                        {2,0,1},
                        {3,1,1}, {3,2,1},
                        {0,4,1}, {1,4,1}, {2,4,1}, {4,3,1},
                        {5,6,1},{6,7,1},{7,5,1}
                    }, 8),

                new TestData(new int[][]{
                        {0,1,1},
                        {1,2,1},
                        {2,0,1},
                        {3,1,1}, {3,2,1},{3,0,1},
                        {0,4,1}, {1,4,1}, {2,4,1}, {4,3,1},
                        {5,0,1},{5,1,1},{2,5,1},{5,3,1}, {5,4,1},
                }, 20)

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
    public void queryTripleSum() throws Exception {
        initNetworks();

        // 按顺序添加所有关系
        for (int[] rel : testData.relations) {
            addRelation(rel[0], rel[1], rel[2]);
        }

        testForTrip();
    }


    private void addRelation(int i, int j, int value) throws Exception {
        network.addRelation(i, j, value);
        oldNetwork.addRelation(i, j, value);
    }


    private void testForTrip() {
        PersonInterface[] persons = oldNetwork.getPersons();
        assertNotNull(persons);


        int sum = 0;
        for (int i = 0; i < persons.length; i++) {
            for (int j = i + 1; j < persons.length; j++) {
                for (int k = j + 1; k < persons.length; k++) {
                    if (isTriangle(persons[i], persons[j], persons[k])) {
                        sum++;
                    }
                }
            }
        }

        // 断言结果
        assertEquals(testData.expectedTriples, network.queryTripleSum());
        assertEquals(testData.expectedTriples, sum);

        // 验证状态一致性
        verifyStateConsistency();
    }


    private boolean isTriangle(PersonInterface a, PersonInterface b, PersonInterface c) {
        return a.isLinked(b) && b.isLinked(c) && c.isLinked(a);
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