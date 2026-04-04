package com.giovanildo.torneiofds.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tab_premio")
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "torneio_id")
    private Torneio torneio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "eatleta_id")
    private EAtleta eAtleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clube_id")
    private Clube clube;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPremio tipo;

    /** Dados do momento da premiacao */
    private int pontos;
    private int golsPro;
    private int golsContra;
    private int vitorias;
    private int derrotas;

    public Premio(Torneio torneio, EAtleta eAtleta, Clube clube, TipoPremio tipo) {
        this.torneio = torneio;
        this.eAtleta = eAtleta;
        this.clube = clube;
        this.tipo = tipo;
    }
}
