import java.util.Scanner;
import java.io.*;

public class AirTest {
	// filename in current directory to use as data source
	// private final String filename = "airQualitySmall.txt";
	// private final String filename = "airQualitySynthetic.txt";
	// private final String filename = "airQualityModerate.txt";
	// private final String filename = "airQualityLarge.txt";
	// private final String filename = "airQualityVeryLarge.txt";
	// private final String filename = "airQualityFull.txt";
	
	public static void main(String[] args) {

		String filename = "airQualitySynthetic.txt";
		// TODO: add any initialization needed before first scan of data file
		int numReadings = 0;
		int minYear = 10000;
		int maxYear = 0;
		
		String[] foundSites = new String[100];
		int numSites = 0;
		
		String[] foundParameters = new String[100];
		int numParameters = 0;
		int[] countParameters = new int[100];

		// try-catch block for first pass through reading the data file
		try {
			Scanner filescan = new Scanner(new File(filename));		
			filescan.useDelimiter("\\t|\\n");
			
			// skip header
			filescan.nextLine();
			
			while(filescan.hasNext()) {
				
				filescan.next();
				String yearString = filescan.next();
				String site = filescan.next();
				String parameter = filescan.next();
				filescan.next();
				filescan.next();
				filescan.next();
				filescan.next();
				
				//update the number of readings
				numReadings++;
				
				//extract year from the second field
				int year = Integer.parseInt(yearString.substring(yearString.lastIndexOf("/")+1));
				minYear = Math.min(minYear, year);
				maxYear = Math.max(maxYear, year);
				
				boolean foundSite = false;
                for (int i = 0; i < foundSites.length; i++) {
                    if (foundSites[i] != null && foundSites[i].equals(site)) {
                        foundSite = true;
                    }
                }
                if (!foundSite) {
                    foundSites[numSites] = site;
                    numSites++;
                }
				
				boolean foundParameter = false;
                for (int i = 0; i < foundParameters.length; i++) {
                    if (foundParameters[i] != null && foundParameters[i].equals(parameter)) {
                        foundParameter = true;
                        countParameters[i]++;
                    }
                }
                if (!foundParameter) {
                    foundParameters[numParameters] = parameter;
                    countParameters[numParameters] = 1; 
                    numParameters++;
				}
				
			}

			// add code here to iterate over file named by "filename"

		} catch (IOException e) {System.out.println(e);};
		
		int maxCount = 0;
        int maxCountIndex = 0;
        for (int i = 0; i < numParameters; i++) {
            if (countParameters[i] > maxCount) {
                maxCount = countParameters[i];
                maxCountIndex = i;
            }
        }
		
		// after the first pass over the data file is fully processed, each
		// of the following facts should be able to be printed out for
		// testing purposes
		System.out.println("Number of readings: " + numReadings);
		System.out.println("Earliest year: " + minYear);
		System.out.println("Latest year: " + maxYear);
		System.out.println("Most readings per parameter: " + countParameters[maxCountIndex]);
		System.out.println("Number of unique sites: " + numSites);
		System.out.println("Number of unique parameters: " + numParameters);
		
		// instantiate the array of readings from the number of readings
		ReadingGDR[] readings = new ReadingGDR[numReadings];
		
		
		// try-catch block for second pass through the file
		try {
			Scanner filescan = new Scanner(new File(filename));		
			filescan.useDelimiter("\\t|\\n");
			
			filescan.nextLine();
			
			//used to tell where to input a new reading
			int index = 0;
			//QUESTION: should I change this to a for loop since I now know
			//the number of iterations? I assume yes.
			while(filescan.hasNext()) {
				String line = filescan.nextLine();
				
				int id = filescan.nextInt();
				String date = filescan.next();
				String site = filescan.next();
				String parameter = filescan.next();
				int indexValue = filescan.nextInt();
				String description = filescan.next();
				String healthAdvisory = filescan.next();
				String healthEffects = filescan.next();
				
				// Create a ReadingXXX object and populate its fields
				ReadingGDR reading = new ReadingGDR(id, date, site, parameter, 
						indexValue, description, healthAdvisory, healthEffects);
				readings[index] = reading;
				//update the index for the next iteration
				index++;
				
			}
			
		} catch (IOException e) {System.out.println(e);};
		
	}
	
	
	
}