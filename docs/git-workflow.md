# Flujo de trabajo recomendado

## Ramas base

- `main`: solo versiones estables listas para entrega o demostracion.
- `develop`: rama de integracion continua del equipo.
- `feat/...`: una funcionalidad concreta por rama.
- `fix/...`: correcciones puntuales sin mezclar objetivos.

## Orden sugerido de ramas

1. `feat/project-base`
2. `feat/custom-structures`
3. `feat/filesystem-core`
4. `feat/disk-allocation`
5. `feat/process-scheduler`
6. `feat/locks-journal`
7. `feat/gui-integration`
8. `feat/json-tests`

## Issues iniciales

1. Base del proyecto y shell de interfaz.
2. Estructuras de datos propias.
3. Arbol de directorios y archivos.
4. Disco simulado y asignacion encadenada.
5. Procesos, PCB y scheduler.
6. Locks compartidos y exclusivos.
7. Journal y recuperacion ante fallos.
8. Integracion GUI, validaciones y pruebas.

## Meta de commits por rama

- Commit 1: estructura o contrato base.
- Commit 2: implementacion principal del modulo.
- Commit 3: integracion inicial.
- Commit 4: validaciones o pruebas.
- Commit 5: limpieza final y documentacion breve.

## Ejemplo para `feat/project-base`

1. `chore: crea base maven y gitignore del proyecto`
2. `feat: agrega punto de entrada y ventana principal`
3. `feat: monta paneles base de la interfaz`
4. `docs: documenta flujo de ramas e issues`
5. `test: valida compilacion inicial del proyecto`

## Regla de integracion

- Crear issue.
- Crear rama desde `develop`.
- Hacer entre 3 y 5 commits pequenos.
- Abrir PR hacia `develop`.
- Revisar y fusionar.
- Pasar a `main` solo cuando el modulo este estable.
