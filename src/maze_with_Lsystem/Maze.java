package maze_with_Lsystem;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import serachItem.MazeNode;

public class Maze {
	public static int width;
	public static int height;
	public static ArrayList<Point> goal = new ArrayList<Point>();
	private static MazeNode[][] MAP;
	private static boolean[][] searchMAP;
	private static boolean[][] wallMAP;
	private static boolean wall_setting = false;
	private static int searchCount = 0;
	private static int node_count = 0;
	private static int wall_count = 0;

	public Maze(int width,int height,Point goal){
		Maze.width = width;
		Maze.height = height;
		Maze.goal.add(goal);

		MAP = new MazeNode[width][height];
		searchMAP = new boolean[width][height];
	}
	public Maze(String filename){
		File f = new File(filename);
		BufferedImage read;
		try {
			read = ImageIO.read(f);
			int w = read.getWidth();
			int h = read.getHeight();
			Maze.width = w;
			Maze.height = h;

			MAP = new MazeNode[width][height];
			searchMAP = new boolean[width][height];
			wallMAP = new boolean[width][height];
			wall_setting = true;

			for(int y = 0;y < h;y++){
				for(int x = 0;x < w;x++){
					int c = read.getRGB(x, y);
					int r = 255-r(c);
					int g = 255-g(c);
					int b = 255-b(c);
					if(r>=255 && g>=255 && b>=255){
						wallMAP[x][y] = true;
						wall_count++;
					}
				}
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}        

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
	/**
	 * 現在のノード数を返す
	 * @return
	 */
	public static int getNodeCount(){
		return node_count;
	}
	public static boolean getSearchResult(int x,int y){
		return searchMAP[x][y];
	}
	public static boolean getWallPoint(int x,int y){
		if(wall_setting){
			return wallMAP[x][y];
		}
		else
			return false;
	}
	public static int getSearchCount(){
		return searchCount;
	}
	public static boolean getWallSetting(){
		return wall_setting;
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
	public static int getTotalCell(){
		return width*height-wall_count;
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
				if(j != y && i != x){
					total++;
					if(searchMAP[i][j] == true){
						count++;
					}
				}
			}
		}
		if(total == 0)
			return 0;
		else
			return (double)count/(double)total;
	}
	public static int a(int c){
		return c>>>24;
	}
	public static int r(int c){
		return c>>16&0xff;
	}
	public static int g(int c){
		return c>>8&0xff;
	}
	public static int b(int c){
		return c&0xff;
	}
	public static int rgb(int r,int g,int b){
		return 0xff000000 | r <<16 | g <<8 | b;
	}
	public static int argb(int a,int r,int g,int b){
		return a<<24 | r <<16 | g <<8 | b;
	}

	public static void main(String args[]){
		Maze maze = new Maze("./resources/Maze1.png");
		//System.out.println(maze.searchMAP[5][0]);
	}
}
