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

## Fases por feature

### `feat/project-base`

1. Base Maven, `.gitignore` y punto de entrada.
2. Ventana principal y enums globales de la simulacion.
3. Paneles base de la interfaz.
4. Documentacion del flujo Git y orden de trabajo.
5. Verificacion de compilacion inicial.

### `feat/custom-structures`

1. Nodo base y contratos de estructuras propias.
2. Lista enlazada simple.
3. Cola y pila propias.
4. Pruebas manuales o unitarias de operaciones basicas.
5. Ajustes de API y documentacion corta.

### `feat/filesystem-core`

1. Definir `FSNode`, `FileNode` y `DirectoryNode`.
2. Implementar arbol de directorios sin colecciones de Java.
3. CRUD logico base y recorridos.
4. Reglas de modo administrador/usuario.
5. Validaciones y pruebas de casos base.

### `feat/disk-allocation`

1. Modelo de bloque y disco simulado.
2. Asignacion encadenada para create.
3. Liberacion de bloques para delete.
4. Tabla de asignacion y estado del disco.
5. Casos de espacio insuficiente y limpieza.

### `feat/process-scheduler`

1. PCB, estados y solicitud de E/S.
2. Cola de procesos propia.
3. FIFO y conexion con operaciones.
4. SSTF, SCAN y C-SCAN.
5. Validacion con los casos del PDF.

### `feat/locks-journal`

1. Modelo de locks compartidos y exclusivos.
2. Bloqueo y desbloqueo de procesos.
3. Journal con `PENDING` y `COMMITTED`.
4. Simulacion de fallo y recuperacion `UNDO`.
5. Ajustes de consistencia y pruebas.

### `feat/gui-integration`

1. Conectar el arbol y paneles al modelo real.
2. Refrescar disco, tabla y cola de procesos.
3. Controles CRUD con validaciones.
4. Visualizacion de locks, log y journal en tiempo real.
5. Pulido visual y correccion de flujos.

### `feat/json-tests`

1. Modelo de persistencia JSON.
2. Guardado de estado del simulador.
3. Carga de estado y recuperacion al iniciar.
4. Casos P1, P2, P3, P4 y J1.
5. Datos de defensa y limpieza final.

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
