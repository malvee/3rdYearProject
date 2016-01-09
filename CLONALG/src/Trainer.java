import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;


public class Trainer 
{
	final int Ngen;
	final int N;
	final int M;
	final int d;//must be >=1
	final int n;
	final double p;
	final int antibodySize;
	final int antibodyRange;
	Antibody Ab[];
	Antigen Ag[];
	public Trainer(Antigen[] Ag, int Ngen, int N, int M, int d, int n, double p, int antibodySize, int antibodyRange)
	{
		this.Ngen = Ngen;
		this.N = N;
		this.M = M;
		this.d = d;
		this.n = n;
		this.p = p;
		this.Ag=Ag;
		this.antibodySize = antibodySize;
		this.antibodyRange = antibodyRange;
		Ab = new Antibody[N];
		for(int i=0;i<N;i++)
			Ab[i]=new Antibody(antibodySize, antibodyRange);
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
					//only fill from index M till possibly end
					for(int j = 2; j < looper; j++){
						Ab[j] = R.mutatedAb[j-2];
					}
					for(int j=0;j<d;j++)
			
						Ab[indx[j]]= new Antibody(Ab[0].size, Ab[0].range);
				}
				
			}
		}
		
		
		Antibody[] rst = new Antibody[M];
		for(int i=0;i<M;i++)
			rst[i]=Ab[i];
		
		return rst;
	}

	public static double iris() throws IOException{
		String fileName = "bezdekIris.data.txt";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		Antigen[] Ag = new Antigen[150];
			for(int j=0;j<150;j++)	//assuming each class has equal number of examples
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
				int classLabel;
				if(j < 50){
					classLabel = 0;
				}	
				else if (j < 100){
					classLabel = 1;
				}
				else{
					classLabel = 2;
				}	
				Ag[j]=new Antigen(classLabel,D, 4);	// 3 classes, first 50 Antigen(0, array)
				//depends on data
			}
		scanner.close();
		Antigen[] trainSet = new Antigen[135];
		ArrayList<Integer> holder = new ArrayList<Integer>();
		ArrayList<Antigen> trainSetArrayList = new ArrayList<Antigen>();
		for(int i = 0; i < 10; i++){
			holder.add(i);
		}
		for(int i = 0; i < 9; i++){
			int temp = new Random().nextInt(holder.size());
			int startIndex = holder.get(temp);
			for(int j = startIndex*15; j < (startIndex*15)+15; j++){
				trainSetArrayList.add(Ag[j]);
			}
			holder.remove(temp);
		}
		Antigen[] testSet = new Antigen[15];
		for(int j = (holder.get(0)*15); j <  (holder.get(0)*15) + 15; j++){
			testSet[j-(holder.get(0)*15)] = Ag[j];
		}
		for(int i = 0; i < trainSetArrayList.size(); i++){
			trainSet[i] = trainSetArrayList.get(i);
		}
		
		Trainer T = new Trainer(trainSet, 400,20,3,3,10,0.01, 4, 4);
		
		Antibody[] Ab = T.train();	//all the work
		
		int S[][] = new int[3][3];
		for(int i=0;i<testSet.length;i++)
		{
			int o = testSet[i].getLabel();
			int indx=-1;
			double max=-1;
			for(int j=0;j<3;j++)
			{
				double fit = testSet[i].affinity(Ab[j]);
				if(fit>max)
				{
					max=fit;
					indx=j;
				}
			}
			S[o][indx]++;
		}
