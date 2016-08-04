package com.google.rbobinx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.util.concurrent.AtomicDouble;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

    private final long id;
    private final String type;
    private final Transaction parent;
    private final double amount;
    private AtomicDouble sum;

    public Transaction(long id, String type, Transaction parent, double amount) {
        this.id = id;
        this.type = type;
        this.parent = parent;
        this.sum = new AtomicDouble(amount);
                this.amount = amount;
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long getParentId() {
        return parent == null ? null : parent.getId();
    }

    public double getAmount() {
        return amount;
    }

    @JsonIgnore
    public double getSum() {
        return sum.doubleValue();
    }

    public void propagateSum() {
        updateParentSum(amount);
    }

    private void addAmount(double deltaAmount) {
        this.sum.addAndGet(deltaAmount);
        updateParentSum(deltaAmount);
    }

    private void updateParentSum(double deltaAmount) {
        if (parent != null) {
            parent.addAmount(deltaAmount);
        }
    }
}
