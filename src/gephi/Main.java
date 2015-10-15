package gephi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import org.apache.bcel.generic.I2F;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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

public class Main  implements Runnable,KeyListener {
	private ProjectController pc;
	private Workspace workspace;
	private Container container;
	private ImportController importController;

	private AttributeModel attributeModel;
	private GraphModel graphModel;
	private PreviewModel model;
	private RankingController rankingController;

	private  DirectedGraph graph;

	//Get Centrality
	private GraphDistance distance;

	//Rank color by Degree
	private Ranking degreeRanking;
	private AbstractColorTransformer colorTransformer;
	//Rank size by centrality
	private AttributeColumn centralityColumn;
	private Ranking centralityRanking;
	private AbstractSizeTransformer sizeTransformer;
	//Rank label size - set a multiplier size
	private Ranking centralityRanking2;
	private AbstractSizeTransformer labelSizeTransformer;

	//Preview configuration
	private PreviewController previewController;

	//New Processing target, get the PApplet
	private ProcessingTarget target;
	private PApplet applet;

	JFrame frame;
	private Thread thread;
	private boolean start = false;

	public Main(){
		pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();

		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		rankingController = Lookup.getDefault().lookup(RankingController.class);

		graph = graphModel.getDirectedGraph();

		//Create three nodes
		Node n0 = graphModel.factory().newNode("1");
		n0.getNodeData().setLabel("1");
		//n0.getNodeData().setSize(1);

		graph.addNode(n0);

		distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
		rankingController.transform(degreeRanking,colorTransformer);

		centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(5);
		sizeTransformer.setMaxSize(20);
		rankingController.transform(centralityRanking,sizeTransformer);

		//		centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		//		labelSizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
		//		labelSizeTransformer.setMinSize(1);
		//		labelSizeTransformer.setMaxSize(3);
		//		rankingController.transform(centralityRanking2,labelSizeTransformer);

		previewController = Lookup.getDefault().lookup(PreviewController.class);
		//model.getProperties().putValue(PreviewProperty.ARROW_SIZE, Boolean.TRUE);
		//model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(1f));
		//model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
		model.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		//		model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
		model.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 1f);
		model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		previewController.refreshPreview();

		target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		applet = target.getApplet();
		applet.init();
		applet.addKeyListener(this);

		//Refresh the preview and reset the zoom
		target.refresh();
		target.resetZoom();

		//Add the applet to a JFrame and display
		frame = new JFrame("Preview");
		frame.setLayout(new BorderLayout());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(applet, BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);

		frame.addKeyListener(this);

