import java.util.Random;


public class Antibody implements Cloneable{
	final int D[];
	final int size;
	public double random(double M, double S) {
		Random r = new Random();
		return r.nextGaussian() * S + M;
	}

	public Antibody(int size, int[] contents) { //length of contents is size
		this.D = new int[size];
		this.size = size;
		for (int i = 0; i < size; i++)
			D[i] = contents[i];
	}
	
	public void mutate(double p) {
		for (int i = 0; i < size; i++) {
			double x = random(D[i], (p * D[i] + 0.1) / 2.0);
			if (x <= 0.0) {
				x = 1;
			}
			D[i] = (int) x;
		}
	}
	
	public Antibody clone() {
		Antibody rst = new Antibody(size, D);
		return rst;
	}
	
	public String toString() {
		String temp = "";
		for (int i = 0; i < size; i++) {
			temp += D[i] + " ";
		}
		return temp;
	}
	
}
