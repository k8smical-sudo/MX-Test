import javax.tools.*; import com.sun.source.util.*; import java.io.*; import java.util.*;
public class ParseCheck{ public static void main(String[] a) throws Exception{
  JavaCompiler c = ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> d = new DiagnosticCollector<>();
  StandardJavaFileManager fm = c.getStandardFileManager(d, null, null);
  JavacTask t = (JavacTask) c.getTask(null, fm, d, List.of("-proc:none"), null, fm.getJavaFileObjectsFromStrings(Arrays.asList(a)));
  int n=0; for(var cu : t.parse()) n++;
  long err = d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count();
  d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).limit(8).forEach(x->System.out.println("  "+x.getSource().getName()+":"+x.getLineNumber()+" "+x.getMessage(null)));
  System.out.println(n+" archivos parseados, "+err+" errores de sintaxis"); System.exit(err==0?0:1);} }
