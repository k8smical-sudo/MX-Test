# Auditoría: dependencias del mundo de tamaño fijo (Mindustry base de MindustryX 160.x)

Generada automáticamente. **Críticos en tiempo de ejecución: 274 sitios en 40 archivos.** Editor/generadores/guardado/red (no participan en el mundo abierto): 94 sitios en 21 archivos.

> Corrección: la versión anterior de este documento solo buscaba `world.width()`/`tiles.width` y daba 132 sitios. Se omitían las dimensiones cacheadas en variables propias (`wwidth`, `ww`, `quadWidth`, `cwidth`…) y la aritmética de posiciones planas (`pos % wwidth`). `ControlPathfinder` solo aparecía con 4 sitios y tiene decenas.

## A. Críticos en tiempo de ejecución

### `mindustry/ai/ControlPathfinder.java` — 75

- L114: `final int cwidth = Mathf.ceil((float)world.width() / clusterSize), cheight = Mathf.ceil((float)world.height() / clusterSize);`
- L269: `for(int cx = 0; cx < controlPath.cwidth; cx++){`
- L270: `for(int cy = 0; cy < controlPath.cheight; cy++){`
- L272: `var cluster = controlPath.clusters[team][cost][cy * controlPath.cwidth + cx];`
- L325: `int cx = entry.key % controlPath.cwidth, cy = entry.key / controlPath.cwidth;`
- L360: `int x = t.x, y = t.y, mx = x % clusterSize, my = y % clusterSize, cx = x / clusterSize, cy = y / clusterSize, cluster = cx + cy * cwidth;`
- L379: `if(cx >= 0 && cy >= 0 && cx < cwidth && cy < cheight){`
- L380: `queue.post(() -> clustersToUpdate.add(cx + cy * cwidth));`
- L420: `return getCluster(team, pathCost, cx + cy * cwidth);`
- L441: `return getCreateCluster(team, pathCost, cx + cy * cwidth);`
- L448: `return updateCluster(team, pathCost, clusterIndex % cwidth, clusterIndex / cwidth);`
- L466: `dim2 = dim1[pathCost] = new Cluster[cwidth * cheight];`
- L469: `Cluster cluster = dim2[cy * cwidth + cx];`
- L471: `cluster = dim2[cy * cwidth + cx] = new Cluster();`
- L486: `if(otherX < 0 || otherY < 0 || otherX >= cwidth || otherY >= cheight){`
- L490: `Cluster other = dim2[otherX + otherY * cwidth];`
- L558: `int minX = cx * clusterSize, minY = cy * clusterSize, maxX = Math.min(minX + clusterSize - 1, wwidth - 1), maxY = Math.min(minY + clusterSize - 1, whe`
- L606: `x + y * wwidth,`
- L607: `otherX + otherY * wwidth,`
- L633: `int x = a % wwidth, x2 = b % wwidth, y = a / wwidth, y2 = b / wwidth;`
- L673: `int cx = current % wwidth, cy = current / wwidth;`
- L682: `int next = newx + wwidth * newy;`
- L718: `return NodeIndex.get(cx + cy * cwidth, dir, portal);`
- L727: `if(cx < 0 || cy < 0 || cx >= cwidth || cy >= cheight){`
- L733: `int minX = cx * clusterSize, minY = cy * clusterSize, maxX = Math.min(minX + clusterSize - 1, wwidth - 1), maxY = Math.min(minY + clusterSize - 1, whe`
- L757: `tileX + tileY * wwidth,`
- L758: `otherX + otherY * wwidth,`
- L795: `x1 = (moveDirs[dirA * 2] * averageA + (clusterA % cwidth) * clusterSize + offsets[dirA * 2] * (clusterSize - 1) + nextOffsets[dirA * 2] / 2f),`
- L796: `y1 = (moveDirs[dirA * 2 + 1] * averageA + (clusterA / cwidth) * clusterSize + offsets[dirA * 2 + 1] * (clusterSize - 1) + nextOffsets[dirA * 2 + 1] / `
- L799: `x2 = (moveDirs[dirB * 2] * averageB + (clusterB % cwidth) * clusterSize + offsets[dirB * 2] * (clusterSize - 1) + nextOffsets[dirB * 2] / 2f),`
- L800: `y2 = (moveDirs[dirB * 2 + 1] * averageB + (clusterB / cwidth) * clusterSize + offsets[dirB * 2 + 1] * (clusterSize - 1) + nextOffsets[dirB * 2 + 1] / `
- L841: `int cx = cluster % cwidth, cy = cluster / cwidth;`
- L844: `if(cx >= cwidth || cy >= cheight || cx < 0 || cy < 0) continue;`
- L856: `if(nextCx >= 0 && nextCy >= 0 && nextCx < cwidth && nextCy < cheight){`
- L919: `int baseX = tile % wwidth, baseY = tile / wwidth;`
- L920: `int curWeightIndex = (baseX / clusterSize) + (baseY / clusterSize) * cwidth;`
- L935: `if(clx < 0 || cly < 0 || dx >= wwidth || dy >= wheight) continue;`
- L937: `int nextWeightIndex = clx + cly * cwidth;`
- L944: `int newPos = tile + point.x + point.y * wwidth;`
- L975: `addFlowCluster(cache, cluster % cwidth, cluster / cwidth, addingFrontier);`
- L980: `if(cx < 0 || cy < 0 || cx >= cwidth || cy >= cheight) return;`
- L983: `int key = cx + cy * cwidth;`
- L992: `if(ox < 0 || oy < 0 || ox >= cwidth || oy >= cheight) continue;`
- L994: `var otherField = fields.get(ox + oy * cwidth);`
- L1014: `cache.frontier.addFirst(worldX + worldY * wwidth);`
- L1029: `int goalPos = (goalX + goalY * wwidth);`
- L1078: `ox = cluster % cwidth + dx,`
- L1079: `oy = cluster / cwidth + dy;`
- L1084: `if(ox >= 0 && oy >= 0 && ox < cwidth && oy < cheight){`
- L1085: `int other = ox + oy * cwidth;`
- L1151: `actualDestPos = actualDestX + actualDestY * wwidth,`
- L1152: `initialCost = tileOn == null ? 0 : cost.getCost(team, pathfinder.tiles[tileOn.array()]),`
- L1153: `destPos = destX + destY * wwidth;`
- L1199: `(Time.timeSinceMillis(request.lastRecomputeTime) < 1000 && Mathf.dst(destX, destY, request.destination % wwidth, request.destination / wwidth) <= 4f))`
- L1282: `int a = current.array();`
- L1354: `initializePathRequest(request, request.team, request.costId, request.unit.tileX(), request.unit.tileY(), request.destination % wwidth, request.destina`
- L1359: `int[] field = cache.fields.get(x / clusterSize + (y / clusterSize) * cwidth);`
- L1375: `int ww = wwidth, wh = wheight;`
- L1380: `while(x >= 0 && y >= 0 && x < ww && y < wh){`
- L1381: `if(avoid(team, type, x + y * wwidth)) return true;`
- L1402: `int ww = world.width(), wh = world.height();`
- L1407: `while(x >= 0 && y >= 0 && x < ww && y < wh){`
- L1408: `if(avoid(team, type, x + y * wwidth)) return Point2.pack(x, y);`
- L1425: `if(x < 0 || y < 0 || x >= wwidth || y >= wheight) return false;`
- L1426: `if(!nearPassable(initialCost, team, type, x + y * wwidth)){`
- L1433: `int ww = wwidth, wh = wheight;`
- L1438: `while(x >= 0 && y >= 0 && x < ww && y < wh){`
- L1440: `!nearPassable(initialCost, team, type, x + y * wwidth) ||`
- L1481: `return x < 0 || y < 0 || x >= wwidth || y >= wheight || solid(team, type, x + y * wwidth, true);`
- L1491: `int x = tilePos % wwidth, y = tilePos / wwidth;`
- L1500: `int index = cx + cy * cwidth;`
- L1521: `updateCluster(team, pathCost, clusterIndex % cwidth, clusterIndex / cwidth);`
- L1522: `clusterChanged(team, pathCost, clusterIndex % cwidth, clusterIndex / cwidth);`
- L1539: `updateInnerEdges(team, pathCost, clusterIndex % cwidth, clusterIndex / cwidth, cluster);`
- L1540: `clusterChanged(team, pathCost, clusterIndex % cwidth, clusterIndex / cwidth);`

