import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;
class Comp2 implements Comparator<Pair> {
	@Override
	public int compare(Pair o1, Pair o2) {
		if (o2.Af > o1.Af)
			return 1;
		else if (o2.Af == o1.Af)
			return 0;
		else
			return -1;
	}
}
public class Trainer {
	final int Ngen;
	final int N;
	final int M;
	final int d;// must be >=1
	final int n;
	final double p;
	final int antibodySize;
	double clonalfactor;
	int numOfClasses;
	ArrayList<Antibody>[] Ab;
	Antigen[] Ag;
	int k;
	public Trainer(ArrayList<Antibody>[] passedAb, Antigen[] Ag, Object[] argsHolder) {//sizee of passedAb is numOfClasses+1, all the AB belonging to r is in the last index
		this.Ngen = new Integer(argsHolder[0].toString());
		this.N = new Integer(argsHolder[1].toString());
		this.M = new Integer(argsHolder[2].toString());
		this.d = new Integer(argsHolder[3].toString());
		this.n = new Integer(argsHolder[4].toString());
		this.p = new Double(argsHolder[5].toString());
		this.antibodySize = new Integer(argsHolder[6].toString());
		this.clonalfactor = new Double(argsHolder[7].toString());
		this.numOfClasses = new Integer(argsHolder[8].toString());
		this.k = new Integer(argsHolder[9].toString());
		this.Ag = Ag;
		this.Ab = passedAb;
	}
	
	private void permuteAg() {
		for (int i = 0; i < Ag.length; i++) {
			int j = (int) (Math.random() * (Ag.length - i)) + i;
			Antigen tmp = Ag[i];
			Ag[i] = Ag[j];
			Ag[j] = tmp;
		}
	}
	
	public ArrayList<Antibody>[] train(){
		ArrayList<Antigen>[] AgSegmentedByClass = (ArrayList<Antigen>[]) new ArrayList[numOfClasses];
		for(int i = 0; i < numOfClasses; i++){
			AgSegmentedByClass[i] = new ArrayList<Antigen>();
		}
		for(int i = 0; i < Ag.length; i++){
			AgSegmentedByClass[Ag[i].getLabel()].add(Ag[i]);
		}
		for(int G = 0; G < Ngen; G++){
			//System.out.println("In generation " + G);
			permuteAg();
			for(int i = 0; i < Ag.length; i++){
				int label = Ag[i].getLabel();
				Antibody[] temp = new Antibody[Ab[label].size() + N - M];
				int x;
				for(x = 0; x < Ab[label].size(); x++){
					temp[x] =  Ab[label].get(x);
				}
				int hold = x;
				for(; x < (N-M) + hold; x++){
					temp[x] = Ab[numOfClasses].get(x-hold);
				}
				Antibody[] R = new List(temp, Ag[i], n, p, clonalfactor, N, k).doWork(); // do all the work
			
				//selectFromOtherClass contains 1 Ag from each other class
				Antigen[] selectFromOtherClass = new Antigen[numOfClasses-1];
				int u = 0;
				for(int j = 0; j < numOfClasses; j++){
					if (j == Ag[i].getLabel()){
						continue;
					}
					selectFromOtherClass[u++] = AgSegmentedByClass[j].get(new Random().nextInt(AgSegmentedByClass[j].size())); 	
				}
				
				
				
				//calculate avg affinity
				Pair[] avgAffinity = new Pair[k];
				for(int j =0; j < k; j++){
					double sum = 0;
					for(int l = 0; l < 4; l++){		// 4 is hard coded here
						sum += AgSegmentedByClass[Ag[i].getLabel()].get(new Random().nextInt(AgSegmentedByClass[Ag[i].getLabel()].size())).affinity(R[j]);
					}
					avgAffinity[j] = new Pair(R[j], sum/4); 
				}
				
				AbDecider[] helper = new AbDecider[k]; //keeps track of which antibodies will go to memory pool
				for(int j = 0; j < k; j++){
					helper[j] = new AbDecider(true, avgAffinity[j]);
				}
				for(int l = 0; l < k; l++){
					int count = 0;
					for(int j = 0; j < selectFromOtherClass.length; j++){
						if(selectFromOtherClass[j].affinity(avgAffinity[l].Ab) > avgAffinity[l].Af ){
							count++;
						}
					}
					if(count > 2){
						helper[l].isIn = false;
					}
				}
				ArrayList<Pair> remaining = new ArrayList<Pair>();
				for(int j = 0; j < k; j++){
					if(helper[j].isIn){
						remaining.add(helper[j].p);
					}
				}
				if(remaining.size() == 0){
					System.out.println("None of k selected");
					continue;
				}
				Pair[] remainingData = new Pair[remaining.size()];
				for(int j = 0; j< remainingData.length; j++){
					remainingData[j] = remaining.get(j);
				}
				Arrays.sort(remainingData, new Comp2());
				
				Pair[] currrentMemoryPool = new Pair[ Ab[Ag[i].getLabel()].size() ];
				for(int j = 0 ; j < Ab[Ag[i].getLabel()].size(); j++){
					currrentMemoryPool[j] = new Pair(Ab[Ag[i].getLabel()].get(j), Ag[i].affinity(Ab[Ag[i].getLabel()].get(j)) );
				}
			
				//decide whether to replace lowest
				int indexOfLowest = 0;
				double min = 999999999;
				for(int j = 0; j < currrentMemoryPool.length; j++){
					if(currrentMemoryPool[j].Af < min){
						indexOfLowest = j;
						min = currrentMemoryPool[j].Af;
					}
				}
				
				if(currrentMemoryPool[indexOfLowest].Af < remainingData[0].Af){
					Ab[Ag[i].getLabel()].set(indexOfLowest, remainingData[0].Ab);
				}
				
				Pair[] lowestD = new Pair[Ab[numOfClasses].size()];
				for(int j = 0; j < Ab[numOfClasses].size(); j++){
					lowestD[j] = new Pair(Ab[numOfClasses].get(j), Ag[i].affinity(Ab[numOfClasses].get(j)), j) ;
				}
				Arrays.sort(lowestD, new Comp2());
				for(int j = 0; j < d; j++){
					int[] temp2 = new int[antibodySize];
					for(int l = 0; l < temp2.length; l++){
						temp2[l] = new Random().nextInt(Ag.length); 	// replace with sth more sensible
					}
					Ab[numOfClasses].set(lowestD[lowestD.length - j - 1].index, new Antibody(temp2.length, temp2));
				}
				
			}
			
		}
		ArrayList<Antibody>[] returnAns = (ArrayList<Antibody>[]) new ArrayList[numOfClasses];
		for(int j = 0; j < numOfClasses; j++){
			returnAns[j] = Ab[j];
		}
		return returnAns;
	}
	
	
	
