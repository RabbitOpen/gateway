package rabbit.gateway.common.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.gateway.common.Weight;
import rabbit.gateway.common.bean.WeightList;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class WeightListTest {

    @Test
    public void maxFactorTest() {
        TestCase.assertEquals(3, WeightList.maxFactor(Arrays.asList(3L, 6L, 9L, 15L)));
        TestCase.assertEquals(31, WeightList.maxFactor(Arrays.asList(31L)));

        WeightList<Sample> list = new WeightList<>(Arrays.asList(new Sample("a", 10L), new Sample("b", 5)));
        TestCase.assertEquals("a", list.next().getName());
        TestCase.assertEquals("a", list.next().getName());
        TestCase.assertEquals("b", list.next().getName());
        TestCase.assertEquals("a", list.next().getName());
        TestCase.assertEquals("a", list.next().getName());
        TestCase.assertEquals("b", list.next().getName());
    }

    class Sample implements Weight {

        private String name;
        private long weight;

        public Sample(String name, long weight) {
            this.name = name;
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        @Override
        public long getWeight() {
            return weight;
        }

        @Override
        public void setWeight(long weight) {
            this.weight = weight;
        }
    }
}
