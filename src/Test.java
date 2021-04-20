import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections4.ListUtils;

public class Test {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		List<CompletableFuture<String>> cfs = new ArrayList<CompletableFuture<String>>();
		CompletableFuture<String> cfa = CompletableFuture.supplyAsync(() -> generateA(1));
		CompletableFuture<String> cfb = CompletableFuture.supplyAsync(() -> generateB(2));
		CompletableFuture<String> cfc = CompletableFuture.supplyAsync(() -> generateA(3));
		CompletableFuture<String> cfd = CompletableFuture.supplyAsync(() -> generateB(4));
		CompletableFuture<String> cfe = CompletableFuture.supplyAsync(() -> generateA(5));
		CompletableFuture<String> cff = CompletableFuture.supplyAsync(() -> generateB(6));
		cfs.add(cfa);
		cfs.add(cfb);
		cfs.add(cfc);
		cfs.add(cfd);
		cfs.add(cfe);
		cfs.add(cff);

		Iterable<List<CompletableFuture<String>>> subSets = ListUtils.partition(cfs, 2);
		for (List<CompletableFuture<String>> subset : subSets) {
			System.out.println("Sub set: "+subset.size());
					List<String> res = new ArrayList<String>();
		for (CompletableFuture<String> cf : subset) {
				try {
					res.add(cf.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
				System.out.println(res);
		}

//		List<String> res=new ArrayList<String>();
//		for (CompletableFuture<String> x : cfs) {
//			res.add(x.get());
//		}

		// System.out.println("Final fooA " + fooA);
		// System.out.println("Final fooB " + fooB);

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
