import mindustry.world.*;
public class Bench{
    public static void main(String[] a){
        Tiles d = new Tiles(512,512); d.fill();
        Tiles r = Tiles.ring(4); for(int cy=-8;cy<8;cy++) for(int cx=-8;cx<8;cx++) r.claimChunk(cx,cy);
        long sink=0; int N=40_000_000;
        for(int w=0;w<3;w++){
            long t0=System.nanoTime(); for(int i=0;i<N;i++){ Tile t=d.get((i*7)&511,(i*13)&511); sink+=t.x; } long td=System.nanoTime()-t0;
            t0=System.nanoTime(); for(int i=0;i<N;i++){ Tile t=r.get(((i*7)&511)-256,((i*13)&511)-256); sink+=t.x; } long tr=System.nanoTime()-t0;
            System.out.printf("get(): denso %.2f ns  anillo %.2f ns%n", td/(double)N, tr/(double)N);
        }
        Tiles c = Tiles.ring(4); int K=2000;
        for(int w=0;w<3;w++){
            long t0=System.nanoTime();
            for(int i=0;i<K;i++){ int cx=i&7, cy=(i>>3)&7; c.releaseChunk(cx+8,cy); c.releaseChunk(cx,cy); c.claimChunk(cx,cy); }
            long t=System.nanoTime()-t0; System.out.printf("release+claim de un chunk (1024 casillas, Tile stub): %.1f us%n", t/1000.0/K);
        }
        // publishChunk: el hilo principal solo copia referencias (el bloque se construyó antes, fuera de él)
        Tiles pu = Tiles.ring(4); Tile[][] bl = new Tile[16][]; 
        for(int i=0;i<16;i++){ bl[i]=new Tile[1024]; for(int ly=0;ly<32;ly++) for(int lx=0;lx<32;lx++) bl[i][ly*32+lx]=new Tile(i*32+lx, ly); }
        for(int w=0;w<3;w++){
            long acc=0; int KK=20000;
            for(int i=0;i<KK;i++){ int cx=i&15; pu.releaseChunk(cx,0); long t0=System.nanoTime(); pu.publishChunk(cx,0,bl[cx]); acc+=System.nanoTime()-t0; }
            System.out.printf("publishChunk en el hilo principal: %.2f us por chunk%n", acc/1000.0/KK);
        }
        System.out.println(sink!=42);
    }
}
