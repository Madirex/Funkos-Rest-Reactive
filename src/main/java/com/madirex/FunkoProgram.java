package com.madirex;

import com.madirex.controllers.FunkoController;
import com.madirex.exceptions.*;
import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.repositories.funko.FunkoRepositoryImpl;
import com.madirex.services.cache.FunkoCacheImpl;
import com.madirex.services.crud.funko.FunkoServiceImpl;
import com.madirex.services.crud.funko.IdGenerator;
import com.madirex.services.database.DatabaseManager;
import com.madirex.services.io.BackupService;
import com.madirex.services.io.CsvManager;
import com.madirex.services.notifications.FunkoNotificationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
                        new FunkoCacheImpl(15, 90),
                        BackupService.getInstance(), FunkoNotificationImpl.getInstance()));
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
        var loadAndUploadFunkos = loadFunkosFileAndInsertToDatabase("data" + File.separator + "funkos.csv");
        loadAndUploadFunkos.block();
        Mono<Void> serviceExceptionMono = callAllServiceExceptionMethods();
        Mono<Void> serviceMono = callAllServiceMethods();
        Mono<Void> queriesMono = databaseQueries();
        Mono<Void> allOperationsMono = Mono.when(serviceExceptionMono, serviceMono, queriesMono);
        allOperationsMono.doFinally(signalType -> {
            controller.shutdown();
            logger.info("Programa de Funkos finalizado.");
        }).subscribe();
    }

    /**
     * Lanzar excepciones de los métodos service
     *
     * @return Devuelve los datos
     */
    private Mono<Void> callAllServiceExceptionMethods() {
        logger.info("🔴 Probando casos incorrectos 🔴");

        Mono<Void> s1 = printFindById(UUID.fromString("569689dd-b76b-465b-aa32-a6c46acd38fd"), false).then();
        Mono<Void> s2 = printFindByName("NoExiste", false).then();

        Funko funko = Funko.builder()
                .name("MadiFunko2")
                .model(Model.OTROS)
                .price(-42)
                .releaseDate(LocalDate.now())
                .build();

        Mono<Void> s3 = printSave(funko, false).then();
        Mono<Void> s4 = printUpdate("One Piece Luffy", "", false).then();
        Mono<Void> s5 = printDelete("NoExiste", false).then();

        return Mono.when(s1, s2, s3, s4, s5);
    }


    /**
     * Llama a todos los métodos de la clase FunkoService
     *
     * @return Devuelve los datos
     */
    private Mono<Void> callAllServiceMethods() {
        logger.info("🟢 Probando casos correctos 🟢");

        Mono<Void> s1 = printFindAll().then();
        Mono<Void> s2 = printFindById(UUID.fromString("3b6c6f58-7c6b-434b-82ab-01b2d6e4434a"), true).then();
        Mono<Void> s3 = printFindByName("Doctor Who Tardis", true).then();

        Funko funko = Funko.builder()
                .name("MadiFunko")
                .model(Model.OTROS)
                .price(42)
                .releaseDate(LocalDate.now())
                .build();

        Mono<Void> s4 = printSave(funko, true).then();
        Mono<Void> s5 = printUpdate("MadiFunko", "MadiFunkoModified", true).then();
        Mono<Void> s6 = printDelete("MadiFunkoModified", true).then();
        Mono<Void> s7 = doBackupAndPrint("data").then();
        Mono<Void> s8 = loadBackupAndPrint("data").then();

        return Mono.when(s1, s2, s3, s4, s5, s6, s7, s8);
    }

    /**
     * Carga una copia de seguridad y la imprime
     *
     * @param rootFolderName Nombre de la carpeta raíz
     * @return Devuelve los datos
     */
    private Mono<Void> loadBackupAndPrint(String rootFolderName) {
        var imported = controller.importData(System.getProperty("user.dir") + File.separator +
                rootFolderName, "backup.json");
        imported.then().block();
        return imported
                .doOnNext(funko -> {
                    logger.info("🟢 Copia de seguridad...");
                    logger.info(funko.toString());
                })
                .then();
    }

    /**
     * Consultas a la base de datos
     *
     * @return Devuelve los datos
     */
    private Mono<Void> databaseQueries() {
        var q1 = printExpensiveFunko();
        var q2 = printAvgPriceOfFunkos();
        var q3 = printFunkosGroupedByModels();
        var q4 = printNumberOfFunkosByModels();
        var q5 = printFunkosReleasedIn(2023);
        var q6 = printNumberOfFunkosOfName("Stitch");
        var q7 = printListOfFunkosOfName("Stitch");
        return Mono.when(q1, q2, q3, q4, q5, q6, q7);
    }

    /**
     * Imprime una lista de Funkos que contengan el nombre pasado por parámetro
     *
     * @param name Nombre del Funko
     * @return Devuelve los datos
     */
    private Mono<Funko> printListOfFunkosOfName(String name) {
        try {
            return controller.findByName(name)
                    .next()
                    .doOnSuccess(funko -> {
                        if (funko != null) {
                            logger.info("🔵 Funko encontrado: " + funko);
                        } else {
                            logger.info("🔵 No se encontró un Funko con el nombre: " + name);
                        }
                    })
                    .onErrorResume(e -> {
                        String str = "Error al buscar el Funko: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (FunkoNotFoundException e) {
            logger.info("🔵 No se encontró un Funko con el nombre: " + name);
            return Mono.empty();
        }
    }

    private Mono<Object> printNumberOfFunkosOfName(String name) {
        try {
            return controller.findAll()
                    .collectList()
                    .flatMap(funkos -> {
                        long count = funkos.stream()
                                .filter(f -> f.getName().startsWith(name))
                                .count();
                        logger.info("🔵 Número de Funkos de " + name + ": " + count);
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String str = "Funkos no encontrados: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String str = "ERROR SQL: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Mono.empty();
        }
    }

    /**
     * Imprime los Funkos lanzados en year
     *
     * @param year Año
     * @return Devuelve los datos
     */
    private Flux<Funko> printFunkosReleasedIn(int year) {
        return Flux.defer(() -> {
                    try {
                        return controller.findAll();
                    } catch (SQLException e) {
                        String str = "ERROR SQL: " + e;
                        logger.error(str);
                        return Flux.empty();
                    } catch (FunkoNotFoundException e) {
                        String str = "Funkos no encontrados: " + e;
                        logger.error(str);
                        return Flux.empty();
                    }
                })
                .filter(funko -> funko.getReleaseDate().getYear() == year)
                .doOnNext(filteredFunko -> {
                    logger.info("🔵 Funko lanzado en el año " + year + ": " + filteredFunko.toString());
                })
                .onErrorResume(e -> {
                    String str = "Funkos no encontrados: " + e;
                    logger.error(str);
                    return Mono.empty();
                });
    }


    /**
     * Imprime el número de Funkos por modelo
     *
     * @return Devuelve los datos
     */
    private Mono<Object> printNumberOfFunkosByModels() {
        try {
            return controller.findAll()
                    .collectMultimap(Funko::getModel, funko -> 1)
                    .flatMap(modelsMap -> {
                        logger.info("🔵 Número de Funkos por modelos...");
                        modelsMap.forEach((model, count) -> {
                            String str = "🔵 " + model + " -> " + count.size();
                            logger.info(str);
                        });
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String str = "Funkos no agrupados: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String str = "ERROR SQL: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Mono.empty();
        }
    }

    /**
     * Imprime los Funkos agrupados por modelos
     *
     * @return Datos
     */
    private Mono<Object> printFunkosGroupedByModels() {
        try {
            return controller.findAll()
                    .collectMultimap(Funko::getModel, funko -> funko)
                    .flatMap(modelToFunKoMap -> {
                        logger.info("🔵 Funkos agrupados por modelos...");

                        modelToFunKoMap.forEach((model, funkoList) -> {
                            String str = "\n🔵 Modelo: " + model;
                            logger.info(str);
                            funkoList.forEach(funko -> logger.info(funko.toString()));
                        });

                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String str = "Funkos no agrupados: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String str = "ERROR SQL: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Mono.empty();
        }
    }


    /**
     * Imprime la media de precio de los Funkos
     *
     * @return Datos
     */
    private Mono<Object> printAvgPriceOfFunkos() {
        try {
            return controller.findAll()
                    .collectList()
                    .flatMap(funkos -> {
                        logger.info("🔵 Media de precio de Funkos...");

                        double averagePrice = funkos.stream()
                                .mapToDouble(Funko::getPrice)
                                .average()
                                .orElse(0.0); // Si no hay Funkos, establecemos un valor por defecto

                        logger.info(String.format("%.2f", averagePrice));

                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String str = "Fallo al calcular la media de precio de Funkos: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String str = "ERROR SQL: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Mono.empty();
        }
    }

    /**
     * Imprime el Funko más caro
     *
     * @return Datos
     */
    private Mono<Object> printExpensiveFunko() {
        try {
            return controller.findAll()
                    .collectList()
                    .flatMap(funkos -> {
                        logger.info("🔵 Funko más caro...");

                        Funko expensiveFunko = funkos.stream()
                                .max(Comparator.comparingDouble(Funko::getPrice))
                                .orElse(null);

                        if (expensiveFunko != null) {
                            logger.info(expensiveFunko.toString());
                        } else {
                            logger.info("No se encontró el Funko más caro.");
                        }

                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String str = "Fallo al encontrar el Funko más caro: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String str = "ERROR SQL: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Mono.empty();
        }
    }

    /**
     * Elimina un Funko y lo imprime
     *
     * @param name      Nombre del Funko
     * @param isCorrect Si es un caso correcto
     * @return Datos
     */
    private Flux<Object> printDelete(String name, boolean isCorrect) {
        try {
            return controller.findByName(name)
                    .flatMap(funko -> {
                        if (funko != null) {
                            var cod = funko.getCod();
                            try {
                                return controller.delete(cod)
                                        .flatMap(deletedFunko -> {
                                            if (isCorrect) {
                                                logger.info("🟢 Probando caso correcto de Delete...");
                                            } else {
                                                logger.info("🔴 Probando caso incorrecto de Delete...");
                                            }
                                            logger.info("\nDelete:");
                                            if (deletedFunko != null) {
                                                String str = "Funko eliminado: " + deletedFunko;
                                                logger.info(str);
                                            } else {
                                                logger.info("No se ha eliminado el Funko.");
                                            }
                                            return Mono.empty();
                                        })
                                        .onErrorResume(ex -> {
                                            String strError = "No se ha eliminado el Funko con id " + cod.toString() + " -> " + ex;
                                            logger.error(strError);
                                            return Mono.empty();
                                        });
                            } catch (SQLException e) {
                                String str = "ERROR SQL: " + e;
                                logger.error(str);
                                return Mono.empty();
                            } catch (FunkoNotRemovedException e) {
                                String str = "Funko no eliminado: " + e;
                                logger.error(str);
                                return Mono.empty();
                            }
                        } else {
                            logger.info("No se ha encontrado el Funko.");
                            return Mono.empty();
                        }
                    })
                    .onErrorResume(e -> {
                        String strError = "No se ha encontrado el Funko con nombre " + name + " -> " + e;
                        logger.error(strError);
                        return Mono.empty();
                    });
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Flux.empty();
        }
    }


    /**
     * Actualiza el nombre de un Funko y lo imprime
     *
     * @param name      Nombre del Funko
     * @param newName   Nuevo nombre del Funko
     * @param isCorrect Si es un caso correcto
     * @return Datos
     * @throws SQLException Excepción SQL
     */
    private Flux<Object> printUpdate(String name, String newName, boolean isCorrect) {
        try {
            return controller.findByName(name)
                    .flatMap(funko -> {
                        if (funko != null) {
                            try {
                                return controller.update(funko.getCod(),
                                                Funko.builder()
                                                        .name(newName)
                                                        .model(Model.DISNEY)
                                                        .price(42.42)
                                                        .releaseDate(LocalDate.now())
                                                        .build())
                                        .map(updatedFunko -> {
                                            if (updatedFunko != null) {
                                                if (isCorrect) {
                                                    logger.info("🟢 Probando caso correcto de Update...");
                                                } else {
                                                    logger.info("🔴 Probando caso incorrecto de Update...");
                                                }
                                                logger.info("\nUpdate:");
                                                logger.info(updatedFunko.toString());
                                            } else {
                                                logger.info("No se ha actualizado el Funko.");
                                            }
                                            return null;
                                        })
                                        .onErrorResume(ex -> {
                                            String strError = "No se ha actualizado el Funko con nombre " + name + " -> " + ex.getMessage();
                                            logger.error(strError);
                                            return null;
                                        });
                            } catch (FunkoNotValidException e) {
                                String str = "El Funko no es válido: " + e;
                                logger.error(str);
                            } catch (SQLException e) {
                                String str = "Fallo SQL: " + e;
                                logger.error(str);
                            }
                        } else {
                            logger.info("El Funko no se ha encontrado.");
                        }
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String strError = "No se ha encontrado el Funko con nombre " + name + " -> " + e;
                        logger.error(strError);
                        return Mono.empty();
                    });
        } catch (FunkoNotFoundException e) {
            String str = "Funko no encontrado: " + e;
            logger.error(str);
            return Flux.empty();
        }
    }


    /**
     * Realiza una copia de seguridad de la base de datos y la imprime
     *
     * @param rootFolderName Nombre de la carpeta raíz
     * @return Datos
     */
    private Mono<Object> doBackupAndPrint(String rootFolderName) {
        try {
            var backup = controller.exportData(System.getProperty("user.dir") + File.separator + rootFolderName, "backup.json");
            backup.block();
            return backup
                    .then(Mono.fromRunnable(() -> {
                        logger.info("🟢 Copia de seguridad...");
                        logger.info("Copia de seguridad realizada.");
                    }))
                    .onErrorResume(e -> {
                        if (e instanceof SQLException) {
                            String strError = "Fallo SQL: " + e;
                            logger.error(strError);
                        } else if (e instanceof IOException) {
                            String strError = "Error de Input/Output: " + e;
                            logger.error(strError);
                        } else if (e instanceof FunkoNotFoundException) {
                            String strError = "Funko no encontrado: " + e;
                            logger.error(strError);
                        }
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String str = "Error SQL: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (IOException e) {
            String str = "Error IO: " + e;
            logger.error(str);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funko no encontrado: " + e;
            logger.error(str);
            return Mono.empty();
        }
    }


    /**
     * Guarda en la base de datos el Funko pasado por parámetro y lo imprime
     *
     * @param funko     Funko a imprimir
     * @param isCorrect Si es un caso correcto
     * @return Datos
     */
    private Mono<Funko> printSave(Funko funko, boolean isCorrect) {
        try {
            return controller.save(funko)
                    .doOnEach(signal -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de Save...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de Save...");
                        }

                        logger.info("\nSave:");

                        if (signal.hasValue()) {
                            logger.info(signal.get().toString());
                        } else {
                            logger.info("No se ha guardado el Funko.");
                        }
                    })
                    .onErrorResume(e -> {
                        if (e instanceof FunkoNotSavedException || e instanceof FunkoNotValidException) {
                            String strError = "No se ha podido guardar el Funko: " + e;
                            logger.error(strError);
                        }
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String strError = "Error SQL: " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        } catch (FunkoNotSavedException e) {
            String strError = "No se ha encontrado el Funko: " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        } catch (FunkoNotValidException e) {
            String strError = "Funko no válido: " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        }
    }


    /**
     * Imprime el Funko dado un ID
     *
     * @param id        Id del Funko
     * @param isCorrect Si es un caso correcto
     * @return Datos
     */
    private Mono<Funko> printFindById(UUID id, boolean isCorrect) {
        try {
            return controller.findById(id)
                    .doOnEach(signal -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de FindById...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de FindById...");
                        }
                        logger.info("\nFind by Id:");
                        if (signal.hasValue()) {
                            logger.info(signal.get().toString());
                        } else {
                            logger.info("No se encontró un Funko con el ID especificado.");
                        }
                    })
                    .onErrorResume(e -> {
                        String strError = "No se ha encontrado el Funko con id " + id + " -> " + e.getMessage();
                        logger.error(strError);
                        return Mono.empty();
                    });
        } catch (SQLException e) {
            String strError = "Error SQL: " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        } catch (FunkoNotFoundException e) {
            String strError = "No se ha encontrado el Funko: " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        }
    }


    /**
     * Imprime los Funkos que tengan el nombre pasado por parámetro
     *
     * @param name      Nombre de los Funkos
     * @param isCorrect Si es un caso correcto
     * @return Datos
     */
    private Flux<Funko> printFindByName(String name, boolean isCorrect) {
        try {
            return controller.findByName(name)
                    .doOnEach(foundFunkosSignal -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de FindByName...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de FindByName...");
                        }
                        if (foundFunkosSignal.hasValue()) {
                            logger.info("\nFind by Name:");
                            logger.info(foundFunkosSignal.get().toString());
                        } else if (foundFunkosSignal.hasError()) {
                            Throwable error = foundFunkosSignal.getThrowable();
                            logger.error("Error al buscar Funkos por nombre: " + error.getMessage());
                        }
                    })
                    .onErrorResume(e -> {
                        String str = "Funkos no encontrados: " + e;
                        logger.error(str);
                        return Flux.empty();
                    });
        } catch (FunkoNotFoundException e) {
            String strError = "No se ha encontrado el Funko: " + e.getMessage();
            logger.error(strError);
            return Flux.empty();
        }
    }


    /**
     * Imprime todos los Funkos
     *
     * @return Datos
     */
    private Flux<Funko> printFindAll() {
        logger.info("🟢 Probando caso correcto de FindAll...");
        logger.info("\nFind All:");
        try {
            return controller.findAll()
                    .doOnNext(foundFunko -> logger.info(foundFunko.toString()))
                    .onErrorResume(e -> {
                        String strError = "No se han encontrado Funkos: " + e;
                        logger.error(strError);
                        return Flux.empty();
                    });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FunkoNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lee un archivo CSV y lo inserta en la base de datos de manera asíncrona
     *
     * @param path Ruta del archivo CSV
     * @return Mono<Void>
     */
    public Mono<Void> loadFunkosFileAndInsertToDatabase(String path) {
        CsvManager csvManager = CsvManager.getInstance();

        try {
            return csvManager.fileToFunkoList(path)
                    .flatMap(funko -> {
                        try {
                            return controller.save(funko)
                                    .onErrorResume(e -> {
                                        logger.error("Error al insertar el Funko en la base de datos: " + e.getMessage());
                                        return Mono.empty();
                                    });
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        } catch (FunkoNotSavedException e) {
                            throw new RuntimeException(e);
                        } catch (FunkoNotValidException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .then();
        } catch (ReadCSVFailException e) {
            throw new RuntimeException(e);
        }
    }

}
