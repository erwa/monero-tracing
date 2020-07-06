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


// time java GetTracedDstTxTypes DestKeysRingCTTraced TracedTxTypeMap TxOutputsRingCTAll InputKeysAll
//
// Output has format:
//   srcType dstType
//
// DestKeysRingCTTraced has format:
//   dstId idx keyId
// TracedTxTypeMap has format: 
//   txId type
// TxOutputsRingCTAll has format:
//   txHash pubKey1,pubKey2,...
// InputKeysAll has format:
//   pubKey
public class GetTracedDstTxTypes {

  public static void main(String[] args) throws Exception {
    String destKeysFile = args[0];
    String tracedTxTypeMapFile = args[1];
    String txOutputsFile = args[2];
    String inputKeysFile = args[3];

    loadTracedTxTypeMap(tracedTxTypeMapFile);
    loadKeyMap(inputKeysFile);

    Map<String, String> keyTxMap = LoadKeyTxMap.loadKeyTxMap(txOutputsFile);
    process(destKeysFile, keyTxMap);
  }

  // keyId -> pubKeyHash
  private static final Map<Integer, String> keyMap = new HashMap<>();

  private static final void loadKeyMap(String file) throws Exception {
    System.err.println("loading keys from " + file);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        keyMap.put(count, line);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading keyMap. Size: " + keyMap.size());
      System.err.println("Count = " + count);
    }
  }

  private static Map<Integer, Integer> txTypeMap = new HashMap<>();

  private static void loadTracedTxTypeMap(String file) throws Exception {
    System.err.println("loading tx type map from " + file);
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        txTypeMap.put(Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]));
      }
    }
    System.err.println("done loading tx type map from " + file);
  }

  private static void process(String destKeysFile, Map<String, String> keyTxMap) throws Exception {
    System.err.println("Reading from " + destKeysFile);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(destKeysFile))) {
      for (String line; (line = br.readLine()) != null; ) {
        // each line is "destId idx keyId"
        String[] parts = line.split(" ");
        int dstId = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
				int key = Integer.parseInt(parts[2]);

        int dstType = txTypeMap.get(dstId);
				
        String keyHash = keyMap.get(key);
        String srcTx = keyTxMap.get(keyHash);
        int srcType = getType(srcTx);

        System.out.println(srcType + " " + dstType);

        count++;
        if (count % 100 == 0) {
          System.err.println("Processed " + count + " lines from " + destKeysFile);
        }
      }
    }
  }

  private static final String BASE_URL = "http://localhost:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();


  private static int getType(String tx) throws Exception {
    String url = BASE_URL + "transaction/" + tx;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();

    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

    // find tx_version
    int txVersionStart = data.indexOf("tx_version\"");
    data = data.substring(txVersionStart);
    int txVersion = Integer.parseInt(data.substring(12, data.indexOf(",")));

    if (txVersion == 1) {
      // clear -> clear
      return 1;
    }
    // else txVersion > 1

    // find xmr_inputs (should appear after tx_version because keys are sorted alphabetically)
    int xmrInputsStart = data.indexOf("xmr_inputs");

    // there should be an xmr_outputs key after xmr_inputs, so searching for , should be safe
    long xmrInputs = Long.parseLong(data.substring(xmrInputsStart + 12, data.indexOf(",", xmrInputsStart)));
    if (xmrInputs == 0) {
      // hidden -> hidden
      return 3;
    }
    // some clear -> hidden
    return 2;
  }
}
