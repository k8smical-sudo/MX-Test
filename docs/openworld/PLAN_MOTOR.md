# Motor sin ventana: mundo por chunks (fork de MindustryX 160.x)

Base verificada: `Mindustry-master.zip` coincide byte a byte (hash git) con los archivos base de los parches de MindustryX
(`World`, `FogControl`, `BlockIndexer`, `Control`). El fork sale sobre la misma versión que usa el APK.

## Diseño (decidido)
- **Coordenadas virtuales** de 16 bits por eje (`Tile.x/y` son `short`, `Point2.pack` es 16+16): 65 536 x 65 536 casillas,
  sin tocar `pos()`, edificios enlazados (nodos de energía, lógica) ni guardados.
- **Anillo en vez de ventana**: `Tiles.ring(n)` = búfer circular de lado potencia de 2; casilla (x,y) -> hueco `(x&mask, y&mask)`.
  Nada se reubica jamás. Cargar/descargar un chunk = sobrescribir sus 1024 huecos.
- **Dueño por chunk**: `get()` devuelve `null` si el chunk no está cargado (misma semántica que "fuera del mapa").
- **Dos modos en la misma clase**: las partidas normales usan el modo denso, sin cambios de comportamiento.
- **`Tile.array()` -> `tiles.index(x,y)`**: todo array "por casilla" (pathfinder, niebla...) sigue indexándose igual,
  pero ahora con el índice del anillo; al cargar un chunk se reinician sus 1024 entradas.

## Etapas
| # | Etapa | Estado |
|---|-------|--------|
| 1 | `Tiles` en anillo + `Tile.array()` + pruebas | **hecha** (`0001`) |
| 2 | Carga barata de chunk: constructor crudo, `publishChunk`, eventos `ChunkLoad/UnloadEvent`, `World.publishChunk/unloadChunk`, `Tiles.chunkHasBuildings` (`0002`, `0003`; ver `ETAPA2.md`) | **hecha** |
| 3a | `World.openRing`, límites de entidades virtuales, `addDarkness`/`getDarkness` sin bordes falsos, `ChunkDarkness` (oscuridad exacta por chunk + detección de chunk **macizo**), `Tiles.capacity()` (`0004`) | **hecha** (52 + oscuridad contra vanilla; `World` solo en sintaxis) |
| 3b | `Pathfinder` en anillo: casilla "no cargada", `packChunk`/`unpackChunk` por evento, vecinos por `Tiles.neighborIndex`, `getTargetTile` con `flowIndex` (`0005`) | **hecha** (equivalencia con BFS denso: 25 mapas, 3,3 M de casillas, 0 distintas; el resto solo en sintaxis) |
| 3c | `BlockIndexer` en anillo (`0007`): menas por cuadrantes de 16 (`RingOreIndex`, alineados a los chunks: descargar = 4 entradas), `indexChunk`/`unindexChunk` por evento, `findClosestOre/WallOre` sobre los cuadrantes cargados, y los árboles de edificios **y de torretas** con límites virtuales | **hecha** (`RingOreIndex`: 1 331 comprobaciones con `LongMap`/`IntSeq` reales; el resto solo en sintaxis) |
| 3d | `FogControl` en anillo (`0006`): `isDiscovered/isVisibleTile` por `index`/`in`, `hline` por chunks cargados (`Tiles.setBitsRow`), eventos decodificados **con signo**, red de seguridad al descargar, `takeStaticChunk`/`putStaticChunk` para que el mod guarde lo explorado. `FogRenderer` pasa a la etapa 4 | **hecha** (fila de bits e ida y vuelta de chunk probadas con la `Bits` real: 64 comprobaciones; `FogControl` solo en sintaxis) |
| 3e | Sistemas pequeños (`0007`): `AvoidanceProcess` (índice del anillo, solo chunks cargados), `World.packArray`, `Fires`, objetivos de `PositionTarget`, orden de dibujo de `Conveyor`, `@mapw/@maph` de lógica. **No aplican** al mundo abierto: `WaveSpawner`, `BaseBuilderAI` (oleadas; `getc` devuelve null), `Astar` (solo generadores de mapas), `LExecutor` (área del mapa de reglas), `Logic`; `LaserBulletType` era un falso positivo. `Puddles` ya era correcto | **hecha** (solo sintaxis) |
| 3f | `ControlPathfinder`: **no se reescribe**; cerrado en modo anillo (`updateTile` no hace nada; `getPathPosition` devuelve línea recta) con el comentario de cuándo reconsiderarlo (`0005`) | **hecha** (solo sintaxis) |
| 4 | `InputHandler` (árboles de planes, `0005`) **hecho**; pendientes de renderizado: **`FogRenderer`** (diseño abajo), `BlockRenderer` (15 sitios, 5 quadtrees: líneas 236-240 usan límites de mapa), `MinimapRenderer` (15) + `Minimap` + `MinimapFragment` (recorte de la vista), `FloorRenderer`, `EnvRenderers` (partículas de clima solo en `[0, unitWidth)`), `OverlayRenderer` (Voronoi con límites del mapa) | parcial |

