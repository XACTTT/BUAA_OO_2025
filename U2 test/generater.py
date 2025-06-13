import numpy as np

# ---参数区---
# 最大的乘客ID，最小的乘客ID为1
MAX_PASSENGER_ID = 2147483646
# 最大的电梯ID，最小的电梯ID为1
MAX_ELEVATOR_ID = 1
# 最小的楼层
MIN_FLOOR = 1
# 最大的楼层
MAX_FLOOR = 11
# 最晚的输入时间
MAX_TIME = 50.0
# 平均数据间隔时间
EXPECT_GAP_TIME = 0.0
# 最大数据间隔时间，必须小于最晚的输入时间的1/2
MAX_GAP_TIME = 10.0
assert 2 * MAX_GAP_TIME <= MAX_TIME
# 最大指令数量
MAX_INSTRUCTION = 30
# 两条指令之间有时间间隔的概率
INSTRUCTION_GAP_PROBABILITY = 0


class Generator(object):
    input_string_list = []
    NOW_TIME = 1.0
    NOW_INSTRUCTION = 0
    NOW_USED_PASSENGER_ID = []

    def get_input_string(self):
        return "\n".join(self.input_string_list)

    def generate(self):
        self.init()
        self.add_time(force_gap=True)
        while self.NOW_TIME <= MAX_TIME and self.NOW_INSTRUCTION < MAX_INSTRUCTION:
            time_str = f"[{self.NOW_TIME:.1f}]"
            passenger_str = self.generate_passenger()
            self.input_string_list.append(f"{time_str}{passenger_str}")
            self.NOW_INSTRUCTION += 1
            self.add_time(force_gap=False)

    def init(self):
        self.input_string_list = []
        self.NOW_TIME = 50.0
        self.NOW_INSTRUCTION = 0
        self.NOW_USED_PASSENGER_ID = []

    def generate_passenger(self):
        pid = self.generate_passenger_id()
        pri = np.random.randint(1, 100)
        from_floor, to_floor = self.generate_from_to_floor()
        return f"{pid}-PRI-{pri}-FROM-{from_floor}-TO-{to_floor}"

    def generate_passenger_id(self):
            while True:
                pid = np.random.randint(1, MAX_PASSENGER_ID)
                if pid not in self.NOW_USED_PASSENGER_ID:
                    self.NOW_USED_PASSENGER_ID.append(pid)
                    return pid

    def generate_from_to_floor(self):
        floors = ["B4", "B3", "B2", "B1", "F1", "F2", "F3", "F4", "F5", "F6", "F7"]
        from_floor = np.random.choice(floors)
        to_floor = np.random.choice(floors)
        while to_floor == from_floor:
            to_floor = np.random.choice(floors)
        return from_floor, to_floor

    def add_time(self, force_gap):
        if force_gap or np.random.rand() <= INSTRUCTION_GAP_PROBABILITY:
            add_time = np.random.exponential(EXPECT_GAP_TIME)
            while add_time >= MAX_GAP_TIME:
                add_time = np.random.exponential(EXPECT_GAP_TIME)
            self.NOW_TIME += add_time


