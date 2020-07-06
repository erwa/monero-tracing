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


// time java GetSrcDstTypes SrcDstRingCTTraced > SrcDstRingCTTracedTypes
//
// Output has format:
//   srcType dstType
//
// SrcDstRingCTTraced has format:
//   srcId dstId idx1,keyId1 idx2,keyId2 ...
public class GetSrcDstTypes {

  public static void main(String[] args) throws Exception {
    String destKeysFile = args[0];

    process(destKeysFile);
  }

  private static void process(String destKeysFile) throws Exception {
    System.err.println("Reading from " + destKeysFile);

    Map<Integer, Integer> tracedTxTypeMap = Utils.loadTracedTxTypeMap();
    Map<Integer, String> txMap = Utils.loadTxIdToHashMap();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(destKeysFile))) {
      for (String line; (line = br.readLine()) != null; ) {
        // each line is "srcId destId idx1,keyId1 idx2,keyId2 ..."
        String[] parts = line.split(" ");
        int srcId = Integer.parseInt(parts[0]);
        int dstId = Integer.parseInt(parts[1]);

        int dstType = tracedTxTypeMap.get(dstId);
				
        String srcTx = txMap.get(srcId);
        int srcType = Utils.getType(srcTx);

        System.out.println(srcType + " " + dstType);

        count++;
        if (count % 100 == 0) {
          System.err.println("Processed " + count + " lines from " + destKeysFile);
        }
      }
    }
  }
}
