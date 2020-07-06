import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// Counts correct and incorrect guesses by num mixins for merging outputs guesses for which we have at least some ground truth for.
//
// time java CountStatsTracedDstKeys DestKeysRingCTTraced
public class CountStatsTracedDstKeys {

  public static void main(String[] args) throws Exception {
    String srcDstFile = args[0];

    process(srcDstFile);
  }

  private static void process(String file) throws Exception {
    Map<Integer, String> txMap = Utils.loadTxIdToHashMap();
    Map<Pair, Integer> inputKeyMap = Utils.loadTracedInputKeyMap();

    int count = 0;
    Map<Integer, Integer> countCorrect = new TreeMap<>();
    Map<Integer, Integer> countIncorrect = new TreeMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        // each line looks like:
        //   dstId idx keyId
        String[] parts = line.split(" ");
        int dstId = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int keyId = Integer.parseInt(parts[2]);

        // figure out correct or incorrect
        boolean correct = inputKeyMap.get(new Pair(dstId, idx)) == keyId;
        // get num mixins
        String dstHash = txMap.get(dstId);
        int numMixins = Utils.getNumMixins(dstHash);
        if (numMixins > 10) {
          numMixins = 10;
        }

        // update stats
        if (correct) {
          countCorrect.compute(numMixins, (k, v) -> (v == null) ? 1 :
              v + 1);
        } else {
          countIncorrect.compute(numMixins, (k, v) -> (v == null) ? 1 :
              v + 1);
        }

        count++;
        if (count % 100 == 0) {
          System.err.println("Processed " + count + " txs from " + file);
        }
      }
    }

    System.err.println("Total number of dest inputs processed: " + count);

    // print stats
    for (int i = 0; i <= 10; i++) {
      if (i == 10) {
        System.out.print("10+ ");
      } else {
        System.out.print(i + " ");
      }

      System.out.print(countCorrect.getOrDefault(i, 0) + " ");
      System.out.println(countIncorrect.getOrDefault(i, 0));
    }
  }
}
