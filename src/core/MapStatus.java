package core;
import java.awt.Color;
import java.util.ArrayList;


public class MapStatus {

	public int x;
	public int y;
	public int population = 0;
	public boolean feed;	//餌
	public boolean wall; //壁
	public Color c = Color.white;
	public ArrayList<CellState> cells = new ArrayList<CellState>(); //同じ色のセルは入らない（ここに入るセルはすべて違う色）

	public MapStatus(int x,int y,int population,boolean feed){
		this.x = x;
		this.y = y;
		this.population = population;
		this.feed = feed;
		this.wall = false;
	}

	public void setFeed(Color c){
		feed = true;
		this.c = c;
	}
	public void removeFeed(){
		feed = false;
	}
	public void setWall(){
		wall = true;
	}
	public void removeWall(){
		wall = false;
	}
	public void addCell(CellState c){
		cells.add(c);
	}
	public void removeCell(CellState c){
		cells.remove(c);
	}
	public void addPopulation(){
		population += 1;
	}
	public void reducePopulation(){
		population -= 1;
		if(population < 0) population = 0;
	}
	public void addPopulation(int n){
		population += n;
	}
	public void reducePopulation(int n){
		population -= n;
		if(population < 0) population = 0;
	}
}
