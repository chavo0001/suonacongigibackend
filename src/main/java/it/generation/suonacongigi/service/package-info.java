/**
 * ARCHITETTURA: Package-Level Null Safety Policy.
 * Utilizzando @NonNullApi, definiamo una "Costituzione" per l'intero package: 
 * ogni parametro e ogni valore di ritorno è considerato @NonNull per default.
 * * Rilevante: Questo elimina la necessità di annotare ogni singolo metodo, 
 * lasciando il codice dei Service pulito ma protetto dall'analisi statica.
 */
@org.springframework.lang.NonNullApi
package it.generation.suonacongigi.service;
