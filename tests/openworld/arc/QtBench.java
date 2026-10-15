import arc.math.geom.*;
import arc.math.geom.QuadTree.*;
import arc.struct.*;
import java.util.*;

public class QtBench{
    static class E implements QuadTreeObject{
        float x, y, s;
        E(float x, float y, float s){ this.x = x; this.y = y; this.s = s; }
        @Override public void hitbox(Rect r){ r.setCentered(x, y, s, s); }
    }

    /** n entidades agrupadas alrededor de (cx, cy) en un radio dado, con tamaños tipo unidad/bala. */
    static Seq<E> cluster(Random r, int n, float cx, float cy, float radio){
        Seq<E> l = new Seq<>(n);
        for(int i = 0; i < n; i++) l.add(new E(cx + (r.nextFloat() * 2 - 1) * radio, cy + (r.nextFloat() * 2 - 1) * radio, 4 + r.nextFloat() * 12));
        return l;
    }

    public static void main(String[] a){
        Random r = new Random(5);
        // Mundo clásico: 500x500 tiles; mundo virtual: rango de un short en tiles (65 536) en unidades de 8
        float clasico = 500 * 8 + 500, virtual = 65536 * 8 + 500;
        float[][] escenas = {{300, 400}, {800, 1500}, {2000, 4000}};   // {entidades, radio de agrupación en unidades}
        for(float[] sc : escenas){
            int n = (int)sc[0]; float radio = sc[1];
            System.out.printf("%n== %d entidades agrupadas en +-%.0f unidades (~%.0f tiles de lado)%n", n, radio, radio * 2 / 8);
            for(int modo = 0; modo < 2; modo++){
                float lado = modo == 0 ? clasico : virtual;
                // centrado en el origen virtual (peor caso: las líneas de corte del árbol pasan por el spawn) y desplazado a un punto "cualquiera"
                for(float[] c : new float[][]{{lado / 2f - 250, lado / 2f - 250}, {lado / 2f + 123457f % (lado / 3), lado / 2f + 98765f % (lado / 3)}}){
                    if(modo == 0 && c[0] > lado) c = new float[]{lado / 2, lado / 2};
                    Seq<E> list = cluster(r, n, c[0], c[1], radio);
                    QuadTree<E> t = new QuadTree<>(new Rect(-250, -250, lado, lado));
                    Rect q = new Rect(); Seq<E> out = new Seq<>();
                    long tf = 0, ti = 0, tq = 0; int rounds = 400, hits = 0;
                    for(int k = 0; k < rounds + 100; k++){
                        long t0 = System.nanoTime(); t.fill(list); long t1 = System.nanoTime();
                        QuadTree<E> t2 = new QuadTree<>(new Rect(-250, -250, lado, lado));
                        long t2s = System.nanoTime(); for(E e : list) t2.insert(e); long t3 = System.nanoTime();
                        long t4 = System.nanoTime();
                        for(int j = 0; j < 200; j++){
                            E e = list.get(j % list.size);
                            out.clear(); t.intersect(q.setCentered(e.x, e.y, 40f, 40f), out); hits += out.size;
                        }
                        long t5 = System.nanoTime();
                        if(k >= 100){ tf += t1 - t0; ti += t3 - t2s; tq += t5 - t4; }
                    }
                    System.out.printf("  raíz %7.0f u, centro %s: fill %6.1f us | insert uno a uno %6.1f us | 200 consultas %6.1f us  (hits %d)%n",
                        lado, modo == 0 ? "clásico  " : "virtual  ", tf / 1000.0 / rounds, ti / 1000.0 / rounds, tq / 1000.0 / rounds, hits / rounds);
                }
            }
        }
    }
}
