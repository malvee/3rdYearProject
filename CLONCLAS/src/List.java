import java.util.*;

class Pair implements Cloneable {
	final Antibody Ab;
	final double Af;
	final int indx;

	public Pair(Antibody Ab, double Af, int indx) {
		this.Ab = Ab;
		this.Af = Af;
		this.indx = indx;
	}

	public Pair clone() {
		return new Pair(Ab.clone(), Af, indx);
	}
}

class Result {
	int[] indicies;
	Antibody Ab;
	Antibody[] mutatedAb;
}

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

// entry point
public class List {
	Pair[] list;
	final int n;
	final int d;
	final int M;
	final double p;
	final Antigen Ag;
	int[] indxOfLowest;
	double clonalFactor;
	int N;

	public List(Antibody[] Ab, Antigen Ag, int n, int d, int M, double p,
			double clonalFactor, int N) {
		this.n = n;
		this.d = d;
		this.M = M;
		this.p = p;
		this.Ag = Ag;
		this.clonalFactor = clonalFactor;
		this.N = N;
		list = new Pair[Ab.length];
		for (int i = 0; i < list.length; i++)
			list[i] = new Pair(Ab[i], Ag.affinity(Ab[i]), i);
	}

	private void pickN() {
		Pair[] nList = new Pair[n];
		for (int i = 0; i < n; i++)
			nList[i] = list[i];
		list = nList;
	}

	private void propotionalClone() {
		ArrayList<Pair> holder = new ArrayList<Pair>();
		for (int i = 0; i < list.length; i++) {
			long count = Math.round((clonalFactor * N) / (i + 1));
			for (int j = 0; j < count; j++) {
				holder.add(list[i].clone());
			}
		}
		Pair[] holderData = new Pair[holder.size()];
		for (int i = 0; i < holderData.length; i++) {
			holderData[i] = holder.get(i);
		}
		list = holderData;
		// double sum = 0.0;
		// for(int i=0;i<n;i++)
		// sum+=list[i].Af;
		// int length = 0;
		// for(int i=0;i<n;i++)
		// {
		// length+=(int)Math.round(list[i].Af*n/sum);
		// }
		// Pair[] nList= new Pair[length]; //total number of cloned antibodies
		// to be produced
		// int k=0; //index of starting element
		// for(int i=0;i<n;i++)
		// {
		// int c = k + (int)Math.round(list[i].Af*n/sum); //c is index of last
		// element
		// for(;k<c;k++)
		// nList[k]=list[i].clone();
		// }
		// list = nList;
	}

	public void antipropotionalMutation() {
		double sum = 0.0;
		for (int i = 0; i < list.length; i++)
			sum += list[i].Af;
		for (int i = 0; i < list.length; i++)
			list[i].Ab.mutate(sum * p / (list[i].Af * n));
		for (int i = 0; i < list.length; i++)
			list[i] = new Pair(list[i].Ab, Ag.affinity(list[i].Ab),
					list[i].indx);
	}

	private void doIndxOfLowest() {
		indxOfLowest = new int[d];
		int i = 0, j = list.length - 1;
		while (i != d) {
			if (list[j].indx >= M) // only replace the ones in r
				indxOfLowest[i++] = list[j].indx;
			j--;
		}

	}

	public Result doWork() {
		Arrays.sort(list, new Comp()); // descending sort
		doIndxOfLowest();
		pickN(); // list now only contains n Pairs(antibody info, affinity with
					// current antigen, index in original antibody list)
		propotionalClone(); // list now contains the clones
		antipropotionalMutation(); // list now contains pairs of mutated clones
									// with updated afinity
		Arrays.sort(list, new Comp());
		Result rst = new Result();
		rst.Ab = list[0].Ab; // take out the best one
		rst.indicies = indxOfLowest;
		Antibody[] temp = new Antibody[list.length - 1];
		for (int i = 1; i < list.length; i++) {
			temp[i - 1] = list[i].Ab;
		}
		rst.mutatedAb = temp;
		return rst;
	}
}
