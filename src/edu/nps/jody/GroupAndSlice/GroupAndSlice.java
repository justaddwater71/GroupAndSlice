package edu.nps.jody.GroupAndSlice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import java.util.Iterator;

/**
 * @author jody
 *
 */
public class GroupAndSlice 
{
	//Data Members
	public static final String 	PATH_DELIM 			= System.getProperty("path.separator");
	public static final String 	PREDICT_FOLDER 	= "predict";
	public static final String 	TRAIN_FOLDER		= "train";
	public static final int		MIN_SLICES 			= 2;
	
	public static final int		GROUP_SMALL_TO_LARGE		= 0;
	public static final int		GROUP_SMALL_AND_LARGE 	= 1;
	public static final int		GROUP_RANDOM_SIZE				= 2;
	
	public static final String		GROUP_SMALL_TO_LARGE_NAME		= "aaaSmallToLarge";
	public static final String		GROUP_SMALL_AND_LARGE_NAME 	= "aaaSmallAndLarge";
	public static final String		GROUP_RANDOM_SIZE_NAME				= "aaaRandom";
	
	//Constructors
	
	
	//Methods
	public static void groupAndSlicePrep(String sourceDirectoryName,  int groupType, int groupSize, int titleDigits, int crossValidationNumber) throws IOException
	{
		List<File> fileList = getFiles(sourceDirectoryName);
		
		String destinationDirectoryName = null;
		
		//TODO seems like an enum could make this much more straightforward, but it seems like the same work, just in a different file
		switch (groupType)
			{
			case(GROUP_SMALL_TO_LARGE):
			{
				destinationDirectoryName = sourceDirectoryName + PATH_DELIM + GROUP_SMALL_TO_LARGE_NAME + PATH_DELIM + groupSize;
				break;
			}
			case(GROUP_SMALL_AND_LARGE):
			{
				destinationDirectoryName = sourceDirectoryName + PATH_DELIM + GROUP_SMALL_AND_LARGE_NAME + PATH_DELIM + groupSize;
				break;
			}
			case(GROUP_RANDOM_SIZE):
			{
				destinationDirectoryName = sourceDirectoryName + PATH_DELIM + GROUP_RANDOM_SIZE + PATH_DELIM + groupSize;
				break;
			}
			default:
			{
				System.out.println("Invalid group type selection.");
				return;
			}
		}
		
		File destinationDirectory 	= new File(destinationDirectoryName);
		
		if( !destinationDirectory.isDirectory())
		{
			destinationDirectory.mkdirs();
		}
		
		switch (groupType)
		{
			case GROUP_SMALL_TO_LARGE:
			{
				sortBySize(fileList);
			}
			case GROUP_SMALL_AND_LARGE:
			{
				fileList = smallAndLargeProcess(fileList, groupSize);
			}
			case GROUP_RANDOM_SIZE:
			{
				Collections.shuffle(fileList);
			}
		}
	
		
		processFiles(fileList, destinationDirectory, groupSize, titleDigits, crossValidationNumber);
	}

	public static void groupAndSlicePrep(String sourceDirectoryName, String destinationDirectoryName, int groupType,  int groupSize, int titleDigits, int crossValidationNumber) throws IOException
	{
		File destinationDirectory 	= new File(destinationDirectoryName + PATH_DELIM + groupSize);
		
		List<File> fileList = getFiles(sourceDirectoryName);
		
		if( !destinationDirectory.isDirectory())
		{
			destinationDirectory.mkdirs();
		}
		
		switch (groupType)
		{
			case GROUP_SMALL_TO_LARGE:
			{
				sortBySize(fileList);
			}
			case GROUP_SMALL_AND_LARGE:
			{
				fileList = smallAndLargeProcess(fileList, groupSize);
			}
			case GROUP_RANDOM_SIZE:
			{
				Collections.shuffle(fileList);
			}
		}
		
		processFiles(fileList, destinationDirectory, groupSize, titleDigits, crossValidationNumber);
	}

