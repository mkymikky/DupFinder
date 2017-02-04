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
	 * 
	 * @param group
	 *            Gruppe, zu der das Element hinzugefügt werden soll
	 * @param element
	 *           Element, das hinzugefügt werden soll
	 */
	public void addGroupedElement(G group, E element) {
		if (group == null) {
			throw new IllegalArgumentException("group darf nicht null sein.");
		}
		if (element == null) {
			throw new IllegalArgumentException("element darf nicht null sein.");
		}

		getOrInitializeGroup(group).add(element);
	}
	
	/**
	 * Fügt Element einer Collection seiner Gruppe hinzu.
	 * @param group
	 *            Gruppe, zu der das Element hinzugefügt werden soll
	 * @param elements
	 *           Elements, die hinzugefügt werden sollen
	 */
	public void addGroupedElements(G group, Collection<E> elements) {
		if (group == null) {
			throw new IllegalArgumentException("group darf nicht null sein.");
		}
		if (elements == null) {
			throw new IllegalArgumentException("elemente darf nicht null sein.");
		}

		getOrInitializeGroup(group).addAll(elements);
	}

	/**
	 * Liefert die Queue der Gruppe.
	 * Neue leere Gruppen werden erstellt.
	 * @param group
	 * @return
	 */
	private Queue<E> getOrInitializeGroup(G group) {
		Queue<E> elements;
		synchronized (this) {
			elements = map.get(group);
			if (elements == null) {
				map.put(group, new ConcurrentLinkedQueue<E>());
				elements = map.get(group);
			}	
		}
		return elements;
	}

	/**
	 * Entlädt die Elemente in Gruppen gebündelt
	 * @return Collection mit den Queues der Elemente
	 */
	public Collection<Queue<E>> values() {
		return map.values();
	}

	/**
	 * Prüft auf die Existenz der Gruppe
	 * @param group abgefragte Gruppe
	 * @return ob die Gruppe existiert
	 */
	public boolean containsGroup(G group) {
		return map.containsKey(group);
	}

	/**
	 * Liefert die Elemente-Queue, welche mit der Gruppe bezeichnet ist
	 * @param group Gruppe, deren Elemente als Queue zurückgeliefert werden
	 * @return Queue mit den Elementen der angegebenen Gruppe
	 */
	public Collection<E> getGroup(G group) {
		return map.get(group);
	}

	/**
	 * Entfernt eine Gruppe aus dem Cluster
	 * @param group Zu entfernende Gruppe
	 * @return Queues der entfernten Elemente
	 */
	public Queue<E> removeGroup(G group) {
		return map.remove(group);
	}
}
