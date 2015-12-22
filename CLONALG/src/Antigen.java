
public class Antigen 
{
	
	final int[] D;
	final int L;
	public Antigen(int L,int D[])
	{
		this.L=L;
		this.D=new int[4];
		for(int i=0;i<4;i++)
			this.D[i]=D[i];
	}
	
	public double affinity(Antibody Ab)
	{
		double d=0;
		for(int i=0;i<4;i++)
			d+=(D[i]-Ab.D[i])*(D[i]-Ab.D[i]);
		return 1/(d+0.01);	// +0.01 incase diff is 0
	}
	public int getLabel()
	{
		return L;
	}
	public String toString()
	{
		return "( "+D[0]+" , "+D[1]+" , "+D[2]+" , "+D[3]+" ) : "+L;
	}
}
