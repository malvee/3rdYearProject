import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;


public class Trainer 
{
	final int Ngen=400;
	final int N=20;
	final int M=3;
	final int d=3;//must be >=1
	final int n=10;
	final double p=0.01;
	Antibody Ab[];
	Antigen Ag[];
	public Trainer(Antigen[] Ag)
	{
		this.Ag=Ag;
		Ab = new Antibody[N];
		for(int i=0;i<N;i++)
			Ab[i]=new Antibody(4, 10);
	}
	private void permuteAg()
	{
		for(int i=0;i<Ag.length;i++)
		{
			int j = (int)(Math.random()*(Ag.length-i))+i;
			Antigen tmp = Ag[i];
			Ag[i] = Ag[j];
			Ag[j] = tmp;
		}
	}
	public Antibody[] train()
	{
		
		for(int G=0;G<Ngen;G++)
		{
			permuteAg();
			for(int i=0;i<Ag.length;i++)
			{
				Result R = new List(Ab,Ag[i],n,d,M,p).doWork();	//do all the work
				Antibody B = R.Ab;
				int c = Ag[i].getLabel();
				if(Ag[i].affinity(Ab[c])<Ag[i].affinity(B))
				{
					Antibody tmp = Ab[c];
					Ab[c]=B;
					B=tmp;					
				}
				//Replace part
				int indx[] = R.indicies;
				
				if(indx.length>0)	//incase some put d = 0
				{
					//R.mutatedAb.length might be bigger than Ab.length
					int looper = ((R.mutatedAb.length - Ab.length) > 0 ? Ab.length : R.mutatedAb.length ) ;
					//only fill from index 3 till possibly end
					for(int j = 3; j < looper; j++){
						Ab[j] = R.mutatedAb[j-3];
					}
					for(int j=0;j<d;j++)
						Ab[indx[j]]= new Antibody(4, 10);
				}
				
			}
		}
		
		
		Antibody[] rst = new Antibody[M];
		for(int i=0;i<M;i++)
			rst[i]=Ab[i];
		
		return rst;
	}

	public static void main(String ...args) throws IOException
	{
		String fileName = "bezdekIris.data.txt";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		Antigen[] Ag = new Antigen[150];
		for(int i=0;i<3;i++)	// i < M
			for(int j=0;j<50;j++)	//assuming each class has equal number of examples
			{
				String str = scanner.nextLine();
				String[] temp = str.split(",");
				
				//depends on data
				double X[]= new double[4]; // X has vals in cm
				for(int k=0;k<4;k++)
					X[k]=new Double(temp[k]);
				int D[] = new int[4];	// D has vals in mm
				for(int k=0;k<4;k++)
					D[k] = (int)(10*X[k]);	//change cm to mm so int
				Ag[i*50+j]=new Antigen(i,D, 4);	// 3 classes, first 50 Antigen(0, array)
				//depends on data
			}
		scanner.close();
		Trainer T = new Trainer(Ag);
		
		Antibody[] Ab = T.train();	//all the work
		
		int S[][] = new int[3][3];
		for(int i=0;i<150;i++)
		{
			int o = Ag[i].getLabel();
			int indx=-1;
			double max=-1;
			for(int j=0;j<3;j++)
			{
				double fit = Ag[i].affinity(Ab[j]);
				if(fit>max)
				{
					max=fit;
					indx=j;
				}
			}
			S[o][indx]++;
		}
		for(int i=0;i<3;i++)
			System.out.println(S[i][0]+" "+S[i][1]+" "+S[i][2]);
		
	}
}
