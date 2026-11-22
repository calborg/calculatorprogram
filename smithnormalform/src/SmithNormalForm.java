import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class SmithNormalForm {
	public static void main(String[] args) {

		Rat [][] A = inputMatrix();
		Poly1 [][] P = makePolyMatrix(A);

		System.out.println("下記多項式行列のSmith標準形を求めます:");
		System.out.println("******************************************************");
		showMatrix(P);
		calculateSmithNormalForm(P);
		System.out.println("******************************************************");
		System.out.println("実行結果は以下の通りです");
		showMatrix(P);
	}

	public static void calculateSmithNormalForm(Poly1 [][] P) {
		int n = P.length;
		for (int d=0; d<n-1; d++) {
			// 1. (d, d)成分より右下の領域から最小次数の非ゼロ多項式を探し(d, d)と入れ替え
			int [] a = searchNonZeroPolynomial(P,d,d);
			if(a[0]==-1) {
				break;
			}
			int row = a[0];
			int column = a[1];
			rowReplace(P,d,row);
			columnReplace(P,d,column);
			// 2. d行・d列の (d, d)成分以外がすべて0になるまで互除法で消去
			// loopJudge は全要素が0のときtrueを返すため、!で反転してwhileループを継続する
			while(!loopJudge(P,d)) {
				deleteRow(P,d);
				deleteColumn(P,d);
			}
			// 3. (d, d)成分より右下の領域にP[d][d]で割り切れない成分があれば行を加算して再消去
			int unDivisibleRow = searchUnDivisiblePolynomial(P,P[d][d],d);
			while (unDivisibleRow!=-1) {
				rowPlus(P,unDivisibleRow,d);
				while(!loopJudge(P,d)) {
					deleteRow(P,d);
					deleteColumn(P,d);
				}
				unDivisibleRow = searchUnDivisiblePolynomial(P,P[d][d],d);
			}
		}
		// 4. 各対角成分(単因子)の係数たちの分母を払って最大公約数で割ることにより正規化
		for (int d=0; d<n; d++) {
			Poly1.completion(P[d][d]);
		}
	}


	public static boolean loopJudge(Poly1 [][] P, int d) {
		for (int j=d+1; j<P[0].length; j++) {
			if (P[d][j].getSize()!=0) {
				return false;
			}
		}
		for (int i=d+1; i<P.length; i++) {
			if (P[i][d].getSize()!=0) {
				return false;
			}
		}
		return true;
	}

	public static void deleteRow(Poly1 [][] P, int d) {//d行目の(d+1)列目以降を互除法により消去する
		for (int j=d+1; j<P[0].length; j++) {
			while (P[d][j].getSize()!=0) {
				Poly1 q = new Poly1();
				q =(P[d][j]).quotient(P[d][d])[0];
				columnOperation(P,q,d,j);//j列目からq倍したd列を引く
				if (P[d][j].getSize() != 0) {
					columnReplace(P,d,j);
				}
			}
		}
	}
	public static void deleteColumn(Poly1 [][] P, int d) {//d列目の(d+1)行目以降を互除法により消去する
		for (int i=d+1; i<P.length; i++) {
			while(P[i][d].getSize()!=0) {
				Poly1 q = new Poly1();
				q =(P[i][d]).quotient(P[d][d])[0];
				rowOperation(P,q,d,i);
				if (P[i][d].getSize() != 0) {
					rowReplace(P,d,i);
				}
			}
		}
	}
	public static void rowPlus(Poly1 [][] P, int p, int q) { //Pのq行目にp行目を足す
		for (int j = 0; j< P[0].length; j++) {
			P[q][j] = P[q][j].plus(P[p][j]);
		}
	}
	public static void rowOperation(Poly1 [][] P, Poly1 f, int p, int q) { //Pのp行目をf倍したものをq行目から引く
		for (int j = 0; j< P[0].length; j++) {
			P[q][j] = P[q][j].minus(f.multiple(P[p][j]));
		}
	}
	public static void columnOperation(Poly1 [][] P, Poly1 f, int p, int q) { //Pのp列目をf倍したものをq列目から引く
		for (int i = 0; i< P.length; i++) {
			P[i][q] = P[i][q].minus(f.multiple(P[i][p]));
		}
	}
	public static void rowReplace(Poly1 [][] P, int p, int q) {// Pのp行目とq行目を入れ替える
		Poly1 dummy = new Poly1();
		for (int j = 0; j < P[0].length; j++) {
			dummy = P[p][j];
			P[p][j] = P[q][j];
			P[q][j] = dummy;
		}
	}
	public static void columnReplace(Poly1 [][] P, int p, int q) {// Pのp列目とq列目を入れ替える
		Poly1 dummy = new Poly1();
		for (int i = 0; i < P.length; i++) {
			dummy = P[i][p];
			P[i][p] = P[i][q];
			P[i][q] = dummy;
		}
	}
	public static Rat[][] inputMatrix(){//行列の入力
		Scanner sc = new Scanner(System.in);
		int n = sc.nextInt();
		if (n<=1) {
			throw new IllegalArgumentException("行列の次数は2以上で入力してください");
		}
		Rat [][] M = new Rat [n][n];
		for (int i=0; i<M.length; i++) {
			for (int j=0; j<M[0].length; j++) {
				String component = sc.next();
				int mid = -1;
				int cnt = 0;
				for (int k=0; k<component.length(); k++) {
					if (component.substring(k,k+1).equals("/")) {
						cnt++;
						mid = k;
					}
					if (!Character.isDigit(component.charAt(k)) && !component.substring(k,k+1).equals("/") && !component.substring(k,k+1).equals("-")) {
						throw new IllegalArgumentException(i +"行" + j +"列に関して、数値か/以外の入力を検知しました");
					}
					if (k>0 && component.substring(k,k+1).equals("-")) {
						throw new IllegalArgumentException(i +"行" + j +"列に関して、-は先頭に入力してください");
					}

				}
				if (cnt>1) {
					throw new IllegalArgumentException(i +"行" + j +"列に関して、/は2つ以上入力しないでください");
				}

				if (mid==0) {
					throw new IllegalArgumentException(i +"行" + j +"列に関して、/の前に数値を入力してください");
				} else if (mid==component.length()-1) {
					throw new IllegalArgumentException(i +"行" + j +"列に関して、/の後に数値を入力してください");
				}

				if (mid==-1) {
					M[i][j] = new Rat(Long.parseLong(component), 1);
				} else {
					String num = component.substring(0,mid);
					String denom = component.substring(mid+1,component.length());
					Long a = Long.parseLong(num);
					Long b = Long.parseLong(denom);
					M[i][j] = new Rat(a,b);
				}
			}
		}
		//sc.close();
		return M;
	}
	public static Poly1 [][] makePolyMatrix(Rat [][] M){//xE-Aを作る
		Poly1 [][] P = new Poly1[M.length][M[0].length];
		for (int i=0; i<M.length; i++) {
			for (int j=0; j<M[0].length; j++) {
				P[i][j] = new Poly1();
				if (M[i][j].getNum()!=0) {
					P[i][j].putTerm(0, M[i][j].mul(new Rat(-1,1)));
				}
				if (i==j) {
					P[i][j].putTerm(1, new Rat(1,1));
				}
			}
		}
		return P;
	}
	public static int searchUnDivisiblePolynomial(Poly1[][] P, Poly1 f, int d) {//(d,d)の右下領域にfで割り切れる成分を探す
		int n = P.length;
		int a = -1;//割り切れる成分がなければ-1を返す
		label:for (int i=d+1; i<n; i++) {
			for (int j=d+1; j<n; j++) {
				if (P[i][j].quotient(f)[1].getSize()!=0) {
					a = i;
					break label;
				}
			}
		}
		return a;
	}

	public static int[] searchNonZeroPolynomial(Poly1[][] P, int p, int q) {//(p,q)の右下領域から非ゼロ成分を探す
		int [] a = new int[2];
		a[0] = -1;
		int deg = Integer.MAX_VALUE;
		for (int i=p; i<P.length; i++) {
			for (int j=q; j<P[0].length; j++) {
				if (P[i][j].getSize() != 0 && P[i][j].getDegree()<deg) {
					deg =  P[i][j].getDegree();
					a[0] = i;
					a[1] = j;
				}
			}
		}
		return a;
	}
	public static void showMatrix(Poly1[][] P) {//行列表示
		for (int i=0; i<P.length; i++) {
			for (int j=0; j<P[0].length; j++) {
				if (P[i][j].getSize()==0) {
					System.out.print("0");
				} else {
					Poly1.show(P[i][j]);
				}
				if (j<P[0].length-1) {
					System.out.print(", ");
				}
			}
			System.out.println();
		}
	}
}

