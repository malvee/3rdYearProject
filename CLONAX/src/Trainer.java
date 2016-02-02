import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
			System.out.println("In generation " + G);
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

	
	
	public static void main(String[] args) throws IOException{
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
			S[var[0].index][focus.getLabel()] += 1;
		}
		for(int i = 0; i < 3; i++){
			System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2]);
		}
	}

}
