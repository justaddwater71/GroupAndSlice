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
	GROUP_SMALL_TO_LARGE		("aaaSmallToLarge"),
	GROUP_SMALL_AND_LARGE	("aaaSmallAndLarge"),
	GROUP_RANDOM_SIZE				( "aaaRandom");
	
	private String dirName;
	
	GroupTypes(String dirName)
	{
		this.dirName = dirName;
	}
	
	//If the enums names get too unwieldy, shorter or more convenient Strings can be had here by overriding toString
	public String dirName()
	{
		return dirName;
	}
}
