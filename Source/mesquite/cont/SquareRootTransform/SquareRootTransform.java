/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.SquareRootTransform;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class SquareRootTransform extends ContDataAlterer {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
		
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
			if (!(data instanceof ContinuousData))
				return false;
			return alterContentOfCells(data,table, undoReference);
   	}

   	
   	boolean firstTime = true;
   	
	/*.................................................................................................................*/
   	public void alterCell(CharacterData ddata, int ic, int it){
   		ContinuousData data = ((ContinuousData)ddata);
		for (int item = 0; item<data.getNumItems(); item++){
			double state = data.getState(ic,it, item);
			if (MesquiteDouble.isCombinable(state)) {
				if (state<0) {
					if (firstTime)
						discreetAlert( "Some values could not be transformed by square root because they were  negative");
					firstTime = false;
				}
				else
					data.setState(ic,it, item, Math.sqrt(state));
			}
		}
	}
	
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Square Root transform";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters continuous data by taking the square root." ;
   	 }
   	 
}