	public static void processFiles(List<File> fileList, File destinationDirectory, int groupSize, int zeroes,  int crossValidationNumber) throws IOException
	{
		Iterator<File> iterator = fileList.iterator();
		List<File> printList = new Vector<File>();
		String outputFileName;
		
		int count = 0;
		
		//If crossvalidation is selected, loop to build train and predict files
		if (crossValidationNumber>0)
		{
			while (iterator.hasNext())
			{
				if (count != 0 && count%groupSize == 0)
				{
					outputFileName = intToStringWithLeadingZeroes(count - groupSize, zeroes) + 
						"_" + 
							intToStringWithLeadingZeroes(count  - 1, zeroes);
					
					roundRobinFiles(printList, outputFileName, destinationDirectory, crossValidationNumber);
					printList.clear();
				}
				
				printList.add(iterator.next());
				count++;	
			}
			
			//catch the last group missed by the previous while (happens due to the way count is incremented)
			outputFileName = intToStringWithLeadingZeroes(count - groupSize, zeroes) + 
			"_" + 
				intToStringWithLeadingZeroes(count  - 1, zeroes);
		
			roundRobinFiles(printList, outputFileName, destinationDirectory, crossValidationNumber);
			
		}
		//else just cat the files together
		else
		{
			while (iterator.hasNext())
			{
				if (count != 0 && count%groupSize == 0)
				{
					outputFileName = intToStringWithLeadingZeroes(count - groupSize, zeroes) + 
						"_" + 
							intToStringWithLeadingZeroes(count  - 1, zeroes);
					
					catFiles(printList, outputFileName,destinationDirectory);
					printList.clear();
				}
				
				printList.add(iterator.next());
				count++;	
			}
			
			//catch the last group missed by the previous while (happens due to the way count is incremented)
			outputFileName = intToStringWithLeadingZeroes(count - groupSize, zeroes) + 
			"_" + 
				intToStringWithLeadingZeroes(count  - 1, zeroes);
			
			catFiles(printList, outputFileName,destinationDirectory);
		}
	}

	public static void makePredictionFiles(List<File> fileList, String outputFilename, File destinationDirectory, int crossValidationNumber) throws FileNotFoundException, IOException
	{
		Iterator<File> fileIterator = fileList.iterator();
		
		//Objects needed to read all the files in file list
		List<BufferedReader> bufferedReaderList = new Vector<BufferedReader>();
		Iterator<BufferedReader> brIterator;
		BufferedReader currentBufferedReader;
		
		//Objects needed to write to all the slice files
		List<PrintWriter> printWriterList = new Vector<PrintWriter>();
		File sliceFile;
		PrintWriter currentPrintWriter;
		Iterator<PrintWriter> pwIterator;
		String currentLine;
		
		//Load up a List of BufferedReaders to read from all the files
		while (fileIterator.hasNext())
		{
			bufferedReaderList.add(new BufferedReader( new FileReader(fileIterator.next())));
		}
		
		//Create PrintWriters to write out to each "slice" file
		for (int i = 0; i < crossValidationNumber; i++)
		{
			try //make the required destination directory if not already present 
			{
			sliceFile = new File(destinationDirectory, PREDICT_FOLDER + PATH_DELIM + crossValidationNumber + PATH_DELIM+ outputFilename + "." + i);
			sliceFile.createNewFile();
			}
			catch(IOException e)
			{
				new File(destinationDirectory + PATH_DELIM + PREDICT_FOLDER + PATH_DELIM + crossValidationNumber).mkdirs();
				sliceFile = new File(destinationDirectory, PREDICT_FOLDER + PATH_DELIM + crossValidationNumber + PATH_DELIM+ outputFilename + "." + i);
				sliceFile.createNewFile();
			}
			
			printWriterList.add(new PrintWriter(sliceFile));
		}
		
		pwIterator = printWriterList.iterator();
	
		//This routine only sets up valid slices IF each source file has at least two separate lines of data so
		//there is at least one line from that source in each training set.
		//I can't EVEN BELIEVE I had to put a synchronize statement in my code.
		synchronized(bufferedReaderList)
		{
			brIterator = bufferedReaderList.iterator();
			while (brIterator.hasNext())
			{
				currentBufferedReader = brIterator.next();
				
				while ((currentLine = currentBufferedReader.readLine()) != null)
				{
					try
					{
						currentPrintWriter = pwIterator.next();
					}
					catch (NoSuchElementException s)
					{
						pwIterator = printWriterList.iterator();
						
						currentPrintWriter = pwIterator.next();
					}
					
					currentPrintWriter.println(currentLine);
					currentPrintWriter.flush();
				}
			}
		}
	}
	

