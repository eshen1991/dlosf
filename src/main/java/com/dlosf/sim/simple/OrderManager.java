/**
 * 
 */
package com.dlosf.sim.simple;

import alphabetsoup.framework.Updateable;
import alphabetsoup.framework.Word;
import alphabetsoup.framework.WordList;
import alphabetsoup.framework.WordStation;


/**Basic simple implementation of a OrderManager, which dispenses incoming words to Word
 * stations as they have space.
 *
 */
public class OrderManager implements Updateable {

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) {
		return Double.POSITIVE_INFINITY;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) {
		WordList wl = SimulationWorldSimple.getSimulationWorld().getWordList();
		LetterManager lm = ((LetterManager)((SimulationWorldSimple) SimulationWorldSimple.getSimulationWorld()).letterManager);
		
		//see if all done
		if(wl.getAvailableWords().size() == 0)
			return;
		
		for(WordStation s : SimulationWorldSimple.getSimulationWorld().getWordStations()) {
			//give the station words if it needs them
			if(s.getAssignedWords().size() < s.getCapacity()) {
				Word w = wl.takeAvailableWord(0);
				s.assignWord(w);
				SimulationWorldSimple.getSimulationWorld().bucketbotManager.newWordAssignedToStation(w, s);
				lm.newWordAssignedToStation(s, w);
			}
			
			//can't continue if out of words
			if(wl.getAvailableWords().size() == 0)
				return;
		}
	}

}
