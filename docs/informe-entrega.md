# Informe Proyecto 2

## 1. Descripción general

El proyecto consiste en un simulador de sistema de archivos con interfaz gráfica en Java Swing. La aplicación permite crear, renombrar y eliminar archivos y directorios, visualizar la asignación de bloques en disco, planificar solicitudes de E/S con distintos algoritmos y registrar operaciones mediante journal para simular recuperación ante fallos.

La solución fue desarrollada con una arquitectura modular para separar interfaz, lógica del sistema de archivos, simulación de disco, planificación de E/S, control de locks y persistencia en JSON. Para que las políticas del scheduler pudieran demostrarse correctamente dentro del simulador, se separó la posición lógica de E/S del bloque físico usado por la asignación encadenada.

## 2. Arquitectura general

Los módulos principales del proyecto son:

- `ui`: contiene la interfaz gráfica y los paneles de visualización.
- `simulation`: coordina toda la lógica del simulador.
- `filesystem`: modela archivos, directorios y navegación por rutas.
- `disk`: representa el disco y la asignación encadenada de bloques.
- `scheduler`: implementa FIFO, SSTF, SCAN y C-SCAN.
- `process`: modela procesos de E/S, solicitudes y estados.
- `locks`: administra locks compartidos y exclusivos.
- `journal`: registra operaciones pendientes y committed para recovery.
- `persistence`: guarda y carga el estado del simulador en JSON.
- `testcase`: carga casos de prueba JSON con el formato sugerido en el enunciado.
- `structures`: implementa estructuras propias como lista, cola y pila.

La clase central del proyecto es `SimulationController`, porque conecta la GUI con el modelo interno y ejecuta el flujo completo de cada operación.

## 3. Métodos y clases más importantes

### `SimulationController`

- `createFile`, `createDirectory`, `renameNode`, `deleteNode`: reciben la operación solicitada desde la GUI.
- `queueRead(...)`: crea procesos de lectura reales con lock compartido.
- `enqueueTask(...)`: crea el proceso de E/S, lo registra en historial y lo inserta en la cola de tareas pendientes.
- `startScheduler()`, `pauseScheduler()`, `resumeScheduler()`, `interruptCurrentProcess()`: controlan el hilo worker del scheduler.
- `simulateFailedCreate(...)`: genera una creación incompleta para demostrar el recovery del journal.
- `saveToJson(...)` y `loadFromJson(...)`: serializan y restauran el estado del simulador.
- `loadTestCase(...)`: carga los casos JSON del enunciado y construye la cola de procesos con sus posiciones lógicas exactas.

### `FileSystemTree`

- `findNode(String path)`: recorre el árbol desde la raíz y ubica un nodo por ruta absoluta.
- `createDirectory(...)` y `createFile(...)`: agregan nodos al directorio correspondiente.
- `renameNode(...)`: cambia el nombre de un nodo validando conflictos.
- `removeNode(...)`: elimina archivos o directorios del árbol.

### `ChainedAllocationManager`

- `allocateFile(FileNode file)`: selecciona bloques libres y construye la cadena enlazada del archivo.
- `releaseFile(FileNode file)`: libera todos los bloques de un archivo recorriendo su cadena.
- `getAllocatedBlocks(FileNode file)`: devuelve los bloques asignados para mostrar la distribución en disco y registrar journal.

En esta implementación cada archivo mantiene dos referencias distintas:

- `firstBlockIndex`: primer bloque físico dentro del disco simulado.
- `ioPosition`: posición lógica usada por el scheduler para ordenar solicitudes de E/S.

### `DiskScheduler`

- `order(...)`: delega la planificación a la estrategia activa.
- `FifoSchedulingStrategy`: conserva el orden de llegada.
- `SstfSchedulingStrategy`: selecciona siempre la solicitud más cercana al cabezal actual.
- `ScanSchedulingStrategy`: atiende en una dirección y luego invierte el barrido.
- `CScanSchedulingStrategy`: atiende en una sola dirección y luego reinicia el recorrido lógico.

### `LockManager`

