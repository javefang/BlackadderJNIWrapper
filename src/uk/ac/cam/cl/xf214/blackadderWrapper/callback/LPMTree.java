package uk.ac.cam.cl.xf214.blackadderWrapper.callback;

import java.util.Arrays;
import java.util.HashMap;

import uk.ac.cam.cl.xf214.blackadderWrapper.BAHelper;
import uk.ac.cam.cl.xf214.blackadderWrapper.BAWrapper;
import uk.ac.cam.cl.xf214.blackadderWrapper.data.BAItem;
import uk.ac.cam.cl.xf214.blackadderWrapper.data.BAScope;

public class LPMTree<V> {
	public static final int DEFAULT_SCOPE_ID_LEN = 8;
	static int scopeIdLen = DEFAULT_SCOPE_ID_LEN;
	
	private Node<V> root;
	
	public LPMTree() {
		root = new Node<V>();
	}
	
	private static void print(String s) {
		System.out.println(s);
	}
	
	public void add(byte[] rid, V value) {
		//print("Inserting RID: " + BAHelper.byteToHex(rid) + "(" + value + ")");
		byte[][] chunks = tokenize(rid, scopeIdLen);
		int ptr = 0;
		Node<V> currentNode = root;
		
		while (currentNode != null && ptr < chunks.length) {
			if (currentNode.hasChild(chunks[ptr])) {
				// only update currentNode if child node already exist
				currentNode = currentNode.getChild(chunks[ptr]);
				//print("Child exist: " + currentNode);
			} else {
				// no such child yet, create a new one and update currentNode
				Node<V> newChild = new Node<V>(chunks[ptr]);
				//print("Adding child to exisiting node: " + currentNode);
				currentNode.addChild(newChild);
				currentNode = newChild;
			}
			ptr++;	// increament the chunk pointer to point to the next chunk
		}
		
		// set value for the last node
		currentNode.setValue(value);
	}
	
	public boolean delete(byte[] rid) {
		byte[][] chunks = tokenize(rid, scopeIdLen);
		int ptr = 0;
		Node<V> currentNode = root;
		
		// array that stores all the node for this rid
		
		Node<V>[] checkRemove = new Node[chunks.length];
		
		while (currentNode != null && ptr < chunks.length) {
			if (currentNode.hasChild(chunks[ptr])) {
				checkRemove[ptr] = currentNode.getChild(chunks[ptr]);
				currentNode = checkRemove[ptr];
			} else {
				// failed to find the rid
				return false;
			}
			ptr++;	// increment the chunk pointer
		}
		
		// remove value of the leaf
		currentNode.setValue(null);
		Node<V> toBeRemoved = null;
		for (int i = checkRemove.length - 1; i >= 0; i--) {
			// remove child
			if (toBeRemoved != null) {
				checkRemove[i].removeChild(toBeRemoved);
				//print("Node size = " + checkRemove[i].size());
				toBeRemoved = null;
			}
			// check if the node itself need to be removed
			if (!checkRemove[i].isValid()) {
				//print("Node " + checkRemove[i] + " is no longer valid, remove in next iteration");
				toBeRemoved = checkRemove[i];
			}
		}
		
		// remove from root
		if (toBeRemoved != null) {
			root.removeChild(toBeRemoved);
		}
		
		return true;
	}
	
	public V searchPrefix(byte[] rid) {
		byte[][] chunks = tokenize(rid, scopeIdLen);
		int ptr = 0;
		Node<V> currentNode = root;
		
		while (currentNode != null && ptr < chunks.length) {
			if (currentNode.hasChild(chunks[ptr])) {
				// further prefix match found, continue search for next chunk
				currentNode = currentNode.getChild(chunks[ptr]);
			} else {
				// prefix only matches to chunk[i], stop searching
				break;
			}
			ptr++;	// increament the chunk pointer
		}
		
		return currentNode.getValue();
	}
	
