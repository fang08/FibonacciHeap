import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class keywordcounter {

	public static void main(String[] args) {
		try {
			FibonacciHeap heap = new FibonacciHeap();
			// read input file
			String filename = args[0];
			File input = new File(filename);
			BufferedReader reader = new BufferedReader(new FileReader(input));
			BufferedWriter writer = new BufferedWriter(new FileWriter("output_file.txt"));
			String readLine = "";
			HashMap<String, FibonacciHeap.Node> searchWords = new HashMap<String, FibonacciHeap.Node>();

			while ((readLine = reader.readLine()) != null && !readLine.equalsIgnoreCase("stop")) {
				// if read key words with search frequencies
				if (readLine.startsWith("$")) {
					String name = readLine.substring(1, readLine.lastIndexOf(" "));
					int count = Integer.parseInt(readLine.substring(readLine.lastIndexOf(" ") + 1));
					// if it's a new node
					if (!searchWords.containsKey(name)) {
						FibonacciHeap.Node n = heap.new Node(name, count);
						searchWords.put(name, n);
						heap.insertHeap(n);
					} else { // node has existed
						FibonacciHeap.Node x = searchWords.get(name);
						heap.increaseKey(x, count);
					}
				// if read number of output
				} else if (readLine.matches("\\d+")) {
					int top = Integer.parseInt(readLine);
					String output = "";
					if (top <= heap.numNode) {
						ArrayList<FibonacciHeap.Node> extracted = new ArrayList<FibonacciHeap.Node>();
						for (int i = 0; i < top; i++) {
							// max nodes are extracted then add back to the heap for future query
							FibonacciHeap.Node max = heap.extractMax();
							FibonacciHeap.Node reinsertNode = heap.new Node(max.keyword, max.val);
							extracted.add(reinsertNode);
							output = output.concat(max.keyword + ",");
							searchWords.remove(max.keyword);
							searchWords.put(max.keyword, reinsertNode);
						}
						// add back previous max nodes
						for (FibonacciHeap.Node n : extracted)
							heap.insertHeap(n);

						output = output.substring(0, output.length() - 1);
						output = output.concat("\n");
						writer.write(output);
					} else {
						System.out.println("Output number exceed the total number.");
					}
				}
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