### `mindustry/ai/Pathfinder.java` — 19

- L32: `static int wwidth, wheight;`
- L120: `tiles = new int[world.width() * world.height()];`
- L121: `wwidth = world.width();`
- L122: `wheight = world.height();`
- L128: `Tile tile = world.tiles.geti(i);`
- L176: `int arr = other.array();`
- L242: `if(solid && !tile.block().teamPassable && other.array() < tiles.length){`
- L243: `tiles[other.array()] |= PathTile.bitMaskNearSolid;`
- L280: `return tiles[x + y * wwidth];`
- L310: `int pos = t.array();`
- L409: `int ww = path.width;`
- L410: `int apos = tile.x/res + tile.y/res * ww;`
- L424: `int packed = dx/res + dy/res * ww;`
- L429: `!(point.x != 0 && point.y != 0 && (!path.passable(((tile.x + point.x)/res + tile.y/res*ww)) || !path.passable((tile.x/res + (tile.y + point.y)/res*ww)`
- L568: `out.add(other.tile.array());`
- L580: `out.add(other.tile.array());`
- L586: `out.add(other.array());`
- L650: `this.width = Mathf.ceil((float)wwidth / resolution);`
- L651: `this.height = Mathf.ceil((float)wheight / resolution);`

### `mindustry/core/World.java` — 18

