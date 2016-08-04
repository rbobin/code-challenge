package com.google.rbobinx.repository;

import com.google.rbobinx.util.ConcurrentUpdateException;
import com.google.rbobinx.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionRepo {

    private Map<Long, Transaction> repository = new ConcurrentHashMap<>();

    public Optional<Transaction> get(Long id) {
        return Optional.ofNullable(repository.get(id));
    }

    public void add(Transaction transaction) throws ConcurrentUpdateException {
        Transaction previousValue = repository.putIfAbsent(transaction.getId(), transaction);
        if (previousValue != null) {
            throw new ConcurrentUpdateException();
        }
        transaction.propagateSum();
    }
}
