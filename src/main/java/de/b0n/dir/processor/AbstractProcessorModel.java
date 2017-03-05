package de.b0n.dir.processor;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;
import com.github.funthomas424242.unmodifiable.UnmodifiableQueueFIFO;
import de.b0n.dir.model.ModelFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Takes Elements an groups them. All Operations try to be ThreadSafe.
 *
 * @param <G> Group type
 * @param <E> Element type
 * @author Claus
 */
public abstract class AbstractProcessorModel<G, E> {

    protected final Map<G, UnmodifiableQueue<E>> map = new ConcurrentHashMap<>();

    public abstract ModelFactory getModelFactory();


    /**
     * Entlädt die Elemente in Gruppen gebündelt
     *
     * @return Collection mit den Queues der Elemente
     */
    final protected Collection<UnmodifiableQueue<E>> values() {
        return map.values();
    }


    /**
     * Prüft auf die Existenz der Gruppe
     *
     * @param group abgefragte Gruppe
     * @return ob die Gruppe existiert
     */
    final protected boolean containsGroup(G group) {
        return map.containsKey(group);
    }

    /**
     * Liefert die Elemente-Queue, welche mit der Gruppe bezeichnet ist
     *
     * @param group Gruppe, deren Elemente als Queue zurückgeliefert werden
     * @return Queue mit den Elementen der angegebenen Gruppe
     */
    final protected UnmodifiableQueue<E> getGroup(G group) {
        return map.get(group);
    }


    /**
     * Fügt ein Element seiner Gruppe hinzu.
     *
     * @param group   Gruppe, zu der das Element hinzugefügt werden soll
     * @param element Element, das hinzugefügt werden soll
     */
    final protected void addGroupedElement(G group, E element) {
        if (group == null) {
            throw new IllegalArgumentException("group darf nicht null sein.");
        }
        if (element == null) {
            throw new IllegalArgumentException("element darf nicht null sein.");
        }

        addElementToGroup(group,element);
    }

    /**
     * Liefert die Queue der Gruppe. Neue leere Gruppen werden erstellt.
     *
     * @param group Key der zu liefernden Gruppe
     * @return Liefert die in der Map enthaltene Gruppe oder eine neue, der Map
     * mit diesem Key hinzugefügten Gruppe
     */
    final protected UnmodifiableQueue<E> addElementToGroup(G group, E element) {
        UnmodifiableQueue<E> elements;
        synchronized (this) {
            elements = map.get(group);
            if (elements == null) {
                map.put(group, new UnmodifiableQueueFIFO<E>().addElement(element));
            }else{
                map.put(group,elements.addElement(element));
            }
            // Da beide Fälle neue Queues erzeugt haben
            elements = map.get(group);
        }
        return elements;
    }

    /**
     * Entfernt eine Gruppe aus dem AbstractProcessorModel
     *
     * @param group Zu entfernende Gruppe
     * @return Queues der entfernten Elemente
     */
    final protected UnmodifiableQueue<E> removeGroup(G group) {
        synchronized (this) {
            return map.remove(group);
        }
    }


    /**
     * Liefert die erste verfügbare Gruppe und entfernt diese aus dem AbstractProcessorModel.
     *
     * @return Elemente der ersten verfügbaren Gruppe
     */
    final protected UnmodifiableQueue<E> popGroup() {
        synchronized (this) {
            Iterator<G> iterator = map.keySet().iterator();
            if (iterator.hasNext()) {
                return map.remove(iterator.next());
            }
            return null;
        }
    }

    /**
     * Filtert Gruppen mit nur einem Element heraus.
     *
     * @return Queue der entfernten einzigartigen Dateien
     */
    final protected Queue<E> removeUniques() {
        final Queue<E> uniques = new ConcurrentLinkedQueue<E>();
        synchronized (this) {
            for (G group : map.keySet()) {
                UnmodifiableQueue<E> elements = map.get(group);
                if (elements.size() <= 1) {
                    UnmodifiableQueue<E> removedGroup = map.remove(group);
                    if (removedGroup.size() == 1) {
                        uniques.add(removedGroup.peek());
                    }
                }
            }
        }
        return uniques;
    }




}