### Límites del mundo (hallazgo grave de la 3c/3e)
Una búsqueda más amplia (`unitWidth()/unitHeight()`, subclases de `QuadTree`) destapó lo que la primera auditoría no veía. **Corregido en `0007`:**
- **`UnitComp.update`: toda unidad con `bounded` se empujaba hacia `[0, unitWidth] x [0, unitHeight]` y se MATABA al salir** (`finalWorldBounds`). En un anillo habría muerto cualquier unidad en coordenadas negativas o más allá del lado del búfer. Ahora los límites son el rango virtual.
- **`MobileInput`: la cámara se recortaba a ese mismo rectángulo** (en Android no podrías alejarte del origen). Ahora se recorta al rango virtual.
- **`turretTree`** (`TurretQuadtree`, subclase que mi `grep new QuadTree` no vio) y **`UnitGroup`** (`IntQuadTree`, física de unidades) usaban `Rect(0, 0, unitWidth, unitHeight)`: sin torretas objetivo ni separación entre unidades fuera de ese rectángulo.
Esta es la lección: buscar por el tipo (`unitWidth`, `finalWorldBounds`, `extends QuadTree`), no solo por el constructor.

### Regla de macizos: "en la oscuridad no hay nada"
Una pared tan gruesa que cae en oscuridad total (todas las casillas del chunk en nivel `darkRadius + 1`) no contiene ni genera nada.
- **Mod (hecho):** estructuras cuyo centro está enterrado en roca no se generan (`Estructuras.tipo`, con veredicto memorizado por celda);
  los enemigos errantes no nacen en roca; un chunk sin nada visible sale vacío del cocinador y **no consume malla de GPU**.
- **Motor (hecho):** `ChunkDarkness.compute` devuelve si el chunk es macizo (0 discrepancias con vanilla en 1 350 chunks).
- **Etapa 5 (pendiente):** el adaptador no publica los macizos. Verificado en el código: `World.solid`, `EntityCollisions.solid/waterSolid` y
  `Build.validPlace` tratan una casilla ausente como sólida y no construible; el pathfinder la verá como no cargada. Ahorra sus casillas, su
  memoria y todos los índices. En el modo ventana actual del mod NO se puede omitir la escritura (el array denso exige contenido).

### Requisito R1: fábricas lejanas que sigan funcionando (para cuando acabemos estas fases)
El mod ya tiene producción **al volver** (`Ausencia`: simula cada edificio restaurado, tope 6 min reales + extrapolación hasta 8 h). Lo que
no resuelve son redes que dependen unas de otras mientras el jugador no está. **Hallazgo que condiciona el motor:** el anillo no puede mantener
islas activas en sitios arbitrarios, porque dos chunks con las mismas coordenadas módulo el lado comparten hueco. Probabilidad de que una isla
remota quepa sin chocar con el vecindario cargado del jugador (Monte Carlo):

| anillo | jugador radio 4 | radio 6 | radio 8 |
|---|---|---|---|
| 16x16 chunks (512 casillas, 0,26 M) | isla 4x4: 49 % | 12 % | 0 % |
| 32x32 chunks (1024 casillas, 1,05 M) | 87 % | 78 % | 67 % |

Salidas: (a) simulación abstracta por tasas para lo descargado (no usa casillas: el anillo basta); (b) cargar islas activas, que exige cambiar el
reparto de huecos del anillo por **bloques libres** (un asignador en vez de `x & mask`). Para no cerrar esa puerta, desde ya: **nunca deducir
coordenadas de un índice** (buscar la casilla con `geti` y leer su x/y) y dimensionar con `Tiles.capacity()`, no con `width * height`.

### Niebla: hallazgos y diseño
- **`FogEvent` guarda x e y en 16 bits SIN signo** (el getter del struct enmascara: `(tipo)((valor >>> offset) & máscara)`). Un evento en `x = -5` se leería como `65531` y se descartaría
  sin avisar: la niebla no revelaría nada en coordenadas negativas. `FogControl` decodifica con `(short)` solo en anillo (`eventX/eventY`); **`FogRenderer.renderEvent` (líneas 129 y 135) tiene el mismo problema.**