- `acquireLock(...)`: concede un lock si es compatible; si no, mueve el proceso a `BLOCKED`.
- `releaseLocksByProcess(int pid)`: libera locks al terminar un proceso y despierta solicitudes en espera.

### `JournalManager`

- `beginCreate(...)` y `beginDelete(...)`: crean entradas `PENDING`.
- `markCommitted(...)`: marca una transacción como confirmada.
- `recoverPending(...)`: revierte operaciones incompletas y deja el journal en estado `UNDONE`.

### `SimulationStateRepository`

- `save(...)`: construye manualmente el JSON del estado actual.
- `load(...)`: reconstruye el estado leyendo modo, política, cabezal, usuario actual, directorios y archivos.

### `TestCaseRepository`

- `load(...)`: interpreta el JSON de casos de prueba del enunciado y transforma sus requests en procesos de E/S ejecutables por el simulador.

## 4. Lógica de procesos y transición de estados

Cada operación del usuario se convierte en un proceso de E/S representado por un `ProcessControlBlock`. Los estados implementados son:

- `NEW`: proceso recién creado.
- `READY`: listo para ser planificado.
- `RUNNING`: operación en ejecución.
- `BLOCKED`: esperando un lock incompatible.
- `TERMINATED`: operación finalizada.

Flujo general:

1. La GUI solicita una operación.
2. `SimulationController` crea el `PCB` y lo agrega a la cola.
3. El hilo worker del scheduler promueve el proceso de `NEW` a `READY`.
4. El scheduler selecciona el próximo proceso según la política activa y la `ioPosition` solicitada.
5. Si el lock no puede concederse, el proceso pasa a `BLOCKED`.
6. Si el lock se concede, el proceso pasa a `RUNNING`.
7. Al terminar, el proceso pasa a `TERMINATED` y se liberan los locks.
8. Los procesos despertados por liberación de locks vuelven a `READY`.

Además del estado del proceso, el journal maneja una transición propia:

- `PENDING`: operación iniciada pero no confirmada.
- `COMMITTED`: operación aplicada correctamente.
- `UNDONE`: operación revertida durante recovery.

## 5. Manejo de interrupciones e hilos

La interfaz gráfica se inicia correctamente en el hilo de eventos de Swing mediante `SwingUtilities.invokeLater`, lo cual evita problemas al construir la ventana principal.

La simulación de E/S sí utiliza un hilo dedicado para el scheduler. La GUI solo encola solicitudes y el worker procesa la cola en segundo plano. El flujo es el siguiente:

1. el usuario crea una solicitud desde la interfaz,
2. `SimulationController` genera el `PCB` y lo coloca en la cola,
3. el worker despierta con `wait()/notifyAll()`,
4. el scheduler ordena los procesos listos según la política activa,
5. se intenta adquirir el lock correspondiente,
6. la operación se ejecuta con checkpoints mínimos,
7. al finalizar se liberan locks y se despiertan procesos bloqueados.

Las interrupciones visibles del sistema son:

- pausa del scheduler,
- reanudación del scheduler,
- interrupción manual del proceso actual,
- bloqueo por lock incompatible,
- falla simulada en `CREATE`.

Para mantener el alcance controlado, el worker utiliza checkpoints simples:

- antes de lock,
- antes de ejecutar,
- antes de commit,
- después de commit.

Si una operación crítica es interrumpida antes del commit, el journal ejecuta recovery y deja evidencia en el log y en el estado `UNDONE`.

## 6. Interfaz gráfica y validaciones

La GUI fue desarrollada con Swing y permite visualizar:

- árbol de directorios y archivos,
- bloques del disco,
- tabla de asignación,
- historial de procesos,
- locks activos,
- log de eventos,
- journal de operaciones,
- posición lógica de E/S del archivo seleccionado.

Además, la interfaz incluye controles mínimos para:

- iniciar, pausar y reanudar el scheduler,
- interrumpir el proceso actual,
- cambiar de modo,
- cambiar el usuario activo,
- leer un archivo seleccionado,
- cargar estado JSON,
- cargar casos de prueba JSON.

Las validaciones principales incluyen:

- verificación de rangos para la posición del cabezal,
- validación numérica y positiva para tamaño de archivos,
- restricción de operaciones de escritura al modo `ADMINISTRADOR`,
- filtrado del árbol cuando el modo es `USUARIO`,
- validación de permisos de lectura según propietario y visibilidad,
- manejo de rutas inválidas,
- prevención de nombres duplicados,
- control de errores en carga y guardado de JSON,
- control de errores en carga de casos de prueba.

Cuando una entrada es inválida, la aplicación muestra un mensaje de error y mantiene el flujo del simulador sin cerrarse.

En modo `USUARIO`, el árbol mostrado se filtra para dejar visibles solo archivos propios, archivos públicos y la estructura mínima necesaria del sistema. Las acciones de escritura quedan reservadas al modo `ADMINISTRADOR`.

## 7. JSON soportados

El proyecto maneja dos tipos de JSON distintos:

1. Persistencia del simulador:
   guarda modo, política, cabezal, usuario actual, directorios, archivos, bloques e `ioPosition`.
2. Casos de prueba del enunciado:
   carga `test_id`, `initial_head`, `requests` y `system_files` para construir la cola de procesos de prueba.

## 8. Análisis comparativo de algoritmos

Para comparar los algoritmos se utilizó el caso de prueba del proyecto con cabezal inicial en `50` y solicitudes en:

`95, 180, 34, 119, 11, 123, 62, 64`

Resultados obtenidos en esta implementación:

| Algoritmo | Orden de atención | Movimiento total |
| --- | --- | ---: |
| FIFO | 95, 180, 34, 119, 11, 123, 62, 64 | 644 |
| SSTF | 62, 64, 34, 11, 95, 119, 123, 180 | 236 |
| SCAN | 62, 64, 95, 119, 123, 180, 34, 11 | 299 |
| C-SCAN | 62, 64, 95, 119, 123, 180, 11, 34 | 322 |

Nota: estos valores corresponden exactamente a la lógica implementada en el simulador. En SCAN y C-SCAN se evalúa el orden de atención de solicitudes, pero no se agrega recorrido extra hasta extremos vacíos del disco si no hay solicitudes en esos puntos.

Conclusiones:

- `FIFO` es el algoritmo más simple, pero también el menos eficiente en este caso, porque ignora la cercanía entre solicitudes.
- `SSTF` ofrece el mejor resultado en movimiento total del cabezal y reduce el tiempo de búsqueda promedio.
- `SCAN` tiene un comportamiento más estable que FIFO y evita cambios bruscos constantes, aunque aquí mueve más que SSTF.
- `C-SCAN` mantiene una política más uniforme para sistemas con carga continua, pero en este caso puntual tuvo un costo ligeramente mayor que SCAN.

En este conjunto de datos, el mejor rendimiento fue de `SSTF`, mientras que `FIFO` fue el peor. Aun así, SCAN y C-SCAN siguen siendo alternativas importantes porque ofrecen mayor previsibilidad cuando el sistema tiene muchas solicitudes distribuidas a lo largo del disco.

## 9. Pruebas realizadas

Se ejecutaron pruebas básicas para verificar:

- estructuras propias,
- creación y navegación en el árbol de archivos,
- asignación y liberación encadenada,
- orden de planificación del scheduler,
- carga integrada de P1-P4 desde JSON,
- pausa, reanudación e interrupción del worker,
- locks y desbloqueo de procesos,
- journal y recovery,
- persistencia JSON,
- integración del controlador principal.

Estas pruebas permitieron validar tanto la lógica de los módulos por separado como el flujo integrado del simulador.

## 10. Conclusión

El proyecto cumple con el objetivo de simular un sistema de archivos con planificación de E/S, asignación encadenada, control de concurrencia, journal y recuperación ante fallos, todo dentro de una interfaz gráfica funcional.

La implementación se enfocó en separar responsabilidades, usar estructuras propias y representar de forma clara la transición de estados del sistema. El resultado es un simulador defendible académicamente, porque ahora puede demostrar el orden real de las políticas del scheduler dentro de la propia aplicación, diferenciar entre posición lógica de E/S y bloque físico, y evidenciar hilos, locks, permisos y recovery en tiempo real.