class Poly1 {//多項式クラス(有理係数 1変数)
	private TreeMap<Integer, Rat> f;
	public Poly1 () {
		this.f = new TreeMap<>();
	}
	public Poly1(TreeMap<Integer, Rat> g) {
		this.f = new TreeMap<>(g);
	}
	public TreeMap<Integer, Rat> getMap(){
		return this.f;
	}
	public Poly1(Poly1 g) {
		this.f = new TreeMap<>(g.getMap());
	}
	public Set<Integer> getMonomialSet() {//項の集合を取得する（多項式の台）
		return this.f.keySet();
	}
	public Rat getCoeff(Integer mono) {//項の係数を取得する
		if (f.containsKey(mono)) {
			return this.f.get(mono);
		} else {
			return new Rat(0,1);
		}
	}
	public int getDegree(){//次数を取得する
		if (this.f.size()>0) {
			return this.f.lastKey();
		} else {
			return -1;
		}
	}
	public int getSize(){
		return this.f.size();
	}
	public void putTerm(Integer mono, Rat c) {//項に係数を設定する
		if(mono >=0 && c.getNum()!=0) {
			this.f.put(mono,c);
		}
	}
	public void putTerm(Integer mono, int num, int denom) {//項に係数を設定する
		if(mono >=0 && num!=0) {
			this.f.put(mono, new Rat(num,denom));
		}
	}
	public void removeTerm(Integer mono) {//項を取り除く
		if (f.containsKey(mono)) {
			this.f.remove(mono);
		}
	}
	public boolean contains(Integer mono) {//項が含まれるか
		return this.f.containsKey(mono);
	}
	public Poly1 plus(Poly1 g) {//多項式の和
		Poly1 h = new Poly1();//f+g
		Set<Integer> monomialSet = new HashSet<>();//supp(f)∪supp(g) (f,gに現れる単項式の合併集合)

		//f,gに現れる単項式の合併集合を作る処理
		for (Integer mono : this.f.keySet()) {
			monomialSet.add(mono);
		}
		for (Integer mono : g.getMonomialSet()) {
			monomialSet.add(mono);
		}

		//合併集合から単項式を順次リストアップし、hに現れる単項式の係数を決定する
		for (Integer mono : monomialSet) {
			if (this.f.containsKey(mono) && g.contains(mono)) {//f,gで同類項があるときの処理
				Rat coeff = this.f.get(mono).plus(g.getCoeff(mono));
				if (!coeff.equal(new Rat(0,1))) {//係数の和が0でない時のみhに追加
					h.putTerm(mono, coeff);
				}
			} else if (this.f.containsKey(mono)) {//同類項が無いときの処理その1(monoがfにしか現れない)
				h.putTerm(mono, this.f.get(mono));
			} else if (g.contains(mono)) {//同類項が無いときの処理その2(monoがgにしか現れない)
				h.putTerm(mono, g.getCoeff(mono));
			}
		}
		return h;
	}

