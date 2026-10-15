import mindustry.world.*;
import arc.struct.Bits;
import java.util.*;

public class TilesTest{
    static int fails = 0, checks = 0;
    static void ok(boolean c, String m){ checks++; if(!c){ fails++; System.out.println("FALLO: " + m); } }

    public static void main(String[] a){
        // 1) modo denso: comportamiento clásico
        Tiles d = new Tiles(40, 30); d.fill();
        ok(d.get(0,0) != null && d.get(39,29) != null, "denso: extremos");
        ok(d.get(-1,0) == null && d.get(40,0) == null && d.get(0,30) == null, "denso: fuera = null");
        ok(d.in(39,29) && !d.in(40,29), "denso: in");
        ok(d.getc(100,100) == d.get(39,29) && d.getc(-5,-5) == d.get(0,0), "denso: getc clamp");
        ok(d.index(3,2) == 3 + 2*40 && d.geti(d.index(3,2)) == d.get(3,2), "denso: index row-major");
        int n=0; for(Tile t : d) n++; ok(n == 1200, "denso: iterador " + n);
        try{ d.getn(40,0); ok(false,"denso getn debe lanzar"); }catch(IllegalArgumentException e){ ok(true,""); }

        // 2) anillo: ciclo de vida de chunks
        Tiles r = Tiles.ring(4);                 // 16 chunks = 512 casillas
        ok(r.width == 512 && r.height == 512 && r.ring, "anillo: tamaño");
        ok(r.get(0,0) == null && !r.in(0,0), "anillo: vacío al inicio");
        r.claimChunk(0,0);
        ok(r.get(31,31) != null && r.get(32,0) == null, "anillo: solo el chunk reclamado");
        Tile t = r.get(5,7); ok(t.x == 5 && t.y == 7, "anillo: coords virtuales en Tile");
        ok(r.geti(r.index(5,7)) == t, "anillo: geti(index) == get");
        ok(r.get(16*32, 0) == null, "anillo: el alias NO ve las casillas del dueño");
        ok(r.chunkSlotBusy(16,0), "anillo: hueco ocupado por otro");
        try{ r.claimChunk(16,0); ok(false,"claim sobre hueco ajeno debe lanzar"); }catch(IllegalStateException e){ ok(true,""); }
        r.releaseChunk(0,0);
        ok(r.get(5,7) == null && r.loadedChunkCount() == 0, "anillo: tras release no existe");
        r.claimChunk(16,0);
        Tile u = r.get(16*32+3, 4); ok(u != null && u.x == 16*32+3 && u.y == 4, "anillo: el alias ahora es el dueño");
        ok(r.get(3,4) == null, "anillo: el antiguo dueño ya no ve su casilla");

        // 3) costura: casillas contiguas a ambos lados del hueco 15|0
        Tiles s = Tiles.ring(4);
        s.claimChunk(15,3); s.claimChunk(16,3);
        Tile l = s.get(15*32+31, 3*32+5), rr = s.get(16*32, 3*32+5);
        ok(l != null && rr != null && l != rr && rr.x == l.x + 1, "costura: vecinos contiguos");
        ok(s.index(l.x, l.y) != s.index(rr.x, rr.y), "costura: índices distintos");

        // 4) negativas y límites del short
        Tiles g = Tiles.ring(3);
        g.claimChunk(-1,-1);
        ok(g.get(-1,-1) != null && g.get(-32,-32) != null && g.get(0,0) == null, "negativas");
        ok(g.get(40000, 0) == null && g.get(0,-40000) == null && g.get(Integer.MAX_VALUE, 0) == null, "fuera de short = null");
        g.releaseChunk(-1,-1); g.claimChunk(1023, 1023); g.claimChunk(-1024,-1024);   // (-1,-1) y (1023,1023) comparten hueco
        ok(g.get(32767,32767) != null && g.get(-32768,-32768) != null, "extremos del mundo");
        try{ g.claimChunk(1024,0); ok(false,"fuera de rango debe lanzar"); }catch(IllegalArgumentException e){ ok(true,""); }

        // 5) biyección en una ventana lejana de NxN chunks
        Tiles b = Tiles.ring(3); int W = b.width; boolean[] seen = new boolean[W*W]; boolean uniq = true;
        for(int cy=-300; cy<-300+8; cy++) for(int cx=700; cx<700+8; cx++) b.claimChunk(cx,cy);
        for(int y=-300*32; y<(-300+8)*32; y++) for(int x=700*32; x<(700+8)*32; x++){
            int i = b.index(x,y); if(i<0 || i>=W*W || seen[i]) uniq=false; else seen[i]=true;
            Tile q = b.get(x,y); if(q==null || q.x!=x || q.y!=y) uniq=false;
        }
        ok(uniq, "biyección en ventana lejana");
        int cnt=0; for(Tile q : b) cnt++; ok(cnt == 64*1024, "iterador anillo: " + cnt);
        int[] each={0}; b.each((x,y)->each[0]++); ok(each[0]==64*1024, "each anillo");
        int[] ch={0}; b.eachLoadedChunk((cx,cy)->{ if(cx>=700&&cx<708&&cy>=-300&&cy<-292) ch[0]++; }); ok(ch[0]==64, "eachLoadedChunk");

        // 6) estrés: ventana móvil con carga/descarga contra un modelo de referencia
        Random rnd = new Random(7); final Tiles m = Tiles.ring(4); final Map<Long,Tile> model = new HashMap<>();
        int px = 0, py = 0; long ops=0; boolean diverge=false;
        for(int step=0; step<6000 && !diverge; step++){
            px += rnd.nextInt(3)-1; py += rnd.nextInt(3)-1;
            if(rnd.nextInt(300)==0){ px += rnd.nextInt(2001)-1000; py += rnd.nextInt(2001)-1000; px=Math.max(-900,Math.min(900,px)); py=Math.max(-900,Math.min(900,py)); }
            int R = 4;
            for(int cy=py-R; cy<=py+R; cy++) for(int cx=px-R; cx<=px+R; cx++){
                if(!m.chunkLoaded(cx,cy)){
                    if(m.chunkSlotBusy(cx,cy)){
                        final int fx=cx, fy=cy;
                        final List<int[]> out = new ArrayList<>();
                        m.eachLoadedChunk((ox,oy)->{ if(((ox ^ fx) & 15)==0 && ((oy ^ fy) & 15)==0 && (ox!=fx||oy!=fy)) out.add(new int[]{ox,oy}); });
                        for(int[] o : out){ m.releaseChunk(o[0],o[1]); for(int i=0;i<32;i++) for(int j=0;j<32;j++) model.remove(key(o[0]*32+i,o[1]*32+j)); }
                    }
                    m.claimChunk(cx,cy);
                    for(int i=0;i<32;i++) for(int j=0;j<32;j++) model.put(key(cx*32+i,cy*32+j), m.get(cx*32+i,cy*32+j));
                    ops++;
                }
            }
            for(int k=0;k<4;k++){
                int x=(px+rnd.nextInt(2*R*32+1)-R*32), y=(py+rnd.nextInt(2*R*32+1)-R*32);
                Tile ref = model.get(key(x,y)), got = m.get(x,y);
                if(ref != got){ ok(false,"estrés: divergencia en "+x+","+y); diverge=true; break; }
            }
        }
        ok(m.loadedChunkCount()*1024 == model.size(), "estrés: conteo coincide con el modelo " + m.loadedChunkCount()*1024 + " vs " + model.size());

        // 7) publishChunk: bloque construido aparte y publicado por copia de referencias
        Tiles pr = Tiles.ring(4);
        Tile[] blk = new Tile[1024];
        for(int ly=0; ly<32; ly++) for(int lx=0; lx<32; lx++) blk[ly*32+lx] = new Tile(-3*32+lx, 17*32+ly);
        ok(pr.get(-3*32, 17*32) == null, "publish: invisible antes de publicar");
        ok(pr.publishChunk(-3,17,blk), "publish: devuelve true");
        ok(pr.get(-3*32+5, 17*32+9) == blk[9*32+5], "publish: misma referencia que el bloque");
        ok(pr.get(-3*32+31, 17*32+31) == blk[1023] && pr.get(-3*32-1, 17*32) == null, "publish: límites del chunk");
        ok(pr.geti(pr.index(-3*32+5,17*32+9)) == blk[9*32+5], "publish: geti(index)");
        ok(!pr.publishChunk(-3,17,blk) && pr.loadedChunkCount()==1, "publish: segunda vez no sobrescribe");
        int pc=0; for(Tile q : pr) pc++; ok(pc==1024, "publish: iterador ve 1024");
        Tile[] blk2 = new Tile[1024];
        for(int ly=0; ly<32; ly++) for(int lx=0; lx<32; lx++) blk2[ly*32+lx] = new Tile((-3+16)*32+lx, 17*32+ly);
        try{ pr.publishChunk(-3+16,17,blk2); ok(false,"publish: hueco ajeno debe lanzar"); }catch(IllegalStateException e){ ok(true,""); }
        pr.releaseChunk(-3,17);
        ok(pr.publishChunk(-3+16,17,blk2) && pr.get(-3*32+5,17*32+9)==null && pr.get((-3+16)*32+5,17*32+9)==blk2[9*32+5], "publish: relevo de dueño tras release");
        Tile[] bad = new Tile[1024];
        for(int ly=0; ly<32; ly++) for(int lx=0; lx<32; lx++) bad[ly*32+lx] = new Tile(lx, ly);
        try{ Tiles.ring(4).publishChunk(5,5,bad); ok(false,"publish: coords erróneas deben lanzar"); }catch(IllegalArgumentException e){ ok(true,""); }
        try{ Tiles.ring(4).publishChunk(5,5,new Tile[10]); ok(false,"publish: tamaño erróneo debe lanzar"); }catch(IllegalArgumentException e){ ok(true,""); }
        try{ new Tiles(8,8).publishChunk(0,0,blk); ok(false,"publish: modo denso debe lanzar"); }catch(UnsupportedOperationException e){ ok(true,""); }
        // publish a través de la costura del anillo (chunks 15 y 16 -> huecos 15 y 0) y coherencia con claimChunk
        Tiles sm = Tiles.ring(4);
        for(int ccx=15; ccx<=16; ccx++){ Tile[] bb = new Tile[1024]; for(int ly=0; ly<32; ly++) for(int lx=0; lx<32; lx++) bb[ly*32+lx] = new Tile(ccx*32+lx, 4*32+ly); sm.publishChunk(ccx,4,bb); }
        ok(sm.get(15*32+31,4*32+3).x+1 == sm.get(16*32,4*32+3).x, "publish: costura");
        ok(sm.index(15*32+31,4*32+3)+1 != sm.index(16*32,4*32+3), "publish: la costura NO es contigua en memoria (esperado)");


        // 8) chunkHasBuildings (descarga segura): un Building en cualquier casilla del chunk, también en su borde y a través de la costura
        Tiles hb = Tiles.ring(4);
        hb.claimChunk(15,2); hb.claimChunk(16,2); hb.claimChunk(-1,-1);
        ok(!hb.chunkHasBuildings(15,2) && !hb.chunkHasBuildings(16,2) && !hb.chunkHasBuildings(-1,-1), "hb: sin edificios");
        hb.get(15*32+31, 2*32+31).build = new mindustry.gen.Building();      // última casilla del chunk 15
        ok(hb.chunkHasBuildings(15,2) && !hb.chunkHasBuildings(16,2), "hb: borde de un chunk; el vecino de la costura sigue limpio");
        hb.get(16*32, 2*32).build = new mindustry.gen.Building();            // primera casilla del chunk 16 (hueco 0)
        ok(hb.chunkHasBuildings(16,2), "hb: primera casilla tras la costura");
        hb.get(-32,-32).build = new mindustry.gen.Building();                // esquina de un chunk negativo
        ok(hb.chunkHasBuildings(-1,-1), "hb: esquina negativa");
        hb.get(15*32+31, 2*32+31).build = null;
        ok(!hb.chunkHasBuildings(15,2), "hb: se limpia al quitar el edificio");
        ok(!hb.chunkHasBuildings(3,3), "hb: chunk no cargado = false");
        try{ new Tiles(8,8).chunkHasBuildings(0,0); ok(false,"hb: modo denso debe lanzar"); }catch(UnsupportedOperationException e){ ok(true,""); }


        // 9) capacity(): tamaño de los arrays por casilla
        ok(new Tiles(40,30).capacity() == 1200 && Tiles.ring(4).capacity() == 512*512, "capacity: denso y anillo");
        Tiles cp = Tiles.ring(4); cp.claimChunk(-7,9);
        boolean idxOk = true; for(Tile q : cp) if(cp.index(q.x,q.y) < 0 || cp.index(q.x,q.y) >= cp.capacity()) idxOk = false;
        ok(idxOk, "capacity: todo índice vivo cabe en capacity()");


        // 10) neighborIndex (regla de oro: vecinos por coordenadas virtuales)
        int[][] D4 = {{1,0},{0,1},{-1,0},{0,-1}};
        Tiles dn = new Tiles(40,30); dn.fill(); boolean par = true;
        for(int i = 0; i < 1200; i++) for(int[] p : D4){
            int x = i % 40 + p[0], y = i / 40 + p[1]; int esp = (x<0||y<0||x>=40||y>=30) ? -1 : x + y*40;
            if(dn.neighborIndex(i,p[0],p[1]) != esp) par = false; }
        ok(par, "neighborIndex: modo denso = aritmética de siempre");
        // inundación (4 vecinos) por neighborIndex vs. referencia en coordenadas virtuales
        Tiles nb = Tiles.ring(4);                        // 16 chunks de lado
        Set<Long> vivo = new HashSet<>();
        int[][] islaA = {{12,5},{13,5},{14,5},{15,5},{16,5},{17,5},{15,6},{15,4}};   // cruza la costura 15|16 (huecos 15 y 0)
        for(int[] c : islaA){ nb.claimChunk(c[0],c[1]); }
        // isla B lejana: mismos huecos que la costura del otro lado -> es adyacente a A EN EL BÚFER pero lejos en el mundo
        nb.claimChunk(34, 21); nb.claimChunk(35, 21);                              // huecos (2,5) y (3,5): justo a la derecha de A en el búfer
        for(Tile q : nb) vivo.add(key(q.x,q.y));
        Tile origen = nb.get(15*32+31, 5*32+3);
        boolean[] vis = new boolean[nb.capacity()]; ArrayDeque<Integer> cola = new ArrayDeque<>(); int alcanzadas = 0;
        int i0 = nb.index(origen.x, origen.y); vis[i0] = true; cola.add(i0);
        while(!cola.isEmpty()){ int c = cola.poll(); alcanzadas++;
            for(int[] p : D4){ int nv = nb.neighborIndex(c,p[0],p[1]); if(nv >= 0 && !vis[nv]){ vis[nv] = true; cola.add(nv); } } }
        // referencia: inundación sobre el conjunto de coordenadas virtuales de la isla A (solo chunks de A)
        Set<Long> conjA = new HashSet<>(); for(int[] c : islaA) for(int ly=0;ly<32;ly++) for(int lx=0;lx<32;lx++) conjA.add(key(c[0]*32+lx, c[1]*32+ly));
        Set<Long> visR = new HashSet<>(); ArrayDeque<long[]> qr = new ArrayDeque<>(); qr.add(new long[]{origen.x, origen.y}); visR.add(key(origen.x, origen.y));
        while(!qr.isEmpty()){ long[] c = qr.poll(); for(int[] p : D4){ int x = (int)c[0]+p[0], y = (int)c[1]+p[1]; long k = key(x,y); if(conjA.contains(k) && visR.add(k)) qr.add(new long[]{x,y}); } }
        ok(alcanzadas == visR.size() && alcanzadas == islaA.length*1024, "neighborIndex: la isla A (que cruza la costura) se recorre entera: " + alcanzadas + " de " + islaA.length*1024);
        // y nunca se alcanza una casilla de la isla B (adyacente en el búfer, lejana en el mundo)
        boolean tocaB = false; for(int i = 0; i < nb.capacity(); i++) if(vis[i]){ Tile q = nb.geti(i); if(q.y >= 21*32) tocaB = true; }
        ok(!tocaB, "neighborIndex: no se conecta con la isla B alias");
        // vecino de un hueco vacío / chunk descargado = -1
        nb.releaseChunk(16,5);
        ok(nb.neighborIndex(nb.index(15*32+31, 5*32+3), 1, 0) == -1, "neighborIndex: chunk descargado = -1");
        ok(nb.neighborIndex(nb.index(15*32+31, 5*32+3), -1, 0) >= 0, "neighborIndex: el vecino cargado sigue");


        // 11) Equivalencia del flowfield: inundación por neighborIndex (anillo, cruzando la costura, con chunks ausentes y paredes)
        //     contra una BFS en un mapa DENSO equivalente. Mismas distancias en cada casilla alcanzable.
        {
            Random rr2 = new Random(21); int fallos = 0, casos = 0, casillasCmp = 0;
            for(int caso = 0; caso < 25; caso++){
                int cx0 = -6 + rr2.nextInt(3) * 8 - 1, cy0 = -7 + rr2.nextInt(3) * 8;   // ventana de 12x12 chunks, a veces a caballo de la costura
                final int NC = 12, NT = NC * 32; Tiles ring = Tiles.ring(4);
                boolean[][] cargado = new boolean[NC][NC];
                for(int i = 0; i < NC; i++) for(int j = 0; j < NC; j++){ cargado[i][j] = rr2.nextInt(10) != 0; if(cargado[i][j]) ring.claimChunk(cx0 + i, cy0 + j); }
                boolean[][] muro = new boolean[NT][NT]; double dens = 0.15 + rr2.nextDouble() * 0.25;
                for(int x = 0; x < NT; x++) for(int y = 0; y < NT; y++) muro[x][y] = !cargado[x/32][y/32] || rr2.nextDouble() < dens;   // ausente = muro
                // origen: una casilla libre cualquiera
                int ox, oy; int guard = 0; do{ ox = rr2.nextInt(NT); oy = rr2.nextInt(NT); }while(muro[ox][oy] && ++guard < 10000);
                if(muro[ox][oy]) continue; casos++;
                // BFS denso (referencia)
                int[][] dd = new int[NT][NT]; for(int[] f : dd) Arrays.fill(f, -1);
                ArrayDeque<int[]> q = new ArrayDeque<>(); dd[ox][oy] = 0; q.add(new int[]{ox, oy});
                while(!q.isEmpty()){ int[] c = q.poll(); for(int[] p : D4){ int x = c[0]+p[0], y = c[1]+p[1];
                    if(x>=0&&y>=0&&x<NT&&y<NT && !muro[x][y] && dd[x][y] < 0){ dd[x][y] = dd[c[0]][c[1]] + 1; q.add(new int[]{x, y}); } } }
                // BFS en el anillo, vecinos por neighborIndex, paredes por (x, y) virtual
                int[] dr = new int[ring.capacity()]; Arrays.fill(dr, -1);
                int src = ring.index((cx0*32)+ox, (cy0*32)+oy); dr[src] = 0; ArrayDeque<Integer> q2 = new ArrayDeque<>(); q2.add(src);
                while(!q2.isEmpty()){ int c = q2.poll(); for(int[] p : D4){ int nn = ring.neighborIndex(c, p[0], p[1]);
                    if(nn < 0 || dr[nn] >= 0) continue; Tile tn = ring.geti(nn); if(muro[tn.x - cx0*32][tn.y - cy0*32]) continue; dr[nn] = dr[c] + 1; q2.add(nn); } }
                for(int x = 0; x < NT; x++) for(int y = 0; y < NT; y++){
                    if(!cargado[x/32][y/32]) continue; int rv = dr[ring.index(cx0*32+x, cy0*32+y)]; casillasCmp++;
                    if(rv != dd[x][y]) fallos++; }
            }
            System.out.println("flowfield: " + casos + " mapas, " + casillasCmp + " casillas comparadas, " + fallos + " distintas");
            ok(fallos == 0 && casos >= 15, "flowfield en anillo = BFS denso: " + casos + " mapas, " + casillasCmp + " casillas comparadas, " + fallos + " distintas");
        }


        // 12) setBitsRow: solo casillas con chunk cargado; contra referencia por coordenadas virtuales
        {
            Random rb = new Random(33); int malos = 0, filas = 0;
            for(int caso = 0; caso < 60; caso++){
                Tiles tb = Tiles.ring(4); Set<Long> carg = new HashSet<>();
                int cx0 = rb.nextInt(40) - 20, cy0 = rb.nextInt(40) - 20;
                // dos islas: la del jugador y otra "alias" (mismos huecos módulo 16 en una fila libre del búfer)
                for(int i = 0; i < 6; i++) for(int j = 0; j < 6; j++) if(rb.nextInt(6) != 0) tb.claimChunk(cx0 + i, cy0 + j);
                for(Tile q : tb) carg.add(key(q.x, q.y));
                Bits arr = new Bits(); Set<Integer> esperado = new HashSet<>();
                for(int k = 0; k < 40; k++){
                    int y = (cy0 * 32) + rb.nextInt(6 * 32 + 20) - 10, x1 = cx0 * 32 + rb.nextInt(6 * 32 + 60) - 30, x2 = x1 + rb.nextInt(200) - 60;
                    if(rb.nextInt(15) == 0){ x2 = rb.nextInt(2) == 0 ? 50000 : -50000; }
                    if(rb.nextInt(15) == 0){ y = rb.nextInt(2) == 0 ? 40000 : -40000; }
                    tb.setBitsRow(arr, x1, x2, y); filas++;
                    int lo = Math.min(x1, x2), hi = Math.max(x1, x2);
                    if(y >= -32768 && y <= 32767) for(int x = Math.max(lo, -32768); x <= Math.min(hi, 32767); x++) if(carg.contains(key(x, y))) esperado.add(tb.index(x, y));
                }
                for(int i = 0; i < tb.capacity(); i++) if(arr.get(i) != esperado.contains(i)) malos++;
            }
            ok(malos == 0, "setBitsRow: " + filas + " filas, " + malos + " bits distintos de la referencia");
        }
        // 13) bits por chunk: ida y vuelta, aislamiento entre vecinos y a través de la costura
        {
            Random rc = new Random(44); int malos = 0;
            Tiles tb = Tiles.ring(4); tb.claimChunk(15, 3); tb.claimChunk(16, 3); tb.claimChunk(-1, -1);
            Bits arr = new Bits();
            for(int[] c : new int[][]{{15,3},{16,3},{-1,-1}}) for(int ly = 0; ly < 32; ly++) for(int lx = 0; lx < 32; lx++) if(rc.nextBoolean()) arr.set(tb.index(c[0]*32+lx, c[1]*32+ly));
            long[] a15 = new long[16], a16 = new long[16], an = new long[16], nada = new long[16];
            tb.readChunkBits(arr, 15, 3, a15); tb.readChunkBits(arr, 16, 3, a16); tb.readChunkBits(arr, -1, -1, an);
            Bits copia = new Bits(); tb.writeChunkBits(copia, 15, 3, a15); tb.writeChunkBits(copia, 16, 3, a16); tb.writeChunkBits(copia, -1, -1, an);
            for(int i = 0; i < tb.capacity(); i++) if(arr.get(i) != copia.get(i)) malos++;
            ok(malos == 0, "bits por chunk: ida y vuelta (" + malos + " distintos)");
            tb.clearChunkBits(arr, 15, 3);
            long[] despues15 = new long[16], despues16 = new long[16];
            tb.readChunkBits(arr, 15, 3, despues15); tb.readChunkBits(arr, 16, 3, despues16);
            ok(Arrays.equals(despues15, nada) && Arrays.equals(despues16, a16), "bits por chunk: limpiar uno no toca a su vecino de la costura");
            tb.readChunkBits(arr, 7, 7, nada); boolean ceros = true; for(long v : nada) if(v != 0) ceros = false;
            ok(ceros, "bits por chunk: chunk no cargado lee ceros");
            tb.writeChunkBits(arr, 7, 7, a16); tb.clearChunkBits(arr, 7, 7);   // no cargado: no debe tocar nada
            long[] otra = new long[16]; tb.readChunkBits(arr, 16, 3, otra);
            ok(Arrays.equals(otra, a16), "bits por chunk: escribir/limpiar un chunk no cargado no corrompe al alias");
            // el alias: chunk (15+16, 3) comparte hueco con (15, 3), que ya no tiene bits; limpiar el alias no cargado no debe afectar
            tb.writeChunkBits(arr, 15, 3, a15);
            tb.clearChunkBits(arr, 15 + 16, 3);
            long[] de15 = new long[16]; tb.readChunkBits(arr, 15, 3, de15);
            ok(Arrays.equals(de15, a15), "bits por chunk: limpiar el alias descargado no borra al dueño del hueco");
        }

        System.out.println("chunks cargados en el estrés: " + ops);
        System.out.println(checks + " comprobaciones, " + fails + " fallos");
        System.exit(fails==0?0:1);
    }
    static long key(int x,int y){ return ((long)x<<32) ^ (y & 0xFFFFFFFFL); }
}
