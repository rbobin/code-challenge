package com.google.rbobinx.repository;

import com.google.rbobinx.TestUtils;
import com.google.rbobinx.model.Transaction;
import com.google.rbobinx.util.ConcurrentUpdateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
public class TransactionRepositoryTest {

    @Test(expected = ConcurrentUpdateException.class)
    public void test() throws ConcurrentUpdateException {
        // Given repository and transaction
        TransactionRepo repo = new TransactionRepo();
        Transaction transaction = TestUtils.generateTransaction();

        // When adding new transaction
        repo.add(transaction);

        // Then it should be available
        Optional<Transaction> foundTransaction = repo.get(transaction.getId());
        assertEquals(transaction, foundTransaction.orElse(null));

        // When getting non existent transaction
        foundTransaction = repo.get(transaction.getId() - 1);

        // Then empty value returned
        assertFalse(foundTransaction.isPresent());

        // When adding transaction with existing id, an exception is thrown, and the old transaction is not updated
        Transaction newTransaction = new Transaction(transaction.getId(), "DYMMU_TYPE", transaction, transaction.getAmount() - 1);
        try {
            repo.add(newTransaction);
        } catch (Exception e) {
            Transaction persistedTransaction = repo.get(transaction.getId()).orElse(null);
            assertEquals(persistedTransaction.getAmount(), transaction.getAmount(), 0);
            assertEquals(persistedTransaction.getType(), transaction.getType());
            assertEquals(persistedTransaction.getParentId(), persistedTransaction.getParentId());
            throw e;
        }

    }
}
