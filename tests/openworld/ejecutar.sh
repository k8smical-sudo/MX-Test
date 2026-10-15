#!/bin/bash
# Con los parches 0001-0007 aplicados a core/:
#   1) compila Tiles.java y ChunkDarkness.java REALES contra stubs mínimos de Arc/gen (más la Bits real de Arc) y corre las pruebas
#      (Tiles, flowfield, niebla por filas/chunks, oscuridad/macizo) + benchmark
#   2) comprueba la SINTAXIS de los archivos del motor tocados (no compilan solos: dependen del resto del motor y de clases generadas)
#   3) si pasas la ruta de Arc: pruebas de RingOreIndex con LongMap/IntSeq reales, y benchmark del QuadTree real
# Uso: ./ejecutar.sh /ruta/a/Mindustry/core/src/mindustry [/ruta/a/Arc/arc-core]
# (con Arc, javac muestra 4 errores de arc/graphics/Pixmap.java: es una dependencia transitiva ajena y las clases probadas se generan igual)
set -e
M="${1:?ruta a core/src/mindustry}"
ARC="${2:-}"
rm -rf out stubtmp pcout && cp -r stub stubtmp && cp "$M/world/Tiles.java" "$M/world/ChunkDarkness.java" stubtmp/mindustry/world/ && mkdir out pcout
javac -d out $(find stubtmp -name '*.java') src/TilesTest.java src/DarknessTest.java src/Bench.java
java -cp out TilesTest && java -cp out DarknessTest && java -cp out Bench
javac -d pcout herramientas/ParseCheck.java
java -cp pcout ParseCheck "$M/core/World.java" "$M/game/EventType.java" "$M/game/FogControl.java" "$M/world/Tile.java" "$M/world/Tiles.java" "$M/world/ChunkDarkness.java" \
  "$M/ai/Pathfinder.java" "$M/ai/ControlPathfinder.java" "$M/ai/BlockIndexer.java" "$M/ai/RingOreIndex.java" "$M/ai/UnitGroup.java" "$M/input/InputHandler.java" "$M/input/MobileInput.java" \
  "$M/async/AvoidanceProcess.java" "$M/entities/Fires.java" "$M/entities/comp/UnitComp.java" "$M/logic/GlobalVars.java" "$M/world/blocks/distribution/Conveyor.java"
if [ -n "$ARC" ]; then
  rm -rf ore_src ore_out && mkdir -p ore_src/mindustry/ai ore_out && cp "$M/ai/RingOreIndex.java" ore_src/mindustry/ai/ && cp ores/OreTest.java ore_src/mindustry/ai/
  javac -proc:none -nowarn -sourcepath "$ARC/src:$ARC/unsafe" -d ore_out ore_src/mindustry/ai/RingOreIndex.java ore_src/mindustry/ai/OreTest.java 2>&1 | grep "error:" | grep -v Pixmap || true
  java -cp ore_out mindustry.ai.OreTest
fi
