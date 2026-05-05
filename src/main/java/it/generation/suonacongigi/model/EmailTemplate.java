package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_template")  
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String subject;

    private String sender;

    @Lob
    private String content;
}