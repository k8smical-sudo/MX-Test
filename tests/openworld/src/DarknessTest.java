import mindustry.world.*;
import java.util.*;

public class DarknessTest{
    static final int R = 4;
    /** Reimplementación fiel de World.addDarkness (vanilla) sobre un mapa N x N ACOTADO: devuelve data por casilla (0 si no oscurecible). */
    static byte[] vanilla(boolean[] dk, int N){
        byte[] dark = new byte[N*N], wb = new byte[N*N], data = new byte[N*N];
        for(int i = 0; i < N*N; i++) if(dk[i]) dark[i] = R;
        int[][] d4 = {{1,0},{0,1},{-1,0},{0,-1}};
        for(int it = 0; it < R; it++){
            for(int y = 0; y < N; y++) for(int x = 0; x < N; x++){
                int idx = y*N + x; boolean min = false;
                for(int[] p : d4){ int nx = x+p[0], ny = y+p[1]; if(nx>=0&&nx<N&&ny>=0&&ny<N && dark[ny*N+nx] < dark[idx]){ min = true; break; } }
                wb[idx] = (byte)Math.max(0, dark[idx] - (min?1:0));
            }
            System.arraycopy(wb, 0, dark, 0, N*N);
        }
        for(int y = 0; y < N; y++) for(int x = 0; x < N; x++){
            int idx = y*N + x;
            if(dk[idx]) data[idx] = dark[idx];
            if(dark[idx] == R){
                boolean full = true;
                for(int[] p : d4){ int px = x+p[0], py = y+p[1]; int nidx = py*N + px;
                    if(px>=0&&px<N&&py>=0&&py<N && !(dk[idx] && dark[nidx] == 4)){ full = false; break; } }
                if(full) data[idx] = R + 1;
            }
        }
        return data;
    }

    static boolean[] noise(Random r, int N, double p){ boolean[] a = new boolean[N*N]; for(int i = 0; i < a.length; i++) a[i] = r.nextDouble() < p; return a; }
    static boolean[] blobs(Random r, int N){
        double[] f = new double[N*N]; for(int k = 0; k < 40; k++){ int cx = r.nextInt(N), cy = r.nextInt(N); double rad = 3 + r.nextInt(18);
            for(int y = 0; y < N; y++) for(int x = 0; x < N; x++){ double d = Math.hypot(x-cx, y-cy); if(d < rad) f[y*N+x] += 1 - d/rad; } }
        boolean[] a = new boolean[N*N]; double th = 0.15 + r.nextDouble()*0.6; for(int i = 0; i < a.length; i++) a[i] = f[i] > th; return a;
    }

    /** Ejecuta ChunkDarkness para el chunk de origen (ox, oy), dejando sin contexto los 'perdidos' anillos exteriores del área acolchada. */
    static byte[] chunk(boolean[] dk, int N, int ox, int oy, int perdidos){
        int P = ChunkDarkness.padded, M = ChunkDarkness.margin;
        boolean[] pad = new boolean[P*P];
        for(int ly = -M; ly < 32 + M; ly++) for(int lx = -M; lx < 32 + M; lx++){
            int pi = ChunkDarkness.paddedIndex(lx, ly);
            int px = lx + M, py = ly + M;   // posición en el área acolchada
            boolean sinContexto = px < perdidos || py < perdidos || px >= P - perdidos || py >= P - perdidos;
            pad[pi] = !sinContexto && dk[(oy+ly)*N + (ox+lx)];
        }
        byte[] out = new byte[1024];
        ultimoMacizo = ChunkDarkness.compute(pad, out, new byte[P*P], new byte[P*P]);
        return out;
    }
    static boolean ultimoMacizo;

    /** Mapa con una gran masa de roca (garantiza chunks macizos) y huecos aleatorios fuera de ella. */
    static boolean[] masa(Random r, int N, int agujeros){
        boolean[] a = new boolean[N*N]; Arrays.fill(a, true);
        for(int k = 0; k < agujeros; k++){ int cx = r.nextInt(N), cy = r.nextInt(N), rad = 1 + r.nextInt(12);
            for(int y = Math.max(0,cy-rad); y < Math.min(N,cy+rad); y++) for(int x = Math.max(0,cx-rad); x < Math.min(N,cx+rad); x++) a[y*N+x] = false; }
        return a;
    }

    static int diffs(boolean[] dk, int N, int perdidos){
        byte[] ref = vanilla(dk, N); int bad = 0;
        for(int oy : new int[]{32, 64, 96}) for(int ox : new int[]{32, 64, 96}){
            byte[] out = chunk(dk, N, ox, oy, perdidos);
            for(int ly = 0; ly < 32; ly++) for(int lx = 0; lx < 32; lx++) if(out[ly*32+lx] != ref[(oy+ly)*N + ox+lx]) bad++;
        }
        return bad;
    }

    public static void main(String[] a){
        int N = 160; Random r = new Random(11); int chk = 0, fails = 0;
        System.out.println("margen = " + ChunkDarkness.margin + ", acolchado = " + ChunkDarkness.padded);
        for(int t = 0; t < 120; t++){
            boolean[] dk = t % 3 == 0 ? noise(r, N, new double[]{0.3,0.5,0.7,0.85,0.95}[r.nextInt(5)]) : blobs(r, N);
            int b = diffs(dk, N, 0); chk++; if(b != 0){ fails++; System.out.println("FALLO: " + b + " casillas distintas (margen completo), caso " + t); }
        }
        System.out.println("margen completo: " + chk + " mapas x 9 chunks, " + fails + " mapas con diferencias");
        // Comprobación empírica de la necesidad del margen: sin el último anillo de contexto DEBE fallar en algún mapa
        int mapasConError = 0, casillas = 0;
        for(int t = 0; t < 120; t++){
            boolean[] dk = t % 3 == 0 ? noise(r, N, new double[]{0.3,0.5,0.7,0.85,0.95}[r.nextInt(5)]) : blobs(r, N);
            int b = diffs(dk, N, 1); if(b != 0){ mapasConError++; casillas += b; }
        }
        System.out.println("con 1 anillo de contexto menos (margen " + (ChunkDarkness.margin-1) + "): " + mapasConError + " de 120 mapas con diferencias (" + casillas + " casillas)");
        // --- macizo: el booleano debe coincidir con "todas las casillas del chunk valen darkRadius+1 en vanilla", y debe haber positivos Y negativos
        int pos = 0, neg = 0, malos = 0;
        for(int t = 0; t < 150; t++){
            boolean[] dk = t % 2 == 0 ? masa(r, N, r.nextInt(14)) : blobs(r, N);
            byte[] ref = vanilla(dk, N);
            for(int oy : new int[]{32, 64, 96}) for(int ox : new int[]{32, 64, 96}){
                chunk(dk, N, ox, oy, 0);
                boolean esperado = true;
                for(int ly = 0; ly < 32 && esperado; ly++) for(int lx = 0; lx < 32; lx++) if(ref[(oy+ly)*N + ox+lx] != R + 1){ esperado = false; break; }
                if(esperado) pos++; else neg++;
                if(esperado != ultimoMacizo) malos++;
            }
        }
        System.out.println("macizo: " + pos + " positivos, " + neg + " negativos, " + malos + " discrepancias con vanilla");
        boolean todoClaro = pos > 0 && neg > 0 && malos == 0;
        System.exit(fails == 0 && mapasConError > 0 && todoClaro ? 0 : 1);
    }
}
