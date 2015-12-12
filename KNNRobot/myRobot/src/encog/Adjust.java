package encog;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork ;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;


public class Adjust {

	private static double distance;
	private static double outputDistance;
	
	//The input
	public static double HIT_RESULT[][] = {{-1},{0},{1}};
	public static double GUNINPUT[][] = {{distance}};

	//The ideal data
	public static double ADJUST[][] =	{{1},{0},{-1}};
	public static double GUNOUTPUT[][] = {{outputDistance}};

	
	private Adjust() {

		
		
		
	}
	
	
	//The main method.
//	public static void main(String[] args) {
	public static BasicNetwork trainNetwork(){
		
		//create a neural network, without using a factory
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(1));
		network.addLayer(new BasicLayer(3));
		network.addLayer(new BasicLayer(1));
		network.getStructure().finalizeStructure();
		network.reset();
		//create training data
		MLDataSet trainingSet = new BasicMLDataSet(HIT_RESULT, ADJUST);
		
		
		
		//train the neural network
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);
		
		int epoch = 1;
		do{
			train.iteration();
			System.out.println("Epoch #" + epoch + "Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01);
		train.finishTraining();
		
		//test the neural network
		System.out.println("Neural Network Results: ");
		for(MLDataPair pair : trainingSet){
			final MLData output = network.compute(pair.getInput());
			System.out.println(	pair.getInput().getData(0) + ", actual=" +
								output.getData(0) + ",ideal=" +
								pair.getIdeal().getData(0));
		}	

/*		
		MLData input = new BasicMLData(TEST);
		MLData output2 = network.compute(input);
		output2 = network.compute(input);
		System.out.println(output2.getData(0));
*/
		return network;
//		double[] wert = {0};
//		predict(network, wert);
//		Encog.getInstance().shutdown();		
	
//		CalculateScore score = new TrainingSetScore(trainingSet);
		
	}
	
	public static double predict(BasicNetwork network, double[] wert){
		
		MLData input = new BasicMLData(wert);
		MLData output2 = network.compute(input);
		output2 = network.compute(input);
		System.out.println(output2.getData(0));
		return output2.getData(0);
		
	}
	
	

}
