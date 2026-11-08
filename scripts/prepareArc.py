#!/usr/bin/env python3
"""Prepara el build de MX-Test para usar Arc upstream (Anuken/Arc) como build compuesto.

Se ejecuta después de scripts/applyPatches.sh, desde la raíz del repo. Hace tres cosas:
  1. work/build.gradle: las dependencias de Arc salen de com.github.Anuken (no de TinyLake/JitPack).
  2. work/settings.gradle: incluye Arc como includeBuild("../Arc"), así las dependencias se
     sustituyen por los proyectos de Arc en el mismo build.
  0. Arc/build.gradle: LWJGL 3.4.2 publicado (antes lo fijaba el parche de Arc 0009).
  3. work/tools/build.gradle: el buildscript de tools (que necesita arc.packer) usa los jars
     que se compilan en Arc, porque el buildscript no admite includeBuild.

Cada edición exige una coincidencia exacta: si el código de work/ cambia, el script falla
con un mensaje claro en vez de modificar el archivo a ciegas.
"""
import sys
from pathlib import Path

ROOT = Path(".")
WORK = ROOT / "work"


def edit(path: Path, pairs):
    if not path.exists():
        sys.exit(f"[prepareArc] no existe {path}")
    s = path.read_text(encoding="utf-8")
    for old, new in pairs:
        n = s.count(old)
        if n != 1:
            sys.exit(f"[prepareArc] {path}: se esperaba 1 coincidencia, hay {n}: {old[:70]!r}")
        s = s.replace(old, new)
    path.write_text(s, encoding="utf-8")
    print(f"[prepareArc] editado: {path}")


# Equivalente al antiguo parche de Arc 0009: LWJGL 3.4.2 publicado, no el SNAPSHOT
# (el SNAPSHOT ya no está en los repos y rompe la resolución del backend SDL3).
edit(ROOT / "Arc" / "build.gradle", [
    ('versions.lwjgl = "3.4.2-SNAPSHOT"', 'versions.lwjgl = "3.4.2"'),
])

edit(WORK / "build.gradle", [
    (
        'return "com.github.TinyLake.MindustryX:$name:${rootProject.version}"',
        'return "com.github.Anuken:$name:release"',
    ),
])

settings = WORK / "settings.gradle"
text = settings.read_text(encoding="utf-8")
if 'includeBuild("../Arc")' not in text:
    settings.write_text(text + '\nincludeBuild("../Arc")\n', encoding="utf-8")
    print(f"[prepareArc] editado: {settings}")

edit(WORK / "tools" / "build.gradle", [
    (
        'classpath arcModule(":extensions:packer")',
        'classpath fileTree(dir: "${rootDir.parent}/Arc", '
        'include: ["**/build/libs/*.jar"], exclude: ["**/*-sources.jar", "**/*-javadoc.jar"])',
    ),
    ('classpath arcModule(":arc-core")\n', ''),
])

print("[prepareArc] listo")
