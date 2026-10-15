package mindustry.ai;

import arc.struct.*;
import java.util.*;

public class OreTest{
    static int checks = 0, fails = 0;
    static void ok(boolean c, String m){ checks++; if(!c){ fails++; System.out.println("FALLO: " + m); } }

    public static void main(String[] a){
        // 1) claves de cuadrante con coordenadas negativas
        ok(RingOreIndex.key(0,0) == RingOreIndex.key(15,15), "key: 0..15 es un cuadrante");
        ok(RingOreIndex.key(15,15) != RingOreIndex.key(16,15) && RingOreIndex.key(15,15) != RingOreIndex.key(15,16), "key: 16 abre otro");
        ok(RingOreIndex.key(-1,-1) == RingOreIndex.key(-16,-16) && RingOreIndex.key(-16,-16) != RingOreIndex.key(-17,-16), "key: negativos [-16,-1]");
        ok(RingOreIndex.key(-1,-1) != RingOreIndex.key(0,0), "key: -1 y 0 no comparten cuadrante");
        ok(RingOreIndex.key(5,-3) != RingOreIndex.key(-3,5), "key: x e y no se confunden");
        Set<Long> ks = new HashSet<>(); for(int x = -64; x < 64; x += 16) for(int y = -64; y < 64; y += 16) ks.add(RingOreIndex.key(x,y));
        ok(ks.size() == 64, "key: 8x8 cuadrantes distintos");

        // 2) seq / límites de id
        RingOreIndex idx = new RingOreIndex(5);
        ok(idx.seq(9, 0, 0, true) == null && idx.seq(-1, 0, 0, true) == null, "seq: id fuera de rango");
        ok(idx.seq(2, 0, 0, false) == null, "seq: sin crear no existe");
        idx.seq(2, -40, 70, true).add(123);
        ok(idx.seq(2, -40, 70, false).size == 1 && idx.seq(2, -33, 79, false) == idx.seq(2, -40, 70, false), "seq: mismo cuadrante");
        ok(idx.map(2) != null && idx.map(1) == null && idx.map(7) == null, "map");

        // 3) un cuadrante vive entero en un chunk: dropChunk quita lo suyo y nada más
        Random r = new Random(9);
        for(int caso = 0; caso < 30; caso++){
            RingOreIndex ix = new RingOreIndex(4);
            Map<Integer, Set<Long>> modelo = new HashMap<>();   // item -> posiciones (x, y) añadidas
            for(int id = 0; id < 4; id++) modelo.put(id, new HashSet<>());
            int n = 3000 + r.nextInt(3000);
            for(int i = 0; i < n; i++){
                int id = r.nextInt(4), x = r.nextInt(400) - 200, y = r.nextInt(400) - 200;
                if(modelo.get(id).add(((long)x << 32) | (y & 0xFFFFFFFFL))) ix.seq(id, x, y, true).add(i);
            }
            // descargar chunks al azar y comparar el recuento
            for(int k = 0; k < 40; k++){
                int cx = r.nextInt(14) - 7, cy = r.nextInt(14) - 7, id = r.nextInt(4);
                int esperado = 0; Iterator<Long> it = modelo.get(id).iterator();
                while(it.hasNext()){ long p = it.next(); int x = (int)(p >> 32), y = (int)p; if((x >> 5) == cx && (y >> 5) == cy){ esperado++; it.remove(); } }
                int real = ix.dropChunk(id, cx, cy);
                ok(real == esperado, "dropChunk (" + cx + "," + cy + ") item " + id + ": " + real + " vs " + esperado);
            }
            for(int id = 0; id < 4; id++){
                int total = 0; var m = ix.map(id); if(m != null) for(IntSeq s : m.values()) total += s.size;
                ok(total == modelo.get(id).size(), "recuento final item " + id + ": " + total + " vs " + modelo.get(id).size());
            }
        }
        // 4) clear
        idx.clear(); ok(idx.map(2) == null, "clear");
        System.out.println(checks + " comprobaciones, " + fails + " fallos");
        System.exit(fails == 0 ? 0 : 1);
    }
}
