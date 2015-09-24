package core;
import java.awt.Color;
import java.util.ArrayList;


public class Map {
	public int LmapY;
	public int LmapX;

	public static MapStatus Lmap[][];
	public static ArrayList<MapStatus> feed_map = new ArrayList<MapStatus>(); //餌のセルだけをこのリストに入れる。
	public static ArrayList<MapStatus> wall_map = new ArrayList<MapStatus>();	//壁のセルだけをこのリストに入れる。

	public Map(){
		LmapX = SimulationStatus.Size_X;
		LmapY = SimulationStatus.Size_Y;
		Lmap = new MapStatus[LmapX][LmapY];
		for(int y = 0;y < LmapY;y++){
			for(int x = 0;x < LmapX;x++){
				Lmap[x][y] = new MapStatus(x,y,0,false);
			}
		}
	}

	public static void addWallMap(int x,int y){
		wall_map.add(Lmap[x][y]);
		Lmap[x][y].setWall();
		//Lmap[x][y].addPopulation();
	}
	public static void addFeedMap(int x,int y,Color c){
		feed_map.add(Lmap[x][y]);
		Lmap[x][y].setFeed(c);
	}
	public static void removeFeedMap(MapStatus ms){
		feed_map.remove(ms);
		ms.removeFeed();
	}
	public static boolean checkFeed(int x,int y){
		return (Lmap[x][y].feed);
	}
	
	public int arroundCells(int x,int y){
		int yr1 = y-1;
		int xr1 = x-1;
		int ya1 = y+1;
		int xa1 = x+1;
		if(yr1 < 0) yr1 = 0;if(ya1 >= LmapY) ya1 = LmapY-1;
		if(xr1 < 0) xr1 = 0;if(xa1 >= LmapX) xa1 = LmapX-1;

		return(Lmap[xr1][yr1].population + Lmap[xr1][y].population + Lmap[xr1][ya1].population
				+ Lmap[x][yr1].population + Lmap[x][ya1].population
				+ Lmap[xa1][yr1].population + Lmap[xa1][y].population + Lmap[xa1][ya1].population);
	}
}