	private static byte[][] tokenize(byte[] rid, int chunkSize) {
		byte[][] chunks = new byte[rid.length / chunkSize][chunkSize];
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = Arrays.copyOfRange(rid, i * chunkSize, (i+1) * chunkSize);
		}
		return chunks;
	}
	
	public static void setScopeIdLen(int newLen) {
		scopeIdLen = newLen;
	}
	
	public static void main(String[] args) {
		// LOAD JNI LIBRARY
		String sharedObjPath = "/home/jave/workspace/BlackadderJNIWrapper/libs/";
		// System.load(sharedObjPath + "libgnustl_shared.so");
		System.load(sharedObjPath + "libblackadder.so");
		BAWrapper.configureObjectFile(sharedObjPath
				+ "libuk_ac_cam_cl_xf214_blackadderWrapper.so");
		
		
		String rootScopeHex = "0000000000000000";
		String voiceScopeHex = "2222222222222222";
		String streamItemHex = "1231238714987123";
		String pubStreamItemHex = "9999999999999999";
		String unknownItemHex = "8761287361876287";
		
		BAScope root = BAScope.createBAScope(rootScopeHex);
		BAScope voice = BAScope.createBAScope(voiceScopeHex, root);
		BAItem stream = BAItem.createBAItem(streamItemHex, voice);
		BAItem pubStream = BAItem.createBAItem(pubStreamItemHex, voice);
		BAItem unknownItem = BAItem.createBAItem(unknownItemHex, voice);
		
		LPMTree<String> tree = new LPMTree<String>();
		tree.add(root.getFullId(), "Root");
		tree.add(voice.getFullId(), "Voice");
		tree.add(stream.getFullId(), "Stream");
		tree.add(pubStream.getFullId(), "Pub");
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			String s1 = tree.searchPrefix(unknownItem.getFullId());
			if (s1 != null) System.out.println(s1);
		}
		
		
		long elapsed = System.currentTimeMillis() - startTime;
		
		print("Deleting voice");
		tree.delete(voice.getFullId());
		print("Deleting stream");
		tree.delete(stream.getFullId());
		//print("Deleting pubstream");
		//tree.delete(pubStream.getFullId());
		print("Deleting root");
		tree.delete(root.getFullId());
		
		String s = tree.searchPrefix(pubStream.getFullId());
		print(s);
		
		tree.delete(pubStream.getFullId());
		
		
		System.out.printf("Elapsed time: %.2f sec\n", elapsed / 1000.0); 
	}
}

class Node<V> {
	private byte[] chunk;
	private V value;
	private HashMap<Integer, Node<V>> children;
	private boolean root;
	
	public Node() {
		root = true;
		this.children = new HashMap<Integer, Node<V>>();
	}
	
	public Node(byte[] chunk) {
		this();
		//print("Creating node with chunk: " + BAHelper.byteToHex(chunk));
		root = false;
		this.chunk = chunk;
	}
	
	public Node(byte[] chunk, V value) {
		this(chunk);
		this.value = value;
		
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
	public V getValue() {
		return value;
	}
	
	// a node is valid if 1) it has child(ren), or 2) it has value 
	public boolean isValid() {
		return children.size() != 0 || value != null;
	}
	
	public boolean hasChild(byte[] chunk) {
		int hashCode = Arrays.hashCode(chunk);
		return children.containsKey(hashCode);
	}
	
	public int size() {
		return children.size();
	}
	
	public void addChild(Node<V> child) {
		//print("Adding child: " + child);
		int hashCode = child.hashCode();
		if (!children.containsKey(hashCode)) {
			children.put(hashCode, child);
		}
	}
	
	public void removeChild(Node<V> child) {
		if (children.containsKey(child.hashCode())) {
			//print("Removing child: " + child);
			children.remove(child.hashCode());
		}
	}
	
	public Node<V> getChild(byte[] chunk) {
		return children.get(Arrays.hashCode(chunk));
	}
	
	public boolean isRoot() {
		return root;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(chunk);
	}
	
	@Override
	public String toString() {
		if (root) {
			return "# ROOT #";
		} else {
			return BAHelper.byteToHex(chunk);
		}
	}
	
	private static void print(String s) {
		System.out.println(s);
	}
}
