package serachItem;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import maze_with_Lsystem.Maze;

public class MazeNode extends Cell{
	//親セル
	private MazeNode parent;
	//子セル
	private ArrayList<MazeNode> children;
	//
	private MazeNode next_parent;
	//
	private ArrayList<MazeNode> next_children;

	private ArrayList<String> next_state_list;

	private Point point;

	private String direction = "";

	private int step = 0;

	public boolean wall = false;
	/**
	 * @param state
	 * @param board
	 * @param direction
	 * @param parent parentがnullの場合はルートノード
	 * 
	 */
	public MazeNode(String state,String direction,MazeNode parent,Point point){
		super();
		this.state = state;
		children = new ArrayList<MazeNode>();
		next_parent = parent;
		next_children = new ArrayList<MazeNode>();
		next_state_list = new ArrayList<String>();
		this.direction = direction;
		this.parent = parent;
		this.point = point;
	}

	public void setDirection(String direction){
		this.direction = direction;
	}
	public String getDirection(){
		return direction;
	}
	public MazeNode getParent(){
		return parent;
	}
	public Iterator<MazeNode> getChildren(){
		return children.iterator();
	}
	public ArrayList<MazeNode> getChildrenByArrayList(){
		return children;
	}

	public void addChild(MazeNode node){
		if(node != null) next_children.add(node);
		else{
			System.out.println("childにnull追加");
		}
	}
	public void setParent(MazeNode parent){
		this.next_parent = parent;
	}
	public void setPoint(Point p){
		this.point = p;
	}
	public void setPoint(int x,int y){
		this.point = new Point(x ,y);
	}
	public Point getPoint(){
		return point;
	}
	public void dead(){
		dead = true;
	}
	public boolean getDeadFlag(){
		return dead;
	}
	public int getStep(){
		return step;
	}
	public void delete(){
		if(dead){
			if(parent != null)
				parent.children.remove(this);
			for(MazeNode child : children){
				child.parent = null;
			}
			//TODO 怪しい
			Maze.setNode(point.x, point.y, null);
			parent = null;
			children.clear();
			next_children.clear();
			next_parent = null;
			next_state_list.clear();
		}
	}

	//TODO 親の処理まだ甘い.(root化の部分)
	@Override
	public void update(){
		parent = next_parent;
		if(next_state == null) System.out.println("nextstate指定されていない");
		state = next_state;
		for(MazeNode node : next_children){
			Point cPoint = node.point;
			if(Maze.getNode(cPoint.x, cPoint.y) == null){
				children.add(node);
				Maze.setNode(cPoint.x, cPoint.y, node);
			}

		}
		next_children.clear();
		next_state_list.clear();
	}

	@Override
	public void setState(String state) {
		next_state_list.add(state);
		next_state = state;
		//			if(next_state_list.size() > 1)
		//				System.out.println("next_stateが2個以上");

		//			if(next_state_list.size() > 1){
		//				if(!next_state_list.get(0).equals(next_state_list.get(1)))
		//				System.out.println("next_stateが2個以上");
		//			}
	}
}
