import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
class Comp implements Comparator<Pair> {
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

public class List {
	Pair[] list;
	Antibody[] Ab;
	Antigen Ag;
	int n;
	double p;
	double clonalfactor;
	int N;
	int k;
	public List(Antibody[] Ab, Antigen Ag, int n, double p, double clonalfactor, int N, int k){
		this.n = n;
		this.p = p;
		this.Ag = Ag;
		this.clonalfactor = clonalfactor;
		this.N = N;
		this.k = k;
		list = new Pair[Ab.length];
		for (int i = 0; i < list.length; i++)
			list[i] = new Pair(Ab[i], Ag.affinity(Ab[i]));
	}
	private void pickN() {
		Pair[] nList = new Pair[n];
		//create median cell and compare
		for (int i = 0; i < n; i++){
			nList[i] = list[i];
		}
		int[] avgContents = new int[nList[0].Ab.size];
		int[] medianContent = new int[nList[0].Ab.size];
		for(int i = 0; i<nList[0].Ab.size ; i++){
			int sum = 0;
			int[] valueHolders = new int[n];
			for(int j = 0; j < n; j++){
				sum += nList[j].Ab.D[i];
				valueHolders[j] = nList[j].Ab.D[i];
			}
			Arrays.sort(valueHolders);
			avgContents[i] = sum/n;
			medianContent[i] = valueHolders[(int)n/2];
		}
		Antibody medianCell = new Antibody(nList[0].Ab.size, medianContent);
		Antibody avgCell = new Antibody(nList[0].Ab.size, avgContents);
		Pair[] temp = new Pair[4];
		temp[0] = nList[0];
		temp[1] = nList[1];
		temp[2] = new Pair(medianCell, Ag.affinity(medianCell));
		temp[3] = new Pair(avgCell, Ag.affinity(avgCell));
		Arrays.sort(temp, new Comp());
		nList[0] = temp[0];
		nList[1] = temp[1];
		if(nList[0].Af == temp[3].Af || nList[1].Af == temp[3].Af)
			System.out.println("Avg in place");
		list = nList;
	}
	private void propotionalClone() {
		ArrayList<Pair> holder = new ArrayList<Pair>();
		for (int i = 0; i < list.length; i++) {
			long count = Math.round((clonalfactor * N) / (i + 1));
			for (int j = 0; j < count; j++) {
				holder.add(list[i].clone());
			}
		}
		Pair[] holderData = new Pair[holder.size()];
		for (int i = 0; i < holderData.length; i++) {
			holderData[i] = holder.get(i);
		}
		list = holderData;
	}
	public void antipropotionalMutation() {
		double sum = 0.0;
		for (int i = 0; i < list.length; i++)
			sum += list[i].Af;
		for (int i = 0; i < list.length; i++)
			list[i].Ab.mutate(sum * p / (list[i].Af * n)); //list[i].Ab.mutate(sum * p / (list[i].Af * n));
		for (int i = 0; i < list.length; i++)
			list[i] = new Pair(list[i].Ab, Ag.affinity(list[i].Ab));
	}
	public Antibody[] doWork(){
		Arrays.sort(list, new Comp());
		pickN();
		propotionalClone();
		antipropotionalMutation();
		Arrays.sort(list, new Comp());
		Antibody[] temp = new Antibody[k];
		for(int i = 0; i < k; i++){
			temp[i] = list[i].Ab;
		}
		return temp;
	}

}
