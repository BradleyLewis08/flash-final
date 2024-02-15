package org.snlab.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;




class JsonTreeNode {
	public int predicate;
	public String leftId;
	public String rightId;
}
class JsonTree {
	public String rootId;
	public HashMap<String, JsonTreeNode> nodes;
}

public class PrefixTreeLoader {
	public PrefixTreeLoader(String fileName) {
		File jsonFile = new File(fileName);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonTree tree = mapper.readValue(jsonFile, JsonTree.class);
			JsonTreeNode rootNode = tree.nodes.get(tree.rootId);
			if(rootNode == null) {
				throw new Exception("Root node not found");
			}

			TreeNode root = new TreeNode()


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
