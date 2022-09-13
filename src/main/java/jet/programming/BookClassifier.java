package jet.programming;

public class BookClassifier {

    public static BookCategory apply(Book book) {
        sleep();
        return new BookCategory("Category_" + book.id());
    }

    public static void sleep() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


