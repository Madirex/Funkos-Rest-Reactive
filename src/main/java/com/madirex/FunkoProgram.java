package com.madirex;

import com.madirex.controllers.FunkoController;
import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.exceptions.FunkoNotSavedException;
import com.madirex.exceptions.FunkoNotValidException;
import com.madirex.exceptions.ReadCSVFailException;
import com.madirex.models.Funko;
import com.madirex.models.Model;
import com.madirex.repositories.funko.FunkoRepositoryImpl;
import com.madirex.services.cache.FunkoCacheImpl;
import com.madirex.services.crud.funko.FunkoServiceImpl;
import com.madirex.services.crud.funko.IdGenerator;
import com.madirex.services.database.DatabaseManager;
import com.madirex.services.io.BackupService;
import com.madirex.services.io.CsvManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Clase FunkoProgram que contiene el programa principal
 */
public class FunkoProgram {

    private static FunkoProgram funkoProgramInstance;
    private final Logger logger = LoggerFactory.getLogger(FunkoProgram.class);
    private FunkoController controller;

    /**
     * Constructor privado para evitar la creación de instancia
     * SINGLETON
     */
    private FunkoProgram() {
        controller = FunkoController.getInstance(FunkoServiceImpl
                .getInstance(FunkoRepositoryImpl.getInstance(IdGenerator.getInstance(), DatabaseManager.getInstance()),
                        new FunkoCacheImpl(10, 2 * 60),
                        BackupService.getInstance()));
    }

    /**
     * SINGLETON - Este método devuelve una instancia de la clase FunkoProgram
     *
     * @return Instancia de la clase FunkoProgram
     */
    public static synchronized FunkoProgram getInstance() {
        if (funkoProgramInstance == null) {
            funkoProgramInstance = new FunkoProgram();
        }
        return funkoProgramInstance;
    }

    /**
     * Inicia el programa
     */
    public void init() {
        logger.info("Programa de Funkos iniciado.");
        CompletableFuture<Void> loadFuture = loadFunkosFileAndInsertToDatabase("data" + File.separator + "funkos.csv");
        loadFuture.join();
        CompletableFuture<Void> serviceExceptionFuture = callAllServiceExceptionMethods();
        CompletableFuture<Void> serviceFuture = callAllServiceMethods();
        CompletableFuture<Void> queriesFuture = databaseQueries();
        CompletableFuture<Void> combinedFuture = CompletableFuture
                .allOf(loadFuture, serviceExceptionFuture, serviceFuture, queriesFuture);
        combinedFuture.join();
        controller.shutdown();
        logger.info("Programa de Funkos finalizado.");
    }

    /**
     * Lanzar excepciones de los métodos service
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Void> callAllServiceExceptionMethods() {
        try {
            logger.info("🔴 Probando casos incorrectos 🔴");
            var s1 = printFindById("569689dd-b76b-465b-aa32-a6c46acd38fd", false);
            var s2 = printFindByName("NoExiste", false);
            var s3 = printSave(Funko.builder()
                    .name("MadiFunko2")
                    .model(Model.OTROS)
                    .price(-42)
                    .releaseDate(LocalDate.now())
                    .build(), false);
            var s4 = printUpdate("One Piece Luffy", "", false);
            var s5 = printDelete("NoExiste", false);
            return CompletableFuture.allOf(s1, s2, s3, s4, s5);
        } catch (SQLException e) {
            String strError = "Fallo SQL: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Llama a todos los métodos de la clase FunkoService
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Void> callAllServiceMethods() {
        try {
            logger.info("🟢 Probando casos correctos 🟢");
            var s1 = printFindAll();
            var s2 = printFindById("3b6c6f58-7c6b-434b-82ab-01b2d6e4434a", true);
            var s3 = printFindByName("Doctor Who Tardis", true);
            var s4 = printSave(Funko.builder()
                    .name("MadiFunko")
                    .model(Model.OTROS)
                    .price(42)
                    .releaseDate(LocalDate.now())
                    .build(), true);
            s4.join();
            var s5 = printUpdate("MadiFunko", "MadiFunkoModified", true);
            s5.join();
            var s6 = printDelete("MadiFunkoModified", true);
            var s7 = doBackupAndPrint("data");
            s7.join();
            var s8 = loadBackupAndPrint("data");
            return CompletableFuture.allOf(s1, s2, s3, s4, s5, s6, s7, s8);
        } catch (SQLException e) {
            String strError = "Fallo SQL: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Carga una copia de seguridad y la imprime
     *
     * @param rootFolderName Nombre de la carpeta raíz
     * @return CompletableFuture
     */
    private CompletableFuture<Void> loadBackupAndPrint(String rootFolderName) {
        return controller.importData(System.getProperty("user.dir") + File.separator + rootFolderName,
                "backup.json").thenAccept(e -> {
            logger.info("🟢 Copia de seguridad...");
            e.forEach(f -> logger.info(f.toString()));
        });
    }

