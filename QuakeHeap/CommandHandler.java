package cmsc420_s22;

// YOU SHOULD NOT MODIFY THIS FILE

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Command handler. This reads a single command line, processes the command (by
 * invoking the appropriate method(s) on the data structure), and returns the
 * result as a string.
 */

public class CommandHandler {

	private boolean initialized; // have we initialized the structure yet?
	private QuakeHeap<Integer, String> heap; // heap
	private HashMap<String, QuakeHeap<Integer, String>.Locator> locators; // locators

	/**
	 * Default constructor.
	 */
	public CommandHandler() {
		initialized = false;
		heap = null;
		locators = null;
	}

	/**
	 * Initialize with a given number of levels.
	 */
	void initialize(int nLevels) {
		heap = new QuakeHeap<Integer, String>(nLevels); // initialize the heap
		locators = new HashMap<String, QuakeHeap<Integer, String>.Locator>(); // initialize locators
		initialized = true;
	}

	/**
	 * Process a single command and return the string output. Each command begins
	 * with a command followed by a list of arguments. The arguments are separated
	 * by colons (":").
	 * 
	 * @param inputLine The input line with the command and parameters.
	 * @return A string summary of the command's execution/result.
	 */
	public String processCommand(String inputLine) throws Exception {
		Scanner line = new Scanner(inputLine);
		line.useDelimiter(":"); // use ":" to separate arguments
		String output = new String(); // for storing summary output
		String cmd = (line.hasNext() ? line.next() : ""); // next command
		try {
			// -----------------------------------------------------
			// SET-N-LEVELS
			// - sets the number of levels in the heap
			// - this must be the first command
			// -----------------------------------------------------
			if (cmd.compareTo("set-n-levels") == 0) {
				int newLevels = line.nextInt(); // read the number of levels
				if (!initialized) {
					initialize(newLevels); // initialize with this many levels
					output += "set-n-levels(" + newLevels + "): " + System.lineSeparator();
				} else {
					output += "set-n-levels(" + newLevels + "): ";
					heap.setNLevels(newLevels);
					output += "successful" + System.lineSeparator();

				}
			}
			// -----------------------------------------------------
			// COMMENT string
			// - comment line for the output
			// -----------------------------------------------------
			else if (cmd.compareTo("comment") == 0) {
				String comment = line.next(); // read the comment
				output += "[" + comment + "]" + System.lineSeparator();
			}

			// -----------------------------------------------------
			// INSERT label key
			// - add item with given label and key and save its location
			// -----------------------------------------------------
			else if (cmd.compareTo("insert") == 0) {
				confirmInitialized(); // confirm that we are initialized
				String label = line.next(); // read the label
				int key = line.nextInt(); // read the key
				QuakeHeap<Integer, String>.Locator loc = locators.get(label); // get this entry's locator
				if (loc != null) {
					throw new Exception("Error - Attempt to insert duplicate label: " + label);
				}
				output += "insert(" + key + ", " + label + "): ";
				loc = heap.insert(key, label); // add to heap
				locators.put(label, loc); // save its locator
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// LIST-HEAP
			// - list the heap contents, with each tree in preorder
			// -----------------------------------------------------
			else if (cmd.compareTo("list-heap") == 0) {
				confirmInitialized(); // confirm that we are initialized
				ArrayList<String> list = heap.listHeap();
				if (list == null)
					throw new Exception("Error - list-heap returned a null result");
				output += "list-heap:" + System.lineSeparator();
				for (String s : list) {
					output += "  " + s + System.lineSeparator();
				}
				output += heapStructure(list); // summarize heap contents (indented)
			}
			// -----------------------------------------------------
			// GET-MIN-KEY
			// - get the minimum element from the heap
			// -----------------------------------------------------
			else if (cmd.compareTo("get-min-key") == 0) {
				confirmInitialized(); // confirm that we are initialized
				output += "get-min-key: ";
				Integer key = heap.getMinKey(); // get the minimum
				output += (key == null ? "null" : key) + System.lineSeparator();
			}
			// -----------------------------------------------------
			// GET-MAX-LEVEL
			// - get the maximum level at which key with label occurs
			// -----------------------------------------------------
			else if (cmd.compareTo("get-max-level") == 0) {
				confirmInitialized(); // confirm that we are initialized
				String label = line.next(); // read the label
				QuakeHeap<Integer, String>.Locator loc = locators.get(label); // get this entry's locator
				if (loc == null) {
					throw new Exception("Error - get-max-level of nonexistent label: " + label);
				}
				int level = heap.getMaxLevel(loc); // get this entry's max level
				output += "get-max-level(" + label + "): " + level + System.lineSeparator();
			}
			// -----------------------------------------------------
			// EXTRACT-MIN
			// - extract the minimum element from the heap
			// -----------------------------------------------------
			else if (cmd.compareTo("extract-min") == 0) {
				confirmInitialized(); // confirm that we are initialized
				output += "extract-min: ";
				String label = heap.extractMin(); // extract the minimum
				locators.remove(label); // remove from locators
				output += label + System.lineSeparator();
			}
			// -----------------------------------------------------
			// DECREASE-KEY
			// - decrease the key value of some entry
			// -----------------------------------------------------
			else if (cmd.compareTo("decrease-key") == 0) {
				confirmInitialized(); // confirm that we are initialized
				String label = line.next(); // read the label
				int newKey = line.nextInt(); // read the new key value
				QuakeHeap<Integer, String>.Locator loc = locators.get(label); // get this entry's locator
				if (loc == null) {
					throw new Exception("Error - Attempt to decrease-key of nonexistent label: " + label);
				}
				output += "decrease-key(" + label + ", " + newKey + "): ";
				heap.decreaseKey(loc, newKey); // decrease the key
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// CLEAR
			// -----------------------------------------------------
			else if (cmd.compareTo("clear") == 0) {
				confirmInitialized(); // confirm that we are initialized
				heap.clear(); // clear the heap
				locators.clear(); // clear the locators
				output += "clear: successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// SIZE
			// -----------------------------------------------------
			else if (cmd.compareTo("size") == 0) {
				int size = heap.size(); // get the heap's current size
				output += "size: " + size + System.lineSeparator();
			}
			// -----------------------------------------------------
			// SET-QUAKE-RATIO
			// - set the quake ratio
			// -----------------------------------------------------
			else if (cmd.compareTo("set-quake-ratio") == 0) {
				double newRatio = line.nextDouble(); // read the new ratio
				output += "set-quake-ratio(" + newRatio + "): ";
				heap.setQuakeRatio(newRatio); // set the new ratio
				output += "successful" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// Unrecognized command
			// -----------------------------------------------------
			else {
				if (cmd.compareTo("") == 0)
					System.err.println("Error: Empty command line (Ignored)");
				else
					System.err.println("Error: Invalid command - \"" + cmd + "\" (Ignored)");
			}
			line.close();
		} catch (Exception e) { // exception thrown?
			output += "Failure due to exception: \"" + e.getMessage() + "\"" + System.lineSeparator();
		} catch (Error e) { // error occurred?
			System.err.print("Operation failed due to error: " + e.getMessage());
			e.printStackTrace(System.err);
		} finally { // always executed
			line.close(); // close the input scanner
		}
		return output; // return summary output
	}
	
	/**
	 * Confirm that the data structure has been initialized, or
	 * throw an exception.
	 */
	void confirmInitialized() throws Exception {
		if (!initialized) {
			throw new Exception("Uninitialized. First command must be set-n-levels.");
		}
	}

	/**
	 * Print the heap contents with indentation.
	 * 
	 * @param entries List of entries in preorder
	 * @return String encoding the heap structure
	 */
	static String heapStructure(ArrayList<String> entries) {
		String output = "Structured list:" + System.lineSeparator();
		ListIterator<String> iter = entries.listIterator(); // iterator for the list
		while (iter.hasNext()) { // structure has more
			output += heapStructureLevel(iter, "  "); // print one level
		}
		return output;
	}

	/**
	 * Prints a single level with indentation.
	 * 
	 * @param iter   Iterator for the entries in the list
	 * @param indent String indentation for the current line
	 */
	static String heapStructureLevel(ListIterator<String> iter, String indent) {
		String output = "";
		String entry = iter.next(); // get the next entry
		if (entry.length() == 0 || entry.charAt(0) != '{') { // expecting a level "{ ... }"
			System.err.println("Invalid preorder structure - Expecting '{'"); // invalid structure
		} else {
			output += indent + entry + System.lineSeparator();
			int treeCt = 0;
			while (iter.hasNext()) { // process all the trees at this level
				entry = iter.next(); // get the next entry
				if (entry.length() > 0 && entry.charAt(0) != '{') { // looks like a tree node
					output += indent + indent + "Tree: " + (treeCt++) + System.lineSeparator();
					iter.previous(); // back the iterator up
					output += heapStructureTree(iter, indent + indent + indent); // print one tree
				} else {
					iter.previous(); // back the iterator up
					break; // done with this level
				}
			}
		}
		return output;
	}

	/**
	 * Recursive helper to print a single subtree given in preorder.
	 * 
	 * @param iter   Iterator for the entries in the list
	 * @param indent String indentation for the current line
	 */
	static String heapStructureTree(ListIterator<String> iter, String indent) {
		final String levelIndent = "| "; // the indentation for each level of the tree
		String output = "";
		if (iter.hasNext()) {
			String entry = iter.next(); // get the next entry
			if (entry.length() > 0 && entry.charAt(0) == '[') { // external node entry
				output += indent + entry + System.lineSeparator();
			} else if (entry.length() > 0 && entry.charAt(0) == '(') { // internal node entry
				output += heapStructureTree(iter, indent + levelIndent); // print left subtree
				output += indent + entry + System.lineSeparator(); // print this node
				output += heapStructureTree(iter, indent + levelIndent); // print right subtree
			} else {
				System.err.println("Invalid preorder structure - Expecting '(' or '['"); // invalid structure
			}
		} else {
			System.err.println("Invalid preorder structure - Expecting more entries"); // invalid structure
		}
		return output;
	}
}
