package maze_with_Lsystem;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;


import processing.core.PApplet;
import serachItem.MazeNode;
import lsystem.Lsystem;
import lsystem.MazeLsystem;



public class Main {

	private ArrayList<MazeNode> rootNode;
	private Lsystem lsys;
	private ArrayDeque<MazeNode> nodes;
	private ArrayDeque<MazeNode> postNodes;
	private ArrayDeque<MazeNode> deleteNodes;
	private ArrayDeque<MazeNode> drawQueue;
	private MazeNode goalNode = null;
	private boolean roop = true;
	private int max_node_count;
	private int sight;

	public Main(MazeNode startNode,int max_node_count,int sight){
		this.max_node_count = max_node_count;
		this.sight = sight;
		lsys = new MazeLsystem(max_node_count,sight);
		rootNode = new ArrayList<MazeNode>();
		rootNode.add(startNode);

		nodes = new ArrayDeque<MazeNode>();
		postNodes = new ArrayDeque<MazeNode>();
		deleteNodes = new ArrayDeque<MazeNode>();

		drawQueue = new ArrayDeque<MazeNode>();

		Maze.setNode(startNode.getPoint().x, startNode.getPoint().y, startNode);

	}

	public void run(){
		int step_num = 0;

		try {
			// 出力ストリームの生成
			FileWriter outFile = new FileWriter("log.csv");
			BufferedWriter outBuffer = new BufferedWriter(outFile);
			String data = "";
			
			outBuffer.write("step"+","+"node"+","+"sig"+","+"state_0"
					+","+"state_1"+","+"state_2"+","+"state_3"+","+"state_d"
					+","+"tate_D"+"\n");
			data = max_node_count+","+sight+"\n";
			outBuffer.write(data);
			while(roop){

				System.out.println(step_num+"ステップ開始");
				for(MazeNode node : rootNode)
					nodes.add(node);
				preStep();
				step();
				postStep();
				deleteStep();
				drawStep2D();
				System.out.println(step_num+"ステップ完了");
				step_num++;

				if(step_num >= 5000)
					roop = false;
				try{
					double sig = sigmoid((double)Maze.getNodeCount()/10000.0 , 4);
					//				System.out.println("sig = "+sig);
					//				System.out.println(
					//						"state_0 = "+MazeLsystem.debug_stste_0+"\n"
					//						+"state_1 = "+MazeLsystem.debug_stste_1+"\n"
					//						+"state_2 = "+MazeLsystem.debug_stste_2+"\n"
					//						+"state_3 = "+MazeLsystem.debug_stste_3+"\n"
					//						+"state_d = "+MazeLsystem.debug_stste_d+"\n"
					//						+"state_D = "+MazeLsystem.debug_stste_D+"\n"
					//						+"DEAD_count = "+MazeLsystem.debug_dead_count+"\n"
					//						+"rule_2 = "+MazeLsystem.debug_rule_2+"\n"
					//						+"rule_3 = "+MazeLsystem.debug_rule_3+"\n");
					//				MazeLsystem.reset_debug_num();
//					data = step_num+","+Maze.getNodeCount()+","+sig+","+MazeLsystem.debug_state_0
//							+","+MazeLsystem.debug_state_1+","+MazeLsystem.debug_state_2+","+MazeLsystem.debug_state_3+","+MazeLsystem.debug_state_d
//							+","+MazeLsystem.debug_state_D+"\n";
					data = step_num+","+Maze.getNodeCount()+","+sig+","+MazeLsystem.debug_state_0+","+
							+Maze.getSearchMAPTrue()+","+Maze.width*Maze.height+"\n";
					outBuffer.write(data);
					MazeLsystem.reset_debug_num();
					Thread.sleep(50);
				}
				catch(InterruptedException e){}
			}
			outBuffer.flush();
			outBuffer.close();

		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("ループ抜け");

		if(goalNode != null){
			finalization();
		}
		else{
			System.out.println("探索終了");
			System.out.println("step = "+step_num);
		}
	}

	public void preStep(){

	}
	public void step(){

		while(!nodes.isEmpty()){
			MazeNode node = nodes.poll();
			lsys.apply(node);
			postNodes.add(node);
			drawQueue.add(node);
			for(MazeNode child : node.getChildrenByArrayList()){
				nodes.add(child);
			}
		}
		System.out.println("node size ="+postNodes.size());
	}
	public void postStep(){
		if(postNodes.isEmpty())
			roop = false;
		while(!postNodes.isEmpty()){
			MazeNode node = postNodes.poll();
			if(node.getState().equals("F")){
				goalNode = node;
				roop = false;
			}
			node.update();
			if(node.getDeadFlag())
				deleteNodes.add(node);
		}
	}
	public void deleteStep(){
		while(!deleteNodes.isEmpty()){
			MazeNode node = deleteNodes.poll();
			node.delete();
		}
	}
	public void drawStep2D(){
		Simulator2D.setBuffer(drawQueue);
	}
	public void drawStep3D(){
		MySketch3D.setBuffer(drawQueue);
	}
	public void stop(){
		roop = false;
	}
	public void start(){
		roop = true;
	}
	//終了処理
	public void finalization(){

	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ		
		MazeNode node = new MazeNode("0", "2", null, new Point(200,200));
		Maze sp = new Maze(400, 400, new Point(390,390));
		Main main = new Main(node,5000,20);

		PApplet.main(new String[] { "--location=100,100","maze_with_Lsystem.Simulator2D"});
		main.run();

	}

	//テスト用
	double sigmoid(double x, double gain)
	{
		return 1.0 / (1.0 + Math.exp(-gain * (x*2-1)));
	}
}