- L91: `return tiles.width;`
- L95: `return tiles.height;`
- L98: `public int unitWidth(){`
- L102: `public int unitHeight(){`
- L179: `return x + y * tiles.width;`
- L197: `if(tiles.width != width || tiles.height != height){`
- L234: `Groups.resize(-finalWorldBounds, -finalWorldBounds, tiles.width * tilesize + finalWorldBounds * 2, tiles.height * tilesize + finalWorldBounds * 2);`
- L400: `byte[] dark = new byte[tiles.width * tiles.height];`
- L401: `byte[] writeBuffer = new byte[tiles.width * tiles.height];`
- L406: `Tile tile = tiles.geti(i);`
- L414: `int idx = tile.y * tiles.width + tile.x;`
- L418: `int nidx = newY * tiles.width + newX;`
- L431: `int idx = tile.y * tiles.width + tile.x;`
- L441: `int nidx = py * tiles.width + px;`
- L492: `edgeDst = Math.min(x, Math.min(y, Math.min(-(x - (tiles.width - 1)), -(y - (tiles.height - 1)))));`
- L509: `float angle = Angles.angle(x, y, tiles.width/2, tiles.height/2) + offset;`
- L518: `float rawDst = Intersector.distanceLinePoint(Tmp.v1.trns(prev, length), Tmp.v2.trns(next, length), Tmp.v3.set(x - tiles.width/2, y - tiles.height/2).r`
- L624: `return tiles.geti(index);`

### `mindustry/graphics/BlockRenderer.java` — 18

- L236: `blockTree = new BlockQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`
- L237: `blockCachedTree = new BlockQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`
- L238: `blockLightTree = new BlockLightQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`
- L239: `overlayTree = new OverlayQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`
- L240: `floorTree = new FloorQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`
- L248: `int chunksx = Mathf.ceil((float)(world.width()) / chunkSize), chunksy = Mathf.ceil((float)(world.height()) / chunkSize);`
- L257: `shadows.resize(world.width(), world.height());`
- L263: `for(Tile tile : world.tiles){`
- L292: `shadows.resize(world.width(), world.height());`
- L298: `for(Tile tile : world.tiles){`
- L312: `dark.resize(world.width(), world.height());`
- L324: `for(Tile tile : world.tiles){`
- L450: `Draw.fbo(dark.getTexture(), world.width(), world.height(), tilesize, tilesize/2f);`
- L506: `float ww = world.width() * tilesize, wh = world.height() * tilesize;`
- L508: `float u = (x - camera.width / 2f) / ww,`
- L509: `v = (y - camera.height / 2f) / wh,`
- L510: `u2 = (x + camera.width / 2f) / ww,`
- L511: `v2 = (y + camera.height / 2f) / wh;`

