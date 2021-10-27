import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Used as part of the SPG project to make sure the billing system (Broadhub),
 * Clearcable (setting up SPG) and NOMS (the actual provisioning system) all
 * agree on which subscriber modems should be provisioned.  This puts out a 
 * number of CSV files easily imported into Excel to find these errors so they
 * can be fixed before SPG is installed.
 * 
 * @author bmarkham
 *
 */
public class Reconcile2 {
	// Class variables
	static String WincableModems = "modems.txt"; //A list of modems exported from the billing system
	static String BroadhubModems = "broadhub.csv";
	static String csvFilesDir = "/";  // directory (with ending forward slash) where the input and output files exist
	static String nomsFilesDir = "/";  //directory (with an ending forward slash) where the modem files from provisioning can be found
	static String ClearcableModems = "Modem_by_dhcpgroup-latest.csv";
	static String ClearcableMtas = "MTA_by_dhcpgroup-latest.csv";
	
	static ArrayList<String> nomsMacs = new ArrayList<String>();
	static ArrayList<String> broadhubMacs = new ArrayList<String>();
	static ArrayList<String> dhcpGroupMacs = new ArrayList<String>();
	
	/**
	 * 
	 * The main program
	 * 
	 * @param args  No arguments
	 */
	public static void main(String[] args) {		
		// Get the list of modem and mta provisioning files
		ArrayList<String> modemfiles = getModemFiles(nomsFilesDir);
		// Put all of the provisioned mac addresses from noms in the nomsMacs arraylist
		for (int i = 0; i < modemfiles.size(); i++) {
			getNomsMacs(nomsFilesDir+modemfiles.get(i));
		}
		getBroadhubMacs(csvFilesDir+BroadhubModems);
		getDhcpGroupMacs(csvFilesDir+ClearcableModems);
		getDhcpGroupMacs(csvFilesDir+ClearcableMtas);
		
		printCsvOne(broadhubMacs,dhcpGroupMacs,nomsMacs);
		printCsvTwo(broadhubMacs,dhcpGroupMacs,nomsMacs);
		printCsvThree(broadhubMacs,dhcpGroupMacs,nomsMacs);
		printCsvFour(broadhubMacs,dhcpGroupMacs,nomsMacs);

	}
	
	/**
	 * Checks for the files being valid, i.e. long enough to contain valid data, 
	 * not a lock file or backup file, not a directory etc.  Then adds them to the list 
	 * of files containing valid modem mac addresses. 
	 * 
	 * @param directory Location of the files from the provisioning system
	 * @return An ArrayList of Strings, each of which contains the name of a cable modem file
	 */
	public static ArrayList<String> getModemFiles(String directory) {
		File directoryPath = new File(directory);
		
		File filesList[] = directoryPath.listFiles();
		ArrayList<String> modemfiles = new ArrayList<String>();
				
		for (File file : filesList) {
			if (!file.isDirectory() && 
					(file.length() > 60) &&
					(!file.getName().contains("'")) &&
					(!file.getName().contains("lock")) &&
					(!file.getName().contains("UnprovCM"))) {
				modemfiles.add(file.getName());
			}
		}
				
		return modemfiles;
	}
	
