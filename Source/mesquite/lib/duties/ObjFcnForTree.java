/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.1, May 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.lib.duties;import java.awt.*;import mesquite.lib.*;/* ======================================================================== *//**Supplies a number for a tree, intended to be used to choose among trees.  Basically a slightmodification on NumberForTree, so that a module can claim to be possibly relevant for choosingtrees (otherwise trivial numbers like "Number of Taxa" would get included).*/public abstract class ObjFcnForTree extends NumberForTree  {   	 public Class getDutyClass() {   	 	return ObjFcnForTree.class;   	 } 	public String getDutyName() { 		return "Objective function for Tree";   	 }		/** indicates default optimization, e.g. for tree searchers.  If true, tree search will maximize,	otherwise minimize.  If number has obvious optimum direction, this should be overridden to indicate	optimum*/	public boolean biggerIsBetter() {		return true;	}}