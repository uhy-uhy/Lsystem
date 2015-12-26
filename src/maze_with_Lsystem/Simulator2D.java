package maze_with_Lsystem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import processing.core.*;
import processing.event.MouseEvent;
import serachItem.MazeNode;
import serachItem.SlidePuzzleNode;


public class Simulator2D extends PApplet {

	ArrayList<MazeNode> drawArray = new ArrayList<MazeNode>();
	private static ArrayDeque<MazeNode> drawBuffer = new ArrayDeque<MazeNode>();
	private static boolean update_flag = false;

	public void settings() {
		size(400, 400);
		// プロットの色と大きさ

	}

	public void setup() {
	}

	public void draw() {

		background(0);  

		update();

		//四隅
		strokeWeight(10);
		point(0, width);
		point(width, 0);
		point(0, 0);
		point(width, width);
		
		//フェロモン（仮）
		stroke(255, 0, 0);
		strokeWeight(1);
		for(int x = 0;x < Maze.width; x++){
			for(int y = 0;y < Maze.height; y++){
				if(Maze.getSearchResult(x, y) == true){
					point(x , y);
				}
			}
		}
		
		//原点
		strokeWeight(1);
		stroke(255, 0, 0);
		point(0,0);

		stroke(0, 255, 0);

		for(MazeNode node : drawArray){
			point(node.getPoint().x , node.getPoint().y );
		}
		

	}

	public static void setBuffer(ArrayDeque<MazeNode> nodes){
		while(!nodes.isEmpty()){
			MazeNode node = nodes.poll();
			//System.out.println(node.getPoint());
			drawBuffer.add(node);
		}
		update_flag = true;
	}

	private void update(){
		if(update_flag){
			drawArray.clear();
			while(!drawBuffer.isEmpty()){
				drawArray.add(drawBuffer.poll());
			}
			update_flag = false;
		}
	}



	public static void main(String args[]) {
		PApplet.main(new String[] { "--location=100,100","maze_with_Lsystem.Simulator2D"});
	}
}