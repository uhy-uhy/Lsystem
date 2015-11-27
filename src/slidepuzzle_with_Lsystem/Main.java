package slidepuzzle_with_Lsystem;

import java.util.ArrayDeque;
import java.util.ArrayList;

import processing.core.PApplet;
import lsystem.Lsystem;
import lsystem.SlidePuzzleLsystem;
import serachItem.SlidePuzzleNode;


public class Main {

	private ArrayList<SlidePuzzleNode> rootNode;
	private Lsystem lsys;
	private ArrayDeque<SlidePuzzleNode> nodes;
	private ArrayDeque<SlidePuzzleNode> postNodes;
	private ArrayDeque<SlidePuzzleNode> deleteNodes;
	private SlidePuzzleNode goalNode = null;
	private boolean roop = true;;

	public Main(SlidePuzzleNode startNode,String goalState,int max_node_count){
		lsys = new SlidePuzzleLsystem(max_node_count);
		rootNode = new ArrayList<SlidePuzzleNode>();
		rootNode.add(startNode);

		nodes = new ArrayDeque<SlidePuzzleNode>();
		postNodes = new ArrayDeque<SlidePuzzleNode>();
		deleteNodes = new ArrayDeque<SlidePuzzleNode>();

		SlidePuzzle.MAP.put(startNode.getBoardState(), true);
	}

	public void run(){
		int step_num = 0;
		while(roop){
			for(SlidePuzzleNode node : rootNode)
				nodes.add(node);
			step_num++;
			preStep();
			step();
			postStep();
			deleteStep();
			drawStep();
		}
		if(goalNode != null){
			finalization();
		}
		else{
			System.out.println("探索終了");
			System.out.println("step = "+step_num);
			System.out.println("map size = " + SlidePuzzle.MAP.size());

		}
	}

	public void preStep(){

	}
	public void step(){

		while(!nodes.isEmpty()){
			SlidePuzzleNode node = nodes.poll();
			lsys.apply(node);
			postNodes.add(node);
			for(SlidePuzzleNode child : node.getChildrenByArrayList()){
				nodes.add(child);
			}
		}
		System.out.println("node size ="+postNodes.size());
		System.out.println("map size ="+SlidePuzzle.MAP.size());
	}
	public void postStep(){
		if(postNodes.isEmpty())
			roop = false;
		while(!postNodes.isEmpty()){
			SlidePuzzleNode node = postNodes.poll();
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
			SlidePuzzleNode node = deleteNodes.poll();
			node.delete();
		}
	}
	public void drawStep(){
		SlidePuzzle.drawFlag = true;
	}
	public void stop(){
		roop = false;
	}
	public void start(){
		roop = true;
	}
	//終了処理
	public void finalization(){
		ArrayDeque<SlidePuzzleNode> reverse = new ArrayDeque<SlidePuzzleNode>();
		reverse.add(goalNode);
		SlidePuzzleNode node = goalNode;
		int count = 0;
		while(true){
			count++;
			System.out.println(node.getBoardState());
			if(node.getParent() != null)
				node = node.getParent();
			else 
				break;
		}
		System.out.println(count);
	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ		
		SlidePuzzle sp = new SlidePuzzle(3, 3, "123456780");
		SlidePuzzleNode node = new SlidePuzzleNode("0", "132406857", "2", null);
		Main main = new Main(node,"123456780",10000);
		
		SlidePuzzle.drawFlag = true;
		PApplet.main(new String[] { "--location=100,100","draw.MySketch"});
		
		Lsystem lsystem = new SlidePuzzleLsystem(10000);
		lsystem.apply(node);
		node.update();
		System.out.println("node1 ="+node.getPoint());
		for(SlidePuzzleNode sNode : node.getChildrenByArrayList()){
			System.out.print(sNode.getPoint()+"  ");
		}
		System.out.println();

//		SlidePuzzle sp = new SlidePuzzle(4, 4);
//		SlidePuzzleNode node = new SlidePuzzleNode("0", "312456089c7bd1ef", "2", null);
//		Main main = new Main(node,"1234567891bcdef0");
//		main.run();

		}

}