	public Poly1 minus(Poly1 g) {//多項式の差
		Poly1 minus_g = new Poly1();
		for (Integer mono : g.getMonomialSet()) {
			Rat negativeCoeff = (new Rat(-1,1)).mul(g.getCoeff(mono));
			minus_g.putTerm(mono, negativeCoeff);
		}
		TreeMap<Integer,Rat> ff = new TreeMap<>(this.f);
		Poly1 copy_f = new Poly1(ff);
		return copy_f.plus(minus_g);
	}

	public Poly1 multiple(Poly1 g) {//多項式の積
		Poly1 h = new Poly1();//f×g

		//f,gの単項式mf, mgを一つずつ取り出し、掛け合わせたものを作り、hに追加していく
		for (Integer mf : this.f.keySet()) {
			for (Integer mg : g.getMonomialSet()) {
				Integer mh = mf + mg;
				Rat newCoeff = (this.f.get(mf)).mul(g.getCoeff(mg));
				if (h.contains(mh)) {//既にmhがhに入っている場合の処理
					Rat oldCoeff = h.getCoeff(mh);
					Rat sumCoeff = oldCoeff.plus(newCoeff);
					if (!sumCoeff.equal(new Rat(0,1))) {//係数の和が0でなければmhの係数を更新
						h.putTerm(mh, sumCoeff);
					} else {//係数の和が0であれば項を削除
						h.removeTerm(mh);
					}
				} else {//mhがhに入ってない場合の処理
					h.putTerm(mh, newCoeff);
				}
			}
		}
		return h;
	}
	public static void show(Poly1 p) {//多項式の内容を出力する
		int d = p.getDegree();

		for(int mono=d; mono>=0; mono--) {
			if (!p.contains(mono)) {
				continue;
			}
			if (mono<d && p.getCoeff(mono).compare(new Rat(0,1))) {
				System.out.print("+");
			}

			if(p.getCoeff(mono).equal(new Rat(1,1))) {//係数が1の場合
				if(mono>1) {
					System.out.print("x^" + mono);
				}
				if(mono==1) {
					System.out.print("x");
				}
				if(mono==0) {
					System.out.print("1");
				}
			}else if((p.getCoeff(mono).equal(new Rat(-1,1)))) {
				if(mono>1) {
					System.out.print("-x^" + mono);
				}
				if(mono==1) {
					System.out.print("-x");
				}
				if(mono==0) {
					System.out.print("-1");
				}
			} else {
				Rat.show(p.getCoeff(mono));
				if(mono>1) {
					System.out.print("x^" + mono);
				}
				if(mono==1) {
					System.out.print("x");
				}
			}
		}
	}
	public Poly1[] quotient(Poly1 g) {//多項式の割り算(商を求める)
		if (g.getSize() == 0) {
			throw new IllegalArgumentException("0多項式での除算はできません。");
		}
		Poly1 q = new Poly1();
		Poly1 p = new Poly1(this.f);

		while (p.getDegree() >= g.getDegree()) {
			int n = p.getDegree();
			int m = g.getDegree();
			Rat a = p.getCoeff(n);
			Rat b = g.getCoeff(m);
			q.putTerm(n-m, a.div(b));

			Poly1 term = new Poly1();
			term.putTerm(n-m, a.div(b));
			p = p.minus(g.multiple(term));
		}

		Poly1 [] pair = new Poly1[2];
		pair[0] = q;
		pair[1] = p;
		return pair;
	}
	public static void completion(Poly1 g) {//多項式の係数の正規化
		if (g.getSize() == 0) return;

		long lcm = 1;
		for (Integer mono : g.getMonomialSet()) {//係数の分母たちの最小公倍数lcmを計算
			lcm = Rat.lcm(lcm, g.getCoeff(mono).getDenom());
		}
		for (Integer mono : g.getMonomialSet()) {//lcmを各係数に掛け算する(gを整数係数多項式にする)
			Rat L = g.getCoeff(mono).mul(new Rat(lcm,1));
			g.putTerm(mono, L);
		}
		long gcd = g.getCoeff(g.getDegree()).getNum();//gの係数たちの最大公約数gcdを計算
		for (Integer mono : g.getMonomialSet()) {
			gcd = Rat.gcd(gcd, g.getCoeff(mono).getNum());
		}
		for (Integer mono : g.getMonomialSet()) {//gの係数たちをgcdで割り算する(gを原始多項式にする)
			Rat D = g.getCoeff(mono).div(new Rat(gcd,1));
			g.putTerm(mono, D);
		}
		if (!g.getCoeff(g.getDegree()).compare(new Rat(0,1))) {//gの最高次の係数が負の場合、全体に-1をかけて整える
			for (Integer mono : g.getMonomialSet()) {
				Rat negativeCoeff =  g.getCoeff(mono).mul(new Rat(-1,1));
				g.putTerm(mono, negativeCoeff);
			}
		}
	}
}

