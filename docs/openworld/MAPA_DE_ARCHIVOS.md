# Mapa de archivos: qué parche toca qué archivo del motor

Todas las rutas son relativas a `work/` (el Mindustry parcheado). `NUEVO` = el parche crea el archivo; `MX` = MindustryX ya modifica ese archivo (la fusión se verificó: no se perdió ninguna línea suya).


## `0001-OW-Tiles-modo-anillo-con-coordenadas-virtuales-y-Til.patch`
OW(Tiles): modo anillo con coordenadas virtuales y Tile.array() por indice

- `core/src/mindustry/world/Tile.java`
- `core/src/mindustry/world/Tiles.java`

## `0002-OW-Tiles-publishChunk-y-constructor-crudo-de-Tile-pa.patch`
OW(Tiles): publishChunk y constructor crudo de Tile para terreno

- `core/src/mindustry/world/Tile.java`
- `core/src/mindustry/world/Tiles.java`

## `0003-OW-World-ChunkLoad-UnloadEvent-World.publishChunk-un.patch`
OW(World): ChunkLoad/UnloadEvent, World.publishChunk/unloadChunk y chunkHasBuildings

- `core/src/mindustry/core/World.java` — MX
- `core/src/mindustry/game/EventType.java`
- `core/src/mindustry/world/Tiles.java`

## `0004-OW-World-openRing-limites-virtuales-ChunkDarkness-y-.patch`
OW(World): openRing, limites virtuales, ChunkDarkness y getDarkness sin bordes

- `core/src/mindustry/core/World.java` — MX
- `core/src/mindustry/world/ChunkDarkness.java` — NUEVO
- `core/src/mindustry/world/Tiles.java`

## `0005-OW-Pathfinder-flowfield-en-anillo-neighborIndex-y-Co.patch`
OW(Pathfinder): flowfield en anillo, neighborIndex y ControlPathfinder cerrado

- `core/src/mindustry/ai/BlockIndexer.java` — MX
- `core/src/mindustry/ai/ControlPathfinder.java`
- `core/src/mindustry/ai/Pathfinder.java`
- `core/src/mindustry/core/World.java` — MX
- `core/src/mindustry/input/InputHandler.java` — MX
- `core/src/mindustry/world/Tiles.java`

## `0006-OW-Fog-FogControl-en-anillo-eventos-con-signo-y-bits.patch`
OW(Fog): FogControl en anillo, eventos con signo y bits por chunk

- `core/src/mindustry/game/FogControl.java` — MX
- `core/src/mindustry/world/Tiles.java`

## `0007-OW-Core-limites-de-unidades-y-camara-menas-por-chunk.patch`
OW(Core): limites de unidades y camara, menas por chunk y sistemas pequenos

- `core/src/mindustry/ai/BlockIndexer.java` — MX
- `core/src/mindustry/ai/Pathfinder.java`
- `core/src/mindustry/ai/RingOreIndex.java` — NUEVO
- `core/src/mindustry/ai/UnitGroup.java`
- `core/src/mindustry/async/AvoidanceProcess.java`
- `core/src/mindustry/core/World.java` — MX
- `core/src/mindustry/entities/Fires.java`
- `core/src/mindustry/entities/comp/UnitComp.java`
- `core/src/mindustry/input/MobileInput.java` — MX
- `core/src/mindustry/logic/GlobalVars.java`
- `core/src/mindustry/world/blocks/distribution/Conveyor.java`

## `0008-OW-Floor-FloorRenderer-reload-no-cachea-en-anillo.patch`
OW(Floor): FloorRenderer.reload no cachea en modo anillo

- `core/src/mindustry/graphics/FloorRenderer.java`

## `0009-OW-Render-niebla-sombras-y-oscuridad-en-anillo.patch`
OW(Render): niebla, sombras y oscuridad en modo anillo

- `core/src/mindustry/graphics/BlockRenderer.java`
- `core/src/mindustry/graphics/FogRenderer.java`

## `0010-OW-Combate-balas-y-explosiones-con-coordenadas-virtuales.patch`
OW(Combate): balas y explosiones funcionan con coordenadas virtuales del anillo

- `core/src/mindustry/entities/Damage.java`
- `core/src/mindustry/entities/comp/BulletComp.java`

---
Total: 23 archivos distintos del motor.

## `0012-OW-Render-sombras-oscuridad-y-niebla-en-trozos-sin-w.patch`
OW(Render): sombras, oscuridad y niebla en trozos sin wrap, limpieza al descargar y StaticWall.eq en anillo

- `core/src/mindustry/graphics/RingFbo.java` — NUEVO
- `core/src/mindustry/graphics/BlockRenderer.java` — MX
- `core/src/mindustry/graphics/FogRenderer.java`
- `core/src/mindustry/world/blocks/environment/StaticWall.java`
