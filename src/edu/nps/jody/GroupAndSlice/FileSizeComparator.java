package edu.nps.jody.GroupAndSlice;

import java.io.File;
import java.util.Comparator;

public class FileSizeComparator<T> implements Comparator<T> 
{

	@Override
	public int compare(T o1, T o2) 
	{
		File file1;
		File file2;
		
		file1 = (File)o1;
		file2 = (File)o2;
		
		if (file1.length() < file2.length())
		{
			return -1;
		}
		
		if (file1.length() >file2.length())
		{
			return 1;
		}
		
		return 0;
	}

	
}
