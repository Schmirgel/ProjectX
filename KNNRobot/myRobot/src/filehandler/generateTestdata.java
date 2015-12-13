/*
 * Klasse zur generierung von CSV Dateien.
 * 
 * Die Funktion generateData erwartet zwei Arrays mit double Werten.
 * Im ersten Array stehen die Werte  die an das KNN uebergeben werden.
 * Im zweiten Array stehen die Werte die das KNN berechnet hat.
 * 
 * Zur Zeit wird in Zeile 35 ein fester Dateiname gesetzt.
 * 
 * In der CSV-Datei werden repraesentieren die Zeilen ein Input-Output Paar
 */
package filehandler;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class generateTestdata {
	
	/*
	 *  Erzeugt eine CSV Datei wobei jede Spalte die Werte aus den Input-Output-Paaren
	 *  besteht.
	 * @param input  ArrayList<double>: Eine Arraylist aus allen Input Werten 
	 * @param output ArrayList<double>:Eine Arraylist aus allen Output Werten
	 */
	public static void generateData(List<Double> input, List<Double> output){
		String filename = "test.csv";
		
		try {
			FileOutputStream outputstream  = new FileOutputStream(filename, true);
			
			OutputStreamWriter filewriter = new OutputStreamWriter(outputstream);
			
			BufferedWriter writer = new BufferedWriter(filewriter);
			
			for(int i=0; i < input.size(); i++){
				writer.write(input.get(i).toString());
				writer.write(",");
				
			}
			
			for(int i=0; i < output.size(); i++){
				writer.write(output.get(i).toString());
				if(i != output.size()-1){
					writer.write(",");
				}
				
			}
			
			writer.write(";");
			writer.write("\n");
			
			writer.flush();
			writer.close();
			
			
		} catch (IOException e) {
			System.out.println("Error writing to file '" + filename + "'");

		}
	}
}
