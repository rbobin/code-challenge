package com.google.rbobinx.service;

import com.google.rbobinx.util.ConcurrentUpdateException;
import com.google.rbobinx.repository.TransactionRepo;
import com.google.rbobinx.repository.TypeRepo;
import com.google.rbobinx.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class Service {

    public static final String TRANSACTION_EXISTS_ERROR = "Transaction already exists";
    public static final String PARENT_NOT_FOUND_ERROR = "Parent transaction not found";

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    TypeRepo typeRepo;

    public Optional<String> createTransaction(final Long id,
                                    final String type,
                                    final Long parentId,
                                    final double amount) {
        if (transactionRepo.get(id).isPresent()) {
            return Optional.of(TRANSACTION_EXISTS_ERROR);
        }

        Transaction parent = parentId == null ? null : transactionRepo.get(parentId).orElse(null);
        if (parentId != null && parent == null) {
            return Optional.of(PARENT_NOT_FOUND_ERROR);
        }

        Transaction t = new Transaction(id, type, parent, amount);
        try {
            transactionRepo.add(t);
        } catch (ConcurrentUpdateException e) {
            return Optional.of(TRANSACTION_EXISTS_ERROR);
        }

        typeRepo.add(t);

        return Optional.empty();
    }

    public Optional<Transaction> getTransaction(final Long id) {
        return transactionRepo.get(id);
    }

    public Set<Long> findByType(final String type) {
        return typeRepo.get(type);
    }

    public Optional<Double> getSum(final Long id) {
        Optional<Transaction> maybeTransaction = transactionRepo.get(id);
        if (maybeTransaction.isPresent()) {
            return Optional.of(maybeTransaction.get().getSum());
        } else {
            return Optional.empty();
        }
    }
}
