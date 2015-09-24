package core;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;


public class Lsystem{

	//mapの大きさもできれば可変にしたい
	int LmapY;
	int LmapX;
	int nutrition;
	int init_nutrition;
	public Lsystem(int mapX,int mapY,int nutrition,int all_nutrition){
		this.LmapX = mapX;
		this.LmapY = mapY;
		this.nutrition = nutrition;
		this.init_nutrition = all_nutrition;
	}

	DelList del = new DelList();
	int cnt = 0;
	Roots root = new Roots();

	/*
	 * 方向を与える定数一覧
	 * ここは引数で与えたい（予定)
	 */
	static String[] cons = {"|","[","]",">","<","(",")","="};

	//Lsystem ルールを適用する
	public void apply_rule(CellState now){

		String new_s = "";
		String s = "";
		if(now.Lstate.length() > 1)s = now.Lstate.substring(0, 1);
		else s = new String(now.Lstate);
		//移動方向を決める定数consを飛ばす処理
		for(int i = 0;i < cons.length;i++){
			if(s.equals(cons[i])){
				s = now.Lstate.substring(1, 2);
				break;
			}
		}

		/*
		 * ルール適用
		 *  ""や変数として登録されていないものをreturnすると何もせず満腹度が0になるまでその場に留まる
		 */
		Random rnd = new Random();


		//一番粘菌っぽい
		//空腹度が0以下のとき状態を変化
		if(now.state <= 0){
			now.replaceLstate("d");
			s = "d";
		}
		if(s.equals("0")){
			int ran = rnd.nextInt(10000);

			if((ran%3)==0) new_s = "3|1";
			else if((ran%3)==1) new_s = "3[s]s";
			else new_s = "3[0]0";
		}
		else if(s.equals("1")){
			new_s = "3|0";
		}
		else if(s.equals("2")){
			new_s = "3]0[0";
		}
		else if(s.equals("4")){
			new_s = "3<0>0";
		}
		else if(s.equals("5")){
			new_s = "3]1[1";
		}
		else if(s.equals("6")){
			new_s = "3<1>1";
		}
		else if(s.equals("7")){
			new_s = "3[0]0|0";
		}
		else if(s.equals("8")){
			//new_s = "3<0[0>0]0|0(0)0=0";
			new_s = "3<0>0|0";
		}
		else if(s.equals("9")){
			new_s = "3<0[0>0]0|0(0)0=0";
		}
		//維持状態
		else if(s.equals("s")){
			now.state -= 1;
			
			int ran = rnd.nextInt(10000);

			if((ran%20)<=3) new_s = "d";
			else if((ran%20)>3 && (ran%20)<10) new_s = "3|0";
			else new_s = "s";
			//new_s = "d";

		}
		//消滅ルール
		else if(s.equals("d")){
			System.out.println("dead");
			//先端の子が死んでいく処理
			if(now.children.size() <= 0){
				//セルが餌の上にいないとき
				if(now.onFeed() == false){
						//親セルに死滅状態が伝染
						now.parent.replaceLstate("d");
						//死滅
						del.add(now);
				}

				else{
					//ここで餌から成長が再開するかどうか決める
					//now.replaceLstate("0");
				}


			}

			return ;
		}
		else{
			new_s = s;
		}
		now.replaceLstate(new_s);
		return ;
	}

