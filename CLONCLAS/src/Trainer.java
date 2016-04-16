import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Trainer {
	final int Ngen;
	final int N;
	final int M;
	final int d;// must be >=1
	final int n;
	final double p;
	final int antibodySize;
	final int antibodyRange;
	double clonalfactor;
	Antibody Ab[];
	Antigen Ag[];

	public Trainer(Antigen[] Ag, Object[] argsHolder) {
		this.Ngen = new Integer(argsHolder[0].toString());
		this.N = new Integer(argsHolder[1].toString());
		this.M = new Integer(argsHolder[2].toString());
		this.d = new Integer(argsHolder[3].toString());
		this.n = new Integer(argsHolder[4].toString());
		this.p = new Double(argsHolder[5].toString());
		this.Ag = Ag;
		this.antibodySize = new Integer(argsHolder[6].toString());
		this.antibodyRange = new Integer(argsHolder[7].toString());
		this.clonalfactor = new Double(argsHolder[8].toString());
		Ab = new Antibody[N];
		for (int i = 0; i < N; i++)
			Ab[i] = new Antibody(antibodySize, antibodyRange);
	}

	private void permuteAg() {
		for (int i = 0; i < Ag.length; i++) {
			int j = (int) (Math.random() * (Ag.length - i)) + i;
			Antigen tmp = Ag[i];
			Ag[i] = Ag[j];
			Ag[j] = tmp;
		}
	}

	public Antibody[] train() {

		for (int G = 0; G < Ngen; G++) {
			permuteAg();
			for (int i = 0; i < Ag.length; i++) {
				Result R = new List(Ab, Ag[i], n, d, M, p, clonalfactor, N)
						.doWork(); // do all the work
				Antibody B = R.Ab;
				int c = Ag[i].getLabel();
				if (Ag[i].affinity(Ab[c]) < Ag[i].affinity(B)) {
					Antibody tmp = Ab[c];
					Ab[c] = B;
					B = tmp;
				}
				// Replace part
				int indx[] = R.indicies;

				if (indx.length > 0) // incase some put d = 0
				{
					// R.mutatedAb.length might be bigger than Ab.length
					int looper = ((R.mutatedAb.length - Ab.length) > 0 ? Ab.length
							: R.mutatedAb.length);
					// only fill from index M till possibly end
					for (int j = M; j < looper; j++) {
						Ab[j] = R.mutatedAb[j - M];
					}
					for (int j = 0; j < d; j++)

						Ab[indx[j]] = new Antibody(Ab[0].size, Ab[0].range);
				}

			}
		}

		Antibody[] rst = new Antibody[M];
		for (int i = 0; i < M; i++)
			rst[i] = Ab[i];

		return rst;
	}

	public static double iris(ArrayList<Antigen> trainSet1, Object[] argsHolder)
			throws IOException {
		long start = System.nanoTime();
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			ArrayList<Antigen>[] group = (ArrayList<Antigen>[]) new ArrayList[3];
			for (int i = 0; i < 3; i++) {
				group[i] = new ArrayList<Antigen>();
			}
			for (int i = 0; i < trainSet.size(); i++) {
				group[trainSet.get(i).getLabel()].add(trainSet.get(i));
			}
			// populate testSet
			for (int i = 0; i < 3; i++) {
				int size = group[i].size();
				for (int j = 0; j < (int) (size * 0.2); j++) {
					int index = new Random().nextInt(group[i].size());
					testSet.add(group[i].get(index));
					group[i].remove(index);
				}
			}
			trainSet = new ArrayList<Antigen>();
			// populate trainSet
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < group[i].size(); j++) {
					trainSet.add(group[i].get(j));
				}
			}

	
			Antigen[] trainData = new Antigen[trainSet.size()];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[testSet.size()];
			for (int i = 0; i < testData.length; i++) {
				testData[i] = testSet.get(i);
			}
			Trainer T = new Trainer(trainData, argsHolder);
			Antibody[] Ab = T.train(); // all the work
			int S[][] = new int[3][3];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 3; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			averageAccuracy += accuracy;

		}
		System.out.println("10 runs took " + (System.nanoTime() - start));
		return averageAccuracy / 10;
		
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

	public static void runWine() throws IOException {
		
		ArrayList<ArrayList<Antigen>> wineData = prepareWine();
		ArrayList<Double> wineResult = new ArrayList<Double>();
		ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
		Object[] temp;
		temp = new Object[] { 400, 30, 3, 10, 8, 0.01, 13, 5000, 3 };
		argsHolder.add(temp);
		temp = new Object[] { 0, 30, 3, 10, 10, 0.01, 13, 5000, 3 };
		argsHolder.add(temp);
		wineResult.add(wine(wineData.get(0), argsHolder.get(0)));
		wineResult.add(wine(wineData.get(0), argsHolder.get(1)));
		for (int i = 0; i < wineResult.size(); i++) {
			System.out.println(wineResult.get(i));
		}
		Antigen[] trainData;
		Antigen[] testData;
		trainData = new Antigen[wineData.get(0).size()];
		testData = new Antigen[wineData.get(1).size()];
		for (int i = 0; i < wineData.get(0).size(); i++) {
			trainData[i] = wineData.get(0).get(i);
		}
		for (int i = 0; i < wineData.get(1).size(); i++) {
			testData[i] = wineData.get(1).get(i);
		}
		int bestParamIndex = 0;
		double maxAccuracy = 0;
		for (int i = 0; i < wineResult.size(); i++) {
			if (wineResult.get(i) > maxAccuracy) {
				maxAccuracy = wineResult.get(i);
				bestParamIndex = i;
			}
		}
		ArrayList<Double> resultRecorder = new ArrayList<Double>();
		double total = 0;
		long start = System.nanoTime();
		for (int k = 0; k < 10; k++) {
			Trainer T = new Trainer(trainData, argsHolder.get(bestParamIndex));
			Antibody[] Ab = T.train(); // all the work

			int S[][] = new int[3][3];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 3; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 3; i++) {
				System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2]);
			}
			System.out.println();
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			resultRecorder.add(accuracy);
		}
		for (int i = 0; i < 10; i++) {
			total += resultRecorder.get(i);
			System.out.println(resultRecorder.get(i));
		}
		System.out.println("Average accuracy is " + total / 10);
		System.out.println("Wine 10 Time taken " + (System.nanoTime() - start));
	}

	public static double wine(ArrayList<Antigen> trainSet1, Object[] argsHolder)
			throws IOException {
		long start = System.nanoTime();
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			ArrayList<Antigen>[] group = (ArrayList<Antigen>[]) new ArrayList[3];
			for (int i = 0; i < 3; i++) {
				group[i] = new ArrayList<Antigen>();
			}
			for (int i = 0; i < trainSet.size(); i++) {
				group[trainSet.get(i).getLabel()].add(trainSet.get(i));
			}
			// populate testSet
			for (int i = 0; i < 3; i++) {
				int size = group[i].size();
				for (int j = 0; j < (int) (size * 0.2); j++) {
					int index = new Random().nextInt(group[i].size());
					testSet.add(group[i].get(index));
					group[i].remove(index);
				}
			}
			trainSet = new ArrayList<Antigen>();
			// populate trainSet
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < group[i].size(); j++) {
					trainSet.add(group[i].get(j));
				}
			}
			Antigen[] trainData = new Antigen[trainSet.size()];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[testSet.size()];
			for (int i = 0; i < testData.length; i++) {
				testData[i] = testSet.get(i);
			}
			Trainer T = new Trainer(trainData, argsHolder);
			Antibody[] Ab = T.train(); // all the work
			int S[][] = new int[3][3];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 3; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			averageAccuracy += accuracy;
		}
		System.out.println("wine 10 runs took " + (System.nanoTime() - start));
		return averageAccuracy / 10;

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

	public static double liverDisorder(ArrayList<Antigen> trainSet1, Object[] argsHolder)
			throws IOException {
		long start = System.nanoTime();
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			ArrayList<Antigen>[] group = (ArrayList<Antigen>[]) new ArrayList[2];
			for (int i = 0; i < 2; i++) {
				group[i] = new ArrayList<Antigen>();
			}
			for (int i = 0; i < trainSet.size(); i++) {
				group[trainSet.get(i).getLabel()].add(trainSet.get(i));
			}
			// populate testSet
			for (int i = 0; i < 2; i++) {
				int size = group[i].size();
				for (int j = 0; j < (int) (size * 0.2); j++) {
					int index = new Random().nextInt(group[i].size());
					testSet.add(group[i].get(index));
					group[i].remove(index);
				}
			}
			trainSet = new ArrayList<Antigen>();
			// populate trainSet
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < group[i].size(); j++) {
					trainSet.add(group[i].get(j));
				}
			}
			Antigen[] trainData = new Antigen[trainSet.size()];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[testSet.size()];
			for (int i = 0; i < testData.length; i++) {
				testData[i] = testSet.get(i);
			}
			Trainer T = new Trainer(trainData, argsHolder);
			Antibody[] Ab = T.train(); // all the work
			int S[][] = new int[2][2];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 2; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 2; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			averageAccuracy += accuracy;
		}
		System.out.println("liver 10 runs took " + (System.nanoTime() - start));
		return averageAccuracy / 10;

	}

	public static void runLiverDisorder() throws IOException {
		
		ArrayList<ArrayList<Antigen>> liverDisorderData = prepareLiverDisorder();
		ArrayList<Double> liverDisorderResult = new ArrayList<Double>();
		ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
		Object[] temp;
		temp = new Object[] { 400, 20, 2, 5, 19, 0.11, 6, 7000, 2 };
		argsHolder.add(temp);
		temp = new Object[] {  0, 20, 2, 5, 20, 0.11, 6, 0, 2 };
		argsHolder.add(temp);
		liverDisorderResult.add(liverDisorder(liverDisorderData.get(0), argsHolder.get(0)));
		liverDisorderResult.add(liverDisorder(liverDisorderData.get(0), argsHolder.get(1)));
		for (int i = 0; i < liverDisorderResult.size(); i++) {
			System.out.println(liverDisorderResult.get(i));
		}
		Antigen[] trainData;
		Antigen[] testData;
		trainData = new Antigen[liverDisorderData.get(0).size()];
		testData = new Antigen[liverDisorderData.get(1).size()];
		for (int i = 0; i < liverDisorderData.get(0).size(); i++) {
			trainData[i] = liverDisorderData.get(0).get(i);
		}
		for (int i = 0; i < liverDisorderData.get(1).size(); i++) {
			testData[i] = liverDisorderData.get(1).get(i);
		}
		int bestParamIndex = 0;
		double maxAccuracy = 0;
		for (int i = 0; i < liverDisorderResult.size(); i++) {
			if (liverDisorderResult.get(i) > maxAccuracy) {
				maxAccuracy = liverDisorderResult.get(i);
				bestParamIndex = i;
			}
		}
		ArrayList<Double> resultRecorder = new ArrayList<Double>();
		double total = 0;
		long start = System.nanoTime();
		for (int k = 0; k < 10; k++) {
			Trainer T = new Trainer(trainData, argsHolder.get(bestParamIndex));
			Antibody[] Ab = T.train(); // all the work

			int S[][] = new int[2][2];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 2; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 2; i++) {
				System.out.println(S[i][0] + " " + S[i][1]);
			}
			System.out.println();
			for (int i = 0; i < 2; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			resultRecorder.add(accuracy);
		}
		for (int i = 0; i < 10; i++) {
			total += resultRecorder.get(i);
			System.out.println(resultRecorder.get(i));
		}
		System.out.println("Average accuracy is " + total / 10);
		System.out.println("Liver disorder Time taken " + (System.nanoTime() - start));
	}


	public static double ecoli(ArrayList<Antigen> trainSet1, Object[] argsHolder)
			throws IOException {
		long start = System.nanoTime();
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			ArrayList<Antigen>[] group = (ArrayList<Antigen>[]) new ArrayList[5];
			for (int i = 0; i < 5; i++) {
				group[i] = new ArrayList<Antigen>();
			}
			for (int i = 0; i < trainSet.size(); i++) {
				group[trainSet.get(i).getLabel()].add(trainSet.get(i));
			}
			// populate testSet
			for (int i = 0; i < 5; i++) {
				int size = group[i].size();
				for (int j = 0; j < (int) (size * 0.2); j++) {
					int index = new Random().nextInt(group[i].size());
					testSet.add(group[i].get(index));
					group[i].remove(index);
				}
			}
			trainSet = new ArrayList<Antigen>();
			// populate trainSet
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < group[i].size(); j++) {
					trainSet.add(group[i].get(j));
				}
			}
			Antigen[] trainData = new Antigen[trainSet.size()];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[testSet.size()];
			for (int i = 0; i < testData.length; i++) {
				testData[i] = testSet.get(i);
			}
			Trainer T = new Trainer(trainData, argsHolder);
			Antibody[] Ab = T.train(); // all the work
			int S[][] = new int[5][5];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 5; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 5; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			averageAccuracy += accuracy;
		}
		System.out.println("Ecoli 10 runs took " + (System.nanoTime() - start));
		return averageAccuracy / 10;

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

	public static void runEcoli() throws IOException {
		
		ArrayList<ArrayList<Antigen>> ecoliData = prepareEcoli();
		ArrayList<Double> ecoliResult = new ArrayList<Double>();
		ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
		Object[] temp;
		temp = new Object[] { 400, 12, 5, 3, 1, 0.01, 7, 40, 1};
		argsHolder.add(temp);
		temp = new Object[] { 0, 12, 5, 3, 1, 0.01, 7, 35, 2};
		argsHolder.add(temp);
		ecoliResult.add(ecoli(ecoliData.get(0), argsHolder.get(0)));
		ecoliResult.add(ecoli(ecoliData.get(0), argsHolder.get(1)));
		for (int i = 0; i < ecoliResult.size(); i++) {
			System.out.println(ecoliResult.get(i));
		}
		Antigen[] trainData;
		Antigen[] testData;
		trainData = new Antigen[ecoliData.get(0).size()];
		testData = new Antigen[ecoliData.get(1).size()];
		for (int i = 0; i < ecoliData.get(0).size(); i++) {
			trainData[i] = ecoliData.get(0).get(i);
		}
		for (int i = 0; i < ecoliData.get(1).size(); i++) {
			testData[i] = ecoliData.get(1).get(i);
		}
		int bestParamIndex = 0;
		double maxAccuracy = 0;
		for (int i = 0; i < ecoliResult.size(); i++) {
			if (ecoliResult.get(i) > maxAccuracy) {
				maxAccuracy = ecoliResult.get(i);
				bestParamIndex = i;
			}
		}
		ArrayList<Double> resultRecorder = new ArrayList<Double>();
		double total = 0;
		long start = System.nanoTime();
		for (int k = 0; k < 10; k++) {
			Trainer T = new Trainer(trainData, argsHolder.get(bestParamIndex));
			Antibody[] Ab = T.train(); // all the work

			int S[][] = new int[5][5];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 5; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 5; i++) {
				System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2] + " " + S[i][3] + " " + S[i][4]);
			}
			System.out.println();
			for (int i = 0; i < 5; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			resultRecorder.add(accuracy);
		}
		for (int i = 0; i < 10; i++) {
			total += resultRecorder.get(i);
			System.out.println(resultRecorder.get(i));
		}
		System.out.println("Average accuracy is " + total / 10);
		System.out.println("Ecoli Time taken " + (System.nanoTime() - start));
	}
	
	public static double breastCancer(ArrayList<Antigen> trainSet1, Object[] argsHolder)
			throws IOException {
		long start = System.nanoTime();
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			ArrayList<Antigen>[] group = (ArrayList<Antigen>[]) new ArrayList[2];
			for (int i = 0; i < 2; i++) {
				group[i] = new ArrayList<Antigen>();
			}
			for (int i = 0; i < trainSet.size(); i++) {
				group[trainSet.get(i).getLabel()].add(trainSet.get(i));
			}
			// populate testSet
			for (int i = 0; i < 2; i++) {
				int size = group[i].size();
				for (int j = 0; j < (int) (size * 0.2); j++) {
					int index = new Random().nextInt(group[i].size());
					testSet.add(group[i].get(index));
					group[i].remove(index);
				}
			}
			trainSet = new ArrayList<Antigen>();
			// populate trainSet
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < group[i].size(); j++) {
					trainSet.add(group[i].get(j));
				}
			}
			

			
			Antigen[] trainData = new Antigen[trainSet.size()];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[testSet.size()];
			for (int i = 0; i < testData.length; i++) {
				testData[i] = testSet.get(i);
			}
			Trainer T = new Trainer(trainData, argsHolder);
			Antibody[] Ab = T.train(); // all the work
			int S[][] = new int[2][2];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 2; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 2; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			averageAccuracy += accuracy;
		}
		System.out.println(" breast 10 runs took " + (System.nanoTime() - start));
		return averageAccuracy / 10;

	}

	public static ArrayList<ArrayList<Antigen>> prepareBreastCancer()
			throws IOException {
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "breastcancertrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		int ki = 0;
		while (scanner.hasNext()) {
			System.out.println(ki++);
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

	public static void runBreastCancer() throws IOException {
		
		ArrayList<ArrayList<Antigen>> breastCancerData = prepareBreastCancer();
		ArrayList<Double> breastCancerResult = new ArrayList<Double>();
		ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
		Object[] temp;
		temp = new Object[] {  400, 10, 2, 3, 8, 0.1, 9, 5000, 2 };
		argsHolder.add(temp);
		temp = new Object[] { 0, 10, 2, 3, 10, 0.1, 9, 5000, 2 };
		argsHolder.add(temp);
		breastCancerResult.add(breastCancer(breastCancerData.get(0), argsHolder.get(0)));
		breastCancerResult.add(breastCancer(breastCancerData.get(0), argsHolder.get(1)));
		for (int i = 0; i < breastCancerResult.size(); i++) {
			System.out.println(breastCancerResult.get(i));
		}
		Antigen[] trainData;
		Antigen[] testData;
		trainData = new Antigen[breastCancerData.get(0).size()];
		testData = new Antigen[breastCancerData.get(1).size()];
		for (int i = 0; i < breastCancerData.get(0).size(); i++) {
			trainData[i] = breastCancerData.get(0).get(i);
		}
		for (int i = 0; i < breastCancerData.get(1).size(); i++) {
			testData[i] = breastCancerData.get(1).get(i);
		}
		int bestParamIndex = 0;
		double maxAccuracy = 0;
		for (int i = 0; i < breastCancerResult.size(); i++) {
			if (breastCancerResult.get(i) > maxAccuracy) {
				maxAccuracy = breastCancerResult.get(i);
				bestParamIndex = i;
			}
		}
		ArrayList<Double> resultRecorder = new ArrayList<Double>();
		double total = 0;
		long start = System.nanoTime();
		for (int k = 0; k < 10; k++) {
			Trainer T = new Trainer(trainData, argsHolder.get(bestParamIndex));
			Antibody[] Ab = T.train(); // all the work

			int S[][] = new int[2][2];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 2; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 2; i++) {
				System.out.println(S[i][0] + " " + S[i][1]);
			}
			System.out.println();
			for (int i = 0; i < 2; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			resultRecorder.add(accuracy);
		}
		for (int i = 0; i < 10; i++) {
			total += resultRecorder.get(i);
			System.out.println(resultRecorder.get(i));
		}
		System.out.println("Average accuracy is " + total / 10);
		System.out.println("Breast Cancer 10 Time taken " + (System.nanoTime() - start));
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

	public static void runIris() throws IOException {
		
		ArrayList<ArrayList<Antigen>> irisData = prepareIris();
		ArrayList<Double> irisResult = new ArrayList<Double>();
		ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
		Object[] temp;
		temp = new Object[] { 400, 20, 3, 3, 10, 0.01, 4, 400, 1.0 };
		argsHolder.add(temp);
		temp = new Object[] { 0, 10, 3, 3, 10, 0.01, 4, 400, 1.0 };
		argsHolder.add(temp);
		irisResult.add(iris(irisData.get(0), argsHolder.get(0)));
		irisResult.add(iris(irisData.get(0), argsHolder.get(1)));
		for (int i = 0; i < irisResult.size(); i++) {
			System.out.println(irisResult.get(i));
		}
		Antigen[] trainData;
		Antigen[] testData;
		trainData = new Antigen[irisData.get(0).size()];
		testData = new Antigen[irisData.get(1).size()];
		for (int i = 0; i < irisData.get(0).size(); i++) {
			trainData[i] = irisData.get(0).get(i);
		}
		for (int i = 0; i < irisData.get(1).size(); i++) {
			testData[i] = irisData.get(1).get(i);
		}
		int bestParamIndex = 0;
		double maxAccuracy = 0;
		for (int i = 0; i < irisResult.size(); i++) {
			if (irisResult.get(i) > maxAccuracy) {
				maxAccuracy = irisResult.get(i);
				bestParamIndex = i;
			}
		}
		ArrayList<Double> resultRecorder = new ArrayList<Double>();
		double total = 0;
		long start = System.nanoTime();
		for (int k = 0; k < 10; k++) {
			Trainer T = new Trainer(trainData, argsHolder.get(bestParamIndex));
			Antibody[] Ab = T.train(); // all the work

			int S[][] = new int[3][3];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 3; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 3; i++) {
				System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2]);
			}
			System.out.println();
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / 30 * 100;
			resultRecorder.add(accuracy);
		}
		for (int i = 0; i < 10; i++) {
			total += resultRecorder.get(i);
			System.out.println(resultRecorder.get(i));
		}
		System.out.println("Average accuracy is " + total / 10);
		System.out.println("Iris 10 Time taken " + ( System.nanoTime() - start));
	}

	public static ArrayList<ArrayList<Antigen>> prepareHeart()
			throws IOException {
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		String fileName = "heartTraining.txt";
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
					D[k] = (int) (1000 * X[k]);
				trainSet.add(new Antigen((new Integer(temp[0]) - 1), D, 6));
			}
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		fileName = "heartTest.txt";
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
					D[k] = (int) (1000 * X[k]);
				testSet.add(new Antigen((new Integer(temp[0]) - 1), D, 6));
			}
		}

		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		
		ans.add(trainSet);
		ans.add(testSet);
		return ans;

	}
	
	public static double heart(ArrayList<Antigen> trainSet1, Object[] argsHolder)
			throws IOException {
		long start = System.nanoTime();
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			ArrayList<Antigen>[] group = (ArrayList<Antigen>[]) new ArrayList[3];
			for (int i = 0; i < 3; i++) {
				group[i] = new ArrayList<Antigen>();
			}
			for (int i = 0; i < trainSet.size(); i++) {
				group[trainSet.get(i).getLabel()].add(trainSet.get(i));
			}
			// populate testSet
			for (int i = 0; i < 3; i++) {
				int size = group[i].size();
				for (int j = 0; j < (int) (size * 0.2); j++) {
					int index = new Random().nextInt(group[i].size());
					testSet.add(group[i].get(index));
					group[i].remove(index);
				}
			}
			trainSet = new ArrayList<Antigen>();
			// populate trainSet
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < group[i].size(); j++) {
					trainSet.add(group[i].get(j));
				}
			}

	
			Antigen[] trainData = new Antigen[trainSet.size()];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[testSet.size()];
			for (int i = 0; i < testData.length; i++) {
				testData[i] = testSet.get(i);
			}
			Trainer T = new Trainer(trainData, argsHolder);
			Antibody[] Ab = T.train(); // all the work
			int S[][] = new int[3][3];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 3; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length * 100;
			averageAccuracy += accuracy;

		}
		System.out.println("10 runs took " + (System.nanoTime() - start));
		return averageAccuracy / 10;
		
	}
	
