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


// java GetPartiallyTracedByDay TxDatesAll PartiallyTracedTxs
public class GetPartiallyTracedByDay {
  private static Map<Integer, String> txDayMap = new HashMap<>();
  private static Map<String, Integer> dayTxCount = new TreeMap<>();
  private static Set<Integer> tracedTxs = new HashSet<>();

  private static void loadTxDates(String file) throws Exception {
    System.err.println("Loading tx dates from file " + file);
    int txIdx = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String date; (date = br.readLine()) != null; ) {
        if (!dayTxCount.containsKey(date)) {
          dayTxCount.put(date, 0);
        }
        dayTxCount.put(date, dayTxCount.get(date) + 1);
        txDayMap.put(txIdx, date);
        txIdx++;
      }
    }
    System.err.println("Loaded " + txIdx + " transaction dates from file " + file);
  }

  private static void loadTracedTxs(String file) throws Exception {
    System.err.println("Loading traced tx from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String tx; (tx = br.readLine()) != null; ) {
        tracedTxs.add(Integer.parseInt(tx));
        count++;
      }
    }
    System.err.println("Loaded " + count + " traced transactions from file " + file);
  }

  public static void main(String[] args) throws Exception {
    String txDatesFile = args[0];
    String tracedTxsFile = args[1];

    loadTxDates(txDatesFile);
    loadTracedTxs(tracedTxsFile);
    process();
  }

  private static void process() throws Exception {
    // Map<String, Integer> dayTxCount
    Map<String, Integer> dayTracedTxCount = new HashMap<>();
    for (String day : dayTxCount.keySet()) {
      dayTracedTxCount.put(day, 0);
    }

    for (int tx : tracedTxs) {
      String day = txDayMap.get(tx);
      dayTracedTxCount.put(day, dayTracedTxCount.get(day) + 1);
    }

    for (Map.Entry<String, Integer> entry : dayTxCount.entrySet()) {
      String day = entry.getKey();
      System.out.print(day + "\t");
      int traced = dayTracedTxCount.getOrDefault(day, 0);
      System.out.println(traced * 1.0 / entry.getValue());
    }
  }
}