### `mindustry/game/FogControl.java` — 17

- L22: `private static volatile int ww, wh;`
- L51: `ww = world.width();`
- L52: `wh = world.height();`
- L109: `if(x < 0 || y < 0 || x >= ww || y >= wh) return false;`
- L110: `return data.get(x + y * ww);`
- L122: `return data.read.get(Mathf.clamp(x, 0, ww - 1) + Mathf.clamp(y, 0, wh - 1) * ww);`
- L258: `int tx = unit.tileX(), ty = unit.tileY(), pos = tx + ty * ww;`
- L447: `stream.writeShort(world.width());`
- L448: `stream.writeShort(world.height());`
- L454: `int size = ww * wh;`
- L483: `ww = w;`
- L484: `wh = h;`
- L540: `if(y < 0 || y >= wh) return;`
- L549: `if(x1 >= ww) return;`
- L553: `if(x2 >= ww) x2 = ww - 1;`
- L555: `int off = y * ww;`
- L572: `int len = ww * wh;`

### `mindustry/graphics/MinimapRenderer.java` — 16

- L105: `zoom = Mathf.clamp(amount, 1f, Math.min(world.width(), world.height()) / baseSize / 2f);`
- L119: `pixmap = new Pixmap(world.width(), world.height());`
- L136: `dx = Mathf.clamp(dx, sz, world.width() - sz);`
- L137: `dy = Mathf.clamp(dy, sz, world.height() - sz);`
- L150: `trans.scl(Tmp.v1.set(scaleFactor = w / world.unitWidth(), h / world.unitHeight()));`
- L220: `float wf = world.width() * tilesize;`
- L221: `float hf = world.height() * tilesize;`
- L322: `float sz = Mathf.clamp(baseSize * zoom, baseSize, Math.min(world.width(), world.height()));`
- L325: `dx = Mathf.clamp(dx, sz, world.width() - sz);`
- L326: `dy = Mathf.clamp(dy, sz, world.height() - sz);`
- L329: `float x = dx - sz, y = world.height() - dy - sz, width = sz * 2, height = sz * 2;`
- L336: `for(Tile tile : world.tiles){`
- L371: `dx = Mathf.clamp(dx, sz, world.width() - sz);`
- L372: `dy = Mathf.clamp(dy, sz, world.height() - sz);`
- L392: `if(real == Blocks.air && tile.y < world.height() - 1 && realBlock(world.tile(tile.x, tile.y + 1)).solid){`
- L394: `}else if(tile.floor().isLiquid && (tile.y >= world.height() - 1 || !world.tile(tile.x, tile.y + 1).floor().isLiquid)){`

### `mindustry/ai/BlockIndexer.java` — 14

- L32: `private int quadWidth, quadHeight;`
- L81: `quadWidth = Mathf.ceil(world.width() / (float)quadrantSize);`
- L82: `quadHeight = Mathf.ceil(world.height() / (float)quadrantSize);`
- L94: `for(Tile tile : world.tiles){`
- L104: `if(ores[drop.id] == null) ores[drop.id] = new IntSeq[quadWidth][quadHeight];`
- L111: `if(wallOres[drop.id] == null) wallOres[drop.id] = new IntSeq[quadWidth][quadHeight];`
- L246: `if(ores[drop.id] == null) ores[drop.id] = new IntSeq[quadWidth][quadHeight];`
- L259: `if(wallOres[wallDrop.id] == null) wallOres[wallDrop.id] = new IntSeq[quadWidth][quadHeight];`
- L531: `for(int qx = 0; qx < quadWidth; qx++){`
- L532: `for(int qy = 0; qy < quadHeight; qy++){`
- L558: `for(int qx = 0; qx < quadWidth; qx++){`
- L559: `for(int qy = 0; qy < quadHeight; qy++){`
- L622: `data.buildingTree = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`
- L628: `data.turretTree = new TurretQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));`

