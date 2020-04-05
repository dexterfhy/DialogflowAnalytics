package sns.nv;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import lombok.Getter;
import org.jgrapht.ext.JGraphXAdapter;
import sns.nv.beans.IntentEdge;
import sns.nv.beans.IntentVertex;

import javax.swing.*;
import java.awt.*;

public class IntentGraphAdapter extends JApplet {

    private SimpleDirectedIntentBeanGraph graph;
    private static Dimension defaultSize = new Dimension(1200, 720);

    @Getter
    private JGraphXAdapter<IntentVertex, IntentEdge> jgxAdapter;

    private IntentGraphAdapter() {}

    public IntentGraphAdapter(SimpleDirectedIntentBeanGraph graph) {
        this.graph = graph;
    }

    public void display() {
        init();

        JFrame frame = new JFrame();
        defaultSize = frame.getBounds().getSize();
        frame.getContentPane().add(this);
        frame.setTitle("Intent Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void init() {
        jgxAdapter = new JGraphXAdapter<>(graph.getGraph());

        setPreferredSize(defaultSize);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(defaultSize);

        mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter);

        layout.setParallelEdgeSpacing(50);
        layout.setIntraCellSpacing(50);
        layout.setInterRankCellSpacing(100);

        layout.execute(jgxAdapter.getDefaultParent());
    }

}
