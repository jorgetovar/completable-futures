package jet.programming;

import org.hamcrest.number.OrderingComparison;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BookClassifierTest {

    private int availableProcessors;

    @BeforeEach
    public void setUp() {
        availableProcessors = Runtime.getRuntime().availableProcessors();
    }

    @Test
    public void numberOfProcessors() {
        assertThat(availableProcessors, is(equalTo(8)));
    }

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
        System.out.printf("The completableFuture operation took %s ms%n", timeInSeconds);

    }


    @Test
    public void parallelStream_whenBooksAreMoreThanNumberOfProcessors() {
        long start = System.currentTimeMillis();
        var categories = getBooks()
                .parallel()
                .map(BookClassifier::apply)
                .toList();
        int timeInSeconds = getTimeInSeconds(start);

        assertThat(categories.size(), is(equalTo(10)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(2));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(2));
        System.out.printf("The parallelStream10 operation took %s ms%n", timeInSeconds);

    }

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

        assertThat(categories.size(), is(equalTo(limit)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(1));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(1));
        System.out.printf("The parallelStream7 operation took %s ms%n", timeInSeconds);

    }

    @Test
    public void stream_whenBooksAreLessThanNumberOfProcessors() {
        long start = System.currentTimeMillis();
        var categories = getBooks()
                .map(BookClassifier::apply).toList();

        int timeInSeconds = getTimeInSeconds(start);
        assertThat(categories.size(), is(equalTo(10)));
        assertThat(timeInSeconds, OrderingComparison.greaterThanOrEqualTo(9));
        assertThat(timeInSeconds, OrderingComparison.lessThanOrEqualTo(10));
        System.out.printf("The stream operation took %s ms%n", timeInSeconds);

    }

    private Stream<Book> getBooks() {
        return Stream.of(
                new Book("1", "Uncle Bob 1"),
                new Book("2", "Rich Hickey 2"),
                new Book("3", "Andy Hunt 3"),
                new Book("4", "Erich Gamma 4"),
                new Book("5", "Steve McConnell 5"),
                new Book("6", "Martin Kleppmann 6"),
                new Book("7", "Eric Evans 7"),
                new Book("8", " Michael C. Feathers 8"),
                new Book("9", "Kent Beck 9"),
                new Book("10", "Martin Fowler 10")
        );
    }

    private int getTimeInSeconds(long start) {
        return (int) ((System.currentTimeMillis() - start) / 1000);
    }


}