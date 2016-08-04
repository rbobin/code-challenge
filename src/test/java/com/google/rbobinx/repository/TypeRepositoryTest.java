package com.google.rbobinx.repository;

import com.google.rbobinx.TestUtils;
import com.google.rbobinx.model.Transaction;
import com.google.rbobinx.repository.TypeRepo;
import com.google.rbobinx.util.ConcurrentUpdateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class TypeRepositoryTest {

    @Test
    public void test() throws ConcurrentUpdateException {
        // Given repository and a transaction
        TypeRepo repo = new TypeRepo();
        Transaction transaction = TestUtils.generateTransaction();

        // When getting non existent type
        Set<Long> idSet = repo.get(transaction.getType());

        // Then empty set is returned
        assertTrue(idSet.isEmpty());

        // When adding new type
        repo.add(transaction);

        // Then it should be available
        idSet = repo.get(transaction.getType());
        assertEquals(idSet.size(), 1);
        assertEquals(idSet.iterator().next(), transaction.getId());

        // When adding another type
        Transaction anotherTransaction = TestUtils.generateTransaction();
        repo.add(anotherTransaction);

        // Then type ids length is 1
        idSet = repo.get(anotherTransaction.getType());
        assertEquals(idSet.size(), 1);
        assertEquals(idSet.iterator().next(), anotherTransaction.getId());

        // When adding existing type
        Transaction existingTypeTransaction = new Transaction(
                transaction.getId() - 1,
                transaction.getType(),
                transaction,
                transaction.getAmount() - 1
        );
        repo.add(existingTypeTransaction);

        // Then type ids length is 2
        idSet = repo.get(existingTypeTransaction.getType());
        assertEquals(idSet.size(), 2);
        assertTrue(idSet.contains(existingTypeTransaction.getId()));
        assertTrue(idSet.contains(transaction.getId()));
    }
}