public static void runHeart() throws IOException {
		
		ArrayList<ArrayList<Antigen>> heartData = prepareHeart();
		ArrayList<Double> heartResult = new ArrayList<Double>();
		ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
		Object[] temp;
		temp = new Object[] { 20, 10, 3, 7, 10, 0.01, 6, 0, 1.0 };
		argsHolder.add(temp);
		heartResult.add(heart(heartData.get(0), argsHolder.get(0)));
		for (int i = 0; i < heartResult.size(); i++) {
			System.out.println(heartResult.get(i));
		}
		Antigen[] trainData;
		Antigen[] testData;
		trainData = new Antigen[heartData.get(0).size()];
		testData = new Antigen[heartData.get(1).size()];
		for (int i = 0; i < heartData.get(0).size(); i++) {
			trainData[i] = heartData.get(0).get(i);
		}
		for (int i = 0; i < heartData.get(1).size(); i++) {
			testData[i] = heartData.get(1).get(i);
		}
		int bestParamIndex = 0;
		double maxAccuracy = 0;
		for (int i = 0; i < heartResult.size(); i++) {
			if (heartResult.get(i) > maxAccuracy) {
				maxAccuracy = heartResult.get(i);
				bestParamIndex = i;
			}
		}
		ArrayList<Double> resultRecorder = new ArrayList<Double>();
		double total = 0;
		long start = System.nanoTime();
		for (int k = 0; k < 10; k++) {
			Trainer T = new Trainer(trainData, argsHolder.get(bestParamIndex));
			Antibody[] Ab = T.train(); // all the work

			int S[][] = new int[3][3];
			for (int i = 0; i < testData.length; i++) {
				int o = testData[i].getLabel();
				int indx = -1;
				double max = -1;
				for (int j = 0; j < 3; j++) {
					double fit = testData[i].affinity(Ab[j]);
					if (fit > max) {
						max = fit;
						indx = j;
					}
				}
				S[o][indx]++;
			}
			double hit = 0.0;
			for (int i = 0; i < 3; i++) {
				System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2]);
			}
			System.out.println();
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / testData.length* 100;
			resultRecorder.add(accuracy);
		}
		for (int i = 0; i < 10; i++) {
			total += resultRecorder.get(i);
			System.out.println(resultRecorder.get(i));
		}
		System.out.println("Average accuracy is " + total / 10);
		System.out.println("Heart 10 Time taken " + ( System.nanoTime() - start));
	}
	
	
	public static void main(String... args) throws IOException {
		 //runIris();
		//runWine();
		//runLiverDisorder();
		 //runEcoli();
		 //runBreastCancer();
		runHeart();
	}
}
