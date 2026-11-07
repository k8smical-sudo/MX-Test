# Parche pendiente de portar: 0073 (combinaciones de modificadores en atajos)

Este parche NO se aplica (el script solo lee patches/client, patches/openworld y patches/picked).

Motivo: depende de APIs de Arc que se quitaron al volver a Arc upstream (Axis con modificadores, Input.modifiers(), KeyBind.modifierExact, matchModifiers). Solo lo usa este parche.

Para portarlo: guardar los modificadores fuera de Arc (un mapa en el motor, persistido como "<nombre>-mods") y leer las teclas con Core.input.keyDown(KeyCode.controlLeft/controlRight, shiftLeft/shiftRight, altLeft/altRight). Después se regenera el parche con genPatches.sh y se mueve a patches/client/.
