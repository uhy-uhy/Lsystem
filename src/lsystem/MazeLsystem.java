package lsystem;

import java.awt.Point;

import maze_with_Lsystem.Maze;
import serachItem.Cell;
import serachItem.MazeNode;
import serachItem.SlidePuzzleNode;
import slidepuzzle_with_Lsystem.SlidePuzzle;

public class MazeLsystem extends Lsystem{

	public int max_node_count = 0;
	public int sight = 0;
	private double pa_rate = 3;
	private double pb_rate = 2;
	private double pc_rate = 1;

	private double pd;
	private double pa;
	private double pb;
	private double pc;
	private double P;
	
	public static int debug_state_0 = 0;
	public static int debug_state_1 = 0;
	public static int debug_state_2 = 0;
	public static int debug_state_3 = 0;
	public static int debug_state_d = 0;
	public static int debug_state_D = 0;
	public static int debug_dead_count = 0;
	public static int debug_rule_2 = 0;
	public static int debug_rule_3 = 0;
	
	public MazeLsystem(int max_node_count,int sight){
		this.sight = sight;
		this.max_node_count = max_node_count;
	}

	@Override
	public void apply(Cell node) {
		// TODO 自動生成されたメソッド・スタブ
		MazeNode mNode = (MazeNode) node;
		if(checkFinish(mNode)){
			finish(mNode);
			return;
		}
		
		//double node_rate = (double)Maze.getNodeCount()/(double)max_node_count ;
		double node_rate = Maze.getArroundSearchResultRate(mNode.getPoint().x, mNode.getPoint().y, sight);
		if(node_rate < 0.3333){
			pa_rate = 1;
			pb_rate = 2;
			pc_rate = 3;
		}
		else if(node_rate < 0.666){
			pa_rate = 2;
			pb_rate = 2;
			pc_rate = 2;
		}
		else{
			pa_rate = 3;
			pb_rate = 2;
			pc_rate = 1;
		}
		double sig = sigmoid((double)Maze.getNodeCount()/(double)max_node_count , 4);
		P = 1 - sig;
		pd = sig;
		pa = P*pa_rate/(pa_rate+pb_rate+pc_rate);
		pb = P*pb_rate/(pa_rate+pb_rate+pc_rate);
		pc = P*pc_rate/(pa_rate+pb_rate+pc_rate);

		String state = mNode.getState();
		switch (state) {
		case "0":
			rule_0(mNode);
			debug_state_0++;
			break;
		case "1":
			rule_1(mNode);
			debug_state_1++;
			break;
		case "2":
			rule_2(mNode);
			debug_state_2++;
			break;
		case "3":
			rule_3(mNode);
			debug_state_3++;
			break;
		case "E":
			rule_E(mNode);
			break;
		case "D":
			rule_D(mNode);
			debug_state_D++;
			break;
		case "d":
			rule_d(mNode);
			debug_state_d++;
			break;
		default:
			break;
		}
	}

	@Override
	public boolean checkFinish(Cell node) {
		// TODO 自動生成されたメソッド・スタブ

		return false;
	}

	public void finish(MazeNode node) {
		node.setState("F");
	}
	
	public static void reset_debug_num(){
		debug_state_0 = 0;
		debug_state_1 = 0;
		debug_state_2 = 0;
		debug_state_3 = 0;
		debug_state_d = 0;
		debug_state_D = 0;
		debug_dead_count = 0;
		debug_rule_2 = 0;
		debug_rule_3 = 0;
	}
	
