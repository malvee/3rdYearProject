import java.util.Random;
public class Antibody implements Cloneable
{
	
	final double D[];
	final int size;
	final int range;
	public double random(double M, double S)
	{
		Random r = new Random();
		return r.nextGaussian()*S+M;
	}
	
	public Antibody(int size, int range)
	{
		this.D = new double[size];
		this.size = size;
		this.range = range;
		for(int i=0;i<size;i++)
			D[i]=Math.random()*range;
	}
	
	public void mutate(double p)
	{
		for(int i=0;i<size;i++)
		{
			double x = random(D[i],(p*D[i]+0.1)/2.0);
			if(x<=0.0){
				//System.out.println(D[i] + " " + x + "happened" + p);
				x=0.1;
			}	
			D[i]=x;
		}
	}
	
	public Antibody clone()
	{
		Antibody rst = new Antibody(size, range);
		for(int i=0;i<size;i++)
			rst.D[i]=D[i];
		
		return rst;
	}
	public String toString()
	{
		String temp = "";
		for(int i = 0; i < size; i++){
			temp += D[i] + " ";
		}
		return temp;
	}
}
