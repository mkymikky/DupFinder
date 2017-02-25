[![Build Status](https://travis-ci.org/mkymikky/DupFinder.svg?branch=master)](https://travis-ci.org/mkymikky/DupFinder)
[![codecov](https://codecov.io/gh/mkymikky/DupFinder/branch/master/graph/badge.svg)](https://codecov.io/gh/mkymikky/DupFinder)

# DupFinder
Dies ist ein Fork des [DupFinder Projektes](mkymikky/DupFinder), welcher ebenfalls unter GPLv3 steht. 
Die grundlegenden fachlichen Ziele sind identisch mit denen des DupFinder Projektes. Technologisch sollen mit diesem Fork jedoch einige Konzepte und Vorgehensweisen überprüft werden.

## Fachliche Anforderungen
Folgende Anforderungen sind für den Frameworknutzer zu erfüllen:
* Das Framework lässt sich über die Kommandozeile wie folgt aufrufen: java -jar <jarFile> <Suchpfad>
* Das Framework lässt sich in eine andere Anwendung integrieren indem eine Instanz der Haupklasse erzeugt und dieser ein vorgegebenes Datenmodell übergeben wird. 
* Das Datenmodell ist vom Frameworknutzer an sein geünschtes Datenmodell anpassbar zum Beispiel durch Vererbung.
* Der Frameworknutzer soll Callbacks an den Suchprozess übergeben können mit deren Hilfe er über Neuigkeiten (neu zu durchsuchender Ordner, übersprungener Ordner, gefundenes Duplikat, ...) vom Framework informiert werden soll. 
* Fehler im Datenmodell des Nutzers oder den übergebenen Callbacks dürfen den eigentlichen Suchprozess nicht beeinflussen. 
* Der Suchprozess lässt sich über die Kombination von Prozessoren durch den Frameworknutzer verändern.
* Vom Framework ist mindestens eine Kombination an Prozessoren bereitzustellen welche das Durchsuchen riesiger Datenbestände ermöglicht  ohne das es zum Abbruch durch zu wenig Speicher (OutOfMemory Exception) oder zu viele offene Dateien (too many open file handles) kommt. Dafür muss der dabei angewendete Suchalgorithmus nicht schnell sein.
* Weiterhin ist vom Framework mindestens eine Kombination an Prozessoren bereitzustellen mit der ein Datenbestand extrem schnell durchsucht werden kann. Sollte der zu durchsuchende Datenbestand dabei zu groß sein darf es zu einem Abbruch über technische Ausnahmen (Exceptions) kommen. 



## Technologische Anforderungen
Es soll untersucht werden wie sich die Architektur verändert (verbessert oder verschlechtert) wenn folgende Design Regeln angewendet werden:
* Im Programm soll es nur eine statische main Methode zum Start der Anwendung per Kommandozeile geben.
* Sonstige statische Methoden und Attribute sind auf ein Minimum zu reduzieren falls sie nicht komplett eliminiert werden können.
* Die Sichtbarkeit private soll konsequent vermieden werden und stattdessen protected eingesetzt werden. 
* Parameter sind soweit möglich durch fachliche Objekte als eigene Klassen zu definieren. z.B. String vorname -> Vorname vorname.
* Der Suchalgorithmus soll durch Nebenläufigkeit maximal parallelisiert werden. 
* Die Suche soll über eine Kette von Prozessoren erfolgen, welche vom Frameworknutzer willkürlich geschaltet werden kann. 
* Der Frameworknutzer soll eigene Prozessoren implementieren und mit bestehenden im Suchprozess kombinieren können. 
* Das Datenmodell soll der Anwendung beim Start übergeben werden und über die Kette an alle Prozessoren durchgereicht werden.
* Die Daten im Datenmodell sollen zwar vom Frameworknutzer verändert werden können aber dabei muss jeweils eine Kopie der Daten entstehen, so das im Original keine Änderungen durchschlagen und die Arbeit des Suchframeworks nicht beeinflusst wird. 
* Im Bezug zum letzten Punkt soll hier mit einem Setterfreien Datenmodell experimentiert werden. 
* Die Callbacks sollen sicher in das Suchframework integriert sein, so dass eine schlechte/langsame Programmierung im Callback nicht zu Performanceeinbussen oder anderen Seiteneffekten im Suchprozess führt.

## Stakeholder
Bei der Entwicklung ist von folgenden Stakeholder Gruppen auszugehen:
* Frameworknutzer mit native GUI Anwendung (z.B. Swing)
* Frameworknutzer mit Web GUI Anwendung (z.B. SPA via HTML)
* Owner anderer Open Source Projekte (z.B. Owner des DupFinder Projektes)
* Weitere Open Source Contributoren im Internet
