package it.generation.suonacongigi.dto.emailTemplate;

import jakarta.validation.constraints.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResponse {

    // Il campo "sender" rappresenta il mittente della mail.
    @Schema
    private String sender;

    // Il campo "subject" rappresenta l'oggetto della mail.
    @Schema
    private String subject;

    // Il campo "content" rappresenta il testo della mail.
    @Schema(description = "Contenuto della mail", example = "Benvenuto in Suona con Gigi")
    @NotBlank
    private String content;

}