# Code challenge

Build project:
```sh
$ gradle build
```

Run project:
```sh
$ gradle bootRun
```

### Description
The project is implemented using `HashMap<Id, Transaction>` for transaction efficient lookup by id and querying sum of linked transactions. Sum is updated right after new transaction inserted into map: the parent is updated with new amount, then parent updates grandparent and so on. Types are stored in form of `HashMap<String, HashSet<Long>>` for fast querying.

Due to time limits a lot of crucial things are not implemented, such as returning proper error codes and meaningful messages.

### Performance analysis
Create transaction ~ O(log n): 
- Check if transaction exists, hashmap query, O(1)
- Check if parent exists, hashmap query, O(1)
- Create new hashmap entry, O(1)
- Update parents, depends on use case (how high are usually transaction trees). I would assume not more than O(log n). If no parents then O(1)
- Update types hashmap: get set from hashmap O(1) + add new hashset element O(1) = O(1)

Get transaction O(1):
- Get entry from hashmap, O(1)

Get types O(1):
- Get entry from hashmap, O(1)

Get sum O(1):
- Get entry from hashmap, O(1)

This design was fairly simple to implement, but in reality, I think, new transactions are created more often than sums queried. If so, then adding new transaction should add a new leaf into sum tree, and querying sum should calculate the sum. That way the burden of calculation is shifted from adding new transaction to the moment of quering sum.

### Tests
All the requested features are implemented except code coverage. Most of the unit tests are there, but controller is not covered at all. Moreover, there is only one functional test for concurrency testing. It proves that parent sum is correct even under heavy load (5 threads, 1000 requests each). This test helped to identify that `AtomicDouble` is needed rather than `volatile double`. The test also showed that on my laptop application performance was ~1000 updates per second.

