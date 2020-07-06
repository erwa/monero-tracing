
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


// Filters src dst with indices guesses down to those that we have ground truth for
// and prints out stats.
//
// time java FilterTracedSrcDst SrcDestTxsRingCTAllFilteredIndices3
public class FilterTracedSrcDst {

  public static void main(String[] args) throws Exception {
    String srcDstFile = args[0];

    process(srcDstFile);
  }

  private static void process(String file) throws Exception {
    Set<Pair> tracedInputs = Utils.loadTracedInputs();

    int notTraced = 0;
    int someTraced = 0;
    int allTraced = 0;

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        int src = Integer.parseInt(parts[0]);
        int dst = Integer.parseInt(parts[1]);

        Set<Integer> indices = new HashSet<>();
        for (int i = 2; i < parts.length; i++) {
          indices.add(Integer.parseInt(parts[i].split(",")[0]));
        }

        boolean isAllTraced = true;
        boolean isSomeTraced = false;

        for (int index : indices) {
          if (tracedInputs.contains(new Pair(dst, index))) {
            isSomeTraced = true;
          } else {
            isAllTraced = false;
          }
        }

        if (isSomeTraced) {
          System.out.println(line);
        }

        // save stats
        if (!isSomeTraced) {
          notTraced++;
        } else if (isAllTraced) {
          allTraced++;
        } else {
          someTraced++;
        }

        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " inputs from " + file);
        }
      }
    }

    System.err.println("Total number of src dst txs processed: " + count);
    System.err.println("None traced: " + notTraced);
    System.err.println("Some traced: " + someTraced);
    System.err.println("All traced: " + allTraced);
  }
}