class Rat {//有理数クラス
	private long num;//分子
	private long denom;//分母

	public Rat () {} //デフォルトコンストラクタ
	public Rat(long num,long denom){
		if (denom != 0) {
			if (denom < 0) {//分母は必ず正になるよう正規化する
				this.num = -num;
				this.denom = -denom;
			} else if (denom > 0) {
				this.num = num;
				this.denom = denom;
			}
		} else {
			throw new IllegalArgumentException("分母にゼロは指定できません");
		}
	}
	public void setNum(long num) {
		this.num = num;
	}
	public long getNum() {
		return num;
	}
	public void setDenom(long denom) {
		this.denom = denom;
	}
	public long getDenom() {
		return denom;
	}
	public boolean equal(Rat s) {//有理数の一致判定
		long a = this.num;
		long b = this.denom;
		long c = s.getNum();
		long d = s.getDenom();
		if (a*d == c*b) {
			return true;
		} else {
			return false;
		}
	}
	public boolean compare(Rat s) {//有理数の大小比較
		long a = this.num;
		long b = this.denom;
		long c = s.getNum();
		long d = s.getDenom();
		if (a*d-c*b > 0) {
			return true;
		} else {
			return false;
		}
	}
	public Rat plus(Rat s) { //有理数同士の足し算
		if (this.num==0) {
			return s;
		}
		if (s.getNum()==0) {
			return new Rat(this.num,this.denom);
		}
		long l = lcm(this.denom,s.getDenom());
		if (l < 0) {
			l=-l;
		}
		long m = l/this.denom;
		long mm = l/s.getDenom();
		return reduce(new Rat(this.num*m+s.getNum()*mm, l));
	}
	public Rat minus(Rat s) { //有理数同士の引き算
		Rat u = new Rat(-s.getNum(),s.getDenom());
		return (new Rat(this.num,this.denom)).plus(u);
	}
	public Rat mul(Rat s) { //有理数同士の掛け算
		if (this.num==0 || s.getNum()==0) {
			return new Rat(0,1);
		}
		long g1 = gcd(this.denom,s.getNum());
		long g2 = gcd(this.num,s.getDenom());
		long a = (this.num/g2);
		long b = (s.getNum()/g1);
		long c =  (this.denom/g1);
		long d = (s.getDenom()/g2);
		return new Rat(a*b, c*d);
	}
	public static Rat inv(Rat r) { //有理数の逆数
		return new Rat(r.getDenom(), r.getNum());
	}
	public Rat div(Rat s) { //有理数同士の割り算
		return (new Rat(this.num,this.denom)).mul(inv(s));
	}
	public static long gcd(long a, long b){ //aとbの最大公約数（Euclidの互除法を使用）
		if(a<0) {
			a = -a;
		}
		if(b<0) {
			b = -b;
		}

		if (a == 0) {
			return b;
		}
		if (b == 0) {
			return a;
		}

		if (a < b) {
			long w = a;
			a = b;
			b = w;
		}
		long r = a % b;
		while (r != 0) {
			a = b;
			b = r;
			r = a % b;
		}
		return b;
	}
	public static long lcm(long a, long b) {//最小公倍数
		return (a/gcd(a,b)) * b;
	}
	public static Rat reduce(Rat r) { //分数の約分
		if (r.getNum() == 0) {
			r.setDenom(1);
			return r;
		} else {
			long a = r.getNum();
			long b = r.getDenom();
			long d = gcd(a,b);
			a = a/d;
			b = b/d;
			if (b < 0) {
				a = -a;
				b = -b;
			}
			r.setNum(a);
			r.setDenom(b);
			return r;
		}
	}
	public static void show(Rat r) { //Rational型変数rを，分数（有理数）として表示
		long a = r.getNum();
		long b = r.getDenom();
		if (b == 1) {//分母が1ならrは整数のため分子のみ表示
			System.out.print(a);
		} else {
			System.out.print(a + "/" + b);
		}
	}
}