    /**
     * Consultas a la base de datos
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Void> databaseQueries() {
        var q1 = printExpensiveFunko();
        var q2 = printAvgPriceOfFunkos();
        var q3 = printFunkosGroupedByModels();
        var q4 = printNumberOfFunkosByModels();
        var q5 = printFunkosReleasedIn(2023);
        var q6 = printNumberOfFunkosOfName("Stitch");
        var q7 = printListOfFunkosOfName("Stitch");
        return CompletableFuture.allOf(q1, q2, q3, q4, q5, q6, q7);
    }

    /**
     * Imprime una lista de Funkos que contengan el nombre pasado por parámetro
     *
     * @param name Nombre del Funko
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printListOfFunkosOfName(String name) {
        try {
            return controller.findAll()
                    .thenApplyAsync(funkos -> {
                        logger.info("🔵 Listado de Funkos de Stitch...");
                        funkos.stream()
                                .filter(f -> f.getName().startsWith(name))
                                .forEach(e -> logger.info(e.toString()));
                        return null;
                    });
        } catch (SQLException | FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime el número de los Funkos dado un nombre
     *
     * @param name Nombre de los Funkos
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printNumberOfFunkosOfName(String name) {
        try {
            return controller.findAll()
                    .thenApplyAsync(a -> {
                        logger.info("🔵 Número de Funkos de Stitch...");
                        logger.info(String.valueOf(a.stream().filter(e -> e.getName().startsWith(name)).count()));
                        return null;
                    });
        } catch (SQLException | FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime los Funkos lanzados en i
     *
     * @param i Año
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printFunkosReleasedIn(int i) {
        try {
            return controller.findAll()
                    .thenApplyAsync(a -> {
                        logger.info("🔵 Funkos que han sido lanzados en 2023...");
                        a.stream().filter(e -> e.getReleaseDate().getYear() == i)
                                .forEach(e -> logger.info(e.toString()));
                        return null;
                    });
        } catch (SQLException | FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime el número de Funkos por modelo
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printNumberOfFunkosByModels() {
        try {
            return controller.findAll()
                    .thenApplyAsync(a -> {
                        logger.info("🔵 Número de Funkos por modelos...");
                        a.stream().collect(Collectors.groupingBy(Funko::getModel, Collectors.counting()))
                                .forEach((model, count) -> {
                                    String str = "🔵 " + model + " -> " + count;
                                    logger.info(str);
                                });
                        return null;
                    });
        } catch (SQLException | FunkoNotFoundException e) {
            String str = "Funkos no agrupados: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime los Funkos agrupados por modelos
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printFunkosGroupedByModels() {
        try {
            return controller.findAll()
                    .thenApplyAsync(a -> {
                        logger.info("🔵 Funkos agrupados por modelos...");
                        Map<Model, List<Funko>> s = a.stream().collect(Collectors.groupingBy(Funko::getModel));
                        s.forEach((model, funkoList) -> {
                            String str = "\n🔵 Modelo: " + model;
                            logger.info(str);
                            funkoList.forEach(funko -> logger.info(funko.toString()));
                        });
                        return null;
                    });
        } catch (SQLException | FunkoNotFoundException e) {
            String str = "Funkos no agrupados: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime la media de precio de los Funkos
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printAvgPriceOfFunkos() {
        try {
            return controller.findAll()
                    .thenApplyAsync(a -> {
                        logger.info("🔵 Media de precio de Funkos...");
                        a.stream().mapToDouble(Funko::getPrice).average()
                                .ifPresent(e -> logger.info(String.format("%.2f", e)));
                        return null;
                    });
        } catch (SQLException e) {
            String str = "Fallo SQL: " + e;
            logger.error(str);
        } catch (FunkoNotFoundException e) {
            String str = "No se han encontrado Funkos: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime el Funko más caro
     *
     * @return CompletableFuture
     */
    private CompletableFuture<Object> printExpensiveFunko() {
        try {
            return controller.findAll().thenApplyAsync(a -> {
                logger.info("🔵 Funko más caro...");
                a.stream().max(Comparator.comparingDouble(Funko::getPrice)).ifPresent(e -> logger.info(e.toString()));
                return null;
            });
        } catch (SQLException e) {
            String str = "Fallo SQL: " + e;
            logger.error(str);
        } catch (FunkoNotFoundException e) {
            String str = "No se han encontrado Funkos: " + e;
            logger.error(str);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Elimina un Funko y lo imprime
     *
     * @param name      Nombre del Funko
     * @param isCorrect Si es un caso correcto
     * @return CompletableFuture
     * @throws SQLException Excepción SQL
     */
    private CompletableFuture<Object> printDelete(String name, boolean isCorrect) throws SQLException {
        try {
            return controller.findByName(name)
                    .thenApplyAsync(a -> {
                        if (a.isEmpty()) {
                            logger.info("No se ha encontrado el Funko.");
                            return null;
                        }
                        controller.delete(a.get(0).getCod().toString())
                                .thenApplyAsync(a2 -> {
                                    if (isCorrect) {
                                        logger.info("🟢 Probando caso correcto de Delete...");
                                    } else {
                                        logger.info("🔴 Probando caso incorrecto de Delete...");
                                    }
                                    logger.info("\nDelete:");
                                    a2.ifPresent(funko -> {
                                        String str = "Funko eliminado: " + funko;
                                        logger.info(str);
                                    });
                                    return null;
                                }).exceptionally(ex -> {
                                    String strError = "No se ha eliminado el Funko con id " + a.get(0).getCod().toString() + " -> " + ex.getMessage();
                                    logger.error(strError);
                                    return null;
                                })
                                .thenApply(ignored -> null);
                        return null;
                    }).exceptionally(ex -> {
                        String strError = "No se ha encontrado el Funko con nombre " + name + " -> " + ex.getMessage();
                        logger.error(strError);
                        return null;
                    })
                    .thenApply(ignored -> null);
        } catch (FunkoNotFoundException e) {
            String strError = "El Funko no se ha encontrado: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Actualiza el nombre de un Funko y lo imprime
     *
     * @param name      Nombre del Funko
     * @param newName   Nuevo nombre del Funko
     * @param isCorrect Si es un caso correcto
     * @return CompletableFuture
     * @throws SQLException Excepción SQL
     */
    private CompletableFuture<Void> printUpdate(String name, String newName, boolean isCorrect) throws SQLException {
        try {
            return controller.findByName(name)
                    .thenApplyAsync(a -> {
                        try {
                            controller.update(a.get(0).getCod().toString(),
                                    Funko.builder()
                                            .name(newName)
                                            .model(Model.DISNEY)
                                            .price(42.42)
                                            .releaseDate(LocalDate.now())
                                            .build()).thenApplyAsync(a2 -> {
                                a2.ifPresent(e -> {
                                    if (isCorrect) {
                                        logger.info("🟢 Probando caso correcto de Update...");
                                    } else {
                                        logger.info("🔴 Probando caso incorrecto de Update...");
                                    }
                                    logger.info("\nUpdate:");
                                    logger.info(e.toString());
                                });
                                return null;
                            });
                        } catch (FunkoNotValidException e) {
                            String str = "El Funko no es válido: " + e;
                            logger.error(str);
                        } catch (SQLException e) {
                            String str = "Fallo SQL: " + e;
                            logger.error(str);
                        }
                        return null;
                    }).exceptionally(ex -> {
                        String strError = "No se ha actualizado el Funko con nombre " + name + " -> " + ex.getMessage();
                        logger.error(strError);
                        return null;
                    })
                    .thenApply(ignored -> null);
        } catch (FunkoNotFoundException e) {
            String strError = "El Funko no se ha encontrado: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Realiza una copia de seguridad de la base de datos y la imprime
     *
     * @param rootFolderName Nombre de la carpeta raíz
     * @return CompletableFuture
     */
    private CompletableFuture<Void> doBackupAndPrint(String rootFolderName) {
        return CompletableFuture.runAsync(() -> {
            try {
                controller.exportData(System.getProperty("user.dir") + File.separator + rootFolderName, "backup.json")
                        .thenApplyAsync(a -> {
                            logger.info("🟢 Copia de seguridad...");
                            logger.info("Copia de seguridad realizada.");
                            return a;
                        }).join();
            } catch (SQLException e) {
                String strError = "Fallo SQL: " + e;
                logger.error(strError);
            } catch (IOException e) {
                String strError = "Error de Input/Output: " + e;
                logger.error(strError);
            } catch (FunkoNotFoundException e) {
                String strError = "Funko no encontrado: " + e;
                logger.error(strError);
            }
        });
    }

    /**
     * Guarda en la base de datos el Funko pasado por parámetro y lo imprime
     *
     * @param funko     Funko a imprimir
     * @param isCorrect Si es un caso correcto
     * @return CompletableFuture
     * @throws SQLException Excepción SQL
     */
    private CompletableFuture<Void> printSave(Funko funko, boolean isCorrect) throws SQLException {
        try {
            return controller.save(funko)
                    .thenApplyAsync(a -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de Save...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de Save...");
                        }
                        logger.info("\nSave:");
                        a.ifPresent(e -> logger.info(e.toString()));
                        return null;
                    }).exceptionally(ex -> {
                        String strError = "No se ha guardado el Funko con id " + funko.getCod().toString() + " -> " + ex.getMessage();
                        logger.error(strError);
                        return null;
                    })
                    .thenApply(ignored -> null);

        } catch (FunkoNotSavedException | FunkoNotValidException e) {
            String strError = "No se ha podido guardar el Funko: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime el Funko dado un ID
     *
     * @param id        Id del Funko
     * @param isCorrect Si es un caso correcto
     * @return CompletableFuture
     * @throws SQLException Excepción SQL
     */
    private CompletableFuture<Void> printFindById(String id, boolean isCorrect) throws SQLException {
        try {
            return controller.findById(id)
                    .thenApplyAsync(a -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de FindById...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de FindById...");
                        }
                        logger.info("\nFind by Id:");
                        a.ifPresent(e -> logger.info(e.toString()));
                        return null;
                    })
                    .exceptionally(ex -> {
                        String strError = "No se ha encontrado el Funko con id " + id + " -> " + ex.getMessage();
                        logger.error(strError);
                        return null;
                    })
                    .thenApply(ignored -> null);
        } catch (FunkoNotFoundException e) {
            String strError = "No se ha encontrado el Funko con id " + id + " -> " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime los Funkos que tengan el nombre pasado por parámetro
     *
     * @param name      Nombre de los Funkos
     * @param isCorrect Si es un caso correcto
     * @return CompletableFuture
     * @throws SQLException Excepción SQL
     */
    private CompletableFuture<Void> printFindByName(String name, boolean isCorrect) throws SQLException {
        try {
            return controller.findByName(name)
                    .thenApplyAsync(a -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de FindByName...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de FindByName...");
                        }
                        logger.info("\nFind by Name:");
                        a.forEach(e -> logger.info(e.toString()));
                        return null;
                    }).exceptionally(ex -> {
                        String strError = "No se ha encontrado el Funko con nombre " + name + " -> " + ex.getMessage();
                        logger.error(strError);
                        return null;
                    })
                    .thenApply(ignored -> null);
        } catch (FunkoNotFoundException e) {
            String strError = "No se han encontrado Funkos: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Imprime todos los Funkos
     *
     * @return CompletableFuture
     * @throws SQLException Excepción SQL
     */
    private CompletableFuture<Void> printFindAll() throws SQLException {
        try {
            return controller.findAll()
                    .thenApplyAsync(a -> {
                                logger.info("🟢 Probando caso correcto de FindAll...");
                                logger.info("\nFind All:");
                                a.forEach(e -> logger.info(e.toString()));
                                return null;
                            }
                    );
        } catch (FunkoNotFoundException e) {
            String strError = "No se han encontrado Funkos: " + e;
            logger.error(strError);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Lee un archivo CSV y lo inserta en la base de datos de manera asíncrona
     *
     * @param path Ruta del archivo CSV
     * @return CompletableFuture
     */
    public CompletableFuture<Void> loadFunkosFileAndInsertToDatabase(String path) {
        return CompletableFuture.runAsync(() -> {
            AtomicBoolean failed = new AtomicBoolean(false);
            CsvManager csvManager = CsvManager.getInstance();
            try {
                List<Funko> funkoList = csvManager.fileToFunkoList(path)
                        .thenApplyAsync(optionalFunkoList -> optionalFunkoList.orElse(Collections.emptyList()))
                        .join();
                CompletableFuture<Void> insertionFuture = CompletableFuture.allOf(
                        funkoList.stream()
                                .map(funko -> CompletableFuture.runAsync(() -> {
                                    try {
                                        controller.save(funko);
                                    } catch (SQLException throwables) {
                                        String strError = "Error: " + throwables;
                                        logger.error(strError);
                                    } catch (FunkoNotValidException | FunkoNotSavedException ex) {
                                        String strError = "El Funko no es válido: " + ex;
                                        logger.error(strError);
                                    }
                                }))
                                .toArray(CompletableFuture[]::new)
                );
                insertionFuture.join();
                if (failed.get()) {
                    logger.error("Error al insertar los datos en la base de datos");
                }
            } catch (ReadCSVFailException | RuntimeException e) {
                logger.error("Error al leer el CSV");
            }
        });
    }
}
