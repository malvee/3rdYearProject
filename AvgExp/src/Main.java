import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;


public class Main {
	public static double affinity(int[] x, int[] y){
		double d = 0;
		for (int i = 0; i < x.length; i++)
			d += (x[i] - y[i]) * (x[i] - y[i]);
		return 1 / (d + 0.01); // +0.01 incase diff is 0
	}
	
	
	public static void prepareBreast()
			throws IOException {
		int[] ag1 = new int[9];
		int[] ag2 = new int[9];
		String fileName = "breastcancertrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				if(new Integer(temp[0]) == 2){
					ag1[0] += (int) (new Double(temp[1]) * 1000);
					ag1[1] += (int) (new Double(temp[2]) * 1000);
					ag1[2] += (int) (new Double(temp[3]) * 1000);
					ag1[3] += (int) (new Double(temp[4]) * 1000);
					ag1[4] += (int) (new Double(temp[5]) * 1000);
					ag1[5] += (int) (new Double(temp[6]) * 1000);
					ag1[6] += (int) (new Double(temp[7]) * 1000);
					ag1[7] += (int) (new Double(temp[8]) * 1000);
					ag1[8] += (int) (new Double(temp[9]) * 1000);
				}
				else{
					ag2[0] += (int) (new Double(temp[1]) * 1000);
					ag2[1] += (int) (new Double(temp[2]) * 1000);
					ag2[2] += (int) (new Double(temp[3]) * 1000);
					ag2[3] += (int) (new Double(temp[4]) * 1000);
					ag2[4] += (int) (new Double(temp[5]) * 1000);
					ag2[5] += (int) (new Double(temp[6]) * 1000);
					ag2[6] += (int) (new Double(temp[7]) * 1000);
					ag2[7] += (int) (new Double(temp[8]) * 1000);
					ag2[8] += (int) (new Double(temp[9]) * 1000);
				}
			}
		}
		for(int i = 0; i <9; i++){
			ag1[i] /= 355;
			ag2[i] /= 192;
		}
		int[][] S = new int[2][2];
		fileName = "breastcancertest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				int X[] = new int[9];
				for (int k = 0; k < 9; k++)
					X[k] = (int) (new Double(temp[k + 1]) * 1000);
				int ans = 0;
				double maxAff = 0;
				double af1 = affinity(X, ag1);
				double af2 = affinity(X, ag2);
				if(af1 > maxAff){
					maxAff = af1;
					ans = 0;
				}
				if(af2 > maxAff){
					maxAff = af2;
					ans = 1;
				}
				S[((new Integer(temp[0]) ) / 2) - 1][ans]++;
			}
		}
		double accuracy = 0;
		for(int i = 0; i < 2; i++){
			accuracy += S[i][i];
		}
		System.out.println(accuracy/ 136 * 100);
	}
	
	public static void prepareEcoli()
			throws IOException {
		Hashtable<String, Integer> classifier = new Hashtable();
		classifier.put("cp", new Integer(0));
		classifier.put("im", new Integer(1));
		classifier.put("pp", new Integer(2));
		classifier.put("imU", new Integer(3));
		classifier.put("om", new Integer(4));
		int[] ag1 = new int[7];
		int[] ag2 = new int[7];
		int[] ag3 = new int[7];
		int[] ag4 = new int[7];
		int[] ag5 = new int[7];
		String fileName = "ecolitrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				if(temp[0].compareTo("cp") == 0){
					ag1[0] += (int) (new Double(temp[1]) * 1000);
					ag1[1] += (int) (new Double(temp[2]) * 1000);
					ag1[2] += (int) (new Double(temp[3]) * 1000);
					ag1[3] += (int) (new Double(temp[4]) * 1000);
					ag1[4] += (int) (new Double(temp[5]) * 1000);
					ag1[5] += (int) (new Double(temp[6]) * 1000);
					ag1[6] += (int) (new Double(temp[7]) * 1000);
				}
				else if(temp[0].compareTo("im") == 0){
					ag2[0] += (int) (new Double(temp[1]) * 1000);
					ag2[1] += (int) (new Double(temp[2]) * 1000);
					ag2[2] += (int) (new Double(temp[3]) * 1000);
					ag2[3] += (int) (new Double(temp[4]) * 1000);
					ag2[4] += (int) (new Double(temp[5]) * 1000);
					ag2[5] += (int) (new Double(temp[6]) * 1000);
					ag2[6] += (int) (new Double(temp[7]) * 1000);
				}
				else if(temp[0].compareTo("pp") == 0){
					ag3[0] += (int) (new Double(temp[1]) * 1000);
					ag3[1] += (int) (new Double(temp[2]) * 1000);
					ag3[2] += (int) (new Double(temp[3]) * 1000);
					ag3[3] += (int) (new Double(temp[4]) * 1000);
					ag3[4] += (int) (new Double(temp[5]) * 1000);
					ag3[5] += (int) (new Double(temp[6]) * 1000);
					ag3[6] += (int) (new Double(temp[7]) * 1000);
				}
				else if(temp[0].compareTo("imU") == 0){
					ag4[0] += (int) (new Double(temp[1]) * 1000);
					ag4[1] += (int) (new Double(temp[2]) * 1000);
					ag4[2] += (int) (new Double(temp[3]) * 1000);
					ag4[3] += (int) (new Double(temp[4]) * 1000);
					ag4[4] += (int) (new Double(temp[5]) * 1000);
					ag4[5] += (int) (new Double(temp[6]) * 1000);
					ag4[6] += (int) (new Double(temp[7]) * 1000);
				}
				else if(temp[0].compareTo("om") == 0){
					ag5[0] += (int) (new Double(temp[1]) * 1000);
					ag5[1] += (int) (new Double(temp[2]) * 1000);
					ag5[2] += (int) (new Double(temp[3]) * 1000);
					ag5[3] += (int) (new Double(temp[4]) * 1000);
					ag5[4] += (int) (new Double(temp[5]) * 1000);
					ag5[5] += (int) (new Double(temp[6]) * 1000);
					ag5[6] += (int) (new Double(temp[7]) * 1000);
				}
			}
		}
		for(int i = 0; i < 7; i++){
			ag1[i] /= 114;
			ag2[i] /= 62;
			ag3[i] /= 28;
			ag4[i] /= 16;
			ag5[i] /= 41;
		}
		int[][] S = new int[5][5];
		fileName = "ecolitest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				int X[] = new int[7];
				for (int k = 0; k < 7; k++)
					X[k] = (int) (new Double(temp[k + 1]) * 1000);
				int ans = 0;
				double maxAff = 0;
				double af1 = affinity(X, ag1);
				double af2 = affinity(X, ag2);
				double af3 = affinity(X, ag3);
				double af4 = affinity(X, ag4);
				double af5 = affinity(X, ag5);
				if(af1 > maxAff){
					maxAff = af1;
					ans = 0;
				}
				if(af2 > maxAff){
					maxAff = af2;
					ans = 1;
				}
				if(af3 > maxAff){
					maxAff = af3;
					ans = 2;
				}
				if(af4 > maxAff){
					maxAff = af4;
					ans = 3;
				}
				if(af5 > maxAff){
					maxAff = af5;
					ans = 4;
				}
				S[new Integer(classifier.get(temp[0]))][ans]++;
			}
		}
		double accuracy = 0;
		for(int i = 0; i < 5; i++){
			accuracy += S[i][i];
		}
		System.out.println(accuracy/ 65 * 100);
	}
	
	
	public static void prepareWine()
			throws IOException {
		int[] ag1 = new int[13];
		int[] ag2 = new int[13];
		int[] ag3 = new int[13];
		String fileName = "winetrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				if(new Integer(temp[0]) == 1){
					ag1[0] += (int) (new Double(temp[1]) * 1000);
					ag1[1] += (int) (new Double(temp[2]) * 1000);
					ag1[2] += (int) (new Double(temp[3]) * 1000);
					ag1[3] += (int) (new Double(temp[4]) * 1000);
					ag1[4] += (int) (new Double(temp[5]) * 1000);
					ag1[5] += (int) (new Double(temp[6]) * 1000);
					ag1[6] += (int) (new Double(temp[7]) * 1000);
					ag1[7] += (int) (new Double(temp[8]) * 1000);
					ag1[8] += (int) (new Double(temp[9]) * 1000);
					ag1[9] += (int) (new Double(temp[10]) * 1000);
					ag1[10] += (int) (new Double(temp[11]) * 1000);
					ag1[11] += (int) (new Double(temp[12]) * 1000);
					ag1[12] += (int) (new Double(temp[13]) * 1000);
				}
				else if(new Integer(temp[0]) == 2){
					ag2[0] += (int) (new Double(temp[1]) * 1000);
					ag2[1] += (int) (new Double(temp[2]) * 1000);
					ag2[2] += (int) (new Double(temp[3]) * 1000);
					ag2[3] += (int) (new Double(temp[4]) * 1000);
					ag2[4] += (int) (new Double(temp[5]) * 1000);
					ag2[5] += (int) (new Double(temp[6]) * 1000);
					ag2[6] += (int) (new Double(temp[7]) * 1000);
					ag2[7] += (int) (new Double(temp[8]) * 1000);
					ag2[8] += (int) (new Double(temp[9]) * 1000);
					ag2[9] += (int) (new Double(temp[10]) * 1000);
					ag2[10] += (int) (new Double(temp[11]) * 1000);
					ag2[11] += (int) (new Double(temp[12]) * 1000);
					ag2[12] += (int) (new Double(temp[13]) * 1000);	
				}
				else{
					ag3[0] += (int) (new Double(temp[1]) * 1000);
					ag3[1] += (int) (new Double(temp[2]) * 1000);
					ag3[2] += (int) (new Double(temp[3]) * 1000);
					ag3[3] += (int) (new Double(temp[4]) * 1000);
					ag3[4] += (int) (new Double(temp[5]) * 1000);
					ag3[5] += (int) (new Double(temp[6]) * 1000);
					ag3[6] += (int) (new Double(temp[7]) * 1000);
					ag3[7] += (int) (new Double(temp[8]) * 1000);
					ag3[8] += (int) (new Double(temp[9]) * 1000);
					ag3[9] += (int) (new Double(temp[10]) * 1000);
					ag3[10] += (int) (new Double(temp[11]) * 1000);
					ag3[11] += (int) (new Double(temp[12]) * 1000);
					ag3[12] += (int) (new Double(temp[13]) * 1000);
				}
			}
		}
		for(int i = 0; i <13; i++){
			ag1[i] /= 47;
			ag2[i] /= 57;
			ag3[i] /= 37;
		}
		int[][] S = new int[3][3];
		fileName = "winetest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				int X[] = new int[13];
				for (int k = 0; k < 13; k++)
					X[k] = (int) (new Double(temp[k + 1]) * 1000);
				int ans = 0;
				double maxAff = 0;
				double af1 = affinity(X, ag1);
				double af2 = affinity(X, ag2);
				double af3 = affinity(X, ag3);
				if(af1 > maxAff){
					maxAff = af1;
					ans = 0;
				}
				if(af2 > maxAff){
					maxAff = af2;
					ans = 1;
				}
				if(af3 > maxAff){
					ans = 2;
				}
				S[new Integer(temp[0])-1][ans]++;
			}
		}
		double accuracy = 0;
		for(int i = 0; i < 3; i++){
			accuracy += S[i][i];
		}
		System.out.println(accuracy/ 36 * 100);
	}
	
	
	public static void prepareLiver()
			throws IOException {
		int[] ag1 = new int[6];
		int[] ag2 = new int[6];
		String fileName = "livertrain.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				if(new Integer(temp[0]) == 1){
					ag1[0] += (int) (new Double(temp[1]) * 1000);
					ag1[1] += (int) (new Double(temp[2]) * 1000);
					ag1[2] += (int) (new Double(temp[3]) * 1000);
					ag1[3] += (int) (new Double(temp[4]) * 1000);
					ag1[4] += (int) (new Double(temp[5]) * 1000);
					ag1[5] += (int) (new Double(temp[6]) * 1000);
				}
				else if(new Integer(temp[0]) == 2){
					ag2[0] += (int) (new Double(temp[1]) * 1000);
					ag2[1] += (int) (new Double(temp[2]) * 1000);
					ag2[2] += (int) (new Double(temp[3]) * 1000);
					ag2[3] += (int) (new Double(temp[4]) * 1000);
					ag2[4] += (int) (new Double(temp[5]) * 1000);
					ag2[5] += (int) (new Double(temp[6]) * 1000);
				}
				
			}
		}
		for(int i = 0; i <6; i++){
			ag1[i] /= 115;
			ag2[i] /= 160;
		}
		int[][] S = new int[2][2];
		fileName = "livertest.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				int X[] = new int[6];
				for (int k = 0; k < 6; k++)
					X[k] = (int) (new Double(temp[k + 1]) * 1000);
				int ans = 0;
				double maxAff = 0;
				double af1 = affinity(X, ag1);
				double af2 = affinity(X, ag2);
				if(af1 > maxAff){
					maxAff = af1;
					ans = 0;
				}
				if(af2 > maxAff){
					maxAff = af2;
					ans = 1;
				}
				
				S[new Integer(temp[0])-1][ans]++;
			}
		}
		double accuracy = 0;
		for(int i = 0; i < 2; i++){
			accuracy += S[i][i];
		}
		System.out.println(accuracy/ 69 * 100);
	}
	
	
	public static void prepareIris()
			throws IOException {
		int[] ag1 = new int[4];
		int[] ag2 = new int[4];
		int[] ag3 = new int[4];
		String fileName = "bezdekIrisTrainData.txt";
		Scanner scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				if(new Integer(temp[0]) == 1){
					ag1[0] += (int) (new Double(temp[1]) * 1000);
					ag1[1] += (int) (new Double(temp[2]) * 1000);
					ag1[2] += (int) (new Double(temp[3]) * 1000);
					ag1[3] += (int) (new Double(temp[4]) * 1000);
				}
				else if(new Integer(temp[0]) == 2){
					ag2[0] += (int) (new Double(temp[1]) * 1000);
					ag2[1] += (int) (new Double(temp[2]) * 1000);
					ag2[2] += (int) (new Double(temp[3]) * 1000);
					ag2[3] += (int) (new Double(temp[4]) * 1000);
				}
				else{
					ag3[0] += (int) (new Double(temp[1]) * 1000);
					ag3[1] += (int) (new Double(temp[2]) * 1000);
					ag3[2] += (int) (new Double(temp[3]) * 1000);
					ag3[3] += (int) (new Double(temp[4]) * 1000);
				}
			}
		}
		for(int i = 0; i <4; i++){
			ag1[i] /= 40;
			ag2[i] /= 40;
			ag3[i] /= 40;
		}
		int[][] S = new int[3][3];
		fileName = "bezdekIrisTestData.txt";
		scanner = new Scanner(Paths.get(fileName));
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			if (str.length() != 0) {
				String[] temp = str.split("\\s+");
				int X[] = new int[4];
				for (int k = 0; k < 4; k++)
					X[k] = (int) (new Double(temp[k + 1]) * 1000);
				int ans = 0;
				double maxAff = 0;
				double af1 = affinity(X, ag1);
				double af2 = affinity(X, ag2);
				double af3 = affinity(X, ag3);
				if(af1 > maxAff){
					maxAff = af1;
					ans = 0;
				}
				if(af2 > maxAff){
					maxAff = af2;
					ans = 1;
				}
				if(af3 > maxAff){
					ans = 2;
				}
				S[new Integer(temp[0])-1][ans]++;
			}
		}
		double accuracy = 0;
		for(int i = 0; i < 3; i++){
			accuracy += S[i][i];
		}
		System.out.println(accuracy/ 30 * 100);
	}
	public static void main(String[] args) throws IOException{
		prepareIris();
		prepareWine();
		prepareLiver();
		prepareEcoli();
		prepareBreast();
	}

}
