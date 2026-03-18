# Guia de contribucion

Este proyecto sigue obligatoriamente las reglas del enunciado de Sistemas Operativos.

## Flujo de ramas

1. `main` solo guarda versiones estables.
2. `develop` es la rama de integracion.
3. Cada funcionalidad sale en una rama `feat/...`.
4. Cada correccion puntual sale en una rama `fix/...`.
5. Todo merge de trabajo diario ocurre hacia `develop`.

## Commits por branch

- Cada branch debe cerrar con entre `3` y `5` commits.
- Los mensajes deben ser descriptivos y pequenos.
- No mezclar varios modulos en una sola rama.

## Flujo obligatorio

1. Crear issue en GitHub.
2. Crear branch desde `develop`.
3. Implementar por fases.
4. Abrir Pull Request hacia `develop`.
5. Revisar y comentar el PR.
6. Hacer merge solo cuando compile y este acotado.

## Regla para el equipo

- Ambos integrantes deben participar de manera equilibrada.
- Antes de cerrar una rama, ambos deben entender la implementacion.
- No usar `ArrayList`, `Queue`, `Stack`, `Vector` ni colecciones del framework de Java.

## Ramas planeadas

1. `feat/project-base`
2. `feat/custom-structures`
3. `feat/filesystem-core`
4. `feat/disk-allocation`
5. `feat/process-scheduler`
6. `feat/locks-journal`
7. `feat/gui-integration`
8. `feat/json-tests`
