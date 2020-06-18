import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// java GetTracedTxVersionXmrInputs TracedInputsAllWithKeys TxHashesAll
public class GetTracedTxVersionXmrInputs {
  private static final String BASE_URL = "http://localhost:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  private static class Pair {
    int tx;
    int idx;
    Pair(int tx, int idx) {
      this.tx = tx;
      this.idx = idx;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }

      if (!(o instanceof Pair)) {
        return false;
      }

      Pair p = (Pair) o;
      return p.tx == this.tx && p.idx == this.idx;
    }

    @Override
    public int hashCode() {
      return 53 * Integer.hashCode(tx) + Integer.hashCode(idx);
    }
  }

  private static final Map<Integer, String> txToHashMap = new HashMap<>();

  private static final void loadTxMap(String file) throws Exception {
    System.err.println("loading txs from " + file);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        line = line.trim();
        txToHashMap.put(count, line); // count is idx
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " txs.");
        }
      }

      System.err.println("Done loading txMap. Size: " + txToHashMap.size());
      System.err.println("Count = " + count);
    }
  }

  public static void main(String[] args) throws Exception {
    String file = args[0];
    String txHashesFile = args[1];

    long start = System.currentTimeMillis();
    loadTxMap(txHashesFile);
    long time1 = System.currentTimeMillis();
    System.err.println("time to load tx hashes: " + ((time1 - start) / 1000));

    getTxInfo(file);
    long time2 = System.currentTimeMillis();
    System.err.println("time to get tx info: " + ((time2 - time1) / 1000));
  }

  private static void getTxInfo(String file) throws Exception {
    System.err.println("Processing all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        String idx = parts[1];

        processTxInput(tx, idx);

        count++;
        if (count % 1000 == 0) {
            System.err.println("Processed " + count + " inputs.");
        }
      }
    }
    System.err.println("Processed " + count + " inputs from file " + file);
  }

  private static int lastTx = -1;
  private static String lastTxVersion = null;
  private static String lastXmrInputs = null;

  private static void processTxInput(int tx, String idx) throws Exception {
    if (lastTx != tx) {
      lastTx = tx;
      String txHash = txToHashMap.get(tx);
      String url = BASE_URL + "transaction/" + txHash;
      HttpRequest request = HttpRequest.newBuilder()
              .GET()
              .uri(URI.create(url))
              .build();
      String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
      // find tx_version
      int txVersionStart = data.indexOf("tx_version\"");
      data = data.substring(txVersionStart);
      lastTxVersion = data.substring(12, data.indexOf(","));

      // find xmr_inputs (should appear after tx_version because keys are sorted alphabetically)
      int xmrInputsStart = data.indexOf("xmr_inputs");

      // there should be an xmr_outputs key after xmr_inputs, so searching for , should be safe
      lastXmrInputs = data.substring(xmrInputsStart + 12, data.indexOf(",", xmrInputsStart));
    }

    System.out.println(lastTx + " " + idx + " " + lastTxVersion + " " + lastXmrInputs);
  }
}