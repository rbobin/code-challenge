package com.google.rbobinx.controller;

import com.google.rbobinx.service.Service;
import com.google.rbobinx.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(Controller.ROOT_PATH)
public class Controller {

    static final String ROOT_PATH = "/transactionservice";

    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String TRANSACTION_PATH = "/transaction/{" + ID + "}";
    private static final String TYPES_PATH = "/types/{" + TYPE + "}";
    private static final String SUM_PATH = "/sum/{" + ID + "}";

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String OK = "ok";
    private static final String SUM = "sum";

    @Autowired
    Service service;

    @RequestMapping(value = TRANSACTION_PATH, method = RequestMethod.PUT)
    public Map<String, String> create(@PathVariable(ID) long id,
                       @RequestBody TransactionObject transactionObject) {

        Optional<String> maybeError = service.createTransaction(id, transactionObject.type, transactionObject.parent_id,
                transactionObject.amount);

        HashMap<String, String> result = new HashMap<>();
        if (maybeError.isPresent()) {
            result.put(ERROR, maybeError.get());
        } else {
            result.put(SUCCESS, OK);
        }

        return result;
    }

    @RequestMapping(value = TRANSACTION_PATH, method = RequestMethod.GET)
    public Transaction get(@PathVariable(ID) long id) {
        Optional<Transaction> maybeTransaction = service.getTransaction(id);

        if (maybeTransaction.isPresent()) {
            return maybeTransaction.get();
        } else {
            return null;
        }
    }

    @RequestMapping(value = TYPES_PATH, method = RequestMethod.GET)
    public Set<Long> listByType(@PathVariable(TYPE) String type) {
        return service.findByType(type);
    }

    @RequestMapping(value = SUM_PATH, method = RequestMethod.GET)
    public Map<String, Double> getSum(@PathVariable(ID) long id) {
        Optional<Double> maybeSum = service.getSum(id);

        if (maybeSum.isPresent()) {
            Map<String, Double> result = new HashMap<>();
            result.put(SUM, maybeSum.get());
            return result;
        } else {
            return null;
        }
    }
}
