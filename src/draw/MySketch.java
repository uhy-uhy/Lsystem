package draw;

import java.util.ArrayDeque;
import java.util.ArrayList;

import processing.core.*;
import processing.event.MouseEvent;
import serachItem.SlidePuzzleNode;
import slidepuzzle_with_Lsystem.SlidePuzzle;

public class MySketch extends PApplet {

	float camera_x = 0;
	float camera_y = -100;
	float camera_z = 500;
	float center_x = 0;
	float center_y = 0;
	float center_z = 0;
	float up_x = 0;
	float up_y = 1;
	float up_z = 0;

	float pre_mouse_x; 
	float pre_mouse_y; 
	
	ArrayList<SlidePuzzleNode> drawArray = new ArrayList<SlidePuzzleNode>();
	ArrayDeque<SlidePuzzleNode> drawBuffer = new ArrayDeque<SlidePuzzleNode>();
	boolean update_flag = false;

	public void settings() {
		size(600, 600, P3D);
		// プロットの色と大きさ

	}

	public void setup() {
		pre_mouse_x = mouseX;
		pre_mouse_y = mouseY;
	}

	public void draw() {
		if (mousePressed == true){
			if(mouseButton == RIGHT){
				camera_x += pre_mouse_x - (float)mouseX; 
				center_x += pre_mouse_x - (float)mouseX;
				camera_y += pre_mouse_y - (float)mouseY; 
				center_y += pre_mouse_y - (float)mouseY; 
			}
			else if(mouseButton == LEFT){
				center_x += pre_mouse_x - (float)mouseX;
				center_y += pre_mouse_y - (float)mouseY; 
			}
		}
		pre_mouse_x = mouseX;
		pre_mouse_y = mouseY;

		chechDrawFlag();
		if(update_flag)
			updateDrawArray();
		
		// カメラを設定  
		camera(camera_x, camera_y, camera_z, center_x, center_y, center_z, up_x, up_y, up_z);  
		background(0);  


		//グリッドの描画   
		stroke(255);  
		fill(0x00FFFFFF);
		strokeWeight(1);

		final int step = 20;    
		for(int i = -width; i < width; i += step) {  
			beginShape(QUAD_STRIP);  
			for(int j = -width; j <= width; j += step) {  
				vertex(i, 0, j);  
				vertex(i + step, 0, j);  
			}  
			endShape();  
		}  

		//四隅
		strokeWeight(30);
		point(-width, 0, width);
		point(width, 0, -width);
		point(-width, 0, -width);
		point(width, 0, width);
		//原点
		strokeWeight(10);
		stroke(255, 0, 0);
		point(0, 0, 0);

		stroke(0, 255, 0);
		point(0, -10, 0);
		point(0, -10, 10);
		point(30, -20, 0);
	}


	public void updateDrawArray(){
		drawArray.clear();
		while(!drawBuffer.isEmpty()){
			drawArray.add(drawBuffer.poll());
		}
		update_flag = false;
	}
	public void chechDrawFlag(){
		if(SlidePuzzle.drawFlag)
			update_flag = true;
	}
	
	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		camera_z += e*20;
	}

	//	public static void main(String args[]) {
	//		PApplet.main(new String[] { "--location=100,100","testProcessing.MySketch"});
	//	}
}