### `mindustry/ai/Astar.java` — 10

- L30: `Tiles tiles = world.tiles;`
- L35: `GridBits closed = new GridBits(tiles.width, tiles.height);`
- L37: `if(costs == null || costs.length != tiles.width * tiles.height){`
- L38: `costs = new float[tiles.width * tiles.height];`
- L44: `queue.comparator = Structs.comparingFloat(a -> costs[a.array()] + dh.cost(a.x, a.y, end.x, end.y));`
- L46: `if(rotations == null || rotations.length != world.width() || rotations[0].length != world.height()){`
- L47: `rotations = new byte[world.width()][world.height()];`
- L53: `float baseCost = costs[next.array()];`
- L61: `if(Structs.inBounds(newx, newy, tiles.width, tiles.height)){`
- L68: `costs[child.array()] = newCost;`

### `mindustry/graphics/FogRenderer.java` — 9

- L49: `boolean clearStatic = staticFog.resizeCheck(world.width(), world.height());`
- L51: `dynamicFog.resize(world.width(), world.height());`
- L114: `Draw.fbo(dynamicFog.getTexture(), world.width(), world.height(), tilesize);`
- L119: `Draw.fbo(staticFog.getTexture(), world.width(), world.height(), tilesize, tilesize/2f);`
- L139: `staticFog.resize(world.width(), world.height());`
- L143: `int ww = world.width(), wh = world.height();`
- L146: `int len = world.width() * world.height();`
- L151: `int x = i % ww, y = i / ww;`
- L154: `if(x > 0 && y > 0 && x < ww - 1 && y < wh - 1){`

### `mindustry/ai/WaveSpawner.java` — 7

- L113: `Angles.angle(x, y, world.width()/2f * tilesize, world.height()/2f * tilesize), state.wave - 1, this::spawnEffect);`
- L152: `Geometry.circle(tx, ty, world.width(), world.height(), 3, (x, y) -> {`
- L182: `float angle = Angles.angle(world.width() / 2f, world.height() / 2f, tile.x, tile.y);`
- L183: `float trns = Math.max(world.width(), world.height()) * Mathf.sqrt2 * tilesize;`
- L184: `float spawnX = Mathf.clamp(world.width() * tilesize / 2f + Angles.trnsx(angle, trns), -margin, world.width() * tilesize + margin);`
- L185: `float spawnY = Mathf.clamp(world.height() * tilesize / 2f + Angles.trnsy(angle, trns), -margin, world.height() * tilesize + margin);`
- L221: `for(Tile tile : world.tiles){`

### `mindustry/async/AvoidanceProcess.java` — 7

- L15: `static int wwidth, wheight;`
- L29: `buffer1 = new int[wwidth * wheight];`
- L30: `buffer2 = new int[wwidth * wheight];`
- L38: `wwidth = Vars.world.width();`
- L39: `wheight = Vars.world.height();`
- L100: `if(x >= 0 && y >= 0 && x < wwidth && y < wheight && (dx*dx + dy*dy) <= rad2){`
- L101: `buffer[x + y * wwidth] = Math.max(buffer[x + y * wwidth], Integer.MAX_VALUE - id);`

### `mindustry/entities/bullet/LaserBulletType.java` — 5

- L100: `float cwidth = width;`
- L106: `Lines.stroke((cwidth *= lengthFalloff) * b.fout());`
- L109: `Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Lines.getStroke(), cwidth * 2f + width / 2f, b.rotation());`
- L111: `Fill.circle(b.x, b.y, 1f * cwidth * b.fout());`
- L113: `Drawf.tri(b.x, b.y, sideWidth * b.fout() * cwidth, sideLength * compound, b.rotation() + sideAngle * i);`

### `mindustry/graphics/FloorRenderer.java` — 5

- L267: `for(int tilex = Math.max(cx * chunksize - 1, 0); tilex < (cx + 1) * chunksize + 1 && tilex < world.width(); tilex++){`
- L268: `for(int tiley = Math.max(cy * chunksize - 1, 0); tiley < (cy + 1) * chunksize + 1 && tiley < world.height(); tiley++){`
- L364: `int chunksx = Mathf.ceil((float)(world.width()) / chunksize), chunksy = Mathf.ceil((float)(world.height()) / chunksize);`
- L371: `packWidth = world.unitWidth() + packPad *2f;`
- L372: `packHeight = world.unitHeight() + packPad *2f;`

