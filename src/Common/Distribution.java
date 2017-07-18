package Common;

import java.util.Random;

public class Distribution {
	/**
	 * param pro
	 * return
	 */
	static Random rd;

    static {
        if( Parameter.nRandomSeed == -1 ){
            rd = new Random( System.currentTimeMillis() );
			System.out.println( "Random noise" );
        }else{
            rd = new Random( Parameter.nRandomSeed );
            System.out.println( "fixed noise" );
        }
    }
	//static Random rd = new Random(2);

	public static int nextInt(int n) {
		return rd.nextInt(n);
	}

	public static int geometric(double pro, int k) {
		pro = Math.exp(pro / k);
		double _para = pro / (1 + pro);
		double randDouble = rd.nextDouble();
		int result = 0;
		double temp;
		if (randDouble < _para) {
			temp = (Math.log(randDouble * (1 + pro)) / Math.log(pro)) - 1;
			result = (int) Math.ceil(temp);
		} else if (randDouble > _para) {
			temp = -Math.log((1 - randDouble) * (1 + pro)) / Math.log(pro);
			result = (int) Math.ceil(temp);
		} else
			result = 0;
		return result;
	//	return 0;
	}

	/**
	 * param pro: privacy budget
	 * param k  : sensitivity
	 * return
	 */
	public static double laplace(double pro, int k) {
		pro = k / pro;
		double _para = 0.5;

		double a = rd.nextDouble();
		double result = 0;
		double temp = 0;
		if (a < _para) {
			temp = pro * Math.log(2 * a);
			result = temp;
		} else if (a > _para) {
			temp = -pro * Math.log(2 - 2 * a);
			result = temp;
		} else
			result = 0;
		//iejr: for debug, in the normal experiment the line "return 0" should be commented
	//	return 0;
		return result;
	}

	public static int binomial(int m, double probability) {
		double rDouble = rd.nextDouble();
		int result = 0;
		double sum = 0;
		while (result <= m) {
			sum += Math.pow(probability, result)
					* Math.pow(1 - probability, m - result)
					* calculateFCT(m, result);
			if (sum <= rDouble)
				++result;
			else
				break;
		}
		// System.out.println(rDouble + ":" + sum);
		return result;
	}

	/**
	 * 
	 * param maxCardinality
	 * param i
	 * return
	 */
	public static int calculateFCT(int maxCardinality, int i) {
		if (i >= maxCardinality || i == 0)
			return 1;
		long num = 1;
		
		if( 2*i > maxCardinality ){
			
			for( int j = maxCardinality;j >= i + 1;j-- ){
				num *= j;
			}
			
			for(; maxCardinality - i > 1; i++){
				num /= (maxCardinality - i);
			}
			
		}else{
		
			for (int j = maxCardinality; j >= maxCardinality - i + 1; j--)
				num *= j;
			for (; i > 1; i--) {
				num /= i;
			}
			
		}
	
		return (int) (num);
	}

	/**
	 * param args
	 */
	
	public static void main(String[] args) {
		for( int i = 0;i < 100;i++ ){
			double sNoisy = Distribution.laplace( 0.2, 100 );
            System.out.println( sNoisy );
		}
	}
}
