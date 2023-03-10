import java.util.*;

/**
 * tree where each node contains an index, a value, and a list of child nodes
 * an ancillary array uses these indices to reference each node directly
 * updates are quick and it can be iterated recursively or by order of creation
 */
public class ReferencedTree {
    private class ReferencedNode {
        public Integer ID;
        public String article;
        public ArrayList<ReferencedNode> children;
    }
    private ArrayList<ReferencedNode> root;
    private ArrayList<ReferencedNode> directList;

    public ReferencedTree() {
        root.ID = 0;
        root.article = "";
        directList.add(root);
    }

    public AddNode(String article, Integer newID, Integer replyTo) {
        // TODO: use replyTo to index directList
        // check that newID matches would-be index in directList, append to directList
        // initialize children ArrayList in replyTo if not exists, append article w/ID to it
    }

    public String Read() {
        // TODO: recursively construct preview string like in the writeup
        // do not iterate the root
        return "";
    }

    public 

}