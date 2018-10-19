package de.b0n.dir.processor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Takes Elements an groups them. All Operations try to be ThreadSafe.
 * @param <G> Group type
 * @param <E> Element type
 */
public class Cluster<G, E> {
	private final Map<G, Queue<E>> map = new ConcurrentHashMap<G, Queue<E>>();

	/**
	 * Filtert Gruppen mit nur einem Element heraus.
	 * 
	 * @return Queue der entfernten einzigartigen Dateien oder null wenn keine einmaligen Elemente existieren
	 */
	public Queue<E> removeUniques() {
		Queue<E> uniques = new ConcurrentLinkedQueue<E>();
		synchronized (this) {
			for (G group : map.keySet()) {
				Queue<E> elements = map.get(group);
				if (elements.size() <= 1) {
					uniques.addAll(map.remove(group));
				}
			}
		}
		return uniques.isEmpty() ? null : uniques;
	}

	/**
	 * Fügt ein Element seiner Gruppe hinzu.
	 * 
	 * @param group
	 *            Gruppe, zu der das Element hinzugefügt werden soll
	 * @param element
	 *            Element, das hinzugefügt werden soll
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
	 * Liefert die Queue der Gruppe. Neue leere Gruppen werden erstellt.
	 * 
	 * @param group
	 *            Key der zu liefernden Gruppe
	 * @return Liefert die in der Map enthaltene Gruppe oder eine neue, der Map
	 *         mit diesem Key hinzugefügten Gruppe
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
	 * 
	 * @return Collection mit den Queues der Elemente
	 */
	public Collection<Queue<E>> values() {
		return map.values();
	}

	/**
	 * Prüft auf die Existenz der Gruppe
	 * 
	 * @param group
	 *            abgefragte Gruppe
	 * @return ob die Gruppe existiert
	 */
	public boolean containsGroup(G group) {
		return map.containsKey(group);
	}

	/**
	 * Liefert die Elemente-Queue, welche mit der Gruppe bezeichnet ist
	 * 
	 * @param group
	 *            Gruppe, deren Elemente als Queue zurückgeliefert werden
	 * @return Queue mit den Elementen der angegebenen Gruppe
	 */
	public Queue<E> getGroup(G group) {
		return map.get(group);
	}

	/**
	 * Entfernt eine Gruppe aus dem Cluster
	 * 
	 * @param group
	 *            Zu entfernende Gruppe
	 * @return Queues der entfernten Elemente
	 */
	public Queue<E> removeGroup(G group) {
		synchronized (this) {
			return map.remove(group);
		}
	}

	/**
	 * Liefert die Gesamtanzahl der Elemente über alle Gruppen. Falls dieser
	 * Cluster mehr als Integer.MAX_VALUE Elemente enthält, liefert er
	 * Integer.MAX_VALUE.
	 * 
	 * @return Anzahl aller Elemente im Cluster
	 */
	public int size() {
		int size = 0;
		for (Queue<E> group : values()) {
			size += group.size();
			if (size < 0) {
				return Integer.MAX_VALUE;
			}
		}
		return size;
	}

	/**
	 * Liefert die erste verfügbare Gruppe und entfernt diese aus dem Cluster.
	 * 
	 * @return Elemente der ersten verfügbaren Gruppe, oder null, wenn keine Gruppe vorhanden ist
	 */
	public Queue<E> popGroup() {
		synchronized (this) {
			Iterator<G> iterator = map.keySet().iterator();
			if (iterator.hasNext()) {
				return map.remove(iterator.next());
			}
			return null;
		}
	}
}
