package com.google.rbobinx;

import com.google.rbobinx.model.Transaction;

import java.util.Random;
import java.util.UUID;

public class TestUtils {

    final static int INT_UPPER_BOUND = 10000000;

    public static Transaction generateTransaction() {
        return generateTransaction(null);
    }

    public static Transaction generateTransaction(int upperBound, Transaction parent) {
        Random gen = new Random();
        long id = gen.nextInt(upperBound);
        double amount = gen.nextInt(upperBound);
        String type = UUID.randomUUID().toString();
        return new Transaction(id, type, parent, amount);
    }

    public static Transaction generateTransaction(Transaction parent) {
        return generateTransaction(INT_UPPER_BOUND, parent);
    }

}