### `mindustry/entities/Fires.java` — 4

- L37: `return tile == null ? null : world.tiles.getFire(tile.array());`
- L41: `return Structs.inBounds(x, y, world.width(), world.height()) ? world.tiles.getFire(world.packArray(x, y)) : null;`
- L45: `world.tiles.setFire(tile.array(), fire);`
- L49: `if(!Structs.inBounds(x, y, world.width(), world.height())){`

### `mindustry/graphics/MenuRenderer.java` — 4

- L177: `for(Tile tile : world.tiles){`
- L191: `for(Tile tile : world.tiles){`
- L195: `for(Tile tile : world.tiles){`
- L202: `for(Tile tile : world.tiles){`

### `mindustry/ui/Minimap.java` — 3

- L35: `scaledX = Mathf.lerp(region.u, region.u2, sx) * world.width() * tilesize,`
- L36: `scaledY = Mathf.lerp(1f - region.v2, 1f - region.v, sy) * world.height() * tilesize;`
- L101: `float max = Math.min(world.width(), world.height()) / 16f / 2f;`

### `mindustry/entities/Puddles.java` — 3

- L30: `return tile == null ? null : world.tiles.getPuddle(tile.array());`
- L109: `world.tiles.setPuddle(tile.array(), null);`
- L113: `world.tiles.setPuddle(puddle.tile.array(), puddle);`

### `mindustry/logic/LExecutor.java` — 3

- L1865: `w = Math.min(world.width(), w);`
- L1866: `h = Math.min(world.height(), h);`
- L1867: `boolean full = x == 0 && y == 0 && w == world.width() && h == world.height();`

### `mindustry/graphics/DebugCollisionRenderer.java` — 3

- L38: `int rx = Mathf.clamp((int)(Core.camera.width / tilesize / 2) + 1, 0, world.width()/2);`
- L39: `int ry = Mathf.clamp((int)(Core.camera.height / tilesize / 2) + 1, 0, world.height()/2);`
- L62: `if(avoid != null && avoid[tile.array()] != 0){`

### `mindustry/ai/BaseBuilderAI.java` — 2

- L82: `if(calculating && calcCount >= world.width() * world.height()){`
- L161: `Tile tile = world.tiles.getc(wx, wy);`

### `mindustry/net/ArcNetProvider.java` — 2

- L521: `buffer.put(byteBuffer.array(), byteBuffer.position(), length);`
- L573: `byteBuffer.put(temp.array(), 0, length);`

### `mindustry/world/Tile.java` — 2

- L79: `return x + y * world.tiles.width;`
- L346: `Geometry.circle(x, y, world.width(), world.height(), radius, cons);`

### `mindustry/world/blocks/environment/TiledFloor.java` — 2

- L49: `return world.tiles.getTmpFloorState(tile.array());`
- L96: `world.tiles.setTmpFloorState(other.array(), TiledState.get(cx, cy, size, changes));`

### `mindustry/world/blocks/environment/TiledWall.java` — 2

- L48: `return world.tiles.getTmpBlockState(tile.array());`
- L92: `world.tiles.setTmpBlockState(other.array(), TiledState.get(cx, cy, size, changes));`

### `mindustry/world/blocks/distribution/Conveyor.java` — 2

- L163: `float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;`
- L175: `Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);`

### `mindustry/input/InputHandler.java` — 2

- L153: `playerPlanTree = new QuadTree<>(new Rect(0f, 0f, world.unitWidth(), world.unitHeight()));`
- L154: `selectPlanTree = new QuadTree<>(new Rect(0f, 0f, world.unitWidth(), world.unitHeight()));`

### `mindustry/entities/comp/UnitComp.java` — 2