	public static void makeTrainingFiles(List<File> fileList, String outputFileName, File destinationDirectory) throws IOException
	{
		
		//Copy first element to a compare variable
		File compareFile = fileList.get(0);
		File holdOutFile;// = fileList.remove(0);
		Iterator<File> fileListIterator;
		BufferedReader bufferedReader;
		File outputFile;
		PrintWriter printWriter;
		String currentLine;
		int listSize = fileList.size();
		
		//do-while first element != compare variable
		do {
			//Remove first element into temp variable
			holdOutFile = fileList.remove(0);
			
			//Assign iterator
			fileListIterator = fileList.iterator();
			
			new File(destinationDirectory, TRAIN_FOLDER + PATH_DELIM + listSize).mkdirs();
			outputFile = new File(destinationDirectory, TRAIN_FOLDER + PATH_DELIM + listSize + PATH_DELIM + outputFileName + "." + holdOutFile.getName().split("\\.")[1]);
			printWriter = new PrintWriter(outputFile);
			while (fileListIterator.hasNext())
			{
				bufferedReader = new BufferedReader(new FileReader (fileListIterator.next()));
				while ((currentLine = bufferedReader.readLine()) != null)
				{
					printWriter.println(currentLine);
				}
			}
			
			//add temp variable to end of list
			fileList.add(holdOutFile);
			
			
		}while (!fileList.get(0).equals(compareFile));
		//end do-while
	}

	/**
	 * Given a String path to a directory, returns a List of the files in that directory.  If a filename
	 * is provided instead of a directory, then a list with one element, the file provided, is returned.
	 * Checking for existence or validity of files is conducted.  Once the list is built, all directories 
	 * in that list a removed.  This method is NOT recursive.
	 * 
	 * @param directoryName String name of the directory containing source files to group and cat/slice
	 * @return List of all files int directoryName.  If directory name was a file instead of a directory, 
	 * the list contains one element, the file designated by directoryName.
	 */
	public static List<File> getFiles(String directoryName)
	{
		File directory = new File(directoryName);
		File[] fileArray;
		List<File> fileVector = new Vector<File>();
		Iterator<File> iterator;
		File file;
		
		//If a filename was provided
		if (!directory.isDirectory())
		{
			fileArray = new File[1];
			fileArray[0] = directory;
		}
		else
		{
			fileArray = directory.listFiles();
		}
		
		// Load the list with all the elements from the array
		for (int i=0; i < fileArray.length; i++)
		{
			fileVector.add(fileArray[i]);
		}
		
		iterator = fileVector.iterator();
		
		while (iterator.hasNext())
		{
			file = iterator.next();
			
			//If the element is a directory, remove.  If the element does not exist, remove.
			if (!file.isFile())
			{
				iterator.remove();
			}
		}
		
		return fileVector;
	}
	
