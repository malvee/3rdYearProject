public class Pair implements Cloneable {
	 Antibody Ab;
	 double Af;
	 int index;

	public Pair(Antibody Ab, double Af) {
		this.Ab = Ab;
		this.Af = Af;

	}
	public Pair(Antibody Ab, double Af, int index) {
		this.Ab = Ab;
		this.Af = Af;
		this.index = index;
	}

	public Pair clone() {
		return new Pair(Ab.clone(), Af);
	}
}