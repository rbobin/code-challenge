package com.google.rbobinx;

import com.google.rbobinx.model.Transaction;
import com.owlike.genson.Genson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FunctionalTest {

    private static final String ROOT_URL = "http://localhost:8080/transactionservice";
    private static final Transaction PARENT_TRANSACTION = TestUtils.generateTransaction();

    private static WebTarget transactionPath(Client client, Long id) {
        return client.target(ROOT_URL).path("transaction").path(id.toString());
    }

    private static WebTarget sumPath(Client client, Long id) {
        return client.target(ROOT_URL).path("sum").path(id.toString());
    }

    private static class TestResults {
        List<Transaction> collisions;
        double sum;

        TestResults(List<Transaction> collisions, double sum) {
            this.collisions = collisions;
            this.sum = sum;
        }
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Client client = ClientBuilder.newClient();
        Genson genson = new Genson();
        putTransaction(genson, client, PARENT_TRANSACTION);

        final ExecutorService pool = Executors.newCachedThreadPool();
        Callable<TestResults> task = FunctionalTest::testTransactionsCreate;
        List<Future<TestResults>> futures = new ArrayList<>();
        List<TestResults> testResults = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            futures.add(pool.submit(task));
        }

        for (Future<TestResults> future : futures) {
            testResults.add(future.get());
        }

        double sumExpected = testResults.stream()
                .mapToDouble(t -> t.sum)
                .sum() + PARENT_TRANSACTION.getAmount();

        double sumActual = getSum(genson, client, PARENT_TRANSACTION.getId());

        assertEquals(sumExpected, sumActual, 0);

    }

    private static Map putTransaction(Genson genson, Client client, Transaction transaction) {
        Map<String, Object> map = new HashMap<>();
        map.put("parent_id", transaction.getParentId());
        map.put("amount", transaction.getAmount());
        map.put("type", transaction.getType());
        String string = genson.serialize(map);
        String result = transactionPath(client, transaction.getId())
                .request()
                .put(Entity.entity(string, MediaType.APPLICATION_JSON), String.class);
        return genson.deserialize(result, Map.class);
    }

    private static Double getSum(Genson genson, Client client, Long id) {
        String result = sumPath(client, id).request().get(String.class);
        return (Double) genson.deserialize(result, Map.class).get("sum");
    }

    private static TestResults testTransactionsCreate() {
        Client client = ClientBuilder.newClient();
        Genson genson = new Genson();
        double sum = 0.0;
        List<Transaction> collisions = new ArrayList<>();
        Transaction previousParent = PARENT_TRANSACTION;

        for (int i = 0; i < 1000; i++) {
            Transaction transaction = TestUtils.generateTransaction(100, previousParent);
            Map result = putTransaction(genson, client, transaction);
            if (result.containsKey("success")) {
                sum += transaction.getAmount();
                if (Math.random() < 0.3) {
                    previousParent = transaction;
                }
            } else {
                collisions.add(transaction);
            }
        }

        return new TestResults(collisions, sum);
    }
}
