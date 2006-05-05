/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. Version 1.1, May 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.lists.CodePosSetsList;/*~~  */import mesquite.lists.lib.*;import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;/* ======================================================================== */public class CodePosSetsList extends DataSpecssetList {	public int currentDataSet = 0;	public CharacterData data = null;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		makeMenu("List");		return true;  	 }	/** returns a String of annotation for a row*/	public String getAnnotation(int row){ return null;}	/** sets the annotation for a row*/	public void setAnnotation(int row, String s, boolean notify){}	public Class getItemType(){		return CodonPositionsSet.class;	}	public String getItemTypeName(){		return "Codon positions set";	}	public String getItemTypeNamePlural(){		return "Codon positions sets";	}	public SpecsSet makeNewSpecsSet(CharacterData data){		if (data!=null)			return new CodonPositionsSet("Untitled Model Set", data.getNumChars(), data);		return null;	}	/*.................................................................................................................*/    	 public String getName() {		return "List of Codon Positions Sets";   	 }	/*.................................................................................................................*/   	  	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Makes windows listing codon positions sets." ;   	 }	/*.................................................................................................................*/}