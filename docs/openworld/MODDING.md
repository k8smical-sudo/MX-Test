# API para mods — Mundo Infinito

Mundo Infinito es un **mundo abierto base**: terreno infinito por chunks, guardado, niebla, minimapa y dos dimensiones.
No genera enemigos, estructuras ni abandonados; eso lo añaden otros mods.

## Declararse dependiente
En el mod.json de tu mod:

    "dependencies": ["mundo-infinito"]        // o "softDependencies" si también funciona sin él

El mundo detecta los mods activos que lo declaran, o que traen código Java, y los muestra en el diálogo de mundo nuevo y en el registro.
Al continuar un mundo avisa si faltan mods con los que se creó.

## Eventos (Events.on)
| Evento | Cuándo |
|---|---|
| OpenWorldAPI.MundoAbierto | al crear o continuar un mundo, o cambiar de dimensión |
| OpenWorldAPI.ChunkCargado (cx, cy) | un chunk se publicó; sus casillas ya existen |
| OpenWorldAPI.ChunkDescargado (cx, cy) | un chunk se guardó y se soltó |

Las coordenadas de chunk son virtuales (tile = chunk * 32 + 0..31) y pueden ser negativas.

## Reglas
    OpenWorldAPI.registrarReglas(r -> { r.waveTimer = false; });

Se aplica a las reglas de cada mundo al abrirlo, después de las del mod.

## Consultas
OpenWorldAPI.activo(), dimension(), semilla(), limiteX(), limiteY(), chunkCargado(cx, cy), chunkPermitido(cx, cy), modsDetectados().

## Persistencia
Lo que tu mod coloque en un chunk (edificios de cualquier equipo, incluidos equipos nuevos) se guarda y restaura con él.
En mundos nuevos el mod no descarta nada. El terreno sí es función pura de la semilla y se regenera igual.

## Límite de terreno
Cada mundo puede tener un límite horizontal y vertical en tiles (0 = infinito), centrado en el punto de aparición.
Fuera del límite no se genera ningún chunk y las unidades quedan dentro.