	public static ArrayList<ArrayList<Antigen>> prepareIris()
			throws IOException {
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "bezdekIrisTrainData.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[4];
				for (int k = 0; k < 4; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[4];
				for (int k = 0; k < 4; k++)
					D[k] = (int) (1000 * X[k]);
				trainSet.add(new Antigen((new Integer(temp[0]) - 1), D, 4));
			}
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		fileName = "bezdekIrisTestData.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[4];
				for (int k = 0; k < 4; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[4];
				for (int k = 0; k < 4; k++)
					D[k] = (int) (1000 * X[k]);
				testSet.add(new Antigen((new Integer(temp[0]) - 1), D, 4));
			}
		}

		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		
		ans.add(trainSet);
		ans.add(testSet);
		return ans;

	}

	public static double runiris() throws IOException{
		ArrayList<ArrayList<Antigen>> irisData = prepareIris();
		Object[] temp;
		temp = new Object[] { 400, 20,6,7,6,0.01,4, 1.0, 3, 5 };
		ArrayList<Antibody>[] Ab = (ArrayList<Antibody>[]) new ArrayList[4];
		for(int i = 0; i < 4; i++){
			Ab[i] = new ArrayList<Antibody>();
		}
		for(int i = 0; i < 6; i++){
			int sum1, sum2, sum3, sum4;
			sum1 = sum2 = sum3 = sum4 = 0;
			for(int j = i * 20; j < (i+1)*20; j++){
				sum1 += irisData.get(0).get(j).returnComponent(0);
				sum2 += irisData.get(0).get(j).returnComponent(1);
				sum3 += irisData.get(0).get(j).returnComponent(2);
				sum4 += irisData.get(0).get(j).returnComponent(3);
			}
			if(i == 0 || i ==1){
				Ab[0].add(new Antibody(4, new int[] {sum1/20, sum2/20, sum3/20, sum4/20}));
			}
			else if(i == 2 || i ==3){
				Ab[1].add(new Antibody(4, new int[] {sum1/20, sum2/20, sum3/20, sum4/20}));
			}
			else{
				Ab[2].add(new Antibody(4, new int[] {sum1/20, sum2/20, sum3/20, sum4/20}));
			}
			
		}
		for(int i = 0; i < 14 ; i++){
			Ab[3].add(new Antibody(4, new int[] {new Random().nextInt(400), new Random().nextInt(400), new Random().nextInt(400), new Random().nextInt(400)}));
		}
		
		Antigen[] passData = new Antigen[irisData.get(0).size()];
		for(int i = 0; i < irisData.get(0).size(); i++){
			passData[i] = irisData.get(0).get(i);
		}
		Trainer x = new Trainer(Ab,passData, temp);
		ArrayList<Antibody>[] trainedAb = (ArrayList<Antibody>[]) new ArrayList[3];
		trainedAb =  x.train();
		int[][] S = new int[3][3];
		for(int i = 0; i < irisData.get(1).size(); i++){
			Antigen focus = irisData.get(1).get(i);
			Pair[] var = new Pair[6];
			var[0] = new Pair(focus.affinity(trainedAb[0].get(0)) , 0);
			var[1] = new Pair(focus.affinity(trainedAb[0].get(1)) , 0);
			var[2] = new Pair(focus.affinity(trainedAb[1].get(0)) , 1);
			var[3] = new Pair(focus.affinity(trainedAb[1].get(1)) , 1);
			var[4] = new Pair(focus.affinity(trainedAb[2].get(0)) , 2);
			var[5] = new Pair(focus.affinity(trainedAb[2].get(1)) , 2);
			Arrays.sort(var, new Comp2());
			S[focus.getLabel()][var[0].index] += 1;
		}
		for(int i = 0; i < 3; i++){
			System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2]);
		}
		return( (S[0][0] + S[1][1] + S[2][2])/ (double) irisData.get(1).size()  );
	}

	public static ArrayList<ArrayList<Antigen>> prepareLiverDisorder()
			throws IOException {
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "livertrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[6];
				for (int k = 0; k < 6; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[6];
				for (int k = 0; k < 6; k++)
					D[k] = (int) (10000 * X[k]);
				trainSet.add(new Antigen((new Integer(temp[0]) - 1), D, 6));
			}
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		fileName = "livertest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[6];
				for (int k = 0; k < 6; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[6];
				for (int k = 0; k < 6; k++)
					D[k] = (int) (10000 * X[k]);
				testSet.add(new Antigen((new Integer(temp[0]) - 1), D, 6));
			}
		}

		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		ans.add(trainSet);
		ans.add(testSet);
		return ans;
	}
	public static double runLiver() throws IOException{
		ArrayList<ArrayList<Antigen>> liverData = prepareLiverDisorder();
		Object[] temp;
		temp = new Object[] { 400, 20, 6, 7, 6, 0.02, 6, 1.0, 2, 6};
		ArrayList<Antibody>[] Ab = (ArrayList<Antibody>[]) new ArrayList[3];
		for(int i = 0; i < 3; i++){
			Ab[i] = new ArrayList<Antibody>();
		}
		int sum1, sum2, sum3, sum4, sum5, sum6;
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = 0;
		for(int i = 0; i < 38; i++){
			sum1 += liverData.get(0).get(i).returnComponent(0);
			sum2 += liverData.get(0).get(i).returnComponent(1);
			sum3 += liverData.get(0).get(i).returnComponent(2);
			sum4 += liverData.get(0).get(i).returnComponent(3);
			sum5 += liverData.get(0).get(i).returnComponent(4);
			sum6 += liverData.get(0).get(i).returnComponent(5);
		}
		Ab[0].add(new Antibody(6, new int[] {sum1/38, sum2/38,sum3/38,sum4/38,sum5/38,sum6/38}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = 0;
		for(int i = 38; i < 76; i++){
			sum1 += liverData.get(0).get(i).returnComponent(0);
			sum2 += liverData.get(0).get(i).returnComponent(1);
			sum3 += liverData.get(0).get(i).returnComponent(2);
			sum4 += liverData.get(0).get(i).returnComponent(3);
			sum5 += liverData.get(0).get(i).returnComponent(4);
			sum6 += liverData.get(0).get(i).returnComponent(5);
		}
		Ab[0].add(new Antibody(6, new int[] {sum1/38, sum2/38,sum3/38,sum4/38,sum5/38,sum6/38}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = 0;
		for(int i = 76; i < 115; i++){
			sum1 += liverData.get(0).get(i).returnComponent(0);
			sum2 += liverData.get(0).get(i).returnComponent(1);
			sum3 += liverData.get(0).get(i).returnComponent(2);
			sum4 += liverData.get(0).get(i).returnComponent(3);
			sum5 += liverData.get(0).get(i).returnComponent(4);
			sum6 += liverData.get(0).get(i).returnComponent(5);
		}
		Ab[0].add(new Antibody(6, new int[] {sum1/39, sum2/39,sum3/39,sum4/39,sum5/39,sum6/39}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = 0;
		for(int i = 115; i < 168; i++){
			sum1 += liverData.get(0).get(i).returnComponent(0);
			sum2 += liverData.get(0).get(i).returnComponent(1);
			sum3 += liverData.get(0).get(i).returnComponent(2);
			sum4 += liverData.get(0).get(i).returnComponent(3);
			sum5 += liverData.get(0).get(i).returnComponent(4);
			sum6 += liverData.get(0).get(i).returnComponent(5);
		}
		Ab[1].add(new Antibody(6, new int[] {sum1/53, sum2/53,sum3/53,sum4/53,sum5/53,sum6/53}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = 0;
		for(int i = 168; i < 221; i++){
			sum1 += liverData.get(0).get(i).returnComponent(0);
			sum2 += liverData.get(0).get(i).returnComponent(1);
			sum3 += liverData.get(0).get(i).returnComponent(2);
			sum4 += liverData.get(0).get(i).returnComponent(3);
			sum5 += liverData.get(0).get(i).returnComponent(4);
			sum6 += liverData.get(0).get(i).returnComponent(5);
		}
		Ab[1].add(new Antibody(6, new int[] {sum1/53, sum2/53,sum3/53,sum4/53,sum5/53,sum6/53}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = 0;
		for(int i = 221; i < 275; i++){
			sum1 += liverData.get(0).get(i).returnComponent(0);
			sum2 += liverData.get(0).get(i).returnComponent(1);
			sum3 += liverData.get(0).get(i).returnComponent(2);
			sum4 += liverData.get(0).get(i).returnComponent(3);
			sum5 += liverData.get(0).get(i).returnComponent(4);
			sum6 += liverData.get(0).get(i).returnComponent(5);
		}
		Ab[1].add(new Antibody(6, new int[] {sum1/54, sum2/54,sum3/54,sum4/54,sum5/54,sum6/54}));
		
		for(int i = 0; i < 14; i++){
			Ab[2].add(new Antibody(6, new int[]{new Random().nextInt(3000), new Random().nextInt(3000), new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000)}));
		}
		Antigen[] passData = new Antigen[liverData.get(0).size()];
		for(int i = 0; i < liverData.get(0).size(); i++){
			passData[i] = liverData.get(0).get(i);
		}
		Trainer x = new Trainer(Ab,passData, temp);
		ArrayList<Antibody>[] trainedAb = (ArrayList<Antibody>[]) new ArrayList[2];
		trainedAb =  x.train();
		int[][] S = new int[2][2];
		for(int i = 0; i < liverData.get(1).size(); i++){
			Antigen focus = liverData.get(1).get(i);
			Pair[] var = new Pair[6];
			var[0] = new Pair(focus.affinity(trainedAb[0].get(0)) , 0);
			var[1] = new Pair(focus.affinity(trainedAb[0].get(1)) , 0);
			var[2] = new Pair(focus.affinity(trainedAb[0].get(2)) , 0);
			var[3] = new Pair(focus.affinity(trainedAb[1].get(0)) , 1);
			var[4] = new Pair(focus.affinity(trainedAb[1].get(1)) , 1);
			var[5] = new Pair(focus.affinity(trainedAb[1].get(2)) , 1);
			Arrays.sort(var, new Comp2());
			int count = 0;
			for(int j = 0; j < 3; j++){
				count += var[j].index;
			}
			if(count >= 2){
				S[focus.getLabel()][1] += 1;
			}
			else{
				S[focus.getLabel()][0] += 1;
			}
			
		}
		for(int i = 0; i < 2; i++){
			System.out.println(S[i][0] + " " + S[i][1]);
		}
		return (((S[0][0] + S[1][1]) / (double)(S[0][0] + S[1][1] + S[0][1] + S[1][0])) * 100.0);
	}

	public static ArrayList<ArrayList<Antigen>> prepareWine()
			throws IOException {
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "winetrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[13];
				for (int k = 0; k < 13; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[13];
				for (int k = 0; k < 13; k++)
					D[k] = (int) (10000 * X[k]);
				trainSet.add(new Antigen((new Integer(temp[0]) - 1), D, 13));
			}
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		fileName = "winetest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[13];
				for (int k = 0; k < 13; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[13];
				for (int k = 0; k < 13; k++)
					D[k] = (int) (10000 * X[k]);
				testSet.add(new Antigen((new Integer(temp[0]) - 1), D, 13));
			}
		}

		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		ans.add(trainSet);
		ans.add(testSet);
		return ans;
	}

	public static double runWine() throws IOException{
		ArrayList<ArrayList<Antigen>> wineData = prepareWine();
		Object[] temp;
		temp = new Object[] { 400, 20, 6, 7, 6, 0.02, 13, 1.0, 3, 5};
		ArrayList<Antibody>[] Ab = (ArrayList<Antibody>[]) new ArrayList[4];
		for(int i = 0; i < 4; i++){
			Ab[i] = new ArrayList<Antibody>();
		}
		int sum1, sum2, sum3, sum4, sum5, sum6, sum7, sum8, sum9, sum10, sum11, sum12, sum13;
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = sum10 = sum11 = sum12 = sum13 = 0;
		for(int i = 0; i < 23; i++){
			sum1 += wineData.get(0).get(i).returnComponent(0);
			sum2 += wineData.get(0).get(i).returnComponent(1);
			sum3 += wineData.get(0).get(i).returnComponent(2);
			sum4 += wineData.get(0).get(i).returnComponent(3);
			sum5 += wineData.get(0).get(i).returnComponent(4);
			sum6 += wineData.get(0).get(i).returnComponent(5);
			sum7 += wineData.get(0).get(i).returnComponent(6);
			sum8 += wineData.get(0).get(i).returnComponent(7);
			sum9 += wineData.get(0).get(i).returnComponent(8);
			sum10 += wineData.get(0).get(i).returnComponent(9);
			sum11 += wineData.get(0).get(i).returnComponent(10);
			sum12 += wineData.get(0).get(i).returnComponent(11);
			sum13 += wineData.get(0).get(i).returnComponent(12);
		}
		Ab[0].add(new Antibody(13, new int[] {sum1/23, sum2/23,sum3/23,sum4/23,sum5/23,sum6/23, sum7/23, sum8/23, sum9/23, sum10/23, sum11/23, sum12/23, sum13/23}));
	
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = sum10 = sum11 = sum12 = sum13 = 0;
		for(int i = 23; i < 46; i++){
			sum1 += wineData.get(0).get(i).returnComponent(0);
			sum2 += wineData.get(0).get(i).returnComponent(1);
			sum3 += wineData.get(0).get(i).returnComponent(2);
			sum4 += wineData.get(0).get(i).returnComponent(3);
			sum5 += wineData.get(0).get(i).returnComponent(4);
			sum6 += wineData.get(0).get(i).returnComponent(5);
			sum7 += wineData.get(0).get(i).returnComponent(6);
			sum8 += wineData.get(0).get(i).returnComponent(7);
			sum9 += wineData.get(0).get(i).returnComponent(8);
			sum10 += wineData.get(0).get(i).returnComponent(9);
			sum11 += wineData.get(0).get(i).returnComponent(10);
			sum12 += wineData.get(0).get(i).returnComponent(11);
			sum13 += wineData.get(0).get(i).returnComponent(12);
		}
		Ab[0].add(new Antibody(13, new int[] {sum1/23, sum2/23,sum3/23,sum4/23,sum5/23,sum6/23, sum7/23, sum8/23, sum9/23, sum10/23, sum11/23, sum12/23, sum13/23}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = sum10 = sum11 = sum12 = sum13 = 0;
		for(int i = 46; i < 69; i++){
			sum1 += wineData.get(0).get(i).returnComponent(0);
			sum2 += wineData.get(0).get(i).returnComponent(1);
			sum3 += wineData.get(0).get(i).returnComponent(2);
			sum4 += wineData.get(0).get(i).returnComponent(3);
			sum5 += wineData.get(0).get(i).returnComponent(4);
			sum6 += wineData.get(0).get(i).returnComponent(5);
			sum7 += wineData.get(0).get(i).returnComponent(6);
			sum8 += wineData.get(0).get(i).returnComponent(7);
			sum9 += wineData.get(0).get(i).returnComponent(8);
			sum10 += wineData.get(0).get(i).returnComponent(9);
			sum11 += wineData.get(0).get(i).returnComponent(10);
			sum12 += wineData.get(0).get(i).returnComponent(11);
			sum13 += wineData.get(0).get(i).returnComponent(12);
		}
		Ab[1].add(new Antibody(13, new int[] {sum1/23, sum2/23,sum3/23,sum4/23,sum5/23,sum6/23, sum7/23, sum8/23, sum9/23, sum10/23, sum11/23, sum12/23, sum13/23}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = sum10 = sum11 = sum12 = sum13 = 0;
		for(int i = 69; i < 92; i++){
			sum1 += wineData.get(0).get(i).returnComponent(0);
			sum2 += wineData.get(0).get(i).returnComponent(1);
			sum3 += wineData.get(0).get(i).returnComponent(2);
			sum4 += wineData.get(0).get(i).returnComponent(3);
			sum5 += wineData.get(0).get(i).returnComponent(4);
			sum6 += wineData.get(0).get(i).returnComponent(5);
			sum7 += wineData.get(0).get(i).returnComponent(6);
			sum8 += wineData.get(0).get(i).returnComponent(7);
			sum9 += wineData.get(0).get(i).returnComponent(8);
			sum10 += wineData.get(0).get(i).returnComponent(9);
			sum11 += wineData.get(0).get(i).returnComponent(10);
			sum12 += wineData.get(0).get(i).returnComponent(11);
			sum13 += wineData.get(0).get(i).returnComponent(12);
		}
		Ab[1].add(new Antibody(13, new int[] {sum1/23, sum2/23,sum3/23,sum4/23,sum5/23,sum6/23, sum7/23, sum8/23, sum9/23, sum10/23, sum11/23, sum12/23, sum13/23}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = sum10 = sum11 = sum12 = sum13 = 0;
		for(int i = 92; i < 115; i++){
			sum1 += wineData.get(0).get(i).returnComponent(0);
			sum2 += wineData.get(0).get(i).returnComponent(1);
			sum3 += wineData.get(0).get(i).returnComponent(2);
			sum4 += wineData.get(0).get(i).returnComponent(3);
			sum5 += wineData.get(0).get(i).returnComponent(4);
			sum6 += wineData.get(0).get(i).returnComponent(5);
			sum7 += wineData.get(0).get(i).returnComponent(6);
			sum8 += wineData.get(0).get(i).returnComponent(7);
			sum9 += wineData.get(0).get(i).returnComponent(8);
			sum10 += wineData.get(0).get(i).returnComponent(9);
			sum11 += wineData.get(0).get(i).returnComponent(10);
			sum12 += wineData.get(0).get(i).returnComponent(11);
			sum13 += wineData.get(0).get(i).returnComponent(12);
		}
		Ab[2].add(new Antibody(13, new int[] {sum1/23, sum2/23,sum3/23,sum4/23,sum5/23,sum6/23, sum7/23, sum8/23, sum9/23, sum10/23, sum11/23, sum12/23, sum13/23}));
	
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = sum10 = sum11 = sum12 = sum13 = 0;
		for(int i = 115; i < 141; i++){
			sum1 += wineData.get(0).get(i).returnComponent(0);
			sum2 += wineData.get(0).get(i).returnComponent(1);
			sum3 += wineData.get(0).get(i).returnComponent(2);
			sum4 += wineData.get(0).get(i).returnComponent(3);
			sum5 += wineData.get(0).get(i).returnComponent(4);
			sum6 += wineData.get(0).get(i).returnComponent(5);
			sum7 += wineData.get(0).get(i).returnComponent(6);
			sum8 += wineData.get(0).get(i).returnComponent(7);
			sum9 += wineData.get(0).get(i).returnComponent(8);
			sum10 += wineData.get(0).get(i).returnComponent(9);
			sum11 += wineData.get(0).get(i).returnComponent(10);
			sum12 += wineData.get(0).get(i).returnComponent(11);
			sum13 += wineData.get(0).get(i).returnComponent(12);
		}
		Ab[2].add(new Antibody(13, new int[] {sum1/23, sum2/23,sum3/23,sum4/23,sum5/23,sum6/23, sum7/23, sum8/23, sum9/23, sum10/23, sum11/23, sum12/23, sum13/23}));
	
		for(int i = 0; i < 14; i++){
			Ab[3].add(new Antibody(13, new int[]{new Random().nextInt(3000),
					new Random().nextInt(3000), 
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000),
					new Random().nextInt(3000)}));
		}
		Antigen[] passData = new Antigen[wineData.get(0).size()];
		for(int i = 0; i < wineData.get(0).size(); i++){
			passData[i] = wineData.get(0).get(i);
		}
		Trainer x = new Trainer(Ab,passData, temp);
		ArrayList<Antibody>[] trainedAb = (ArrayList<Antibody>[]) new ArrayList[3];
		trainedAb =  x.train();
		int[][] S = new int[3][3];
		for(int i = 0; i < wineData.get(1).size(); i++){
			Antigen focus = wineData.get(1).get(i);
			Pair[] var = new Pair[6];
			var[0] = new Pair(focus.affinity(trainedAb[0].get(0)) , 0);
			var[1] = new Pair(focus.affinity(trainedAb[0].get(1)) , 0);
			var[2] = new Pair(focus.affinity(trainedAb[1].get(0)) , 1);
			var[3] = new Pair(focus.affinity(trainedAb[1].get(1)) , 1);
			var[4] = new Pair(focus.affinity(trainedAb[2].get(0)) , 2);
			var[5] = new Pair(focus.affinity(trainedAb[2].get(1)) , 2);
			Arrays.sort(var, new Comp2());
			S[focus.getLabel()][var[0].index] += 1;
		}
		for(int i = 0; i < 3; i++){
			System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2]);
		}
		return (((S[0][0] + S[1][1] + S[2][2]) / (double)(S[2][0] + S[2][1] + S[2][2] + S[0][2] + S[1][2]+ S[0][0] + S[1][1] + S[0][1] + S[1][0])) * 100.0);
		
	}
	
	public static ArrayList<ArrayList<Antigen>> prepareBreastCancer()
			throws IOException {
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "breastcancertrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[9];
				for (int k = 0; k < 9; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[9];
				for (int k = 0; k < 9; k++)
					D[k] = (int) (10000 * X[k]);
				trainSet.add(new Antigen( ((new Integer(temp[0]))/2)-1 , D, 9));
			}
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		fileName = "breastcancertest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[9];
				for (int k = 0; k < 9; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[9];
				for (int k = 0; k < 9; k++)
					D[k] = (int) (10000 * X[k]);
				testSet.add(new Antigen( ((new Integer(temp[0]))/2)-1, D, 9));
			}
		}

		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		ans.add(trainSet);
		ans.add(testSet);
		return ans;
	}
	
	public static double runBreast() throws IOException{
		
		ArrayList<ArrayList<Antigen>> breastData = prepareBreastCancer();
		Object[] temp;
		temp = new Object[] { 400, 20, 6, 7, 6, 0.02, 9, 1.0, 2, 6};
		ArrayList<Antibody>[] Ab = (ArrayList<Antibody>[]) new ArrayList[3];
		for(int i = 0; i < 3; i++){
			Ab[i] = new ArrayList<Antibody>();
		}
		int sum1, sum2, sum3, sum4, sum5, sum6, sum7, sum8, sum9;
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = 0;
		for(int i = 0; i < 91; i++){
			sum1 += breastData.get(0).get(i).returnComponent(0);
			sum2 += breastData.get(0).get(i).returnComponent(1);
			sum3 += breastData.get(0).get(i).returnComponent(2);
			sum4 += breastData.get(0).get(i).returnComponent(3);
			sum5 += breastData.get(0).get(i).returnComponent(4);
			sum6 += breastData.get(0).get(i).returnComponent(5);
			sum7 += breastData.get(0).get(i).returnComponent(6);
			sum8 += breastData.get(0).get(i).returnComponent(7);
			sum9 += breastData.get(0).get(i).returnComponent(8);
			
		}
		Ab[0].add(new Antibody(9, new int[] {sum1/91, sum2/91,sum3/91,sum4/91,sum5/91,sum6/91, sum7/91, sum8/91, sum9/91}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = 0;
		for(int i = 91; i < 182; i++){
			sum1 += breastData.get(0).get(i).returnComponent(0);
			sum2 += breastData.get(0).get(i).returnComponent(1);
			sum3 += breastData.get(0).get(i).returnComponent(2);
			sum4 += breastData.get(0).get(i).returnComponent(3);
			sum5 += breastData.get(0).get(i).returnComponent(4);
			sum6 += breastData.get(0).get(i).returnComponent(5);
			sum7 += breastData.get(0).get(i).returnComponent(6);
			sum8 += breastData.get(0).get(i).returnComponent(7);
			sum9 += breastData.get(0).get(i).returnComponent(8);
			
		}
		Ab[0].add(new Antibody(9, new int[] {sum1/91, sum2/91,sum3/91,sum4/91,sum5/91,sum6/91, sum7/91, sum8/91, sum9/91}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = 0;
		for(int i = 182; i < 273; i++){
			sum1 += breastData.get(0).get(i).returnComponent(0);
			sum2 += breastData.get(0).get(i).returnComponent(1);
			sum3 += breastData.get(0).get(i).returnComponent(2);
			sum4 += breastData.get(0).get(i).returnComponent(3);
			sum5 += breastData.get(0).get(i).returnComponent(4);
			sum6 += breastData.get(0).get(i).returnComponent(5);
			sum7 += breastData.get(0).get(i).returnComponent(6);
			sum8 += breastData.get(0).get(i).returnComponent(7);
			sum9 += breastData.get(0).get(i).returnComponent(8);
			
		}
		Ab[0].add(new Antibody(9, new int[] {sum1/91, sum2/91,sum3/91,sum4/91,sum5/91,sum6/91, sum7/91, sum8/91, sum9/91}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = 0;
		for(int i = 273; i < 364; i++){
			sum1 += breastData.get(0).get(i).returnComponent(0);
			sum2 += breastData.get(0).get(i).returnComponent(1);
			sum3 += breastData.get(0).get(i).returnComponent(2);
			sum4 += breastData.get(0).get(i).returnComponent(3);
			sum5 += breastData.get(0).get(i).returnComponent(4);
			sum6 += breastData.get(0).get(i).returnComponent(5);
			sum7 += breastData.get(0).get(i).returnComponent(6);
			sum8 += breastData.get(0).get(i).returnComponent(7);
			sum9 += breastData.get(0).get(i).returnComponent(8);
			
		}
		Ab[1].add(new Antibody(9, new int[] {sum1/91, sum2/91,sum3/91,sum4/91,sum5/91,sum6/91, sum7/91, sum8/91, sum9/91}));
	
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = 0;
		for(int i = 364; i < 455; i++){
			sum1 += breastData.get(0).get(i).returnComponent(0);
			sum2 += breastData.get(0).get(i).returnComponent(1);
			sum3 += breastData.get(0).get(i).returnComponent(2);
			sum4 += breastData.get(0).get(i).returnComponent(3);
			sum5 += breastData.get(0).get(i).returnComponent(4);
			sum6 += breastData.get(0).get(i).returnComponent(5);
			sum7 += breastData.get(0).get(i).returnComponent(6);
			sum8 += breastData.get(0).get(i).returnComponent(7);
			sum9 += breastData.get(0).get(i).returnComponent(8);
			
		}
		Ab[1].add(new Antibody(9, new int[] {sum1/91, sum2/91,sum3/91,sum4/91,sum5/91,sum6/91, sum7/91, sum8/91, sum9/91}));
		
		sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = sum7 = sum8 = sum9 = 0;
		for(int i = 455; i < 547; i++){
			sum1 += breastData.get(0).get(i).returnComponent(0);
			sum2 += breastData.get(0).get(i).returnComponent(1);
			sum3 += breastData.get(0).get(i).returnComponent(2);
			sum4 += breastData.get(0).get(i).returnComponent(3);
			sum5 += breastData.get(0).get(i).returnComponent(4);
			sum6 += breastData.get(0).get(i).returnComponent(5);
			sum7 += breastData.get(0).get(i).returnComponent(6);
			sum8 += breastData.get(0).get(i).returnComponent(7);
			sum9 += breastData.get(0).get(i).returnComponent(8);
			
		}
		Ab[1].add(new Antibody(9, new int[] {sum1/92, sum2/92,sum3/92,sum4/92,sum5/92,sum6/92, sum7/92, sum8/92, sum9/92}));
		for(int i = 0; i < 14; i++){
			Ab[2].add(new Antibody(9, new int[]{new Random().nextInt(3000), new Random().nextInt(3000), new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000)}));
		}
		Antigen[] passData = new Antigen[breastData.get(0).size()];
		for(int i = 0; i < breastData.get(0).size(); i++){
			passData[i] = breastData.get(0).get(i);
		}
		Trainer x = new Trainer(Ab,passData, temp);
		ArrayList<Antibody>[] trainedAb = (ArrayList<Antibody>[]) new ArrayList[2];
		trainedAb =  x.train();
		int[][] S = new int[2][2];
		for(int i = 0; i < breastData.get(1).size(); i++){
			Antigen focus = breastData.get(1).get(i);
			Pair[] var = new Pair[6];
			var[0] = new Pair(focus.affinity(trainedAb[0].get(0)) , 0);
			var[1] = new Pair(focus.affinity(trainedAb[0].get(1)) , 0);
			var[2] = new Pair(focus.affinity(trainedAb[0].get(2)) , 0);
			var[3] = new Pair(focus.affinity(trainedAb[1].get(0)) , 1);
			var[4] = new Pair(focus.affinity(trainedAb[1].get(1)) , 1);
			var[5] = new Pair(focus.affinity(trainedAb[1].get(2)) , 1);
			Arrays.sort(var, new Comp2());
			S[focus.getLabel()][var[0].index] += 1;
		}
		for(int i = 0; i < 2; i++){
			System.out.println(S[i][0] + " " + S[i][1]);
		}
		return (((S[0][0] + S[1][1]) / (double)(S[0][0] + S[1][1] + S[0][1] + S[1][0])) * 100.0);
		
		
	}
	
	public static ArrayList<ArrayList<Antigen>> prepareEcoli()
			throws IOException {
			Hashtable<String, Integer> classifier = new Hashtable();
		classifier.put("cp", new Integer(0));
		classifier.put("im", new Integer(1));
		classifier.put("pp", new Integer(2));
		classifier.put("imU", new Integer(3));
		classifier.put("om", new Integer(4));
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "ecolitrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[7];
				for (int k = 0; k < 7; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[7];
				for (int k = 0; k < 7; k++)
					D[k] = (int) (1000 * X[k]);
				trainSet.add(new Antigen(classifier.get(temp[0]), D, 7));
			}
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		fileName = "ecolitest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				double X[] = new double[7];
				for (int k = 0; k < 7; k++)
					X[k] = new Double(temp[k + 1]);
				int D[] = new int[7];
				for (int k = 0; k < 7; k++)
					D[k] = (int) (1000 * X[k]);
				testSet.add(new Antigen(classifier.get(temp[0]), D, 7));
			}
		}

		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		ans.add(trainSet);
		ans.add(testSet);
		return ans;
	}

	public static double runEcoli() throws IOException{
		ArrayList<ArrayList<Antigen>> ecoliData = prepareEcoli();
		Object[] temp;
		temp = new Object[] { 400, 20, 10, 7, 10, 0.02, 7, 1.0, 5, 6};
		ArrayList<Antibody>[] Ab = (ArrayList<Antibody>[]) new ArrayList[6];
		for(int i = 0; i < 6; i++){
			Ab[i] = new ArrayList<Antibody>();
		}
		int sum1, sum2, sum3, sum4, sum5, sum6, sum7;
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 0; i < 28; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[0].add(new Antibody(7, new int[] {sum1/28, sum2/28,sum3/28,sum4/28,sum5/28,sum6/28, sum7/28}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 28; i < 56; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[0].add(new Antibody(7, new int[] {sum1/28, sum2/28,sum3/28,sum4/28,sum5/28,sum6/28, sum7/28}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 56; i < 84; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[0].add(new Antibody(7, new int[] {sum1/28, sum2/28,sum3/28,sum4/28,sum5/28,sum6/28, sum7/28}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 84; i < 114; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[0].add(new Antibody(7, new int[] {sum1/30, sum2/30,sum3/30,sum4/30,sum5/30,sum6/30, sum7/30}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 114; i < 144; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[1].add(new Antibody(7, new int[] {sum1/30, sum2/30,sum3/30,sum4/30,sum5/30,sum6/30, sum7/30}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 144; i < 175; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[1].add(new Antibody(7, new int[] {sum1/31, sum2/31,sum3/31,sum4/31,sum5/31,sum6/31, sum7/31}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 175; i < 204; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[3].add(new Antibody(7, new int[] {sum1/29, sum2/29,sum3/29,sum4/29,sum5/29,sum6/29, sum7/29}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 204; i < 220; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[4].add(new Antibody(7, new int[] {sum1/16, sum2/16,sum3/16,sum4/16,sum5/16,sum6/16, sum7/16}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 220; i < 240; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[2].add(new Antibody(7, new int[] {sum1/20, sum2/20,sum3/20,sum4/20,sum5/20,sum6/20, sum7/20}));
		
		sum1 = sum2 = sum3 = sum4 = sum5= sum6 = sum7 = 0;
		for(int i = 240; i < 261; i++){
			sum1 += ecoliData.get(0).get(i).returnComponent(0);
			sum2 += ecoliData.get(0).get(i).returnComponent(1);
			sum3 += ecoliData.get(0).get(i).returnComponent(2);
			sum4 += ecoliData.get(0).get(i).returnComponent(3);
			sum5 += ecoliData.get(0).get(i).returnComponent(4);
			sum6 += ecoliData.get(0).get(i).returnComponent(5);
			sum7 += ecoliData.get(0).get(i).returnComponent(6);
		}
		Ab[2].add(new Antibody(7, new int[] {sum1/21, sum2/21,sum3/21,sum4/21,sum5/21,sum6/21, sum7/21}));
		
		for(int i = 0; i < 10; i++){
			Ab[5].add(new Antibody(7, new int[]{new Random().nextInt(3000), new Random().nextInt(3000), new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000),new Random().nextInt(3000)}));
		}
		
		Antigen[] passData = new Antigen[ecoliData.get(0).size()];
		for(int i = 0; i < ecoliData.get(0).size(); i++){
			passData[i] = ecoliData.get(0).get(i);
		}
		Trainer x = new Trainer(Ab,passData, temp);
		ArrayList<Antibody>[] trainedAb = (ArrayList<Antibody>[]) new ArrayList[5];
		trainedAb =  x.train();
		int[][] S = new int[5][5];
		for(int i = 0; i < ecoliData.get(1).size(); i++){
			Antigen focus = ecoliData.get(1).get(i);
			Pair[] var = new Pair[10];
			var[0] = new Pair(focus.affinity(trainedAb[0].get(0)) , 0);
			var[1] = new Pair(focus.affinity(trainedAb[0].get(1)) , 0);
			var[2] = new Pair(focus.affinity(trainedAb[0].get(2)) , 0);
			var[3] = new Pair(focus.affinity(trainedAb[0].get(3)) , 0);
			var[4] = new Pair(focus.affinity(trainedAb[1].get(0)) , 1);
			var[5] = new Pair(focus.affinity(trainedAb[1].get(1)) , 1);
			var[6] = new Pair(focus.affinity(trainedAb[2].get(0)) , 2);
			var[7] = new Pair(focus.affinity(trainedAb[2].get(1)) , 2);
			var[8] = new Pair(focus.affinity(trainedAb[3].get(0)) , 3);
			var[9] = new Pair(focus.affinity(trainedAb[4].get(0)) , 4);
			Arrays.sort(var, new Comp2());
			S[focus.getLabel()][var[0].index] += 1;
		}
		for(int i = 0; i < 5; i++){
			System.out.println(S[i][0] + " " + S[i][1]+ " " + S[i][2]+ " " + S[i][3]+ " " + S[i][4]);
		}
		return((S[0][0] + S[1][1] + S[2][2]+ S[3][3]+ S[4][4])/(double)ecoliData.get(1).size());
		
	}
	
	public static void main(String[] args) throws IOException{
		double sum = 0;
//		for(int i = 0; i < 10; i++){
//			double temp = runiris();
//			sum += temp;
//			System.out.println(temp);
//		}
//		System.out.println("iris Avg is " + sum/10);
//		sum = 0;
//		for(int i = 0; i < 10; i++){
//			double temp = runWine();
//			sum += temp;
//			System.out.println(temp);
//		}
//		System.out.println("wine Avg is " + sum/10);
//		sum = 0;
//		for(int i = 0; i < 10; i++){
//			double temp = runLiver();
//			sum += temp;
//			System.out.println(temp);
//		}
//		System.out.println("liver Avg is " + sum/10);
//		
//		sum = 0;
//		for(int i = 0; i < 10; i++){
//			double temp = runBreast();
//			sum += temp;
//			System.out.println(temp);
//		}
//		System.out.println("Breast Avg is " + sum/10);
		
		sum = 0;
		for(int i = 0; i < 100; i++){
			double temp = runEcoli();
			sum += temp;
			System.out.println(temp);
		}
		System.out.println("Ecoli Avg is " + sum/100);
			
	}

}