	/**
	 * 0 →　3 ; 1
	 * 0 →　d
	 * @param node
	 */
	private void rule_0(MazeNode node){
		double random = Math.random();
		if(random > pd){
			node.setState("3");
			String direction = changeDirection(node, "TOP");
			Point next_point = changePosition(node, direction);
			if(next_point.x >= 0 && next_point.y >= 0 && next_point.x < Maze.width && next_point.y < Maze.height){
				MazeNode node2 = new MazeNode("1", direction, node, next_point);
				node.addChild(node2);
			}
		}
		else{
			node.setState("d");
		}
	}
	/**
	 * 1 →　2
	 * 1 →　3 ; 1
	 * @param node
	 */
	private void rule_1(MazeNode node){
		double random = Math.random();
		if(random > (pa/P)){
			node.setState("2");
		}
		else{
			node.setState("3");
			String direction = changeDirection(node, "TOP");
			Point next_point = changePosition(node, direction);
			if(next_point.x >= 0 && next_point.y >= 0 && next_point.x < Maze.width && next_point.y < Maze.height){
				MazeNode node2 = new MazeNode("1", direction, node, next_point);
				node.addChild(node2);
			}
		}
	}
	/**
	 * 2 →　3 [0 ]0 ;0
	 * 2 →　3 [0 ]0
	 * @param node
	 */
	private void rule_2(MazeNode node) {
		double random = Math.random();
		//子を３つ生成
		if(random > (pb/(pb+pc))){
			debug_rule_3++;
			node.setState("3");
			Point[] next_point = new Point[3];
			String[] direction = new String[3];
			direction[0] = changeDirection(node, "TOP");
			next_point[0] = changePosition(node, direction[0]);
			direction[1] = changeDirection(node, "LEFT");
			next_point[1] = changePosition(node, direction[1]);
			direction[2] = changeDirection(node, "RIGHT");
			next_point[2] = changePosition(node, direction[2]);
			for(int i = 0;i < next_point.length;i++){
				if(next_point[i].x >= 0 && next_point[i].y >= 0 && next_point[i].x < Maze.width && next_point[i].y < Maze.height){
					MazeNode node2;
					if(i == 0)
						node2 = new MazeNode("1", direction[i], node, next_point[i]);
					else
						node2 = new MazeNode("1", direction[i], node, next_point[i]);
					node.addChild(node2);
				}
			}
		}
		//子を２つ生成
		else{
			debug_rule_2++;
			node.setState("3");
			Point[] next_point = new Point[2];
			String[] direction = new String[2];
			direction[0] = changeDirection(node, "LEFT");
			next_point[0] = changePosition(node, direction[0]);
			direction[1] = changeDirection(node, "RIGHT");
			next_point[1] = changePosition(node, direction[1]);
			for(int i = 0;i < next_point.length;i++){
				if(next_point[i].x >= 0 && next_point[i].y >= 0 && next_point[i].x < Maze.width && next_point[i].y < Maze.height){
					MazeNode node2 = new MazeNode("0", direction[i], node, next_point[i]);
					node.addChild(node2);
				}
			}
		}
	}
	/**
	 * 3$ →　d
	 * @param node
	 */
	private void rule_3(MazeNode node){
		//double ph = pd;
		//double ph = sigmoid(Maze.getArroundSearchResultRate(node.getPoint().x, node.getPoint().x, 2), 4);
		double ph = Maze.getArroundSearchResultRate(node.getPoint().x, node.getPoint().y, sight);
		double random = Math.random();
		if(random > ph){
			//node.setState("3");
		}
		else{
			node.setState("d");
		}
	}
	/**
	 * 終了状態
	 * @param node
	 */
	private void rule_E(MazeNode node){	
		if(node.getChildrenByArrayList().size() == 0)
			node.setState("d");	
	}
	/**
	 * 死滅ルール
	 * .D$ →　d
	 * @param node
	 */
	private void rule_D(MazeNode node){	
		//子ノードが存在しない場合のみ
		if(node.getChildrenByArrayList().size() == 0){
			MazeNode parent = node.getParent();
			if(parent != null){
				if(node.getChildrenByArrayList().size() == 0){
					parent.setState("d");
					node.dead();
					debug_dead_count++;
				}	
			}
		}
	}
	/**
	 * 成長と死滅の中間状態
	 * 問題に合わせた評価関数で次の状態を決めたい
	 * d →　D
	 * d →　0
	 * @param node
	 */
	private void rule_d(MazeNode node){	
		//double ph = pd;
		//double ph = sigmoid(Maze.getArroundSearchResultRate(node.getPoint().x, node.getPoint().x, 2), 4);
		double ph = Maze.getArroundSearchResultRate(node.getPoint().x, node.getPoint().y, sight);
		double random = Math.random();
		if(random > ph){
			node.setState("0");
		}
		else{
			node.setState("D");
		}
	}

	public String changeDirection(MazeNode node,String change_type){
		String direction = "";
		switch (node.getDirection()) {
		case "8":
			if(change_type.equals("TOP")) direction = "8";
			else if(change_type.equals("RIGHT")) direction = "6";
			else if(change_type.equals("LEFT")) direction = "4";
			else if(change_type.equals("BOTOM")) direction = "2";
			break;
		case "6":
			if(change_type.equals("TOP")) direction = "6";
			else if(change_type.equals("RIGHT")) direction = "2";
			else if(change_type.equals("LEFT")) direction = "8";
			else if(change_type.equals("BOTOM")) direction = "4";
			break;
		case "4":
			if(change_type.equals("TOP")) direction = "4";
			else if(change_type.equals("RIGHT")) direction = "8";
			else if(change_type.equals("LEFT")) direction = "2";
			else if(change_type.equals("BOTOM")) direction = "6";
			break;
		case "2":
			if(change_type.equals("TOP")) direction = "2";
			else if(change_type.equals("RIGHT")) direction = "4";
			else if(change_type.equals("LEFT")) direction = "6";
			else if(change_type.equals("BOTOM")) direction = "8";
			break;
		default:
			System.err.println("direction error");
			break;
		}
		return direction;
	}

	public Point changePosition(MazeNode node, String direction){
		Point next_point;
		int x = node.getPoint().x;
		int y = node.getPoint().y;
		switch (direction) {
		case "2":
			next_point = new Point(x, y + 1);
			break;
		case "4":
			next_point = new Point(x - 1, y);
			break;
		case "6":
			next_point = new Point(x + 1, y);
			break;
		case "8":
			next_point = new Point(x, y - 1);
			break;
		default:
			next_point = new Point(x , y);
			System.err.println( "想定外の方向を指定しています\n"  );
			break;
		}

		return next_point;
	}

	//xが0~1のシグモイド関数
	double sigmoid(double x, double gain)
	{
		return 1.0 / (1.0 + Math.exp(-gain * (x*2-1)));
	}

}