//		for(int i=0;i<3;i++)
//			System.out.println(S[i][0]+" "+S[i][1]+" "+S[i][2]);
		double hit = 0.0;
		for(int i =0; i < 3; i++){
			hit += S[i][i];
		}
		return(hit/15 *100);
	}
	public static double wine() throws IOException{
		String fileName = "wine.data.txtProcessed";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		//25 in testSet
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
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		for(int i = 0; i < Ag.length; i++){
			trainSet.add(Ag[i]);
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		for(int i = 0; i < 25; i++){
			int index = new Random().nextInt(trainSet.size());
			testSet.add(trainSet.get(index));
			trainSet.remove(index);
			
		}
		Antigen[] testData = new Antigen[25];
		for(int i = 0; i < 25; i++){
			testData[i] = testSet.get(i);
		}
		Antigen[] trainData = new Antigen[153];
		for(int i = 0; i < 153; i++){
			trainData[i] = trainSet.get(i);
		}
		Trainer T = new Trainer(trainData,400,25,3,3,8,0.01, 13, 600);
		
		Antibody[] Ab = T.train();	//all the work
		
		int S[][] = new int[3][3];
		for(int i=0;i<testData.length;i++)
		{
			int o = testData[i].getLabel();
			
			int indx=-1;
			double max=-1;
			for(int j=0;j<3;j++)
			{
				double fit = testData[i].affinity(Ab[j]);
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
		double hit = 0.0;
		for(int i =0; i < 3; i++){
			hit += S[i][i];
		}
		 return(hit/25 *100);
	}
	public static double liverDisorder() throws IOException{
		String fileName = "bupa.data.txtProcessed";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		//39 in testset
		Antigen[] Ag = new Antigen[345];
		for(int i = 0; i < Ag.length; i++){
			String str = scanner.nextLine();
			String[] temp = str.split(",");
			double X[]= new double[6];
			for(int k=0;k<temp.length-1;k++)
				X[k]=new Double(temp[k]);
			int D[] = new int[6];	// D has vals in mm
			for(int k=0;k<X.length;k++)
				D[k] = (int)(X[k]);	//change cm to mm so int
			Ag[i] = new Antigen(new Integer(temp[6])-1, D, 6);
		}
		scanner.close();
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		for(int i = 0; i < Ag.length; i++){
			trainSet.add(Ag[i]);
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		for(int i = 0; i < 39; i++){
			int index = new Random().nextInt(trainSet.size());
			testSet.add(trainSet.get(index));
			trainSet.remove(index);
			
		}
		Antigen[] testData = new Antigen[39];
		for(int i = 0; i < 39; i++){
			testData[i] = testSet.get(i);
		}
		Antigen[] trainData = new Antigen[306];
		for(int i = 0; i < 306; i++){
			trainData[i] = trainSet.get(i);
		}
		Trainer T = new Trainer(trainData,600,20,2,5,20,0.01, 6, 500);
		
		Antibody[] Ab = T.train();	//all the work
		
		int S[][] = new int[2][2];
		for(int i=0;i<testData.length;i++)
		{
			int o = testData[i].getLabel();
			
			int indx=-1;
			double max=-1;
			for(int j=0;j<2;j++)
			{
				double fit = testData[i].affinity(Ab[j]);
				if(fit>max)
				{
					max=fit;
					indx=j;
				}
			}
			S[o][indx]++;
		}
		for(int i=0;i<2;i++)
			System.out.println(S[i][0]+" "+S[i][1]);
		double hit = 0.0;
		for(int i =0; i < 2; i++){
			hit += S[i][i];
		}
		 return(hit/39 *100);
	}
	public static double ecoli() throws IOException{
		Hashtable<String, Integer> classifier = new Hashtable();
		classifier.put("cp", new Integer(0));
		classifier.put("im", new Integer(1));
		classifier.put("pp", new Integer(2));
		classifier.put("imU", new Integer(3)); 
		classifier.put("om", new Integer(4));
		classifier.put("omL", new Integer(5));
		classifier.put("imL", new Integer(6));
		classifier.put("imS", new Integer(7));
		String fileName = "ecoli.data.txt.removed";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		Antigen[] Ag = new Antigen[336];
		//39 in test set
		for(int i = 0; i < Ag.length; i++){
			String str = scanner.nextLine();
			String[] temp = str.split("\\s+");
			double X[]= new double[7];
			for(int k=0;k<temp.length-1;k++)
				X[k]=new Double(temp[k]);
			int D[] = new int[7];	// D has vals in mm
			for(int k=0;k<X.length;k++)
				D[k] = (int)(100*X[k]);	//change cm to mm so int
			Ag[i] = new Antigen(classifier.get(temp[7]), D, 7);
		}
		scanner.close();
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		for(int i = 0; i < Ag.length; i++){
			trainSet.add(Ag[i]);
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		for(int i = 0; i < 39; i++){
			int index = new Random().nextInt(trainSet.size());
			testSet.add(trainSet.get(index));
			trainSet.remove(index);
			
		}
		Antigen[] testData = new Antigen[39];
		for(int i = 0; i < 39; i++){
			testData[i] = testSet.get(i);
		}
		Antigen[] trainData = new Antigen[297];
		for(int i = 0; i < 297; i++){
			trainData[i] = trainSet.get(i);
		}
		Trainer T = new Trainer(trainData,400,20,7,3,5,0.01, 7, 50);
		
		Antibody[] Ab = T.train();	//all the work
		//number of classes
		int S[][] = new int[8][8];
		for(int i=0;i<testData.length;i++)
		{
			int o = Ag[i].getLabel();
			
			int indx=-1;
			double max=-1;
			//size of antigen
			for(int j=0;j<7;j++)
			{
				double fit = testData[i].affinity(Ab[j]);
				if(fit>max)
				{
					max=fit;
					indx=j;
				}
			}
			S[o][indx]++;
		}
		for(int i=0;i<8;i++)
			System.out.println(S[i][0]+" "+S[i][1]+" "+S[i][2]+" "+S[i][3]+" "+S[i][4]+" "+S[i][5]+" "+S[i][6]+" "+S[i][7]);
		double hit = 0.0;
		for(int i =0; i < 8; i++){
			hit += S[i][i];
		}
		return(hit/39 *100);
	}
	public static double breastCancer()  throws IOException{
		String fileName = "breast-cancer-wisconsin.data.txt";
		Scanner scanner =  new Scanner(Paths.get(fileName));
		Antigen[] Ag = new Antigen[683];
		//71 in testset
		for(int i = 0; i < Ag.length; i++){
			String str = scanner.nextLine();
			String[] temp = str.split(",");
			double X[]= new double[9];
			for(int k=0;k<temp.length-2;k++)
				X[k]=new Double(temp[k+1]);
			int D[] = new int[9];	// D has vals in mm
			for(int k=0;k<X.length;k++)
				D[k] = (int)(X[k]);	//change cm to mm so int
			Ag[i] = new Antigen( (new Integer(temp[10])/2)-1, D, 9);
		}
		scanner.close();
		ArrayList<Antigen> trainSet = new ArrayList<Antigen>();
		for(int i = 0; i < Ag.length; i++){
			trainSet.add(Ag[i]);
		}
		ArrayList<Antigen> testSet = new ArrayList<Antigen>();
		for(int i = 0; i < 71; i++){
			int index = new Random().nextInt(trainSet.size());
			testSet.add(trainSet.get(index));
			trainSet.remove(index);
			
		}
		Antigen[] testData = new Antigen[71];
		for(int i = 0; i < 71; i++){
			testData[i] = testSet.get(i);
		}
		Antigen[] trainData = new Antigen[612];
		for(int i = 0; i < 612; i++){
			trainData[i] = trainSet.get(i);
		}
		Trainer T = new Trainer(trainData,600,20,2,5,20,0.01, 9, 5);
		
		Antibody[] Ab = T.train();	//all the work
		
		int S[][] = new int[2][2];
		for(int i=0;i<testData.length;i++)
		{
			int o = testData[i].getLabel();
			
			int indx=-1;
			double max=-1;
			for(int j=0;j<2;j++)
			{
				double fit = testData[i].affinity(Ab[j]);
				if(fit>max)
				{
					max=fit;
					indx=j;
				}
			}
			S[o][indx]++;
		}
		for(int i=0;i<2;i++)
			System.out.println(S[i][0]+" "+S[i][1]);
		double hit = 0.0;
		for(int i =0; i < 2; i++){
			hit += S[i][i];
		}
		return(hit/71 *100);
	}
	public static void main(String ...args) throws IOException
	{
		double total = 0.0;
		//one cheeky detail is that antigen class label starts at 0
		for(int j = 0; j < 10; j++){
			total = 0;
			for(int i = 0; i < 10; i++){
				total += iris();
			}
			System.out.println("Iris accuracy is " + total/10);
		}
			
		
		
//		total = 0;
//		for(int i = 0; i < 10; i++){
//			total += wine();
//		}
//		System.out.println("wine accuracy is " + total/10);
		
//		total = 0;
//		for(int i = 0; i < 10; i++){
//			total += liverDisorder();
//		}
//		System.out.println("liverDisorder accuracy is " + total/10);
//		total = 0;
//		for(int i = 0; i < 10; i++){
//			total += ecoli();
//		}
//		System.out.println("ecoli accuracy is " + total/10);
		
//		total = 0;
//		for(int i = 0; i < 10; i++){
//			total += breastCancer();
//		}
//		System.out.println("breast cancer accuracy is " + total/10);

	}
}
