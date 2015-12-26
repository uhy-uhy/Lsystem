package maze_with_Lsystem;

import java.awt.Point;
import java.util.ArrayList;

import serachItem.MazeNode;

public class Maze {
	public static int width;
	public static int height;
	public ArrayList<Point> goal = new ArrayList<Point>();
	private static MazeNode[][] MAP;
	private static boolean[][] searchMAP;
	private static int searchCount = 0;
	private static int node_count = 0;
	public Maze(int width,int height,Point goal){
		Maze.width = width;
		Maze.height = height;
		this.goal.add(goal);
		
		MAP = new MazeNode[width][height];
		searchMAP = new boolean[width][height];
	}
	public Maze(String filename){
	}
	
	public static void setNode(int x,int y,MazeNode node){
		if(node != null){
			node_count++;
			MAP[x][y] = node;
			if(searchMAP[x][y] == false){
				searchCount++;
			}
			searchMAP[x][y] = true;
		}
		else{
			node_count--;
			MAP[x][y] = node;
		}
	}
	public static MazeNode getNode(int x,int y){
		return MAP[x][y];
	}
	public static int getNodeCount(){
		return node_count;
	}
	public static boolean getSearchResult(int x,int y){
		return searchMAP[x][y];
	}
	public static int getSearchCount(){
		return searchCount;
	}
	public static int getSearchMAPTrue(){
		int cnt = 0;
		for(int y = 0;y < height;y++){
			for(int x = 0;x < width;x++){
				if(searchMAP[x][y] == true)
					cnt++;
			}
		}
		return cnt;
	}
	/**
	 * 周囲の探索済みセルの割合を取得する
	 * @param x
	 * @param y
	 * @param radius
	 * @return
	 */
	public static double getArroundSearchResultRate(int x,int y,int radius){
		int total = 0;
		int count = 0;
		int x_min = x - radius;
		while(x_min < 0){
			x_min++;
		}
		int x_max = x + radius;
		while(x_max >= width){
			x_max--;
		}
		int y_min = y - radius;
		while(y_min < 0){
			y_min++;
		}
		int y_max = y + radius;
		while(y_max >= height){
			y_max--;
		}
		for(int j = y_min; j <= y_max; j++){
			for(int i = x_min; i <= x_max; i++){
				total++;
				if(searchMAP[i][j] == true){
					count++;
				}
			}
		}
		return (double)count/(double)total;
	}
	public static void main(String args[]){
		Maze maze = new Maze(10,10,new Point(3,3));
		System.out.println(maze.searchMAP[5][0]);
	}
}