	/**
	 * Sorts a list of files by the file size
	 * 
	 * @param fileList List of files to sort by size
	 */
	static public void sortBySize(List<File> fileList)
	{
		FileSizeComparator<File> fileSizeComparator = new FileSizeComparator<File>();
		
		Collections.sort(fileList, fileSizeComparator);
	}
	
	
	/**
	 * Takes a given number and left pads it with zeroes so the total number of digits
	 * int the String equals the value "totalDigits"
	 * @param number the number to be left padded with zeroes
	 * @param totaldigits the total number of digits required in the final string
	 * @return a String representation of an integer with left padded zeroes
	 */
	public static String intToStringWithLeadingZeroes(int number, int totalDigits)
	{
		String result = String.valueOf(number);
		
		int start = result.length();
		
		for (int i = start; i < totalDigits; i++)
		{
			result = "0" + result;
		}
		
		return result;
	}
	
	public static void roundRobinFiles(List<File> fileList, String outputFilename, File destinationDirectory, int crossValidationNumber) throws IOException
	{
		if (crossValidationNumber < MIN_SLICES)
		{
			System.out.println("A cross validation size of less than 2 will be treated as a cat operation, now invoking catFiles...");
			catFiles(fileList, outputFilename,  destinationDirectory);
			return;
		}
		
		makePredictionFiles(fileList, outputFilename, destinationDirectory, crossValidationNumber);
		
		FilenameFilter fileNameFilter = new ThisFileNameFilter(outputFilename);
		
		List<File> predictFileList = new Vector<File>();
		
		Collections.addAll(predictFileList, new File(destinationDirectory, PREDICT_FOLDER + PATH_DELIM + crossValidationNumber).listFiles(fileNameFilter));
		
		makeTrainingFiles(predictFileList, outputFilename, destinationDirectory);
	}
	
	public static void catFiles(List<File> fileList, String outputFileName, File destinationDirectory) throws FileNotFoundException, IOException
	{
		Iterator<File> iterator = fileList.iterator();

			File currentFile;
			
			Reader reader;
			BufferedReader bufferedReader;
			
			File outputFile = new File(destinationDirectory, outputFileName);
			PrintWriter printWriter = new PrintWriter(outputFile);
	
			String currentLine;
			
			while (iterator.hasNext())
			{
				currentFile = iterator.next();
				reader = new FileReader(currentFile);
				bufferedReader = new BufferedReader(reader);
				
				while((currentLine =bufferedReader.readLine()) != null)
				{
					printWriter.println(currentLine);
				}
				
				printWriter.flush();
			}
			
			printWriter.flush();
	}
	
	private static List<File> smallAndLargeProcess(List<File> fileList,  int groupSize)
	{
		File file;
		Iterator<File> iterator;
		sortBySize(fileList);
		int buckets = fileList.size() / groupSize;
		Vector<List<File>> bucketList = new Vector<List<File>>();
		
		bucketList.setSize(buckets);
		
		for (int i = 0; i < buckets; i++)
		{
			bucketList.set(i, new Vector<File>());
		}
		
		iterator = fileList.iterator();
		
		//FIXME This is WAY brittle.  Need to improve.
		for (int j = 0; j < groupSize; j++)
			for (int k = 0; k < buckets; k++)
			{
				if (iterator.hasNext())
				{
					file = iterator.next();
					bucketList.get(k).add(file);
				}
				else
				{
					break;
				}
			}
		
		fileList.clear();
		
		for (int l = 0; l < buckets; l++)
		{
			fileList.addAll(bucketList.get(l));
		}

		return fileList;
	}
	
	public static void main(String[] args) throws IOException 
	{
		/*String sourceDirectory = "/thesis/corpora/enron/0/smallSVMFiles";
		String destinationDirectory = "/thesis/corpora/enron/0/smallSVMFiles/aaaSmallToLarge";
		int groupSize = 5;
		int titleDigits = 3;
		int crossValidationNumber = 5;*/
		
		//smallToLargeProcess(sourceDirectory, groupSize, titleDigits, destinationDirectory, crossValidationNumber);
	}

	

}
