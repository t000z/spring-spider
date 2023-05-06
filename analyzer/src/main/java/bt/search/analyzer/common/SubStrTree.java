package bt.search.analyzer.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class SubStrTree {
    String data;
    Set<SubStrTree> node;

    public SubStrTree(String data) {
        this.data = data;
        this.node = new HashSet<>();
    }

    public void addNode(SubStrTree sub) {
        this.node.add(sub);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
