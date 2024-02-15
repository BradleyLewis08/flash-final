package org.snlab.network;

import java.io.FileWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TreeWriter {
    @SuppressWarnings("unchecked")
    public TreeWriter(PrefixTree prefixTree) {
        this.prefixTree = prefixTree;

        JSONObject tree_Object = new JSONObject();

        tree_Object.put("root", "1");

        JSONArray nodes = new JSONArray();

        TreeNode[] nodeList = prefixTree.getNodes();
        for (int i = 0; i < nodeList.length; i++) {
            // TreeNode curr_node = nodeList[i];

            JSONObject curr_node = new JSONObject();
            curr_node.put("predicate", nodeList[i].getPrefix());
            curr_node.put("prefix", nodeList[i].getPredicate());
            nodes.add(curr_node);
        }
        tree_Object.put("nodes", nodes);

        FileWriter file = new FileWriter("prefix_tree.json");
        file.write(tree_Object.toJSONString());
        file.close();
    }
}