- **Persistencia:** el mod debe llamar a `fogControl.takeStaticChunk(team, cx, cy, long[16])` ANTES de `world.unloadChunk` (lee y borra lo explorado), y a `putStaticChunk` DESPUÉS de `publishChunk`.
  Al descargar, `FogControl` además limpia los tres mapas de bits del hueco, para que el chunk siguiente no herede niebla ajena.
- **Límite conocido:** un revelado cuyo chunk no esté cargado en ese momento se pierde (los radios de niebla son de decenas de casillas y el vecindario cargado es mucho mayor). Hay una carrera benigna entre
  el hilo de niebla y la descarga: como mucho un puñado de casillas quedan mal marcadas hasta que se vuelvan a revelar.
- **`FogRenderer` (etapa 4, sin código a propósito: es GPU y no puedo ejecutarla aquí):** sus framebuffers miden `world.width() x world.height()` texels (un texel por casilla); en anillo miden el lado del anillo
  (potencia de 2: admite `TextureWrap.repeat` incluso en GLES2). `copyFromCpu` ya funciona tal cual (`i % ww` es la coordenada de texel del anillo). Hay que: (1) decodificar eventos con `(short)`;
  (2) dibujar los polígonos dinámicos en coordenadas `x mod (W*8)`, repetidos una vez más cuando cruzan la costura; (3) pintar la textura sobre la cámara con UV repetidos en un solo quad en vez de
  `Draw.fbo(tex, w, h, tilesize)` sobre `[0, w*8)`; (4) al publicar o descargar un chunk, limpiar y repintar su cuadro de 32x32 texels.

### Quadtrees con límites de 524 288 unidades: medido
Código de Arc verificado: `QuadTree` **no limita la profundidad** (solo `minNodeSize = 10` y `maxObjectsPerNode = 5`) y `insert` **ignora en silencio** lo que queda fuera de
sus límites. Los árboles dinámicos se reconstruyen cada fotograma (`EntityCollisions` hace `fill`). Medido con el `QuadTree` real (sandbox, una sola ejecución, con ruido):

| entidades agrupadas | raíz 4 500 u (mapa clásico): fill / insert / 200 consultas | raíz 524 788 u (mundo virtual) |
|---|---|---|
| 300 en ±400 u | 43-56 / 88-124 / 60-97 µs | 28-43 / 36-79 / 41-83 µs (no peor) |
| 800 en ±1 500 u | 67-72 / 52-72 / 32-41 µs | 109-138 / 122-146 / 49-72 µs |
| 2 000 en ±4 000 u | 193-227 / 87-98 / 18-25 µs | 316-331 / 331-362 / 75-78 µs |

Sobrecoste: hasta ~+130 µs por reconstrucción y grupo con 2 000 entidades (~0,8 % de un fotograma a 60 fps) y ~0,1-0,4 µs más por consulta. Es aceptable; no medido en Android.
Optimización posible si el perfil lo pide: límites flotantes alrededor de la ventana (los árboles dinámicos se rehacen cada fotograma; los estáticos habría que reinsertarlos).
Sitios que crean `QuadTree` (todos revisados): `Teams`, `PhysicsProcess`, `Groups.resize` ya usan `getQuadBounds` (`0004`); `BlockIndexer` e `InputHandler` usaban `Rect(0, 0, unitWidth, unitHeight)` y ahora `getTreeBounds` (`0005`).

## Riesgos conocidos (no resueltos)
- Los flowfields del pathfinder resuelven sobre todo el anillo; con un núcleo fuera del anillo no hay camino (política a decidir:
  IA solo cerca del jugador, el mod ya hiberna enemigos lejanos).
- Edificios que cruzan chunks: hay que mantener cargados juntos todos los chunks que toque un edificio.
- Entidades a >200 000 unidades del origen pierden precisión de `float` (~0,03 unidades): aceptable, no desaparece.
- `getc()` en modo anillo devuelve `null` (no hay borde al que acotar): lo usa `BaseBuilderAI` (IA de bases enemigas).
- Multijugador/guardado nativo: fuera de alcance; el mundo abierto usa el guardado por regiones del mod.
- Todo lo medido (get ~6-7 ns, chunk ~12 us) es en un sandbox con `Tile` simplificado; confirmar en el APK.
