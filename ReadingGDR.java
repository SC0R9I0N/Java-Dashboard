// ReadingGDR		Author: Garrett Reihner
// 
// An object class designed to hold data entries about daily air quality in 
// Western PA neighborhoods and provides methods to extract specifically the
// description

public class ReadingGDR {
	private int id; // id number of the entry
	private int year; // year of the entry
	private String site; // neighborhood where the data was collected
	private String parameter; // Air quality or temperature measured
	private int indexValue; // air quality index of the instance
	private String description; // description (good, mdoerate, unhealthy)
	private String healthAdvisory; // notes on any health advisory
	private String healthEffects; // notes on any health effects
	
	public ReadingGDR(int numId, int dateEntry, String location, String para,
			int index, String descr, String advisory, String effects) {
		
		id = numId;
		year = dateEntry;
		site = location;
		parameter = para;
		indexValue = index;
		// handles the potential for text file to contain more than just
		// "Unhealthy" values
		if (descr.equals("Unhealthy for Sensitive Groups") || 
				descr.equals("Very Unhealthy")) {
			description = "Unhealthy";
		}else {
			description = descr;
		}
		healthAdvisory = advisory;
		healthEffects = effects;		
	}
	
	// used to filter data based on the description
	public String getDescription() {
		return description;
	}
	
	// used to filter data based on parameter
	public String getParameter() {
		return parameter;
	}
	
	// used to filter data based on site
	public String getSite() {
		return site;
	}
	
	// used to filter data based on year
	public int getYear() {
		return year;
	}
	
}