- L167: `return !type.flying && world.tiles.in(tileX, tileY) && type.pathCost.getCost(team.id, pathfinder.get(tileX, tileY)) == -1;`
- L676: `float bot = 0f, left = 0f, top = world.unitHeight(), right = world.unitWidth();`

### `mindustry/entities/comp/BulletComp.java` — 2

- L233: `int ww = world.width(), wh = world.height();`
- L235: `while(x >= 0 && y >= 0 && x < ww && y < wh){`

### `mindustry/logic/GlobalVars.java` — 2

- L206: `varMapW.numval = world.width();`
- L207: `varMapH.numval = world.height();`

### `mindustry/core/Control.java` — 2

- L700: `if(Float.isNaN(camera.position.x)) camera.position.x = world.unitWidth()/2f;`
- L701: `if(Float.isNaN(camera.position.y)) camera.position.y = world.unitHeight()/2f;`

### `mindustry/core/Logic.java` — 2

- L150: `world.tiles.eachTile(t -> {`
- L303: `world.tiles = new Tiles(0, 0);`

### `mindustry/graphics/EnvRenderers.java` — 2

- L59: `float x = (rand.random(0f, world.unitWidth()) + (pos % 100)*753) % world.unitWidth();`
- L60: `float y = (rand.random(0f, world.unitHeight()) + (pos % 120)*453) % world.unitHeight();`

### `mindustry/ai/UnitGroup.java` — 1

- L55: `IntQuadTree tree = new IntQuadTree(new Rect(0f, 0f, Vars.world.unitWidth(), Vars.world.unitHeight()),`

### `mindustry/ui/fragments/ChatFragment.java` — 1

- L214: `if(world.tiles.in(x, y)){`

### `mindustry/world/blocks/environment/StaticWall.java` — 1

- L114: `return rx < world.width() - 1 && ry < world.height() - 1`

### `mindustry/game/Teams.java` — 1

- L388: `Geometry.circle(World.toTile(x), World.toTile(y), world.width(), world.height(), Mathf.round(range / tilesize), (tx, ty) -> {`

### `mindustry/input/MobileInput.java` — 1

- L983: `camera.position.clamp(-camera.width/4f, -camera.height/4f, world.unitWidth() + camera.width/4f, world.unitHeight() + camera.height/4f);`

### `mindustry/entities/Damage.java` — 1

- L563: `int x = Mathf.clamp(tx, -100, world.width() + 100), y = Mathf.clamp(ty, -100, world.height() + 100);`

### `mindustry/core/Renderer.java` — 1

- L589: `int w = world.width() * tilesize, h = world.height() * tilesize;`

### `mindustry/graphics/OverlayRenderer.java` — 1

- L69: `var result = Voronoi.generate(pos.toArray(Vec2.class), 0, world.unitWidth(), 0, world.unitHeight());`

## B. Editor / generadores / guardado / red (se ignoran en modo mundo abierto)

- `mindustry/editor/MapEditor.java` — 13
- `mindustry/maps/generators/BasicGenerator.java` — 13
- `mindustry/io/SaveVersion.java` — 12
- `mindustry/ui/fragments/MinimapFragment.java` — 7
- `mindustry/maps/SectorDamage.java` — 5
- `mindustry/maps/filters/GenerateFilter.java` — 5
- `mindustry/editor/EditorRenderer.java` — 4
- `mindustry/maps/generators/PlanetGenerator.java` — 4
- `mindustry/io/MapIO.java` — 4
- `mindustry/editor/MapGenerateDialog.java` — 3
- `mindustry/editor/MapProcessorsDialog.java` — 3
- `mindustry/editor/EditorTool.java` — 3
- `mindustry/mod/DataPatcher.java` — 3
- `mindustry/maps/planet/SerpuloPlanetGenerator.java` — 3
- `mindustry/maps/generators/BaseGenerator.java` — 3
- `mindustry/game/SectorInfo.java` — 2
- `mindustry/maps/Maps.java` — 2
- `mindustry/maps/generators/FileMapGenerator.java` — 2
- `mindustry/editor/MapEditorDialog.java` — 1
- `mindustry/maps/planet/AsteroidGenerator.java` — 1
- `mindustry/maps/filters/SpawnPathFilter.java` — 1