		thread = new Thread(this);
		thread.start();
	}

	public Main(String filename){
		pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();

		//Import file
		importController = Lookup.getDefault().lookup(ImportController.class);
		try {
			File file = new File(getClass().getResource(filename).toURI());
			container = importController.importFile(file);
			container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		//Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);

		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		rankingController = Lookup.getDefault().lookup(RankingController.class);

		graph = graphModel.getDirectedGraph();

		//無理やりエッジの太さを決める
		for(Edge e:graph.getEdges()){
			e.setWeight(3);
		}


		distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		//		degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		//		colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
		//		colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
		//		rankingController.transform(degreeRanking,colorTransformer);

		centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(5);
		sizeTransformer.setMaxSize(100);
		rankingController.transform(centralityRanking,sizeTransformer);

		//		centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		//	    labelSizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
		//	    labelSizeTransformer.setMinSize(1);
		//	    labelSizeTransformer.setMaxSize(3);
		//	    rankingController.transform(centralityRanking2,labelSizeTransformer);

		//	    YifanHuLayout();

		previewController = Lookup.getDefault().lookup(PreviewController.class);
		//model.getProperties().putValue(PreviewProperty.ARROW_SIZE, Boolean.TRUE);
		//model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(1f));
		//model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
		model.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		//		model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
		model.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 1f);
		model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		previewController.refreshPreview();

		target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		applet = target.getApplet();
		applet.init();
		applet.addKeyListener(this);

		//Refresh the preview and reset the zoom
		target.refresh();
		target.resetZoom();

		//Add the applet to a JFrame and display
		frame = new JFrame("Preview");
		frame.setLayout(new BorderLayout());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(applet, BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);

		thread = new Thread(this);
		thread.start();
	}
	public void ranking(){
		distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
		rankingController.transform(degreeRanking,colorTransformer);

		centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(3);
		sizeTransformer.setMaxSize(20);
		rankingController.transform(centralityRanking,sizeTransformer);

		centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		labelSizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
		labelSizeTransformer.setMinSize(1);
		labelSizeTransformer.setMaxSize(3);
		rankingController.transform(centralityRanking2,labelSizeTransformer);
	}

	public void YifanHuLayout(){
		//レイアウト
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		layout.setOptimalDistance(200f);

		//		layout.initAlgo();
		//		layout.goAlgo();
		//		layout.endAlgo();

		layout.initAlgo();
		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
	}

	public void addNode(Node node){
		graph.addNode(node);
	}
	public void removeNode(Node node){
		graph.removeNode(node);
	}
	public void addEdge(Edge edge){
		graph.addEdge(edge);
	}
	public void removeEdge(Edge edge){
		graph.removeEdge(edge);
	}

	public void generate_sample(){
		String num = String.valueOf(graph.getNodeCount()+1);
		Node n = graphModel.factory().newNode(num);
		n.getNodeData().setLabel(num);
		//n.getNodeData().setSize(1);
		addNode(n);

		if(graph.getNodeCount() > 1){
			Random rnd = new Random();
			String ran =  String.valueOf(rnd.nextInt(graph.getNodeCount()) + 1);
			while(ran.equals(num)){
				ran =  String.valueOf(rnd.nextInt(graph.getNodeCount()) + 1);
			}
			Node target = graph.getNode(ran);

			Edge e = graphModel.factory().newEdge(n, target, 2f, true);
			addEdge(e);
		}
	}

	public void run() {
		HashMap<String,Boolean> map = new HashMap<String,Boolean>();
		Node startNode = graph.getNode("86543217");
		startNode.getNodeData().setColor(1.0f,0.0f, 0.0f);
		startNode.getNodeData().setSize(20);
		ArrayDeque<Node> nextNode  = new ArrayDeque<Node>();
		ArrayDeque<Node> nowNode  = new ArrayDeque<Node>();
		nowNode.add(startNode);
		while(true)
		{
			if(start){
				//				generate_sample();
				//				ranking();
				//				YifanHuLayout();

				while(!nowNode.isEmpty()){
					Node node = nowNode.poll();
					map.put(node.getNodeData().getId(), true);
					for(Edge edge:graph.getEdges(node)){
						//						if(edge.getSource() == node){
						//							edge.getEdgeData().setColor(1.0f,0.0f, 0.0f);
						//							edge.setWeight(5.0f);
						//							nextNode.add(edge.getTarget());
						//						}
						edge.getEdgeData().setColor(1.0f,0.0f, 0.0f);
						edge.setWeight(5.0f);
						Node target  = edge.getTarget();
						Node source = edge.getSource();
						if(!map.containsKey(target.getNodeData().getId()))
							nextNode.add(target);
						if(!map.containsKey(source.getNodeData().getId()))
							nextNode.add(source);
					}
				}
				System.out.println(nextNode.size());
				while(!nextNode.isEmpty()){
					Node node = nextNode.poll();
					node.getNodeData().setColor(1.0f,0.0f, 0.0f);
					if(node.getNodeData().getSize() < 20f)
						node.getNodeData().setSize(20);
					nowNode.add(node);
				}

				previewController.refreshPreview();
				target.refresh();
			}
			try {
				Thread.sleep(200);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		//Main main = new Main();
		Main main = new Main("/AllNode_Yifan.gexf");
	}

	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();
		if (keycode == 16){
			if(!start) start = true;
			else start = false;
		}
	}

	public void keyReleased(KeyEvent arg0) {

	}

	public void keyTyped(KeyEvent arg0) {

	}

}
