package de.b0n.dir.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Takes Elements an groups them.
 * All Operations try to be ThreadSafe.
 * @author Claus
 *
 * @param <G> Group type
 * @param <E> Element type
 */
public class Cluster<G, E> {
	private final Map<G, Queue<E>> map = new ConcurrentHashMap<G, Queue<E>>();

	/**
	 * Filtert Gruppen mit nur einem Element heraus.

	 * @return Eingegebenes Cluster ohne Gruppen mit nur einem Element
	 */
	public Cluster<G, E> removeUniques() {
		for (G group : map.keySet()) {
			Queue<E> elements = map.get(group);
			if (elements.size() <= 1) {
				map.remove(group);
			}
		}
		return this;
	}

	/**
	 * Fügt ein Element seiner Gruppe hinzu.
	 * Neue leere Gruppen werden erstellt.
	 * 
	 * @param group
	 *            Gruppe, zu der das Element hinzugefügt werden soll
	 * @param element
	 *           Element, das hinzugefügt werden soll
	 */
	public void addGroupedElement(G group, E element) {
		if (group == null) {
			throw new IllegalArgumentException("Group darf nicht null sein.");
		}
		if (element == null) {
			throw new IllegalArgumentException("Element darf nicht null sein.");
		}

		Queue<E> elements;
		synchronized (this) {
			elements = map.get(group);
			if (elements == null) {
				map.put(group, new ConcurrentLinkedQueue<E>());
				elements = map.get(group);
			}	
		}
		elements.add(element);
	}

	/**
	 * Entlädt die Elemente in Gruppen gebündelt
	 * @return Collection mit den Queues der Elemente
	 */
	public Collection<Queue<E>> values() {
		return map.values();
	}
}
