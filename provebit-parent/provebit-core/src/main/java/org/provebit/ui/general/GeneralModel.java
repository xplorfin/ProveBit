package org.provebit.ui.general;

import java.util.Observable;

import javax.swing.event.TableModelListener;
import javax.swing.table.*;

public class GeneralModel extends Observable {
	
	
	public GeneralModel() {
		// TODO
	}
	
	
	private void notifyChange() {
		setChanged();
		notifyObservers();
	}
}
