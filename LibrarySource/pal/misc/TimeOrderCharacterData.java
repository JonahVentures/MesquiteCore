// TimeOrderCharacterData.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import java.io.*;
import pal.util.*;
import pal.math.*;

/**
 * Character data that expresses an order through time.
 * 
 * @version $Id: TimeOrderCharacterData.java,v 1.9 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class TimeOrderCharacterData implements Serializable, BranchLimits, IdGroup {
	 
	/** Order of times */
	protected int[] timeOrdinals = null;
	
	/** Actual times of each sample */
	protected double[] times = null;
	
	/** the identifier group */
	protected IdGroup taxa;

	protected int units = Units.GENERATIONS;
	
	/**
	 * Parameterless constructor for superclasses.
	 */
	protected TimeOrderCharacterData() {}
	
	/**
	 * Constructor taking only IdGroup.
	 * Beware! This constructor does not initialize 
	 * any time ordinals or times.
	 */
	public TimeOrderCharacterData(IdGroup taxa, int units) {
		this.taxa = taxa;
	}
   
	/**
	 * Constructs a TimeOrderCharacterData.
	 */
	public TimeOrderCharacterData(int numSeqsPerSample, int numSamples, 
		double timeBetweenSamples, int units) {
		
		int n = numSeqsPerSample * numSamples;
		
		taxa = IdGenerator.createIdGroup(n);

		// create times and ordinals
		timeOrdinals = new int[taxa.getIdCount()];
		times = new double[taxa.getIdCount()];
	
		
		int index = 0;
		for (int i = 0; i < numSamples; i++) {
			for (int j = 0; j < numSeqsPerSample; j++) {
				times[index] = timeBetweenSamples * (double)i;
				timeOrdinals[index] = i;
				index += 1;
			}
		}

		this.units = units;
	}

	/**
	 * Returns a clone of the specified TimeOrderCharacterData 
	 */
	public static TimeOrderCharacterData clone(TimeOrderCharacterData tocd) {
		return tocd.subset(tocd);
	}
	 
	/**
	 * Extracts a subset of a TimeOrderCharacterData.
	 */
	public TimeOrderCharacterData subset(IdGroup staxa) {
	
		TimeOrderCharacterData subset = 
			new TimeOrderCharacterData(staxa, getUnits());
	 
		subset.timeOrdinals = new int[staxa.getIdCount()];
		if (hasTimes()) {
			subset.times = new double[staxa.getIdCount()];
		}
		
		for (int i = 0; i < subset.timeOrdinals.length; i++) {
			int index = taxa.whichIdNumber(staxa.getIdentifier(i).getName());
			subset.timeOrdinals[i] = timeOrdinals[index];
	
			if (hasTimes()) {
				subset.times[i] = times[index];
			}
		}
		return subset;
	}

	public int getUnits() {
		return units;
	}

	/**
	 * Sets the times, and works out what the ordinals should be.
	 */
	public void setTimes(double[] times, int units) {
		setTimes(times, units, true);
	}
	 
	/**
	 * Sets the times. 
	 * @param recalculateOrdinals true if ordinals should be 
	 * recalculated from the times.
	 */
	public void setTimes(double[] times, int units, boolean recalculateOrdinals) {
		this.times = times;
		this.units = units;
		if (recalculateOrdinals) {
			setOrdinalsFromTimes();
		}
	}

	public TimeOrderCharacterData scale(double rate, int newUnits) {
		
		TimeOrderCharacterData scaled = clone(this);
		
		scaled.units = newUnits;
		for (int i = 0; i < times.length; i++) {
			scaled.times[i] = times[i] * rate;
		}

		return scaled;
	}

	/** 
	 * Sets ordinals.
	 */
	public void setOrdinals(int[] ordinals) {
		timeOrdinals = ordinals;
	}

	/**
	 * Gets ordinals.
	 */
	public int[] getOrdinals() {
		return timeOrdinals;
	}

	/**
	 * Returns a copy of the times in the form of an array.
	 */
	public double[] getCopyOfTimes() {
		double[] copyTimes = new double[times.length];
		System.arraycopy(times, 0, copyTimes, 0, times.length);

		return copyTimes;
	}

	/**
	 * Remove time character data.
	 */
	public void removeTimes() {
		times = null;
	}
	
	/**
	 * Set time ordinals from another TimeOrderCharacterData.
	 * Select ordinals by matching names.
	 * @param tocd to take ordinals from.
	 */
	public void setOrdinals(TimeOrderCharacterData tocd) {
		setOrdinals(tocd, null, false);
	}

	public void setTimesAndOrdinals(TimeOrderCharacterData tocd) {
		setOrdinals(tocd, null, true);
	}

	/**
	 * Set time ordinals from another TimeOrderCharacterData.
	 * Select ordinals by matching names.
	 * @param tocd to take ordinals from
	 * @param idgroup use these labels to match indices in given tocd.
	 * @param doTimes if set then sets times as well
	 */
	public void setOrdinals(TimeOrderCharacterData tocd, IdGroup standard, boolean doTimes) {
		
		if (timeOrdinals == null) {
			timeOrdinals = new int[taxa.getIdCount()];
		}

		if (doTimes && tocd.hasTimes()) {
			times = new double[taxa.getIdCount()];
		}
		
		if (standard == null) {
			standard = tocd;
		}
	
		for (int i = 0; i < taxa.getIdCount(); i++) {
			
			String name = taxa.getIdentifier(i).getName();
			int index = standard.whichIdNumber(name);
		 	if (index == -1) {
				System.err.println("Identifiers don't match!");
				System.err.println("Trying to find: '" + name + "' in:");
				System.err.println(standard);
				//System.exit(1);
			}
			
			timeOrdinals[i] = tocd.getTimeOrdinal(index);
			if (doTimes && tocd.hasTimes()) {
				times[i] = tocd.getTime(index);
			}
		}
	}

	private void setOrdinalsFromTimes() {
		
		int[] indices = new int[times.length];
		timeOrdinals = new int[times.length];
		HeapSort.sort(times, indices);

		int ordinal = 0;
		int lastIndex = 0;
		timeOrdinals[indices[0]] = ordinal;
		
		for (int i = 1; i < indices.length; i++) {
			if (Math.abs(times[indices[i]] - times[indices[lastIndex]]) <= ABSTOL) {
				// this time is still within the tolerated error
			} else {
				// this is definitely a new time
				lastIndex = i;
				ordinal += 1;
			}
			timeOrdinals[indices[i]] = ordinal;
		}
	}
	
	/**
	 * Returns the number of characters per identifier 
	 */
	public int getNumChars() {
		if (hasTimes()) {
			return 2;
		} else return 1;
	}
	
	/**
	 * Returns a name for this character data.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this character data.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public double getTime(int taxon) {
		return times[taxon];
	}

	/**
	 * NOTE: currently assumes times exist!
	 */
	public double getHeight(int taxon, double rate) {
		return times[taxon] * rate; 
	}
	
	public int getTimeOrdinal(int taxon) {
		return timeOrdinals[taxon];
	}
	
	public boolean hasTimes() {
		return times != null;
	}

	/**
	 * Returns an ordered vector of unique times in this
	 * time order character data.
	 */
	public double[] getUniqueTimeArray() {
		int count = getSampleCount();
	
		double[] utimes = new double[count];
		for (int i = 0; i < times.length; i++) {
			utimes[getTimeOrdinal(i)] = times[i];
		}

		return utimes;
	}

	/**
	 * Returns a matrix of times between samples. A
	 * sample is any set of identifiers that have the same times.
	 */
	public double[][] getUniqueTimeMatrix() {
	
		
		double[] utimes = getUniqueTimeArray();
		int count = utimes.length;
		
		double[][] stimes = new double[count][count];
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				stimes[i][j] = Math.abs(utimes[i] - utimes[j]);
			}
		}
		
		return stimes;
	}
		
	/**
	 * Returns the number of unique times in this data.
	 * A sample is any set of identifiers that have the same times.
	 */
	public int getSampleCount() {
		int max = 0;
		for (int i = 0; i < timeOrdinals.length; i++) {
			if (timeOrdinals[i] > max) max = timeOrdinals[i];
		}
		return max + 1;
	}
	
	/** 
	 * Returns a string representation of this time order character data.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Identifier\t"+ (hasTimes() ? "Times\t" : "") + "Sample\n");
		for (int i = 0; i < taxa.getIdCount(); i++) {
			sb.append(taxa.getIdentifier(i) + "\t" + 
				(hasTimes() ? getTime(i) + "\t" : "") + 
				getTimeOrdinal(i)+"\n"); 
		}
		return new String(sb);
	}

	public void shuffleTimes() {
		MersenneTwisterFast mtf = new MersenneTwisterFast();
		
		int[] indices = mtf.shuffled(timeOrdinals.length);
		
		int[] newOrdinals = new int[timeOrdinals.length];
		double[] newTimes = null;
		if (hasTimes()) {
			newTimes = new double[times.length];
		}
		for (int i = 0; i < timeOrdinals.length; i++) {
			newOrdinals[i] = timeOrdinals[indices[i]];
			if (hasTimes()) { newTimes[i] = times[indices[i]]; }
		}

		timeOrdinals = newOrdinals;
		if (hasTimes()) times = newTimes;
	}

	//IdGroup interface
	public Identifier getIdentifier(int i) {return taxa.getIdentifier(i);}
	public void setIdentifier(int i, Identifier ident) { taxa.setIdentifier(i, ident); }
	public int getIdCount() { return taxa.getIdCount(); }
	public int whichIdNumber(String name) { return taxa.whichIdNumber(name); }

	/**
	 * Return id group of this alignment. 
	 * @deprecated TimeOrderCharacterData now implements IdGroup
	 */
	public IdGroup getIdGroup() { return taxa; }

	// PRIVATE STUFF

	/** Name of this character data */
	private String name = "Time/order character data";
}