	/*
	 * 変化した状態に対しての処理
	 * 子を作成し、配置する。（n分木を作成する）
	 */
	public void set_Tree(CellState now){
		StringBuffer Ls = now.Lstate;
		ArrayList<String> Lstr = new ArrayList<String>();

		//stringBuffer初期化
		StringBuffer sb = new StringBuffer("");
		sb.setLength(0);


		//式を分割してリストに入れる処理
		//最初にnowセルの状態変化をリストに入れる
		Lstr.add(String.valueOf(Ls.charAt(0)));
		int cnt = 1;
		//現在の仕様だと定数が2回連続続いたものが来ると正しい処理を行わないので事前に弾くかプログラムを書き換える
		while(cnt < Ls.length()){
			int num = 0;
			int i = 0;
			for(int i2 = cnt;i2 < Ls.length();i2++){
				i++;

				if(cnt +i + 1 < Ls.length()){
					for(int i3 = 0;i3 < cons.length;i3++){
						if(Ls.substring(cnt + i, cnt + i + 1).equals(cons[i3])){
							num++;
						}
					}
				}
				else{
					sb.append(Ls.substring(cnt, Ls.length()));
					Lstr.add(new String(sb));
					sb.setLength(0);
					//ループ抜け
					num = 0;
					cnt = Ls.length();
					break;
				}

				if(num == 1 ){
					sb.append(Ls.substring(cnt, cnt + i));
					Lstr.add(new String(sb));
					sb.setLength(0);
					cnt = cnt + i ;
					break;
				}
			}
		}

		//nowセルの状態変化
		now.replaceLstate(Lstr.get(0));
		//子を作る
		//Lstrの１つ目は自分の状態変化　子は２つ目から
		boolean child_create = false;
		for(int i = 1;i < Lstr.size();i++){
			//子の数に応じて子の枝を作る
			CellState chi = set_child(now,Lstr.get(i), 1);
			//chiはnullで帰ってくるかどうか
			if(chi != null){
				now.addChild(chi);
				child_create = true;
			}
		}
		//子セルが1つも生成できない場合
		/*
		 * 子セルを生成する遺伝子（ルール）を持っているが周囲が一杯で生成できない場合と、
		 * 子セルを生成する遺伝子（ルール）を持っていないため生成出来ない場合があるため場合分けする。
		 */
		if(child_create == false){
			//周囲がいっぱいの場合
			if(Lstr.size() > 1){
				now.replaceLstate("d");
				//now.replaceLstate("s");
				//now.state-=(nutrition*sigmoid(1-((double)now.state/(double)nutrition),10));
			}
			//遺伝子を持っていない場合
			else{
				now.replaceLstate("d");
			}
		}

		return ;
	}



	//Lsystem2 子の設定
	public CellState set_child(CellState now,String Lstr,int num){

		int xyd[] = new int[3];
		/* ここは仕様によって変える必要あり
		 * mark 方向等を指定する定数
		 * var 状態変数
		 * xyd[0] 次に作る子セルのx座標
		 * xyd[1] 次に作る子セルのy座標
		 * xyd[2] 次に作る子セルの向き（親から見て）
		 */
		char mark = Lstr.charAt(0);
		char var = Lstr.charAt(num);
		xyd = set_locate(now,mark);
		CellState child = null;

		//mapを見て既に別のセルが配置されていたら子を作成しない  ←今のところ
		//overlap個の重なりを許す
		//TODO toriaezu
		if(Map.Lmap[xyd[0]][xyd[1]].population < 1){
			//自分と同じ色の枝にぶつかるか見る
			if(Map.Lmap[xyd[0]][xyd[1]].cells.size() >= 1){
				if(Map.Lmap[xyd[0]][xyd[1]].c == now.c) return null;
				for(CellState cell:Map.Lmap[xyd[0]][xyd[1]].cells){
					if(cell.c == now.c) return null;
				}
			}
			//空腹度が-1された子セルを生成
			child = new CellState(now.state,xyd[0],xyd[1]);
			//向きの指定
			child.setdirection(xyd[2]);
			//親を設定
			child.setParent(now);
			//親の色を遺伝
			child.setpColor(now.c);
			Map.Lmap[xyd[0]][xyd[1]].addPopulation();;
			Map.Lmap[xyd[0]][xyd[1]].addCell(child);
			//子の状態変数を設定
			child.addLstate(String.valueOf(mark)+String.valueOf(var));
			//空腹度が0以下の子セルが生成されたときは子セルを死滅状態に
			if(child.state <= 0){
				child.replaceLstate("d");
				return child;
			}

		}
		return child;

	}