	/**
	 * Accepts the name of a csv exported from Broadhub
	 * and scans this file for mac addresses.  Adds these
	 * to the class variable broadhubMacs.
	 *  
	 * @param filename Name of the file containing MACs from Broadhub
	 */
	public static void getBroadhubMacs(String filename) {
		FileInputStream stream = null;
        try {
            stream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String strLine;
        String lastWord="";
        try {
            while ((strLine = reader.readLine()) != null) {
            	lastWord = strLine.toLowerCase();
            	broadhubMacs.add(lastWord);
                lastWord="";            	                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	/**
	 * Grab mac addresses from a file sent by clearcable,
	 * convert to lowercase and add to the class variable dhcpGroupMacs
	 * 
	 * @param filename1 Name of the file sent by Clearcable
	 */
	public static void getDhcpGroupMacs(String filename1) {
		FileInputStream stream = null;
        try {
            stream = new FileInputStream(filename1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String strLine;
        String lastWord="";
        try {
            while ((strLine = reader.readLine()) != null) {
            	lastWord = strLine.toLowerCase();
            	dhcpGroupMacs.add(lastWord);
                lastWord="";            	                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	/**
	 *  Prints a CSV with the following fields
	 *  MacFromBroadHub, FoundInCC, FoundInNoms
	 *  mac address, 0 for no 1 for yes, 0 for no 1 for yes
	 * 
	 * 
	 * @param bh MAC addresses from Broadhub
	 * @param cc MAC addresses from Clearcable
	 * @param noms MAC addresses from provisioning system
	 */
	public static void printCsvOne(ArrayList<String> bh, ArrayList<String> cc, ArrayList<String> noms ) {
		String line = "";
		try {
		      File myObj = new File(csvFilesDir+"BH-vs-others.csv");
		      if (myObj.createNewFile()) {
		    	  try {
		    	      FileWriter myWriter = new FileWriter(csvFilesDir+"BH-vs-others.csv");
		    	      for (String mac : bh) {
		    	    	  line = mac+",";
		    	    	  if (cc.contains(mac)) {
		    	    		  line=line+"1,";
		    	    	  }
		    	    	  else {
		    	    		  line=line+"0,";
		    	    	  }
		    	    	  if (noms.contains(mac)) {
		    	    		  line=line+"1";
		    	    	  }
		    	    	  else {
		    	    		  line=line+"0";
		    	    	  }
		    	    	  myWriter.write(line);
		    	    	  myWriter.write("\n");
		    	    	  		    	    	  
		    	      }
		    	      myWriter.close();
		    	    } catch (IOException e) {
		    	      System.out.println("An error occurred.");
		    	      e.printStackTrace();
		    	    }  
		        
		      } else {
		        System.out.println("File already exists.");
		      }
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
	}
	
	
	/**
	 * Prints a CSV with the following fields
	 * MacFromCC, FoundInBH, FoundInNoms
	 * mac address, 0 for no 1 for yes, 0 for no 1 for yes
	 * 
	 * @param bh MAC addresses from Broadhub
	 * @param cc MAC addresses from Clearcable
	 * @param noms MAC addresses from provisioning system
	 */
	public static void printCsvTwo(ArrayList<String> bh, ArrayList<String> cc, ArrayList<String> noms ) {
		String line = "";
		try {
		      File myObj = new File(csvFilesDir+"CC-vs-others.csv");
		      if (myObj.createNewFile()) {
		    	  try {
		    	      FileWriter myWriter = new FileWriter(csvFilesDir+"CC-vs-others.csv");
		    	      for (String mac : cc) {
		    	    	  line = mac+",";
		    	    	  if (bh.contains(mac)) {
		    	    		  line=line+"1,";
		    	    	  }
		    	    	  else {
		    	    		  line=line+"0,";
		    	    	  }
		    	    	  if (noms.contains(mac)) {
		    	    		  line=line+"1";
		    	    	  }
		    	    	  else {
		    	    		  line=line+"0";
		    	    	  }
		    	    	  myWriter.write(line);
		    	    	  myWriter.write("\n");
		    	    	  		    	    	  
		    	      }
		    	      myWriter.close();
		    	    } catch (IOException e) {
		    	      System.out.println("An error occurred.");
		    	      e.printStackTrace();
		    	    }  
		        
		      } else {
		        System.out.println("File already exists.");
		      }
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
	}
	
	/**
	 * Prints a CSV with the following fields
	 * MacFromNoms, FoundInBH, FoundInCC
	 * mac address, 0 for no 1 for yes, 0 for no 1 for yes
	 * 
	 * @param bh MAC addresses from Broadhub
	 * @param cc MAC addresses from Clearcable
	 * @param noms MAC addresses from provisioning system
	 */
	public static void printCsvThree(ArrayList<String> bh, ArrayList<String> cc, ArrayList<String> noms ) {
		String line = "";
		try {
		      File myObj = new File(csvFilesDir+"Noms-vs-others.csv");
		      if (myObj.createNewFile()) {
		    	  try {
		    	      FileWriter myWriter = new FileWriter(csvFilesDir+"Noms-vs-others.csv");
		    	      for (String mac : noms) {
		    	    	  line = mac+",";
		    	    	  if (bh.contains(mac)) {
		    	    		  line=line+"1,";
		    	    	  }
		    	    	  else {
		    	    		  line=line+"0,";
		    	    	  }
		    	    	  if (cc.contains(mac)) {
		    	    		  line=line+"1";
		    	    	  }
		    	    	  else {
		    	    		  line=line+"0";
		    	    	  }
		    	    	  myWriter.write(line);
		    	    	  myWriter.write("\n");
		    	    	  		    	    	  
		    	      }
		    	      myWriter.close();
		    	    } catch (IOException e) {
		    	      System.out.println("An error occurred.");
		    	      e.printStackTrace();
		    	    }  
		        
		      } else {
		        System.out.println("File already exists.");
		      }
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
	}
	
			
		/**
		 *
		 * Prints a CSV with the following fields
		 * MacFromNoms, FoundInBH
		 * mac address, 0 for no 1 for yes
		 * 
		 * @param bh MAC addresses from Broadhub
		 * @param cc MAC addresses from Clearcable
		 * @param noms MAC addresses from provisioning system
		 */
		public static void printCsvFour(ArrayList<String> bh, ArrayList<String> cc, ArrayList<String> noms ) {
			String line = "";
			try {
			      File myObj = new File(csvFilesDir+"Noms-vs-broadhub.csv");
			      if (myObj.createNewFile()) {
			    	  try {
			    	      FileWriter myWriter = new FileWriter(csvFilesDir+"Noms-vs-broadhub.csv");
			    	      for (String mac : noms) {
			    	    	  line = mac+",";
			    	    	  if (bh.contains(mac)) {
			    	    		  line=line+"1";
			    	    	  }
			    	    	  else {
			    	    		  line=line+"0";
			    	    	  }
			    	    	  myWriter.write(line);
			    	    	  myWriter.write("\n");
			    	    	  		    	    	  
			    	      }
			    	      myWriter.close();
			    	    } catch (IOException e) {
			    	      System.out.println("An error occurred.");
			    	      e.printStackTrace();
			    	    }  
			        
			      } else {
			        System.out.println("File already exists.");
			      }
			    } catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
			
		}
	
	
	/**
	 * 
	 * Goes through each noms modem file and puts all the macs in nomsMacs
	 * 
	 * @param filename Name of the file containing cable modem provisioning
	 */
	public static void getNomsMacs(String filename) {
		FileInputStream stream = null;
        try {
            stream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String strLine;
        String lastWord="";
        
        try {
            while ((strLine = reader.readLine()) != null) {
            	if (strLine.contains("hardware ethernet")) {
            		int spot = strLine.indexOf("hardware ethernet");
            		String rawMac = strLine.substring(spot+18, strLine.length()-1);
            		lastWord = cleanMac(rawMac);
            		nomsMacs.add(lastWord);
                    lastWord="";
            	}                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
	
	/**
	 * 
	 * Helps put all the mac addresses in the same format for easy comparison	
	 * 
	 * @param dirtyMac The upper or mixed case MAC with colons
	 * @return the mac in all lower case with no punctuation
	 */
	public static String cleanMac(String dirtyMac) {
		String[] tokens = dirtyMac.split(":");
		String completed="";
		for (int i = 0; i < tokens.length; i++) {
			completed = completed+tokens[i];
		}
		completed=completed.toLowerCase();
		return completed;
	}		

}
