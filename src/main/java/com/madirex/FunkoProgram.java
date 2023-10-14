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

        Mono<Void> loadMono = Mono.fromRunnable(() -> loadFunkosFileAndInsertToDatabase("data" + File.separator + "funkos.csv"));
        loadMono.subscribe();

        Mono<Void> serviceExceptionMono = callAllServiceExceptionMethods();
        serviceExceptionMono.subscribe();

        Mono<Void> serviceMono = callAllServiceMethods();
        serviceMono.subscribe();

        Mono<Void> queriesMono = databaseQueries();
        queriesMono.subscribe();

        Mono<Void> allOperationsMono = Mono.when(loadMono, serviceExceptionMono, serviceMono, queriesMono);

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

        Mono<Void> s1 = printFindById("569689dd-b76b-465b-aa32-a6c46acd38fd", false).then();
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
        Mono<Void> s2 = printFindById("3b6c6f58-7c6b-434b-82ab-01b2d6e4434a", true).then();
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
        return controller.importData(System.getProperty("user.dir") + File.separator + rootFolderName, "backup.json")
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
        var q1 = printExpensiveFunko().then();
        var q2 = printAvgPriceOfFunkos().then();
        var q3 = printFunkosGroupedByModels().then();
        var q4 = printNumberOfFunkosByModels().then();
        var q5 = printFunkosReleasedIn(2023).then();
        var q6 = printNumberOfFunkosOfName("Stitch").then();
        var q7 = printListOfFunkosOfName("Stitch").then();
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
    private Flux<Object> printFunkosReleasedIn(int year) {
        try {
            return controller.findAll()
                    .flatMap(funko -> {
                        if (funko.getReleaseDate().getYear() == year) {
                            logger.info("🔵 Funko lanzado en el año " + year + ": " + funko.toString());
                        }
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
            return Flux.empty();
        } catch (FunkoNotFoundException e) {
            String str = "Funkos no encontrados: " + e;
            logger.error(str);
            return Flux.empty();
        }
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
                            String cod = funko.getCod().toString();
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
                                            String strError = "No se ha eliminado el Funko con id " + cod + " -> " + ex;
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
                                return controller.update(funko.getCod().toString(),
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
            return controller.exportData(System.getProperty("user.dir") + File.separator + rootFolderName, "backup.json")
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
    private Mono<Object> printSave(Funko funko, boolean isCorrect) {
        try {
            return controller.save(funko)
                    .flatMap(savedFunko -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de Save...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de Save...");
                        }
                        logger.info("\nSave:");
                        if (savedFunko != null) {
                            logger.info(savedFunko.toString());
                        } else {
                            logger.info("No se ha guardado el Funko.");
                        }
                        return Mono.empty();
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
    private Mono<Object> printFindById(String id, boolean isCorrect) {
        try {
            return controller.findById(id)
                    .flatMap(foundFunko -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de FindById...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de FindById...");
                        }
                        logger.info("\nFind by Id:");
                        logger.info(foundFunko.toString()); // Asumiendo que foundFunko es un Funko
                        return Mono.empty();
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
    private Mono<Object> printFindByName(String name, boolean isCorrect) {
        try {
            return controller.findByName(name)
                    .collectList()
                    .flatMap(foundFunkos -> {
                        if (isCorrect) {
                            logger.info("🟢 Probando caso correcto de FindByName...");
                        } else {
                            logger.info("🔴 Probando caso incorrecto de FindByName...");
                        }
                        logger.info("\nFind by Name:");
                        foundFunkos.forEach(funko -> logger.info(funko.toString()));
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String str = "Funkos no encontrados: " + e;
                        logger.error(str);
                        return Mono.empty();
                    });
        } catch (FunkoNotFoundException e) {
            String strError = "No se ha encontrado el Funko: " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        }
    }


    /**
     * Imprime todos los Funkos
     *
     * @return Datos
     */
    private Mono<Object> printFindAll() {
        try {
            return controller.findAll()
                    .collectList()
                    .flatMap(foundFunkos -> {
                        logger.info("🟢 Probando caso correcto de FindAll...");
                        logger.info("\nFind All:");
                        foundFunkos.forEach(funko -> logger.info(funko.toString()));
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        String strError = "No se han encontrado Funkos: " + e;
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
     * Lee un archivo CSV y lo inserta en la base de datos de manera asíncrona
     *
     * @param path Ruta del archivo CSV
     * @return Datos
     */
    public Mono<Void> loadFunkosFileAndInsertToDatabase(String path) {
        CsvManager csvManager = CsvManager.getInstance();

        try {
            return csvManager.fileToFunkoList(path)
                    .collectList()
                    .flatMap(funkoList -> Flux.fromIterable(funkoList)
                            .flatMap(funko -> {
                                try {
                                    return controller.save(funko);
                                } catch (SQLException throwables) {
                                    String strError = "Error: " + throwables;
                                    logger.error(strError);
                                    return Mono.empty();
                                } catch (FunkoNotValidException | FunkoNotSavedException ex) {
                                    String strError = "El Funko no es válido: " + ex;
                                    logger.error(strError);
                                    return Mono.empty();
                                }
                            })
                            .then()
                            .onErrorResume(e -> {
                                logger.error("Error al insertar los datos en la base de datos: " + e.getMessage());
                                return Mono.empty();
                            })
                            .then()
                    );
        } catch (ReadCSVFailException e) {
            String strError = "No se ha podido leer el CSV -> " + e.getMessage();
            logger.error(strError);
            return Mono.empty();
        }
    }

}