	//Lsystem2 親のdirectionによる子供の配置位置算出　ついでに方向も
	//返り値は子の位置、向き
	public int[] set_locate(CellState now,char c){
		int xyd[] = new int[3];
		int direction = set_direction(now,c);
		int x=0,y=0;
		if(direction == 1) {
			x = -1;
			y = 1;
		}else if(direction == 2){
			x = 0;
			y = 1;
		}else if(direction == 3){
			x = 1;
			y = 1;
		}else if(direction == 4){
			x = -1;
			y = 0;
		}else if(direction == 5){
			x = 0;
			y = 0;
		}else if(direction == 6){
			x = 1;
			y = 0;
		}else if(direction == 7){
			x = -1;
			y = -1;
		}else if(direction == 8){
			x = 0;
			y = -1;
		}else if(direction == 9){
			x = 1;
			y = -1;
		}
		xyd[0] = now.x + x;
		xyd[1] = now.y + y;
		//壁に当たったときの処理
		if(xyd[0] < 0) xyd[0] = 0;
		if(xyd[0] >= LmapX) xyd[0] = LmapX - 1;
		if(xyd[1] < 0) xyd[1] = 0;
		if(xyd[1] >= LmapY) xyd[1] = LmapY -1;
		if(Map.Lmap[xyd[0]][xyd[1]].wall) {
			xyd[0] = xyd[0] - x;
			xyd[1] = xyd[1] -y;
		}

		xyd[2] = direction;

		return xyd;
	}
	//Lsystem2 方向の割り当て
	public int set_direction(CellState now,char d){
		int now_d = now.direction;
		int next_d = 0;
		if(now_d == 1){
			if(d == ']') next_d = 4;
			else if(d == '[') next_d = 2;
			else if(d == '>') next_d = 7;
			else if(d == '<') next_d = 3;
			else if(d == '(') next_d = 6;
			else if(d == ')') next_d = 8;
			else if(d == '=') next_d = 9;
			else next_d = 1;
		}
		if(now_d == 2){
			if(d == ']') next_d = 1;
			else if(d == '[') next_d = 3;
			else if(d == '>') next_d = 4;
			else if(d == '<') next_d = 6;
			else if(d == '(') next_d = 9;
			else if(d == ')') next_d = 7;
			else if(d == '=') next_d = 8;
			else next_d = 2;
		}
		if(now_d == 3){
			if(d == ']') next_d = 2;
			else if(d == '[') next_d = 6;
			else if(d == '>') next_d = 1;
			else if(d == '<') next_d = 9;
			else if(d == '(') next_d = 8;
			else if(d == ')') next_d = 4;
			else if(d == '=') next_d = 7;
			else next_d = 3;
		}
		if(now_d == 4){
			if(d == ']') next_d = 7;
			else if(d == '[') next_d = 1;
			else if(d == '>') next_d = 8;
			else if(d == '<') next_d = 2;
			else if(d == '(') next_d = 3;
			else if(d == ')') next_d = 9;
			else if(d == '=') next_d = 6;
			else next_d = 4;
		}
		if(now_d == 5){
			Random rnd = new Random();
			int ran = rnd.nextInt(4);
			if(ran == 0) next_d = 2;
			else if(ran == 1) next_d = 4;
			else if(ran == 2) next_d = 6;
			else next_d = 8;
		}
		if(now_d == 6){
			if(d == ']') next_d = 3;
			else if(d == '[') next_d = 9;
			else if(d == '>') next_d = 2;
			else if(d == '<') next_d = 8;
			else if(d == '(') next_d = 7;
			else if(d == ')') next_d = 1;
			else if(d == '=') next_d = 4;
			else next_d = 6;
		}
		if(now_d == 7){
			if(d == ']') next_d = 8;
			else if(d == '[') next_d = 4;
			else if(d == '>') next_d = 9;
			else if(d == '<') next_d = 1;
			else if(d == '(') next_d = 2;
			else if(d == ')') next_d = 6;
			else if(d == '=') next_d = 3;
			else next_d = 7;
		}
		if(now_d == 8){
			if(d == ']') next_d = 9;
			else if(d == '[') next_d = 7;
			else if(d == '>') next_d = 6;
			else if(d == '<') next_d = 4;
			else if(d == '(') next_d = 1;
			else if(d == ')') next_d = 3;
			else if(d == '=') next_d = 2;
			else next_d = 8;
		}
		if(now_d == 9){
			if(d == ']') next_d = 6;
			else if(d == '[') next_d = 8;
			else if(d == '>') next_d = 3;
			else if(d == '<') next_d = 7;
			else if(d == '(') next_d = 4;
			else if(d == ')') next_d = 2;
			else if(d == '=') next_d = 1;
			else next_d = 9;
		}

		return next_d;
	}


	//ルートセルが死滅するかチェック
	public void checkRoot(CellState root_cell) {
		//ルートの親が死んでいく処理
		String s = root_cell.Lstate.substring(0, 1);
		if(s.equals("d")){
			//子セルが１つ以下の場合はroot属性を子セルに移し、死滅する
			if(root_cell.children.size() <= 1){
				del.add(root_cell);
			}
		}
	}
	//xが０～１のシグモイド関数
	double sigmoid(double x, double gain)
	{
		return 1.0 / (1.0 + Math.exp(-gain * (x*2-1)));
	}

}
