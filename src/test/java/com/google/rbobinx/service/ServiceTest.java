package com.google.rbobinx.service;

import com.google.rbobinx.TestUtils;
import com.google.rbobinx.model.Transaction;
import com.google.rbobinx.repository.TransactionRepo;
import com.google.rbobinx.repository.TypeRepo;
import com.google.rbobinx.util.ConcurrentUpdateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceTest {

    @InjectMocks
    Service service;

    @Mock
    TransactionRepo transactionRepo;

    @Mock
    TypeRepo typeRepo;

    @Test
    public void createWithoutParent() throws ConcurrentUpdateException {
        // Given a transaction without parent
        Transaction transaction = TestUtils.generateTransaction();
        when(transactionRepo.get(transaction.getId())).thenReturn(empty());
        doNothing().when(transactionRepo).add(any(Transaction.class));
        doNothing().when(typeRepo).add(any(Transaction.class));

        // When saving transaction
        Optional<String> result = service.createTransaction(transaction.getId(),
                transaction.getType(),
                transaction.getParentId(),
                transaction.getAmount());

        // Then Optional.empty is returned
        assertEquals(empty(), result);
        verify(transactionRepo, times(1)).get(any(Long.class));
        verify(transactionRepo, times(1)).add(any(Transaction.class));
        verify(typeRepo, times(1)).add(any(Transaction.class));
    }

    @Test
    public void createWithParent() throws ConcurrentUpdateException {
        // Given a transaction with parent
        Transaction parent = TestUtils.generateTransaction();
        Transaction transaction = TestUtils.generateTransaction(parent);
        when(transactionRepo.get(transaction.getId())).thenReturn(empty());
        when(transactionRepo.get(transaction.getParentId())).thenReturn(Optional.of(parent));
        doNothing().when(transactionRepo).add(any(Transaction.class));
        doNothing().when(typeRepo).add(any(Transaction.class));

        // When saving transaction
        Optional<String> result = service.createTransaction(transaction.getId(),
                transaction.getType(),
                transaction.getParentId(),
                transaction.getAmount());

        // Then Optional.empty is returned
        assertEquals(empty(), result);
        verify(transactionRepo, times(2)).get(any(Long.class));
        verify(transactionRepo, times(1)).add(any(Transaction.class));
        verify(typeRepo, times(1)).add(any(Transaction.class));
    }

    @Test
    public void createExisting() throws ConcurrentUpdateException {
        // Given a transaction
        Transaction transaction = TestUtils.generateTransaction();
        when(transactionRepo.get(transaction.getId())).thenReturn(Optional.of(transaction));

        // When saving transaction
        Optional<String> result = service.createTransaction(transaction.getId(),
                transaction.getType(),
                transaction.getParentId(),
                transaction.getAmount());

        // Then Optional of error is returned
        assertEquals(Optional.of(Service.TRANSACTION_EXISTS_ERROR), result);
        verify(transactionRepo, times(1)).get(any(Long.class));
        verify(transactionRepo, times(0)).add(any(Transaction.class));
        verify(typeRepo, times(0)).add(any(Transaction.class));
    }

    @Test
    public void createParentNoFound() throws ConcurrentUpdateException {
        // Given a transaction with parent
        Transaction transaction = TestUtils.generateTransaction(TestUtils.generateTransaction());
        when(transactionRepo.get(transaction.getId())).thenReturn(empty());
        when(transactionRepo.get(transaction.getParentId())).thenReturn(empty());

        // When saving transaction
        Optional<String> result = service.createTransaction(transaction.getId(),
                transaction.getType(),
                transaction.getParentId(),
                transaction.getAmount());

        // Then Optional of error is returned
        assertEquals(Optional.of(Service.PARENT_NOT_FOUND_ERROR), result);
        verify(transactionRepo, times(2)).get(any(Long.class));
        verify(transactionRepo, times(0)).add(any(Transaction.class));
        verify(typeRepo, times(0)).add(any(Transaction.class));
    }

    @Test
    public void createWithConcurrentUpdate() throws ConcurrentUpdateException {
        // Given a transaction
        Transaction transaction = TestUtils.generateTransaction();
        when(transactionRepo.get(transaction.getId())).thenReturn(empty());
        doThrow(ConcurrentUpdateException.class).when(transactionRepo).add(any(Transaction.class));

        // When saving transaction
        Optional<String> result = service.createTransaction(transaction.getId(),
                transaction.getType(),
                transaction.getParentId(),
                transaction.getAmount());

        // Then Optional of error is returned
        assertEquals(Optional.of(Service.TRANSACTION_EXISTS_ERROR), result);
        verify(transactionRepo, times(1)).get(any(Long.class));
        verify(transactionRepo, times(1)).add(any(Transaction.class));
        verify(typeRepo, times(0)).add(any(Transaction.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getTransaction() {
        // Given
        Transaction transaction = TestUtils.generateTransaction();
        when(transactionRepo.get(transaction.getId())).thenReturn( Optional.of(transaction));
        when(transactionRepo.get(transaction.getId() - 1)).thenReturn(empty());

        // When
        Optional<Transaction> maybeTransaction = service.getTransaction(transaction.getId());

        // Then
        assertEquals(Optional.of(transaction), maybeTransaction);

        // When
        maybeTransaction = service.getTransaction(transaction.getId() - 1);

        // Then
        assertEquals(empty(), maybeTransaction);
    }

    @Test
    public void findByType() {
        // Given
        Transaction transaction = TestUtils.generateTransaction();
        when(typeRepo.get(transaction.getType())).thenReturn(Collections.singleton(transaction.getId()));

        // When
        Set<Long> resultSet = typeRepo.get(transaction.getType());

        // Then
        assertEquals(1, resultSet.size());
        assertTrue(resultSet.contains(transaction.getId()));
    }

    @Test
    public void getSumTransactionExists() {
        // Given
        Transaction transaction = TestUtils.generateTransaction();
        when(transactionRepo.get(transaction.getId())).thenReturn(Optional.of(transaction));

        // When
        Optional<Double> maybeSum = service.getSum(transaction.getId());

        // Then
        assertEquals(Optional.of(transaction.getSum()), maybeSum);
    }

    @Test
    public void getSumTransactionNotExists() {
        // Given
        Transaction transaction = TestUtils.generateTransaction();
        when(transactionRepo.get(transaction.getId())).thenReturn(empty());

        // When
        Optional<Double> maybeSum = service.getSum(transaction.getId());

        // Then
        assertEquals(empty(), maybeSum);
    }
}
