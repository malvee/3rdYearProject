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

	public static double iris(ArrayList<Antigen> trainSet1, Object[] argsHolder) throws IOException {
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			for (int i = 0; i < 12; i++) {
				int index = new Random().nextInt(trainSet.size());
				testSet.add(trainSet.get(index));
				trainSet.remove(index);
			}
			System.out.println("trainSet " + trainSet.size() + " testSet "
					+ testSet.size());
			Antigen[] trainData = new Antigen[108];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[12];
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
			double accuracy = hit / 12 * 100;
			averageAccuracy += accuracy;

		}
		return averageAccuracy / 10;

	}

	public static ArrayList<ArrayList<Antigen>> prepareWine()
			throws IOException {
		String fileName = "wine.data.txtProcessed";
		Scanner scanner = new Scanner(Paths.get(fileName));
				Antigen[] Ag = new Antigen[178];
		for (int i = 0; i < Ag.length; i++) {
			String str = scanner.nextLine();
			String[] temp = str.split(",");
			double X[] = new double[13];
			for (int k = 1; k < temp.length; k++)
				X[k - 1] = new Double(temp[k]);
			int D[] = new int[13]; // D has vals in mm
			for (int k = 0; k < X.length; k++)
				D[k] = (int) (100 * X[k]); // change cm to mm so int
			Ag[i] = new Antigen(new Integer(temp[0]) - 1, D, 13);
		}
		scanner.close();
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		for (int i = 0; i < Ag.length; i++) {
			trainSet.add(Ag[i]);
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		for (int i = 0; i < 36; i++) {
			int index = new Random().nextInt(trainSet.size());
			testSet.add(trainSet.get(index));
			trainSet.remove(index);
		}
		ArrayList<ArrayList<Antigen>> ans = new ArrayList<ArrayList<Antigen>>();
		ans.add(trainSet);
		ans.add(testSet);
		return ans;

	}
	public static void runWine() throws IOException {
		// train - 142, test - 36
			ArrayList<ArrayList<Antigen>> wineData = prepareWine();
			ArrayList<Double> wineResult = new ArrayList<Double>();
			ArrayList<Object[]> argsHolder = new ArrayList<Object[]>();
			Object[] temp;
			temp = new Object[] {400, 25, 3, 3, 8, 0.01, 13, 600, 1};
			argsHolder.add(temp);
			temp = new Object[] {600, 25, 3, 3, 8, 0.01, 13, 400, 2};
			argsHolder.add(temp);
			wineResult.add(wine(wineData.get(0), argsHolder.get(0)));
			wineResult.add(wine(wineData.get(0), argsHolder.get(1) ));
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
			for(int i = 0; i < wineResult.size(); i++){
				if(wineResult.get(i) > maxAccuracy){
					maxAccuracy = wineResult.get(i);
					bestParamIndex = i;
				}
			}
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
			for (int i = 0; i < 3; i++) {
				hit += S[i][i];
			}
			double accuracy = hit / 36 * 100;
			System.out.println(accuracy);
		}
	public static double wine(ArrayList<Antigen> trainSet1, Object[] argsHolder) throws IOException {
		double averageAccuracy = 0.0;
		for (int k = 0; k < 10; k++) {
			ArrayList<Antigen> trainSet = new ArrayList<Antigen>(trainSet1);
			ArrayList<Antigen> testSet = new ArrayList<Antigen>();
			for (int i = 0; i < 14; i++) {
				int index = new Random().nextInt(trainSet.size());
				testSet.add(trainSet.get(index));
				trainSet.remove(index);
			}
			System.out.println("trainSet " + trainSet.size() + " testSet "
					+ testSet.size());
			Antigen[] trainData = new Antigen[128];
			for (int i = 0; i < trainData.length; i++) {
				trainData[i] = trainSet.get(i);
			}
			Antigen[] testData = new Antigen[14];
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
			double accuracy = hit / 14 * 100;
			averageAccuracy += accuracy;

		}
		return averageAccuracy / 10;

	}
//	public static double liverDisorder() throws IOException {
//		String fileName = "bupa.data.txtProcessed";
//		Scanner scanner = new Scanner(Paths.get(fileName));
//		// 39 in testset
//		Antigen[] Ag = new Antigen[345];
//		for (int i = 0; i < Ag.length; i++) {
//			String str = scanner.nextLine();
//			String[] temp = str.split(",");
//			double X[] = new double[6];
//			for (int k = 0; k < temp.length - 1; k++)
//				X[k] = new Double(temp[k]);
//			int D[] = new int[6]; // D has vals in mm
//			for (int k = 0; k < X.length; k++)
//				D[k] = (int) (X[k]); // change cm to mm so int
//			Ag[i] = new Antigen(new Integer(temp[6]) - 1, D, 6);
//		}
//		scanner.close();
//		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
//		for (int i = 0; i < Ag.length; i++) {
//			trainSet.add(Ag[i]);
//		}
//		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
//		for (int i = 0; i < 39; i++) {
//			int index = new Random().nextInt(trainSet.size());
//			testSet.add(trainSet.get(index));
//			trainSet.remove(index);
//
//		}
//		Antigen[] testData = new Antigen[39];
//		for (int i = 0; i < 39; i++) {
//			testData[i] = testSet.get(i);
//		}
//		Antigen[] trainData = new Antigen[306];
//		for (int i = 0; i < 306; i++) {
//			trainData[i] = trainSet.get(i);
//		}
//		Trainer T = new Trainer(trainData, 600, 20, 2, 5, 20, 0.01, 6, 500, 1);
//
//		Antibody[] Ab = T.train(); // all the work
//
//		int S[][] = new int[2][2];
//		for (int i = 0; i < testData.length; i++) {
//			int o = testData[i].getLabel();
//
//			int indx = -1;
//			double max = -1;
//			for (int j = 0; j < 2; j++) {
//				double fit = testData[i].affinity(Ab[j]);
//				if (fit > max) {
//					max = fit;
//					indx = j;
//				}
//			}
//			S[o][indx]++;
//		}
//		// for(int i=0;i<2;i++)
//		// System.out.println(S[i][0]+" "+S[i][1]);
//		double hit = 0.0;
//		for (int i = 0; i < 2; i++) {
//			hit += S[i][i];
//		}
//		return (hit / 39 * 100);
//	}
//
//	public static double ecoli() throws IOException {
//		Hashtable<String, Integer> classifier = new Hashtable();
//		classifier.put("cp", new Integer(0));
//		classifier.put("im", new Integer(1));
//		classifier.put("pp", new Integer(2));
//		classifier.put("imU", new Integer(3));
//		classifier.put("om", new Integer(4));
//		classifier.put("omL", new Integer(5));
//		classifier.put("imL", new Integer(6));
//		classifier.put("imS", new Integer(7));
//		String fileName = "ecoli.data.txt.removed";
//		Scanner scanner = new Scanner(Paths.get(fileName));
//		Antigen[] Ag = new Antigen[336];
//		// 39 in test set
//		for (int i = 0; i < Ag.length; i++) {
//			String str = scanner.nextLine();
//			String[] temp = str.split("\\s+");
//			double X[] = new double[7];
//			for (int k = 0; k < temp.length - 1; k++)
//				X[k] = new Double(temp[k]);
//			int D[] = new int[7]; // D has vals in mm
//			for (int k = 0; k < X.length; k++)
//				D[k] = (int) (100 * X[k]); // change cm to mm so int
//			Ag[i] = new Antigen(classifier.get(temp[7]), D, 7);
//		}
//		scanner.close();
//		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
//		for (int i = 0; i < Ag.length; i++) {
//			trainSet.add(Ag[i]);
//		}
//		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
//		for (int i = 0; i < 39; i++) {
//			int index = new Random().nextInt(trainSet.size());
//			testSet.add(trainSet.get(index));
//			trainSet.remove(index);
//
//		}
//		Antigen[] testData = new Antigen[39];
//		for (int i = 0; i < 39; i++) {
//			testData[i] = testSet.get(i);
//		}
//		Antigen[] trainData = new Antigen[297];
//		for (int i = 0; i < 297; i++) {
//			trainData[i] = trainSet.get(i);
//		}
//		Trainer T = new Trainer(trainData, 600, 12, 7, 3, 1, 0.01, 7, 40, 1);
//
//		Antibody[] Ab = T.train(); // all the work
//		// number of classes
//		int S[][] = new int[8][8];
//		for (int i = 0; i < testData.length; i++) {
//			int o = Ag[i].getLabel();
//
//			int indx = -1;
//			double max = -1;
//			// size of antigen
//			for (int j = 0; j < 7; j++) {
//				double fit = testData[i].affinity(Ab[j]);
//				if (fit > max) {
//					max = fit;
//					indx = j;
//				}
//			}
//			S[o][indx]++;
//		}
//		for (int i = 0; i < 1; i++)
//			System.out.println(S[i][0] + " " + S[i][1] + " " + S[i][2] + " "
//					+ S[i][3] + " " + S[i][4] + " " + S[i][5] + " " + S[i][6]
//					+ " " + S[i][7]);
//		// System.out.println();
//		double hit = 0.0;
//		for (int i = 0; i < 8; i++) {
//			hit += S[i][i];
//		}
//		return (hit / 39 * 100);
//	}
//
//	public static double breastCancer() throws IOException {
//		String fileName = "breast-cancer-wisconsin.data.txt";
//		Scanner scanner = new Scanner(Paths.get(fileName));
//		Antigen[] Ag = new Antigen[683];
//		// 71 in testset
//		for (int i = 0; i < Ag.length; i++) {
//			String str = scanner.nextLine();
//			String[] temp = str.split(",");
//			double X[] = new double[9];
//			for (int k = 0; k < temp.length - 2; k++)
//				X[k] = new Double(temp[k + 1]);
//			int D[] = new int[9]; // D has vals in mm
//			for (int k = 0; k < X.length; k++)
//				D[k] = (int) (X[k]); // change cm to mm so int
//			Ag[i] = new Antigen((new Integer(temp[10]) / 2) - 1, D, 9);
//		}
//		scanner.close();
//		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
//		for (int i = 0; i < Ag.length; i++) {
//			trainSet.add(Ag[i]);
//		}
//		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
//		for (int i = 0; i < 71; i++) {
//			int index = new Random().nextInt(trainSet.size());
//			testSet.add(trainSet.get(index));
//			trainSet.remove(index);
//
//		}
//		Antigen[] testData = new Antigen[71];
//		for (int i = 0; i < 71; i++) {
//			testData[i] = testSet.get(i);
//		}
//		Antigen[] trainData = new Antigen[612];
//		for (int i = 0; i < 612; i++) {
//			trainData[i] = trainSet.get(i);
//		}
//		Trainer T = new Trainer(trainData, 600, 20, 2, 5, 20, 0.01, 9, 5, 1);
//
//		Antibody[] Ab = T.train(); // all the work
//
//		int S[][] = new int[2][2];
//		for (int i = 0; i < testData.length; i++) {
//			int o = testData[i].getLabel();
//
//			int indx = -1;
//			double max = -1;
//			for (int j = 0; j < 2; j++) {
//				double fit = testData[i].affinity(Ab[j]);
//				if (fit > max) {
//					max = fit;
//					indx = j;
//				}
//			}
//			S[o][indx]++;
//		}
//		for (int i = 0; i < 2; i++)
//			System.out.println(S[i][0] + " " + S[i][1]);
//		double hit = 0.0;
//		for (int i = 0; i < 2; i++) {
//			hit += S[i][i];
//		}
//		return (hit / 71 * 100);
//	}

	public static ArrayList<ArrayList<Antigen>> prepareIris()
			throws IOException {
		String fileName = "bezdekIris.data.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		Antigen[] Ag = new Antigen[150];
		for (int j = 0; j < 150; j++) // assuming each class has equal number of
										// examples
		{
			String str = scanner.nextLine();
			String[] temp = str.split(",");

			// depends on data
			double X[] = new double[4]; // X has vals in cm
			for (int k = 0; k < 4; k++)
				X[k] = new Double(temp[k]);
			int D[] = new int[4]; // D has vals in mm
			for (int k = 0; k < 4; k++)
				D[k] = (int) (10 * X[k]); // change cm to mm so int
			int classLabel;
			if (j < 50) {
				classLabel = 0;
			} else if (j < 100) {
				classLabel = 1;
			} else {
				classLabel = 2;
			}
			Ag[j] = new Antigen(classLabel, D, 4); // 3 classes, first 50
													// Antigen(0, array)
			// depends on data
		}
		scanner.close();
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		for (int i = 0; i < Ag.length; i++) {
			trainSet.add(Ag[i]);
		}

		// final testing
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		for (int i = 0; i < 30; i++) {
			int index = new Random().nextInt(trainSet.size());
			testSet.add(trainSet.get(index));
			trainSet.remove(index);
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
		temp = new Object[] {400, 20, 3, 3, 10, 0.01, 4, 4, 1.0};
		argsHolder.add(temp);
		temp = new Object[] {500, 20, 3, 4, 15, 0.1, 4, 4, 2.0};
		argsHolder.add(temp);
		irisResult.add(iris(irisData.get(0), argsHolder.get(0)));
		irisResult.add(iris(irisData.get(0), argsHolder.get(1) ));
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
		for(int i = 0; i < irisResult.size(); i++){
			if(irisResult.get(i) > maxAccuracy){
				maxAccuracy = irisResult.get(i);
				bestParamIndex = i;
			}
		}
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
		for (int i = 0; i < 3; i++) {
			hit += S[i][i];
		}
		double accuracy = hit / 30 * 100;
		System.out.println(accuracy);
	}

	public static void main(String... args) throws IOException {
		runWine();
		// one cheeky detail is that antigen class label starts at 0
		// for(int j = 0; j < 10; j++){
		// total = 0;
		// for(int i = 0; i < 10; i++){
		// total += iris();
		// }
		// System.out.println("Iris accuracy is " + total/10);
		// }
		//

		// for(int j = 0; j < 10; j++){
		// total = 0;
		// for(int i = 0; i < 10; i++){
		// total += wine();
		// }
		// System.out.println("wine accuracy is " + total/10);
		// }

		// for(int j = 0; j < 10; j++){
		// total = 0;
		// for(int i = 0; i < 10; i++){
		// total += liverDisorder();
		// }
		// System.out.println("liverDisorder accuracy is " + total/10);
		// }

		// for(int j = 0; j < 10; j++){
		// long start = System.nanoTime();
		// total = 0;
		// for(int i = 0; i < 10; i++){
		// double temp = ecoli();
		// //System.out.println(temp);
		// total += temp;
		// }
		// System.out.println("ecoli accuracy is " + total/10);
		// System.out.println("Time taken " + (System.nanoTime() - start));
		// }

		// for(int j = 0; j < 10; j++){
		// total = 0;
		// for(int i = 0; i < 10; i++){
		// total += breastCancer();
		// }
		// System.out.println("breast cancer accuracy is " + total/10);
		// }

	}
}
