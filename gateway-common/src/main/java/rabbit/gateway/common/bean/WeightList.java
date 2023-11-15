package rabbit.gateway.common.bean;

import rabbit.gateway.common.Weight;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 包含权重因子的循环list
 *
 * @param <T>
 */
public class WeightList<T extends Weight> {

    // 索引
    private int index = 0;

    // 当前索引下的计数器，最大值不超过权重
    private long counter = 0;

    /**
     * 原始数据
     */
    private List<T> list;

    /**
     * 缓存每个节点的权重
     */
    private Map<Integer, Long> weightCache = new HashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    public WeightList(List<T> list) {
        this.list = list;
        long maxFactor = maxFactor(list.stream().map(Weight::getWeight).collect(Collectors.toList()));
        for (int i = 0; i < list.size(); i++) {
            Weight t = list.get(i);
            t.setWeight(t.getWeight() / maxFactor);
            weightCache.put(i, t.getWeight());
        }
    }

    /**
     * 获取下一个节点
     *
     * @return
     */
    public T next() {
        try {
            lock.lock();
            if (counter == weightCache.get(index)) {
                index++;
                counter = 0L;
                if (index == weightCache.size()) {
                    index = 0;
                }
            }
            counter++;
            return list.get(index);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 最大公约数
     *
     * @param numbers
     * @return
     */
    public static long maxFactor(List<Long> numbers) {
        if (numbers.isEmpty()) {
            throw new RuntimeException("空数组");
        }
        if (numbers.stream().filter(v -> v == 0).count() != 0) {
            throw new RuntimeException("权重不能为0");
        }
        if (1 == numbers.size()) {
            return numbers.get(0);
        }
        Collections.sort(numbers, Long::compareTo);
        long factor = Long.MAX_VALUE;
        for (int i = 0; i < numbers.size() - 1; i++) {
            long n = numbers.get(i);
            for (int j = i + 1; j < numbers.size(); j++) {
                long m = numbers.get(j);
                while ((m % n != 0)) {
                    long temp = m % n;
                    m = n;
                    n = temp;
                }
                if (n <= factor) {
                    factor = n;
                }
            }
        }
        return factor;
    }
}
