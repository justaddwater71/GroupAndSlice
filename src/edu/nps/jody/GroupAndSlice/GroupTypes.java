/**
 * 
 */
package edu.nps.jody.GroupAndSlice;

/**
 * @author jody
 *
 */
public enum GroupTypes 
{
	GROUP_SMALL_TO_LARGE_NAME		("aaaSmallToLarge"),
	GROUP_SMALL_AND_LARGE_NAME	("aaaSmallAndLarge"),
	GROUP_RANDOM_SIZE_NAME				( "aaaRandom");
	
	private String dirName;
	
	GroupTypes(String dirName)
	{
		this.dirName = dirName;
	}
	
	private String dirName()
	{
		return dirName;
	}
}
