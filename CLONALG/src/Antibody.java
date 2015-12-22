import java.util.Random;
public class Antibody implements Cloneable
{
	
	final double D[] = new double[4];
	
	public double random(double M, double S)
	{
		Random r = new Random();
		return r.nextGaussian()*S+M;
	}
	
	public Antibody()
	{
		for(int i=0;i<4;i++)
			D[i]=Math.random()*10;
	}
	
	public void mutate(double p)
	{
		for(int i=0;i<4;i++)
		{
			double x = random(D[i],(p*D[i]+0.1)/2.0);
			if(x<=0.0)
				x=0.1;
			D[i]=x;
		}
	}
	
	public Antibody clone()
	{
		Antibody rst = new Antibody();
		for(int i=0;i<4;i++)
			rst.D[i]=D[i];
		
		return rst;
	}
	public String toString()
	{
		return "( "+D[0]+" , "+D[1]+" , "+D[2]+" , "+D[3]+" )";
	}
}
