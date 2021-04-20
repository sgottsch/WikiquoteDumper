import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Test2 {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		
		CompletableFuture<String> cfa = CompletableFuture.supplyAsync(() -> generateA(1));
		CompletableFuture<String> cfb = CompletableFuture.supplyAsync(() -> generateB(2));
		String fooA = cfa.get();
		String fooB = cfb.get();
		System.out.println("Final fooA " + fooA);
		System.out.println("Final fooB " + fooB);

	}
	
	public static String generateA(int id) {
	    System.out.println("Entering generateA " + Thread.currentThread());
	    sleep(2000);
	    System.out.println("Leaving generateA");
	    return "A" + id;
	}

	public static String generateB(int id) {
	    System.out.println("Entering generateB " + Thread.currentThread());
	    sleep(1000);
	    System.out.println("Leaving generateB");
	    return "B" + id;
	}

	private static void sleep(int n) {
	    try {
	        Thread.sleep(n);
	    } catch (InterruptedException ex) {
	        // never mind
	    }
	}

}
