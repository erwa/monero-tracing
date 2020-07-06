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


// Counts true, false, and unknown positives for merging outputs guesses for which we have at least some ground truth for.
//
// time java CountPositivesSrcDst SrcDstRingCTTraced
public class CountPositivesSrcDst {

  public static void main(String[] args) throws Exception {
    String srcDstFile = args[0];

    process(srcDstFile);
  }

  private static void process(String file) throws Exception {
    Map<Pair, Integer> inputKeyMap = Utils.loadTracedInputKeyMap();

    int count = 0;
    int truePos = 0;
    int falsePos = 0;
    int unkPos = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        // each line looks like:
        //   srcId dstId  idx1,keyId1 idx2,keyId2 ...
        String[] parts = line.split(" ");
        int dstId = Integer.parseInt(parts[1]);

        boolean errorDetected = false;
        boolean missing = false;
        boolean someCorrect = false;
        for (int i = 2; i < parts.length; i++) {
          String[] idxKey = parts[i].split(",");
          int idx = Integer.parseInt(idxKey[0]);
          int key = Integer.parseInt(idxKey[1]);
          Pair input = new Pair(dstId, idx);

          if (inputKeyMap.containsKey(input)) {
            if (inputKeyMap.get(input) != key) {
              errorDetected = true;
            } else {
              someCorrect = true;
            }
          } else {
            missing = true;
          }
        }

        // update stats
        if (errorDetected) {
          falsePos++;
        } else if (!someCorrect) {
          throw new RuntimeException();
        } else if (missing) {
          unkPos++;
        } else {
          truePos++;
        }

        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " txs from " + file);
        }
      }
    }

    System.err.println("Total number of dest txs processed: " + count);

    // print stats
    System.out.println("Num true positives: " + truePos);
    System.out.println("Num false positives: " + falsePos);
    System.out.println("Num unknown positives: " + unkPos);
  }
}
