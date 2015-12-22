import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;


public class Trainer 
{
	final int Ngen;
	final int N;
	final int M;
	final int d;//must be >=1
	final int n;
	final double p;
	Antibody Ab[];
	Antigen Ag[];
	public Trainer(Antigen[] Ag, int Ngen, int N, int M, int d, int n, double p)
	{
		this.Ngen = Ngen;
		this.N = N;
		this.M = M;
		this.d = d;
		this.n = n;
		this.p = p;
		this.Ag=Ag;
		Ab = new Antibody[N];
		for(int i=0;i<N;i++)
			//edit
			Ab[i]=new Antibody(13, 100);
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
					//
					for(int j = 3; j < looper; j++){
						Ab[j] = R.mutatedAb[j-3];
					}
					for(int j=0;j<d;j++)
						//edit
						Ab[indx[j]]= new Antibody(Ab[0].size, Ab[0].range);
				}
				
			}
		}
		
		
		Antibody[] rst = new Antibody[M];
		for(int i=0;i<M;i++)
			rst[i]=Ab[i];
		
		return rst;
	}
	public static void iris() throws IOException{
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
		Trainer T = new Trainer(Ag, 400,20,3,3,10,0.01);
		
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
	public static void wine() throws IOException{
		String fileName = "wine.data.txt";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		Antigen[] Ag = new Antigen[178];
		for(int i = 0; i < Ag.length; i++){
			String str = scanner.nextLine();
			String[] temp = str.split(",");
			double X[]= new double[13];
			for(int k=1;k<temp.length;k++)
				X[k-1]=new Double(temp[k]);
			int D[] = new int[13];	// D has vals in mm
			for(int k=0;k<X.length;k++)
				D[k] = (int)(100*X[k]);	//change cm to mm so int
			Ag[i] = new Antigen(new Integer(temp[0])-1, D, 13);
		}
		scanner.close();
		Trainer T = new Trainer(Ag,400,25,3,3,8,0.01);
		
		Antibody[] Ab = T.train();	//all the work
		
		int S[][] = new int[3][3];
		for(int i=0;i<Ag.length;i++)
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
	public static void main(String ...args) throws IOException
	{
		//one cheeky detail is that antigen class label starts at 0
		iris();
		wine();
	}
}
