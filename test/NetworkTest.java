import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;

import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
// 假设 Network, Person, EmojiMessage, Message 类是可访问的
// import your.package.Network; // 如果它们在特定包中，请取消注释并修改

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class NetworkTest {

    // 内部类，用于定义表情的初始状态以进行设置
    static class EmojiDef {
        final int id; // 表情ID
        final int targetInitialHeat; // 在添加通用消息之前要达到的目标热度

        public EmojiDef(int id, int targetInitialHeat) {
            this.id = id;
            this.targetInitialHeat = targetInitialHeat;
        }
    }

    // 内部类，用于定义消息以进行设置
    static class MessageDef {
        final int id; // 消息ID
        final int type; // 0 表示普通 Message, 1 表示 EmojiMessage
        final int person1Id; // 发送者ID
        final int person2Id; // 接收者ID (对于type=0)
        final int emojiId; // 表情ID (如果 type 是 1 EmojiMessage)
        final int socialValue; // 社交值 (如果 type 是 0 Normal Message)

        // EmojiMessage 的构造函数
        public MessageDef(int id, int person1Id, int person2Id, int emojiIdForMessage) {
            this.id = id;
            this.type = 1; // 类型为 EmojiMessage
            this.person1Id = person1Id;
            this.person2Id = person2Id;
            this.emojiId = emojiIdForMessage;
            this.socialValue = 0; // EmojiMessage 通常不直接通过构造函数设置社交值，其社交值由表情ID决定
        }

        // Normal Message 的构造函数
        public MessageDef(int id, int person1Id, int person2Id, int socialValueForMessage, boolean isNormal) {
            this.id = id;
            this.type = 0; // 类型为普通消息
            this.person1Id = person1Id;
            this.person2Id = person2Id;
            this.emojiId = 0; // 普通消息不使用 emojiId
            this.socialValue = socialValueForMessage;
        }
    }

    // 内部类，用于保存单个参数化测试用例的所有数据
    static class DeleteColdEmojiTestData {
        final String testCaseName; // 测试用例名称
        final EmojiDef[] initialEmojisToSetup; // 需要预先存储和预热的表情定义
        final MessageDef[] messagesInQueueBeforeTest; // deleteColdEmoji 调用前，网络中应存在的消息定义
        final int limitParameter; // 调用 deleteColdEmoji 时的 limit 参数
        final int expectedReturnValue; // 预期的 deleteColdEmoji 返回值
        final Set<Integer> expectedFinalEmojiIds; // 预期的操作后剩余的表情 ID 集合
        final Map<Integer, Integer> expectedFinalEmojiHeats; // 预期的操作后剩余表情的热度 (ID -> 热度)
        final Set<Integer> expectedFinalMessageIds; // 预期的操作后剩余的消息 ID 集合

        public DeleteColdEmojiTestData(String testCaseName, EmojiDef[] initialEmojisToSetup,
                                       MessageDef[] messagesInQueueBeforeTest, int limitParameter,
                                       int expectedReturnValue, Set<Integer> expectedFinalEmojiIds,
                                       Map<Integer, Integer> expectedFinalEmojiHeats,
                                       Set<Integer> expectedFinalMessageIds) {
            this.testCaseName = testCaseName;
            this.initialEmojisToSetup = initialEmojisToSetup;
            this.messagesInQueueBeforeTest = messagesInQueueBeforeTest;
            this.limitParameter = limitParameter;
            this.expectedReturnValue = expectedReturnValue;
            this.expectedFinalEmojiIds = expectedFinalEmojiIds;
            this.expectedFinalEmojiHeats = expectedFinalEmojiHeats;
            this.expectedFinalMessageIds = expectedFinalMessageIds;
        }

        @Override
        public String toString() {
            return testCaseName; // JUnit 测试报告中将使用此名称显示测试用例
        }
    }

    private final DeleteColdEmojiTestData testData; // 当前参数化测试运行所使用的数据对象
    private Network network; // 待测试的 Network 实例
    private Person person0; // 用于发送设置/预热消息的通用人物 P0
    private Person person1; // 用于发送设置/预热消息的通用人物 P1

    // 参数化测试类的构造函数，由 JUnit 框架调用
    public NetworkTest(DeleteColdEmojiTestData testData) {
        this.testData = testData;
    }

    // 创建 Person 实例的辅助方法 (需确保 Person 类有此构造函数)
    private Person createPerson(int id, String name, int age) {
        return new Person(id, name, age);
    }

    // 创建 EmojiMessage 实例的辅助方法 (需确保 EmojiMessage 类有此构造函数)
    private EmojiMessage createEmojiMessage(int msgId, int emojiId, PersonInterface p1, PersonInterface p2) {
        return new EmojiMessage(msgId, emojiId, p1, p2);
    }

    // 创建普通 Message 实例的辅助方法 (需确保 Message 类有此构造函数)
    private Message createNormalMessage(int msgId, int socialValue, PersonInterface p1, PersonInterface p2) {
        return new Message(msgId, socialValue, p1, p2);
    }

    // === 获取网络当前状态的辅助方法 (依赖 Network 类提供的 get 方法) ===
    private Set<Integer> getCurrentEmojiIdsFromNetwork() {
        int[] ids = network.getEmojiIdList(); // Network 类必须提供此方法
        if (ids == null) return new HashSet<>();
        return Arrays.stream(ids).boxed().collect(Collectors.toSet());
    }

    private Map<Integer, Integer> getCurrentEmojiHeatsFromNetwork() {
        int[] ids = network.getEmojiIdList();   // Network 类必须提供此方法
        int[] heats = network.getEmojiHeatList(); // Network 类必须提供此方法
        Map<Integer, Integer> map = new HashMap<>();
        if (ids != null && heats != null && ids.length == heats.length) {
            for (int i = 0; i < ids.length; i++) {
                map.put(ids[i], heats[i]);
            }
        }
        return map;
    }

    private Set<Integer> getCurrentMessageIdsFromNetwork() {
        MessageInterface[] msgs = network.getMessages(); // Network 类必须提供此方法
        if (msgs == null) return new HashSet<>();
        return Arrays.stream(msgs).map(MessageInterface::getId).collect(Collectors.toSet());
    }

    // 根据 testData 初始化网络状态的方法
    private void initializeNetworkState() throws Exception {
        network = new Network(); // 为每个测试场景创建新的 Network 实例
        // 创建并添加用于发送预热消息的通用人物
        person0 = createPerson(0, "通用人物P0", 20);
        person1 = createPerson(1, "通用人物P1", 21);
        network.addPerson(person0);
        network.addPerson(person1);
        // 如果 P0 和 P1 不同且未连接，则添加关系以允许发送消息
        if (person0.getId() != person1.getId() && !person0.isLinked(person1)) {
            network.addRelation(person0.getId(), person1.getId(), 1);
        }

        // 1. 存储表情并将其预热到 targetInitialHeat
        for (EmojiDef emojiDef : testData.initialEmojisToSetup) {
            network.storeEmojiId(emojiDef.id); // 存储表情，初始热度为0
            // 发送并消费 emojiDef.targetInitialHeat 个 EmojiMessage 来增加热度
            for (int i = 0; i < emojiDef.targetInitialHeat; i++) {
                // 使用唯一的负ID以避免与测试数据中的消息ID冲突
                int tempMsgId = - (emojiDef.id * 1000 + i + 100000); // 保证ID唯一性
                if (network.getMessage(tempMsgId) == null) { // 确保消息ID未被使用
                    EmojiMessage heatUpMsg = createEmojiMessage(tempMsgId, emojiDef.id, person0, person1);
                    network.addMessage(heatUpMsg);
                    try {
                        network.sendMessage(tempMsgId); // 发送并消费消息，增加表情热度
                    } catch (MessageIdNotFoundException e) {
                        // 此处理论上不应发生，因为我们刚添加了消息
                        System.err.println("测试设置警告：发送预热消息 " + tempMsgId + " 失败 (MessageIdNotFoundException)");
                    } catch (RelationNotFoundException | TagIdNotFoundException e) {
                        // 根据 sendMessage 的 JML，这些也可能抛出，尽管在此设置中不太可能
                        System.err.println("测试设置警告：发送预热消息 " + tempMsgId + " 失败: " + e.getClass().getSimpleName());
                    }
                }
            }
        }

        // 2. 添加在调用 deleteColdEmoji 之前应存在于消息队列中的消息
        for (MessageDef msgDef : testData.messagesInQueueBeforeTest) {
            MessageInterface messageToAdd;
            // 获取或创建消息中涉及的人物
            PersonInterface p1 = network.getPerson(msgDef.person1Id);
            if (p1 == null) {
                p1 = createPerson(msgDef.person1Id, "消息人物P"+msgDef.person1Id, 25);
                network.addPerson(p1);
            }
            PersonInterface p2 = null;
            if (msgDef.type == 0 || (msgDef.type == 1 && msgDef.person2Id != -1) ) { // type 1 to person or type 0
                p2 = network.getPerson(msgDef.person2Id);
                if (p2 == null) {
                    p2 = createPerson(msgDef.person2Id, "消息人物P"+msgDef.person2Id, 26);
                    network.addPerson(p2);
                }
            }

            // 如果是点对点消息 (type 0) 且人物不同且未连接，则添加关系
            if (msgDef.type == 0 && p1.getId() != p2.getId() && !p1.isLinked(p2)) {
                network.addRelation(p1.getId(), p2.getId(), 1);
            }

            // 创建消息实例
            if (msgDef.type == 1) { // EmojiMessage
                // 确保 EmojiMessage 引用的 emojiId 已存在 (如果不存在，则热度为0)
                if (!network.containsEmojiId(msgDef.emojiId)) {
                    network.storeEmojiId(msgDef.emojiId);
                }
                messageToAdd = createEmojiMessage(msgDef.id, msgDef.emojiId, p1, p2);
            } else { // 普通 Message
                messageToAdd = createNormalMessage(msgDef.id, msgDef.socialValue, p1, p2);
            }

            // 添加消息到网络，前提是该ID的消息尚不存在
            if (network.getMessage(msgDef.id) == null) {
                try {
                    network.addMessage(messageToAdd);
                } catch (EmojiIdNotFoundException e) {
                    // 如果 EmojiMessage 的 emojiId 不存在于 network.emojiIdList, addMessage 应抛出此异常
                    // 测试设置应确保这种情况被正确处理或避免（例如，通过先 storeEmojiId）
                    System.err.println("测试设置警告：添加消息 " + msgDef.id + " 失败 (EmojiIdNotFoundException for emoji " + msgDef.emojiId + ")");
                } catch (ArticleIdNotFoundException e) {
                    System.err.println("测试设置警告：添加消息 " + msgDef.id + " 失败 (ArticleIdNotFoundException)");
                }
            }
        }
    }

    // 参数化测试的数据源方法
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<DeleteColdEmojiTestData> data() {
        return Arrays.asList(
                new DeleteColdEmojiTestData(
                        /*testCaseName=*/"没有表情的情况",
                        /*initialEmojisToSetup=*/new EmojiDef[]{},
                        /*messagesInQueueBeforeTest=*/new MessageDef[]{},
                        /*limitParameter=*/5, /*expectedReturnValue=*/0,
                        /*expectedFinalEmojiIds=*/new HashSet<>(), /*expectedFinalEmojiHeats=*/new HashMap<>(), /*expectedFinalMessageIds=*/new HashSet<>()
                ),
                new DeleteColdEmojiTestData(
                        /*testCaseName=*/"所有表情都热门_队列无消息",
                        /*initialEmojisToSetup=*/new EmojiDef[]{new EmojiDef(101, 2), new EmojiDef(102, 1)}, // emojiId, 目标预热后的热度
                        /*messagesInQueueBeforeTest=*/new MessageDef[]{},
                        /*limitParameter=*/1, /*expectedReturnValue=*/2,
                        /*expectedFinalEmojiIds=*/new HashSet<>(Arrays.asList(101, 102)),
                        /*expectedFinalEmojiHeats=*/new HashMap<Integer, Integer>() {{ put(101, 2); put(102, 1); }},
                        /*expectedFinalMessageIds=*/new HashSet<>()
                ),
                new DeleteColdEmojiTestData(
                        /*testCaseName=*/"所有表情都冷门_队列无消息",
                        /*initialEmojisToSetup=*/new EmojiDef[]{new EmojiDef(201, 0), new EmojiDef(202, 0)},
                        /*messagesInQueueBeforeTest=*/new MessageDef[]{},
                        /*limitParameter=*/1, /*expectedReturnValue=*/0,
                        /*expectedFinalEmojiIds=*/new HashSet<>(), /*expectedFinalEmojiHeats=*/new HashMap<>(), /*expectedFinalMessageIds=*/new HashSet<>()
                ),
                new DeleteColdEmojiTestData(
                        /*testCaseName=*/"混合表情_冷门表情关联的消息被删除",
                        /*initialEmojisToSetup=*/new EmojiDef[]{
                        new EmojiDef(301, 0), // 冷门 (热度0)
                        new EmojiDef(302, 2), // 热门 (热度2)
                        new EmojiDef(303, 0), // 冷门 (热度0), 关联消息 4001
                        new EmojiDef(304, 1)  // 对于 limit=2 是冷门 (热度1), 关联消息 4002
                },
                        /*messagesInQueueBeforeTest=*/new MessageDef[]{
                        new MessageDef(4001, 0, 1, 303),          // 指向冷门表情303的Emoji消息
                        new MessageDef(4002, 0, 1, 304),          // 指向冷门表情304的Emoji消息
                        new MessageDef(4003, 0, 1, 10, true), // 普通消息, 应保留
                        new MessageDef(4004, 0, 1, 302)           // 指向热门表情302的Emoji消息, 应保留
                },
                        /*limitParameter=*/2, /*expectedReturnValue=*/1, // 预期剩余1个热门表情 (302)
                        /*expectedFinalEmojiIds=*/new HashSet<>(Arrays.asList(302)),
                        /*expectedFinalEmojiHeats=*/new HashMap<Integer, Integer>() {{ put(302, 2); }},
                        /*expectedFinalMessageIds=*/new HashSet<>(Arrays.asList(4003, 4004)) // 消息4001,4002被删除
                ),
                new DeleteColdEmojiTestData(
                        /*testCaseName=*/"Limit为零_没有表情被删除_关联消息也不删除",
                        /*initialEmojisToSetup=*/new EmojiDef[]{new EmojiDef(501, 0), new EmojiDef(502, 1)},
                        /*messagesInQueueBeforeTest=*/new MessageDef[]{new MessageDef(5002, 0, 1, 501)}, // 表情501(热度0)的消息
                        /*limitParameter=*/0, /*expectedReturnValue=*/2, // 热度0不小于limit0，所以501保留
                        /*expectedFinalEmojiIds=*/new HashSet<>(Arrays.asList(501, 502)),
                        /*expectedFinalEmojiHeats=*/new HashMap<Integer, Integer>() {{ put(501, 0); put(502, 1); }},
                        /*expectedFinalMessageIds=*/new HashSet<>(Arrays.asList(5002)) // 消息5002也保留
                ),
                new DeleteColdEmojiTestData(
                        /*testCaseName=*/"Limit很高_所有表情被删除_普通消息保留",
                        /*initialEmojisToSetup=*/new EmojiDef[]{new EmojiDef(601, 0), new EmojiDef(602, 1)},
                        /*messagesInQueueBeforeTest=*/new MessageDef[]{
                        new MessageDef(6001, 0, 1, 601), // 表情601(热度0)的消息
                        new MessageDef(6002, 0, 1, 602), // 表情602(热度1)的消息
                        new MessageDef(6003, 0, 1, 5, true)   // 普通消息
                },
                        /*limitParameter=*/100, /*expectedReturnValue=*/0, // 所有表情热度均小于100
                        /*expectedFinalEmojiIds=*/new HashSet<>(), /*expectedFinalEmojiHeats=*/new HashMap<>(),
                        /*expectedFinalMessageIds=*/new HashSet<>(Arrays.asList(6003)) // 只有普通消息6003保留
                )
                // 可在此处添加更多测试用例
        );
    }

    // 核心测试方法，每个参数化数据项都会执行一次此方法
    @Test
    public void testDeleteColdEmojiParameterized() throws Exception {
        initializeNetworkState(); // 根据当前 testData 初始化网络状态

        // (可选) 捕获调用前 `persons` 状态，用于验证 `assignable`
        PersonInterface[] personsBeforeRaw = network.getPersons();
        Set<Integer> personIdsBefore = new HashSet<>();
        int personCountBefore = 0;
        if (personsBeforeRaw != null) {
            personCountBefore = personsBeforeRaw.length;
            for (PersonInterface p : personsBeforeRaw) {
                personIdsBefore.add(p.getId());
            }
        }

        // 执行待测试的 deleteColdEmoji 方法
        int actualReturnValue = network.deleteColdEmoji(testData.limitParameter);

        // === 断言验证 ===
        // 1. 验证返回值是否符合预期
        assertEquals("返回值与预期不符 (" + testData.testCaseName + ")",
                testData.expectedReturnValue, actualReturnValue);

        // 2. 验证操作后网络中剩余的表情ID集合是否符合预期
        assertEquals("剩余表情ID集合与预期不符 (" + testData.testCaseName + ")",
                testData.expectedFinalEmojiIds, getCurrentEmojiIdsFromNetwork());

        // 3. 验证操作后网络中剩余表情的热度是否符合预期
        assertEquals("剩余表情热度与预期不符 (" + testData.testCaseName + ")",
                testData.expectedFinalEmojiHeats, getCurrentEmojiHeatsFromNetwork());

        // 4. 验证操作后网络中剩余的消息ID集合是否符合预期
        assertEquals("剩余消息ID集合与预期不符 (" + testData.testCaseName + ")",
                testData.expectedFinalMessageIds, getCurrentMessageIdsFromNetwork());

        // 5. (可选) 验证 `assignable`：`persons` 数组本身不应被修改
        PersonInterface[] personsAfterRaw = network.getPersons();
        Set<Integer> personIdsAfter = new HashSet<>();
        int personCountAfter = 0;
        if (personsAfterRaw != null) {
            personCountAfter = personsAfterRaw.length;
            for (PersonInterface p : personsAfterRaw) {
                personIdsAfter.add(p.getId());
            }
        }
        assertEquals("deleteColdEmoji 方法不应改变 Person 数量 (" + testData.testCaseName + ")",
                personCountBefore, personCountAfter);
        assertEquals("deleteColdEmoji 方法不应改变 Person ID 集合 (" + testData.testCaseName + ")",
                personIdsBefore, personIdsAfter);
        // 注意：此处的 assignable 检查较为基础。更严格的检查可能需要比较 Person 对象的内部状态，
        // 但这超出了 deleteColdEmoji 的 JML assignable 范围（其主要关注 emojiIdList, emojiHeatList, messages）。
    }
}
