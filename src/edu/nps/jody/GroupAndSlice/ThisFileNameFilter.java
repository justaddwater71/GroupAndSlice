package edu.nps.jody.GroupAndSlice;

import java.io.File;
import java.io.FilenameFilter;

public class ThisFileNameFilter implements FilenameFilter 
{
	//Data Member
	String string;
	
	//Constructor
	ThisFileNameFilter(String string)
	{
		this.string = string;
	}
	
	@Override
	public boolean accept(File file, String name) 
	{
		return name.contains(string);
	}

}
