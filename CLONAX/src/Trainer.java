import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

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
		for(int i = 0; i < Ag.length; i++){
			AgSegmentedByClass[Ag[i].getLabel()].add(Ag[i]);
		}
		for(int G = 0; G < Ngen; G++){
			permuteAg();
			for(int i = 0; i < Ag.length; i++){
				int label = Ag[i].getLabel();
				Antibody[] temp = new Antibody[Ab[label].size() + N - M];
				int x;
				for(x = 0; x < Ab[label].size(); x++){
					temp[x] =  Ab[label].get(x);
				}
				for(; x < (N-M); x++){
					temp[x] = Ab[numOfClasses].get(x);
				}
				Antibody[] R = new List(temp, Ag[i], n, p, clonalfactor, N, k).doWork(); // do all the work
				
				//selectFromOtherClass contains 1 Ag from each other class
				Antigen[] selectFromOtherClass = new Antigen[numOfClasses-1];
				for(int j = 0; j < numOfClasses-1; j++){
					if (j == Ag[i].getLabel()){
						j--;
						continue;
					}
					selectFromOtherClass[j] = AgSegmentedByClass[j].get(new Random().nextInt(AgSegmentedByClass[j].size())); 	
				}
				//calculate avg affinity
				Pair[] avgAffinity = new Pair[k];
				for(int j =0; j < k; j++){
					double sum = 0;
					for(int l = 0; l < 4; l++){
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
				
				Pair[] currrentMemoryPool = new Pair[Ab[Ag[i].getLabel()].size()];
				for(int j = 0 ; j < Ab[Ag[i].getLabel()].size(); j++){
					currrentMemoryPool[j].Ab = Ab[Ag[i].getLabel()].get(j);
					currrentMemoryPool[j].Af = Ag[i].affinity(Ab[Ag[i].getLabel()].get(j));
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
				Pair[] lowestD = new Pair[d];
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
	
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
