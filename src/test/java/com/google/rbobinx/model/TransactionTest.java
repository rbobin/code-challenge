package com.google.rbobinx.model;

import com.google.rbobinx.TestUtils;
import com.google.rbobinx.model.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class TransactionTest {

    @Test
    public void testPropagateSum2Levels() {
        // Given a transaction and it's parent
        Transaction parent = TestUtils.generateTransaction();
        Transaction child = TestUtils.generateTransaction(parent);

        double childSum = child.getAmount();
        double parentSum = parent.getAmount() + childSum;

        // When invoking propagateSum() on a child
        child.propagateSum();

        // Then parent sum is updated
        assertEquals(parent.getSum(), parentSum, 0);
        assertEquals(child.getSum(), childSum, 0);
    }

    @Test
    public void testPropagateSum3Levels() {
        // Given a transaction, it's parent and grandparent and child
        Transaction grandParent = TestUtils.generateTransaction();
        Transaction parent = TestUtils.generateTransaction(grandParent);
        Transaction child = TestUtils.generateTransaction(parent);

        double childSum = child.getAmount();
        double parentSum = parent.getAmount() + childSum;
        double grandParentSum = grandParent.getAmount() + parentSum;

        // When invoking propagateSum() on child and parent
        parent.propagateSum();
        child.propagateSum();

        // Then parent sum is updated
        assertEquals(child.getSum(), childSum, 0);
        assertEquals(parent.getSum(), parentSum, 0);
        assertEquals(grandParent.getSum(), grandParentSum, 0);
    }
}
