# Etapa 2: cargar un chunk de la forma más barata

## 1. Qué cuesta hoy escribir una casilla (verificado en el código)
`setBlock` -> `preChanged` (evento `TilePreChange`) -> `changeBuild` -> comprobación de multibloque -> `changed()` (4 búsquedas de
vecinos + evento `TileChange`) -> `blockChanged`. `setFloor`/`setOverlay` disparan además `pathfinder.updateTile`, `removeFloorIndex`
y sus propios eventos. Cada evento llega a **7 suscriptores**:

| Suscriptor | Qué hace por casilla |
|---|---|
| `BlockIndexer` | `removeIndex`/`addIndex` (recorre `getLinkedTiles`, `drop()`, `wallDrop()`), `floorMap.remove(tile)` es **O(n)** |
| `Pathfinder` | `updateTile` + recorrido de 4 vecinos para `nearSolid` |
| `ControlPathfinder` | `updateTile` |
| `FogControl` | filtro de edificios con radio de niebla |
| `BlockRenderer` | quita/inserta en 5 quadtrees (`block`, `blockLight`, `blockCached`, `floor`, `overlay`) + cola de sombras |
| `MinimapRenderer` | repinta el píxel (y el de debajo si es sólido) |
| `World` | contadores |

`World.isGenerating()` silencia los eventos, `recache` y `pathfinder.updateTile`, pero también deja a los 7 suscriptores sin enterarse.

## 2. Métodos comparados
| | Método | Dónde corre | Cuesta en el hilo principal | Veredicto |
|---|---|---|---|---|
| A | `setFloor/setOverlay/setBlock` normales (tu mod hoy) | mod | 7 suscriptores x 1024 casillas | descartado |
| B | A, pero con `world.setGenerating(true)` alrededor (**ese setter es público**) | mod | sin eventos, pero cada setter aún compara, llama a `floorChanged`, `changeBuild`, `blockChanged`; y los índices quedan obsoletos | atajo intermedio, ver 5 |
| C | Fork: constructor crudo (solo campos) en el hilo principal | fork | crear y rellenar 1024 objetos | mejor, pero evitable |
| **D** | **Fork: un trabajador construye `Tile[1024]` con el constructor crudo y el hilo principal solo lo *publica*** | **fork** | **~0,3 µs publicar + ~1,7 µs liberar** | **elegido** |

Por qué D es posible: el constructor crudo `Tile(x, y, floor, overlay, wall, data)` solo asigna campos (no llama a `changed()` ni a
`changeBuild()`), y el terreno no tiene `Building` (`hasBuilding() = destructible || update`, falso para muros y rocas). Las casillas
del bloque no son alcanzables por nadie hasta publicarlas, así que construirlas en un hilo de fondo es seguro.

Medido en el sandbox con un `Tile` simplificado (confirmar en el APK): `publishChunk` 0,3 µs, `releaseChunk` 1,7 µs, crear las
1024 casillas en el hilo principal ~13 µs, `get()` en anillo ~2 ns más que en denso. **No** he medido el coste real del camino A.

## 3. Flujo de un chunk (D)
1. (trabajador) el mod ya genera `ChunkData`; ahora además construye `Tile[1024]` con el constructor crudo, y calcula la oscuridad.
2. (principal, si el hueco está ocupado) `ChunkUnloadEvent` **antes** de `releaseChunk`: los suscriptores aún necesitan las casillas
   para sacarlas de sus índices (`floorMap`, quadtrees).
3. (principal) `releaseChunk` del dueño anterior y `publishChunk` del nuevo.
4. (principal) `ChunkLoadEvent`: cada suscriptor hace **una** pasada por el chunk en vez de 1024 eventos (etapa 3).
5. Los edificios del chunk se colocan después con `setBlock` normal, ya publicado: son pocos.

## 4. Hecho en la etapa 2
- `0002`: `Tile(x,y,floor,overlay,wall,data)` y `Tiles.publishChunk`.
- `0003`: `ChunkLoadEvent` / `ChunkUnloadEvent` (en `EventType`), `World.isRing/publishChunk/unloadChunk` y `Tiles.chunkHasBuildings`.
  `unloadChunk` dispara el evento **antes** de liberar y devuelve `false` sin hacer nada si el chunk aún tiene un `Building` (también
  los de multibloques que vienen de un chunk vecino): quien llama debe guardarlos y quitarlos antes.
- 50 comprobaciones sin fallos sobre el `Tiles.java` real. `World.java` y `EventType.java` solo están verificados en **sintaxis**
  (no compilan sin el resto del motor y sus clases generadas); la compilación real queda para cuando exista el fork construible.
- Los manejadores de los 7 suscriptores son la etapa 3.

## 5. Instalación: fork, no archivos en tu repositorio
**Las piezas del motor van en un fork de MindustryX. Tu repositorio `MindustryOpenWorldTest` sigue siendo el mod.** Tres razones
comprobadas en el código:
- `ModClassLoader` de vanilla es **padre primero**: "si no existe en el cargador principal, prueba los hijos". Un JAR de mod con su
  propio `mindustry.world.Tiles` nunca se usaría.
- Tu mod compila contra `Anuken:Mindustry:<versión>` **vanilla**, que no tiene `Tiles.ring` ni `publishChunk`.
- `Tile.array()` y los 132 sitios críticos están en clases del núcleo.

Reparto:
- **Fork de MindustryX** (nuevo parche `0081-...` en `patches/client/`, siguiendo su flujo patch-first de `AGENTS.md`): `Tiles`,
  `Tile`, eventos de chunk, y las etapas 3-4. Lo construyes como APK propio, o como *Loader Mod* (su cargador es **hijo primero**,
  por eso puede reemplazar clases del núcleo; no he comprobado que sirva para nuestros parches).
- **`MindustryOpenWorldTest`**: generación, caché, malla, regiones y `Motor`, más un adaptador fino que detecta el motor por
  reflexión (`Tiles.ring`): si existe usa `publishChunk`; si no, cae en la `Ventana` actual. Así el mod no se rompe en vanilla ni en
  tu APK actual.
- Los parches de este paquete son diffs simples sobre el código vanilla; hay que aplicarlos en `work/` y regenerarlos con
  `scripts/genPatches.sh`. `Tile`/`Tiles` no los toca ningún parche de MindustryX (0 conflictos). En la etapa 3-4 sí: `FogControl` (1),
  `BlockIndexer` (1), `World` (1), `BlockRenderer` (3), `MinimapRenderer` (2) ya están modificados por MindustryX.

## 6. Atajo opcional en el mod, sin fork (método B)
Envolver `Streamer.aplicarTramo` con `Vars.world.setGenerating(true/false)` quitaría los eventos por casilla. **Riesgo:** pathfinder,
indexer de ores y minimapa no se enterarían de los cambios; hoy el mod depende de esos eventos. Solo merece la pena si antes se
añade una resincronización por chunk, y no lo he probado. No sustituye al fork.
