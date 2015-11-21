package gephi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import org.apache.bcel.generic.I2F;
import org.apache.bcel.generic.NEW;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
import org.gephi.preview.plugin.items.EdgeItem;
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
	private ProcessingTarget pTarget;
	private PApplet applet;

	JFrame frame;
	private Thread thread;
	private boolean start = false;

	private HashMap<String,Boolean> map = new HashMap<String,Boolean>();
	private Node startNode;
	private ArrayDeque<Node> nextNode  = new ArrayDeque<Node>();
	private ArrayDeque<Node> nowNode  = new ArrayDeque<Node>();

	public Main(String start_id){
		pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();

		init();

		//Create three nodes
		Node n0 = graphModel.factory().newNode(start_id);
		n0.getNodeData().setLabel(start_id);
		//n0.getNodeData().setSize(1);
		//n0.getNodeData().setColor(1.0f, 0.0f, 0.0f);

		graph.addNode(n0);

		startNode = graph.getNode(start_id);
		nowNode.add(startNode);
		map.put(startNode.getNodeData().getId(), true);

		pTarget = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		applet = pTarget.getApplet();
		applet.init();
		applet.addKeyListener(this);

		//Refresh the preview and reset the zoom
		pTarget.refresh();
		pTarget.resetZoom();

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

	public Main(String filename, String start_id){
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
		
		init();

		startNode = graph.getNode(start_id);
		startNode.getNodeData().setColor(1.0f,0.0f, 0.0f);
		startNode.getNodeData().setSize(40);
		nowNode.add(startNode);
		map.put(startNode.getNodeData().getId(), true);
		
		//エッジ無し
		graph.clearEdges();
		//無理やりエッジの太さを決める
//		for(Edge e:graph.getEdges()){
//			//EdgeItem item = new EdgeItem(e);
//			//item.setData(EdgeItem.COLOR, new Color(255, 0, 0, 100));
//			e.setWeight(0);
//			e.getEdgeData().setColor(1.0f, 1.0f, 1.0f);
//			e.getEdgeData().setAlpha(1.0f);
//		}

		System.out.println("プレビューコントローラー");

		pTarget = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		applet = pTarget.getApplet();
		applet.init();
		applet.addKeyListener(this);

		//Refresh the preview and reset the zoom
		pTarget.refresh();
		pTarget.resetZoom();

		pTarget.zoomPlus();
		pTarget.zoomPlus();
		pTarget.zoomPlus();
		
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
	
	public void init(){
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		rankingController = Lookup.getDefault().lookup(RankingController.class);

		graph = graphModel.getDirectedGraph();
		
		
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		//model.getProperties().putValue(PreviewProperty.ARROW_SIZE, Boolean.TRUE);
		//model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(1f));
		//model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
		model.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		//model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 1f);
		model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		//↓これめっちゃ大事
		model.getProperties().putValue(PreviewProperty.EDGE_COLOR,new EdgeColor(EdgeColor.Mode.ORIGINAL) );
		previewController.refreshPreview();
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
		sizeTransformer.setMinSize(6);
		sizeTransformer.setMaxSize(30);
		rankingController.transform(centralityRanking,sizeTransformer);

		centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		labelSizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
		labelSizeTransformer.setMinSize(1);
		labelSizeTransformer.setMaxSize(3);
		rankingController.transform(centralityRanking2,labelSizeTransformer);
	}
	
	//レイアウト
	public void YifanHuLayout(){
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		layout.setOptimalDistance(200f);

		layout.initAlgo();
		for (int i = 0; i < 800 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
	}

	public void generate_sample(){
		String num = String.valueOf(graph.getNodeCount()+1);
		Node n = graphModel.factory().newNode(num);
		n.getNodeData().setLabel(num);
		n.getNodeData().setColor(1.0f, 0.0f, 0.0f);;
		graph.addNode(n);

		if(graph.getNodeCount() > 1){
			Random rnd = new Random();
			String ran =  String.valueOf(rnd.nextInt(graph.getNodeCount()) + 1);
			while(ran.equals(num)){
				ran =  String.valueOf(rnd.nextInt(graph.getNodeCount()) + 1);
			}
			Node target = graph.getNode(ran);

			Edge e = graphModel.factory().newEdge(n, target, 2f, true);
			//e.getEdgeData().setColor(1.0f, 0.0f, 0.0f);
			graph.addEdge(e);
		}
	}



	public void run() {
		boolean roop = true;
		System.out.println("start");
		while(roop)
		{
			if(start){
				dynamic_sample();
				//test_search();

				//roop = test_edgeGenerating_search();
				
				previewController.refreshPreview();
				pTarget.refresh();
				System.out.println("表示");
				//start = false;
			}
			try {
				Thread.sleep(200);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		System.out.println(graph.getEdges(startNode).toArray().length);


		
		ranking();
		startNode.getNodeData().setSize(40);

		reverse();
		
		//YifanHuLayout();
		
		previewController.refreshPreview();
		pTarget.refresh();
		System.out.println("finish ranking");

		
		output_gexf();
	}
	
	//ゴールのノードからリバース
	public void reverse(){
		Node goal = graph.getNode("12345678");
		ArrayDeque<Node> nextNode = new ArrayDeque<Node>();
		nextNode.add(goal);
		
		while(!nextNode.isEmpty()){
			Node node = nextNode.poll();
			node.getNodeData().setSize(30);
			node.getNodeData().setColor(0.0f, 1.0f, 0.0f);
			Edge[] edges = graph.getEdges(node).toArray();
			for(Edge edge : edges){
				if(edge.getTarget() == node){
					nextNode.add(edge.getSource());
					edge.setWeight(20.0f);
				}
				else{
				}
			}
			System.out.println(node.getNodeData().getLabel());
		}
	}

	public void dynamic_sample(){
		generate_sample();
		ranking();
		YifanHuLayout();
		while(!nowNode.isEmpty()){
			Node node = nowNode.poll();
			map.put(node.getNodeData().getId(), true);
			for(Edge edge:graph.getEdges(node)){
				if(edge.getSource() == node){
					nextNode.add(edge.getTarget());
				}
			}
		}
		while(!nextNode.isEmpty()){
			Node node = nextNode.poll();
			nowNode.add(node);
		}
	}

	public void test_search(){
		while(!nowNode.isEmpty()){
			Node node = nowNode.poll();
			map.put(node.getNodeData().getId(), true);
			for(Edge edge:graph.getEdges(node)){
				//edge.getEdgeData().setColor(1.0f,0.0f, 0.0f);
				//edge.setWeight(5.0f);
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
				node.getNodeData().setSize(10);
			nowNode.add(node);
		}

	}
	
	//エッジ無しの状態からスタート
	//既に読み込んであるノードの中から取得
	public boolean test_edgeGenerating_search(){
		while(!nowNode.isEmpty()){
			Node node = nowNode.poll();
			//ノードにエッジ装着
			testLsys(node);
			EdgeIterable edgeIterable = graph.getEdges(node);
			Edge[] edges = edgeIterable.toArray();
			//for(Edge edge:graph.getEdges(node)){		←この書き方だとlockされていてremove,clearできない
			for(Edge edge : edges){
				Node target  = edge.getTarget();
				if(!map.containsKey(target.getNodeData().getId())){
					map.put(target.getNodeData().getId(), true);
					nextNode.add(target);
				}
			}
		}
		System.out.println("next node = " + nextNode.size());
		//終了条件
		if(nextNode.size() == 0)
			return false;
		while(!nextNode.isEmpty()){
			Node node = nextNode.poll();
			node.getNodeData().setColor(1.0f,0.0f, 0.0f);
			if(node.getNodeData().getSize() < 20f)
				node.getNodeData().setSize(10);
			nowNode.add(node);
		}
		
		return true;
	}
	
	public String[] commands = new String[]{"LS1","LS2","LS3","LS4","LR1","LR2","LR3","LR4"};
	public void testLsys(Node node){
		//全状態生成
		for(String command : commands){
			String newLabel = deform(node.getNodeData().getLabel(), command);
			//既にキーが入っている場合はスキップ
			if(!map.containsKey(newLabel)){
				Node target = graph.getNode(newLabel);
				Edge newEdge = graphModel.factory().newEdge(node, target);
				if(command.substring(0, 2).equals("LS"))
					newEdge.getEdgeData().setColor(1.0f,0.0f, 0.0f);
				else if(command.substring(0, 2).equals("LR")){
					newEdge.getEdgeData().setColor(0.0f, 0.0f, 1.0f);
				}
				else
					newEdge.getEdgeData().setColor(0.0f, 1.0f, 0.0f);

				newEdge.setWeight(4.0f);
				newEdge.getEdgeData().setAlpha(0.3f);
				graph.addEdge(newEdge);
			}
		}
	}
	
	//変形用関数
	public String deform(String label,String command){
		String tmp = "";
		String result = "";
		switch (command) {
		case "LR1":
			tmp = label.substring(0, 2) + label.substring(label.length() - 1, label.length());
			result = tmp.substring(1,2) + tmp.substring(2,3) + label.substring(2,label.length() - 1) + tmp.substring(0,1);
			//System.out.println(result);
			break;
		case "LR2":
			tmp = label.substring(1, 4);
			result = label.substring(0,1) + tmp.substring(1 ,2) + tmp.substring(2 ,3) + tmp.substring(0 ,1) +label.substring(4 ,label.length()); 
			//System.out.println(result);
			break;
		case "LR3":
			tmp = label.substring(3, 6);
			result = label.substring(0,3) + tmp.substring(1 ,2) + tmp.substring(2 ,3) + tmp.substring(0 ,1) +label.substring(6 ,label.length());  
			break;
		case "LR4":
			tmp = label.substring(5, 8);
			result = label.substring(0,5) + tmp.substring(1 ,2) + tmp.substring(2 ,3) + tmp.substring(0 ,1); 
			break;
		case "LS1":
			tmp = label.substring(0, 3);
			result = tmp.substring(1,2) + tmp.substring(2, 3) + tmp.substring(0, 1) + label.substring(3,label.length());
			break;
		case "LS2":
			tmp = label.substring(2, 5);
			result = label.substring(0, 2) + tmp.substring(1,2) + tmp.substring(2, 3) + tmp.substring(0, 1) + label.substring(5,label.length());
			break;
		case "LS3":
			tmp = label.substring(4, 7);
			result = label.substring(0, 4) + tmp.substring(1,2) + tmp.substring(2, 3) + tmp.substring(0, 1) + label.substring(label.length() - 1,label.length());
			break;
		case "LS4":
			tmp = label.substring(0, 1) + label.substring(6, 8);
			result = tmp.substring(1, 2) + label.substring(1, 6) + tmp.substring(2, 3) + tmp.substring(0, 1);
			break;
		default:
			System.out.println("No command!!!!");
			break;

		}

		return result;
	}

	
	public void output_gexf(){
        //Export full graph
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("output_gexf.gexf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		Main main = new Main("1");
		//Main main = new Main("/YifanHu_3x3_86543217_searched.gexf","86543217");
		//Main main = new Main("/YifanHu_800step_3x3_86543217_searched.gexf");
		
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
