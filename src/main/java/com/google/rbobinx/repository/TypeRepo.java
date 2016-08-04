package com.google.rbobinx.repository;

import com.google.common.base.MoreObjects;
import com.google.rbobinx.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TypeRepo {

    private Map<String, Set<Long>> repository = new ConcurrentHashMap<>();

    public Set<Long> get(String type) {
        return MoreObjects.firstNonNull(repository.get(type), Collections.emptySet());
    }

    public void add(Transaction transaction) {
        if (repository.containsKey(transaction.getType())) {
            repository.get(transaction.getType()).add(transaction.getId());
        } else {
            Set<Long> set = ConcurrentHashMap.newKeySet();
            set.add(transaction.getId());
            repository.put(transaction.getType(), set);
        }
    }
}
