
public class Antigen {

	final int[] D;
	final int L;
	final int size;
	public Antigen(int L, int D[], int size) {
		this.size = size;
		this.L = L;
		this.D = new int[size];
		for (int i = 0; i < size; i++)
			this.D[i] = D[i];
	}
	public double affinity(Antibody Ab) {
		double d = 0;
		for (int i = 0; i < size; i++)
			d += (D[i] - Ab.D[i]) * (D[i] - Ab.D[i]);
		return 1 / (d + 0.01); // +0.01 incase diff is 0
	}
	public int getLabel() {
		return L;
	}

	public String toString() {
		String temp = "";
		for (int i = 0; i < size; i++) {
			temp += D[i] + " ";
		}
		temp += ": " + L;
		return temp;
	}
}
