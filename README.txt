GRUP 1: Llenguatges de Programació 2023-2024

Integrants: Oriol González      oriol.gg
            Alèxia Cabrera      alexia.cabrera
            Àlex Ferré          alex.fl

Versió de JDK utilitzada: 19.0+

A l'hora d'executar, cal entrar com a paràmetre la ruta i el nom del fitxer que es vol llegir. Per exemple:
src/test/resources/ExempleFibonacci.farm
És una ruta vàlida d'un fitxer que hem usat per fer proves.

A més cal instal·lar les dependències de Maven, per tal de poder executar el programa. El fitxer pom.xml ja conté les dependències necessàries.
L'IDE d'IntelliJ permet tant instal·lar les dependències com executar fàcilment i és el que hem usat tots els membres de l'equip, recomanem fer-lo servir.

La classe PrettyPrintTree permet mostrar tot l'arbre sintàctic generat per el parser recursiu descendent; a més, el fitxer
de MIPS generat amb la compilació del codi es troba a "target/farm.asm".