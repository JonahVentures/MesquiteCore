// SimulatedAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.datatype.*;
import pal.substmodel.*;
import pal.alignment.*;
import pal.math.*;
import pal.misc.*;


/**
 * generates an artificial data set
 *
 * @version $Id: SimulatedAlignment.java,v 1.9 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class SimulatedAlignment extends AbstractAlignment
{
	//
	// Public stuff
	//

	/**
	 * Inititalisation
	 *
	 * @param sites number of sites
	 * @param t     tree relating the sequences
	 * @param m     model of evolution
	 */
	public SimulatedAlignment(int sites, Tree t, SubstitutionModel m)
	{
		dataType = m.rateMatrix.getDataType();
		numStates = dataType.getNumStates();
		model = m;
		
		rng = new MersenneTwisterFast();
		
		tree = t;
		tree.createNodeList();

		numSeqs = tree.getExternalNodeCount();
		numSites = sites;
		idGroup = new SimpleIdGroup(numSeqs);
				
		for (int i = 0; i < numSeqs; i++)
		{
			idGroup.setIdentifier(i, tree.getExternalNode(i).getIdentifier());
		}

		stateData = new byte[numSeqs][numSites];
		
		for (int i = 0; i < tree.getExternalNodeCount(); i++)
		{
			tree.getExternalNode(i).setSequence(stateData[i]);
		}
		for (int i = 0; i < tree.getInternalNodeCount()-1; i++)
		{
			tree.getInternalNode(i).setSequence(new byte[numSites]);
		}
		
		rootSequence = new byte[numSites];
				
		rateAtSite = new int[numSites];
		cumFreqs = new double[numStates];
		cumRateProbs = new double[m.rateDistribution.numRates];
	}


	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return dataType.getChar(stateData[seq][site]);
	}


	/** generate new artificial data set (random root sequence) */
	public void simulate()
	{
		makeRandomRootSequence();
		simulate(rootSequence);
	}
	
	/** generate new artificial data set (specified root sequence) */
	public void simulate(byte[] rootSeq)
	{
		// Check root sequence
		for (int i = 0; i < numSites; i++)
		{
			if (rootSeq[i] >= numStates || rootSeq[i] < 0)
			{
				throw new IllegalArgumentException("Root sequence contains illegal state (?,-, etc.)");
			}
		}
		
		tree.getInternalNode(tree.getInternalNodeCount()-1).setSequence(rootSeq);
		
		// Assign new rate categories
		assignRates();
		
		// Visit all nodes except root
		Node node = NodeUtils.preorderSuccessor(tree.getRoot());
		do
		{
			determineMutatedSequence(node);
			node = NodeUtils.preorderSuccessor(node);	
		}
		while (node != tree.getRoot());
	}


	//
	// Private stuff
	//
	
	private Tree tree;
	private SubstitutionModel model;
	private byte[] rootSequence;
	private double[] cumFreqs;
	private int[] rateAtSite;
	private double[] cumRateProbs;
	private int numStates;
	private byte[][] stateData;
	private MersenneTwisterFast rng;
	
	private void determineMutatedSequence(Node node)
	{
		if (node.isRoot()) throw new IllegalArgumentException("Root node not allowed");
		
		model.setDistance(node.getBranchLength());

		byte[] oldS = node.getParent().getSequence();
		byte[] newS = node.getSequence();		
		
		for (int i = 0; i < numSites; i++)
		{
			cumFreqs[0] = model.transProb(rateAtSite[i], oldS[i], 0);
			for (int j = 1; j < numStates; j++)
			{
				cumFreqs[j] = cumFreqs[j-1] + model.transProb(rateAtSite[i], oldS[i], j);
			}		
			
			newS[i] = (byte) randomChoice(cumFreqs);
		}
	}

	private void makeRandomRootSequence()
	{
		double[] frequencies = model.rateMatrix.getEqulibriumFrequencies();
		cumFreqs[0] = frequencies[0];
		for (int i = 1; i < numStates; i++)	{
			cumFreqs[i] = cumFreqs[i-1] + frequencies[i];
		}

		for (int i = 0; i < numSites; i++)
		{
			rootSequence[i] = (byte) randomChoice(cumFreqs);
		}
	}
	
	private void assignRates()
	{
		cumRateProbs[0] = model.rateDistribution.probability[0];
		for (int i = 1; i < model.rateDistribution.numRates; i++)
		{
			cumRateProbs[i] = cumRateProbs[i-1] + model.rateDistribution.probability[i];
		}		

		for (int i = 0; i < numSites; i++)
		{
			rateAtSite[i] = randomChoice(cumRateProbs);
		}


	}
	
	// Chooses one category if a cumulative probability distribution is given
	private int randomChoice(double[] cf)
	{
		double rnd = rng.nextDouble();
			
		int s;
		if (rnd <= cf[0])
		{
			s = 0;
		}
		else
		{
			for (s = 1; s < cf.length; s++)
			{
				if (rnd <= cf[s] && rnd > cf[s-1])
				{
					break;
				}
			}
		}
			
		return s;
	}
}

