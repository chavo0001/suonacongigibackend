package it.generation.suonacongigi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "SuonaConGigi API",
        version     = "1.0",
        description = "Documentazione interattiva delle API per la piattaforma musicale SuonaConGigi. " +
                      "Include gestione utenti, eventi live e forum della community.",
        contact     = @Contact(
                        name = "Supporto Didattico SuonaConGigi - Marco Ratto",
                        email = "marco.ratto@wennove.it"
                    )
    ),
    /* * MECCANICA: Sicurezza Globale.
     * Aggiungendo questo requisito qui, istruiamo Swagger ad applicare il token JWT 
     * a TUTTI gli endpoint, risolvendo il problema dei 403 dovuti alla mancanza 
     * dell'header Authorization negli endpoint non annotati esplicitamente.
     */
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name         = "bearerAuth",
    type         = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme       = "bearer",
    description  = "Inserisci il token JWT ottenuto dal login per accedere agli endpoint protetti."
)
public class OpenApiConfig {
    // Non serve logica interna, le annotazioni configurano il Bean automaticamente
}