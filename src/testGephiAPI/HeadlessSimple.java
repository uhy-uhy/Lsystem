/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package testGephiAPI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import processing.core.PApplet;

/**
 * This demo shows several actions done with the toolkit, aiming to do a complete chain,
 * from data import to results.
 * <p>
 * This demo shows the following steps:
 * <ul><li>Create a project and a workspace, it is mandatory.</li>
 * <li>Import the <code>polblogs.gml</code> graph file in an import container.</li>
 * <li>Append the container to the main graph structure.</li>
 * <li>Filter the graph, using <code>DegreeFilter</code>.</li>
 * <li>Run layout manually.</li>
 * <li>Compute graph distance metrics.</li>
 * <li>Rank color by degree values.</li>
 * <li>Rank size by centrality values.</li>
 * <li>Configure preview to display labels and mutual edges differently.</li>
 * <li>Export graph as PDF.</li></ul>
 * 
 * @author Mathieu Bastian
 */
public class HeadlessSimple {

    public void script() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);

        DirectedGraph graph = graphModel.getDirectedGraph();
        
        //Create three nodes
        Node n0 = graphModel.factory().newNode("n0");
        n0.getNodeData().setLabel("Node 0");
        n0.getNodeData().setSize(1);
        Node n1 = graphModel.factory().newNode("n1");
        n1.getNodeData().setLabel("Node 1");
        n1.getNodeData().setSize(1);
        Node n2 = graphModel.factory().newNode("n2");
        n2.getNodeData().setLabel("Node 2");
        n2.getNodeData().setSize(1);

        //Create three edges
        Edge e1 = graphModel.factory().newEdge(n1, n2, 1f, true);
        Edge e2 = graphModel.factory().newEdge(n0, n2, 2f, true);
        Edge e3 = graphModel.factory().newEdge(n2, n0, 2f, true);   //This is e2's mutual edge
        

        graph.addNode(n0);
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());


//        //Filter      
//        DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
//        degreeFilter.init(graph);
//        degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));     //Remove nodes with degree < 30
//        Query query = filterController.createQuery(degreeFilter);
//        GraphView view = filterController.filter(query);
//        graphModel.setVisibleView(view);    //Set the filter result as the visible view

//        //See visible graph stats
//        UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
//        System.out.println("Nodes: " + graphVisible.getNodeCount());
//        System.out.println("Edges: " + graphVisible.getEdgeCount());

        //Run YifanHuLayout for 100 passes - The layout always takes the current visible view
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(200f);

        
        layout.initAlgo();
        layout.goAlgo();
        layout.endAlgo();
        
        
//        layout.initAlgo();
//        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
//            layout.goAlgo();
//        }
//        layout.endAlgo();

        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel, attributeModel);

        //Rank color by Degree
        Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
        AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
        colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
        rankingController.transform(degreeRanking,colorTransformer);

        //Rank size by centrality
        AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
        sizeTransformer.setMinSize(3);
        sizeTransformer.setMaxSize(10);
        rankingController.transform(centralityRanking,sizeTransformer);

//        //Preview
//        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
//        model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, Color.BLACK);
//        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
//        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
//        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
        
        
        //Preview configuration
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        //PreviewModel previewModel = previewController.getModel();
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
        model.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        model.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
        model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
        previewController.refreshPreview();

        //New Processing target, get the PApplet
        ProcessingTarget target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
        PApplet applet = target.getApplet();
        applet.init();

        //Refresh the preview and reset the zoom
        target.refresh();
        target.resetZoom();

        //Add the applet to a JFrame and display
        JFrame frame = new JFrame("Test Preview");
        frame.setLayout(new BorderLayout());
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(applet, BorderLayout.CENTER);
        
        frame.pack();
        frame.setVisible(true);
       
        // 待機時間（ミリ秒）
        long waitChkTime = 5000;
        // 指定した秒数、スレッドを停止します。
        try {
            Thread.sleep(waitChkTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("sleep end");
        //ノード削除
//        graph.removeNode(graph.getNode("n0"));
        //ノード追加
        Node nx = graphModel.factory().newNode("nx");
        nx.getNodeData().setLabel("Node x");
        nx.getNodeData().setSize(1);
        Edge ex = graphModel.factory().newEdge(nx, n1, 2f, true);
        graph.addNode(nx);
        graph.addEdge(ex);

        rankingController.transform(degreeRanking,colorTransformer);
        rankingController.transform(centralityRanking,sizeTransformer);

        layout.initAlgo();
        layout.goAlgo();
        layout.endAlgo();
        
//        layout.initAlgo();
//        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
//            layout.goAlgo();
//        }
//        layout.endAlgo();
        
        
        previewController.refreshPreview();
        target.refresh();

        System.out.println("update");
    }
}
