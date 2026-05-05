package it.generation.suonacongigi.dto.emailTemplate;

import jakarta.validation.constraints.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {

    // Il campo "sender" rappresenta il mittente della mail.
    @Schema
    private String sender;

    // Il campo "subject" rappresenta l'oggetto della mail.
    @Schema
    private String subject;

    // Il campo "content" rappresenta il testo della mail.
    @Schema(description = "Posizione dell'evento", example = "Teatro alla Scala, Milano")
    @NotBlank
    private String content;

}