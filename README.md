# Completable Futures

Do you know that parallel stream in Java has performance limitations due to Java will use the number of available processors?

If you want to take advance of the latest Java features and overcome this problem, use CompletableFuture.

You can create a ThreadPool greater than the number of available processors and reap the performance improvements.
CompletableFuture uses lambdas, and the code is expressive.

### For example, my machine has 8 processors
```java
   @Test
    public void numberOfProcessors() {
        assertThat(availableProcessors, is(equalTo(8)));
    }
```

### With more than 10 processes of 1 second, we would have everything processed in 1-second approx
```java
    @Test
    public void completableFuture_whenBooksAreMoreThanNumberOfProcessors() {
        Executor executor = Executors.newFixedThreadPool(10);

        long start = System.currentTimeMillis();
        var futureCategories = getBooks()
                .map(e -> CompletableFuture.supplyAsync(() -> BookClassifier.apply(e), executor))
                .toList();

        var categories = futureCategories.stream()
                .map(CompletableFuture::join).toList();
        int timeInSeconds = getTimeInSeconds(start);
        assertThat(categories.size(), is(equalTo(10)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(1));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(1));

    }
```

### With more than 10 processes of 1 second, we would have everything processed in more than 2-seconds 
Because our current limitation would have to process 8 books first and then 2 more
```java
    @Test
public void parallelStream_whenBooksAreMoreThanNumberOfProcessors() {
        long start = System.currentTimeMillis();
        var categories = getBooks()
        .parallel()
        .map(BookClassifier::apply)
        .toList();
        int timeInSeconds = getTimeInSeconds(start);

        System.out.printf("The operation took %s ms%n", timeInSeconds - start);
        assertThat(categories.size(), is(equalTo(10)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(1));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(2));
        }
```
### With more than 10 processes of 1 second, we would have everything processed in more than 1-second less than 2-seconds
Because we are within the limit of the available processors
```java

    @Test
    public void parallelStream_whenBooksAreLessThanNumberOfProcessors() {
        int limit = availableProcessors - 1;
        long start = System.currentTimeMillis();
        var categories = getBooks()
                .limit(limit)
                .parallel()
                .map(BookClassifier::apply)
                .toList();
        int timeInSeconds = getTimeInSeconds(start);

        System.out.printf("The operation took %s ms%n", timeInSeconds - start);
        assertThat(categories.size(), is(equalTo(limit)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(1));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(2));

    }
```
### Synchronous implementation has the worst performance
```java
   @Test
   public void stream_whenBooksAreLessThanNumberOfProcessors() {
        long start = System.currentTimeMillis();
        var categories = Stream.of(
        new Book("1", "Rich Hickey"),
        new Book("2", "Uncle Bob"),
        new Book("3", "Martin Fowler"))
        .map(BookClassifier::apply).toList();

        int timeInSeconds = getTimeInSeconds(start);
        assertThat(categories.size(), is(equalTo(3)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(1));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(3));

        }
```