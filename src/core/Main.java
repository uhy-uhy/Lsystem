package core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {

	public static void main(String[] args) {

		JFrame frame = new JFrame();

		frame.setTitle("Animation");
		frame.setBounds(200, 200, 100, 80);
		frame.setSize(800, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Map map = new Map();

		//20,35
		//320,235
		int startX = 320;
		int startY = 235;
		//描画間隔
		int wait_time = 0;
		//MAP上に何個までセルの重なりを許すか(別色のセルの重なりを許すかどうかで同色は許さない)
		int overlap = 1;

		int wallNum[][] = new int[SimulationStatus.Size_X][SimulationStatus.Size_Y];

		if(SimulationStatus.Wall == true){
			try{
				// ファイルの読み込み
				FileReader fr = new FileReader(SimulationStatus.WallName);
				BufferedReader br = new BufferedReader(fr);
				String line;
				String[] lineAry;
				int j = 0;
				while ((line = br.readLine()) != null) {
					//区切り文字","で分割する
					lineAry = line.split(",");
					for(int i = 0;i < lineAry.length;i++){
						wallNum[j][i] = Integer.parseInt(lineAry[i]);
						//wallNum[j][i] = 0;
					}
					j++;
				}
			}catch(IOException ex){

			}
		}

		MyPanel panel = new MyPanel(wallNum,SimulationStatus.Wall,SimulationStatus.Size_X,SimulationStatus.Size_Y,SimulationStatus.Nutrition,
				SimulationStatus.All_Nutrition,startX,startY,wait_time);
		frame.getContentPane().add(panel);

		frame.setVisible(true);
	}
}


class MyPanel extends JPanel implements Runnable {
	// ボールの位置
	private int x,y;
	private int wait_time;

	static CellState now;
	int LmapY;
	int LmapX;
	int Nutrition;
	int All_Nutrition;
	boolean wall_flag;
	boolean finish = false;

	Roots root = new Roots();
	DelList dl = new DelList();
	Lsystem ls;

	public MyPanel(int[][] wall,boolean wall_flag,int LmapX,int LmapY,int Nutrition,int All_Nutrition,int startX,int startY,int wait_time) {

		this.LmapY = LmapY;
		this.LmapX = LmapX;
		this.Nutrition = Nutrition;
		this.wait_time = wait_time;
		this.wall_flag = wall_flag;
		this.All_Nutrition = All_Nutrition;
		ls = new Lsystem(LmapX,LmapY,Nutrition,All_Nutrition);
		setBackground(Color.white);
		setFocusable(true);
		x = 0;
		y = 0;
		//setPreferredSize(new Dimension(160, 140));
		//setSize(660,540);

		String s = "0";
		//(満腹度、x、y)
		now = new CellState(Nutrition,startX,startY);
		now.setdirection(9);

		now.addLstate(s);
		root.setRoot(now);


		/*迷路
		 * 	スタート　20,35
		 *
		 */


		for(int i = 10;i < 40;i++){
			for(int j = 10;j < 40;j++){
				createFeed(j, i, Color.red);
			}
		}

		for(int i = 440;i < 470;i++){
			for(int j = 600;j < 630;j++){
				createFeed(j, i, Color.orange);
			}
		}


		//壁
		if(wall_flag == true){
			for(int i = 0;i < wall.length;i++){
				for(int j = 0;j < wall[0].length;j++){
					if(wall[j][i] > 0) createWall(j, i);
				}
			}
		}

		Thread th = new Thread(this);
		th.start();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);


		//餌の描画
		for(int i = 0;i < Map.feed_map.size();i++){
			g.setColor(Map.feed_map.get(i).c);
			g.drawLine(Map.feed_map.get(i).x, Map.feed_map.get(i).y,
					Map.feed_map.get(i).x, Map.feed_map.get(i).y);
		}
		if(wall_flag == true){
			//壁の描画
			for(int i = 0;i < Map.wall_map.size();i++){
				g.setColor(Color.blue);
				g.drawLine(Map.wall_map.get(i).x, Map.wall_map.get(i).y,
						Map.wall_map.get(i).x, Map.wall_map.get(i).y);
			}
		}

		//一番古い親・・・・・・・・・・一番若い子の幅優先探索の順番でキューに入れていく
		Deque<CellState> queue = new ArrayDeque<CellState>();
		for(CellState rc:Roots.roots) queue.offer(rc);
		CellState node;
		while(!queue.isEmpty()) {
			node = queue.poll();
			x = node.x;
			y = node.y;

			//枝の色を餌の色と同じ色にする
			if(Map.checkFeed(x,y)) node.setpColor(Map.Lmap[x][y].c);

			g.setColor(node.c);
			//餌の上に乗ったら色を変えておく
			if(Map.checkFeed(x, y)){
				g.setColor(Color.green);
				//node.setpColor(Color.green);
				//-----****
				node.set_State(Nutrition); //餌の上は空腹度初期値に
			}

			//TODO 描画は別スレッドでやろう！！  →　やっぱやめよう! →　どうしよう
			//g.drawLine(x, y, x, y);	//描画

			for(CellState childNode : node.children) {
				queue.offer(childNode); //現セルの子セルをキューの末尾に追加
			}

			ls.apply_rule(node);
			//TODO 死滅用のapply作ったほうがいいかな
			
			//TODO いらないかな？
			//if(node.dead)

			if(node.Lstate.length() > 2) ls.set_Tree(node);

		}
		for(CellState root_cell:Roots.roots){
			ls.checkRoot(root_cell);
		}
		//死滅属性のセル全て削除する
		//dl.delete();

	}
	//餌を設置する
	public void createFeed(int x,int y,Color c){
		Map.addFeedMap(x, y, c);
	}
	//餌を設置する
	public void createWall(int x,int y){
		Map.addWallMap(x, y);
	}


	public void run() {
		while(true) {
			try {
				repaint();
				Thread.sleep(wait_time);
			}
			catch(Exception e) {
			}
		}
	}
}