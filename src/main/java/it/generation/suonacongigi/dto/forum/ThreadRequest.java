package it.generation.suonacongigi.dto.forum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questo DTO rappresenta la richiesta di creazione o modifica di un thread del forum, 
// contenente il titolo del thread, l'id della categoria a cui appartiene e il contenuto del post iniziale del thread.
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ThreadRequest {
    // Il campo "title" rappresenta il titolo del thread. 
    // Deve essere non vuoto e avere una lunghezza compresa tra 5 e 200 caratteri.
    @Schema(description = "Titolo del thread", example = "Discussione su come migliorare la tecnica di batteria")
    @NotBlank @Size(min = 5, max = 200)
    private String title;

    // Il campo "categoryId" rappresenta l'identificativo della categoria a cui appartiene il thread. 
    // Deve essere un valore non null e positivo, che corrisponde a una categoria esistente nel sistema.
    @Schema(description = "ID della categoria a cui appartiene il thread", example = "1")
    @NotNull
    private Long categoryId;

    // Il campo "content" rappresenta il contenuto del post iniziale del thread. 
    // Deve essere non vuoto e avere una lunghezza minima di 10 caratteri, 
    // per garantire che il post abbia un contenuto significativo.
    @Schema(description = "Contenuto del post iniziale del thread", example = "Questo è il contenuto del post iniziale.")
    @NotBlank @Size(min = 10)
    private String content;  
}